# ARCHIVE — `PROJECT_STATE.md` as it stood on 2026-08-07

**This is not the live state file. `PROJECT_STATE.md` is.**

Kept verbatim on the owner's instruction when the live file was condensed, so that §0 rule 4 —
append, never rewrite another agent's record — survives the compression. Every change-log entry
from `2026-07-29-A` to `2026-08-07-O` is here in full, with the reasoning that produced it.

**Open this when an entry code in `PROJECT_STATE.md` §11 needs its detail.** Do not read it to
learn the current state: everything below was true on 2026-08-07 and some of it was already
history then.

---

**This file is the single source of truth for this project.**

Several independent agents work on this repository and none of them can see each other's
sessions. This file is the only channel between them. Read it first; update it last.

---

## 0. AGENT PROTOCOL — OBEY BEFORE TOUCHING ANYTHING

1. **Read this file in full before any work.** Do not crawl the repository to orient
   yourself — everything you need is here or is named here. Open only the files your
   assigned task names.
2. **You edit; the OWNER builds.** Never run `gradlew`, never sync, never install. When
   you finish editing, report and stop. The owner runs the build and pastes the output
   back.
3. **Update this file in the same work unit as your edit — without being asked.** Any
   change to the tree, however small, requires you to update, before you report:
   - §7 CURRENT STATE — if what works or does not work has changed
   - §8 NEXT STEP — so the next agent knows where to pick up
   - §10 CHANGE LOG — always, one entry, newest first, even for a one-line change

   This is part of the edit, not a follow-up task. An edit reported without a change-log
   entry is an incomplete edit.
4. **Never delete or rewrite another agent's change-log entry.** Append only. If you undo
   someone's change, add a new entry saying so and why.
5. **If this file contradicts the code, the code wins.** Correct this file, and record the
   correction in §10 so the next agent knows the discrepancy was real.
6. **Scope discipline.** Touch only the files your task names. Anything else you believe
   needs changing goes in §9 as a note — you report it, you do not change it.
7. **Stop rule.** If one problem consumes more than ~15 minutes or 3 attempts with no
   measurable progress, stop and report what is blocking you. An earlier agent burned ten
   hours looping on a one-line fix. Do not repeat that.
8. **Diagnose before editing.** State your reasoning and your assumptions. Ask when
   genuinely blocked. One correct change beats three speculative ones.
9. **Talk to the owner in plain Arabic. This is a standing instruction, given 2026-08-06.**
   He is Arabic-first and is not an Android developer. Write to him the way you would explain
   the change to a smart friend who does not know the codebase.
   - Short sentences. Everyday words. No `camelCase` identifiers, no class names, no Android
     API names unless he must type them.
   - Say **what changes for him**, not what changes in the code.
   - **Tell him exactly what to do, step by step.** He asked twice on 2026-08-06 what was
     required of him, because the answer was buried in explanation.
   - **Do not ask him to rule on implementation details.** If you have a recommendation, you
     have already decided — decide it, tell him in one line, and offer to reverse it. Product
     and priority calls are his; audio formats and error-handling shapes are not.
   - Keep the density here, in this file. `PROJECT_STATE.md` is written for the next agent and
     may stay technical. The chat is not.

   **Extended 2026-08-07, at the owner's report «لغتك معقدة»: the rule covers the acceptance
   tables too, not just the prose around them.** Two checks went unrun because they described a
   behaviour in the abstract — "the first turn restates your idea", "a question that moves the
   subject says so" — with nothing named to look at. **One instruction, one visible thing, per
   row.** Name the button, the word, the line on screen. If a row cannot be written that way, it
   is not yet a check.

   **Refined 2026-08-06, at the owner's request: he wants to learn the engineering, not be
   shielded from it.** So the shape is *plain first, then a short teaching section* — clearly
   separated, a few lines, never the whole diff:
   - Lead with what changed for him and what to do. That part stays free of jargon.
   - Then a brief section naming the pieces involved and **why they were chosen** — the
     trade-off, not the syntax. Name a thing once, explain it once, move on.
   - Prefer the reasoning over the mechanism: *why a WebSocket instead of uploading a file* is
     worth his time; the exact parameter list is not.
   - If it cannot be explained in a few lines, it belongs in this file, not in the chat.

---

## 1. WHAT THIS PROJECT IS

**Braining (فهم)** — an Arabic voice-commanded AI orchestrator for Android.
Location: `C:\Dev\Braining` (Windows). The owner is the sole human and is Arabic-first.

**Product flow (target):** speak a request in Arabic → transcribe → the app *interrogates
the idea with you* (questions, suggestions, caveats) until you declare it mature → it
forges a professional **English** prompt from a framework library → executes it → returns
results **translated to Arabic** → spoken feedback loops back with full session context →
everything saved to searchable history. Orchestrated across several AI providers, each
used where it is strongest.

**Two execution paths.** Path A = direct provider APIs from the phone; works with the PC
off; this is the self-sufficient core and is built first. Path B (M6) = a PC bridge over
Tailscale driving OpenCode headless, for tasks that must act on real files.

