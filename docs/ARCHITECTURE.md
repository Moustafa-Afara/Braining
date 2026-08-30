# Architecture — Braining (فهم)

This document describes the system design. The master build prompt (`BRAINING.md`)
is authoritative; this expands the technical detail for the building agent.

> **READ `PROJECT_STATE.md` FIRST — it is the only file that knows what is built.**
> This one describes the *design*, and a design document ages faster than the code it describes.
> Where the two disagree, **the code wins** (`PROJECT_STATE.md` §0 rule 5).
>
> **Corrected 2026-08-17** in five places that had drifted far enough to mislead: the provider
> list, the module list (§2), the interface sketches (§3), the `Session` object (§5), and the
> status of everything Path B. Each correction is marked where it sits. Nothing was deleted for
> being unbuilt — unbuilt is now said out loud instead of implied.

## 1. High-level shape

Braining has three tiers:

1. **Android app (the product)** — captures voice, runs the clarify/forge dialogue,
   orchestrates providers, executes Path A, translates results, stores history.
2. **AI providers (cloud)** — Claude, ChatGPT, DeepSeek and **Google Gemini**, reached with
   the user's own API keys. *(Corrected: GitHub Models was replaced by Gemini in `ANSWERS.md`
   Part 1 §3, kept as a dead stub, and removed from the app entirely on 2026-08-17 —
   `ANSWERS.md` Part 8 §D1.)*
3. **PC bridge (optional, Path B)** — a lightweight local server on Windows, wrapped
   in Tailscale, that drives OpenCode headless to act on the user's files.

```
┌──────────────────────────────────────────────────────────────────────┐
│                         ANDROID APP (Braining)                         │
│                                                                        │
│  Voice ─► Transcribe ─► CLARIFY dialogue ─► FORGE English prompt ─┐     │
│   ▲                         ▲                                     │     │
│   │                         │ (asks, suggests, cautions)          ▼     │
│  Spoken                 user "idea ready"                    ROUTER     │
│  feedback                                                  (A or B?)    │
│   ▲                                                          │  │       │
│   │                                                  Path A  │  │ Path B │
│   │                                                          ▼  │       │
│   │                                            ┌─────────────┐  │       │
│   └── Arabic ◄── TRANSLATE ◄── result ◄────────│  Providers  │  │       │
│                                                └─────────────┘  │       │
│                                                                 ▼       │
└─────────────────────────────────────────────────────────┐     │       │
                                                           │     │       │
                              Tailscale private net        ▼     ▼       │
                          ┌────────────────────────────────────────┐     │
                          │      WINDOWS PC BRIDGE (Path B only)     │     │
                          │  REST/WebSocket server ─► OpenCode (headless) │
                          │  Guardrails: change-report · approval gate ·  │
                          │              working-dir confinement          │
                          └────────────────────────────────────────┘     │
```

## 2. Android modules (Clean Architecture, multi-module)

**Corrected 2026-08-17 to the modules that exist.** This list used to name modules that were
planned; four were never created, and one was overruled by a ruling. Unbuilt is now said out loud.

**Built:**

- `:app` — entry point, `NavGraph`, DI wiring, the manifest and `res/values*`.
- `:core-ui` — Compose design system, Material 3, RTL/Arabic theming, and everything two features
  share: error phrasing, the Developer Mode panel, bidirectional text, the voice panel.
- `:core-domain` — pure Kotlin: models and the interfaces in §3.
- `:core-data` — `SharedPreferences` settings and the Keystore-backed key store.
  **No Room yet** — persistence arrives with M5.
- `:ai-providers` — one implementation per vendor over a shared `BaseHttpProvider`. Four of them.
- `:speech` — Deepgram over a WebSocket, the platform recogniser as the offline fallback, and a
  router that chooses between them at call time.
- `:feature-chat` — the plain streaming chat, and the voice capture that ends as text in its input.
- `:feature-clarify` — **CLARIFY and FORGE together, in one module.**
- `:feature-settings` — keys, provider cards, Developer Mode, language, the "about me" note.
- `:build-logic/convention` — the three Gradle convention plugins.

**Planned, not built:**

- `:feature-router` — M4: Path A/B classification, model selection, fallback.
- `:feature-history` — M5: searchable history. Room lands with it, not before.
- `:bridge-client` — M6: the PC bridge client.

**Overruled — do not create:**

- `:feature-voice` — ruling M2-4 (`ANSWERS.md` Part 5) put voice capture in `:feature-chat`,
  because that flow ends as text in the chat input and a boundary drawn before M3's shape was
  known would have been a guess. The shared *components* later moved to `:core-ui` when Clarify
  became a second consumer; the module never existed.
- `:feature-forge` — nothing calls FORGE except CLARIFY and no screen shows it except CLARIFY's.
  A boundary with nothing on the other side is a cost with no purchase.

## 3. Key domain interfaces (in `:core-domain`)

**Corrected 2026-08-17.** The sketches here had drifted from the real signatures — `AiProvider`
had no `verify()` and `SpeechToText` bore no resemblance to what `:speech` implements. What
follows is the shape as built; the source is still the authority.