**Milestones.** M1 skeleton + providers + streaming chat · **M2 voice capture** ·
M3 Clarify + Forge (the product's soul) · M4 router + translate + feedback loop ·
M5 history + polish + signed release APK · M6 PC bridge.

---

## 2. DOCUMENT MAP — AUTHORITY ORDER

| File | Role | Authority |
|---|---|---|
| `PROJECT_STATE.md` (this file) | live state, protocol, next step | **highest for state** |
| `ANSWERS.md` | the owner's binding rulings | **highest for decisions** |
| `BRAINING.md` | master spec, 6 milestones | superseded by ANSWERS.md on conflict |
| `docs/BRAND.md` | visual identity; the 5-bar waveform is the M2 visualiser | authoritative for UI |
| `docs/ARCHITECTURE.md` | module and layer design | reference |
| `docs/M2_DESIGN_NOTE.md` | M2 voice capture — design, gate, owner decisions | **binding for M2 — signed 2026-08-04**; its §7 rulings are restated in `ANSWERS.md` Part 5, which wins on conflict |
| `docs/M2_GATE.md` | the M2 gate: fixed passage, setup, recording table, outcome readings | the instrument. `ANSWERS.md` Part 1 §1 + Part 5 §M2-3 remain the ruling |
| `docs/DEEPGRAM_DESIGN_NOTE.md` | cloud STT design — key handling, audio, transport, four open decisions | **UNSIGNED.** No Deepgram code until its §8 is answered |
| `docs/M3_DESIGN_NOTE.md` | M3 Clarify + Forge — scope, architecture, the gate, five decisions | **SIGNED 2026-08-07**; its §7 rulings are restated in `ANSWERS.md` Part 7, which wins on conflict |
| `docs/M3_GATE.md` | the M3 gate: the paired comparison, the recording tables, the fourth run, a reading for every outcome | the instrument. `docs/M3_DESIGN_NOTE.md` §5 remains the ruling |
| `docs/PROMPT_FRAMEWORKS.md` | the FORGE framework library (M3) | reference |
| `docs/SETUP.md` | build/signing setup | reference |
| `.opencode/instructions.md` | working discipline for OpenCode sessions | reference |

Read `ANSWERS.md` before designing anything. Read `docs/BRAND.md` before any UI work.

---

## 3. TOOLCHAIN — EXACT VERSIONS, DO NOT CHANGE WITHOUT AN EXPLICIT ORDER

Gradle wrapper **9.0** · AGP **8.13.0** · Kotlin **2.3.10** · KSP **2.3.10** (matches
Kotlin exactly; no dual-version suffix at 2.3+) · Hilt **2.58** (deliberate — 2.60.x
requires AGP 9, which we do not use) · Ktor **3.5.1** · kotlinx-serialization **1.11.0** ·
Compose BOM **2026.06.01** · activityCompose **1.10.1** · JVM target 17 via
`kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }` (the old `kotlinOptions`
form is removed — do not reintroduce) · minSdk 26, compile/target 35 · package
`com.braining.app`.

**Compose BOM is duplicated in two places and they must stay in sync:**
`gradle/libs.versions.toml` **and** the hardcoded string in
`build-logic/convention/src/main/kotlin/braining.android.compose.gradle.kts`.
A split-generation Compose runtime caused by these drifting apart produced the
`setViewTreeSavedStateRegistryOwner` navigation crash. Giving `build-logic` access to the
version catalog is in §9.

---

## 4. HARD CONSTRAINTS — VIOLATIONS HAVE COST DAYS

1. **Never modify:** `gradle-wrapper.properties`; `gradle.properties` (the entries
   `android.builder.sdkDownload=false` and `org.gradle.java.home` are load-bearing); the
   hand-written `package.xml` files inside the Android SDK; `keystore.properties`.
   Never invoke Android Studio's upgrade assistant or SDK "Quickfix" — both have
   previously destroyed this build.
2. **`io.ktor:ktor-client-sse` does not exist.** SSE lives in `ktor-client-core`. A prior
   agent hallucinated this dependency and it cost a day. Never add it.
3. **BYOK, always.** Keys are entered at runtime and stored Keystore-encrypted. Never
   hardcode, embed, log, or commit an API key, and never place one in a URL query string.
   The shipped APK must contain zero owner-specific data — distribution to friends is a
   first-class product goal (`ANSWERS.md` Part 3).
4. **Hilt multibinding maps** inject as `Map<String, @JvmSuppressWildcards AiProvider>` —
   the annotation goes on the *value* type, never the key.
5. **kotlinx-serialization 1.11:** wrap primitives in `JsonPrimitive(...)` inside
   `buildJsonObject { put(...) }`.
6. **Arabic-first.** User-facing strings belong in string resources, never hardcoded. RTL
   is the default direction; use `start`/`end`, never `left`/`right`; mirror directional
   icons. The last hardcoded UI literals were removed in §10 `2026-08-03-E`: provider
   failure wording now lives in `chat_`/`settings_` resources, resolved from typed
   `AiError`s in the UI layer.
7. **Never revert `BaseHttpProvider.complete()` to `httpClient.post()`.** See §10, entry
   2026-07-29-A.

---

## 5. MODULE MAP

| Module | Contents |
|---|---|
| `app` | `MainActivity`, `BrainingApp`, `navigation/NavGraph.kt`, manifest, `res/values*` |
| `core-ui` | `theme/` (`BrandPalette`, `BrainingTheme`, `BrainingTypography`), `text/BidiText.kt`, `error/AiErrorMessage.kt`, **`diagnostics/DiagnosticsPanel.kt`**, `res/font/` (IBM Plex Sans Arabic), `res/values*/` (`error_*` and **`dev_*`** strings) |
| `core-domain` | interfaces `AiProvider`, `EncryptedKeyStore`, `AppPreferences`, **`SpeechToText`**, **`ClarifyEngine`**; models `AiRequest`, `AiChunk`, `AiError`, `ChatMessage`, `ProviderId`, `ProviderState`, `ProviderCapabilities`, `RequestDiagnostics`, **`SttError`**, **`TranscriptionEvent`**; **M3** `clarify/` — `ClarifyState`, `ClarifyTurn`, `TurnKind`, `ClarifySession`, `ClarifyEvent` |
| `core-data` | `EncryptedKeyStoreImpl`, `AppPreferencesImpl`, `di/CoreDataModule` (Ktor `HttpClient`, `Json`) |
| `ai-providers` | `BaseHttpProvider` + `anthropic/` `openai/` `deepseek/` `gemini/` + `github/GitHubModelsStub` + `di/ProvidersModule` |
| `speech` | **M2.** `RoutingSpeechToText` (picks the engine) → `DeepgramSpeechToText` (WebSocket + `AudioRecord`) or `AndroidSpeechToText`; `di/SpeechModule` + `SpeechNetworkModule`. Declares `RECORD_AUDIO` and the `RecognitionService` `<queries>` entry. Separate from `core-data` on purpose — a heavy optional engine belongs where it can be swapped, and `core-data` is imported by everything |
| `feature-chat` | `ChatScreen`, `ChatViewModel` |
| `feature-clarify` | **M3.** `ClarifyEngineImpl` (the five-state machine over any `AiProvider`) + `PromptForgeImpl` (FORGE) + `ClarifyPrompt` / `ForgePrompt` (the two system prompts, one place each) + `ClarifyScreen` / `ClarifyViewModel` + `di/ClarifyModule` + `res/raw/prompt_frameworks.json` (**the framework library, as data**) + `res/values{,-en}/` (`clarify_*`). **One module for CLARIFY and FORGE, not the two `docs/ARCHITECTURE.md` §2 names** — nothing calls Forge but Clarify and no screen shows it but Clarify's, so the second boundary has nothing on the other side of it |
| `feature-settings` | `SettingsScreen`, `SettingsViewModel` |
| `build-logic/convention` | precompiled script plugins `braining.android.application` / `.library` / `.compose` |

**Single points of change worth knowing:**

- All four HTTP providers inherit `BaseHttpProvider.complete()`. None overrides it. Fix
  streaming or transport behaviour there once and it applies to all of them.
  `GitHubModelsStub` overrides `complete()` but never touches the network.
- `GeminiProvider.DEFAULT_MODEL` is the single source for both the streaming endpoint and
  `verify()`.
- `core-ui/text/BidiText.kt` is the single place that decides text direction.
- `core-ui/diagnostics/DiagnosticsPanel.kt` is the single Developer Mode readout, shared by Chat
  and Clarify since `2026-08-07-E`. Anything new that wants to display request data renders it
  through this, and everything it renders is already redacted by `BaseHttpProvider.redactSecrets`.
- `AppPreferences` is the single home for non-secret settings. The "retain raw audio"
  toggle and M5's TTS opt-in belong there, not in `EncryptedKeyStore`. **The raw-audio
  toggle is no longer an M2 item** — deferred by the owner's ruling of 2026-08-04
  (`ANSWERS.md` Part 5 §M2-2), because `SpeechRecognizer` never hands the app an audio
  file. This line reserves its future home; it is not an instruction to build it in M2.
- **`ProviderId.defaultModel` is the only place a model name is written.** Never type a
  model string anywhere else. It was previously duplicated across `ChatViewModel`,
  `SettingsViewModel` and `GeminiProvider`, and when DeepSeek retired `deepseek-chat` all
  three copies were wrong at once. The user's per-provider override lives in
  `AppPreferences.selectedModels` and wins over the default.
- `BaseHttpProvider.redactSecrets` is the only thing standing between an API key and the
  Developer Mode panel. Anything new that displays request data must go through it.

---

## 6. BUILD, DEVICE, AND VERIFICATION

**Test device:** Xiaomi Redmi Note 13 Pro 5G, Android 14, connected over USB, also paired
with Android Studio.

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

Xiaomi requires "Install via USB" and "USB debugging (Security settings)" enabled, and
shows a transient confirm dialog. `adb` lives loose in the SDK root:
`C:\Users\ASUS\AppData\Local\Android\Sdk\adb.exe`.

**Network reality (measured, do not design around a VPN).** All 10 provider endpoints are
reachable **without** a VPN (219–700 ms). With a VPN, latency roughly doubles (542–977 ms)
and Groq changes from `401` to `403` — i.e. at least one provider actively rejects the VPN
exit IP. Never assume or require a VPN. Report the provider's actual response accurately;
do not interpret or work around it. Raw data: `tools/reachability-بدون_VPN.txt`, produced by
`tools/check-reachability.ps1`.

**Reachability is not authorisation — do not read this table as "the provider works".** It
proves only that a TCP connection reaches the endpoint and gets an HTTP answer; any status,
including `401`, counts as reachable. Gemini appears here as reachable, and yet refuses real
requests from this location with `HTTP 400 — User location is not supported for the API use.`
(§7). The measurement answers "is the network blocking me", not "will this provider serve me".

---

## 7. CURRENT STATE

### Working and verified on device

- Build succeeds; app installs and runs.
- Chat screen renders in Arabic RTL with provider selector, settings gear, delete action.
- Navigation / lifecycle crash (`ViewModelStore` / `SavedStateRegistryOwner`): **resolved**
  by aligning the Compose BOM in both locations. Do not reopen.
- Keystore crash (`AEADBadTagException`): **resolved**. `EncryptedKeyStoreImpl` no longer
  rotates the master key on cold start; it self-heals once then degrades gracefully.
- API keys can be entered, stored encrypted, read back, and shown via the eye toggle.
- `verify()` inspects HTTP status through `BaseHttpProvider.classifyHttpError()`; an invalid
  key is reported as invalid. All four HTTP providers use it. (Retyped to return `AiError?`
  in §10 `2026-08-03-E` — the status/typed-error plumbing is new and unbuilt.)
- Provider failure bodies are parsed into typed `AiError`s (`classifyHttpError`) and phrased
  from resources in the UI (`AiError.toUserMessage()`); the provider's raw `detail` is kept
  for Developer Mode only and never rendered as UI text.
- **Streaming is genuinely token-by-token** — owner-verified on DeepSeek, 2026-07-29.
- **DeepSeek account is funded and returns real answers.** (An earlier note in this file
  said `HTTP 402 Insufficient Balance`; that is stale and has been corrected.)
- **Bidirectional text renders correctly** — owner-verified on device, 2026-07-29. Arabic
  answers containing English terms no longer interleave. `core-ui/text/BidiText.kt` is the
  single place that decides direction; route any new mixed-script text through it rather
  than calling `Text` directly.
- **Localization structure is correct** — `values/` Arabic (default) + `values-en/`, strings
  owned by the module that renders them, no hardcoded user-facing text in `ChatScreen` or
  `SettingsScreen`. Owner-verified, 2026-07-29.

### Verified on device, 2026-08-03 — nothing here is awaiting anything

- **Developer Mode** (`ANSWERS.md` Part 2 §9). The toggle is the first card in Settings, and
  the diagnostics strip renders under **both** a successful reply and an error card — the
  `lastDiagnostics` design from `2026-07-29-D` doing exactly its job. Token counts read 0 as
  expected (deferred, §9). This feature paid for itself repeatedly today: the captured request
  body is what proved which build was under test, and first-token latency is what separated
  "the answer looks fine" from "the fix landed".
- **DeepSeek streaming.** Final run: **1094 chunks · first token 1300 ms · total 11532 ms**,
  complete Arabic answer, no `"null"`, and `"thinking":{"type":"disabled"}` in the captured
  body. The four-run progression is worth keeping, because each number diagnosed the next
  fault and because runs 1 and 3 both *looked* like successes:

  | Run | Chunks | First token | Total | Reading |
  |---|---|---|---|---|
  | 1 | 2916 | 651 ms | 27.3 s | inflated — reasoning chunks rendered as `"null"` |
  | 2 | 1 | 38235 ms | 38.2 s | `"null"` gone; reasoning ate the whole token budget |
  | 3 | 1785 | 15376 ms | 29.7 s | parser fix built, thinking fix not yet — survived on luck |
  | 4 | 1094 | **1300 ms** | **11.5 s** | both fixes in — **pass** |

- **Gemini streaming** — 54 chunks, complete answer, correct multi-turn history. **Over a VPN**;
  see "Blocked by something outside the code" below for what that does and does not prove.
- **Per-provider model override.** Typing `deepseek-v4-pro` in Settings changed the model in the
  captured request body; restored to `deepseek-v4-flash` afterwards.
- **A3 legible failures.** Typed `AiError` (missing key / invalid key / forbidden / rate limited
  / provider down / no network / timeout / region blocked / unknown) replaces the old
  `HTTP nnn — message` literal. Classification in `BaseHttpProvider` + `ErrorClassifier`,
  wording in `AiError.toUserMessage()` from resources. `ChatUiState.error` and
  `ProviderState.error` are `AiError?`. `HttpTimeout` in `CoreDataModule` (600 s request /
  15 s connect / 60 s socket). All seven acceptance cases passed. **The airplane-mode case
  mattered most**: it proves that deleting `catch (e: Exception)` from `BaseHttpProvider` was
  safe, and that socket exceptions reach `ChatViewModel`'s `.catch` and get classified there.
  Details in §10 `2026-08-03-E`.
- **Arabic/English in-app toggle.** `androidx.appcompat` **1.7.1** (no other version moved),
  `MainActivity` is an `AppCompatActivity`, `Theme.Braining` descends from
  `Theme.AppCompat.Light.NoActionBar` (a platform theme crashes AppCompatActivity), Language
  card in Settings drives `AppCompatDelegate.setApplicationLocales()`. Flips every screen
  immediately and survives a full restart.
- **Rotation and process death.** Rotation preserves the conversation; under "don't keep
  activities" the app is killed and relaunches cleanly with no crash. The conversation is not
  persisted — correct for M1, and M5's job.

### Blocked by something outside the code

- **Gemini is geo-blocked at the owner's location.** A chat request returns
  `HTTP 400 — User location is not supported for the API use.` — Google's standard regional
  refusal, not a bug in this app. `verify()` had appeared to succeed earlier; treat that as
  unreliable evidence, since it hits `generateContent` rather than `streamGenerateContent`
  and the two need not be gated identically.

  This is a **product-strategy problem, not a defect**. `ANSWERS.md` Part 3 makes "usable by a
  friend with only a free-tier Gemini key" a first-class goal, and Part 1 §3 chose Gemini over
  GitHub Models precisely for its no-credit-card free tier. If the owner cannot reach Gemini,
  the owner cannot test or support the path the distribution plan rests on.

  Google's supported-region list covers nearly every Arabic-speaking country — Algeria,
  Bahrain, Egypt, Iraq, Jordan, Kuwait, Lebanon, Libya, Mauritania, Morocco, Oman, Palestine,
  Qatar, Saudi Arabia, Somalia, Sudan, Tunisia, UAE, Yemen — so this affects some locations
  and not others, and a friend elsewhere may be unaffected.
  Ref: <https://ai.google.dev/gemini-api/docs/available-regions>

  **Owner's ruling, 2026-08-03** (recorded in `ANSWERS.md` Part 3 §B): **Gemini stays** as the
  recommended starting provider for friends in supported countries. The app must state the
  regional refusal plainly in Arabic and steer the user to another provider — a distinct error
  case in A3, not a generic bad-request. The owner develops on DeepSeek. **No VPN** (§6).

  Consequence for M1: the second streaming provider must be Anthropic or OpenAI. Gemini
  cannot serve as proof of anything the owner cannot run.

### Known not done

- ~~**Anthropic has never been exercised.**~~ **CLOSED 2026-08-06 — Anthropic works.** The
  owner's first attempt failed; the cause was the account's billing state, not the code, which
  is what §9's "the app captures what we sent, never what came back" item predicted. Once the
  API account was funded it ran. This is the **third streaming provider proven**, and the first
  one the owner can reach without a VPN besides DeepSeek — which matters for M3: Anthropic is
  the intended brain for Clarify/Forge, and it is no longer an unknown. `2026-08-03-C`'s SSE
  parser and `2026-08-03-E`'s `verify()`/error path have now both run.
  - **Still worth knowing:** the promo credit expires **19 Sep 2026**, and the app's error
    message for an exhausted balance is still the useless «حدث خطأ غير متوقّع (400)». The §9
    `InsufficientCredit` item is therefore not academic — it is what the owner will see on the
    day the credit runs out, and he has now seen once already how little it says.
  - `claude-sonnet-5` is **verified correct against a primary source, 2026-08-06** — it is
    Active, and Anthropic's dateless-ID convention (major releases omit the minor segment)
    is exactly this string. It was the last unverified model name in the app; that row is
    now closed. Refs: <https://docs.claude.com/en/docs/about-claude/model-deprecations>,
    <https://platform.claude.com/docs/en/about-claude/models/model-ids-and-versions>
  - The code was desk-checked the same day and is **not** the prime suspect: headers
    (`x-api-key` + `anthropic-version: 2023-06-01`), body shape, `max_tokens`, the
    `message_stop` terminator, and the absence of any `SYSTEM` role in `messages` are all
    correct for `/v1/messages`. Billing state and key provenance are the live hypotheses.
- **Gemini sends deprecated sampling parameters.** Google has deprecated `temperature`,
  `top_p` and `top_k`; `GeminiProvider.buildRequestBody` still sends `temperature` inside
  `generationConfig`. Deprecated, not yet rejected — harmless today, a failure later.
- **Gemini thinking is on by default and costs 13–17 seconds before the first token.**
  Measured 2026-08-03: a one-word greeting returned a single chunk after 13.6 s. Google lists
  `gemini-3.5-flash` as "On (medium)" with `thinking_level` accepting minimal / low / medium /
  high. Same problem, same fix shape as DeepSeek in `2026-08-03-D`. **Verify the field's exact
  name and nesting for `streamGenerateContent` before writing it** — the guide describing
  `thinking_level` covers the Interactions API, not the endpoint this app calls, and hard
  constraint 2 is the standing reminder of what guessing an API costs.

### M1 exit checklist — **CLOSED 2026-08-03**

Every row below is green. Nine were closed by device tests, one by audit, one by the owner's
ruling (marked as such — it was not quietly passed). M1 is done; M2 is open.

| Criterion | Status |
|---|---|
| Streaming visibly token-by-token on ≥2 providers | **pass** — DeepSeek (1094 chunks) and Gemini (54 chunks, complete answer), both 2026-08-03. The Gemini run was **over a VPN**; that does not weaken this criterion, which is about the streaming implementation, but see the free-tier row below, which it does not satisfy |
| `verify()` rejects a wrong key, accepts a valid one | **pass** — a fabricated OpenAI key produced the typed Arabic 401 under Settings → تحقّق, 2026-08-03 |
| Switching provider mid-session works | **pass** |
| Multi-turn conversation history sent correctly | **pass** — user/assistant/user verified in the captured request body, 2026-08-03 |
| Settings ⇄ Chat stable across rotation and process death | **pass on stability** — verified 2026-08-03. Rotation preserves the conversation; under "don't keep activities" the app is killed and relaunches cleanly into a fresh session with no crash. The conversation itself is **not** persisted, which is correct for M1: `ChatViewModel` holds messages in memory and durable history is M5's job (`ANSWERS.md` §10). Not a defect — but M5 must not treat this as already solved |
| Settings model override reaches Chat | **pass** — verified 2026-08-03. Typing `deepseek-v4-pro` in Settings changed the model in the captured request body; restored to `deepseek-v4-flash` afterwards. This closes the plumbing added in §10 `2026-08-03-A` |
| Arabic RTL correct on every screen | **pass** — see `core-ui/text/BidiText.kt` |
| Arabic/English toggle | **pass** — verified on device 2026-08-03; flips every screen immediately and survives a full restart |
| Legible failures in Arabic (A3) | **pass** — all seven cases verified on device 2026-08-03 |
| No API key or owner-specific value in source/resources/build | **pass** — audited 2026-08-03, see §10 `2026-08-03-F` |
| Usable on a fresh install with only a free-tier Gemini key, **no VPN** | **closed by owner's ruling, not by test.** Gemini works at the owner's location only with a VPN. The ruling in `ANSWERS.md` Part 3 §B stands: Gemini remains the recommended first provider for friends in supported countries — Google's list covers nearly every Arabic-speaking country — and the app now states the regional refusal in Arabic and steers the user elsewhere (A3, verified). The owner's own location is a known exception the product handles gracefully. **The APK still assumes no VPN; nothing in it requires one.** A friend in a supported country satisfies this criterion literally; the owner cannot personally verify it, and that is recorded rather than hidden |

### M2 — open, design note SIGNED 2026-08-04

`docs/M2_DESIGN_NOTE.md` is signed. Its four §7 decisions are answered and restated as
binding rulings in `ANSWERS.md` **Part 5**. All four went with the note's recommendation.

| Decision | Ruling | What it obliges |
|---|---|---|
| 1 · Phase 0 before the waveform | **yes, full scope** | BRAND `ColorScheme` (dark default + light), adaptive icon + monochrome layer, IBM Plex Sans Arabic 400/500 @ 1.7 |
| 2 · "retain raw audio" toggle | **deferred, not cancelled** | no such setting ships in M2; it arrives with the first engine that owns the audio stream. `ANSWERS.md` §10 annotated in place |
| 3 · auto-restart on silence | **allowed mitigation, recorded as a shortfall** | the segment count of **each** of the three gate runs is published here in §7; the fail criterion is unchanged |
| 4 · M2 UI location | **`feature-chat`** | the §9 `feature-settings → feature-chat` dependency is cleared **first**, as a prerequisite |

**Mandatory order (from the rulings, not a preference):** §9 dependency fix → Phase 0 →
M2 proper → the 60–90 s gate. §8 carries the detail.

**Phase 0 has since been executed** (§10 entries `2026-08-04-C`, `-D`, `-F`). All three SVGs
were present, and the owner supplied both IBM Plex Sans Arabic weights plus the OFL licence on
the same day. **The fonts live in `core-ui/src/main/res/font/`, never in `app/`** — `Type.kt`
compiles into `core-ui`, and a library module cannot see the app module's `R` class. The
licence sits in `licenses/`, outside `res/`, because AAPT2 rejects the filename.

**Step 1 — DONE AND VERIFIED ON DEVICE, 2026-08-04** (`2026-08-04-B`). The §9 sibling
dependency is gone: `AiErrorMessage.kt` lives in `core-ui` as `com.braining.core.ui.error`,
its strings moved with it as `error_*` in `core-ui/res/values{,-en}/`, and `feature-settings`
no longer depends on `feature-chat`. Installed on `2312DRA50G` (Redmi Note 13 Pro 5G,
Android 14) and **all four acceptance checks passed** — build; the Arabic missing-key card in
Chat; the Arabic invalid-key sentence in Settings → تحقّق; and both again in English after the
language toggle. That last pair is what matters: the same resources now resolve from two
sibling features that no longer know about each other, in both locales. A3 has not regressed.

The first `api(project(...))` in the repo is accepted by the toolchain.

**Step 2 (Phase 0) — since verified; this paragraph describes the edit as it was made**
(`2026-08-04-C`). BRAND is applied: `BrandPalette` in
`core-ui/theme/Color.kt` is now the only place a hex is written in Kotlin, both `ColorScheme`s
are built from it, `BrainingTypography` carries BRAND's 400/500 weights and 1.7 body leading,
and the adaptive icon gained an exact foreground plus the missing monochrome layer.

**The finding that mattered: `BrainingTheme` had `dynamicColor = true`.** On Android 12+ that
replaces the whole scheme with colours sampled from the user's wallpaper — so on this Android 14
device, *no palette defined in this repo has ever reached the screen*. That is how `BRAND.md`
§7 could sit unexecuted since M1 without anything looking wrong: the app was not showing a
wrong palette, it was showing Material You. The parameter is deleted, not defaulted to false.

**Phase 0 item 3 (the Arabic font) remains open** — still no `.ttf` in the tree. `Type.kt`
holds the whole type scale and routes every style through one `FontFamily`, so supplying the
two IBM Plex Sans Arabic weights is a two-line change; the exact steps are in that file's
KDoc. Until then the device font is in use, which BRAND §3 warns breaks layout on Xiaomi.

**M2 code now exists** — steps 3a and 3b are both written and compiled; see below. (This line
previously read "No M2 code exists yet." and was left stale when 3a landed. Corrected
2026-08-06 under §0.5.)

**Build status corrected, 2026-08-06 — `2026-08-04-I` IS BUILT.** Its change-log entry says
"edits complete, not built" and that is now wrong. Evidence, from the tree rather than from
memory: `AndroidSpeechToText$Attempt.class` — an inner class that exists **only** in the
attempt-ladder fix — is compiled at 17:38, and `app-debug.apk` is packaged at 17:39 on
2026-08-06. The voice UI of `2026-08-04-H` is compiled too (`VoiceCaptureSheetKt.class`,
`BrainingWaveformKt.class`). **Whether that APK reached the phone is a separate question**
and is not answerable from this seat; `installDebug` is cheap and settles it.

### M3 — open, design note SIGNED 2026-08-07

`docs/M3_DESIGN_NOTE.md` is signed. Its five §7 decisions are answered and restated as binding
rulings in `ANSWERS.md` **Part 7**. All five went with the note's recommendation.

| # | Decision | Ruling | What it obliges |
|---|---|---|---|
| ١ | Clarify: only path, or a mode? | **a mode** | the M1 plain chat is untouched; an explicit control starts Clarify. Narrows `BRAINING.md` §2's "for every request" |
| ٢ | Answer language before M4 | **English** | the screen must say so in Arabic; no translation step is pulled forward |
| ٣ | Which brain | **the chat's selected provider** | **overrides `BRAINING.md` §5.** Claude becomes a recommendation, not a pin. A friend with only a free Gemini key gets a working Clarify |
| ٤ | Interrupted session | **nothing survives** | no storage in M3; M5 owns Room. The loss is stated to the user, not hidden |
| ٥ | The forged prompt | **a screen for every user** | prompt + framework + one-line Arabic rationale + edit / swap / regenerate |

**Ruling ٣ is the one to know about: it overrides `BRAINING.md` §5**, which names Claude the
default brain for CLARIFY / FORGE / TRANSLATE. `PROJECT_STATE.md` §2 makes `ANSWERS.md` the only
document that can do that, which is why it is recorded there and not only in the note.

**Seven implementation decisions were taken by the agent and are in the note's §3.4**, marked
reversible — module boundary, questions from the model rather than a local rule table, the
five-state machine verbatim, `AiRequest.systemPrompt` instead of a `SYSTEM` message, no new
error vocabulary, the existing streaming transport, and the framework library as data. This
follows `2026-08-06-M`: a question with a recommendation attached is a decision already made.

### M3 step 1 — the state machine, no screen. ✅ **BUILDS** (`2026-08-07-C`)

The M2 3a/3b split, repeated: a new Gradle module and a new Hilt binding are the parts most
likely to fail, and failing them with nothing else in the build makes the cause unambiguous.
`.\gradlew.bat installDebug` succeeded on 2026-08-07, so `:feature-clarify` configures, the
unscoped `@Binds` resolves, and `braining.android.compose` is accepted on a module containing no
composable.

**Acceptance checks 2, 3 and 4 are NOT confirmed** — the owner reported the build and not the
device. They are the regression checks (app looks unchanged · a DeepSeek message still streams ·
dictation still works), and they are recorded as unrun rather than assumed, the same distinction
the M1 checklist draws on the free-tier Gemini row: "we did not look" is a different claim from
"we looked and it was right".

### M3 step 2 — the Clarify screen. ✅ **ALL NINE CHECKS PASS — verified on device 2026-08-07**

**The interrogation works.** The lightbulb entry, the streamed Arabic question with its kind
label, answering and getting the next turn, «نضجت الفكرة», a fresh session on re-entry, the turn
counter under Developer Mode, English after the toggle, and the network error with a working
retry — all confirmed by the owner (`2026-08-07-D`). The three regression checks passed with
them: chat still streams, the microphone still works, Settings is unchanged.

**No `[[سؤال]]` marker was visible in any turn**, which is the one thing about `HeaderReader`
that could not be proven from the compiler.

### M3 step 3 — FORGE. ✅ **EIGHT OF NINE CHECKS PASS, 2026-08-07** (`2026-08-07-H`, `-I`)

**«نضجت الفكرة» writes a real English prompt.** The framework is chosen and named with an Arabic
one-line reason, the skeleton is filled, the prompt is editable, the framework is swappable and
the whole thing regenerates. Owner's verdict: **"النتائج مذهلة حقاً"**.

**Check 9 — airplane mode — is deferred at the owner's request. Not passed, not failed, unrun.**
It is the check that proves `retry()` repeats the right one of three actions, which is the fault
`2026-08-07-H` was written to prevent, so it is carried forward into step 3b's list rather than
quietly dropped. Same distinction as the M1 checklist's free-tier row: "we did not look" is a
different claim from "we looked and it was right".

### M3 GATE — run 1 of 3 is in. **NOT a pass yet.**

Step 4a's seven checks passed, and the owner then ran the first paired comparison and the
mandatory fourth run.

| | Result |
|---|---|
| **Paired run 1 — path (أ), sent straight** | "سطحية وعلى قدر السؤال" — shallow, exactly as deep as the question was |
| **Paired run 1 — path (ب), through Clarify + Forge** | **The idea was transformed into a different question entirely** — by his own choice, arising from his understanding after the successive questions. "استمتعت حقاً بالأمر" |
| **Verdict, run 1** | **(ب) wins** |
| **Runs 2 and 3** | ⛔ **not run.** `docs/M3_GATE.md` §6: three ideas, and two of three is a fail. One run is not the gate |
| **Run 4 — the damaged transcript** | **It did NOT notice the errors — and reached the intended meaning anyway.** A second, faster, longer dictation raised the error and omission rate noticeably |

**The owner's own finding, and it is sharper than anything in the note: the interrogation is
«سلاح ذو حدّين».** It can carry the idea off its original axis. He caught it because he was
watching at every question — *"وأنا منتبه تماماً لهذه النقطة عند كل سؤال يسأله وجواب أجيبه"* — and
a user who is not watching will not. `2026-08-07-N` makes the drift **announce itself** rather than
trying to prevent it, because the same property is what produced the run he enjoyed.

**Run 4's result is a pass by a route the design did not predict.** `docs/M3_DESIGN_NOTE.md` §2
said correction would happen because the engine notices damage. **It does not notice.** What
recovers the meaning is the conversation itself — the answers fill in what the transcript lost.
Worth knowing precisely, because the mechanism being relied on is not the one that was designed,
and the second dictation shows the input can degrade much further than run 4 tested.

### ✅ M3 CODE IS COMPLETE — 2026-08-07. Only the gate remains.

| Item | Result |
|---|---|
| The five-state machine, no screen | ✅ built, app unchanged as intended (`2026-08-07-C`) |
| The Clarify screen, nine checks | ✅ all pass — a real Arabic question about a real idea, streamed, with no marker leaking into the text (`-D`) |
| Shared diagnostics in `core-ui` | ✅ the interrogation's system prompt is readable on the device (`-E`, `-G`) |
| FORGE — framework, rationale, English prompt, editable, swappable | ✅ eight of nine; owner's verdict **"النتائج مذهلة حقاً"** (`-H`, `-I`) |
| Execute the forged prompt | ✅ six of seven (`-J`) |
| `retry()` repeats the right action at all three stages | ✅ six observations, the deferred airplane-mode case included (`-K`, step 3c) |
| Session timer for the gate's second number | ⏳ built, one line to confirm (`-L`) |
| **The gate** (`docs/M3_GATE.md`) | ⛔ **not run — the only thing left in M3** |

### M3 step 3b — execute the forged prompt. ✅ **SIX OF SEVEN PASS, 2026-08-07** (`2026-08-07-J`)

**M3's loop is closed on device: speak → interrogate → forge → execute.** The prompt runs, the
English answer streams in its own area, editing the prompt changes the answer, and prompt and
answer scroll independently without pushing the controls off screen.

**Check 7 (airplane mode) is INCONCLUSIVE, and the check itself was the problem.** The owner
reports everything failed in airplane mode — which is the correct outcome and not a result. The
check was written to prove that `retry()` repeats the right one of three actions, and **in
airplane mode all three retries fail identically, so the thing being measured is invisible.**
A test whose pass and fail look the same is not a test. Rewritten in §8 Step 3c to be observable;
the fault is in the instrument, not the app, and it is mine.

**A second fact from that run matters more than the check: he was on Gemini** — and **he reaches
Gemini fine, over a VPN** (owner's correction, 2026-08-07; §9 carries it). §7's geo-block is a
property of his location, not of his setup.

**What survives that correction is the part that actually threatens the gate.** §6 measured that a
VPN roughly **doubles** latency, and `docs/M3_DESIGN_NOTE.md` §5 publishes seconds as one of the
two numbers the gate is judged on. Combined with a provider choice that resets on every restart,
three paired comparisons could each run under different conditions and the file would record them
as one measurement. **Run the gate on DeepSeek with no VPN** — that is what every M3 figure
already in this document was measured on.

### M3 step 2b — shared diagnostics. **BUILT. 6 of 8 checks pass; 4 and 5 unrun** (`2026-08-07-E`)

`DiagnosticsPanel` moved to `core-ui` and Clarify now captures its request.

**Checks 1, 2, 6, 7, 8 pass** — owner-verified 2026-08-07 with a screenshot. The chat panel
behaves identically after changing modules, which was the refactor's real test: endpoint, request
body and the token line all render, and `Chunks: 636 · first: 388 ms · total: 8594 ms` on
DeepSeek. Developer Mode off suppresses everything; English relabels the panel.

**Checks 4 and 5 were not run, and they are the two this unit existed for.** The instruction
named the Clarify screen but did not say so plainly enough, and the owner read the panel on the
**chat** screen instead. Recorded rather than assumed: **nobody has yet seen the interrogation's
system prompt**, so the one claim this build was meant to settle is still unsettled.

**Second attempt, on the right screen, and the panel would not open.** Owner's screenshot,
2026-08-07: the Clarify screen shows «أدوار الاستجواب: ١ · النموذج: deepseek-v4-flash», the
divider, and «الأجزاء: ٤٤ · أول جزء: ٩١٧ ms · الإجمالي: ١٥١١ ms» — so the panel **renders**, the
capture pipeline runs, and the turn streamed in 44 pieces. Tapping produced no visible endpoint or
request body. Fixed in `2026-08-07-G` (wrong parent — a growing child in a region that cannot
scroll).

### ✅ Step 2c passes — outcome (i), 2026-08-07

After the fix the panel opens and **«المسار» and «جسم الطلب» both carry content**. The red
"no capture" line did not appear, which is the discriminator working: `ClarifyEvent.Meta` arrives,
so `AiRequest.diagnostics` is reaching the provider and the captured body is real.

**One correction to the instruction, and it is the same mistake in a new place.** The owner was
told to look for a top-level `"system"` **field**. That is **Anthropic's** shape. DeepSeek is
OpenAI-shaped: `DeepSeekProvider.buildRequestBody` prepends
`{"role":"system","content":…}` as the **first element of `messages`**. Verified by reading the
provider, not remembered. Both are correct implementations; the instruction described one vendor's
wire format while the owner was running another's — the same class of error as telling him to look
at a panel without saying which screen. **What to look for on DeepSeek:** `"role":"system"` at the
head of `messages`, its `content` beginning «أنت محاور يساعد المستخدم على إنضاج فكرته».

---

## 8. NEXT STEP

## ✅ M1 CLOSED 2026-08-03 · ✅ M2 CLOSED 2026-08-06 · **M3 OPEN — DESIGN NOTE AWAITING SIGNATURE**

**M3 = Clarify + Forge.** Speak an idea → the app *interrogates* it with questions, suggestions
and caveats until the owner declares it mature → it forges a professional **English** prompt
from the framework library in `docs/PROMPT_FRAMEWORKS.md` → executes it.

### ✅ NOTE SIGNED 2026-08-07 — the five rulings are in `ANSWERS.md` Part 7

All five went with the recommendation. §7 above carries the table. **Ruling M3-3 overrides
`BRAINING.md` §5** — Claude is no longer the pinned brain for Clarify, only the recommendation.

### Step 1 — the state machine, no screen. ✅ **BUILD SUCCEEDED 2026-08-07.**

The module configures, the unscoped `@Binds` resolves, and `braining.android.compose` is accepted
on a module with no composable in it. **Checks 2–4 were not reported and are recorded as unrun**
(§7) — they fold into step 2's list below rather than being asked for twice.

### Step 2 — the Clarify screen. ✅ **ALL NINE CHECKS PASS, 2026-08-07.**

The interrogation works end to end, and the three regression checks step 1 never got passed with
it. §7 carries the detail. **No `[[سؤال]]` marker was visible in any turn** — the one thing about
`HeaderReader` the compiler could not prove.

### Step 2b — shared diagnostics. Six checks pass. **Step 2c fixes what blocked the last two.**

Checks 1, 2, 6, 7 and 8 passed on 2026-08-07 — see §7 for the numbers.

### Step 2c — the panel would not open on Clarify. ⏳ **EDITS COMPLETE, REBUILD NEEDED.**

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

**Developer Mode on.** Then:

| # | Do this | Expect |
|---|---|---|
| a | Chat: type a real idea, a sentence or two | The **lightbulb** button appears |
| b | Tap it, wait for the first question | The «استجواب الفكرة» screen |
| c | **Scroll to the very bottom of the conversation** | The grey line «الأجزاء: N · أول جزء: … ms» now sits **below the last card**, not at the top. It moved on purpose — see below |
| d | **Tap that grey line** | It expands. Scroll down through it |
| e | Read what appeared | **One of two outcomes, and they mean different things — report which:**<br>**(i)** «المسار» and «جسم الطلب» with content in them → find `"system"` in the body: it must hold **the seven Arabic rules**. That is the claim this whole unit exists to prove.<br>**(ii)** A red line saying «لم يُلتقط أي طلب» → the panel works and the capture never arrived. A different fault, and now a legible one |

**Why the line moved to the bottom.** It was in the fixed header, above the scrolling list.
Expanded, its request body is about a thousand characters of JSON — so the header grew, the list
was squeezed toward nothing, and the JSON ran off the bottom of a region that **does not scroll**.
`ChatScreen` never had this because its panel lives inside a message bubble, inside the lazy list.
Same composable, same data, one wrong parent.

**And the red line in outcome (ii) is new.** Before it, an unarrived capture rendered as two
labels with nothing under them — indistinguishable from a tap that did nothing. A diagnostic that
is confidently blank fails the same way as one that is confidently wrong, only more quietly
(`2026-08-06-D`).

**No new Gradle dependency.** `gradle/libs.versions.toml` is still untouched.

### Step 3 — FORGE. ✅ **EIGHT OF NINE CHECKS PASS — 2026-08-07.**

Owner's verdict on the forged prompts: **"النتائج مذهلة حقاً"**. Check 9 (airplane mode) is
**deferred at his request, not failed** — recorded as unrun in §7 and folded into step 3b below.

The build took one attempt to fix (`2026-08-07-I`, `StringBuilder.trim()` returning `CharSequence`)
and passed on the second.

### Step 3b — execute the forged prompt. ✅ **SIX OF SEVEN PASS, 2026-08-07.**

M3's loop runs end to end on device. §7 has the detail.

### ✅ Step 3c PASSED — 2026-08-07. **M3's code is complete.**

All six observations. `retry()` repeats the right one of three actions at every stage, the
interrogation survives a failed forge, the prompt survives a failed execution, and the network
errors are the typed Arabic ones. **Nothing in M3 is now unbuilt or unverified.**

### Step 4d — answer buttons, and a label that had gone stale. ⏳ **REBUILD.**

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

> **Write the owner's checks in short, plain sentences.** He reported on 2026-08-07 that he did
> not understand checks 3 and 4 of step 4c — «لغتك معقدة» — and he was right: they described a
> behaviour in the abstract instead of naming a thing to look at. §0 rule 9 is not only about the
> prose around a table. **One instruction, one visible thing, per row.**

| # | Do this | Expect |
|---|---|---|
| 1 | Interrogate an idea | When a question has two or three likely answers, **buttons appear above the writing box** with the answers written on them |
| 2 | Tap one | It is sent as your reply, exactly as if you had typed it |
| 3 | Ignore the buttons and type something else | It works. The buttons never replace the writing box |
| 4 | «نضجت الفكرة» → «نفّذ البرومبت» | The word above the answer box now reads **«الجواب»**, not «الجواب — بالإنجليزية» |

**Checks 3 and 4 from step 4c, re-asked simply — they were never run:**

| # | Do this | Expect |
|---|---|---|
| 5 | Start a new interrogation and read the **first** thing it writes | It begins by repeating your idea in **one line** in its own words, then asks its question |
| 6 | Keep answering. Watch for a question about something you did not mention | Before that question it says one sentence like «هذا ينقل الفكرة إلى…، هل تقصد ذلك؟» |

**Then gate runs 2 and 3** — `docs/M3_GATE.md`. Two more real ideas. DeepSeek, no VPN, Developer
Mode on, lightbulb before send.

### Step 4a — four defects the first gate attempt exposed. ✅ **PASSED 2026-08-07.**

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

**The gate could not be run, and every reason was a real defect.** The owner's report:
the answer area was too cramped to read · everything came back in English · the answer could not
be copied · the questions never ended. Plus a request: **Ctrl+Enter should send.** All five are in
this build (`2026-08-07-M`).

| # | Do this | Expect |
|---|---|---|
| 1 | Interrogate an idea and keep answering | **It converges.** Within about 3–5 questions a turn arrives labelled «لم يبقَ ما أسأل عنه», in an amber card, summarising the idea. It still does not press «نضجت الفكرة» for you |
| 2 | Press «نضجت الفكرة» | **The interrogation disappears** and the prompt gets the whole screen. A «اعرض الاستجواب» button brings it back |
| 3 | Press «نفّذ البرومبت» | **The answer arrives in Arabic**, not English |
| 4 | Long-press the answer | It selects. And a **copy icon** sits beside the buttons for both the prompt and the answer |
| 5 | With a hardware keyboard: type a multi-line reply, press **Enter** | A new line — unchanged |
| 6 | Press **Ctrl+Enter** | It sends. Works in the chat input too |
| 7 | Read the forged prompt's `# OUTPUT CONTRACT` | It now names **Arabic** as the answer's language. That is *why* check 3 works |

**If the build fails, suspect:** (a) `Icons.Default.ContentCopy` — extended icons, same family as
`Lightbulb` and `Key` which are already used; (b) `submitOnCtrlEnter` in `core-ui` — it uses
`onPreviewKeyEvent`, which is `@ExperimentalComposeUiApi` and carries its own `@OptIn`;
(c) the new `ClarifyTurn.Enough` branch — every `when` over it is exhaustive, so a missing arm is
a compile error, which is the point.

### Step 4b — THE GATE. **After 4a passes.**

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

**`docs/M3_GATE.md` is the instrument** — setup, the exact run order, recording tables, the fail
criterion written before any run, and a reading for every outcome. Do not improvise it: the three
runs are only comparable against the same conditions, and the fail criterion cannot be negotiated
after the numbers are in.

**This build adds one thing and nothing else: the session timer.** Developer Mode now shows
«زمن الاستجواب: N ث · حتى الجواب: N ث» beside the turn count. The gate needs seconds, and
`2026-08-06-J` is an entry about why that number must come from the device rather than a
stopwatch — M2 produced two consecutive dictation reports with no duration attached before the
lesson landed.

**Check it in one line before starting the gate:** interrogate anything, press «نضجت الفكرة», and
confirm the timing line appears. Then run `docs/M3_GATE.md`.

**Two things from that file worth repeating here, because getting either wrong voids the runs:**

1. **DeepSeek, VPN off, and re-checked before *every* run** — the provider does not persist (§9),
   and §6 measured that a VPN roughly doubles latency, which is one of the gate's two numbers.
2. **Lightbulb first, send second.** Sending clears the input; the lightbulb does not. Reversed,
   the transcript is lost and the second path runs on a *different* dictation of the same idea —
   at which point the comparison measures the two transcripts, not the two paths.

---

**Historical — the retry check as it was asked:**

### Step 3c — the retry check, rewritten so it can be observed. **NO REBUILD. Nothing changed.**

**Switch the provider to DeepSeek in the chat first, and turn the VPN off.** Not because Gemini is
broken — the owner reaches it over a VPN and §9 records that correction — but because every M3
number already in this file was measured on `deepseek-v4-flash` without one, and §6 measured that
a VPN roughly doubles latency. Same reason the gate that follows must do the same.

The previous version of this check could not pass or fail: in airplane mode all three retries fail
identically, so *which* action was retried was invisible. What makes it observable is turning the
network **back on** and seeing what survived.

| # | Do this | Expect |
|---|---|---|
| a | Interrogate an idea. Answer **two** questions, so there is something to lose | Two «سؤال» cards and two «أنت» cards |
| b | Airplane mode ON. Press **«نضجت الفكرة»** | An Arabic error naming the provider, and «أعد المحاولة» |
| c | Airplane mode **OFF**. Press «أعد المحاولة» | **The prompt is forged, and your two questions and answers are still above it.** If the conversation was wiped and it started asking again, `retry()` ran the wrong action — the one destructive outcome this check exists to catch |
| d | Airplane mode ON. Press **«نفّذ البرومبت»** | Arabic error, «أعد المحاولة», and **the forged prompt is still in its box** |
| e | Airplane mode OFF. Press «أعد المحاولة» | The answer streams. The prompt was not re-forged and not lost |
| f | New interrogation, airplane mode ON from the start | Arabic error on the first turn, «أعد المحاولة»; with the network back it asks the first question normally |

**What each error must be:** «لا يوجد اتصال بالشبكة» in airplane mode — **not** «حدث خطأ غير
متوقّع». If you see the generic one, the classifier missed and that is a real defect worth
reporting with the provider name that appeared beside it.

### Step 4 — the gate in `docs/M3_DESIGN_NOTE.md` §5. **← after step 3b passes. Last thing in M3.**

The paired comparison on three real ideas — the same idea sent straight to the provider versus
the same idea through Clarify and Forge — plus the mandatory fourth run started from a real
transcript **with its errors left in**. Two of three is a fail, written before any run.

**Two things M2 leaves M3 that are not obvious:**

1. **The transcript will contain errors and always will.** Clarify's first question is asked
   about text that may have misheard a word. The design must treat correction as part of the
   conversation, not as a failure state — the input field is editable for exactly this reason
   (`docs/M2_DESIGN_NOTE.md` §1).
2. **Anthropic is proven and is the intended brain** (§7). Its promo credit expires
   **19 Sep 2026**, which is now a real deadline rather than a note: M3 is the milestone that
   spends it.

### What the M3 agent needs, and nothing else

**Read in this order, then start.** No exploration of the tree — §0 rule 1.

1. **§0** of this file — the protocol. Rule 9 is new: plain Arabic to the owner, with a short
   technical section, because he is learning the engineering and is not an Android developer.
2. **`ANSWERS.md`** — highest authority for decisions. **Part 6 is the newest** and contains the
   voice-engine rulings; §10 there defines what M3 is allowed to persist. Part 2 §5, §6, §9 and
   §11 are the ones M3 is built on: the five-state Clarify machine, `frameworkOverrides`,
   Developer Mode showing the generated English prompt, and the ban on a local rule-based
   classifier.
3. **`docs/M3_DESIGN_NOTE.md`** — written 2026-08-07, **unsigned**. Its §7 is the gate on all
   M3 work; its §3.4 lists what the agent already decided so it is not re-litigated.
4. **`docs/PROMPT_FRAMEWORKS.md`** — the FORGE library M3 draws on. Nobody has used it yet.
5. **§7 and §9 of this file** — what works, and what is knowingly deferred.

`BRAINING.md` §2 and §5–§6 and `docs/ARCHITECTURE.md` §5 were read once while writing the note
and are **not** in this list, for a reason: three of `ARCHITECTURE.md`'s claims contradict the
tree and are now filed in §9. Read the note instead; it carries what survived the check.

**The owner builds; the agent edits.** Never run `gradlew`. Report, then stop.

**State of the tree, 2026-08-06:** everything is **built and verified on device**. There is no
outstanding "edited, not built" work anywhere. A clean `.\gradlew.bat installDebug` should
reproduce exactly what the owner is running.

**The first work unit was a design note, not code — and it is done (`2026-08-07-A`).** M2's
build order never had to be undone because `docs/M2_DESIGN_NOTE.md` named its open decisions and
was signed before a line was written. M3 is larger and more speculative than M2, so the same
order was followed: the note exists, its five decisions are named, and it is unsigned.

Everything below this line is M1/M2 history, kept for the reasoning rather than the status.

---

**M1 IS CLOSED (2026-08-03).** Every row of the §7 checklist is green.

**M2 IS OPEN AND AUTHORISED. `docs/M2_DESIGN_NOTE.md` is SIGNED (2026-08-04).** The four
decisions in its §7 are answered; the binding text is `ANSWERS.md` **Part 5**. Coding may
begin — in the order below, which the rulings dictate rather than merely suggest.

### Step 1 — clear the §9 dependency. ✅ **DONE, verified on device 2026-08-04.**

All four acceptance checks passed on `2312DRA50G`. Details in §10 `2026-08-04-B` and §7.

**Operational note worth keeping:** the first run failed at `:app:installDebug` with
`No online devices found` — the only candidate was
`adb-525ae8c7-E0TtBS._adb-tls-connect._tcp.`, a stale wireless entry marked OFFLINE (§9's
Wi-Fi-pairing item). Nothing was wrong with the build; the APK simply had nowhere to go. The
recovery, for next time:

```
C:\Users\ASUS\AppData\Local\Android\Sdk\adb.exe disconnect
C:\Users\ASUS\AppData\Local\Android\Sdk\adb.exe kill-server
C:\Users\ASUS\AppData\Local\Android\Sdk\adb.exe devices     # expect: <serial>  device
```

USB cable, phone unlocked, "Install via USB" and "USB debugging (Security settings)" on,
accept the transient Xiaomi dialog. **Never open SDK Manager or accept a "Quickfix" prompt**
(hard constraint 1) — the wireless path is a convenience with a bad risk trade.

### Step 2 — Phase 0. Verified 2026-08-04 except one retest and the font.

Built and run on `2312DRA50G`. **Checks 1, 2, 3 and 5 passed**, owner-verified: the app renders
in BRAND indigo in dark, inverts correctly to light, and the launcher icon shows the five
stadium-ended bars with the amber centre and dot. Check 2 passing is the proof that
`dynamicColor` was what had been hiding the identity all along.

| # | Check | Result |
|---|---|---|
| 1 | Build | ✅ pass |
| 2 | Dark mode = BRAND indigo, not wallpaper colours | ✅ pass |
| 3 | Light mode readable | ✅ pass |
| 4 | Error card legible in both modes | ✅ pass — on the **second** run. The first was "not weak, but far too bright"; the derived tones of `2026-08-04-D` fixed it, owner-verified in both themes |
| 5 | Launcher icon matches the SVG | ✅ pass |
| 6 | Themed icons keep the five-bar silhouette | ⛔ **not testable here** — HyperOS does not expose Android's "Themed icons" toggle. Moved to §9; the layer is present and well-formed, just unobservable on this device |

### ✅ PHASE 0 IS CLOSED — 2026-08-04, verified on device

All three items done and owner-verified on `2312DRA50G`:

| Item | Result |
|---|---|
| 1 · BRAND palette as `ColorScheme`, dark + light | ✅ dark and light both correct on device. `dynamicColor` removed — see `2026-08-04-C`, this is what had been hiding the identity all along |
| 2 · Adaptive icon + monochrome layer | ✅ icon matches the SVG. The monochrome layer is present but **unobservable on HyperOS** (§9) — not a failure, just untestable here |
| 3 · IBM Plex Sans Arabic 400/500, 1.7 leading | ✅ owner-confirmed the Arabic visibly changed shape, so the font genuinely loaded rather than silently falling back |

Errors were re-tuned once along the way (`2026-08-04-D`) after the owner reported glare, and
the font's location was corrected once (`2026-08-04-F`). `docs/BRAND.md` §7, owed since M1,
is now executed. **M2's waveform can be built against a palette that exists.**

### Step 3 — M2 proper. **✅ BOTH HALVES DONE AND VERIFIED (2026-08-06).** History below.

**3a — the engine, no UI. Built, installed, app unchanged as intended (2026-08-04).**
`SpeechToText` + `TranscriptionEvent` in `core-domain/speech/`,
`SttError` in `core-domain/model/`, new `:speech` module with `AndroidSpeechToText` and
`SpeechModule`, `RECORD_AUDIO` declared, `settings.gradle.kts` and `app/build.gradle.kts`
wired. Details in §10 `2026-08-04-G`.

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

**Acceptance is deliberately thin: the app must build, install, and behave exactly as before.**
There is no visible change — nothing calls `SpeechToText` yet. That is the point of splitting
here: a new Gradle module, a new Hilt binding and a `settings.gradle.kts` edit are the parts
most likely to fail, and failing them alone makes the cause unambiguous.

**If the build fails, suspect in this order:** (a) `settings.gradle.kts` — the module is only
seen if `include(":speech")` is picked up, which needs a Gradle sync/configure; (b) the Hilt
`@Binds` in `SpeechModule` — `AndroidSpeechToText`'s `@Inject` constructor and
`@ApplicationContext` must both resolve; (c) `RecognizerIntent.EXTRA_PREFER_OFFLINE` or
`EXTRA_SPEECH_INPUT_*` — all exist at API 23+ so they should be fine on minSdk 26, but they
are the newest API surface touched here.

**3a is verified: built and installed 2026-08-04, app unchanged as intended.**

### Step 3b — the voice UI. ✅ **ALL SIX CHECKS PASS — verified on device 2026-08-06.**

The microphone button, the permission rationale, the five-bar waveform moving with the voice,
the editable transcript in the input field, the denial card without a crash, and the segment
count under Developer Mode — all confirmed by the owner. **The M2 UI works.**

**The Arabic accuracy problem is CLOSED — verified on device 2026-08-06.** The transcript was
very bad because the attempt ladder led with the offline rungs and only advances on a language
error, so the weak on-device model won every time and Google's network model was never reached.
Reversed in `2026-08-06-B`, with `ar-SY` leading from `-C`. The owner confirms it now works.

**One defect remained and it was in the instrument.** The Developer Mode engine line kept
naming the previous language after the toggle was flipped, because `engineTag` lives in the
ViewModel and survives the activity recreation. The recognition language itself was always
correct. **Fixed and verified on device 2026-08-06** (`2026-08-06-D`): the line now follows the
toggle before any recording. That line is what the gate's runs are judged by, so it had to be
right before Step 4, not after.

### ✅ M2 STEP 3 IS CLOSED — 2026-08-06, all verified on device

| Item | Result |
|---|---|
| 3a · engine, no UI | ✅ built, app unchanged as intended |
| 3b · voice UI, six checks | ✅ all six pass — waveform moves with the voice, transcript editable in the input field, denial does not crash |
| Arabic accuracy | ✅ fixed — the ladder reaches the network model, `ar-SY` leads |
| Developer Mode instrumentation | ✅ segment count + accepted tag + online/offline, and the tag follows the language toggle |

### M2 GATE — RUN 2026-08-06 · truncation **PASSES** · accuracy is a separate, worse problem

The owner read `docs/M2_GATE.md`'s 161-word passage **three times in a single continuous
recording** — ~480 words, roughly 3.5 minutes, harder than the ruling asked for.

| | Run 1 | Run 2 | Run 3 |
|---|---|---|---|
| Reached the last word `بعد` | ✅ | ✅ | ✅ |
| Truncated | no | no | no |
| Word accuracy vs source (diacritics/hamza/ة normalised — generous) | **66 %** | **66 %** | **65 %** |
| Spans of 2+ consecutive source words lost or replaced | 14 | 15 | 16 |
| Segments | **37 total across all three** (≈ the 33 sentence boundaries) | | |
| Accepted tag | `ar-SY` | | |
| Mode (network / on-device) | **NOT RECORDED — see below** | | |

**The gate's own criterion is met and `SpeechRecognizer` stays.** `ANSWERS.md` Part 1 §1 asks
one question — does the transcript reach the end — and the answer is yes, three times, over an
utterance three times longer than the test required. The fear that motivated the gate (a
short-utterance engine truncating a long paragraph) is **disproved on this device**. Vosk is
not triggered: it was pre-approved for *truncation*, and truncation did not happen.

**A different problem is now the blocker: 66 % word accuracy.** M3 Clarify builds on this text,
so this is not cosmetic. Samples: «بخطة واضحة» → «مبدأ التفويض» · «قد يوفّر» → «قد يستمتع» ·
«حين تنضج الفكرة» → «حين تنضج الزوجة» · «ليشعروا بالتقدّم» → «وبالتوقيع».

**The three runs failed almost identically, and that is the important finding.** «تماسكاً مما
ظننّا» → «ظنا», «يعمل بها» → «تعمل على», «فكرة تنجح» → «الفكرة» — the same substitutions, in
the same places, in all three runs. **A restart seam would land somewhere different each time.**
Consistent errors mean the model is confidently wrong, not that the stitching is dropping
words. So the 37 segments are not the main cause of the 34 %, and raising the silence
thresholds would not fix this.

**Mode CONFIRMED 2026-08-06 by screenshot: `ar-SY · عبر الشبكة`.** The gate measured Google's
network model. The 66 % is real and belongs to the shipping configuration.

**And the same screenshots overturned `2026-08-06-B`'s diagnosis — see the correction in that
entry.** Airplane mode walks the *entire* ladder and fails with code 12: **there is no offline
Arabic pack on this device.** The offline rungs have therefore never been able to win, which
means the ladder was already reaching the network *before* the reorder. The reorder did not
change which model ran. **What changed was the tag: bare `ar` → `ar-SY`.**

### Two facts about voice input that follow, and neither is a defect

1. **Dictation on this device requires an internet connection.** With no network the ladder
   exhausts every rung and reports the language error honestly, steering the user to enable the
   internet or download Google's offline pack. Correct behaviour, but it means the offline
   fallback built in `2026-08-06-B` is **untested and currently unreachable here**. It will
   matter for a friend whose device *does* have the pack.
2. **Short colloquial speech transcribes perfectly.** «مرحبا يا عزيزي كيف حالك» came back exact,
   one segment, over the same connection that scored 66 % on the passage. The engine is not
   broadly bad at Arabic.

**Live hypothesis for the 66 %, not yet tested: the passage is the wrong test for this tag.**
`ar-SY` tells the recogniser to expect Syrian colloquial; `docs/M2_GATE.md` is deliberate,
literary MSA (تتعثّر · الإلهام · تماسكاً · يوفّر). A dialect-tuned model fed formal MSA would
mis-hear exactly the dense vocabulary it did, identically on every run. The gate flagged this
limitation in advance — *"it does not measure how the engine handles your everyday dialect"* —
and it now looks less like a footnote and more like the explanation. **The product's real input
is spontaneous Levantine speech, not read MSA.** Test that before drawing any conclusion about
accuracy or about changing engines.

---

**Historical — how 3b got here:**

**What happened on 2026-08-04:** the microphone button appeared and the permission was granted
(checks 1 and 2 **pass**), then the app reported that the recognition language was not
installed — on a device where Arabic is installed as a system language, as a keyboard, and in
the engine's own speech settings. The app was not wrong about the error code; it was wrong
about what that code means. Cause and fix in §10 `2026-08-04-I`. **Rebuild and rerun 3–6.**

**The fix is already compiled (2026-08-06).** `2026-08-04-I`'s own status line says "not
built" and is wrong: `AndroidSpeechToText$Attempt.class`, which exists only in the
attempt-ladder fix, is in the build output, and `app-debug.apk` was packaged a minute later.
Run `installDebug` anyway rather than assuming the phone has it — the build succeeding and the
APK reaching the device are two different events, and §10 `2026-08-04-B` is the entry where
exactly that distinction bit.

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

**Turn Developer Mode on first** (Settings, first card) — check 6 needs it.

| # | Do this | Expect |
|---|---|---|
| 1 | Build and open Chat | A **microphone button** left of the send button. If it is missing, the device reports no engine — that is `NoEngine`, see below, and it is not a code fault |
| 2 | Tap the microphone, **first time** | An Arabic dialog explaining why the microphone is needed and that audio stays on the device — **before** Android's own prompt. Allow it |
| 3 | Speak a short Arabic sentence | A bottom sheet with the **five-bar mark**: bars rise with your voice and settle when you stop. Silence must look like the logo, speech must move it. Your words appear below as they are recognised |
| 4 | Press «تمّ» | The sheet closes and the transcript is sitting **in the input field, editable**. Change a word, then send — the edited text is what goes to the provider |
| 5 | Deny the permission (Settings → Apps → Braining → revoke, then tap the mic and refuse) | An Arabic card explaining how to enable it from system settings. Tap the card to dismiss. **No crash** |
| 6 | With Developer Mode on, dictate with a deliberate 3-second pause mid-sentence | A line reading «مقاطع التفريغ: N». **N > 1 means the engine restarted** — that is the number the gate is judged on |

**If the build fails, suspect in this order:** (a) `ChatViewModel`'s new `SpeechToText`
constructor parameter — Hilt must resolve it from `:speech`, and the app module's dependency
on `:speech` was added in 3a but never exercised; (b) `rememberModalBottomSheetState` /
`ModalBottomSheet` — Material 3 experimental API, and `ChatScreen` is already `@OptIn`; (c) the
`Icons.Default.Mic` import, which needs `material-icons-extended` (the convention plugin
supplies it).

**A `NoEngine` result at check 1 is a real possible outcome, not a bug.**
`docs/M2_DESIGN_NOTE.md` §9 predicts it: HyperOS does not always ship a recognition engine. If
the button never appears, install Google's app or enable its speech service, and if that is
impossible then Vosk becomes the only path to running the gate locally — which is a schedule
problem, not a design one.

### Step 4 — the gate. After 3b passes.

Unchanged, and see below.

### Step 4 — the gate. **← THE ONLY THING LEFT IN M2.**

**Run `docs/M2_GATE.md`.** It holds the fixed 161-word passage (67–77 s), the pre-run setup,
the per-run recording table and a reading for every outcome. Do not improvise a passage — the
three runs are only comparable against the same source, and the gate's endpoint word (`بعد`)
is fixed in advance on purpose.

Ruling: `ANSWERS.md` Part 1 §1, refined by Part 5 §M2-3. Read straight through, **three
times**. Record whether the transcript ended early and **at which exact word**, the `Segment`
count, the accepted tag, and rough word accuracy. Publish all three runs in §7. Auto-restart
is an allowed mitigation but is reported, not hidden. Vosk is pre-approved and needs figures,
not a new decision. **Two clean runs out of three is a fail.**

### ✅ THE GATE RAN 2026-08-06 AND PASSES. §7 carries the numbers.

Reached the final word all three times, on ~480 words continuous. `SpeechRecognizer` stays;
Vosk is not triggered. **Accuracy — 66 % — is now the open question, and it is a different
question from the one the gate was written to answer.**

### Step 5 — the dialect test. **← NEXT. Costs one recording and no build.**

**Do not change the engine, the tag or anything else until this is done.** The gate passage is
literary MSA; `ar-SY` tells the recogniser to expect Syrian colloquial; a short colloquial
greeting already transcribes perfectly on the same connection. The 66 % may be measuring a
mismatch between the test and the tag rather than a limit of the engine — and **the product's
real input is spontaneous Levantine speech, not read MSA**.

**The test:** with Developer Mode on and the network up, **speak** — do not read — for 60–90
seconds, in the Arabic you would actually use to describe an idea to this app. Then judge the
transcript by one question: *would someone who was not in the room understand what you meant?*

### ✅ RAN 2026-08-06. Meaning survives; words go missing. Details in §10 `2026-08-06-H`.

14 segments · 84 words for 60–90 s (natural speech is 120–150 wpm). No meaning-destroying
substitutions — the intent is fully recoverable — but **roughly a third to a half of the words
are simply absent**, lost in the gap at each of the 14 restarts.

### Step 6 — segmented session. **← NEXT. One build, one number.**

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

Developer Mode on, network up, Arabic. **Speak — do not read — for about a minute, the same
way as before.** Then read one number.

| `مقاطع التفريغ` | Reading |
|---|---|
| **1 or 2** | `EXTRA_SEGMENTED_SESSION` is honoured. The seams are gone. Compare the transcript to the last one — this is the fix |
| **still ~14** | The engine ignores the extra, as Google warns it may. Nothing is broken and nothing regressed; the restart ladder behaved exactly as before. **Then the seam is structural to `SpeechRecognizer`, and the engine question is genuinely open** — and Vosk becomes interesting for a new reason: it is continuous and has no utterance ceiling, so it has no seams to lose words in |

Nothing else changed in this build, so the number is unambiguous either way.

### ✅ RAN 2026-08-06 — **the engine ignores it. 13 segments.** See §10 `2026-08-06-J`.

The seam is structural to `SpeechRecognizer` on this device: silence hints set, platform
continuous mode refused, restart already as tight as `Handler.post` allows. Two runs agree at
~5–6 words per segment. **Both transcripts remain intelligible.**

### Step 7 — the engine decision. **← OPEN. The owner's, not an agent's.**

M2's own gate passed. What is in front of the owner is not a gate failure but a quality
judgement, and it has three shapes:

| | Cost | What it buys | What it risks |
|---|---|---|---|
| **Accept, move to M3** | none | M3 Clarify is a conversation and will surface what the transcript lost; the text is editable by design (`docs/M2_DESIGN_NOTE.md` §1) | M3 built on lossy input |
| **Vosk** | ~30 MB, offline-only | continuous recognition — **no utterance ceiling, so no seams and no deletions**. Pre-approved in `ANSWERS.md` Part 1 §1 | its Arabic model is MSA-trained and small; trades today's deletions for tomorrow's substitutions, and Levantine is the weak case |
| **Cloud STT via BYOK** | a build; needs `AudioRecord`, a key, a network | best Arabic-dialect accuracy available, no utterance ceiling, and it fits the app's existing BYOK provider architecture exactly | **activates the deferred raw-audio ruling** — `ANSWERS.md` Part 5 §M2-2 deferred that toggle "until the first engine that owns the audio stream", and this is that engine |

### ✅ RULED 2026-08-06 — **cloud STT, BYOK, audio deleted immediately.**

`ANSWERS.md` Part 6 §M2-10. Recommendation and reasoning in §10 `2026-08-06-K`:
**Deepgram Nova-3, `ar-SY`, streaming** — `ar-SY` is a first-class code there, Arabic is served
in both streaming and batch, ~$0.46 per hour of dictation, and streaming means **the live text
while speaking survives** (an earlier claim in this file that it would be lost was wrong).

### Step 8 — build the Deepgram `SpeechToText`. **← NEXT.**

**Key: DONE** — the owner holds a Deepgram key as of 2026-08-06. It goes into the app the same
way every other key does: **typed into Settings on the phone**, Keystore-encrypted. It does not
pass through an agent, a file, or this repo (hard constraint 3).

**Design note: `docs/DEEPGRAM_DESIGN_NOTE.md`. Its four §8 decisions were taken by the agent,
not the owner** — they were implementation judgements with recommendations already attached, and
putting them to him was offloading. Reversible on request; reasoning in §10 `2026-08-06-M`.

### Step 8a — the key card. ✅ **DONE — built and verified on device 2026-08-06.**

The owner built, installed, entered his Deepgram key and followed the checks. The card renders,
the key is stored encrypted, and it survives a full restart. **Nothing consumes it yet, by
design.** Step 8b is next.

---

**Historical — the acceptance that was run:**

Deliberately split from the engine, as M2 split 3a from 3b: "the key saves and survives a
restart" should pass or fail before any audio or socket code exists to confuse it.

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

| # | Do this | Expect |
|---|---|---|
| 1 | Open Settings | A card **«تفريغ الصوت — Deepgram»**, above the provider list, below the language card |
| 2 | Paste the key | It appears masked; the eye toggle reveals it; a line confirms it is stored |
| 3 | Close the app fully and reopen Settings | **The key is still there.** This is the only check that matters — it proves the encrypted store round-trip |
| 4 | Switch the app to English | The card reads in English |
| 5 | Use voice as before | **Unchanged.** Nothing consumes this key yet |

There is no verify button on purpose: verifying needs the connection that does not exist yet.

**If the build fails, suspect:** the new `SpeechKeyCard` composable's imports — it reuses
`Icons.Default.Key`, `PasswordVisualTransformation` and `KeyboardOptions`, all already imported
by `ProviderCard` in the same file, so a missing import would be surprising.

### Step 8b — the Deepgram engine. ✅ **VERIFIED ON DEVICE 2026-08-06.**

Bars move, «تمّ» closes cleanly, `ar-SY · عبر الشبكة`, **14 segments / 38 seconds**. Key deleted
→ device engine. Airplane mode with a key → network error; without one → Google's language
error. The transcript quality against the Google engine is not a close call.

`2026-08-06-Q` verified too: a wrong key names the key, airplane mode names the network.

### ✅ M2 IS CLOSED — 2026-08-06

Every item below is done and owner-verified on `2312DRA50G`.

| Item | Result |
|---|---|
| Voice capture, editable transcript, denial handling | ✅ six checks, `2026-08-06-D` |
| The five-bar waveform moving with real audio | ✅ real RMS over PCM we own, in dB |
| The 60–90 s gate (`ANSWERS.md` Part 1 §1) | ✅ **passed** — reached the end three times on ~480 words continuous |
| Arabic accuracy fit for M3 | ✅ Deepgram `ar-SY`, streaming, with punctuation |
| Device engine retained for no-key / no-network | ✅ falls back, verified |
| Typed Arabic errors for every failure | ✅ missing key, wrong key, no network, no engine |
| Font licence in the binary | ✅ Licences card, both locales |
| Key-safety audit | ✅ pass, `2026-08-06-R` |

**Final dialect test, Deepgram, 2026-08-06: 45 words · 17 segments · 49 s · `ar-SY` · عبر الشبكة.**
Punctuation present, Levantine rendered as Levantine («بدي احكي لك», «هيك», «بس»), no
meaning-destroying substitutions. Owner's verdict: many errors, **the idea came through** — which
is the gate's own criterion and his to apply.

**What the numbers could not settle, stated rather than glossed.** 45 words over 49 seconds is
55 wpm, against 120–150 for natural Arabic — but **spontaneous speech has no ground truth**, so
that figure cannot separate "words were lost" from "he spoke slowly with thinking pauses". The
errors present read as substitutions and garbling, not the whole-phrase holes Google left. The
one measurement that would settle it is the fixed MSA passage on Deepgram, where ground truth
exists and the answer would be directly comparable to Google's 66 %. **Not required, and
deliberately not requested:** no decision now turns on it. It is here so a future agent knows
the question was seen and priced rather than missed.

---

**Historical — how M2 closed:**

### Step 9 — close M2.

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

| | Item | State |
|---|---|---|
| 1 | **OFL font attribution** at the foot of Settings | ✅ built in `2026-08-06-R`, **not yet run** — open Settings, scroll to «التراخيص», expand the licence, check both locales |
| 2 | **Key-safety audit** (`ANSWERS.md` Part 3 §C) | ✅ **PASS**, re-run 2026-08-06. Re-run again before any APK leaves the machine |
| 3 | **Dialect test on Deepgram**, for the record | owner's, one recording. `docs/M2_GATE.md`, second half |
| 4 | M2 closes | after 1 and 3 |

**Judge the dialect test on words-per-minute against the duration, not on segment count**
(`2026-08-06-P`). Deepgram's segments are finalisation points inside one unbroken stream, not
restarts, so its count and Google's are not the same measurement. The prior figures to beat are
**84 words / 14 segments** and **63 / 13** on Google, both with roughly a third of the speech
missing.

### Then M3 — Clarify and Forge — opens.

The product's soul, and the reason a day was spent on transcript quality: Clarify interrogates
whatever the microphone produced, so everything lost here is lost there too.

---

**Historical — the acceptance that was run:**

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

Developer Mode on, network up, Arabic, key already entered in 8a.

| # | Do this | Expect |
|---|---|---|
| 1 | Build | The only new dependency is `ktor-client-websockets`, `:speech` only |
| 2 | Tap the microphone, speak a sentence | Text appears **while you speak**, waveform moves. If either is missing the socket is not carrying interim results |
| 3 | Press «تمّ» | The last sentence is **not** lost — `CloseStream` flushes it |
| 4 | Read the Developer Mode lines | `ar-SY · عبر الشبكة`, and a **duration that is not zero**. **Do NOT judge this run by segment count** — see `2026-08-06-P`: Deepgram's segments are finalisation points inside one unbroken stream, not restarts, so 19 of them is healthy. Word rate against duration is the measure now |
| 5 | Delete the key in Settings, dictate again | Falls back to the device engine, behaves exactly as before. **Not** an error — no key means Deepgram was never selected |
| 6 | Airplane mode with the key present | Arabic network error, no crash, no silent downgrade |
| 7 | **`docs/M2_GATE.md` dialect test** | Compare against **13 segments / 63 words** and **14 / 84** |

**If the build fails, suspect in this order:** (a) `ktor-client-websockets` — first new artifact
since `androidx.appcompat`, and hard constraint 2's whole history is a hallucinated Ktor
dependency, though this one was checked on Maven Central first; (b) the `@Named("speech")`
qualifiers — `DeepgramSpeechToText` must not resolve `:core-data`'s client, which has no
WebSockets plugin; (c) `webSocketSession` / `Frame` imports from `io.ktor.client.plugins.websocket`
and `io.ktor.websocket`.

**If it builds but no text appears:** the socket opened or it did not. `EngineConfig` is emitted
immediately after the handshake, so if the Developer Mode line names `ar-SY · عبر الشبكة` the
connection is live and the fault is in parsing; if the line never appears, the handshake failed
and the key or the URL is wrong.

**Do not delete `AndroidSpeechToText`.** It is the only path that works with no network and no
key, and a friend without a Deepgram account is exactly the user `ANSWERS.md` Part 3 protects.
Which of the two is used, and how the app chooses, is a design-note question — not something to
settle by deleting the fallback.

### Parked — not part of the M2 sequence

1. **Anthropic, when the card allows.** The default brain for M3 Clarify/Forge; promo credit
   expires **19 Sep 2026**; `AnthropicProvider.parseSSELine` (`2026-08-03-C`) and its
   `verify()`/error path (`2026-08-03-E`) were both rewritten and have never run. Budget for
   it to fail on first contact; every other provider did.
2. **Gemini thinking latency** (§9) — 13.6 s for a one-word greeting. Verify the field name
   against the `generateContent` reference before writing it.

---

## 9. DEFERRED QUEUE — do not touch unless assigned

- ~~**`feature-settings` depends on `feature-chat`**~~ — **DONE 2026-08-04-B, edits complete,
  awaiting the owner's build.** `AiErrorMessage.kt` moved to `core-ui` and the strings with it
  under `error_*`; the `implementation(project(":feature-chat"))` line is gone. Kept here as a
  closed row rather than deleted, because the rule it established still binds: **feature
  modules are siblings.** Anything two of them need goes in `core-ui`. Never make one depend
  on another — the day `feature-chat` needs something back, Gradle rejects the cycle outright.
- ~~**The engine turn count is computed and not displayed.**~~ **DONE 2026-08-07-D** — it is in
  the Clarify screen's Developer Mode line beside the resolved model name.
- ~~**`DiagnosticsPanel` is private to `feature-chat`**~~ · ~~**Clarify requests do not set
  `AiRequest.diagnostics`**~~ — **BOTH DONE 2026-08-07-E.** The panel is in
  `core-ui/diagnostics/`, its strings renamed `chat_dev_*` → `dev_*`, and Clarify captures its
  request under Developer Mode. Kept as closed rows rather than deleted, because the rule they
  turn on still binds and this is the **second** time it has been paid for: **anything two
  feature modules need goes to `core-ui`, never to a peer** (`2026-08-04-B` for error wording,
  this entry for diagnostics). The tell both times was the same — a composable or a string that
  a second screen wanted, and a tempting `implementation(project(":feature-chat"))` that would
  have compiled today and become a Gradle cycle the first time chat needed something back.
- **`docs/ARCHITECTURE.md` contradicts the tree in four places, found 2026-08-07 while writing
  the M3 note. Not corrected there — §0 rule 6 confines an agent to the files its task names,
  and that task named the M3 note and this file.** All four are §0 rule 5 cases: the code wins.
  1. **§5's `Session` object opens with `originalAudioRef`, a field that can never be filled.**
     `ANSWERS.md` Part 6 §M2-10 deletes raw audio the moment the transcript returns, with no
     toggle, and the streaming Deepgram build never writes audio to storage at all — microphone
     buffer straight to socket. This is not pedantry: «لا يحفظ التطبيق الصوت» is the sentence the
     whole microphone rationale rests on, it has already been corrected twice for describing an
     intention rather than the code, and an architecture document promising an audio reference is
     the road by which it becomes a lie again. Delete the field with a line saying why.
  2. **§2 lists a `:feature-voice` module.** Overruled by `ANSWERS.md` Part 5 §M2-4 — the voice
     UI lives in `feature-chat`.
  3. **§3's `SpeechToText` sketch** (`EngineId`, `AudioClip`, `Language`, `Flow<Transcript>`)
     bears no resemblance to the interface actually in `core-domain/speech/`.
  4. **§3's `AiProvider` sketch has no `verify()`.** It has had one since M1 and it returns
     `AiError?` since `2026-08-03-E`.
- **The provider selection is not persisted.** `ChatUiState.selectedProvider` is
  `ProviderId.GEMINI` and `selectProvider` writes only to in-memory state, so every app restart
  returns to Gemini regardless of what was chosen. `AppPreferences.selectedModels` already
  persists the *model* override through SharedPreferences, so the provider is the odd one out and
  the fix copies an existing pattern rather than inventing one. Low urgency — see the correction
  below — but it is a real "the setting I chose did not survive" defect.

  > **CORRECTION, 2026-08-07, by the owner.** The first version of this item said the default put
  > him "on the one provider he cannot reach". **That is wrong: he runs a VPN and reaches Gemini
  > normally.** The geo-block in §7 is real and unchanged — it is a property of his location, not
  > of his setup — but he works around it deliberately and Gemini is a working provider for him.
  > Left in place and corrected rather than rewritten, because the reasoning that produced the
  > error is worth more than a clean file: **I inferred his configuration from a fact in this
  > document instead of asking, and §7's line said what his location does, not what he does.**
  > Fifth time in this project that a confident cause was published from evidence that only named
  > a symptom (`2026-08-04-I`, `2026-08-06-A`, `-B`, `-Q`) — and the first time the missing fact
  > was about the owner rather than the device.

- **Any measurement that mixes providers — or mixes VPN and no-VPN — is not a measurement.**
  Raised 2026-08-07, immediately before the M3 gate. §6 records that a VPN roughly **doubles**
  latency (219–700 ms becomes 542–977 ms), and `docs/M3_DESIGN_NOTE.md` §5 publishes **seconds**
  as one of the two numbers the gate is judged on. So a gate run over a VPN carries the VPN in its
  headline figure. This is `2026-08-03-G`'s lesson one milestone later: a Gemini success recorded
  without noting the VPN badge in the screenshot, caught only because someone looked again.
  **The gate should run on DeepSeek, no VPN** — every M3 measurement so far was on
  `deepseek-v4-flash` (the owner's own screenshots), so that is the only choice that makes the new
  numbers comparable to the ones already in this file. Whatever is chosen, **all runs use the same
  one and the file says which**.
- **Chat sends no system prompt at all, and an Arabic-first app therefore has no instruction to
  answer in Arabic.** Found 2026-08-07 the only way it could be: the owner sent a message
  containing just «.» and DeepSeek answered in fluent **Chinese**. Not a bug in this app —
  `ChatViewModel.sendMessage` builds `AiRequest(model, messages, stream, diagnostics)` and leaves
  `systemPrompt` null, which the captured request body in his screenshot confirms directly rather
  than by inference. Given a content-free prompt and no instruction, a Chinese vendor's model
  falls back to its own default language, which is correct behaviour for the request we sent.
  **Why it still matters:** the reply is then in the history, so it keeps steering later turns
  until the chat is cleared — and a friend who types something ambiguous gets the same. **Not a
  defect to fix quietly: whether chat pins an Arabic system prompt is a product call** (it costs
  tokens on every request and changes what the app is), and M3 has already shown the plumbing
  works — Clarify is the first thing in this repo ever to use `systemPrompt`.
- **Four places now collect an `AiProvider.complete()` stream and map it to their own events** —
  `ChatViewModel`, `ClarifyEngineImpl`, `PromptForgeImpl` and `ClarifyViewModel.execute`. Each
  maps to a different event type so they are not duplicates in the copy-paste sense, but the
  shape — rethrow `CancellationException`, classify with `toAiError`, count chunks, time the
  first token — is now written out four times, and `2026-08-07-J` was where it became four.
  **Not extracted yet on purpose**: the four differ in exactly the parts an abstraction would
  have to parameterise, and a helper invented before M4's router is known would be guessing at
  the fifth caller. Recorded so the next agent sees a deliberate decision rather than drift.
- Real `TokenUsage` parsing instead of zeros on `[DONE]`. Developer Mode will display `0`
  until this is done; that is expected, not a new defect.
- `verify()` ignores the user's model override — it always sends `id.defaultModel`, because
  `AiProvider.verify(apiKey)` has no model parameter. So typing a bad model name in Settings
  still shows a green tick, and chat then fails. Fixing it means widening the interface;
  worth doing when A3's typed errors land, since the two touch the same surface.
- Gemini `verify()` uses `generateContent` while chat uses `streamGenerateContent`. Close
  enough today, but it does not prove the streaming endpoint works for that key.
- **`GeminiProvider.parseSSELine` reads `parts[0]` only.** `gemini-3.5-flash` is a thinking
  model, so a chunk's `parts` array can carry a thought part ahead of the text part — the
  parser then finds no `text`, returns null, and the chunk is dropped silently. Symptoms
  would be an empty reply on HTTP 200, truncated text, or reasoning leaking into the answer.
  Iterate all parts and skip those marked `thought` instead of indexing `[0]`.
- **Google now documents streaming only via the new Interactions API** (`/v1beta/interactions`,
  `step.delta`/`delta.text` shape). `streamGenerateContent` is not on the deprecation
  schedule and the owner's `verify()` — which calls `generateContent` — succeeded on device
  2026-08-03, so the classic path is alive. Treat this as a migration to plan, not a fire.
  Ref: <https://ai.google.dev/gemini-api/docs/streaming>
- `readUTF8Line` is deprecated in Ktor 3.5 — migrate to `readLine`.
- `MenuAnchorType` is deprecated — `ChatScreen.kt` lines 36 and 91, renamed to
  `ExposedDropdownMenuAnchorType`. Warning only; seen in the 2026-08-04 build, pre-existing.
- Settings has no back button (system back only).
- **Settings gives no feedback that a provider key was saved.** Still open for the four provider
  cards. The Deepgram card added in `2026-08-06-M` *does* confirm storage, so the fix is now a
  matter of copying an existing pattern rather than inventing one.
- **`verify()` has no Deepgram equivalent.** The Deepgram card stores a key and cannot tell you
  it works; the first thing that proves it is a dictation. Ruled a real check in
  `docs/DEEPGRAM_DESIGN_NOTE.md` §8 D3 and not built, because verifying needs a socket and
  `2026-08-06-M` deliberately shipped the card before the engine. Now that the engine exists,
  this is a small job — and `SttError.InvalidKey` already carries the wording.
- Dead manual `KeyGenerator` block in `EncryptedKeyStoreImpl` — already removed; verify no
  reintroduction.
- `statusBarColor` deprecation in `Theme.kt` — now `@Suppress`ed rather than fixed (2026-08-04-C).
  The real fix is `enableEdgeToEdge()` in `MainActivity` plus insets handling in each screen;
  that is a layout change, not a colour one, and belongs with M5 polish.
- ~~**Four derived error tones need the owner's ratification**~~ — **RATIFIED 2026-08-06**
  (`ANSWERS.md` Part 6 §M2-5). `ErrorLight`, `ErrorDark`, `ErrorPale`, `ErrorDeep` in
  `Color.kt` (added 2026-08-04-D) stand. Kept as a closed row rather than deleted, because the
  rule it settles still binds: **a contrast ratio computed for text does not predict how the
  same colour reads as a large fill** — check the role, not just the number. That is the
  failure `2026-08-04-D` fixed and the one waiting for the M2 waveform. Also worth knowing: `primary` indigo400 `#7F77DD` is
  deliberately **not** a filled-button background — white on it is 3.76:1 — which is why the
  dark scheme uses indigo200 for `primary` and indigo700 for `primaryContainer`.
- ~~**The bundled font attribution**~~ — **DONE 2026-08-06-R, verified on device in both
  locales.** A Licences card at the foot of Settings; text bundled at
  `feature-settings/res/raw/ibm_plex_sans_arabic_ofl.txt`. Kept as a closed row because the
  rule still binds: **`res/font/` accepts font resources only and rejects that filename**
  (`2026-08-04-F`), and any future bundled asset with a licence faces the same choice.
- **The app captures what we sent and never what came back.** `AiChunk.Meta` carries
  `endpoint` and `requestBody` only, and `AiError.Unknown.detail` is deliberately never
  rendered (`AiErrorMessage.kt`). So when a provider fails for a reason its **own message
  explains**, that sentence is discarded and the user reads «حدث خطأ غير متوقّع (400)».
  Concretely: **Anthropic answers an empty credit balance with HTTP 400**, body
  `{"type":"invalid_request_error","message":"Your credit balance is too low to access the
  Anthropic API…"}` — a billing state wearing a bad-request status. `classifyHttpError` has
  no branch for it, so it lands in `Unknown` and the one fact that would have solved it is
  dropped. Fix has two halves and they are separable: (a) an `AiError.InsufficientCredit`
  branch matched on the body marker, exactly as `RegionBlocked` already matches "location is
  not supported"; (b) show `detail` **in the Developer Mode strip only**, where it is already
  redacted by `redactSecrets`. This is the same lesson as `2026-08-04-I`, one layer up: a
  status code names the symptom the platform saw, not the cause.
  Ref: <https://github.com/continuedev/continue/issues/5499>
- **Themed-icon (`<monochrome>`) rendering is unverified.** Xiaomi HyperOS does not expose
  Android's "Themed icons" toggle, so acceptance check 6 of §8 Step 2 could not be run on the
  test device (2026-08-04). The layer is present and well-formed; it is simply unobservable
  here. Any friend on a Pixel or stock-Android launcher closes this in ten seconds.
- Give `build-logic` access to the version catalog so the Compose BOM stops being
  duplicated as a string literal.
- Unused `jsonArray` import in `BaseHttpProvider.kt` (warning only).
- Cosmetic SDK warnings (`SDK XML version 4`, cmdline-tools location, `platform-tools
  package is not installed`) — all harmless. Two more of the same kind appeared in the 2026-08-07
  log and are recorded so nobody "fixes" them: `Observed package id 'platform-tools' in
  inconsistent location …\platform-tools-2` and the same for `cmdline-tools;20.0` in `…\tools`.
  Hard constraint 1 forbids SDK Manager and the hand-written `package.xml` files are exactly what
  makes these paths look wrong to the tooling while working perfectly.
- **`Kotlin does not yet support 25 JDK target, falling back to Kotlin JVM_24 JVM target`** —
  seen 2026-08-07, warning only, and the build compiles. The daemon is running on JDK 25 while
  §3 pins the *output* to JVM 17, which is unaffected. **Do not chase it.** The knob is
  `org.gradle.java.home` in `gradle.properties`, which hard constraint 1 names as load-bearing
  and forbids touching.
- **Wi-Fi device pairing does not work: "ADB Version Too Low".** Surfaced 2026-08-03 after an
  Android Studio update, which revealed rather than caused it — `platform-tools` was never
  installed and `adb.exe` sits loose in the SDK root (§6), so it is older than the IDE now
  expects. The dialog offers "Open SDK Manager"; **do not take it.** Hard constraint 1 forbids
  it, the SDK's `package.xml` files are hand-written, and SDK Manager operations have
  destroyed this build before. USB installs work — `installDebug` succeeded minutes before the
  dialog appeared. Wi-Fi pairing is a convenience with a bad risk trade; if it is ever wanted,
  do it after M1 closes and back up the SDK directory first.
- Markdown / code-block rendering in chat answers, with code forced LTR and
  left-aligned. Not needed yet; will matter once answers routinely contain code.

---

## 10. CHANGE LOG — newest first, append only

### 2026-08-07-O · Claude (Cowork) · answer buttons · a label that outlived its behaviour
**Files:** `core-domain/clarify/ClarifyTurn.kt` · `feature-clarify/…/ClarifyEngineImpl.kt` ·
`…/ClarifyPrompt.kt` · `…/ClarifyViewModel.kt` · `…/ClarifyScreen.kt` ·
`feature-clarify/res/values{,-en}/strings.xml` · `PROJECT_STATE.md` (§8, §9, this entry)

`2026-08-07-N` built. Checks 1, 2 and 5 pass; **checks 3 and 4 were not run because the owner
could not tell what they were asking** — see below. One real defect and one feature.

**The defect: «الجواب — بالإنجليزية» was a lie by the time it shipped.** `-M` made the forged
prompt ask for an Arabic reply; the label above the answer box still announced English, and so did
the note under the buttons. The owner reported it as «خطأ» and he was reading the screen
correctly. **A label that describes what the code used to do is a wrong label** — the identical
fault as `2026-08-06-D`, where the Developer Mode line went on naming the previous language after
the toggle, and that entry's own conclusion applies again: a diagnostic that is confidently wrong
is worse than none, because it is believed. Both strings corrected, with the reason left in the
XML so the next person to change the behaviour sees what else has to move with it.

**The feature: a question with a small set of likely answers now offers them as buttons.** The
owner asked for it directly, having spent the interrogation retyping options that were already on
screen.

**Plain `- ` bullets carry them, not a `[[…]]` marker, and that is the design rather than
laziness.** Options stream token by token like everything else. A marker would sit visibly in the
text as `[[خيار]] نعم` for a second before the turn ended and it could be stripped; **a bullet
list reads correctly while it is still arriving** and needs no cleanup at all. Parsed only at the
end, only trailing, only consecutive, only in a question, only two to four — one bullet is not a
choice and a long list is a form. Anything else falls back to a plain open question, which is
always safe.

**The buttons are above the writing box and never instead of it, and this is the part that
matters.** Gate run 1's value was that the interrogation carried the owner's idea somewhere he had
not started from — *"تم تحويل السؤال بالكلية إلى قضية أخرى… وكل ذلك باختياري"*. **Every answer
that produced that was one no list would have contained.** A choice set that replaced free text
would have removed the one property the gate had just measured as valuable. The prompt is told the
same thing: no options for open questions.

**Tapping a button goes down the identical path as typing.** `replyWith` is what `reply` now
calls, so the transcript, the session and everything FORGE later reads are the same either way. A
separate channel for "structured" answers would have been two histories to keep in sync.

**And the reason two checks went unrun is worth more than the checks.** They read *"the first turn
opens by restating your idea"* and *"a question that moves the subject says so"* — descriptions of
behaviour in the abstract, with nothing named to look at. §0 rule 9 is usually applied to the prose
*around* the table; it applies inside it too. **One instruction, one visible thing, per row** —
re-asked in §8 Step 4d that way, and the rule written down there rather than in this entry, because
that is where the next agent will be standing when it matters.

### 2026-08-07-N · Claude (Cowork) · gate run 1 · a prompt that asked for a prompt
**Files:** `feature-clarify/…/ForgePrompt.kt` · `…/ClarifyPrompt.kt` · `…/ClarifyScreen.kt` ·
`PROJECT_STATE.md` (§7, §8, this entry)

`2026-08-07-M`'s seven checks passed. **Gate run 1 went to path (ب)**: path (أ) came back
"سطحية وعلى قدر السؤال" while the interrogation carried the idea into a different and better
question — the owner's own, arrived at through the answering. Full result in §7. **This is one run
of three and the gate is not passed**; recording it as a pass would be the exact softening
`docs/M3_GATE.md` §6 was written before any run to prevent.

**The English answer, and the cause was not the one I had already fixed.** `-M` made the OUTPUT
CONTRACT require an Arabic reply, and it did. His uploaded transcript shows what happened next:
his dictated idea was itself a request for *a prompt* — «قم بصياغة برومبت احترافي» — so FORGE
wrote a prompt whose OBJECTIVE was **"Construct a single, self-contained English prompt"**, and
faithfully placed the Arabic requirement inside *that* prompt's output contract. Executing it
produced another English prompt. **The instruction was obeyed one level too deep.**

Two rules, because there are two faults stacked here and fixing only the visible one would leave
the worse one in place:

1. **Never write a prompt that asks for a prompt.** When the idea says "build me a professional
   prompt", that describes the job FORGE is already doing — it is not the deliverable. The real
   goal is whatever the prompt was for. Without this, the app quietly adds a layer of indirection
   every time a user talks about prompts, which for this app's audience is often.
2. **The Arabic requirement is about the immediate reply**, stated in those words, and explicitly
   not about any document the reply might itself produce.

**Worth naming: a correct instruction can be obeyed at the wrong level.** That is not a wording
problem and not a model failure — the model did exactly as told. It is the same family as
`2026-08-06-P`'s dialect tag, which was resolved in one place and read in another; here the
resolution was right and the *scope* was wrong.

**The owner's finding is sharper than anything the design note predicted: «سلاح ذو حدّين».** The
interrogation genuinely improves the idea *and* can carry it off its original axis. He noticed
only because he was watching every single exchange. **Not prevented — the same property produced
the run he enjoyed — but made to announce itself:** if a question would shift the subject, the
engine now says so in one sentence and asks whether that is intended. Silent drift becomes an
offer the user can decline.

**And the first turn now restates the idea in one line before asking anything.** Run 4 found that
Clarify **does not notice** transcription damage — it reached the intended meaning anyway, through
the answers rather than through detection. `docs/M3_DESIGN_NOTE.md` §2 had assumed detection would
be the mechanism. The restatement costs no extra turn, surfaces a garbled transcript at the one
moment it is cheapest to fix, and anchors the axis against the drift above. One line, two faults.

**`P` and `R` on the copy buttons.** Two identical icons side by side, and no way to tell which
took the prompt. The content descriptions were correct and served a screen reader and nobody else.
**A control that has to be guessed at has not been labelled** — the same lesson as `-M`'s four
defects, which is that every check so far asked whether output was *correct*, never whether it
could be *used*.

**Status:** edits complete, **not built**. Five checks in §8 Step 4c, then gate runs 2 and 3.

### 2026-08-07-M · Claude (Cowork) · the gate could not be run — four defects, and the predicted one arrived
**Files:** `core-domain/clarify/ClarifyTurn.kt` · `core-ui/…/input/SubmitOnCtrlEnter.kt` (new) ·
`feature-clarify/…/ClarifyPrompt.kt` · `…/ForgePrompt.kt` · `…/ClarifyEngineImpl.kt` ·
`…/ClarifyScreen.kt` · `feature-clarify/res/values{,-en}/strings.xml` ·
`feature-chat/…/ChatScreen.kt` · `docs/M3_GATE.md` (§5 rewritten) ·
`PROJECT_STATE.md` (§7, §8, this entry)

`2026-08-07-L` built and the timer works. **The gate then could not be run at all**, and the four
reasons were not complaints — every one was a defect.

**1 · «أسئلته التي لا تنتهي» — the interrogation would not stop.** This is the exact risk
`docs/M3_DESIGN_NOTE.md` §9 named as the highest in the milestone: *"استجواب مُرهِق … ميزة يتعلّم
المستخدم تجاوزها ميّتة"*. **Predicted, and still shipped**, because the prompt told the model to
ask one thing per turn and never to declare readiness while giving it **no definition of enough
and no way to say it had finished**. Those two rules alone make an infinite loop. It now knows
that four things settle an idea — objective, real constraints, output shape, what is out of
scope — is asked to get there in three to five questions, and has a `[[كافٍ]]` marker.
`ClarifyTurn.Enough` renders in amber (BRAND §2's insight accent, spent once per interrogation)
and **still does not move the machine**: `BRAINING.md` §2.3 and ruling M3-1 keep readiness with
the user. The fix for an endless interrogation is letting it admit it is finished, not letting it
finish on the user's behalf.

**2 · Everything came back in English.** Ruling M3-2 says translation is M4's job, and I built to
that literally — leaving the owner unable to judge an Arabic answer against an English one, which
also makes the gate's paired comparison unjudgeable. **The root fix costs one line and no
milestone scope: the forged prompt's OUTPUT CONTRACT now requires the answer be written in
Arabic.** Nothing is translated, so M3-2 is untouched; the answer is simply generated in the right
language. The ruling was about *translation*, and I had read it as being about *English*.

**3 · The answer could not be copied.** It was a plain `Text`; the prompt beside it was copyable
only because a text field happens to be. Now inside a `SelectionContainer`, with copy buttons for
both. **An output the user cannot take out of the app is an output they cannot use** — and this
went unnoticed because every check so far asked whether the text was *correct*, never whether it
was *usable*.

**4 · The answer area was unreadable.** Conversation and forge panel each held `weight(1f)`, then
the panel split again between prompt and answer — two long documents in a quarter screen each.
`2026-08-07-J` called that split "the design"; it was the design for a screen whose answer nobody
had yet tried to read. Once the idea is mature the conversation now collapses and the forge takes
the whole screen, one tap from coming back. **Fourth layout fault in this screen, and the first
one that was a wrong judgement rather than a wrong parent.**

**And Ctrl+Enter, requested in the same message.** Enter still inserts a newline — these fields
hold paragraphs, and `docs/M2_DESIGN_NOTE.md` §1 made them editable for exactly that reason, so
Enter-sends would mean sending half a thought. `submitOnCtrlEnter` lives in `core-ui` because Chat
and Clarify both need it (§9's standing rule), uses `onPreviewKeyEvent` because the text field
consumes Enter before a normal handler sees it, and acts on key-**down** only so one press does
not send twice.

**`docs/M3_GATE.md` §5 rewritten: the owner did not understand the fourth run, and the fault was
mine.** It said "start from a damaged transcript" without saying how one is obtained — the answer
being *dictate normally and then do not fix the errors*, which is the one habit the instruction
was silently asking him to break. Now three numbered steps and one question.

**The pattern across all four: every check I wrote asked whether the output was correct. None
asked whether it could be used.** Correctness is what an agent can verify from a transcript;
usability is only visible to the person holding the phone. That is the split worth carrying into
M4.

**Status:** edits complete, **not built**. Seven checks in §8 Step 4a, then the gate.

### 2026-08-07-L · Claude (Cowork) · M3's code closes · the gate written as an instrument
**Files:** `docs/M3_GATE.md` (new) · `feature-clarify/…/ClarifyViewModel.kt` ·
`…/ClarifyScreen.kt` · `feature-clarify/res/values{,-en}/strings.xml` ·
`PROJECT_STATE.md` (§2, §7, §8, this entry)

Step 3c passed all six observations. **`retry()` repeats the right one of three actions at every
stage** — the interrogation survives a failed forge, the prompt survives a failed execution — and
the network errors are the typed Arabic ones rather than the generic fallback. **Nothing in M3 is
now unbuilt or unverified. Only the gate remains.**

**`docs/M3_GATE.md` turns §5 of the design note from an intention into an instrument**, which is
what `2026-08-06-E` did for M2 and the reason that gate could not be argued with afterwards. It
carries the fixed setup, the run order, recording tables to copy three times, the fourth run, the
fail criterion **written before any run**, and a reading for each of seven possible outcomes.

**Three decisions inside it worth defending.**

1. **The run order is lightbulb-first, send-second, and the file marks it as not reversible.**
   Sending clears the input; the lightbulb does not. Reversed, the transcript is gone and the
   second path runs on a *different dictation of the same idea* — at which point the comparison
   measures two transcripts rather than two paths. A procedural trap that would have produced
   confident, meaningless numbers.
2. **"Equal" counts as a loss for Clarify.** If the interrogation produces no clear difference it
   did not earn its time, and the benefit of the doubt belongs to the behaviour that already
   works. Stated up front so it cannot be softened once real numbers are in front of anyone.
3. **The fourth run is not one of the three.** It starts from a real transcript with its errors
   left in and asks one question: did Clarify *notice*? Its outcome is recorded independently of
   the gate's, because **an interrogation that builds confidently on a broken sentence forges a
   rigorous prompt for the wrong idea — the one failure in this milestone that looks like a
   success.**

**And one number the device now holds instead of the owner.** Developer Mode shows
«زمن الاستجواب · حتى الجواب» beside the turn count. `2026-08-06-J` is a whole entry about asking a
human for a figure the device already has, written after M2 produced two consecutive dictation
reports with no duration attached and neither could be interpreted. **Stamped once at
«نضجت الفكرة» and never recomputed** — re-forging does not change how long the interrogation took,
and a number that drifts after the event it names is worse than no number.

**Wall-clock on purpose**, not request latency: the question the gate asks is whether the
interrogation was worth the *user's* time, and their thinking and typing are part of what it cost.

**Status:** edits complete, **not built**. One line to confirm (the timing line appears after
«نضجت الفكرة»), then `docs/M3_GATE.md` — which is the owner's to run, not an agent's.

### 2026-08-07-K · Claude (Cowork) · M3's loop runs · a test that could not fail · the Gemini trap
**Files:** `PROJECT_STATE.md` (§7, §8, §9, this entry). **No code changed.**

`2026-08-07-J` built and **six of seven checks pass. M3's loop is closed on device** — speak,
interrogate, forge, execute — with the owner's earlier verdict on the prompts themselves standing:
"النتائج مذهلة حقاً".

**Check 7 is inconclusive because I wrote it badly.** It asked the owner to press «أعد المحاولة»
in airplane mode at three different stages and observe which action was retried. **In airplane
mode all three retries fail identically**, so the distinction the check existed to measure is not
visible from the outside. He reported the only thing that was: everything failed. That is the
correct behaviour and it is not a result.

**A test whose pass and fail look the same is not a test**, and that is a different failure from
the ones this file already records. `2026-08-06-D` is about a diagnostic that was confidently
wrong; `2026-08-07-G` about one that was confidently blank. This is a third shape: an instrument
pointed at something it cannot see. The rewrite in §8 Step 3c turns the network **back on** and
asks whether the interrogation survived — the same question, made observable, and needing no
rebuild.

**The second finding, and the correction that followed it.** He mentioned in passing that he was on
Gemini, and I filed a §9 item saying the default had stranded him on a provider he "cannot reach",
citing §7's geo-block. **The owner corrected it immediately: he runs a VPN and Gemini works for
him.**

**The reasoning error is worth more than the item was.** §7 says Gemini is geo-blocked *at his
location* — a fact about the place, which I read as a fact about his setup, and then built a
diagnosis on top without asking. This is the fifth time in this project that a confident cause was
published from evidence that only named a symptom (`2026-08-04-I`, `2026-08-06-A`, `-B`, `-Q`), and
the first where **the missing fact was about the owner rather than the device** — the one category
that cannot be settled by reading the tree, and therefore the one where asking was cheapest.

**What survives, and it is the part that actually matters before the gate.** §6 measured that a VPN
roughly **doubles** latency, and `docs/M3_DESIGN_NOTE.md` §5 publishes **seconds** as one of the two
numbers the gate is judged on. Add a provider choice that resets on every restart (§9, still true)
and three paired comparisons could each run under different conditions while the file records them
as one measurement. `2026-08-03-G` is the precedent: a Gemini success logged without noticing the
VPN badge in the screenshot. **So: gate on DeepSeek, no VPN** — the configuration every M3 figure
in this document was already measured on. Persisting the provider is filed in §9 as a real but
no-longer-urgent defect.

**Status:** documents only, **no rebuild**. Six observations in §8 Step 3c, then the gate.

### 2026-08-07-J · Claude (Cowork) · execute the forged prompt — M3's loop closes
**Files:** `feature-clarify/…/ClarifyViewModel.kt` · `…/ClarifyScreen.kt` ·
`feature-clarify/res/values{,-en}/strings.xml` · `PROJECT_STATE.md` (§7, §8, §9, this entry)

FORGE passed eight of nine checks, with the owner's verdict **"النتائج مذهلة حقاً"** on the
prompts themselves. This is the last piece of M3's own sentence: speak → interrogate → forge →
**execute**.

**Run on the Clarify screen, not handed back to the chat — and the reasoning is the interesting
part.** The chat would have given bubbles, streaming, error cards and diagnostics for free, and
that was the first design. It fails on one thing: the chat prepends whatever conversation is
already open, and **a forged prompt is self-contained by construction** — it carries its own
`# ROLE`, `# CONTEXT` and `# INPUT`. Prior turns can only contradict it. The escape would have
been to clear the user's chat first, which is destructive for a reason they never asked about.
`2026-08-07-F` had just finished documenting how sticky an unrelated history is: one Chinese reply
to a full stop went on steering every later turn. **Reversible on request**, and recorded here so
the choice is visible rather than inferred from the file layout.

**No system prompt on the execute call, deliberately.** The forged prompt *is* the instruction.
A second one would put two voices in one request and quietly undo whatever the framework decided —
which would be invisible, because the answer would still look fine.

**`retry()` now dispatches on a recorded `LastAction` instead of inferring from state.** Three
things on this screen can fail and each needs a different recovery, and the wrong one is
**destructive**: re-opening the interrogation throws the entire session away. `2026-08-07-H`
already fixed one version of this by checking `state == READY`; adding a third case would have
turned that into a condition chain one refactor from re-running the wrong branch. An explicit
enum costs three assignments and cannot drift.

**Two `weight(1f)` siblings, and that is the design rather than an accident.** Prompt and answer
split whatever the fixed controls leave, each scrolling internally. This screen has now produced
three layout faults from one root — two composables in one lazy item (`-D`), a growing child in a
region that cannot scroll (`-G`), and an unbounded prompt above the controls (`-H`) — so the
answer area was bounded before it was written rather than after it was reported.

**The answer uses `fallback = Ltr`, not `forced = Ltr`.** English is the right default while the
first tokens are still ambiguous, but if a model answers in Arabic anyway, detection must be
allowed to win — forcing it would render real Arabic backwards. The prompt field above it *is*
forced, because English is a rule there and not a guess.

**Airplane mode is deferred and carried forward, not dropped.** The owner asked to postpone it and
it is now check 7 of step 3b — where it does more work than it would have done alone, because it
is the only check that proves `retry()` repeats the right one of three actions.

**Status:** edits complete, **not built**. Seven checks in §8 Step 3b. **After it passes, the gate
is the only thing left in M3.**

### 2026-08-07-I · Claude (Cowork) · one compile error — and what the failure already proved
**Files:** `feature-clarify/…/PromptForgeImpl.kt` · `PROJECT_STATE.md` (§8, §9, this entry)

`2026-08-07-H` failed to compile with exactly one error:

```
PromptForgeImpl.kt:146:51 Argument type mismatch: actual type is 'CharSequence', but 'String' was expected.
```

**Cause: `StringBuilder.trim()` resolves to the `CharSequence` extension, not the `String` one.**
`ForgeReader` exposed its accumulator as a public `StringBuilder`, so `reader.body.trim()` read
correctly and returned the wrong type.

**Fixed at the API, not the call site.** `.toString().trim()` would have compiled and left the
next caller to hit the same thing; the reader now exposes `val body: String get()` and keeps the
builder private. `ClarifyEngineImpl` never had the bug because its accumulator is a local it
converts before use — the difference was not care, it was that one of them was published.

**Worth naming as a pattern, since this is the second type-shaped trap in two entries:**
`JsonNull.content` returning `"null"` (`2026-08-03-C`) and this one are the same shape —
**a type that is *almost* the type you wanted, silently.** Kotlin caught this one at compile time
because the destination was typed; the JSON one reached the owner's screen because `String` was
also what the wrong path produced.

**What the failure proved before the rerun, read off the build output rather than assumed.** It
reached `:feature-clarify:compileDebugKotlin` — so `:core-domain` compiled with the new FORGE
types, the `kotlinx-serialization-json` line on this module resolved, and
`res/raw/prompt_frameworks.json` passed the resource pipeline. **Three of the four suspects §8
listed are cleared**, and only Compose (`FilterChip`, `LazyRow`) remains untested, because the
build never got that far. A failed build is evidence, not a blank result.

**Two environment warnings recorded in §9 rather than acted on:** the SDK's "inconsistent
location" complaints about `platform-tools-2` and `cmdline-tools`, which are what hand-written
`package.xml` files look like to the tooling, and `Kotlin does not yet support 25 JDK target,
falling back to JVM_24` — a daemon-JDK warning that does not touch §3's JVM 17 output target. Both
lead to `gradle.properties` and SDK Manager, which hard constraint 1 forbids. **Recorded so the
next agent does not treat a working warning as a broken build.**

**Status:** edits complete, not built. §8 Step 3, unchanged — nine checks.

### 2026-08-07-H · Claude (Cowork) · FORGE — the English prompt
**Files:** `core-domain/clarify/PromptForge.kt` (new) ·
`feature-clarify/res/raw/prompt_frameworks.json` (new) · `feature-clarify/…/ForgePrompt.kt` (new) ·
`…/PromptForgeImpl.kt` (new) · `…/ClarifyViewModel.kt` · `…/ClarifyScreen.kt` ·
`…/di/ClarifyModule.kt` · `feature-clarify/build.gradle.kts` ·
`feature-clarify/res/values{,-en}/strings.xml` · `PROJECT_STATE.md` (§5, §7, §8, this entry)

Step 2c passed — the diagnostics panel opens and the captured body is real (§7). So the last
piece of M3's payload: «نضجت الفكرة» now writes the prompt.

**The model writes it; the app does not fill a template.** A local template could only concatenate
Arabic into English headings — a document in two languages instructing nobody. Choosing the
framework, translating the matured idea and filling `docs/PROMPT_FRAMEWORKS.md` §4's skeleton are
one act of writing, not three mechanical steps.

**The library is data, in `res/raw/prompt_frameworks.json`.** §5 of that document asks for exactly
this — "new frameworks can be added without code changes" — and a `when` over framework names in
Kotlin would have broken it on the first day. **Parsed with `Json.parseToJsonElement`, not
`@Serializable`**, which is not laziness: `@Serializable` needs the serialization *compiler
plugin* in this module, and `:speech` already parses JSON without it. One fewer plugin in a build
file is one fewer thing that can fail for a reason unrelated to the feature. `contentOrNull`
throughout, because `JsonNull.content` returns the string `"null"` and that cost a day in
`2026-08-03-C`.

**A malformed library degrades instead of crashing.** It ships inside the APK so it cannot be
corrupted in the field — but it exists to be hand-edited, and an empty list yields a forge that
still runs and lets the model choose freely. A format that turns a typo into a dead screen is a
format nobody edits.

**The framework and its reason are emitted before the prompt body finishes.** `ForgeEvent`
carries `FrameworkChosen` separately from `Completed` for that alone: a user watching an English
wall of text arrive with no idea why *that* framework was picked has been shown the output and
denied the reasoning, which inverts what §3.7 asks for.

**`maxTokens` is 8192 here, against 4096 everywhere else.** A filled skeleton is longer than a
chat reply and longer than a Clarify turn, and a prompt truncated mid-OUTPUT-CONTRACT is worse
than a short one because it looks finished. Worth knowing beside `2026-08-03-D`: if thinking mode
is ever enabled, reasoning is billed against this same budget.

**The same header-eating bug, written a second time and caught before shipping.** `ForgeReader`
buffers until `[[PROMPT]]`, and the first version discarded that buffer whenever the cap fired —
so a model that ignored the format would have lost the first 400 characters of its prompt, looking
exactly like a model quirk. `crossIntoBody` now takes `markerFound`: the text before a real marker
is header and is dropped; the same text with no marker is the answer and is kept. **`HeaderReader`
had this identical fault in `2026-08-07-C`, two days ago.** Writing it twice is why the parameter
carries a KDoc rather than a name.

**Controls above the prompt, not below it, and that is the third layout fault in this screen from
one root.** `2026-08-07-D` put two composables in one lazy item; `-G` put a growing child in a
region that cannot scroll; here an unbounded prompt above the swap chips would have pushed «أعد
الصياغة» off the bottom of a short screen, silently. The short fixed controls come first and the
field takes `weight(1f)`, so the prompt can only consume what is left. **Designed rather than
discovered, for once.**

**`retry()` had to learn where it is.** After the idea is declared mature the failure belongs to
the forge, and the old unconditional `start()` would have thrown away the entire interrogation and
begun a fresh one on the original transcript. A retry button that destroys the work it is
retrying is worse than no retry button.

**Deliberately not done: executing the prompt.** Ruling M3-2 says the answer comes back in English
until M4 translates; the screen says so in Arabic instead of leaving the user to wonder. That is
the last piece before the gate.

**Status:** edits complete, **not built**. Nine checks in §8 Step 3. Check 2 is the one that
cannot be faked: **the answers you gave during the interrogation must be visible inside the
English prompt** — otherwise FORGE is reading the idea and not the session.

### 2026-08-07-G · Claude (Cowork) · the diagnostics panel had the wrong parent
**Files:** `feature-clarify/…/ClarifyScreen.kt` · `core-ui/…/diagnostics/DiagnosticsPanel.kt` ·
`core-ui/res/values{,-en}/strings.xml` · `PROJECT_STATE.md` (§7, §8, this entry)

Owner reached the Clarify screen, found the diagnostics line, and could not open it. His
screenshot is the diagnosis: «أدوار الاستجواب: ١ · النموذج: deepseek-v4-flash», the divider, and
«الأجزاء: ٤٤ · أول جزء: ٩١٧ ms · الإجمالي: ١٥١١ ms». **The panel renders and the turn streamed in
44 pieces** — so nothing is wrong with the capture pipeline, the binding, or the model.

**The defect is where I put it: in the fixed header, above a `weight(1f)` list.** Expanded, its
request body is roughly a thousand characters of JSON. A `Column` gives its unweighted children
their full height first, so the header grew, the list was squeezed toward zero, and the JSON ran
off the bottom of a region that **does not scroll**. `ChatScreen` never had the problem because
its panel lives inside a message bubble, inside the lazy list — **same composable, same data, one
wrong parent.**

**Moved into the lazy list, and appended rather than prepended.** Appending leaves every existing
item's index untouched, so the auto-scroll effect keeps working without being re-derived — and
after a turn completes, the bottom is where the eye already is. This is the third layout fault in
this screen from the same root: `2026-08-07-D` put two composables in one lazy item (they would
have drawn on top of each other), and this one put a growing child in a region that cannot grow.
**A composable that was correct in its old home is not thereby correct in its new one** — the
reuse is in the rendering, not in the layout contract around it.

**A second candidate cause was not ruled out, so it was made legible instead of guessed at.** If
`ClarifyEvent.Meta` never arrives, `endpoint` and `requestBody` are empty strings and the expanded
panel shows two labels with nothing under them — which reads exactly like a tap that did nothing.
The panel now says so in red. **This project has published a confident cause from partial evidence
four times** (`2026-08-04-I`, `2026-08-06-A`, `-B`, `-Q`); the alternative is not a better guess,
it is an instrument that makes the next observation decide. The owner's next report now separates
the two outcomes without either of us reasoning about it.

**Status:** edits complete, **not built**. Five steps in §8 Step 2c, and step (e) has two named
outcomes that mean different things.

### 2026-08-07-F · Claude (Cowork) · step 2b results · a Chinese reply that was not a bug
**Files:** `PROJECT_STATE.md` (§7, §8, §9, this entry). **No code changed.**

`2026-08-07-E` built. **Checks 1, 2, 6, 7 and 8 pass**, with the chat panel behaving identically
after changing modules — `Chunks: 636 · first: 388 ms · total: 8594 ms`, endpoint, body and token
line all rendering. That was the refactor's real test and it is green.

**Checks 4 and 5 did not run, and the fault is in how they were written.** They name the CLARIFY
screen; the owner read the panel on the **chat** screen and reported the other two as not
understood. He was reading the instruction correctly — it described a panel without saying which
screen to be standing on, and both screens now have one. Re-asked in §8 as a sequence of taps.
**Recorded as unrun rather than folded into a pass:** nobody has yet seen the interrogation's
system prompt, so the single claim `-E` was built to settle is still open. This is the M1
checklist's own distinction — "we did not look" is a different result from "we looked and it was
right" — applied to my own instruction rather than to the device.

**The Chinese reply, and why it is filed in §9 instead of being fixed.** The owner sent a message
containing only «.» and DeepSeek answered at length in Chinese. **The app did nothing wrong**, and
the screenshot proves it rather than an argument doing so: the captured body reads
`{"role":"user","content":"."}` with **no `system` field anywhere**. `ChatViewModel` has never set
one. Given a content-free prompt and no instruction, a Chinese vendor's model answering in Chinese
is the correct response to the request we actually sent.

**What makes it worth an entry is the second-order effect.** That Chinese turn is now in the
message history, so it keeps steering later turns until the chat is cleared — the failure is
sticky in a way the first symptom does not suggest.

**And it is a product decision, not a defect, which is why it is not being fixed in passing.**
Pinning an Arabic system prompt to chat costs tokens on every request and changes what the app is
— a neutral chat versus an instructed one. `2026-08-06-B` is the entry about an agent making
exactly that kind of call in the owner's name by quietly ordering a list. The plumbing is no
longer in question: Clarify is the first thing in this repo ever to use `systemPrompt`, and
`2026-08-07-E`'s check 4 exists to prove it arrives.

**Status:** documents only. **No rebuild.** Two observations owed, both in §8 Step 2b.

### 2026-08-07-E · Claude (Cowork) · `DiagnosticsPanel` → `core-ui` · Clarify's prompt becomes visible
**Files:** `core-ui/…/diagnostics/DiagnosticsPanel.kt` (new) ·
`core-ui/res/values{,-en}/strings.xml` · `feature-chat/…/ChatScreen.kt` ·
`feature-chat/res/values{,-en}/strings.xml` · `core-domain/clarify/ClarifyEngine.kt` ·
`feature-clarify/…/ClarifyEngineImpl.kt` · `…/ClarifyViewModel.kt` · `…/ClarifyScreen.kt` ·
`PROJECT_STATE.md` (§5, §7, §8, §9, this entry)

**Step 2 passed all nine checks on device**, including the one the compiler could not settle: no
`[[سؤال]]` marker appeared in any turn. This unit is the small debt §8 named before FORGE, and it
is two things that are really one.

**The move was owed, not tidy-minded.** `2026-08-04-B` established the rule after `AiErrorMessage`
forced `feature-settings` to depend on `feature-chat`: **feature modules are siblings; anything
two of them need goes to `core-ui`.** Clarify is the second screen that shows request
diagnostics, so the same fork appeared — move it, or add
`implementation(project(":feature-chat"))` and have it compile happily until chat needs something
back, at which point Gradle rejects the cycle and the fix is no longer fifteen minutes. **Second
time this rule has been paid for; the tell was identical both times.**

**The strings were renamed `chat_dev_*` → `dev_*`, and the rename is the mechanism.** Because it
is a rename and not a copy, any call site left behind fails as an unresolved symbol at compile
time rather than silently resolving to a stale duplicate — exactly the reasoning of the A3 move.
`chat_dev_details` was **dropped** rather than moved: nothing had referenced it since Developer
Mode shipped in `2026-07-29-D`, which a move would have carried forward unexamined.

**And the reason it was worth doing before FORGE rather than after.** The interrogation's system
prompt appears on no screen. Until this build, the only way to know what Clarify actually sent
was to read `ClarifyPrompt.kt` and *believe* it arrived — and `2026-08-06-P` is an entry about a
tag that was set in one place, read in another, and silently never applied. **The single most
likely thing in M3 to be quietly wrong is now readable on the device.** Layering FORGE on top
first would have meant debugging two unverified prompts through one output.

**Timings live in the ViewModel, not the engine** — the same split `2026-07-29-D` made for chat,
and for the same reason: the provider knows what it sent, only the UI layer knows what the user
experienced. `nanoTime`, so a clock adjustment mid-turn cannot yield a negative duration.

**Two races closed while wiring it.** `developerMode` is now read with `first()` before the
opening request, not merely collected — the collector may not have delivered by then, and the one
request whose captured body matters most is the first, because it carries the system prompt. The
model override was already read this way for the same reason (`2026-08-07-D`); this is the second
instance of a pattern worth naming: **a screen that fires one request on open cannot rely on a
flow that is still warming up.**

**`lastDiagnostics` survives a failed turn**, deliberately, mirroring `ChatUiState`. A request
that failed is precisely when the endpoint and the outgoing body are worth reading — the original
reasoning behind that field, which existed because a failed request discards its bubble and would
otherwise throw the diagnostics away exactly when they became useful.

**Status:** edits complete, **not built**. Eight checks in §8 Step 2b. Check 1 is the refactor's
real test (the chat panel must behave identically) and check 4 is the one this was for: **the
seven Arabic rules, visible inside the request body.**

### 2026-08-07-D · Claude (Cowork) · M3 step 2 — the interrogation runs
**Files:** `feature-clarify/…/ClarifyViewModel.kt` (new) · `…/ClarifyScreen.kt` (new) ·
`feature-clarify/res/values{,-en}/strings.xml` (new) ·
`feature-chat/…/ChatScreen.kt` · `feature-chat/res/values{,-en}/strings.xml` ·
`app/…/navigation/NavGraph.kt` · `PROJECT_STATE.md` (§5, §7, §8, §9, this entry)

**Step 1 built.** The module configures, the unscoped `@Binds` resolves, and the compose
convention plugin is accepted on a module holding no composable. This is the visible half.

**The chat does not depend on `:feature-clarify`, and that is the first thing to protect here.**
`ChatScreen` gained an `onOpenClarify(idea, provider)` callback and nothing else; `:app`'s
NavGraph decides where it goes. Feature modules are siblings — the rule `2026-08-04-B` spent a
morning establishing, and the first screen added after it is exactly where it would have been
quietly broken.

**The provider travels through the route, and that is ruling M3-3 made literal.** Clarify runs on
whatever the chat had selected — but that selection lives only in `ChatViewModel`'s memory, and a
second ViewModel cannot see it. The alternatives were a singleton handoff (the global mutable
state `2026-08-07-C` deliberately avoided when it left the engine unscoped) or persisting the
chat's provider choice (a change to chat's behaviour smuggled in under an M3 slice). A path
argument costs one `Uri.encode` and leaves nothing hidden.

**A consequence worth stating before someone finds it and calls it a bug:** navigation saves its
arguments, so the *idea* survives process death while the *interrogation* does not. That is not a
half-done §M3-4 — it is the ruling working out exactly as written.

**The model override is read before the first request, not collected alongside it.**
`ChatViewModel` collects `selectedModels` and re-resolves when it changes, which is right for a
screen that lives for a long time. An interrogation fires one request the moment it opens, so an
override arriving a moment later would arrive after the turn had already gone out on the default
model — a race that would show up as "Settings did nothing", occasionally.

**A bug caught while writing, in the guard rather than the feature.** `isBusy()` first required
both a busy state *and* streamed text to be present, which made the gap between sending the
request and the first token read as **idle** — so a second tap during those seconds would have
opened a second turn on top of the first. State is the whole truth; the text is only what has
arrived so far. Same family as the two `HeaderReader` and `en-SY` cases: correct for the expected
timing, wrong for the plausible one.

**Three UI decisions that are rulings, not taste.**

1. **The lightbulb is hidden, not disabled, when the field is empty.** `docs/M2_DESIGN_NOTE.md`
   §6's rule for the microphone — a control that is present and always fails is worse than one
   that is absent — and here it is literal: the route carries the idea, so an empty one has
   nothing to open.
2. **«نضجت الفكرة» is gated on nothing.** Not on the engine having finished, not on a minimum
   number of turns. `BRAINING.md` §2.3 gives the decision to the user without conditions, and an
   app that decides when someone has thought enough is deciding the one thing it was told not to.
3. **A caveat is neither amber nor an error colour.** `docs/BRAND.md` §6 reserves amber for
   insight — "the idea is ready" — so a warning wearing it inverts the app's own signal, which is
   precisely the defect `2026-08-04-D` found in the light error card. And a caveat is not a
   failure, so the error container would overstate it. Both use `surfaceVariant`.

**A failed turn leaves the machine in `AWAITING_USER_DECISION`, not `ANALYZING`.** The user can
retry, type, or declare the idea ready. Stranding someone on a spinner after a network blip is a
worse outcome than the blip, and this app has already shipped one screen that could not be left
(`2026-08-06-P`, where a missing terminal event made «تمّ» look like a freeze).

**Deliberately not done.** `AiRequest.diagnostics` stays off for Clarify: capture means holding
the prompt in memory for the life of the session, and there is nothing yet to display it in —
`DiagnosticsPanel` is private to `feature-chat`, and Clarify is now the **second** consumer, so
under §9's standing rule it must move to `core-ui` as its own unit. Both filed in §9, and §8
names the move as the next work unit rather than leaving it to be rediscovered.

**Status:** edits complete, **not built**. Nine checks in §8 Step 2, plus the three regression
checks step 1 never got. Check 3 is the one that cannot be faked: **a real Arabic question about
your real idea, with no `[[سؤال]]` marker visible in the text.**

### 2026-08-07-C · Claude (Cowork) · M3 slice 1 — the state machine, no screen
**Files:** `core-domain/clarify/ClarifyEngine.kt` · `ClarifyState.kt` · `ClarifyTurn.kt` ·
`ClarifySession.kt` (all new) · `feature-clarify/build.gradle.kts` (new) ·
`feature-clarify/src/main/AndroidManifest.xml` (new) ·
`feature-clarify/…/ClarifyEngineImpl.kt` (new) · `…/ClarifyPrompt.kt` (new) ·
`…/di/ClarifyModule.kt` (new) · `settings.gradle.kts` · `app/build.gradle.kts` ·
`PROJECT_STATE.md` (§5, §7, §8, §9, this entry)

The note is signed, so M3 opens. Split 3a-style: engine first, screen second, because a new
Gradle module and a new Hilt binding are the highest-risk part of any milestone here and are
worth failing on their own. **`gradle/libs.versions.toml` is untouched — no new dependency.**

**One module for CLARIFY and FORGE, against `docs/ARCHITECTURE.md` §2's two.** That second
boundary has nothing on the other side: nothing calls Forge but Clarify, and no screen shows it
but Clarify's. `:speech` set the precedent in the other direction — two interchangeable engines
and a router in one module — and a boundary drawn before a second consumer exists is a guess.

**The Hilt binding is unscoped, and that is the entry's most reversible-looking decision.**
`ClarifyEngineImpl` holds the session, so `@Singleton` would make it outlive the ViewModel that
opened it and the next interrogation would start carrying the previous one's turns. Ruling M3-4
says a session does not survive its owner; an unscoped binding enforces that in the graph rather
than by remembering to clear state. `SpeechModule` **is** `@Singleton` and that is not an
inconsistency — one microphone, no per-conversation state. The question to ask of any binding is
whether the object holds something that belongs to one conversation.

**The turn marker, and why not JSON.** The model opens each turn with `[[سؤال]]` / `[[اقتراح]]`
/ `[[تنبيه]]` on its own line; `HeaderReader` reads it, strips it, and passes everything after it
straight through. Structured output would parse more cleanly and would cost the one thing M1
spent two work units securing — `2026-07-29-A` and `2026-08-03-D` are both about genuine
token-by-token streaming, and a JSON envelope cannot be shown until it closes. **A missing or
unrecognised marker resolves to a question**, the commonest turn and the one whose affordance is
right even when the guess is wrong; a hard parse would have made a formatting slip a dead screen.

**A bug I wrote and caught before it shipped, recorded because the shape recurs.** The first
`HeaderReader` dropped the entire first line whenever a marker was found. That is correct only
while the model obeys the instruction to put the marker alone on its own line. Written on one
line — `[[سؤال]] ما هو هدفك من هذا؟` — the buffer cap fires before any newline arrives, and the
"header" being discarded is 32 characters of real question. **It would have silently eaten the
opening of every turn the model formatted slightly wrong, and looked like a model quirk.** The
fix strips the marker, never the line. Same family as `2026-08-06-C`'s `en-SY`: code that is
correct for the expected input and quietly destructive for the plausible one.

**Two traps avoided because this repo has already paid for them.** `CancellationException` is
rethrown before classification, so leaving the screen is not reported as a failure — the fix
already made once in `ChatViewModel` and once in `BaseHttpProvider`. And **an empty turn is a
failure, not a turn**: `2026-08-03-D` is a whole entry about DeepSeek returning HTTP 200, one
chunk and no answer after 38 seconds because thinking tokens ate the budget. Appending a blank
turn would leave an empty bubble on screen and the machine waiting for the user to answer nothing.

**The system prompt is in Kotlin and not in `res/values`, deliberately.** Hard constraint 6
covers *user-facing* strings; this is sent to a model and never rendered. Localising it would be
an active defect — the English UI toggle must not change how the model reasons about an Arabic
idea. It is Arabic because the ideas are, not because the interface is.

**A `@Serializable` I added and then removed, in the same unit.** The first draft annotated all
four domain types, on the argument that one annotation now saves reopening them when M5 brings
Room. That is speculative generality dressed as foresight, and it is worse than harmless here: it
serves a persistence layer that does not exist, **against a ruling made an hour earlier that says
it must not** (M3-4), and it puts unproven polymorphic codegen inside the one build whose entire
purpose is to isolate whether a module and a binding wire up. Removed. **The rule this keeps
teaching: "it is only one line" is an argument about cost, never about whether the line belongs.**

**No new Gradle dependency.** `gradle/libs.versions.toml` untouched; every alias `:feature-clarify`
uses was already in the catalog and was checked against it rather than assumed — hard constraint 2
exists because an invented Ktor artifact once cost a day.

**Status:** edits complete, **not built**. Acceptance in §8 Step 1, and it is deliberately thin:
the app must build, install, and look **exactly as before**.

### 2026-08-07-B · Claude (Cowork) · M3 design note SIGNED — the five §7 decisions ruled
**Files:** `ANSWERS.md` (new **Part 7**) · `docs/M3_DESIGN_NOTE.md` (header, §7) ·
`PROJECT_STATE.md` (§7, §8, this entry). **No code changed.**

The owner answered all five decisions in `docs/M3_DESIGN_NOTE.md` §7, and all five went with the
note's recommendation: **(1)** Clarify is a mode entered by an explicit control, and the M1 plain
chat is untouched; **(2)** the forged prompt's answer returns in English until M4 translates, and
the screen says so; **(3)** Clarify runs on the provider selected in the chat; **(4)** an
interrupted session does not survive the process, and M5 owns persistence; **(5)** the forged
English prompt is a screen every user sees and can edit, not a Developer Mode panel.

**The binding text is `ANSWERS.md` Part 7**, for the reason `2026-08-04-A` established for M2: §2
makes that file the highest authority for decisions, and a ruling recorded only inside a design
note is invisible to an agent that reads `ANSWERS.md` first, exactly as §2 instructs it to. The
note's §7 table now carries the rulings **beside** the recommendations rather than replacing
them, so a later reader can still tell an argued decision from a default.

**Two rulings change documents above the note, and both are written there rather than here.**
M3-1 narrows `BRAINING.md` §2's "for every request" — that section describes the full path a
request *may* take, not one every keystroke must. **M3-3 overrides `BRAINING.md` §5 outright**,
which names Claude the default brain for CLARIFY / FORGE / TRANSLATE. Recorded as an override and
not softened into a preference, because the next agent reading §5 literally would pin the most
expensive stage in the app to the one account with an expiry date — **19 Sep 2026** — and would
hand a friend holding only a free Gemini key an app whose core feature is switched off, which is
the user `ANSWERS.md` Part 3 exists to protect.

**Claude survives as the recommendation.** "Default, not a lock" was already §5's own wording;
what changed is that the default now matches what the screen shows instead of silently overriding
it. A hidden second provider choice is the class of defect Developer Mode exists to expose.

**Status:** documents only, nothing to build. Next work unit is code — §8 Step 1.

### 2026-08-07-A · Claude (Cowork) · M3 design note written, unsigned
**Files:** `docs/M3_DESIGN_NOTE.md` (new) · `PROJECT_STATE.md` (§2, §7, §8, §9, this entry).
**No code changed.**

`2026-08-06-T` handed over a clean tree and §8 said the first M3 work unit is a design note, not
code. This is it. **Five decisions in its §7 are unanswered and no M3 code is written until they
are** — the order M2 followed, and the reason M2's build sequence never had to be undone.

**The hard part of M3 is not the code, it is that the exit criterion is a judgement.** M2's gate
asked one falsifiable question — does the transcript reach the last word — and named that word in
advance. M3's real question is *was the interrogation worth its time*, which cannot be answered by
a counter. So the note's §5 turns it into a **paired comparison**: the same idea dictated and sent
straight through today's app, versus the same idea through Clarify and Forge. The owner judges
which answer is better; the two numbers he judges against — turns to `READY`, and seconds — are
published in §7 the way the segment count was. **Two out of three is a fail, written before any
run**, exactly as `2026-08-06-E` fixed the M2 criterion before it could be negotiated.

**A fourth run is mandatory and it is the one M2 paid for.** Start Clarify from a real transcript
**with its errors left in**. The last dictation lost a third of its words and M3 builds on what
survived. An interrogation that does not notice a truncated sentence will forge a rigorous prompt
for the wrong idea — **the only failure in this milestone that looks like a success**, which is
the same shape as `2026-08-06-Q`'s wrong key reported as a good outcome.

**A dead field found in `docs/ARCHITECTURE.md`, and three more contradictions beside it.** §5's
`Session` object opens with `originalAudioRef`. `ANSWERS.md` Part 6 §M2-10 deleted raw audio
outright, and the streaming build never writes it to storage at all — so that field cannot be
filled, now or after M3. It matters because «لا يحفظ التطبيق الصوت» is the sentence the microphone
rationale rests on and has already been corrected twice for describing an intention rather than
the code. All four are filed in §9 and **deliberately not fixed here**: §0 rule 6 confines an
agent to the files its task named, and this one named the note and this file.

**Five decisions to the owner, seven taken by the agent, and the line between them is the point.**
`2026-08-06-M` established that a question with a recommendation attached is a decision already
made, and that asking it anyway moves the reading burden onto the person least equipped to carry
it. So the module boundary, the state machine's shape, `systemPrompt` versus a `SYSTEM` message,
the error vocabulary and the transport are settled in the note's §3.4, marked reversible. What
went to the owner is what is actually his: whether Clarify is the only path or a mode (it decides
what the app *is*), what language the answer comes back in before M4 (scope), which provider
spends his money on the most expensive stage in the app, what survives a killed process
(pulls M5 scope or does not), and whether the forged prompt is a screen or a diagnostic panel.

**Decision ٣ is in his list for a reason that is not judgement-call sizing.** `BRAINING.md` §5
names Claude the default brain for CLARIFY / FORGE / TRANSLATE. Recommending the chat's selected
provider instead contradicts the master spec in as many words — and §2 makes `ANSWERS.md` the only
document that can overrule it. **An agent does not resolve a conflict with the spec by preferring
its own reasoning**, however good the reasoning is; that is `2026-08-06-B`'s failure, where an
ordering decision was made in the owner's name from a ruling that had answered a different
question.

**One finding that saves work, verified in the tree rather than remembered.** `AiRequest.systemPrompt`
has existed since M1 and **all four providers already read it** — Anthropic correctly putting it in
the top-level `system` field, which is the only place `/v1/messages` accepts it. Nothing has ever
passed it: `ChatViewModel` builds its `AiRequest` without it. So Clarify's system instructions —
the longest system text this project will write — have a path that is already built and already
exercised across four vendors, and M3 needs no change in the provider layer at all.

**Two dates are now load-bearing rather than notes.** Anthropic's promo credit expires
**19 Sep 2026** and M3 is the milestone that spends it — Clarify is a full conversation ahead of
every request, so it is the first stage measured in money rather than seconds. And `2026-08-03-D`
named M3 as the likely place thinking mode gets enabled, together with the trap: reasoning tokens
are billed against `max_tokens`, and when they consumed the lot the reply came back **empty after
38 seconds**. `AiRequest.maxTokens` is 4096 today. Both are in the note's §9.

**Status:** documents only, nothing to build. Blocked on the owner's five rulings. When they
arrive they belong in a new `ANSWERS.md` **Part 7** — §2 makes that file the highest authority for
decisions, and `2026-08-04-A` is the precedent: a ruling recorded only inside a design note is
invisible to an agent that reads `ANSWERS.md` first, as §2 instructs it to.

### 2026-08-06-T · Claude (Cowork) · handoff sweep before the M3 session
**Files:** `PROJECT_STATE.md` (§8, §9, this entry). **No code changed.**

Session-closing pass, so the M3 agent starts from a tree that does not contradict itself. This
is the discipline `2026-08-03-I` was written about: state notes accumulate contradictions when
appended to under pressure, and an agent obeying §0 rule 1 has no way to tell a live claim from
a stale one.

**Two stale live-state headers corrected.** §8 Step 3 still read "3b edited, not built" and §7
still called Phase 0 "EDITED, NOT BUILT" — both verified days ago. The §10 entries keep their
original wording, because a change-log entry is a snapshot of its moment and rewriting it would
destroy the record. **§7 and §8 describe now; §10 describes then.** That distinction is what
makes the append-only rule survivable.

**§9 reconciled.** The font attribution row is struck (done in `-R`). Two rows added that are
real and were about to be forgotten: the provider cards still give no save feedback — though the
Deepgram card now does, so the fix is a copy rather than an invention — and Deepgram has no
`verify()`, which was ruled a real check in the design note and deliberately not built while the
card shipped ahead of the engine.

**§8 now opens with what the M3 agent must read and in what order**, and states plainly that
nothing anywhere is "edited, not built". Whoever picks this up should be able to start on the
design note without a warm-up pass through the repository.

### 2026-08-06-S · Claude (Cowork) · **M2 IS CLOSED**
**Files:** `PROJECT_STATE.md` (§7, §8, this entry). **No code changed.**

Licences card verified in both locales. Final dialect test on Deepgram: **45 words · 17 segments
· 49 s · `ar-SY` · عبر الشبكة**, with punctuation, Levantine rendered as Levantine, and no
meaning-destroying substitutions. Owner's verdict: many errors, the idea came through. **M2 is
closed.** Full table in §7.

**The closing measurement did not measure what it was designed to, and that is recorded rather
than smoothed over.** 45 words over 49 seconds is 55 wpm against 120–150 for natural Arabic —
but spontaneous speech has no ground truth, so the figure cannot separate lost words from a
slow, thinking speaker. `2026-08-06-J` put the duration on screen precisely to make word rate
readable, and it turns out word rate only answers the question when the words are known in
advance. **The instrument was right and the input was uncontrolled.** The fixed MSA passage on
Deepgram would settle it against Google's 66 %; it was not requested because no decision turns
on it, and saying so is cheaper than a test nobody will act on.

**What M2 actually delivered, beyond the checklist.** The gate asked one question — does a long
Arabic paragraph survive — and the honest answer took a whole day to reach, because the first
version of it passed on read MSA while real speech was losing a third of its words. Three
engines were tried, two diagnoses of mine were overturned by the owner's own screenshots, and
the instrument that finally made the difference visible (segment count, accepted tag, duration)
did not exist when the milestone opened.

**The habit worth carrying into M3:** every real fault today was found by a number that had been
put on screen for a different reason. The Developer Mode line earned itself three separate
times, and none of them were the reason it was built.

**Next: M3 — Clarify and Forge.** `ANSWERS.md` and `BRAINING.md` describe it; nothing is
designed yet. It is the product's soul and the reason transcript quality was worth a day.

### 2026-08-06-R · Claude (Cowork) · key-safety audit PASS · the font licence ships
**Files:** `feature-settings/res/raw/ibm_plex_sans_arabic_ofl.txt` (new) ·
`feature-settings/SettingsScreen.kt` · `feature-settings/res/values{,-en}/strings.xml` ·
`PROJECT_STATE.md` (§7, §8, §9, this entry)

`2026-08-06-Q` verified on device: a wrong key now names the key, airplane mode still names the
network. **Two of the four items closing M2 are done in this unit.**

**Key-safety audit (`ANSWERS.md` Part 3 §C) — PASS, re-run 2026-08-06.** Vendor key shapes: none.
Deepgram-shaped 40-hex tokens: none. `key`/`secret`/`token` assigned an 8+ character literal in
shipped code: none. Owner identity in shipped resources: none. Sensitive files tracked: none.
**Re-run before any APK leaves the machine** — the clause ties the check to each release build,
and the tree now holds a second key shape that did not exist this morning.

**The OFL attribution ships.** Ordered in `ANSWERS.md` Part 6 §M2-7 and deferred only until 3b
passed. A Licences card at the foot of Settings carries the copyright line in both locales and
expands to the full text.

**Three decisions in it.** The licence lives in `res/raw/` — AAPT2 accepts only font resources
under `res/font/` and rejects the filename besides, which is exactly the trap `2026-08-04-F`
recorded. It is read on demand rather than held as a string resource, because 4 KB of English
licence has no business in every locale's string table. And it renders through `BidiText` with
direction **forced to LTR**: an English legal text inside an RTL app would otherwise have its
punctuation thrown to the wrong side — the `forced` parameter exists for exactly this, and
`core-ui/text/BidiText.kt` stays the single place that decides direction.

**Status:** edits complete, not built. Two things left to close M2 — §8 Step 9.

### 2026-08-06-Q · Claude (Cowork) · Deepgram verified · a wrong key was blamed on the network
**Files:** `speech/…/DeepgramSpeechToText.kt` · `speech/src/main/AndroidManifest.xml` ·
`core-domain/model/SttError.kt` · `core-ui/error/SttErrorMessage.kt` ·
`core-ui/res/values{,-en}/strings.xml` · `PROJECT_STATE.md` (§7, §8, this entry)

**Deepgram is verified end to end on device.** Bars move with the voice, «تمّ» closes cleanly,
`ar-SY · عبر الشبكة`, **14 segments over 38 seconds**. Deleting the key falls back to the device
engine; airplane mode with a key gives the network error; airplane mode without one gives
Google's language error. All of §8 Step 8b passes.

**One case passed the test and was still wrong.** A deliberately mistyped key produced
«يحتاج محرّك التعرّف إلى اتصال بالشبكة» on a device with full signal. The owner reported it as a
good outcome. It is not: the handshake failure was being classified as `NetworkRequired`
regardless of cause, so the app sent him to check a router that was working. **This is the same
fault as `2026-08-04-I` and `2026-08-06-A`, for the fourth time in one day — the symptom the
code saw, published as the cause.** That it arrived disguised as a passing test is the part worth
remembering: an error message that is confidently wrong reads like a feature.

**Fix: `SttError.InvalidKey`, discriminated by asking the platform whether a network exists.**
With connectivity, the handshake failed for a reason that is not the network — practically always
the key. Without it, nothing could have connected. `ACCESS_NETWORK_STATE` is declared in
`:speech` for this and nothing else; it is a normal permission with no runtime prompt.

**Deliberately not done: inspecting the exception for a 401.** That is the direct route and it
requires assuming which type Ktor raises for a non-101 upgrade. Hard constraint 2 exists because
an assumed Ktor API cost this project a day. Connectivity is a fact the platform will state
plainly, so the fix rests on that instead.

**Status:** edits complete, not built. Two checks: a wrong key must now name the key, and
airplane mode must still name the network.

### 2026-08-06-P · Claude (Cowork) · Deepgram works · three faults, one of them my yardstick
**Files:** `speech/…/DeepgramSpeechToText.kt` · `PROJECT_STATE.md` (§7, §8, this entry)

**Deepgram transcribes, and the difference is not subtle.** The owner's first dictation came back
as fluent Syrian Levantine with the sentence structure intact — «شو أيامك وين هيك ما عم نشوفك»,
«بالنسبة لكلاود أنا بحبه وهو بيساعدني كتير بس اشتراكه غالي». Against Google, where «الفكرة»
became «الزوجة», this is a different class of output. **The engine decision is vindicated.**

Three faults, and the third is the one worth the most.

**1 · No `Completed` was ever emitted.** The reader's loop ends when Deepgram closes the socket
in answer to `CloseStream` — and nothing said so. One missing line produced three symptoms the
owner reported as separate problems: «تمّ» appeared to freeze the app, the sheet stayed on
«أستمع…», and the duration read «٠ ثانية» because that field is computed only when `Completed`
or `Failed` arrives. **The acceptance list asked "is the last sentence lost?" and never asked
"does the run end?"** — a state machine with no terminal state passes every test that only looks
at content.

**2 · The tag was `ar`, not `ar-SY`.** `ChatViewModel` passes the app's UI language, which is
bare `ar`; the regional expansion lived inside `AndroidSpeechToText`'s attempt ladder. The design
note said "one tag, no ladder" and I read my own sentence as "no resolution either", so Deepgram
was asked for generic Arabic and the owner's dialect ruling silently did not apply. `resolveTag`
now does for Deepgram what the ladder's first rung does for Google — device region when it names
an Arabic country, `SY` otherwise. **Caught by the owner reading the Developer Mode line**, which
is the third time that line has earned itself.

**3 · The waveform was dead because my meter was linear.** Ordinary speech sits around 0.03–0.09
of full scale, so linear RMS pins the bars to the bottom tenth of their travel while the audio is
perfectly good — and it was: Deepgram transcribed the same signal flawlessly. Hearing is
logarithmic; a level meter that is not will always look broken. Now dBFS against a −50 dB floor,
which puts normal speech at 0.3–0.8. `docs/BRAND.md` §6 asks for this mark to be got right, and
an under-reporting meter is the same failure as one ignoring the microphone, only harder to spot.

### The yardstick I gave the owner was wrong, and that matters more than the bugs

§8 Step 8b told him to expect **1–2 segments instead of 13**. He got **19**, and **that is not a
regression — the number no longer measures the same thing.**

Google's segments are *restarts*: the session ends, a new one begins, and audio in the gap is
lost. Deepgram's `is_final` messages are *finalisation boundaries inside one continuous stream* —
no gap, nothing dropped. Nineteen of them over a minute means Deepgram finalised nineteen times,
not that it stopped nineteen times.

**A metric is only comparable across engines that fail the same way.** Segment count was a good
proxy for word loss on `SpeechRecognizer` and is meaningless on Deepgram; carrying it over
unexamined would have read a healthy run as a failure and could have sent the next agent tuning
away a number that was never a symptom. Word rate against duration is the measure that survives
the change of engine — which is why `2026-08-06-J` put the duration on screen.

**Status:** edits complete, not built. Retest in §8 Step 8b.

### 2026-08-06-O · Claude (Cowork) · the Deepgram engine — one socket, no seams
**Files:** `gradle/libs.versions.toml` · `speech/build.gradle.kts` ·
`speech/…/DeepgramSpeechToText.kt` (new) · `speech/…/RoutingSpeechToText.kt` (new) ·
`speech/…/di/SpeechModule.kt` (rewritten) · `core-domain/model/SttError.kt` ·
`core-ui/error/SttErrorMessage.kt` · `core-ui/res/values{,-en}/strings.xml` ·
`PROJECT_STATE.md` (§0 rule 9 refinement, §5, §8, this entry)

`AudioRecord` at 16 kHz mono PCM straight into one WebSocket, open from the first word to «تمّ».
`interim_results=true` maps to `Partial`, `is_final` maps to `Segment` — **the same two events
the UI already renders, so no screen changed.** That is `2026-08-04-G`'s interface paying for
itself: a whole engine swapped and `ChatViewModel` never learned about it.

**Five decisions worth the next agent's attention.**

1. **A router, not a Hilt qualifier.** `RoutingSpeechToText` picks the engine at the moment of
   the tap. A binding is fixed when the graph is built, so a key entered in Settings would not
   take effect until the process restarted — the same shape as the stale label in `2026-08-06-D`,
   where the app was right and the thing reporting it was late.
2. **The key is read inside `flow { }`.** `transcribe` is not `suspend`, so the obvious reading
   would have been `runBlocking` on the tapping thread — the main one. Quick work on the main
   thread is how ANRs are written. The choice still lands before any audio is captured.
3. **`SttError.MissingKey` added, and the fallback is deliberately absent.** If Deepgram is
   selected and fails, that is reported. Quietly re-running on the device engine would give a
   user the belief they are getting accuracy they are not, and a stitched result would poison
   every measurement after it — the objection that settled D2.
4. **A second `HttpClient`, `@Named("speech")`.** Installing WebSockets on `:core-data`'s shared
   client would put the plugin on every module's path; `core-data` is imported by everything,
   which is the same reasoning that kept the engines out of it. An instance costs memory once;
   a dependency in the wrong module costs a refactor.
5. **`Context.checkSelfPermission`, not `ContextCompat`.** API 23, minSdk 26 — the AndroidX
   helper would have pulled `androidx.core` into a module that has no AndroidX dependency at all.

**Two traps avoided in the writing, both previously paid for here.** `contentOrNull` throughout
the parser, because `JsonNull.content` returns the string `"null"` (`2026-08-03-C`, one day). And
`stop()` sends `CloseStream` rather than tearing the socket down, because Deepgram is still
holding the final transcript — the identical mistake `2026-08-04-H` fixed for the Google engine
in a different mechanism.

**`awaitClose` cancels the session rather than closing it.** It runs while the flow's scope is
being torn down, so `launch { session.close() }` would have created an already-cancelled job and
leaked the socket. That path is the user dismissing the sheet; the graceful goodbye belongs on
«تمّ».

**Status:** edits complete, not built. Seven checks in §8 Step 8b — and check 4 is the one this
was all for: **1–2 segments instead of 13.**

### 2026-08-06-N · Claude (Cowork) · key card verified · plain-language rule added to §0
**Files:** `PROJECT_STATE.md` (§0 rule 9, §7, §8, this entry). **No code changed.**

**Step 8a passes.** Built, installed, key entered, survives a full restart. Deepgram's key is in
the encrypted store and nothing reads it yet — exactly the split this unit was for.

**New protocol rule 9: talk to the owner in plain Arabic.** He asked for it explicitly on
2026-08-06, after asking twice in a row what was actually required of him because the answer was
buried in explanation. It is a rule and not a note because it changed the *work*, not just the
tone: `2026-08-06-M` records four implementation decisions that were put to him as questions
when each already carried a recommendation, which cost a round trip and moved the reading burden
onto the person least equipped to carry it.

**The rule draws a line rather than banning detail.** This file is written for the next agent and
stays technical; the chat does not. Density here, plain language there. An agent that flattens
this file to match the chat will have obeyed the letter and destroyed the thing that has kept
five sessions from repeating each other's mistakes.

### 2026-08-06-M · Claude (Cowork) · the Deepgram key card — and four decisions I took back
**Files:** `feature-settings/SettingsViewModel.kt` · `feature-settings/SettingsScreen.kt` ·
`feature-settings/res/values{,-en}/strings.xml` · `PROJECT_STATE.md` (§8, §9, this entry)

**The owner said the explanations had become hard to read, and he was right.** He was then asked
to rule on four items — where the engine choice lives, what happens when the network drops,
whether `verify()` spends a fraction of a cent, and what to do when a key is missing. **Those are
implementation judgements, not product decisions, and every one of them came with a
recommendation.** Putting them to the owner was offloading work, dressed as consultation. They
are settled here, in the note's §8, marked as agent-taken and reversible on request:

1. A Settings choice between engines, defaulting to Deepgram when a key exists.
2. Network drop mid-dictation → clean typed error, no stitching two engines into one transcript.
3. `verify()` opens a real connection.
4. Key missing while Deepgram is selected → typed error, never a silent downgrade.

The pattern worth naming: **a question with a recommendation attached is usually a decision the
agent has already made.** Asking it anyway is not deference — it moves the reading burden onto
the person least equipped to carry it and slows the work.

**What this build actually is.** The owner asked how to put his key into the app, and the honest
answer was that there was nowhere to put it — the field did not exist. So this unit builds only
that: a Deepgram card in Settings, above the provider list, storing the key through the existing
`EncryptedKeyStore` under the id `"DEEPGRAM"`. **Split deliberately from the engine**, the same
way M2 split 3a from 3b, so that "the key saves and survives a restart" can pass or fail on its
own before any audio or socket code exists to confuse it.

**Two decisions inside it.** The id is a `const` in one place, because the store is keyed by
free-form strings and a typo would compile on both sides while storing under one name and reading
another — the same failure `ProviderId.defaultModel` was created to prevent. And there is **no
verify button**, because verifying needs the connection that does not exist yet, and a tick that
means nothing is worse than no tick (`2026-08-03-A`).

**Status:** edits complete, not built. Acceptance in §8 Step 8a.

### 2026-08-06-L · Claude (Cowork) · Deepgram design note written, unsigned
**Files:** `docs/DEEPGRAM_DESIGN_NOTE.md` (new) · `PROJECT_STATE.md` (§2, §8, this entry).
**No code changed.**

Owner has a Deepgram key and instructed that `AndroidSpeechToText` stay until the basic tests
are done. Note written; **four decisions in its §8 are unanswered and no code is written until
they are** — the same order M2 followed, which is why M2's build sequence never had to be undone.

**Three things the note settles that were not obvious.**

1. **`EncryptedKeyStore` needs no change.** It takes a plain `String` id, so `"DEEPGRAM"` slots
   in. Deepgram must **not** become a `ProviderId` — it is not an AI provider and has no business
   in the chat selector.
2. **Deepgram accepts its key as a `?token=` query parameter, and we must refuse that.** It
   exists for browser clients that cannot set headers. Hard constraint 3 names a key in a URL as
   a violation on its own, and `redactSecrets` exists because such a key leaks into every
   diagnostic that touches it. `Authorization: Token` header only.
3. **The raw-audio ruling gets stronger for free.** The owner ruled audio deleted the moment the
   transcript returns. Streaming means it is never written to storage at all — microphone buffer
   to socket. «لا يحفظ التطبيق الصوت» stops being a policy and becomes a property of the code.

**`io.ktor:ktor-client-websockets` was verified on Maven Central before being written into the
note.** One artifact, `:speech` only. The check took a minute; hard constraint 2 exists because
an invented Ktor artifact once cost a day.

**Two smaller consequences worth keeping.** The attempt ladder does not carry over — it exists
because Android's engine lies about language support, and Deepgram publishes its list. And the
waveform gets *better* input: amplitude becomes a real RMS over PCM we own, instead of Google's
loosely-documented `onRmsChanged`.

**Status:** documents only. Blocked on the owner's four rulings in the note's §8.

### 2026-08-06-K · Claude (Cowork) · engine ruled: cloud STT, BYOK · Deepgram recommended
**Files:** `ANSWERS.md` (Part 6 §M2-10) · `PROJECT_STATE.md` (§8, this entry).
**No code changed.**

Owner ruled the engine question (§8 Step 7): **cloud transcription with his own key**, and
**raw audio deleted the moment the transcript returns — no toggle**. Both recorded in
`ANSWERS.md` Part 6 §M2-10. He asked for a recommendation rather than naming a service.

**Two things the research overturned, and both were mine.**

1. **Whisper is a poor choice for this app.** It was the obvious default and it is measured at
   **36.86 % WER on multi-dialect Arabic** — the worst of the serious options. Recommending it
   from familiarity would have cost a build and a subscription to land back here.
2. **"You will lose the live text" was wrong.** I told the owner that plainly one message
   earlier. Streaming STT exists: Deepgram serves Nova-3 over a WebSocket at 200–300 ms, and
   ElevenLabs Scribe v2 Realtime at ~150 ms. The live partial text survives, and so does the
   waveform driving off real audio.

**Recommendation: Deepgram Nova-3, `ar-SY`, streaming.** Decisive reasons, in order:

- **`ar-SY` is a first-class language code there** — Nova-3 documents 16 Arabic variants
  including `ar-SY`, `ar-LB`, `ar-PS`, `ar-JO`. The tag the owner ruled on 2026-08-06 is a tag
  the service was built to serve, not one it tolerates.
- **Streaming and batch both available for Arabic** — checked in Deepgram's own docs rather
  than inferred from a pricing page, because one search summary suggested realtime multilingual
  covered only ten non-Arabic languages and that would have quietly broken the live-text promise.
- **$0.0077/min streaming ≈ $0.46 per hour of dictation.** An hour a month is 46 cents.
- No utterance ceiling, therefore no seams, therefore the deletion problem disappears at the
  root rather than being tuned around.

**The caveat that matters more than any of those numbers.** ElevenLabs advertises 3.1 % WER for
Arabic on FLEURS and 5.5 % on Common Voice. **Those are read-speech, largely MSA benchmarks.**
This project spent 2026-08-06 discovering that read MSA and spontaneous Levantine fail in
completely different ways and that a passing score on the first says nothing about the second.
A vendor benchmark is the same trap with a nicer chart. The dialect-test numbers — 26–37 % WER
on actual multi-dialect sets — are the honest range, and **the only measurement that decides
this is the owner's own voice through `docs/M2_GATE.md`'s dialect test**, which already exists
and produces comparable numbers.

**So: build one provider behind the existing `SpeechToText` interface, then run the dialect
test.** Not an abstraction over three services first — that interface was designed in
`2026-08-04-G` precisely so the engine could be swapped without touching a screen, and the way
to use it is to swap one thing and measure, not to generalise before there is a second case.

**Status:** documents only. Blocked on the owner creating a Deepgram key. Design note next.

### 2026-08-06-J · Claude (Cowork) · segmented session ignored · the app now times itself
**Files:** `feature-chat/ChatViewModel.kt` · `feature-chat/ChatScreen.kt` ·
`feature-chat/res/values{,-en}/strings.xml` · `PROJECT_STATE.md` (§7, §8, this entry)

**`EXTRA_SEGMENTED_SESSION` has no effect on this engine.** Second dictation test:
**13 segments · 63 words · `ar-SY` · عبر الشبكة** — against 14 segments and 84 words before it.
Google's own documentation warns the extra "may have no effect depending on the recognizer
implementation", and on this device it does not. `onSegmentResults` never fires; the restart
ladder runs exactly as before.

**Kept rather than reverted.** It costs one `putExtra` behind an API-33 guard, changes nothing
where it is ignored, and a friend on a different launcher or a future Google app update may
honour it. Recorded here so nobody re-discovers it as a new idea.

**Two runs now agree on the shape of the problem: ~5–6 words per segment.**
Run A 84 words / 14 segments = 6.0. Run B 63 / 13 = 4.8. The engine ends an utterance every few
seconds regardless of content, and each ending is a place where words can vanish. **This is
structural to `SpeechRecognizer` here, not a tuning problem** — the silence hints are set, the
platform's own continuous mode is refused, and the restart is already as tight as
`Handler.post` allows.

**Both runs remain intelligible, and that is the other half of the finding.** Run B's idea — an
app where pharmacists list near-expiry stock for other pharmacists, plus a space to trade
knowledge and product — survives intact despite «القمر هاي» and a dropped «الأدوية». On the
gate's own question, *would someone who was not in the room understand*, both dictations pass.

**The app now measures its own recording duration.** Two consecutive tests were reported without
it, and a word count cannot be read without one: 84 words is unremarkable over 45 seconds and
catastrophic over 90. The Developer Mode line now reads «مقاطع التفريغ: N · المدة: N ثانية».
**Asking a human for a number the device already holds is a measurement design error**, and the
right response to being given it twice was to stop asking, not to ask more firmly.

**Status:** edits complete, not built. The duration line is a read-out, not a behaviour change —
it cannot confound the engine decision now in front of the owner (§8 Step 7).

### 2026-08-06-I · Claude (Cowork) · the dialect test written down as an instrument
**Files:** `docs/M2_GATE.md` (new second half) · `PROJECT_STATE.md` (§8, this entry).
**No code changed.**

The dialect test was run once from a chat instruction and immediately became the more important
of the two — so it is now in the file, repeatable after any engine change.

**It is talking points, deliberately not a script.** A script would be read, reading creates
clean pauses, and clean pauses are precisely what makes the MSA passage easy and the real
product hard. Writing the test as prose would have quietly recreated the condition that made
the first gate misleading.

**And it demands the duration, with a stopwatch.** That was the number missing on 2026-08-06:
84 words is either normal or catastrophic depending on whether it covered 45 seconds or 90, and
without it the transcript could be read but not measured. A test that produces an
uninterpretable number is not cheaper than one that produces none.

**Status:** documents only. Run it after §8 Step 6's build.

### 2026-08-06-H · Claude (Cowork) · the dialect test — and the real defect it exposed
**Files:** `speech/…/AndroidSpeechToText.kt` · `PROJECT_STATE.md` (§7, §8, this entry)

Owner spoke, rather than read, for roughly 60–90 seconds of natural Syrian Levantine.
**14 segments · `ar-SY` · عبر الشبكة · 84 words captured.**

**The dialect hypothesis was right in kind.** No meaning-destroying substitutions of the
«الفكرة» → «الزوجة» sort. Reading it cold, the intent survives intact: a PhD thesis on
blockchain and deep learning, blocked by technical, professional, social and economic
constraints, no university affiliation, wanting advice. On the gate's own test — *would someone
who was not in the room understand?* — this passes and the MSA passage did not.

**And a bigger defect appeared underneath it: the words are not wrong, they are missing.**
84 words for 60–90 seconds implies 56–84 wpm. Natural Arabic speech runs 120–150. The owner
independently reported "a large number of words the engine could not capture", and the gaps are
visible mid-sentence — «تقنيه ومنها قيود مهنيه» has lost the «منها قيود» that must precede
«تقنية».

**Why the gate did not catch this, and it is not a flaw in the gate.** Reading and speaking fail
differently. A reader stops at full stops, so the engine's restarts land in silence and cost
almost nothing — the MSA run reached the end with ~155 of 161 words. Spontaneous speech has no
such gaps, so each of the 14 restarts cuts mid-phrase and the words spanning the seam are gone.
**The gate measured substitution; real use is dominated by deletion.** Two different failure
modes, and the passing one was the one the product does not depend on.

**`ANSWERS.md` Part 5 §M2-3 called this in advance.** Auto-restart was allowed *on condition the
segment count be published*, "because the gap between stopping and starting swallows words". The
count is now in, and the ruling's suspicion is confirmed. This is the clearest case yet for
publishing a number you do not yet know how to interpret.

**Fix attempted: `RecognizerIntent.EXTRA_SEGMENTED_SESSION`** — the platform's own answer. One
session stays open, results arrive through `onSegmentResults`, and it ends at
`onEndOfSegmentedSession`. No stop, no restart, no gap. **API 33 verified against the reference
before writing** (hard constraint 2); the device is API 34. Its value must name another extra set
in the same intent — `EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS`, already present.

**Google documents it as possibly having no effect depending on the engine, and the change is
built to degrade to exactly today's behaviour if so.** `onResults` and the restart ladder are
untouched; the new callbacks simply never fire. **Nothing else changed in this build** — one
variable, because `2026-08-06-G` was written an hour ago about crediting the wrong change out of
two.

**The read-out is already on screen.** If it works, one dictation reports roughly **1 segment**
instead of 14. That number alone settles it.

**Status:** edits complete, not built. Acceptance in §8 Step 6.

### 2026-08-06-G · Claude (Cowork) · CORRECTION — my diagnosis in `2026-08-06-B` was wrong
**Files:** `PROJECT_STATE.md` (§7, §10 — this entry, and a correction note appended to
`2026-08-06-B`). **No code changed.**

Owner supplied two screenshots: one with the network, one in airplane mode. They settle the
open question and overturn a diagnosis I recorded as settled two entries ago.

**Confirmed:** `محرّك التعرّف: ar-SY · عبر الشبكة`. The gate measured Google's network model.
The 66 % is real and belongs to the shipping configuration.

**Overturned:** `2026-08-06-B` states that the offline rungs led the ladder, so "the weak
on-device model always won and Google's network model was never reached." **That is false on
this device.** Airplane mode walks the *entire* ladder — every tag, offline and online — and
ends with `ERROR_LANGUAGE_NOT_SUPPORTED` (code 12) on the last rung. **There is no offline
Arabic pack installed here.** The offline rungs have therefore never been able to win. The
ladder was already falling through to the network before the reorder, exactly as
`2026-08-04-I` was designed to make it.

**So the reorder did not change which model ran. What changed was the tag: bare `ar` → `ar-SY`
(`2026-08-06-C`).** Any improvement the owner saw came from the dialect tag, not from putting
the network first. The reorder is still correct — it stops ~6 doomed rungs from being tried at
the start of every session, and it is the right order for a device that *does* have the pack —
but it was not the cure, and this file said it was.

**How the wrong diagnosis passed.** The evidence was consistent with it: the ladder did lead
with offline, the transcript was poor, and reversing the order coincided with improvement. What
was never checked was the premise underneath — *is the offline pack even installed?* One
airplane-mode test answers it in ten seconds, and `2026-08-04-I`'s own KDoc had already named
the missing pack as the prime suspect for the original language error. The fact was in this
repository, in my own words, and I built a causal story on top of it without reading back.

**Standing lesson, and it is a different one from the previous three.** Those were about
publishing a symptom as a cause. This is worse: a plausible cause, confirmed by a coincidence
in timing, recorded as fact. **A fix that is followed by an improvement has not thereby been
shown to be the cause of it.** Two things changed in one build — the order and the tag — and
the entry credited the wrong one. Change one thing per build, or accept that the changelog is
telling a story rather than reporting a result.

**Not corrected by deletion.** `2026-08-06-B` keeps its text, with a correction note appended
pointing here — protocol rule 4, and because the reasoning that produced the error is more
useful to the next agent than a clean file would be.

**Status:** documents only. Next work unit is a dialect test, in §8.

### 2026-08-06-F · Claude (Cowork) · the gate ran — it passes, and it found a different problem
**Files:** `PROJECT_STATE.md` (§7, §8, this entry). **No code changed.**

Owner ran `docs/M2_GATE.md`, reading the passage **three times in one continuous recording** —
~480 words, ~3.5 minutes, harder than the ruling asked. Full table in §7.

**Truncation: passes, decisively.** All three readings reach the final word `بعد`. The premise
behind the gate — that a short-utterance engine would cut off a long Arabic paragraph — is
disproved on this device. **Vosk is not triggered**, and it is important to say why precisely:
`ANSWERS.md` Part 1 §1 pre-approved Vosk *for truncation*. Reaching for it now would be
answering a question the gate did not ask.

**Accuracy: 66 %, 66 %, 65 %** against the source, measured after normalising diacritics, hamza
and ة — i.e. **generously**. Fifteen-ish spans of two or more consecutive words lost or replaced
per run.

**The finding that changes the diagnosis: the three runs fail identically.** «تماسكاً مما
ظننّا» → «ظنا», «يعمل بها» → «تعمل على», «فكرة تنجح» → «الفكرة» — same substitutions, same
places, all three times. A restart seam is a timing artifact and would land differently on each
reading. **Consistent errors are model errors.** So the 37 segments are not what is costing the
34 %, and tuning `EXTRA_SPEECH_INPUT_*` — the obvious next move — would have been wasted work.
This is the first time in this project that a measurement has ruled a suspect *out* before
anyone spent a build on it.

**Recorded as unresolved rather than concluded: the run's mode was not captured.** The owner
reported `ar-SY` but not whether the line said عبر الشبكة or على الجهاز. `docs/M2_GATE.md`
lists exactly this as "the run measured the wrong engine". The whole accuracy reading hangs on
it, and one glance settles it — so it is being asked rather than assumed. Writing "Google's
online Arabic model scores 66 %" without that check would be the same error this project has
now made three times: publishing a confident cause from evidence that only named a symptom.

**Status:** documents only. Blocked on one observation from the owner.

### 2026-08-06-E · Claude (Cowork) · M2 step 3 closed · the gate instrument written
**Files:** `docs/M2_GATE.md` (new) · `PROJECT_STATE.md` (§2, §7, §8, this entry).
**No code changed.**

`2026-08-06-D` verified on device: the Developer Mode line follows the language toggle before
any recording. **M2 Step 3 is closed in full** — engine, UI, Arabic accuracy, and the
instrumentation the gate depends on. Only Step 4 remains.

**`docs/M2_GATE.md` is the gate as an instrument rather than an intention.** It carries a fixed
161-word passage (67–77 s at an ordinary pace, inside the ruling's 60–90 s window), the exact
pre-run setup, a per-run recording table, and a reading for each outcome.

**Three decisions inside it worth defending.**

1. **The passage is adversarial on purpose.** Eleven sentence boundaries and three question
   marks in one sentence, because those silences are precisely what makes `SpeechRecognizer`
   decide an utterance has ended. A passage read in one breath would pass a test the product
   will fail in real use.
2. **The last word is named — `بعد`.** `ANSWERS.md` Part 5 forbids describing truncation as a
   platform limit and demands the exact stopping word. A gate that does not fix its own
   endpoint in advance invites "it got most of it" as a result, which is not a result.
3. **Two out of three is a fail, stated up front.** Written before any run, so it cannot be
   negotiated afterwards. The product's core interaction cannot work two times in three.

**A limitation recorded rather than smoothed over:** the passage is MSA and the owner speaks
Syrian Levantine. That is the right trade for *this* gate — repeatability matters and
truncation is dialect-independent — but it means the gate says nothing about everyday dialect
accuracy. That deserves its own informal test and should not be quietly folded into a pass.

**Status:** documents only, nothing to build. Next work unit is the owner running the gate.

### 2026-08-06-D · Claude (Cowork) · the accuracy fix works · and the diagnostic that lied about it
**Files:** `feature-chat/ChatScreen.kt` · `feature-chat/res/values{,-en}/strings.xml` ·
`PROJECT_STATE.md` (§7, §8, this entry)

**`2026-08-06-B` and `-C` are built, installed and verified on device.** The owner reports the
transcription now works. **M2 step 3b is complete and the accuracy problem is closed** — the
ladder reaches the network model and `ar-SY` leads it.

**The owner then found the one thing left wrong, and it was in the instrument, not the
engine.** Switching the UI language did not change the recognition language "immediately" —
the Developer Mode line went on naming the old tag until the next recording.

**The recognition language was never actually stale.** `ChatScreen` reads
`LocalConfiguration.current.locales[0].language` on every recomposition and passes it to
`startVoice` at the moment of the tap, and the toggle recreates the activity anyway. Every
recording used the right language. **What lagged was the label** — `engineTag` lives in the
ViewModel, which survives that recreation, so it kept displaying `en-US` under an Arabic UI.

**This is worse than a cosmetic bug and is the reason it was fixed rather than explained
away.** That line was added one work unit earlier for a single purpose: so the engine's
behaviour could be *checked* instead of assumed. A diagnostic that is confidently wrong is
more harmful than no diagnostic, because it is believed — and it would have been believed
during the gate, where three runs are judged on exactly these two fields. The owner caught it
by noticing the number did not move when it should have, which is the habit that found the
`dynamicColor` bug too.

**Fix: the accepted-tag line is shown only while it still describes the current language**
(matched on the language subtag). Otherwise the line names the language that *will* be
requested — so the toggle visibly takes effect the instant it is flipped, which is what the
owner expected and was right to expect. It deliberately shows the language and not a dialect:
which region the ladder settles on is unknowable until the engine accepts a rung, and this
line has just finished being punished for claiming more than it knew. Matching rather than
clearing also means the reading survives a rotation, which changes nothing about the request.

**Status: VERIFIED ON DEVICE 2026-08-06.** The line follows the toggle before any recording.
**M2 Step 3 is closed in full** — see §7. Only the gate remains.

### 2026-08-06-C · Claude (Cowork) · `ar-SY` leads · and the device's region leads it
**Files:** `speech/…/AndroidSpeechToText.kt` · `ANSWERS.md` (Part 6 §M2-9) ·
`PROJECT_STATE.md` (§8, this entry). Refines `2026-08-06-B`, which had not been built.

Owner's correction: he speaks **Syrian** Levantine and his device region is `SY`, not `SA`.
`DEFAULT_REGIONS["ar"]` now reads `SY, LB, PS, JO, SA`.

**He asked for `ar-SY` "first and last" and it is not the only entry — stated rather than
quietly done.** A one-tag list is a dead microphone the instant the engine rejects it, which
is `2026-08-04-I` arriving by a different road. The rest of the group sits underneath,
unreachable while `ar-SY` is accepted. The Developer Mode line makes that claim checkable
instead of asking him to take it on trust, which is the only reason overriding the literal
instruction is defensible at all.

**A bug I introduced and caught in the same unit.** The first version trusted the device's
region whenever it named an Arabic country — with no check on the *language*. The owner's
phone reports `SY`, so flipping the UI to English would have built **`en-SY`** and burned a
rung on a tag no engine has, on every English run. `TRUSTED_DEVICE_REGIONS` is keyed by
language now. Recorded rather than silently corrected: it is the same shape as the `en`/`ar`
mix-ups this project has already paid for, and it survived one reading of my own code.

**The device's region leads the preference list, and that is the substantive design point.**
Ranking dialects by the owner's speech would put owner-specific data in a shipped APK, which
`ANSWERS.md` Part 3 exists to prevent — a friend in Cairo should get `ar-EG`, not Syrian.
For the owner the device resolves to `SY` and the two agree, so the ruling costs him nothing.
**A personal default belongs behind the user's own setting, never in front of it.**

**Status:** edits complete, not built. Folded into `2026-08-06-B`'s acceptance — one build
covers both.

### 2026-08-06-B · Claude (Cowork) · the Arabic was bad because we never used the good model
**Files:** `core-domain/speech/SpeechToText.kt` · `speech/…/AndroidSpeechToText.kt` ·
`feature-chat/ChatViewModel.kt` · `feature-chat/ChatScreen.kt` ·
`feature-chat/res/values{,-en}/strings.xml` · `ANSWERS.md` (Part 6 §M2-8) ·
`PROJECT_STATE.md` (§7, §8, this entry)

**M2 step 3b passed all six checks on device.** The waveform moves with the voice, the
transcript lands editable in the input field, denial does not crash. Anthropic also works now —
the owner's key was a billing state, as §9 predicted, and `2026-08-06-A`'s reading of the 400
holds. Both recorded in §7.

**And the transcript was very bad.** Not truncated, not empty — wrong words.

**Cause: the attempt ladder led with the offline rungs, and only advances on a *language*
error.** So on a device with the on-device Arabic pack installed, rung one won every time and
Google's network model — markedly stronger for Arabic — was never reached. Not once, in any
run, since 3b existed.

**This was my judgement, not the owner's, and it was made in his name.** `2026-08-04-I` wrote
"offline leads because it keeps the audio on the device and that is the better outcome when it
is available" and cited his ruling. His ruling had answered *offline-only-and-broken versus
working-and-candid*. He was never asked whether he would trade Arabic accuracy for local
audio. That is a product decision, it was his, and ordering a list quietly took it. Put to him
2026-08-06: **accuracy leads** (`ANSWERS.md` Part 6 §M2-8).

**Three changes, and the second is the one that would have bitten.**

1. **Order reversed** — every tag online, then every tag offline.
2. **`onError` now falls through to the offline rungs on a network failure**, jumping straight
   to the first of them. Without this, reversing the order would have turned "dictation works
   with no signal" into "dictation fails with no signal" — a regression *caused by an accuracy
   fix*, and invisible to anyone testing on Wi-Fi. The ladder's advance condition was written
   for language errors alone because, offline-first, a network error was never on the path.
   Changing the order changed which failures are reachable; the error handling had to move with
   it.
3. **Levantine tags lead for bare `ar`** (owner's dialect), device country first when it names
   an Arabic region, `ar-SA` last. Guessing tags is safe *here specifically* because an
   unsupported tag returns a language error and the ladder moves on — the ladder is what makes
   the guess cheap.

**The real defect underneath all of it: nothing recorded which rung won.** A poor transcript
had at least three candidate causes — wrong model, wrong dialect, bad audio — and the app
published none of the evidence. New `TranscriptionEvent.EngineConfig` carries the accepted tag
and the mode; `ChatScreen` prints both under Developer Mode beside the segment count.
**That is the third time in three days.** `2026-08-04-I`: the platform's error code was
published as a cause. `2026-08-06-A`: Anthropic's own explanation of the 400 was discarded.
Here: the winning configuration was never captured at all. Each time the fix was cheap and the
blindness was expensive. The standing lesson is now explicit — **when a subsystem chooses
between strategies at runtime, the chosen strategy is diagnostic output, not an implementation
detail.**

**Honesty obligation discharged in the same unit.** The permission rationale said on-device was
preferred. It no longer is, so both locales were rewritten to say the engine uses its servers
first for accuracy and falls back when offline. This is the second correction to that string
for over-promising; it describes what the code does, not what the author hoped.

**Status:** edits complete, **not built**. Acceptance in §8 Step 3b. **The gate must not run
until this is verified** — three passages measured against the wrong engine is three passages
wasted.

> **⚠ CORRECTED 2026-08-06 by `2026-08-06-G`. Read that entry before trusting this one.**
> The central claim above — that the offline rungs always won and the network model was never
> reached — is **false on this device**: there is no offline Arabic pack installed, so the
> offline rungs could never win and the ladder was already falling through to the network. The
> reorder did not change which model ran; the tag change in `-C` (`ar` → `ar-SY`) did. The
> reorder remains correct for other reasons. Left in place rather than rewritten, per rule 4.

### 2026-08-06-A · Claude (Cowork) · three rulings recorded · Anthropic diagnosed · build status corrected
**Files:** `ANSWERS.md` (new **Part 6**) · `PROJECT_STATE.md` (§7, §8, §9, this entry).
**No code changed.**

**Three §9 items the owner had been holding are now ruled and are in `ANSWERS.md` Part 6.**
The derived error tones are **ratified**; `darkTheme` **keeps** `isSystemInDarkTheme()`, so
`2026-08-04-C`'s open offer of "always dark" is declined and should not be reopened; the OFL
attribution is **ordered** and no longer waits for M5 — it ships at the foot of Settings as its
own work unit immediately after step 3b passes, deliberately not folded into the 3b rebuild.

**`2026-08-04-I` is built, and its own status line said otherwise.** The owner did not know;
neither did the file. Settled by looking at the tree rather than asking:
`AndroidSpeechToText$Attempt.class` is an inner class that exists **only** in the attempt-ladder
fix, and it is compiled at 17:38 with `app-debug.apk` packaged at 17:39 on 2026-08-06. The
voice UI of `2026-08-04-H` is compiled beside it. Recorded in §7 and §8. Corollary worth
keeping: **a "status: not built" line is a claim about the past that nobody updates**, and the
build directory answers it in seconds — check it before asking the owner to remember.

**Anthropic: the owner pasted a key and it failed, and the app made that impossible to
diagnose.** `claude-sonnet-5` was the last model name never checked against a primary source;
it is now **verified Active**, and matches Anthropic's dateless-ID convention where major
releases omit the minor segment. The provider code was desk-checked and is not the prime
suspect — headers, body shape, `max_tokens`, the `message_stop` terminator and the absence of
a `SYSTEM` role in `messages` are all correct for `/v1/messages`.

**What is wrong is that we cannot see the answer.** Anthropic reports an empty credit balance
as **HTTP 400** with `invalid_request_error` and the sentence "Your credit balance is too low
to access the Anthropic API" — a billing state wearing a bad-request status. `classifyHttpError`
has no branch for it, so it becomes `AiError.Unknown`, and `AiErrorMessage.kt` renders Unknown
without its `detail` **by design**. The provider told us exactly what was wrong and the app
discarded the sentence. Filed in §9 with a two-part fix that is deliberately separable: an
`InsufficientCredit` branch matched on the body marker the way `RegionBlocked` already is, and
`detail` surfaced in the Developer Mode strip only, where `redactSecrets` already covers it.

This is `2026-08-04-I`'s lesson one layer up. There it was `ERROR_LANGUAGE_UNAVAILABLE`
translated into "this language is not installed", sending the owner to three settings screens
that were already correct. Here it is `400` translated into "unexpected error", sending him to
suspect a key that may well be fine. **Both times the platform named a symptom and the app
published it as a cause.** The difference is that this one is worse: the platform had also
supplied the cause, in plain English, and we were the ones who threw it away.

**Status:** documents only, nothing to build. The next work unit is unchanged — §8 Step 3b,
checks 3–6.

### 2026-08-04-I · Claude (Cowork) · "Arabic is not installed" — on a device that had it
**Files:** `speech/…/AndroidSpeechToText.kt` · `core-domain/model/SttError.kt` ·
`core-ui/error/SttErrorMessage.kt` · `core-ui/res/values{,-en}/strings.xml` ·
`feature-chat/res/values{,-en}/strings.xml` · `PROJECT_STATE.md` (§7, §8, §9, this entry)

Checks 1 and 2 of §8 Step 3b passed. Check 3 failed with the language-unavailable message —
and the owner then verified that Arabic *is* installed on the device, as a system language, as
a keyboard, and in the engine's own speech settings.

**The app reported the error code faithfully and misread what it meant.** Two independent
things produce `ERROR_LANGUAGE_NOT_SUPPORTED` (12) and `ERROR_LANGUAGE_UNAVAILABLE` (13):

1. **The tag.** Some engines accept `"ar"`; others insist on a region, `"ar-SA"`. A rejected
   tag is indistinguishable from a missing language.
2. **`EXTRA_PREFER_OFFLINE`, which `2026-08-04-G` set unconditionally to `true`.** With offline
   recognition requested, the engine answers with a *language* error when the **offline
   recognition pack** is not downloaded. That pack is a third thing, separate from the system
   language and from the keyboard — which is exactly why the owner checked, found Arabic
   present, and the app still refused. This is the prime suspect here.

**Fix — an ordered attempt ladder** (`buildAttempts`): every tag variant offline first, then
every tag variant online. Offline leads because keeping the audio on the device is the better
outcome when it is available; falling back beats failing. A language error is only believed
after every rung fails, and is then reported **with its raw code** so the two causes stay
distinguishable — `SttError.LanguageUnavailable` gained a `code` field for that.

**The owner's ruling, and the promise I had to withdraw.** Falling back to online changes what
the app can honestly claim, so this was put to the owner rather than decided: offline-only and
correct, or working and candid. He chose **try offline, fall back to the network**. The
permission rationale I wrote in `2026-08-04-H` said the audio "is never sent to a server" —
a promise **I invented**. `ANSWERS.md` Part 1 §1 chose `SpeechRecognizer` for being free and
weightless, not for being offline; nothing in the architecture ever guaranteed it. Both locales
now say on-device is preferred, the engine may use its servers if it is not available, and
Braining stores no audio — which is true in every branch.

**Standing lesson.** A platform error code names the *symptom the platform saw*, not the cause.
`ERROR_LANGUAGE_UNAVAILABLE` was translated straight into "this language is not installed", and
that sentence sent the owner to check three settings screens that were all already correct.
Before an error message tells a user what is wrong with their device, it should have exhausted
what might be wrong with our request.

**Status:** edits complete, not built. Rerun checks 3–6 of §8 Step 3b.

### 2026-08-04-H · Claude (Cowork) · M2 step 3b — the voice UI, and the mark comes alive
**Files:** `core-ui/error/SttErrorMessage.kt` (new) · `core-ui/res/values{,-en}/strings.xml` ·
`feature-chat/voice/BrainingWaveform.kt` (new) ·
`feature-chat/voice/VoiceCaptureSheet.kt` (new) · `feature-chat/ChatViewModel.kt` ·
`feature-chat/ChatScreen.kt` · `feature-chat/res/values{,-en}/strings.xml` ·
`PROJECT_STATE.md` (§7, §8, this entry)

3a built and installed with no visible change, as designed. This is the visible half.

**The transcript goes into the existing input field, and that is the design.** Not a separate
"voice text" state — `TranscriptionEvent.Segment` appends straight to `ChatUiState.inputText`,
the same field the keyboard writes to. `docs/M2_DESIGN_NOTE.md` §1 calls the editable text "not
a detail": Arabic transcription *will* be wrong, and M3 Clarify builds on whatever survives, so
an error passed through here is magnified there. Using one field means editing, sending,
clearing and rotation all already work rather than each needing a voice-shaped copy.

**The waveform is the logo, not a meter that resembles it.** `BrainingWaveform` reads its
proportions off `assets/logo/icon.svg` in the SVG's own units — 82 / 143 / 215 tall, 36 wide,
67 apart — so the drawing can be checked against the file instead of against someone's memory.
At amplitude 0 it *is* the mark; speech drives the bars toward the centre bar's height, so loud
input converges on the silhouette rather than flattening it, and the amber nuqta rides above
the centre bar as it grows. BRAND §6 calls this "the signature interaction of the app; get it
right", and BRAND §5 caps state-change motion at 150 ms — the smoothing is 120 ms, because
`onRmsChanged` fires far faster than the eye can follow and unsmoothed bars strobe.

**Permission: the rationale comes first, deliberately.** Android's own prompt explains nothing,
and a user who refuses it without context may never be asked again. So a dialog states why the
microphone is wanted and that the audio never leaves the device — which is true of
`EXTRA_PREFER_OFFLINE` on an engine that honours it, and is the promise the whole feature rests
on. Denial produces a typed `SttError.PermissionDenied` and an Arabic card, never a crash and
never a dead button: `docs/M2_DESIGN_NOTE.md` §6, refusal is not a malfunction.

**Speech errors get their own card.** Sharing the provider card would let a microphone failure
overwrite the reason a message never sent — two subsystems, two remedies, two cards.
`SttErrorMessage.kt` sits beside `AiErrorMessage.kt` in `core-ui/error/`; a second home for
error wording is exactly what `2026-08-04-B` spent the morning eliminating.

**The segment count is on screen, behind Developer Mode.** `VoiceUiState.segments` counts
stabilised segments, and more than one means the engine restarted mid-paragraph. The owner's
ruling (`ANSWERS.md` Part 5 §M2-3) requires that number for each of the three gate runs, so it
is displayed rather than left to be inferred — a measurement the gate depends on should not
have to be reconstructed from a log.

**Two behaviours worth knowing apart.** «تمّ» calls `stopVoice()`, which lets the engine deliver
its final sentence. Swiping the sheet away calls `cancelVoice()`, which cancels the collector
and tears the recogniser down through `awaitClose` — a user who dismisses did not ask for their
words to be kept. Recognition language follows the app's own Arabic/English toggle, so there is
no second setting to find.

**Status:** edits complete, not built. Six acceptance checks in §8 Step 3b. Check 3 is the one
that cannot be faked: the bars must move with the voice.

### 2026-08-04-G · Claude (Cowork) · M2 step 3a — the speech engine, no UI
**Files:** `core-domain/speech/SpeechToText.kt` (new) · `core-domain/model/SttError.kt` (new) ·
`speech/build.gradle.kts` (new) · `speech/src/main/AndroidManifest.xml` (new) ·
`speech/…/AndroidSpeechToText.kt` (new) · `speech/…/di/SpeechModule.kt` (new) ·
`settings.gradle.kts` · `app/build.gradle.kts` · `PROJECT_STATE.md` (§5, §7, §8, this entry)

Phase 0 closed, so M2 opens. Split into 3a (engine) and 3b (UI) because a new Gradle module, a
new Hilt binding and a `settings.gradle.kts` edit are the highest-risk part of this milestone
and are worth failing on their own.

**Shape of the interface, as signed.** One `Flow<TranscriptionEvent>` carrying `Partial`,
`Segment`, `Amplitude`, `Failed` and `Completed`. Amplitude rides the same stream as text
rather than getting an interface of its own — the design note's reasoning, and the project's
own precedent: Developer Mode passed `AiChunk.Meta` down the token stream instead of adding a
method to `AiProvider`.

**`SttError` follows `AiError` exactly** — classified in the domain, phrased in the UI from
resources. `docs/M2_DESIGN_NOTE.md` §6 warns that rebuilding English strings in the data layer
would undo A3 within a day of finishing it.

**One case added beyond the note's list: `LanguageUnavailable(languageTag)`.** The note listed
`PermissionDenied`, `NoEngine`, `NoSpeechDetected`, `NetworkRequired`, `EngineFailure`.
`SpeechRecognizer` also reports `ERROR_LANGUAGE_NOT_SUPPORTED` / `ERROR_LANGUAGE_UNAVAILABLE`,
and for an **Arabic-first** app on a device that may not ship Arabic recognition, "Arabic is
not installed — here is how to add it" and "the engine broke" are different sentences with
different remedies. Folding them together would produce the generic message A3 exists to
abolish.

**Three implementation decisions that are not obvious from the interface.**

1. **Restart happens in `onResults`, never `onEndOfSpeech`.** `onEndOfSpeech` fires when the
   engine hears silence but *before* it delivers the text preceding it; restarting there
   discards the sentence the user just finished. Restarts are also `Handler.post`ed rather than
   called inline, because several engines answer `startListening` from inside a callback with
   `ERROR_RECOGNIZER_BUSY`.
2. **Silent restarts are capped at three.** `ERROR_NO_MATCH` and `ERROR_SPEECH_TIMEOUT`
   mid-paragraph mean a pause, not a failure, so they restart — but an unbounded loop with a
   dead microphone would spin forever. After the cap: `Completed` if any segment arrived,
   `NoSpeechDetected` if none did. A run that captured speech and then ran out of patience is
   not an error.
3. **`<queries>` for `android.speech.RecognitionService` is in the manifest.** Without it,
   Android 11+ package visibility can make `isRecognitionAvailable()` answer `false` on a
   device that *does* have an engine — which would surface as `NoEngine` and send the next
   agent hunting for a missing engine that is installed.

**Not done, deliberately.** No UI, no permission request, no waveform — 3b. The
`EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS` family is set but is documented as
advisory and widely ignored by manufacturer engines; it is insurance against restarts, not a
substitute for the gate.

**Status:** edits complete, not built. Acceptance in §8 Step 3a — and it is deliberately thin:
the app must build, install, and look **exactly as before**.

### 2026-08-04-F · Claude (Cowork) · font wired — and my own instruction was wrong
**Files:** `core-ui/src/main/res/font/ibm_plex_sans_arabic_regular.ttf` (moved, new location) ·
`core-ui/src/main/res/font/ibm_plex_sans_arabic_medium.ttf` (moved) ·
`licenses/IBM-Plex-Sans-Arabic-OFL.txt` (new) · `app/src/main/res/font/` (**deleted**) ·
`core-ui/theme/Type.kt` · `PROJECT_STATE.md` (§7, §8, this entry)

The owner supplied both weights, correctly named, with the OFL licence — exactly as asked.
**The instruction was wrong.** `2026-08-04-C` told him to put them in `app/src/main/res/font/`,
in this file's §8 and in `Type.kt`'s own KDoc.

**Why that would have failed.** `Type.kt` compiles into `core-ui`, so `R.font.…` written there
resolves against `com.braining.core.ui.R`. Resources in the app module belong to
`com.braining.app.R`, which a library module cannot see. This is **the same trap as
`2026-07-29-C`**, which moved the screen strings out of `app` for identical reasons, and the
same shape as the §9 dependency cleared this morning: put a resource where its consumer cannot
reach it and the tempting fix is always a dependency that should not exist. I had written that
lesson into three places today and then broke it in a fourth. Recorded rather than quietly
corrected, because §0.5 says the code wins over this file — and that has to apply to an
instruction I wrote an hour ago as much as to one written last week.

**A second failure the same instruction would have caused.** `OFL.txt` was placed in
`res/font/` alongside the fonts, as told. Files under `res/` may contain only lowercase
letters, digits and underscores, and `res/font/` accepts font resources only — `OFL.txt` fails
AAPT2 on both counts, before any Kotlin is compiled. The licence now lives at
`licenses/IBM-Plex-Sans-Arabic-OFL.txt`. **It still owes an in-app attribution**: OFL requires
the notice to travel with the binary, and `ANSWERS.md` Part 3 makes sharing the APK a
first-class goal, so M5's About screen must carry it. Filed in §9.

**What changed in code.** `BrainingFontFamily` is now a real `FontFamily` over the two weights
instead of `FontFamily.Default`. Nothing else — the fifteen styles were written longhand in
`2026-08-04-C` precisely so this would be a one-value change, and it was.

**Status:** edits complete, not built. One check in §8 Step 2, and it is a check that can
silently "pass" while failing: Compose falls back to the system font rather than erroring, so
"the app still looks fine" is not evidence. The Arabic must visibly change shape.

### 2026-08-04-E · Claude (Cowork) · Phase 0 items 1–2 verified on device
**Files:** none changed. `PROJECT_STATE.md` §7, §8 and this entry only.

Owner ran `2026-08-04-D` on `2312DRA50G`. **The error card passes in both themes** — the
derived tones read as intended, and the light theme no longer dresses an error in the insight
accent. That closes acceptance check 4, and with it Phase 0 items 1 and 2.

Final tally for §8 Step 2: checks 1, 2, 3, 4 and 5 **pass**; check 6 (themed icons) is
**unobservable on this hardware**, not failed — HyperOS ships no "Themed icons" toggle, so the
`<monochrome>` layer cannot be seen here. It is recorded in §9 rather than marked green,
because "we could not look" is a different claim from "we looked and it was right" — the same
distinction the M1 checklist draws on the free-tier Gemini row.

**Check 4 needed two rounds, and that is the entry's real content.** The first attempt looked
correct by every measurement I had taken and was still wrong on the device: `#E24B4A` cleared
its contrast target as *text*, but it was being used as a full-card *fill*, where saturation —
not contrast — is what the eye reports. A ratio computed against the wrong role predicts the
wrong outcome. Worth remembering when M2 colours the waveform: five saturated bars animating
against indigo900 is the same failure mode waiting to happen, and BRAND §2's "amber is scarce"
rule is partly about exactly this.

**Phase 0 is NOT closed.** Item 3, the bundled Arabic font, is still outstanding and is the
owner's to supply. See §8 Step 2.

### 2026-08-04-D · Claude (Cowork) · the error card glared — derived error tones
**Files:** `core-ui/theme/Color.kt` · `core-ui/theme/Theme.kt` · `PROJECT_STATE.md` (§7, §8, §9,
this entry)

Owner's report on acceptance check 4 of `2026-08-04-C`: the error card was "not weak, but far
too bright — it needs dimming a little."

**Diagnosis: the wrong role was carrying the raw hex.** `ChatScreen` uses `errorContainer` as
the **fill of a full-width card**, and `2026-08-04-C` set `errorContainer = Error` (`#E24B4A`)
on the dark scheme. A 72 %-saturated red filling a large area against indigo900 glares —
the eye responds to chroma, not to a contrast ratio, which is why the §9 note filed hours
earlier had described the *opposite* problem (3.67:1, too little luminance contrast for body
text). Both readings were correct about the same pairing. Both are fixed by the same move.

**The move is Material's own model, not a redesign:** on dark, a *dark* container with *light*
text; on light, a *pale* container with *deep* text. Four tones were derived in `Color.kt` —
`ErrorLight` `#EC9393`, `ErrorDark` `#4A1C1C`, `ErrorPale` `#FBE4E4`, `ErrorDeep` `#631D1D`.
They are **tonal steps on BRAND's own error hue (H≈0°)**, the same way Material 3 derives a
tonal palette from a seed: lightness and chroma move, hue does not, so the identity is intact.
Measured on the surfaces they actually appear on: `ErrorLight` on indigo900 **6.30:1**,
`ErrorLight` on `ErrorDark` **6.26:1**, `ErrorDeep` on `ErrorPale` **10.1:1** — all now clear
4.5:1 for body text, which `#E24B4A` never did in either theme.

This sits against BRAND §1 ("do not invent colours") and §2 (error is one hex), so it is filed
in §9 for the owner to ratify or overrule rather than treated as settled. Reverting means
accepting both the glare and sub-4.5:1 error text.

**A second defect found while in there.** The light scheme had
`errorContainer = Amber300 / onErrorContainer = Amber800` — an amber error card. That directly
violates BRAND §6, "caveats and warnings carry the warning colour, **never amber**", and amber
is the *insight* accent, the colour that means the idea is ready. An error dressed as an
insight is the worst possible mixed signal. It was mine, from `2026-08-04-C`, and it survived
the owner's light-mode check only because check 3 asked about readability, not meaning.

**Status:** edits complete, not built. One retest in §8 Step 2 — the error card in both themes.

### 2026-08-04-C · Claude (Cowork) · Phase 0 — BRAND applied, and the reason it never was
**Files:** `core-ui/theme/Color.kt` (rewritten) · `core-ui/theme/Theme.kt` (rewritten) ·
`core-ui/theme/Type.kt` (new) · `app/res/values/colors.xml` (new) ·
`app/res/drawable/ic_launcher_foreground.xml` (rewritten) ·
`app/res/drawable/ic_launcher_monochrome.xml` (new) ·
`app/res/drawable/ic_launcher_background.xml` · `app/res/mipmap-anydpi-v26/ic_launcher.xml` ·
`PROJECT_STATE.md` (§7, §8, §9, this entry)

§8 Step 2 — Phase 0 of `docs/M2_DESIGN_NOTE.md` §2, owner's ruling M2-1 (full scope). This is
`docs/BRAND.md` §7, which instructed the building agent to apply the identity "now" during M1
and was never executed.

**The finding: `BrainingTheme` took `dynamicColor: Boolean = true`.** On Android 12+ that path
calls `dynamicDarkColorScheme(context)` and replaces the entire scheme with colours sampled
from the user's wallpaper. The test device is Android 14. So **no palette defined anywhere in
this repository has ever been visible on the device** — and that is the answer to a question
nobody had asked: how could BRAND §7 sit unexecuted for a whole milestone without anything
looking obviously wrong? Because the app was not rendering a wrong palette. It was rendering
Material You, which always looks deliberate. `Color.kt`'s `Purple80`/`Pink80` template values
were never on screen either. **The parameter is deleted, not defaulted to `false`** — the same
reasoning the owner applied to the raw-audio toggle (`ANSWERS.md` Part 5 §M2-2): a switch that
must never be flipped is worse than no switch, and re-adding this one silently deletes the
brand.

**Palette.** `Color.kt` is now `object BrandPalette` — the nine indigo/amber tokens and the
three semantic colours of BRAND §2, verbatim, and the only place a hex is written in Kotlin.
`Theme.kt` builds both schemes from it. The mapping is not a literal transcription, because
BRAND lists *tokens* and Material 3 wants *roles*: in the dark scheme `primary` takes the light
end of the ramp (indigo200) so the accent stays legible **on** a dark surface, while BRAND's
"primary interactive" indigo400 becomes `outline` — which is what BRAND §5's hairline borders
actually need. Amber maps to `tertiary` deliberately: Material spends tertiary sparingly, which
is precisely BRAND's "amber is scarce by rule". Amber500 itself is reserved, unused by either
scheme, because BRAND §6 assigns it to the M2 waveform's centre bar and dot.

**Typography.** `Type.kt` restates all fifteen Material styles longhand rather than inheriting,
for one reason: the defaults use Bold in display and headline, and BRAND §3 forbids 600/700
outright — heavy weights read poorly in Arabic. Body styles carry 1.7 leading. Every style
routes through a single `FontFamily` value so item 3 below is genuinely two lines.

**Item 3 is not done and is not an agent's to do.** BRAND §3 requires IBM Plex Sans Arabic
bundled, and warns that the device font "will break the layout on Xiaomi/Samsung devices" —
the test device is a Xiaomi. There is no `.ttf` or `.otf` anywhere in this repository; fetching
one is the owner's job (§0.2, and the SIL OFL licence must travel with the file). Recorded in
§7 and §8 as an open Phase 0 item with the exact two-line change, **not** quietly closed by
accepting `FontFamily.Default`.

**Icon.** The previous `ic_launcher_foreground.xml` was a hand-drawn approximation on a 108
viewport with **square-cornered** bars and an off-centre dot; the mark's bars are stadiums
(`rx=14` on a 29-wide rect). It is replaced by a literal conversion of
`assets/logo/icon-foreground.svg` at viewport 512 — the SVG's own coordinates, each rounded
rect expressed as the equivalent path since VectorDrawable has no `<rect>`. BRAND §1 says do
not redraw the mark, and an approximation is a redraw. The **monochrome layer required by
BRAND §7.1 did not exist** and now does; without it, Android 13+ themed icons flatten the whole
foreground into a blob. It uses the foreground's geometry (so the icon does not jump when
themed icons are switched on) with `icon-mono.svg`'s alpha ramp (0.55/0.75/1.0), which is what
keeps five bars distinguishable once the hue difference is gone. The background hex moved to
`values/colors.xml`, so indigo900 is written once on the launcher side.

**Two judgement calls, flagged rather than buried.** (1) `statusBarColor` now follows
`colorScheme.background` instead of `colorScheme.primary`; against the real palette the old
line painted a light indigo band above an indigo900 app. (2) `darkTheme` still follows
`isSystemInDarkTheme()`. BRAND §2 calls dark "the default" and light "fully supported", and
honouring the OS setting is how a user who chose light gets light — but if the owner meant
"always dark", that is a one-line change and his call.

**Status:** edits complete, **not built.** Six acceptance checks in §8 Step 2; checks 2 and 6
are the ones that could not have passed yesterday.

### 2026-08-04-B · Claude (Cowork) · §9 sibling dependency cleared — `AiErrorMessage` → `core-ui`
**Files:**
- `core-ui/src/main/kotlin/com/braining/core/ui/error/AiErrorMessage.kt` (new) ·
  `core-ui/src/main/res/values/strings.xml` (new) ·
  `core-ui/src/main/res/values-en/strings.xml` (new) · `core-ui/build.gradle.kts`
- `feature-chat/.../AiErrorMessage.kt` (**deleted**) · `feature-chat/.../ChatScreen.kt` ·
  `feature-chat/res/values/strings.xml` · `feature-chat/res/values-en/strings.xml`
- `feature-settings/.../SettingsScreen.kt` · `feature-settings/build.gradle.kts`
- `PROJECT_STATE.md` (§7, §8, §9, this entry)

§8 Step 1, mandated as a prerequisite by the owner's ruling M2-4 (`ANSWERS.md` Part 5). It had
to happen before M2 because every screen M2 adds to `feature-chat` raises the price of undoing
the dependency, and because `feature-settings → feature-chat` would have become a hard Gradle
cycle the first time `feature-chat` needed anything back.

**The move.** `AiErrorMessage.kt` is now `com.braining.core.ui.error` in `core-ui`, which both
features already depended on. The nine A3 strings moved with it and were **renamed**
`chat_error_*` → `error_*` in both `values/` (Arabic, default) and `values-en/`. The rename is
the point, not cosmetics: the wording is no longer chat's, and library resources merge into one
app-wide namespace, so the name had to stop claiming an owner it no longer has. Because it is a
rename rather than a copy, any missed call site fails as an unresolved symbol at compile time
instead of silently resolving to a stale duplicate. `ChatScreen` gained the import it had not
needed while the function was in its own package; `SettingsScreen`'s import changed from
`com.braining.feature.chat.toUserMessage`. Then
`implementation(project(":feature-chat"))` was deleted from `feature-settings`.

**One change not in the plan, and the reason for it.** `core-ui/build.gradle.kts` now declares
`api(project(":core-domain"))` where it declared `implementation`. `core-ui`'s public surface
now *exposes* a `core-domain` type — `AiError` is the receiver of `toUserMessage()` — and
`implementation` does not put it on a consumer's compile classpath. It would have compiled
today, because `feature-chat` and `feature-settings` both declare `core-domain` themselves, and
broken for the next module that calls `toUserMessage()` without doing so. This is the first
`api` configuration in the repo, so it is the **first thing to suspect if the build fails**
(§8 Step 1 says so too).

**Deliberately not done.** No behaviour changed: the `when` over `AiError`, every sentence, and
the RegionBlocked steering ruling are byte-identical to `2026-08-03-E`. No other module's build
file was touched. §9's row is struck through rather than deleted, because the rule it
established — **feature modules are siblings; shared code goes to `core-ui`, never to a
peer** — still binds, and a future agent should be able to see that this was decided, not
overlooked.

**Status, updated 2026-08-04 after the owner's run: COMPILES.** `.\gradlew.bat installDebug`
reached `:app:installDebug` — 174 actionable tasks, 47 executed — which means every module
compiled, resources merged, and the APK was packaged. That clears all three suspects named
above: the `api` configuration is accepted, no stale `chat_error_*` reference survived the
rename, and no duplicate `error_*` resource collided. **Acceptance check 1 passes.**

`installDebug` itself failed with `DeviceException: No online devices found` — the only
candidate was `adb-525ae8c7-E0TtBS._adb-tls-connect._tcp.`, a **stale wireless entry**, marked
OFFLINE. That is §9's known "Wi-Fi pairing does not work / ADB Version Too Low" item, not a
result of this change; it is an empty device list, not a rejected APK. Checks 2–4 (the Arabic
and English error wording in Chat and in Settings) still need a USB-connected device.

The only compiler warnings were two pre-existing `MenuAnchorType` deprecations in
`ChatScreen.kt` — untouched by this entry, now recorded in §9.

### 2026-08-04-A · Claude (Cowork) · M2 design note SIGNED — the four §7 decisions ruled
**Files:** `ANSWERS.md` (§1 annotated, §10 annotated, Part 4 tail, new **Part 5**) ·
`docs/M2_DESIGN_NOTE.md` (header, §2, §4, §5, §7) ·
`PROJECT_STATE.md` (§2, §5, §7, §8, this entry)

**No code changed.** This work unit exists to unblock M2, which `2026-08-03-H` left waiting
on an unsigned note.

The owner answered all four decisions in `docs/M2_DESIGN_NOTE.md` §7, and all four went with
the note's recommendation: **(1)** Phase 0 runs first at full scope — palette *and* icon *and*
font, not the palette alone; **(2)** the "retain raw audio" toggle is deferred, not cancelled;
**(3)** auto-restarting `SpeechRecognizer` on silence is an allowed mitigation but is recorded
as a shortfall, with the segment count of each of the three gate runs published; **(4)** M2's
UI lives in `feature-chat`, and clearing the §9 `feature-settings → feature-chat` dependency
is a **prerequisite**, not a suggestion.

**Where each ruling now lives, and why in more than one place.** The binding text is
`ANSWERS.md` **Part 5** — that file is the highest authority for decisions, and a ruling
recorded only inside a design note would be invisible to an agent that reads `ANSWERS.md`
first, as §2 instructs. The note's §7 table now carries the rulings alongside the
recommendations so the two can be told apart, and points at Part 5 as authoritative. Rulings 2
and 3 are additionally annotated **at the section they modify** (`ANSWERS.md` §10 and §1
respectively) rather than only at the end of the file, because the failure mode this project
has already suffered — `2026-08-03-I` — is an agent reading a superseded line and obeying it.
For the same reason `PROJECT_STATE.md` §5's `AppPreferences` bullet now says explicitly that
the raw-audio toggle is *not* an M2 item; it previously read as an instruction to build one.

**A concrete consequence of ruling 1, found by looking rather than assuming.** `assets/logo/`
does contain all three SVGs the note claims, so the icon work is unblocked. **The font is
not:** there is no `.ttf` or `.otf` anywhere in the tree. Phase 0 items 1 and 2 can start
immediately; item 3 is blocked on the owner supplying the two IBM Plex Sans Arabic weights —
the agent edits and does not download (§0.2), and the SIL OFL licence has to travel with the
file. The instruction recorded in §7 and §8 is to keep `Typography` centralised in `core-ui`
so the font's arrival is a two-line change, and **not** to quietly fall back to the device
font, which BRAND §3 says breaks the layout on exactly the Xiaomi device M2 must be proven on.

**Ordering is now stated as an obligation, not a menu.** §8 previously offered "then, in
whatever order suits". Rulings 1 and 4 make the sequence load-bearing: §9 dependency fix →
Phase 0 → M2 → gate. The §9 fix is first because it is the cheapest it will ever be, and every
line M2 adds to `feature-chat` raises its price. Anthropic and the Gemini thinking latency are
moved to a "parked" list so they cannot be mistaken for steps in the M2 sequence — Anthropic's
promo credit still expires 19 Sep 2026 and that date is preserved.

**Status:** documents only; nothing to build. The next work unit is §8 Step 1.

### 2026-08-03-E · OpenCode (OpenAgent) · A3 legible failures + Arabic/English toggle (HANDOFF-OPENCODE-A3.md)
**Files:**
- `core-domain/model/AiError.kt` (new) · `core-domain/model/AiChunk.kt` · `core-domain/model/ProviderState.kt` · `core-domain/provider/AiProvider.kt`
- `ai-providers/ErrorClassifier.kt` (new) · `ai-providers/BaseHttpProvider.kt` · `ai-providers/anthropic/AnthropicProvider.kt` · `ai-providers/openai/OpenAiProvider.kt` · `ai-providers/deepseek/DeepSeekProvider.kt` · `ai-providers/gemini/GeminiProvider.kt` · `ai-providers/github/GitHubModelsStub.kt`
- `feature-chat/AiErrorMessage.kt` (new) · `feature-chat/ChatViewModel.kt` · `feature-chat/ChatScreen.kt` · `feature-chat/res/values*/strings.xml`
- `feature-settings/SettingsViewModel.kt` · `feature-settings/SettingsScreen.kt` · `feature-settings/res/values*/strings.xml` · `feature-settings/build.gradle.kts`
- `core-data/di/CoreDataModule.kt`
- `gradle/libs.versions.toml` · `app/build.gradle.kts` · `app/src/main/kotlin/com/braining/app/MainActivity.kt` · `app/src/main/res/values/themes.xml`
- `PROJECT_STATE.md` (§4.6, §7, §8, this entry)

**Task 1 — A3: typed errors resolved to Arabic in the UI.** The old design let `HTTP nnn —
message` literals and `"No API key configured"` strings escape the provider and data layers.
A new `sealed interface AiError` (`provider: ProviderId` on every branch) carries the
classification: `MissingKey`, `InvalidKey`, `Forbidden`, `RateLimited`, `ProviderDown`,
`NoNetwork`, `Timeout`, `RegionBlocked` (status on all but MissingKey/NoNetwork/Timeout), and
`Unknown(status: Int?, detail: String?)` where `detail` is the provider's raw text, kept for
Developer Mode only. `AiChunk.Error` now holds an `AiError` (its `code: Int?` is gone — not
duplicated); `ProviderState.error` and `ChatUiState.error` are `AiError?`. `verify()` returns
`AiError?` instead of `Result<Unit>`.

Classification, in `BaseHttpProvider` (HTTP path, `classifyHttpError`: 401 → InvalidKey,
403 → Forbidden, 429 → RateLimited, 5xx → ProviderDown, then case-insensitive
`"location is not supported"` body match → RegionBlocked, else Unknown with
`redactSecrets(...).take(500)`) and in the new `ErrorClassifier.kt` (`Throwable.toAiError`:
`HttpRequestTimeoutException`/`SocketTimeoutException` → Timeout; `ConnectException` (Ktor's
`ConnectTimeoutException` is a typealias of it)/`UnknownHostException`/`SocketException` →
NoNetwork; else Unknown). `BaseHttpProvider.complete()` **no longer swallows transport errors
in a `catch`** — they propagate to the collector's `.catch`, which is where socket-level
failures arrive and get classified (the owner's correction #2). Every provider's `verify()`
and `catch` use `toAiError`.

Wording lives in one place, `feature-chat/AiErrorMessage.kt` —
`@Composable fun AiError.toUserMessage()` — consumed by both Chat's error card and Settings'
verify status. Strings are in `values/` (Arabic) and `values-en/`, prefixed `chat_`
(the `settings_` screens reuse the same composable via a new `feature-chat` dependency in
`feature-settings`, which is how a library module reaches another library's `R` class).
RegionBlocked is a steering sentence without a status, per the owner's ruling; Unknown
renders `status` as ` (nnn)` and never `detail`. `HttpTimeout` is installed in
`CoreDataModule` (600 s request / 15 s connect / 60 s socket) — from `ktor-client-core`,
no new dependency (hard constraint 2).

**Task 2 — in-app Arabic/English toggle.** Owner-approved `androidx.appcompat` **1.7.1**
(latest stable; 1.8.0 still alpha — nothing else in the catalog moved, so the "stop if a
version must change" guard did not fire). `MainActivity` is now an `AppCompatActivity`;
`Theme.Braining` descends from `Theme.AppCompat.Light.NoActionBar` — the previous
`android:Theme.Material.Light.NoActionBar` is a platform theme that makes `AppCompatActivity`
throw at inflate, so this parent change is a mandatory companion to the host switch (not in
the handoff's file list; recorded here). Settings gained a Language card (two radio rows)
driving `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(...))` —
the only per-app locale path on minSdk 26. The active locale is read from
`LocalConfiguration` in the UI, so the toggle always reflects the effective language. The
choice persists in AppCompat's own storage across restarts; the activity recreates on switch,
flipping the whole app (and layout direction) immediately. Dependency declared in
`libs.versions.toml` + `app/build.gradle.kts`, and — a necessary extension to the handoff's
file list — in `feature-settings/build.gradle.kts`, because `SettingsViewModel` references
`AppCompatDelegate` and a library module cannot compile against a dependency its own build
file does not declare.

**Corrections to this file (protocol rule 5, code wins):** §4.6's "constraint is currently
violated and the fix is queued" tail removed — A3 removed the last hardcoded UI literals;
§7's `verifyResult()`/`describeHttpError()` bullets renamed to the new API; §7 "Known not
done" pruned of the three items this entry closes (A3 failures, toggle, `HttpTimeout`).

**Deliberately not done (handoff's do-not-touch list held):** no `ktor-client-sse` added,
`complete()` still `preparePost(...).execute` (not reverted to `post()`), model-name sources
and Developer Mode position untouched, §9 untouched, no `gradlew` run, no protected build
files touched.

**Status:** edits complete, not built. Acceptance steps in §8.

### 2026-08-03-A · Claude (Cowork) · dead model name, model plumbing, Developer Mode placement
**Files:** `core-domain/model/ProviderId.kt` · `core-domain/store/AppPreferences.kt` ·
`core-data/store/AppPreferencesImpl.kt` · `ai-providers/gemini/GeminiProvider.kt` ·
`feature-chat/ChatViewModel.kt` · `feature-settings/SettingsViewModel.kt` ·
`feature-settings/SettingsScreen.kt` · `.opencode/instructions.md` · `ANSWERS.md`

**1 — `deepseek-chat` was shut down on 2026-07-24 and the app still sent it.** DeepSeek
announced on 2026-04-24 that `deepseek-chat` and `deepseek-reasoner` would be discontinued in
three months; both had pointed at DeepSeek-V4-Flash's non-thinking and thinking modes. The
replacement for chat is `deepseek-v4-flash` (owner's choice, 2026-08-03).
Ref: <https://api-docs.deepseek.com/updates>. This mattered more than a single wrong string:
DeepSeek was the only provider with owner-verified streaming, so the one working end-to-end
path had been dead for ten days without anyone noticing.

**2 — The model name was written in three places, so it could only ever be half-fixed.**
`ChatViewModel.selectProvider` held a `when` over all five providers, `SettingsViewModel.init`
held a parallel map, and `GeminiProvider` held a `const`. All three said `deepseek-chat`.
Model names now exist once, in `ProviderId.defaultModel`; `GeminiProvider.DEFAULT_MODEL`
delegates to it (and is therefore no longer `const`).

**3 — The Settings model field was write-only.** `updateModel` wrote to `SettingsViewModel`'s
in-memory UI state and nowhere else. `ChatViewModel` is a separate object that could not see
it, so editing the field changed nothing and the value died with the screen — while
`GeminiProvider.getEndpoint` carried a comment claiming the opposite. Fixed by moving the
override into `AppPreferences.selectedModels` (SharedPreferences, `model_<PROVIDER>` keys),
which both ViewModels collect. A blank field removes the entry rather than storing `""`, so
clearing it restores the default instead of pinning the provider to a model named nothing.
This is also the operational escape hatch: the next time a vendor retires a name, the owner
can type the replacement instead of waiting for a build.

**4 — Developer Mode was rendered last and the owner reported it as missing.** The chain was
re-traced end to end and is correct: Hilt binds `AppPreferences`, `DeveloperModeCard` renders
unconditionally, both ViewModels collect the flow, `BaseHttpProvider` emits `AiChunk.Meta`,
`ChatScreen` renders the panel. What was wrong is placement — the card sat below four provider
cards and the GitHub stub, roughly 1200dp into an ~840dp viewport, with nothing on screen to
suggest the column continued. Moved to the top of Settings. This knowingly trades against the
`ANSWERS.md` Part 3 onboarding goal of greeting a new user with provider setup; when that flow
is built in M5 the answer is a collapsed "advanced" section, not a return to burying it. The
owner was also unsure whether the tested APK predated the feature, so §8 now opens with a
`clean installDebug` — the placement fix and the stale-APK hypothesis are both still live and
one clean build distinguishes them.

**5 — Found during the verification sweep: every `verify()` carried its own model name.**
`DeepSeekProvider.verify()` sent `deepseek-chat`, so from 2026-07-24 onward Settings would
have reported a perfectly valid DeepSeek key as **invalid** — a second, independent symptom of
the same retirement, and one that would have sent the next agent hunting through the auth code.
`OpenAiProvider.verify()` sent `gpt-4o-mini` while chat sent `gpt-4o`; that asymmetry is the
same bug waiting to happen in the opposite direction — a green tick in Settings while every
message fails. All three now send `id.defaultModel`. The few tokens saved by verifying against
a cheaper model are not worth answering a question nobody asked.

**Also corrected, documentation only:** §5 listed a `SpeechToText` interface in `core-domain`
that does not exist anywhere in the tree (it is an M2 item); `.opencode/instructions.md` still
named GitHub Models as the fourth provider, superseded by `ANSWERS.md` Part 1 §3; `ANSWERS.md`
§12 specified `values-ar` as the default locale folder, which Android has no concept of — the
working structure is `values/` = Arabic. The §12 wording is annotated rather than rewritten,
since it is an owner ruling and only its implementation detail was wrong.

**Verified against primary sources, 2026-08-03:** `gemini-3.5-flash` is stable and carries no
announced shutdown date (<https://ai.google.dev/gemini-api/docs/models>, updated 2026-07-30);
`gpt-4o` appears on no OpenAI shutdown list, though the dated snapshot `gpt-4o-2024-05-13`
retires 2026-10-23. `claude-sonnet-5` was **not** checked and is now flagged in §7.

**Status:** edits complete, not built. Awaiting the owner's clean rebuild and the five checks
in §8.

### 2026-08-03-I · Claude (Cowork) · session handoff hygiene
**Files:** `PROJECT_STATE.md` §7 · `NEXT_TEST.md`

Cleanup before closing the session, so the next agent starts from an uncontradicted tree.

**§7 "Awaiting owner verification" was self-contradicting.** It listed A3 and the language
toggle twice each — once as verified, once as "implemented, awaiting build" — because entries
were appended as results arrived rather than replaced. An agent obeying §0.1 ("everything you
need is here") would have read both and had to guess. The section is now
"Verified on device, 2026-08-03" with one entry per item and no duplicates.

**`NEXT_TEST.md` was stale enough to mislead.** Left over from a session before 2026-07-29, it
still read as a live instruction list: run the reachability script, then evaluate OpenRouter,
Groq, Mistral and Together as providers — none of which are in the plan, since `ANSWERS.md`
Part 1 §3 fixed the four. It also declared M1 complete on a condition since met by other means.
Replaced with a table mapping each instruction to what actually happened. Kept as a file rather
than deleted so this entry's reference does not dangle.

**Correction, made in the same work unit (rule 5).** The first version of that rewrite asserted
`tools/check-reachability.ps1` "does not exist in this repository". **It does** — along with
`tools/reachability-بدون_VPN.txt`, which is the primary source behind §6's latency table. The
claim came from a directory listing that had excluded `tools/`; the fix was to look rather than
infer. Both the file and this entry are corrected. Recorded rather than silently patched
because §0.5 says the code wins over this file, and that has to apply to text written minutes
ago as much as to text written last week.

**Also added to `NEXT_TEST.md`, because §6 invites the mistake:** reachability is not
authorisation. That measurement shows all ten endpoints answering without a VPN — Gemini
included, as "متاح HTTP 404" — yet Gemini refuses real use from this location with
`HTTP 400 — User location is not supported`. A future agent reading §6 could reasonably
conclude Gemini is fine. It is not.

**Standing lesson:** state notes accumulate contradictions when appended to under time
pressure. §0.3 requires updating §7 with every edit; it is worth also *removing* the line the
new one supersedes, in the same pass.

### 2026-08-03-H · Claude (Cowork) · M1 closed · M2 design note
**Files:** `docs/M2_DESIGN_NOTE.md` (new) · `PROJECT_STATE.md` §2, §7, §8

**M1 is closed** at the owner's instruction. Nine checklist rows were closed by device tests
across 2026-08-03, one by the key-safety audit (`-F`), and one — "usable on a fresh install
with only a free-tier Gemini key, no VPN" — by the owner's ruling rather than by a test. That
row is annotated as such in §7 rather than quietly marked pass: the shipped APK requires no
VPN and a friend in a supported country satisfies it literally, but the owner cannot personally
verify it from his location. A future agent reading "pass" deserves to know which kind it is.

**`docs/M2_DESIGN_NOTE.md` written, unsigned.** It scopes voice capture, defines the
`SpeechToText` interface (a single event stream carrying partials, segments, amplitude and
typed errors — following the `AiChunk.Meta` precedent rather than adding interface methods),
places the implementation in a new `:speech` module by analogy with `:ai-providers` so that a
possible 30 MB Vosk dependency never lands in `core-data`, and specifies the mandatory
60–90 second Arabic gate as a repeatable measurement — three runs, compared against a written
source, recording the exact word at which any truncation occurs.

Three things the note surfaces that were not previously written down anywhere:

1. **`docs/BRAND.md` §7 was never executed.** `Color.kt` still contains the Compose template
   palette. BRAND §6 defines the M2 waveform in brand tokens, so M2's signature interaction is
   blocked on identity work owed since M1. The note proposes a short Phase 0.
2. **The "retain raw audio" toggle (`ANSWERS.md` §10) cannot exist with `SpeechRecognizer`,**
   which never surfaces an audio file. Recommendation: defer the toggle until an engine that
   owns the audio ships, rather than shipping a setting that does nothing.
3. **`SttError` must follow the `AiError` pattern from A3** — typed in the domain, phrased in
   the UI. Rebuilding English strings in the data layer would undo work finished hours earlier.

**Status:** awaiting the owner's answers to the four decisions in §7 of the note. No M2 code.

### 2026-08-03-G · Claude (Cowork) · Gemini streams — over a VPN — and is slow
**Files:** none changed. `PROJECT_STATE.md` §7 and §9 only.

Owner ran Gemini successfully on device, 2026-08-03 21:51. **54 chunks, first token 16605 ms,
total 22824 ms**, complete and well-formed Arabic answer, correct multi-turn history
(`user`/`model`/`user`) in the captured body, endpoint `…/gemini-3.5-flash:streamGenerateContent?alt=sse`.

**This proves the Gemini code path end to end** — including the parser rewritten in
`2026-08-03-C`, which now walks every `part` and skips `thought` parts instead of indexing
`parts[0]`. Had that fix been wrong, a thinking model like `gemini-3.5-flash` would have
produced dropped text or leaked reasoning. It produced neither.

**It does not prove the free-tier criterion.** The device status bar shows a **VPN** badge in
every frame of the recording. Gemini works because the VPN moves the exit IP into a supported
region — the block from earlier today is unchanged underneath. §6 records that this project
does not design around a VPN, and the owner's ruling in `ANSWERS.md` Part 3 §B, made hours
earlier, says the shipped APK must not assume one. So the "≥2 providers stream" row in §7 is
now **pass** (the implementation is proven twice over), while the "free-tier Gemini, no VPN"
row stays **fail**. Those are two different claims and only one of them moved.

**New: Gemini is slow for the same reason DeepSeek was.** A one-word greeting («مرحبا»)
returned **1 chunk after 13625 ms**. Google's own table lists `gemini-3.5-flash` as
"Default Thinking: On (medium)", adjustable through `thinking_level`
(minimal / low / medium / high). This is the DeepSeek thinking-mode problem again, on a
different vendor. Logged in §9 rather than fixed here: the documentation I could reach
describes `thinking_level` under the **Interactions API**, and this app calls
`streamGenerateContent`, where the field is nested differently. Hard constraint 2 exists
because an agent once invented a dependency that cost a day — so the exact field name must be
read off the `generateContent` reference before anyone writes it.
Ref: <https://ai.google.dev/gemini-api/docs/thinking>

### 2026-08-03-F · Claude (Cowork) · key-safety audit — PASS
**Files:** none changed. `PROJECT_STATE.md` §7 only.

`ANSWERS.md` Part 3 §C requires an explicit check, before any shareable APK, that no API key,
token, keystore password, Tailscale identity or owner-specific endpoint exists in the source,
resources, build files or artifact. Run 2026-08-03 across the whole tree excluding `build/`:

| Check | Result |
|---|---|
| Vendor key shapes (`sk-ant-`, `sk-proj-`, `sk-`, `AIza`, `ghp_`, `xox[bapr]-`) | none |
| `key`/`secret`/`password`/`token` assigned a literal ≥8 chars | none in app code (three hits, all in third-party docs under `.agents/skills/azure-cloud-migrate/`, not compiled and not shipped) |
| Owner identity — `ASUS`, `C:\Users`, the owner's email | only in `PROJECT_STATE.md` (the adb path, documentation) and `local.properties` (gitignored, never in an APK) |
| Hardcoded IPs / Tailscale identity | none. Every Tailscale mention is prose in docs or `scripts/setup.ps1`, which reads the IP at runtime |
| Sensitive files tracked by git (`local.properties`, `keystore.properties`, `*.jks`) | none — `.gitignore` covers all three and `git ls-files` confirms |
| Opaque literals ≥24 chars in shipped Kotlin | one: `"braining_encrypted_prefs"`, a SharedPreferences filename |

Every string resource that ships (`app`, `feature-chat`, `feature-settings` — ten XML files)
was enumerated and contains only UI text.

The M1 criterion is now **pass**. This is not a one-time clearance: `ANSWERS.md` Part 3 §C ties
the check to *each* release build, and BYOK means a leaked key is a release blocker, not a bug.
Re-run before any APK leaves this machine.

### 2026-08-03-D · Claude (Cowork) · DeepSeek thinking mode
**File:** `ai-providers/deepseek/DeepSeekProvider.kt`

After the `"null"` fix in `2026-08-03-C`, DeepSeek returned **1 chunk, first token at 38235 ms,
total 38236 ms, and an empty reply** (owner's device). The `"null"` text was gone, so that fix
held — but the stream now carried almost nothing.

Cause: **thinking mode is enabled by default on V4, at `high` effort**, and reasoning tokens are
billed against `max_tokens`. The model reasoned for 38 seconds, exhausted the 4096-token budget
on its chain of thought, and had nothing left for the answer. Before `2026-08-03-C` this was
invisible for the wrong reason — every reasoning chunk was being rendered as the literal string
`"null"`, which is what produced the 2916-chunk "successful" run. Neither run was correct; the
first merely looked busier.

This also means `2026-08-03-A` silently changed the product. `deepseek-chat` mapped to
V4-Flash's **non-thinking** mode, so swapping in plain `deepseek-v4-flash` — which defaults to
thinking — was not the like-for-like replacement it was documented as. `buildRequestBody` now
sends `"thinking": {"type": "disabled"}`, restoring the configuration whose streaming the owner
verified on 2026-07-29.

Recorded for whoever enables thinking later (M3 Clarify is the likely candidate): thinking mode
**silently ignores** `temperature`, `top_p`, `presence_penalty` and `frequency_penalty` — no
error, no effect — and returns its chain of thought in `reasoning_content` beside `content`.
`max_tokens` must then cover reasoning *and* answer.
Ref: <https://api-docs.deepseek.com/guides/thinking_mode>

**Status: verified on device 2026-08-03, 20:10.** 1094 chunks, first token **1300 ms**, total
11532 ms, `"thinking":{"type":"disabled"}` present in the captured request body. Against the
run 24 minutes earlier: first token 15376 → 1300 ms, total 29726 → 11532 ms. Developer Mode
paid for itself here — the request body is what proved which build was under test, and the
first-token number is what separated "the answer looks fine" from "the fix landed".

### 2026-08-03-C · Claude (Cowork) · the "null" bug, and what the device tests proved
**Files:** `ai-providers/deepseek/DeepSeekProvider.kt` · `ai-providers/openai/OpenAiProvider.kt` ·
`ai-providers/anthropic/AnthropicProvider.kt` · `ai-providers/gemini/GeminiProvider.kt`

**The bug: `JsonNull.content` returns the string `"null"`.** In kotlinx-serialization a JSON
`null` parses to `JsonNull`, which is a genuine non-null `JsonElement` and a subtype of
`JsonPrimitive`. So in

```kotlin
delta?.get("content")?.jsonPrimitive?.content
```

the safe-call operators never short-circuit, and `.content` hands back the four-character
string `"null"`. Every chunk carrying no text became `AiChunk.Token("null")`. On device this
printed hundreds of `nullnullnull…` in front of a real Arabic answer (owner's recording,
2026-08-03). The fix is `contentOrNull`, which returns Kotlin `null` for `JsonNull`.

**Why it appeared only now.** `deepseek-v4-flash` is a thinking model and streams
`{"delta":{"content":null,"reasoning_content":"…"}}` throughout its reasoning phase. The
retired `deepseek-chat` mapped to the NON-thinking mode and never sent a null content, so the
latent bug had nothing to trigger it. Replacing the dead model name in `2026-08-03-A` is what
exposed a fault that had been in the parser all along.

**A second instance was hiding behind the first.** `finish_reason` is `null` on every
non-final chunk of an OpenAI-shaped stream, and it was read with `.content` too. That branch
was unreachable while `content` never returned Kotlin null. Fixing only the `content` line
would have made `finishReason` read `"null"` on the first reasoning chunk, emit
`AiChunk.Done`, and truncate every reply — trading a visible mess for a silent one. Both lines
are fixed in DeepSeek and OpenAI.

**Same class, other providers.** `AnthropicProvider` read a delta's `text` the same way.
`GeminiProvider` did too, and additionally indexed `parts[0]`, which would drop any chunk
whose first part is a thought — a real risk for a thinking model such as `gemini-3.5-flash`.
It now walks all parts, skips those flagged `thought`, and concatenates. Neither is verified on
device: Anthropic has never been exercised, and Gemini is region-blocked (see §7).

**What the owner's tests proved.** Developer Mode works end to end and is now closed in §7 —
the diagnostics strip rendered under a successful DeepSeek reply (2916 chunks / 651 ms first /
27287 ms total) and under a failed Gemini request, which is exactly the `lastDiagnostics`
design from `2026-07-29-D` doing its job. Streaming on `deepseek-v4-flash` is confirmed. The
error card also proved `describeHttpError()` correct: it extracted Google's own sentence out
of the JSON body rather than dumping the payload.

**Not a defect, recorded in §7:** Gemini answers `HTTP 400 — User location is not supported
for the API use.` at the owner's location. That is a regional refusal and it undercuts the
distribution plan in `ANSWERS.md` Part 3. It needs an owner decision, not a code change, and
explicitly not a VPN (§6).

**Status:** edits complete, not built. Re-test DeepSeek after rebuilding: the reply must
contain no `null`, and must still stream to completion rather than stopping after one chunk.

### 2026-08-03-B · Claude (Cowork) · Gemini streaming desk-check + OpenCode handoff
**Files:** `PROJECT_STATE.md` §9 · `HANDOFF-OPENCODE-A3.md` (new)

No code changed. The owner asked for a live Gemini streaming test; that cannot be done from
this seat — the device build is the owner's (§0 rule 2) and this agent may not issue HTTP
requests programmatically, nor should an API key ever pass through it (hard constraint 3).
What was possible was a desk check of the parser against Google's current documentation, which
surfaced two things now recorded in §9: `parseSSELine` indexes `parts[0]` and will silently
drop chunks whose first part is a thought — material because `gemini-3.5-flash` is a thinking
model — and Google's streaming documentation now covers only the new Interactions API, leaving
`streamGenerateContent` undeprecated but no longer documented.

`HANDOFF-OPENCODE-A3.md` delegates A3 and the newly-approved `androidx.appcompat` language
toggle to an OpenCode session. It is a briefing, not a duplicate: it states the boundaries,
the design, the explicit do-not-touch list, and an acceptance table, and points at this file
for everything else. `androidx.appcompat` is named there as the single dependency that session
is permitted to add, because an unbounded licence to edit build files is how this toolchain
was broken before.

### 2026-07-29-D · Claude (Cowork) · Developer Mode
**Files:** `core-domain/store/AppPreferences.kt` (new) ·
`core-domain/model/RequestDiagnostics.kt` (new) · `core-domain/model/AiChunk.kt` ·
`core-domain/model/AiRequest.kt` · `core-data/store/AppPreferencesImpl.kt` (new) ·
`core-data/di/CoreDataModule.kt` · `ai-providers/BaseHttpProvider.kt` ·
`core-ui/text/BidiText.kt` · `feature-chat/ChatViewModel.kt` · `feature-chat/ChatScreen.kt` ·
`feature-chat/res/values*/strings.xml` · `feature-settings/SettingsViewModel.kt` ·
`feature-settings/SettingsScreen.kt` · `feature-settings/res/values*/strings.xml`

Implements the instrumentation approved in `ANSWERS.md` Part 2 §9.

Design: `AiRequest.diagnostics` gates capture; when set, `BaseHttpProvider` emits a new
`AiChunk.Meta(endpoint, requestBody)` **before** the first token, so the data survives a
request that then fails. Timings and chunk count are measured in `ChatViewModel` rather
than in the provider, deliberately — they should describe what the user experienced, not
what the socket did. The two halves meet in `RequestDiagnostics`, attached to the assistant
message on `Done` and to `ChatUiState.lastDiagnostics` on `Error` (a failed request discards
its bubble, which would otherwise throw the diagnostics away exactly when they became
useful).

`BaseHttpProvider.redactSecrets` scrubs the captured endpoint and body in two independent
passes: the key we were handed, and any key-bearing query parameter regardless of origin.
The second pass exists because the next provider someone adds may put a token in a URL
without remembering this function exists, and hard constraint 3 makes a leaked key a
release blocker.

Toggle persisted through `AppPreferences` / `SharedPreferences` — no new dependency, per
the owner's decision. `AppPreferences` is deliberately separate from `EncryptedKeyStore`:
a debug toggle should not be able to die with a corrupted Keystore keyset.

`BidiText` gained a `forced` parameter: a JSON body reads left-to-right however much Arabic
the payload contains, so content detection must be overridable.

**Status:** awaiting owner verification on device.

### 2026-07-29-C · Claude (Cowork) · localization structure
**Files:** `app/src/main/res/values/strings.xml` · `app/src/main/res/values-en/strings.xml`
(new) · `app/src/main/res/values-ar/` (deleted) · `feature-chat/src/main/res/values/` and
`values-en/strings.xml` (new) · `feature-settings/src/main/res/values/` and
`values-en/strings.xml` (new) · `feature-chat/.../ChatScreen.kt` ·
`feature-settings/.../SettingsScreen.kt`

`values/` held English and was therefore the *default* locale, with Arabic relegated to
`values-ar/` — the exact inverse of `ANSWERS.md` §12. Meanwhile every string actually on
screen was hardcoded in Kotlin, so the resource files were dead weight and screens carried
bilingual labels («الإعدادات — Settings») as a workaround for having no real localization.

Fix: `values/` is now Arabic (the default) and `values-en/` carries the English overrides.
Strings moved into the module that renders them — a library module cannot see the app
module's `R` class, so screen strings in `app` could never have compiled from
`feature-chat` or `feature-settings`. Names are prefixed `chat_` / `settings_` because
library resources merge into one namespace and an unprefixed name can be silently
overridden. `app/res/values-ar/` was **deleted**: it defined `chat_title` as «محادثة`,
which after this change would have overridden the feature module's value on Arabic devices.
Bilingual labels removed; each locale now carries its own wording.

Not touched, and deliberately so: error strings produced in the ViewModel, provider and
`BaseHttpProvider` layers. Translating those literals in place would entrench the wrong
architecture — A3 replaces them with typed errors resolved to resources in the UI.

**Status:** awaiting owner verification on device.

### 2026-07-29-B · Claude (Cowork) · bidirectional text
**Files:** `core-ui/src/main/kotlin/com/braining/core/ui/text/BidiText.kt` (new) ·
`feature-chat/src/main/kotlin/com/braining/feature/chat/ChatScreen.kt`

Arabic answers containing English terms rendered scrambled — words interleaved, punctuation
and digits on the wrong side. Cause: Compose resolves paragraph direction **per paragraph**
from the first strong directional character (`TextDirection.Content`, the default), so in a
mixed answer nearly every line got a different direction. The Unicode bidirectional
algorithm was being handed the wrong paragraph direction; it was not itself at fault.

Fix: `BidiText.kt` resolves **one** direction for a whole block by counting strong
characters via `Character.getDirectionality`, with a deliberate Arabic-first bias (RTL wins
at ≥30 % RTL share) and a minimum sample size that prevents the direction flickering as
tokens stream in. `BidiText` then provides both `LocalLayoutDirection` and
`TextStyle.textDirection` — setting only one yields right-aligned-but-still-scrambled text,
or correct text pinned to the wrong edge. Applied to message bubbles, the error card, the
token bar and the input field. The override is scoped per composable, so bubble placement,
the app bar and the send button still follow the app's own direction.

**Status:** verified by the owner on device, 2026-07-29. Mixed Arabic/English answers render
correctly.

### 2026-07-29-A · Claude (Cowork) · streaming
**File:** `ai-providers/src/main/kotlin/com/braining/ai/providers/BaseHttpProvider.kt`

Replies arrived as one block after ~7 s instead of token by token. The request body, the
SSE parsing and the ViewModel were all correct. Cause: `complete()` used
`httpClient.post()`, a **non-streaming** request — Ktor loads and caches the entire
response body in memory before returning the `HttpResponse`, so `bodyAsChannel()` handed
back an already-complete in-memory copy rather than the live socket. The SSE loop then ran
correctly but only after the last token had arrived.

Fix: `preparePost(url){...}.execute { response -> ... }`, with the status check and the SSE
loop moved inside the `execute` block. Ref: <https://ktor.io/docs/client-responses.html#streaming>.
Import `request.post` → `request.preparePost`. Verified by the owner on DeepSeek: text now
appears line by line. **Never revert this to `post()`.**

Also corrected in this file: the previous claim that DeepSeek returns `HTTP 402
Insufficient Balance` was stale — the account is funded and DeepSeek answers normally.

### Earlier (consolidated, pre-2026-07-29)
Compose BOM aligned in both locations → navigation crash resolved · `EncryptedKeyStoreImpl`
rewritten to be non-throwing with one self-heal pass → Keystore crash resolved ·
`SettingsScreen` API-key field bound to real state → keys can be entered and inspected ·
`verifyResult()` added and adopted by all four providers → invalid keys no longer report
success · Gemini endpoint gained `?alt=sse` and `x-goog-api-key` (chat was previously
unauthenticated) · `getEndpoint()` takes the `AiRequest` so the Settings model field drives
Gemini's URL · model names refreshed to `gemini-3.5-flash` and `claude-sonnet-5` ·
`describeHttpError()` extracts `error.message` · `.catch {}` added before `.collect {}` in
`ChatViewModel` · `CancellationException` rethrown so the stop button is not an error.

---

## 11. REPORT TEMPLATE — use this after every work unit

1. **What changed** — file by file, one line each.
2. **Why** — the diagnosis, not just the edit.
3. **What the owner should run** — exact command and exact test input.
4. **What success looks like, and what a failure would mean** — so a bad result points at
   the next suspect instead of restarting the investigation.

Then confirm you have updated §7, §8 and §10 of this file.