```kotlin
// A single AI vendor. Adding one = a class extending BaseHttpProvider + a line in ProvidersModule.
interface AiProvider {
    val id: ProviderId
    val capabilities: ProviderCapabilities
    fun complete(request: AiRequest): Flow<AiChunk>   // token-by-token streaming
    suspend fun verify(apiKey: String): AiError?      // null = the key works
}

// Transcription. `:speech` holds two engines and a router; the caller sees only this.
interface SpeechToText {
    suspend fun isAvailable(): Boolean
    fun transcribe(languageTag: String = "ar"): Flow<TranscriptionEvent>
    fun stop()
}

// The interrogation. The provider is a PARAMETER, never a constructor dependency: the user can
// change provider between two turns (ANSWERS.md Part 7 §M3-3).
interface ClarifyEngine {
    fun open(idea: String, provider: AiProvider, model: String, diagnostics: Boolean = false): Flow<ClarifyEvent>
    fun reply(text: String, provider: AiProvider, model: String, diagnostics: Boolean = false): Flow<ClarifyEvent>
    fun declareReady()          // the ONLY route to READY, and only the UI calls it
    val session: ClarifySession
}

// The English prompt.
interface PromptForge {
    val frameworks: List<FrameworkOption>
    fun forge(
        session: ClarifySession,
        provider: AiProvider,
        model: String,
        frameworkOverride: String? = null,
        diagnostics: Boolean = false,
    ): Flow<ForgeEvent>
}

// Non-secret settings. Developer Mode, the per-provider model override, the selected provider,
// and the "about me" note that CLARIFY and FORGE read (ANSWERS.md Part 8 §D3).
interface AppPreferences { /* … */ }
```

**Not built yet, and deliberately unspecified until M4 needs them:** `ModelRouter`,
`PromptFramework` as an interface (the library is data in
`feature-clarify/res/raw/prompt_frameworks.json`, not a class per framework), and `AgentProfile`.
The earlier sketches of all three were removed rather than left to be implemented from memory.

Design rule, unchanged: **adding a provider, an STT engine, or a framework must be a small,
isolated change** — no edits rippling across features.

## 4. The two execution paths in detail

### Path A — Direct API (works with PC off)
- The router classifies the request as "answerable by API alone".
- The forged English prompt is sent to the chosen provider; the response streams
  back token-by-token, is translated to Arabic, and shown.
- File outputs (documents, sheets, code) are produced **as text/content** the user
  can copy or export — no disk access on any PC required.

### Path B — PC Agent Bridge (needs PC on)
- The router classifies the request as "must act on the PC's files/disk".
- The `:bridge-client` sends a structured command to the PC bridge over Tailscale.
- The bridge runs OpenCode headless inside a confined working directory.
- On completion, the bridge returns: the result + a **full change-report** (files
  created/modified/deleted, config/system changes, and an action log).
- Destructive steps pause at an **approval gate** surfaced back in the app.
- If the PC is unreachable, the app reports "PC not connected" and offers to queue.

## 5. Session context object (context engineering)

Every request carries a single evolving `Session` object, persisted to Room.

> **Corrected 2026-08-17.** This is **M5's** object and nothing like it exists yet — what exists
> is `ClarifySession`, in memory, for the length of one interrogation (`ANSWERS.md` Part 7 §M3-4).
> And the field `originalAudioRef` was **deleted from this sketch**: it could never be filled.
> Raw audio is destroyed the moment the transcript returns, with no toggle — ruling M2-10 — and
> the streaming engine never writes a file at all. A field for a reference that cannot exist is
> how a future agent ends up building storage for audio the app promised never to keep.

```
Session {
  id
  originalTranscript        // Arabic, as transcribed
  editedTranscript          // Arabic, after user edits
  clarifyDialogue[]         // full Q/A, suggestions, caveats
  refinedIdea               // the matured, agreed idea
  chosenFramework           // which framework + why
  generatedPrompt           // the English prompt
  routingDecision           // path, provider, model, rationale
  executionLog[]            // streamed output, tool calls, PC change-report
  translatedResult          // Arabic
  feedback[]                // spoken feedback rounds
  status                    // draft | clarifying | ready | executing | done | queued
}
```

Loop refinement (feedback → improve) always operates on the **complete** session, so
context is never lost between rounds.

## 6. Standalone APK & multi-user portability

- The APK contains **zero** owner-specific data. All user-specific values (API keys,
  PC pairing) are entered at runtime and stored encrypted on-device.
- Each user pairs with **their own** PC via a QR/short-code shown by `setup.ps1`.
- Path A needs no PC at all — a fresh install works with just API keys.
- Release build: document the exact Gradle command and APK output path; provide
  plain sideload steps in `docs/SETUP.md`.

## 7. Security model

- API keys: encrypted via Android Keystore; leave the device only to their own
  provider endpoint. Never logged, never in URLs.
- PC bridge: reachable only inside the user's private Tailscale net; no public ports.
- Guardrails on the PC (change-report, approval gate, dir confinement) are mandatory
  and non-bypassable.
- In-app warning that client-side keys carry risk; a clean seam is left to add an
  optional backend proxy later.

## 8. Reliability & performance

- Streaming everywhere (SSE); all I/O off the main thread.
- Retries with backoff; request cancellation; provider fallback on failure.
- Graceful offline behavior; clear loading/empty/error states on every screen.
- Unit tests for the router and provider layers; a fake `AiProvider` for tests.
