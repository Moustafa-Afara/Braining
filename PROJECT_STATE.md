# PROJECT_STATE.md — Braining (فهم)

**The single source of truth. Several agents work this repo and cannot see each other's
sessions; this file is the only channel between them. Read it first, update it last.**

> **Condensed 2026-08-07 on the owner's instruction.** The verbatim change log — 57 entries,
> 2026-07-29 → 2026-08-07 — is preserved at **`docs/HISTORY_2026-07_to_08.md`**. §10 keeps the
> lessons and §11 the index. Nothing was deleted; it was moved. Open the archive when an entry
> code in §11 needs its detail.

---

## 0. AGENT PROTOCOL — obey before touching anything

1. **Read this file in full first.** Do not crawl the repo to orient yourself. Open only the
   files your task names.
2. **You edit; the OWNER builds.** Never run `gradlew`, never sync, never install. Finish, report,
   stop. He builds and pastes the output back.
3. **Update this file in the same work unit as the edit, unasked** — §7 (what is true now),
   §8 (where the next agent picks up), §11 (one line, newest first). An edit reported without a
   log line is an incomplete edit.
4. **Append, never rewrite, another agent's record.** If you undo their work, add a line saying
   so and why. (§10/§11 were condensed once, on the owner's explicit instruction, with the
   original archived.)
5. **If this file contradicts the code, the code wins.** Correct the file and record it.
6. **Scope discipline.** Touch only what your task names. Everything else goes to §9 as a note.
7. **Stop rule.** One problem, ~15 minutes or 3 attempts with no measurable progress → stop and
   report what is blocking you. An early agent burned ten hours looping on a one-line fix.
8. **Diagnose before editing.** State your reasoning and assumptions. One correct change beats
   three speculative ones.
9. **Talk to the owner in plain Arabic.** He is Arabic-first and not an Android developer.
   - Short sentences, everyday words. No identifiers, class names or Android APIs unless he must
     type them.
   - Say **what changes for him**, and **what to do, step by step**.
   - **Do not ask him to rule on implementation details.** A question with a recommendation
     attached is a decision you already made — make it, tell him in one line, offer to reverse.
     Product and priority calls are his; error shapes and module boundaries are not.
   - Then a **short teaching section** — he wants to learn the engineering, not be shielded.
     Name the trade-off, not the syntax. A few lines, clearly separated.
   - **Acceptance tables obey this rule too: one instruction, one visible thing, per row.** Name
     the button, the word, the line on screen. A row describing behaviour in the abstract is not
     yet a check — two such rows went unrun on 2026-08-07.
   - Density belongs **here**. The chat stays plain.

10. **Batch a whole milestone; do not ship it in slices.** The owner's ruling of 2026-08-17
    (`ANSWERS.md` Part 9 §E1). §10 entry 2 — "change one thing per build" — is about *attributing*
    an improvement to a cause; it governs two changes to the same behaviour, not the size of a
    milestone. Independent work ships together.
11. **Automated tests are part of "done".** A milestone whose logic could have been unit-tested
    and was not is not finished. **The tests live in `:core-domain`**, whose build file already
    declares JUnit — move testable logic there rather than testing it where it happens to sit.
    Run `gradlew :core-domain:test`. This build has twice been broken by dependency changes; do
    not add a test dependency to another module without a ruling.
12. **Hand the owner every check at once, at the end.** One file — `docs/TESTS_PENDING.md` — the
    automated command first, then tables in build order, one instruction and one visible thing per
    row, and a "what a failure would mean" column. He tests in sittings; do not drip-feed checks.

---

## 1. What this is

**Braining (فهم)** — an Arabic voice-commanded AI orchestrator for Android. `C:\Dev\Braining`.
The owner is the sole human, Arabic-first, and distributes the APK to friends.

**Flow:** speak Arabic → transcribe → the app **interrogates the idea** with you until you declare
it mature → it **forges a professional English prompt** from a framework library → executes it →
translates results to Arabic → spoken feedback loops with full session context → saved to
searchable history.

**Two paths.** A = direct provider APIs from the phone, works with the PC off — the self-sufficient
core, built first. B (M6) = a PC bridge over Tailscale driving OpenCode headless.

**Milestones.** ✅ M1 skeleton + providers + streaming · ✅ M2 voice capture ·
✅ **M3 Clarify + Forge — CLOSED 2026-08-17** · ✅ **M4 route + translate + feedback — CLOSED
2026-08-18** · **«مِداد» identity built 2026-08-18 — untested on the phone** ·
**M5 history + polish — tested on the phone 2026-08-28, four findings fixed the same day
(M5.1)** · **M5.2 providers (OpenRouter, network Ollama) ← next** · M6 PC bridge.

---

## 2. Documents — authority order

| File | Role | Authority |
|---|---|---|
| `PROJECT_STATE.md` | live state, protocol, next step | **highest for state** |
| `ANSWERS.md` | the owner's binding rulings, Parts 1–7 | **highest for decisions** |
| `BRAINING.md` | master spec, 6 milestones | superseded by `ANSWERS.md` on conflict |
| `docs/BRAND.md` | visual identity; the 5-bar mark is the M2 visualiser | authoritative for UI |
| `docs/M2_DESIGN_NOTE.md` | voice capture | **signed 2026-08-04**; rulings restated in `ANSWERS.md` Part 5 |
| `docs/M3_DESIGN_NOTE.md` | Clarify + Forge | **signed 2026-08-07**; rulings in `ANSWERS.md` Part 7 |
| `docs/M5_DESIGN_NOTE.md` | history, first-run, TTS, the release build | written 2026-08-28 from the owner's five answers (`ANSWERS.md` Part 11) |
| `docs/M2_GATE.md` · `docs/M3_GATE.md` | the gates, as instruments — **both closed** | the rulings stay in the notes |
| `docs/TESTS_PENDING.md` | **every check waiting on the owner's hands**, in build order | the instrument; §8 points at it and does not copy it |
| `docs/PROMPT_FRAMEWORKS.md` | the FORGE library spec | reference |
| `docs/DEEPGRAM_DESIGN_NOTE.md` | cloud STT design | signed in effect; §8 decisions were agent-taken |
| `docs/HISTORY_2026-07_to_08.md` | the verbatim change log to 2026-08-07 | archive |
| `docs/ARCHITECTURE.md` | module/layer design | **stale in 4 places — see §9** |
| `docs/SETUP.md` · `.opencode/instructions.md` | build/signing · OpenCode discipline | reference |

---

## 3. Toolchain — exact, do not change without an explicit order

Gradle **9.0** · AGP **8.13.0** · Kotlin **2.3.10** · KSP **2.3.10** · Hilt **2.58** (deliberate —
2.60.x needs AGP 9) · Ktor **3.5.1** · kotlinx-serialization **1.11.0** · Compose BOM
**2026.06.01** · activityCompose 1.10.1 · appcompat 1.7.1 · JVM target 17 via
`kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }` (never `kotlinOptions`) ·
**Room 2.8.4** (raised from the unused 2.6.1 on 2026-08-28 — see below) ·
minSdk 26, compile/target 35 · package `com.braining.app`.

**Room 2.6.1 sat in the catalog unused since M1 and could never have worked here.** It is from
November 2023 and has no KSP2 support; KSP2 support arrived in Room **2.7.0** (April 2025), whose
release note says it "is recommended when using Room with Kotlin 2.0 or higher". This project runs
Kotlin 2.3.10 and KSP2. The first build that actually invoked Room failed in one line —
`IllegalStateException: unexpected jvm signature V` out of `KspAAWorkerAction` (AA = the Analysis
API, i.e. KSP2). **`room-ktx` is deliberately absent**: since 2.7.0 the artifact is empty and
Room's own note asks for it to be removed.

**Compose BOM is written in two places and must stay in sync:** `gradle/libs.versions.toml` and
the hardcoded string in `build-logic/convention/src/main/kotlin/braining.android.compose.gradle.kts`.
Drift between them caused the `setViewTreeSavedStateRegistryOwner` navigation crash.

---

## 4. Hard constraints — violations have cost days

1. **Never modify:** `gradle-wrapper.properties` · `gradle.properties` (`android.builder.sdkDownload=false`
   and `org.gradle.java.home` are load-bearing) · the hand-written `package.xml` files in the
   Android SDK · `keystore.properties`. **Never** open Android Studio's upgrade assistant or SDK
   "Quickfix" — both have destroyed this build.
2. **`io.ktor:ktor-client-sse` does not exist.** SSE is in `ktor-client-core`. An agent
   hallucinated it and it cost a day. Verify every artifact against the catalog or Maven Central.
3. **BYOK, always.** Keys entered at runtime, Keystore-encrypted. Never hardcode, embed, log,
   commit, or put a key in a URL query string. The shipped APK contains zero owner-specific data —
   distribution to friends is a first-class goal (`ANSWERS.md` Part 3).
4. **Hilt multibinding:** `Map<String, @JvmSuppressWildcards AiProvider>` — the annotation goes on
   the *value* type, never the key.
5. **kotlinx-serialization 1.11:** wrap primitives in `JsonPrimitive(...)` inside `buildJsonObject`.
   And **always `contentOrNull`, never `.content`** — `JsonNull.content` returns the string `"null"`.
6. **Arabic-first.** User-facing strings in resources, never in Kotlin. `values/` is Arabic
   (default), `values-en/` overrides — never `values-ar/`. RTL default: `start`/`end`, never
   `left`/`right`; mirror directional icons. **Model system prompts are not user-facing** and stay
   in Kotlin — localising them would let the UI toggle change how the model reasons.
7. **Never revert `BaseHttpProvider.complete()` to `httpClient.post()`.** `preparePost{}.execute{}`
   is what makes streaming stream.
8. **Feature modules are siblings.** Anything two of them need goes to `core-ui` / `core-domain`,
   never to a peer. Paid for twice: `AiErrorMessage` and `DiagnosticsPanel`.

---

## 5. Modules

| Module | Contents |
|---|---|
| `app` | `MainActivity` (AppCompatActivity), `BrainingApp`, `navigation/NavGraph.kt`, manifest, `res/values*` |
| `core-ui` | `theme/`, `text/BidiText.kt`, `error/AiErrorMessage.kt` + `SttErrorMessage.kt`, `diagnostics/DiagnosticsPanel.kt`, **`routing/RouteMessage.kt`**, **`components/BrainingButtons.kt`**, `input/SubmitOnCtrlEnter.kt`, **`voice/`** (`VoiceCapturePanel` — docked, **not** a modal sheet; `BrainingWaveform`, `MicPermissionDialog`), `res/font/`, `res/values*/` (`error_*`, `dev_*`, `voice_*`) |
| `core-domain` | interfaces `AiProvider`, `SpeechToText`, `ClarifyEngine`, `PromptForge`, `EncryptedKeyStore`, `AppPreferences`, **`ModelRouter` (+ `DefaultModelRouter`, pure Kotlin, unit-tested)**; **`text/ScriptDetector`**; **`routing/RoutingDecision` + `RouteReason`**; models `AiRequest`, `AiChunk`, `AiError`, `ChatMessage`, `ProviderId`, `RequestDiagnostics`, `SttError`, `TranscriptionEvent`; `clarify/` — `ClarifyState`, `ClarifyTurn`, `TurnKind`, `ClarifySession`, `ClarifyEvent`, `ForgeEvent`, `ForgedPrompt`, `FrameworkOption` |
| `core-data` | `EncryptedKeyStoreImpl`, `AppPreferencesImpl`, `di/CoreDataModule` (Ktor `HttpClient`, `Json`, `HttpTimeout` 600/15/60 s) |
| `ai-providers` | `BaseHttpProvider` + `anthropic/ openai/ deepseek/ gemini/` + `ErrorClassifier` + `di/ProvidersModule`. **Four providers, no fifth** — GitHub Models removed 2026-08-17 (`ANSWERS.md` Part 8 §D1) |
| `speech` | `RoutingSpeechToText` → `DeepgramSpeechToText` (WebSocket + `AudioRecord`) or `AndroidSpeechToText`; `di/`. Declares `RECORD_AUDIO`, `ACCESS_NETWORK_STATE`, the `RecognitionService` `<queries>` |
| `feature-chat` | `ChatScreen`, `ChatViewModel` |
| `feature-clarify` | **M3.** `ClarifyEngineImpl` (five-state machine over any `AiProvider`), `PromptForgeImpl`, `ClarifyPrompt` / `ForgePrompt` / **`TranslatePrompt`** (the three system prompts, one place each), `ClarifyScreen` / `ClarifyViewModel`, `di/`, `res/raw/prompt_frameworks.json` (the framework library, as data) |
| `feature-settings` | `SettingsScreen`, `SettingsViewModel`, **`OnboardingScreen` / `OnboardingViewModel`** (M5 first-run) |
| `feature-history` | **M5.** `HistoryScreen`, `HistoryViewModel`. Depends on `:core-domain` and `:core-ui` only — **never on `:feature-clarify`**; re-running a saved session travels through the NavGraph as an id |
| `build-logic/convention` | `braining.android.application` / `.library` / `.compose` |

**Single points of change — do not duplicate these:**

- **`ProviderId.defaultModel` is the only place a model name is written.** It once lived in three
  files and was wrong in all three at once when DeepSeek retired `deepseek-chat`.
- `core-ui/text/BidiText.kt` — the only thing that decides text direction. `forced` for JSON/URLs.
- `core-ui/diagnostics/DiagnosticsPanel.kt` — the only Developer Mode readout. Chat and Clarify.
- `BaseHttpProvider.complete()` — all four HTTP providers inherit it; fix transport there once.
- `BaseHttpProvider.redactSecrets` — the only thing between an API key and Developer Mode.
- `AppPreferences` — the only home for non-secret settings: Developer Mode, the per-provider model
  override, **the "about me" note**, and **the selected provider**. Its `MAX_PROFILE_LENGTH` is the
  one place the note's ceiling is written; the store and the Settings counter both read it.
- `ClarifyPrompt` / `ForgePrompt` / `TranslatePrompt` — one place each. Prompt text that gets
  copied gets edited in one copy.
- **`core-ui/components/BrainingButtons.kt` — the only place a button's shape and press response
  are decided.** Material's `Button` / `OutlinedButton` / `FilledTonalButton` are not called
  directly anywhere; `BRAND.md` §5b is the rule and this file is its single implementation.
- **`core-ui/res/values*/app_name` — the only place the app is named.** «فهم» / «Braining», and
  it follows the device's language. In `:core-ui` and not in `:app` because the first-run flow
  greets by it and a library module cannot see the app module's `R` (§10 entry 18). Never
  hardcode it in Kotlin or copy it into another string.
- **`PromptPreview` — the only thing that turns a captured request body into prose.** Four
  provider shapes, one reader. The raw body stays beside it and is not replaced.
- **`ArabicNormalizer` — the only thing that folds text for search.** Applied twice: once when a
  record's `searchText` is written and once to the query. Two nearly-identical folds would give a
  search that works for whoever wrote it and fails silently for the user.
- **`HistoryContext` — the only place the history block for the prompts is built**, and the only
  place its budget (`MAX_SESSIONS`, `MAX_CHARS`) is written. It rides on every Clarify turn.
- **`SessionSummary` — the only place a session is reduced to a line**, for the list and for the
  prompt block alike.
- **`DefaultModelRouter` — the only thing that decides which provider answers**, including every
  fallback. It is pure Kotlin in `:core-domain` so it could be unit-tested without touching a
  build file, and `DefaultModelRouterTest` pins its order and its refusals.

---

## 6. Build, device, network

**Device:** Xiaomi Redmi Note 13 Pro 5G (`2312DRA50G`), Android 14, USB.

```
cd C:\Dev\Braining
.\gradlew.bat installDebug
```

Xiaomi needs "Install via USB" + "USB debugging (Security settings)" and shows a transient dialog.
`adb` is loose in the SDK root: `C:\Users\ASUS\AppData\Local\Android\Sdk\adb.exe`. If
`No online devices found`: `adb disconnect` → `adb kill-server` → `adb devices`. **Never open SDK
Manager** (constraint 1); Wi-Fi pairing is broken and is a convenience with a bad risk trade.

**Network.** All 10 provider endpoints are reachable **without** a VPN (219–700 ms). **A VPN
roughly doubles latency** (542–977 ms) — which matters because seconds are a gate number. Never
design around a VPN; the shipped APK must not assume one.

**Reachability is not authorisation.** Gemini answers TCP fine and still refuses real requests from
the owner's location with `HTTP 400 — User location is not supported`. He reaches it **over a VPN,
deliberately** — that is his workaround, not a property of the product.

---

## 7. State now — 2026-08-17

### ✅ M1 CLOSED 2026-08-03 · ✅ M2 CLOSED 2026-08-06

**M1.** Streaming genuinely token-by-token on DeepSeek, Gemini and Anthropic · `verify()` rejects
a bad key with a typed Arabic sentence · multi-turn history correct · Settings model override
reaches Chat · Arabic RTL everywhere + in-app EN toggle · A3 typed errors (all seven cases) ·
Developer Mode · key-safety audit pass · rotation and process death survive.

**M2.** Voice capture with the five-bar waveform on real RMS · editable transcript · the 60–90 s
gate passed (~480 words, three readings, reached the end each time) · **Deepgram Nova-3 `ar-SY`
streaming** is the engine, with `AndroidSpeechToText` retained for no-key/no-network · audio never
written to storage · typed Arabic errors for every failure · OFL licence in the binary.

**Known limits that do not block anything:** the transcript loses words and always will — the
final dialect test was 45 words / 49 s with Levantine rendered as Levantine and the idea intact.
Gemini is geo-blocked at his location. Anthropic's promo credit expires **19 Sep 2026**.

### ✅ M3 CLOSED 2026-08-17 — on the owner's instruction

He closed it on the strength of the gate verdict below. **Gate runs 2 and 3 are cancelled, not
outstanding** — the earlier §8 asked for them, that ask is withdrawn, and nothing in M3 is open.

Speak → interrogate → «نضجت الفكرة» → forge an English prompt → execute → Arabic answer.

| Piece | State |
|---|---|
| Five-state machine, `:feature-clarify`, unscoped Hilt binding | ✅ |
| Clarify screen: streamed Arabic turns, kind labels, editable replies | ✅ |
| Shared `DiagnosticsPanel` — the system prompts are readable on device | ✅ |
| FORGE: framework chosen + Arabic reason + editable prompt + swap + regenerate | ✅ owner: **"النتائج مذهلة حقاً"** |
| Execute → answer streams in Arabic, selectable, copy buttons `P` / `R` | ✅ |
| `retry()` repeats the right one of three actions | ✅ six observations |
| Convergence (`[[كافٍ]]`), first-turn restatement, drift announcement, Ctrl+Enter | ✅ |
| **The gate** (`docs/M3_GATE.md`) | ✅ **passed on the owner's verdict, 2026-08-08** |

### The gate — passed, and the abandoned run is the most useful of the three

| Run | Result |
|---|---|
| 1 | (ب) wins. The interrogation carried the idea into a different and better question, by his own choice |
| 2 | **Abandoned.** Too many questions; he gave up before «نضجت الفكرة» |
| 3 | Completed. *"النتيجة ممتازة"* — and a lighter subject than run 2 |

**Owner's verdict, 2026-08-08: two different cases succeeded, the gate passes at this stage.**
His words on the general pattern: pressing «نضجت الفكرة» after a long *or* short discussion gives
**"جواب محدد جداً ودقيق"**. Path (أ) without the interrogation gives a competent professional
answer with reasonable assumptions — good, but it leaves the user to find the depth themselves.

**Run 2 failed for fatigue, not for quality, and that is the finding.** It is what produced the
batch/single turn shape. He also frames what comes next himself: *"على أعتاب تغيير جذري لاحق
بالإصدار الثاني"*.

### The open problem he named, and it is the sharpest diagnosis in this file

**The engine over-specifies because it has no idea who it is talking to.** His example: a father
struggling with his small child should never be asked *"do you want the results compared against
academic research?"* — the question is only reachable because the engine has **no prior context,
no session history and no user profile** to rule it out. Asking it is not precision; it makes the
user carry the cost of denying things that never occurred to them.

**Half of it is fixed now, for free.** `ClarifyPrompt` now says: assume the likeliest case for
someone who says this, ask only where a wrong assumption would waste the user's time, and put the
assumption in the suggestion turn so it can be corrected.

**The other half is a real feature and needs the owner's ruling.** Three shapes, cheapest first:

| | What | Cost |
|---|---|---|
| **أ** | A short "about me" written once in Settings, injected into the Clarify system prompt | ~30 lines. Directly kills his example. **Not in any spec — new scope** |
| **ب** | Session history as context — the app remembers previous conversations | **This is M5.** Already planned, and it is the real answer |
| **ج** | Nothing until v2 | Free, and the interrogation stays wider than it needs to be |

**Recommendation: أ now, because it is small and it is the exact miss he hit — and ب arrives on
its own with M5.** But it is new scope on a milestone that has just passed its gate, so it is his
call, not mine.

### The interrogation was too slow, and the cause was a line I wrote

The owner, 2026-08-07: the questions repeat, it takes a very long time, and it does not widen the
idea. **He asked whether the fault was the app, the model, or him. It was mine, and it is
nameable:** `ClarifyPrompt` said *"in each turn do one thing only: one question"*. Five questions
meant five round trips. **His own screenshot of the plain chat shows the same model asking nine
questions in one message when nothing forbids it** — batching is its natural behaviour and the
prompt suppressed it.

**Four rulings followed (2026-08-07):**

| | Ruling |
|---|---|
| Turn shape | **Mixed.** A batch of 2–4 numbered questions when they are independent — which is the norm for the opening turn — and a single question **with buttons** when it is a follow-up |
| Syntax split | **Numbers mean questions, dashes mean answers.** The parser takes dashes only; accepting numbers would turn a batch of questions into answer buttons |
| Brain | **Anthropic**, for the next runs. `BRAINING.md` §5 names Claude the default brain for CLARIFY and it has never once been tried. One comparison separates "design fault" from "model fault" — which was the owner's actual question. Credit expires 19 Sep 2026 and none of it is spent |
| Widening | The engine must **propose**, not only ask. At least one «اقتراح» turn offering something the user did not mention — an interrogation that only asks leaves the user where it found them |

**Judging path (ب) on two answered questions is not the gate.** He noted this himself. The gate
assumes the idea *matured*; two answers do not mature an idea, so a result matching plain chat is
expected rather than informative.

**Gate run 1.** Path (أ) sent straight: *"سطحية وعلى قدر السؤال"*. Path (ب) through Clarify+Forge:
**the idea was transformed into a different question entirely — by his own choice, through the
answering**, and he enjoyed it. **(ب) wins run 1.** Runs 2 and 3 are not run, and two of three is a
fail, written before any run.

**Run 4 (the damaged transcript) passed by a route the design did not predict.** Clarify **does not
notice** transcription damage; the *conversation* recovers the meaning. The design note assumed
detection would be the mechanism. A faster, longer dictation degraded the input much further than
run 4 tested.

**The owner's own finding, sharper than anything in the note:** the interrogation is
**«سلاح ذو حدّين»** — it can carry the idea off its axis, and he only caught it because he watched
every exchange. Not prevented (the same property produced the run he valued) but now **announced**:
a question that shifts the subject says so and asks whether that is intended.

### The owner's rulings of 2026-08-17 — all four recorded in `ANSWERS.md` Part 8

| | Ruling |
|---|---|
| M3 | **Closed.** Runs 2 and 3 cancelled |
| The over-questioning | **Shape أ is built:** a short "about me" note written once in Settings. It reaches **CLARIFY and FORGE only** — plain chat stays a clean instrument, and giving it a system prompt is a separate product call he has not made |
| GitHub Models | **Removed from the app entirely.** Overrides `ANSWERS.md` Part 1 §3 |
| Delivery | **Two builds, not one.** Build A = the recording panel + the GitHub removal. Build B = the note, the empty-credit message, the small fixes. §10 entry 2 is the reason: one build carrying five changes cannot tell you which one broke |

### The recording sheet was modal, and that was the whole fault

His report, testing the voice button in Clarify: while dictating an answer he **could not scroll
the conversation** back to re-read the question, and the **first touch that tried ended the
recording**.

Neither is a bug inside the recorder. `VoiceCaptureSheet` was a `ModalBottomSheet`, and a modal
draws a scrim, swallows every touch behind it, and treats an outside touch as *dismiss*. The
component was doing exactly what it was built to do. **The modality was the defect**, so the
modality is what went: `core-ui/voice/VoiceCapturePanel.kt` is an ordinary composable each screen
places at its foot, in place of the input row. The conversation above keeps its weight, its
scrolling and its touches.

**A labelled «إلغاء» replaces the swipe-away**, because removing a modal removes the gesture that
abandoned a run, and an escape hatch deleted without a replacement is a trap (§10 entry 26).

**The general lesson, and it is a new one:** *a component chosen for how it looks brings its
interaction model with it.* A bottom sheet was the right shape for a recorder and the wrong
**mode** for one used while reading the page underneath. Nothing in the sheet's own code could
have shown that — only using it could.

### Build B — written 2026-08-17, **not yet built or tested**

Four changes and one correction. All of it compiles in the editor's sense only; nothing below has
run on a device.

| What | Where it landed |
|---|---|
| **The "about me" note** | `AppPreferences.userProfile` (+ `MAX_PROFILE_LENGTH` = 600) · a card in Settings · read per turn by `ClarifyEngineImpl` and once per forge by `PromptForgeImpl` · appended by `ClarifyPrompt.system(profile)` and `ForgePrompt.system(…, profile)` |
| **`AiError.InsufficientCredit`** | Matched on the response **body** in `BaseHttpProvider`, *before* the status codes — an empty balance is 400 at Anthropic and 429 at OpenAI, so status-first would classify one condition two ways |
| **The provider is remembered** | `AppPreferences.selectedProvider` · restored and persisted in `ChatViewModel` |
| **Gemini's dropped chunks** | **Already fixed in the code** — `parseSSELine` iterates every part and skips `thought`. §9 had listed it as open since 2026-08-07; the entry was stale, not the code |

**Two things the note is deliberately *not*.** It is not sent with a plain chat message
(`ANSWERS.md` Part 8 §D3), and it is not a topic: the prompt carries three explicit prohibitions
telling the model not to quote it, not to assume every idea belongs to it, and not to drag the
conversation toward a detail in it. Without those, a note saying "English teacher" turns a
question about a car into a question about teaching.

**It is visible in Developer Mode**, because it is part of the system prompt — which is the only
honest way to confirm it reached the model at all.

### ✅ M4 CLOSED 2026-08-18 — every row of `docs/TESTS_PENDING.md` passed on the owner's phone

**What the owner ran and passed:** the automated suite, then Build A, Build B and M4 together
against `docs/TESTS_PENDING.md` — the routing line, the forced fallback through a region-blocked
Gemini, the manual re-run, the feedback loop, and (after the procedure was rewritten) the
translate button. Three defects surfaced and were fixed the same day: «إلغاء» kept the dictation,
the prompt box deleted itself when emptied, and the assumption instruction had never been tested
at all.

**§٣ز passed on the rebuild** — «امسح», «تراجع عن المسح», and the box that no longer vanishes.
With that, nothing in M4 is outstanding. **The file `docs/TESTS_PENDING.md` is now a record, not a
queue**; the next agent must not read it as work.

Four rulings from the owner the same day (`ANSWERS.md` Part 9) shaped it, and three of them
*removed* work the original spec asked for.

| Piece | What shipped |
|---|---|
| **The router** | `ModelRouter` + `DefaultModelRouter` in `:core-domain`, bound in `:ai-providers`. Always Path A. **No classification call** — see below |
| **The decision is visible** | A line under the answer: «أجاب: Claude (Anthropic) · claude-sonnet-5». `core-ui/routing/RouteMessage.kt` phrases it; `RouteReason` is an enum so the domain never holds a sentence |
| **Manual override** | A row of provider chips under the answer re-runs it. **For that answer only** — the chat's selection is untouched |
| **Fallback** | Automatic on a provider-side failure, with a **red** line naming both: «تعذّر Google Gemini، فأجاب Claude». Never on a missing/invalid key or a dead network — those are the user's setup, and routing around them hides a problem they must fix |
| **Translation** | On demand. `ScriptDetector` offers the button only when the answer does not look Arabic; the original is kept and the button flips back |
| **The feedback loop** | The note is appended to the execution exchange and the whole thing re-sent, so «وسّع النقطة الثانية» refers to something. Dictatable — the same voice panel |
| **Tests** | 23 JVM checks in `:core-domain`, run by `gradlew :core-domain:test`. No device, no keys, no network |

**Three things the spec asked for and did not get, each on a ruling, not an oversight:**

1. **No A/B classification call.** `BRAINING.md` §3 requires it and `ANSWERS.md` Part 2 §11 says
   it must be an API call. Both stand — **for M6**. Today Path B does not exist, so a classifier
   would spend a round trip and the user's money on every request to answer a question with one
   possible answer, and would put an untestable branch in the hot path. `RoutingDecision.NeedsPc`
   exists so M6 adds a branch rather than widening a type every caller assumes is single-valued.
2. **No AI-router toggle** (`BRAINING.md` §5). It toggles between a classifier and a rule table;
   neither exists yet. It arrives with the classifier or not at all.
3. **No mandatory translate step**, which `ANSWERS.md` Part 7 §M3-2 had promised. That ruling was
   written when answers came back in English. `2026-08-07-M` made the forged prompt require Arabic
   and quietly made the step unnecessary — **and nobody noticed for ten days**, because the plan
   was never re-read against the code. Same shape as the stale §9 entry about Gemini.

**Fallback is deliberately absent from plain chat.** Chat is the instrument for testing a
provider, a key or a model (`ANSWERS.md` Part 7 §M3-1); an instrument that quietly switches to a
different provider when the one under test fails is measuring the wrong thing.

### First real test round on the owner's phone — 2026-08-18

Build A, Build B and M4 were installed together and run against `docs/TESTS_PENDING.md`. **Almost
everything passed**, including the routing line and the forced fallback through a
region-blocked Gemini. Three things did not, and only one of them was a defect.

**1. «إلغاء» kept the words. Fixed.** Transcribed segments land in the input field as they arrive
— that is what makes them appear while you speak — so by the time cancel is pressed the words are
already there, and cancelling cancelled nothing. Both ViewModels now remember the field's contents
at `startVoice` and restore them. **The button had been promising something the code never did**,
which is §10 entry 14 wearing different clothes.

**2. «افترض الحالة العادية» had never actually been tested — and this file said it had.** The
owner looked for a *button*. There is none: it is rule 4 inside `ClarifyPrompt`, invisible except
through Developer Mode. The entry `2026-08-08-A` recorded it as verified because it travelled in a
list with two visible checks (the app's name, the microphone) that *were* verified, and his "it
worked" was read as covering all three. **A real check now exists** — `docs/TESTS_PENDING.md` §2ب,
which separates "the instruction reached the model" from "the model obeyed it".

**4. The prompt box deleted itself.** Clearing the last character of the forged prompt by hand
removed the *field*, leaving nothing to type into and no way back except regenerating. It was
wrapped in `if (forgedPrompt.isNotEmpty())` — **a container gated on its own content, which is a
container that cannot be refilled.** It now renders unconditionally, and an «امسح» button empties
it in one tap. That button becomes «تراجع» while the box is empty: a one-tap delete of two
thousand characters needs an undo, and an undo costs nothing until the mistake happens, where a
confirmation dialog taxes every use.

**3. The translate button never appeared, and the procedure was the fault.** The owner appended
`Reply in English only.` to a plain chat message. The button does not exist in plain chat — and
even in the right place, appending English to the forged prompt loses to the OUTPUT CONTRACT
inside it, which explicitly requires Arabic. **The test was fighting the product's own rule.**
Rewritten to replace the whole prompt, and — the useful part — **Developer Mode now prints the
detector's own number** under the answer: «نسبة العربية في الجواب: ٩٧٪ · زر الترجمة يظهر تحت
٢٠٪». Three possible causes for a missing button collapse into one glance.

### The visual identity — «مِداد» adopted 2026-08-18, and one addition rejected

The owner reviewed three directions and adopted **مِداد (Ink)**: the deep-violet *ground* becomes
a near-black neutral with a faint violet bias, and the violet itself moves to what is touched.
His words on the proposal: «الآن صار التصميم أجمل فعلاً». The full spec — palette in both themes,
the four button weights, the corner radii, and the three `docs/BRAND.md` rules it overrides — is
the preview page produced that day.

**One addition was proposed by him, built as a preview, and then rejected by him: the dot falling
onto the amber centre bar when «تمّ» is pressed.** He asked for it, saw it animated, and said no.

**Nothing was written to the repository for it** — no `BRAND.md` edit, no Kotlin. That is the
point of previewing a design before building it, and it is why cancelling cost nothing.

**Built the same day, and not yet seen on the phone.** The palette, the shapes, the four button
weights and the press response are in the code; `docs/BRAND.md` §2, §5 and the new §5b are
rewritten, and the rulings are `ANSWERS.md` Part 10. What changed, file by file:

| File | Change |
|---|---|
| `core-ui/theme/Color.kt` | Rewritten. `BrandPalette.Ink` (dark) and `.Paper` (light). The error family carries over unchanged and still clears 4.5:1 |
| `core-ui/theme/Shape.kt` | **New.** 10 / 14 / 20 / 24 / 28dp across Material's five slots |
| `core-ui/theme/Theme.kt` | Both schemes rebuilt. **The `surfaceContainer*` ladder is now set explicitly** — left unset, `darkColorScheme()` fills it from Material's baseline purple and cards render in a palette nobody chose |
| `core-ui/components/BrainingButtons.kt` | **New.** Four weights + `Modifier.pressScale`, 16dp corners |
| `core-ui/voice/BrainingWaveform.kt` | The mark reads `primary` / `tertiary` instead of fixed hexes |
| Clarify · Settings · the voice panel | Every filled and outlined button converted to a weight |

**Do not revive it.** A later agent reading the brand's own line — "the dot is the moment of
insight" — will find the landing animation an obvious idea. It was tried, and the owner's verdict
on seeing it stands.

### ⏳ M5 CODE COMPLETE 2026-08-28 — **compiles and its tests pass; its behaviour is unverified**

**Status on 2026-08-28, after two build rounds:** `gradlew :core-domain:test` is green (84 checks)
and `installDebug` succeeds. That proves the code compiles and that the pure logic is right. It
proves **nothing** about whether a session is actually saved, whether search finds anything, or
whether the history block reaches the model — all of which are `docs/TESTS_PENDING.md` §٢–§١١ and
all of which need the owner's hands.

**Written as one batch** (§0 rules 10–12): `docs/M5_DESIGN_NOTE.md` first, then the whole
milestone, then one test file. The owner's five rulings are `ANSWERS.md` Part 11.

| # | Piece | What shipped |
|---|---|---|
| 1 | **Room-backed history, searchable** | `:core-data/history` — entity, DAO, database, repository. **Room 2.8.4** — the catalog's unused 2.6.1 could not run under KSP2 and the first build proved it (§3). Schemas exported to `core-data/schemas/`. **No `fallbackToDestructiveMigration`, deliberately** — that one line would silently delete every session on a future schema change |
| 2 | **Re-run a past task** | A saved run reopens in `READY` with its prompt, its idea and its turns, and **asks nothing** — the user already pressed «نضجت الفكرة» once. Travels as `?session=<id>` on the existing Clarify route |
| 3 | **Delete one · delete all · storage used** | Delete-one has an **undo**; delete-all has a **confirmation naming the count**. The split is the rule: undo where the action is cheap and common, confirm where it is rare and total |
| 4 | **History feeds CLARIFY** | `SessionSummary` → `HistoryContext` → `ClarifyPrompt.system` and `ForgePrompt.system`. **The engine already wrote the summary** — its `[[كافٍ]]` turn — so no extra call is spent producing one |
| 5 | **First-run flow** | `OnboardingScreen` in `:feature-settings`. Gemini pre-selected per Part 3 §B, **with its regional refusal stated up front in Arabic** rather than met as a bare 400. «تخطَّ» always enabled |
| 6 | **Loading / empty / error states** | Three shared composables in `core-ui/state`. Settings **gained a back button** — a §9 item, closed |
| 7 | **TTS readback** | `TextReader` in `:core-domain`, `AndroidTextReader` in `:speech`. Off by default. Queues one utterance until the engine initialises, so it works on the **first** press |
| 8 | **Release APK** | proguard extended for Room and serialization; `docs/SETUP.md` rewritten with the signing procedure; key-safety audit run over 135 source files — **clean** |
| — | **Tests** | **59 new JVM checks in `:core-domain`, 84 by annotation count.** Search folding, summary selection, the prompt budget, storage formatting, relative time. *(The pre-M5 figure was recorded as 23 while `@Test` counts 25; the runner's number is the one to trust.)* |

**Three facts about this build that matter more than the table:**

1. **The release build has never been run in this project's life.** Every build to date was
   `installDebug`, which does not run R8 at all. The proguard rules added for Room and
   serialization are a **claim about a build that has not happened** (§10 entry 3), which is why
   `docs/TESTS_PENDING.md` §١٠ is a step of its own with its own one-line diagnosis.
2. **The signing keystore does not exist on the owner's disk.** `keystore.properties` still holds
   placeholder values and there is no `.jks` anywhere. `assembleRelease` cannot succeed until he
   runs the `keytool` command in `docs/SETUP.md`. **Losing that file later means never being able
   to update an app his friends have installed.**
3. **`ClarifyTurn`'s subclasses carry explicit `@SerialName` values, and they must never change.**
   Polymorphic JSON's default discriminator is the fully-qualified class name, which R8 renames —
   so a history written by a debug build would be unreadable by the release APK, appearing as
   sessions that had lost their interrogation rather than as any error. Those short names are on
   disk in every user's history.

### The owner's first M5 test round — 2026-08-28, and four things it found

**§2, §3, §5 and §9 passed.** History saves, survives a restart, updates in place; re-run reopens
a saved prompt and asks nothing; the empty, loading and back-button states are right. His four
rulings are `ANSWERS.md` Part 12 and the fixes shipped the same day.

| What he found | What it turned out to be |
|---|---|
| **The list was titled with the raw dictation** | Working as built and wrong as designed. The model now writes a two-to-five-word Arabic name — **on the forge call that already happens**, as a `[[العنوان]]` marker, so it costs nothing. Schema 1 → 2 with a real migration |
| **Developer Mode's body was "متداخل ببعضه وفيه رموز `\n`"** | Correct: it is one JSON line where every newline is two characters. `PromptPreview` renders it as labelled Arabic parts; **the raw stays one tap below** (§10 entry 6) |
| **«استمع» flipped twice and then ran silently** | **One fault, and the interface was hiding it.** `TextReader` carried a bare boolean, which can say *speaking* or *not speaking* and cannot say *this phone has no Arabic voice*. So a missing voice was reported as an utterance that began and ended instantly. The contract now carries a typed `ReaderStatus`, and the screen says what is wrong and opens the settings screen that fixes it |
| **Gemini answered «حدث خطأ غير متوقّع»** | **Not yet diagnosed, and that is the honest state.** Three causes produce that sentence and none can be told apart from it. The §9 item open since 17 August — the provider's own words captured, redacted, and shown to nobody — is now closed, so the next attempt prints what Gemini actually said |

**Also shipped in the same batch:** manual choice of the fallback provider with a «جرّب أي واحد»
escape (reversing his own ruling of 17 August), a «لغة الجهاز» option that makes the language
choice reversible for the first time, the app named «فهم» / «Braining» by the device's language,
and the spoken word marked in the answer with the page following it **until the reader's finger
moves**.

**Test count: 105.** New: the request-body reader, and the fallback candidate list.

---

## 8. Next step

### ✓ THE RELEASE BUILD IS PROVEN — 2026-08-30

`assembleRelease` completed. The APK was signed (v2/v3), installed, and audited on the real
artifact: 3.6 MB, no API key, no filesystem path, no device id, **no signing password**.
`isMinifyEnabled = true` is no longer a claim. **That whole category of failure is now exhausted**
— all four build failures this project has had (KSP running Room, R8, `lintVitalRelease`,
signing) were "the first time this tool ever ran", and three of them run only on release builds.

### ⚠ M5.2 IS BUILT AND UNCOMPILED — 2026-08-30

The share link, the two copy buttons and `ApiKeySanitizer` landed in one batch and **no compiler
has seen them.** Structural checks pass (braces, imports, every `R.string` resolved across
modules under `nonTransitiveRClass`, format specifiers against their call sites, AAPT2
apostrophes) and an adversarial review fixed 8 defects including 2 compile-breakers — but
structural checks are not a compiler. First command of the next session:

```
.\gradlew.bat :core-domain:test 2>&1 | Out-File -Encoding utf8 build-log.txt
```

**`| Out-File -Encoding utf8`, never `>`.** PowerShell's `>` writes UTF-16 and the log is then
unreadable to the agent, which costs a round trip every time.

### ⛔ AND M5.1's BEHAVIOUR IS STILL UNTESTED — now with M5.2 stacked behind it

The whole milestone landed on 2026-08-28 in one batch and **not one line of it has executed on a
device**. `docs/TESTS_PENDING.md` is a complete session's worth of checks, in build order, and it
is the next thing that happens. Do not begin M6 while it is unrun.

```
cd C:\Dev\Braining
.\gradlew.bat :core-domain:test      ← 120 checks · one minute · no phone
.\gradlew.bat installDebug
```

**The three checks most likely to fail, and why they are worth naming in advance:**

| Where | What would break | Why it is the likely one |
|---|---|---|
| `installDebug` | `:feature-history` or Room's KSP step | A new module and a new annotation processor in one build. The error line will name which |
| `TESTS_PENDING` §٤.٢ | Searching `احمد` does not find `أحمد` | The fold is unit-tested, so a failure here means the **stored column** was written by a different path than the query — the one thing a unit test cannot see |
| `TESTS_PENDING` §٦ | The history block never reaches the model | Invisible except through Developer Mode. It has its own section for exactly that reason (§10 entry 37) |

---

### Then M6 — the PC bridge (Path B)

Tailscale, OpenCode headless, the three guardrails. **And two items that were deferred *into* M6
by name and must not be read as missed work:**

- **The A/B classification call.** `BRAINING.md` §3 and `ANSWERS.md` Part 2 §11 both require it.
  It was deliberately not built in M4 because Path B did not exist, so a classifier would have
  spent a round trip and the user's money to answer a question with one possible answer.
  `RoutingDecision.NeedsPc` already exists so M6 adds a branch rather than widening a type.
- **The AI-router toggle** (`BRAINING.md` §5). It toggles between the classifier and a rule table;
  it arrives with the classifier or not at all.

**Before building either, re-read them against the code** (§10 entries 33 and 34). Both of those
lessons were paid for by plans that had quietly been made unnecessary or already done.

---

### What M5 deliberately did not do, so nobody reads it as missing

- **A Clarify session still does not survive process death mid-interrogation** (`ANSWERS.md`
  Part 7 §M3-4). M5 persists **finished** runs. Resuming an interrupted interrogation is a
  different feature with a different failure surface and no ruling.
- **No hard size cap and no auto-deletion.** `ANSWERS.md` Part 1 §10 — storage used is surfaced
  and the user decides. An app that silently deletes the user's thinking to save 40 MB has made a
  decision that was not its to make.
- **No FTS, no sync, no export.** Sync is closed (Part 11 §K5). Export was never asked for.
- **Plain chat is still not recorded**, and still sends no system prompt. Chat is the instrument
  for testing a provider, a key or a model; an instrument that accumulates state measures
  something different every time it is used.

---

## 9. Deferred queue — do not touch unless assigned

**Correctness, ranked:**

- ~~**`AiError.Unknown.detail` is still invisible.**~~ **Closed 2026-08-28** — `ProviderErrorDetail`
  in `core-ui`, rendered under the error card in chat, Clarify and Settings whenever Developer
  Mode is on. It was opened on 17 August and closed the day it was needed: the owner met «حدث خطأ
  غير متوقّع» from Gemini and there was nothing on any screen that could say why.
- ~~**Settings gives no feedback that a key was saved.**~~ **Partly closed 2026-08-30** — a
  pasted key now reports what was *repaired* in it (`KeyFixNotice`), on all five key fields. It
  still does not say "saved"; that remains open, and is smaller than it was.
- **`verify()` is the only thing that can say a key works, and three fields do not have it.**
  Deepgram has no `verify()` at all (below); the sanitizer can prove a key is *well-formed* and
  nothing more. Do not let the repair notice be read as validation.
- **`ApiKeySanitizer` is not applied to a key already stored.** It runs on entry. A key saved
  before 2026-08-30 keeps whatever damage it arrived with until the owner re-pastes it. Cheap to
  fix at read time in `EncryptedKeyStore`; deferred because it would silently rewrite a stored
  credential, which §10 entry 45 is precisely about.
- ~~**The fallback chooser exists only in Clarify.**~~ **Closed 2026-08-30** — chat has it. It
  was never on the queue, which is the point of §10 entry 47: nobody noticed it was missing until
  a provider failed on that screen.
- **The AI-router toggle is unbuilt** (`BRAINING.md` §5). It toggles between an AI classifier and
  a rule table and neither exists; it belongs to M6 with the classifier. Recorded so it is not
  read as a missed M4 item.
- **Chat sends no system prompt at all.** An Arabic-first app with no instruction to answer in
  Arabic: a message containing only «.» came back in fluent Chinese, and the reply then steered
  every later turn until the chat was cleared. **A product call, not a defect to fix quietly** —
  it costs tokens on every request and changes what the app is.
- **`verify()` ignores the user's model override** — it always sends `id.defaultModel`, so a bad
  model name shows a green tick and chat then fails. Fixing means widening the interface.
- **Deepgram has no `verify()`.** The card stores a key and cannot say whether it works.
- Gemini `verify()` uses `generateContent` while chat uses `streamGenerateContent`.
- Gemini still sends deprecated `temperature`; thinking is on by default and costs 13–17 s to
  first token. **Verify the field name against the `generateContent` reference before writing it.**
- Real `TokenUsage` parsing instead of zeros on `[DONE]` — Developer Mode shows `0` by design.

**Note on deletions:** the agent tooling on this machine can move files but cannot delete them.
Retired files go to a `_to_delete/` folder outside every source set and the owner removes it — he
did exactly that on 2026-08-18 for `VoiceCaptureSheet.kt` and `GitHubModelsStub.kt`.

**Opened by M5.1, recorded rather than built:**

- **Gemini's refusal is still undiagnosed.** The owner's key produced `AiError.Unknown` on
  2026-08-28. The instrument to read it now exists; the reading has not been taken. **Do not
  guess at the cause** — three are plausible (a retired model name, an unentitled key, a quota
  shape the classifier does not know) and §10 entry 3 is about exactly this.
- **`ForgeReader`'s title parsing has no unit test.** It is a private class in `:feature-clarify`,
  which has no test source set, and §0 rule 11 forbids adding a test dependency to another module
  without a ruling. Moving the reader to `:core-domain` would make it testable and is the obvious
  next step if it ever misbehaves.
- **The word-boundary highlight depends on the engine.** `onRangeStart` is optional for a TTS
  engine; `ReaderStatus.reportsWords` records whether it ever fired, and Developer Mode prints it.
  A highlight that never moves is explainable rather than mysterious — but it is not fixable.

**Opened by M5, recorded rather than built:**

- **The release build is unproven.** The proguard rules for Room and serialization are written
  and have never been exercised. Until `assembleRelease` runs and the resulting APK saves and
  reopens a session, `isMinifyEnabled = true` is an untested claim.
- **No Room migration exists, and none can until version 2.** The schema is exported to
  `core-data/schemas/` so that one can be written properly. **Never add
  `fallbackToDestructiveMigration` to make a schema change compile** — it deletes every user's
  history silently, against the ruling that keeps their text until they delete it.
- **History detail has no screen of its own.** A row shows its title, summary, provider and age;
  reading the full interrogation means re-opening the session. Enough for now, and named here so
  it is not mistaken for an oversight.
- **`AndroidTextReader` never calls `shutdown()`.** It is a `@Singleton` living for the process,
  which is correct, but a device-level engine held for the app's lifetime is worth revisiting if
  battery ever becomes a question.

**Design notes, no action yet:**

- **Four places collect an `AiProvider.complete()` stream** and map it to their own events —
  `ChatViewModel`, `ClarifyEngineImpl`, `PromptForgeImpl`, `ClarifyViewModel.execute`. Deliberately
  not extracted: they differ in exactly what an abstraction would have to parameterise, and M4's
  router is the unknown fifth caller.
- Settings gives no save feedback for the four provider cards (the Deepgram card does — copy it).
- ~~Settings has no back button.~~ **Fixed 2026-08-28.** `statusBarColor` deprecation is
  `@Suppress`ed; the real fix is `enableEdgeToEdge()` + insets. `readUTF8Line` and
  `MenuAnchorType` are deprecated. Unused `jsonArray` import in `BaseHttpProvider`.
- Markdown / code-block rendering in chat answers, code forced LTR.
- Give `build-logic` access to the version catalog so the Compose BOM stops being a string literal.
- Themed-icon (`<monochrome>`) rendering unverified — HyperOS exposes no toggle. Any Pixel closes it.

**Environment noise — recorded so nobody "fixes" it:**

- **`MainActivity must extend android.app.Activity [Instantiatable]`** from `lintVitalRelease`.
  **A false positive**, silenced with `tools:ignore` on that one `<activity>` element and nowhere
  wider — the full reasoning is in the manifest beside it. It fires only on release builds, which
  is why it went unseen until 2026-08-30. **Do not "fix" it by disabling the rule module-wide**:
  the same check is what would catch a genuinely uninstantiatable service later.
- **`WARNING: R8: An error occurred when parsing kotlin metadata`, roughly 130 times** during
  `assembleRelease`. R8 ships inside AGP 8.13.0 and predates Kotlin 2.3.10, so it cannot read the
  newer metadata format and falls back to language-agnostic shrinking. **It is a warning and the
  build completes.** The consequence is that R8 leans on the explicit keep rules in
  `app/proguard-rules.pro` rather than on Kotlin metadata — which is why those rules are written
  out by hand for serialization, Room and the sealed `ClarifyTurn` hierarchy instead of being
  left to inference. **Do not "fix" it by upgrading AGP: hard constraint 1.** SDK "inconsistent location" warnings about
`platform-tools-2` and `cmdline-tools` (that is what hand-written `package.xml` files look like to
the tooling) · `Kotlin does not yet support 25 JDK target, falling back to JVM_24` (daemon JDK;
§3's JVM 17 output is unaffected) · Wi-Fi ADB pairing "Version Too Low". **All roads lead to
`gradle.properties` or SDK Manager, which constraint 1 forbids.**

---

## 10. Lessons that bind — distilled from 57 entries

These are the ones that were paid for more than once. The archive has the incidents.

**On diagnosis**

1. **A platform error code names the symptom the platform saw, not the cause.**
   `ERROR_LANGUAGE_UNAVAILABLE` became "Arabic is not installed" and sent the owner to three
   settings screens that were all already correct. Exhaust what might be wrong with *your request*
   before telling a user what is wrong with their device.
2. **A fix followed by an improvement has not thereby been shown to be its cause.** Two things
   changed in one build; the log credited the wrong one and stated it as fact. **Change one thing
   per build**, or accept that the changelog is telling a story.
3. **Publishing a confident cause from partial evidence has happened five times.** The fifth was
   about the *owner's* configuration, not the device — the one category that cannot be settled by
   reading the tree, and therefore the one where asking is cheapest.
4. **A failed build is evidence, not a blank result.** How far it got clears suspects.

**On instruments**

5. **When a subsystem chooses a strategy at runtime, the chosen strategy is diagnostic output**,
   not an implementation detail. Every real fault in M2 was found by a number put on screen for a
   different reason.
6. **A diagnostic that is confidently wrong is worse than none** — it is believed. So is one that
   is confidently **blank**.
7. **A test whose pass and fail look identical is not a test.** An airplane-mode check could not
   see which of three retries had run, because all three failed the same way.
8. **Asking a human for a number the device already holds is a measurement design error.**
9. **Publish a number you cannot yet interpret.** M2's segment count was published before anyone
   knew what it meant; it later revealed a third of the speech was being lost.
10. **A metric is only comparable across engines that fail the same way.** Segment count meant
    word loss on `SpeechRecognizer` and means nothing on Deepgram.
11. **Write the fail criterion before the first run.** A criterion written after seeing results is
    not a criterion.

**On correctness that looks fine**

12. **A correct instruction can be obeyed at the wrong level.** FORGE was told "the answer must be
    Arabic" and put that requirement inside the *prompt it was writing*, one level too deep.
13. **The failure that looks like a success is the dangerous one** — a rigorous prompt for the
    wrong idea passes every check that examines form.
14. **A label that describes what the code used to do is a wrong label.** Twice: the engine tag
    after a language toggle, and «الجواب — بالإنجليزية» after the answer became Arabic.
15. **A type that is *almost* the type you wanted, silently:** `JsonNull.content` returns `"null"`
    (cost a day); `StringBuilder.trim()` returns `CharSequence` (caught by the compiler only
    because the destination was typed).
16. **Code correct for the expected input can be quietly destructive for the plausible one** —
    a header reader that ate the first 400 characters whenever a model formatted its answer
    slightly differently. Written twice in two days.

**On architecture**

17. **Feature modules are siblings.** Paid for twice. The tempting fix is always a dependency that
    must not exist, and it compiles happily until the day it becomes a Gradle cycle.
18. **A resource lives where its consumer can read it.** A library module cannot see the app
    module's `R`.
19. **Ask of every Hilt binding: does this object hold something belonging to one conversation?**
    If yes, it is not a `@Singleton`.
20. **A composable correct in its old home is not thereby correct in its new one.** Three layout
    faults in one screen: two children in one lazy item, a growing child in a region that cannot
    scroll, an unbounded field above the controls.
21. **A screen that fires one request on open cannot rely on a flow still warming up.** Read it
    with `first()`.
22. **Resolve late what the user can change late.** A binding fixed at graph construction cannot
    see a key entered afterwards.

**On working with the owner**

23. **A question with a recommendation attached is a decision already made.** Asking it anyway
    moves the reading burden onto the person least equipped to carry it.
24. **Never make a product call in his name by quietly ordering a list.** An agent chose "offline
    first" citing a ruling that had answered a different question.
25. **Every check written so far asked whether the output was correct; none asked whether it could
    be used.** Correctness is verifiable from a transcript. Usability is only visible to the person
    holding the phone.
26. **A control that has to be guessed at has not been labelled.** Two identical copy icons; the
    content descriptions served a screen reader and nobody else.
27. **A rule that shapes a model's behaviour is a design decision, not a phrasing choice.** "One
    question per turn" read like good practice and was the entire cause of the interrogation
    feeling like an interrogation. The evidence was in plain sight for days: the same model, asked
    the same thing outside Clarify, batched nine questions into one message.
28. **When the same symptom is fixed wrongly twice, stop fixing and start instrumenting.** Option
    buttons "worked but not always" through two guessed causes. The third attempt put on screen
    what the model had actually written.
29. **An abandoned test is a result, and often the most useful one.** Gate run 2 was given up
    from fatigue, not disagreement — and it, not the two successes, is what produced the change
    to how the interrogation asks.
31. **A component chosen for its shape brings its interaction model with it.** A bottom sheet was
    the right *look* for a recorder and the wrong *mode* for one used while reading the page
    underneath: modal means a scrim, swallowed touches, and dismiss-on-outside-tap. Nothing in the
    component's own code could have shown that. Only using it could.
32. **A fact handed to a model that has nothing else to hold onto becomes the subject.** The
    "about me" note had to ship with three explicit prohibitions — do not quote it, do not assume
    every idea belongs to it, do not steer toward it — because background that is merely *present*
    reads as background that is *relevant*.
40. **A `Row` does not wrap — it compresses, and the last child pays.** Five controls sat in one
    row of the forge panel. The redesign widened every button, the row ran out of width, and the
    final label was squeezed narrower than one word — so Compose broke it **one letter per line**.
    The redesign did not create the fault; it removed the slack that was hiding it. Any row of
    buttons whose contents can grow needs either a wrap, a scroll, or a second row decided in
    advance.

48. **A guard that detects the untouched default does not protect against the mistake people
    make.** `share_download_url` shipped as `…/USER/REPO/…` and the button was hidden while the
    string still contained `USER/REPO`. The owner replaced the **whole string** with his
    repository's clone URL — `…/Braining.git`, which is the URL GitHub puts in front of you on
    the repository page — and the guard passed it, because the placeholder was indeed gone. The
    check now tests the **shape** the value must have (`https://github.com/…/app-release.apk`)
    rather than the shape it must not. **"Did they edit it?" is a weaker question than "is it
    right?", and only the second one is worth asking.**

47. **A feature built on one screen is not a feature; it is a feature on one screen.** The manual
    fallback chooser landed in Clarify on 28 August and the milestone was recorded as done. Two
    days later Gemini returned 429 **in chat** — where the chooser had never been built — and the
    card named the failure and offered nothing. The whole point of the 28 August ruling was that
    the user picks who answers when a provider fails; on the screen he uses most, he could not.
    A capability stated in a ruling belongs on **every screen the failure can reach**, and the
    only reliable way to know which those are is to grep for the error path, not to remember.

46. **`?:` after a call whose `null` means success inverts the result silently.**
    `provider?.verify(key) ?: AiError.Unknown(… "Provider not found")` — `verify` returns `null`
    for **a key that works**, so every good key in onboarding was reported as a missing provider.
    It compiles, it type-checks, and it is exactly backwards. The elvis operator is a test for
    absence and it cannot tell "nothing to report" from "nothing there"; when `null` is a
    **result** and not a gap, ask the two questions separately with an `if`. Grep the codebase for
    `?:` sitting after any call documented to return `null` on success before trusting the next
    one.

45. **A value the user cannot see is a value nobody can debug — including the provider.**
    Gemini refused the owner's key for two days behind «حدث خطأ غير متوقّع». The key was correct
    in every character but one: a `-` had become `—` in a copy-and-paste, and an em dash is two
    pixels from a hyphen on a phone. **Nothing in the app could see it, and neither could he.**
    Three lessons, in the order they cost time: the vendor's own sentence was already being
    captured and rendered to nobody (§9's oldest item — *capture without display is not
    diagnosis*); the fault was in **input the app accepted without inspecting**, not in the logic
    everyone was reading; and the class of fault is systematic, not freak — an Arabic-first app
    invites invisible bidi marks into every pasted field by construction. `ApiKeySanitizer` now
    checks the input, repairs only what has exactly one possible original, and **says what it
    changed**. Validate what crosses the boundary; a credential is plain ASCII and that makes the
    damage provable rather than guessable.

44. **A static analyser's error is a claim about its own analysis, not about the code.** Lint
    failed the release build with "MainActivity must extend android.app.Activity" — about a class
    the platform had already instantiated dozens of times on the owner's phone. **The decisive
    evidence was not more reading; it was behaviour that had already happened.** Before spending a
    build on a tool's complaint, ask what the running app has already proved: a class that has
    been constructed cannot be unconstructible, and no amount of analysis outranks that. The
    corollary is the one that saves the time — when the tool is wrong, suppress it **exactly where
    it is wrong** (`tools:ignore` on one element), never by turning the check off, or the next
    real instance of the same fault ships silently.

43. **A type that cannot express a failure will report it as a success.** `TextReader` shipped
    with `speaking: Boolean`. A phone with no Arabic voice therefore produced *true* on the press
    and *false* a millisecond later — the owner saw the button flip twice and then run silently,
    and neither symptom named its cause, because **the interface had no word for what was wrong**.
    Adding a typed `ReaderStatus` fixed the symptom and the diagnosis at once. The general form:
    before deciding *how* to report a state, ask what states the return type is capable of
    holding — a boolean has exactly two, and one of them will end up meaning three things.

42. **A dependency parked in a version catalog is not a dependency that works.** Room 2.6.1 was
    added to the catalog during M1 and left unused for six weeks. `PROJECT_STATE.md` §8 then read
    "Room 2.6.1 is already in the version catalog — use it; do not upgrade it", which sounds like
    a tested constraint and was in fact a note about a line in a file. The first build that
    actually invoked the processor failed immediately: 2.6.1 predates KSP2 by two years. **A
    version that has never been resolved, compiled or run is a plan, not a fact** — and §0 rule 5
    is the escape hatch that existed for exactly this: the code wins, correct the file, record it.
    The general form: an instruction that pins an *unexercised* dependency is pinning a guess, and
    it should be written as one.

41. **A screen stopped by an empty state is not stopped unless its controls are stopped too.**
    A failed restore drew "this session no longer exists" and left the whole control block
    rendering underneath it. Everything there was inert except one button — «نضجت الفكرة», which
    is ungated on purpose because the user owns that decision — and pressing it would have forged
    a prompt from a session that was never opened. **An empty state is a message, not a guard.**
    The general form: when a screen gains a terminal state, every branch below it needs the same
    condition, and the one control that is deliberately always-enabled is the one that will still
    fire.

39. **A wrapper narrows an API, and the narrowing has to be deliberate.** The new button
    wrappers omitted `contentPadding`, and the one call site that set it — Clarify's option
    buttons, where an option is a sentence and not a word — stopped compiling. Before wrapping a
    component, read what its call sites already pass. Anything dropped is a rewrite disguised as
    a refactor.

38. **A container gated on its own content cannot be refilled.** The forged-prompt field was
    drawn only `if (forgedPrompt.isNotEmpty())`, so deleting the last character deleted the field.
    The general form: an input's visibility must never depend on the input's value. The same
    mistake in an empty-state, a list header or a search box has the same shape and the same
    exit — there isn't one.

36. **A test procedure that fights the product's own rules is not a test.** To see the translate
    button, the plan said to append "reply in English" to a prompt whose OUTPUT CONTRACT requires
    Arabic. The model obeyed the contract, correctly, and the check reported a defect that did not
    exist. Before writing a step that forces a behaviour, ask what in the product is designed to
    prevent exactly that.
37. **An invisible check bundled with visible ones is marked passed by association.** "The name,
    the microphone, and the assumption instruction" went out as one list; two were on screen and
    one was a line inside a prompt. The owner said it worked — meaning the two he could see — and
    the log recorded all three as verified for ten days. **Give an invisible thing its own row,
    and make the row name where to look.**

34. **A planned step can be made unnecessary by an earlier fix, and the plan will not notice.**
    `ANSWERS.md` Part 7 §M3-2 promised that M4 would translate every answer. Ten days earlier, a
    change to the forged prompt had already made answers arrive in Arabic — so the promised step
    would have cost a second call on every request to change nothing. Plans decay exactly like
    queues do (entry 33): **before building a planned item, re-read it against the code.**
35. **The fastest thing you can do for a human tester is stop needing them.** Twenty-three checks
    that used to require eyes, a phone and a paid API call now run in a minute with none of the
    three. The lever was not writing code faster; it was moving the testable logic into the one
    module that could already be tested.

33. **A deferred item can be fixed by someone else and stay on the list.** §9 carried
    "GeminiProvider reads parts[0] only" for ten days after the code had started iterating every
    part. A queue is a claim about the code, and it decays: re-read it against the source before
    working from it, never instead of reading the source.

30. **A question the model has no business asking is usually a missing fact about the user, not a
    bad prompt.** "Do you want a comparison with academic research?" is reachable only because
    nothing tells it who is speaking. Some of that is fixable by instruction; the rest is
    context the app does not yet keep.

---

## 11. Change log index — newest first, append only

One line each. Full text in `docs/HISTORY_2026-07_to_08.md`.

> **Dates before 2026-08-17 are labels, not clocks.** Sessions dated `2026-08-07` and
> `2026-08-08` were still running as late as **16 August** by the file timestamps on disk — an
> agent dated its entries by assumption and the next one inherited the drift. The order of the
> entries is right; the dates are approximate. **From 2026-08-17 they are real.** Do not compute
> an interval from any date above that line.

```
2026-08-30-E  **The fallback chooser reaches chat**, and the share-link guard is repaired. The
              owner's Gemini key was fixed by the sanitizer's own rule (he re-pasted it) and the
              provider then returned a real **429 quota** error — which is exactly the case the
              28 August ruling was written for, and chat had no chooser. `ChatViewModel` gained
              `router`, `fallbackOptions`, `triedThisTurn`, `chooseFallback`, `tryAnyFallback`
              and `retry`; `sendMessage` split into "append the turn" + `runCompletion(provider)`
              so a retry and a fallback are one operation with a different argument. Chat's
              fallback **persists the choice** (a conversation continues; Clarify's answers one
              question) — §10 entry 47. Separately, `ShareCard`'s readiness test now checks the
              URL's shape rather than the absence of the placeholder, after the owner replaced
              the whole string with his repository's `.git` clone URL and the old test passed it
              — §10 entry 48.
2026-08-30-D  **M5.2 — the batch the Gemini failure paid for.** Three owner requests
              (2026-08-30): the share button now shares the **GitHub download link**; the request
              body in Developer Mode gained a copy button per part; **every provider error gained
              a copy button, outside Developer Mode.** Plus the thing the incident actually
              demanded: `ApiKeySanitizer` in `:core-domain` — trims, drops interior whitespace,
              deletes invisibles, replaces look-alikes (7 dashes, curly quotes, Arabic-Indic
              digits, full-width forms) and **reports what it changed** in `KeyFixNotice`;
              anything else non-ASCII is flagged and left alone, because guessing at a character
              inside a credential turns a legible failure into a mysterious one. Wired into all
              three key entry points. `versionCode 2` / `versionName 1.1.0`. 15 new tests, 120
              total. Adversarial review found 8 defects including 2 compile-breakers
              (`state.keyFixes` referenced in a card with no `state`; `R.string.copy_action`
              resolved against the wrong module's R under `nonTransitiveRClass`) — all fixed
              before the owner's build. **A pre-existing onboarding bug fell out of the same
              read**: see §10 entry 46.
2026-08-30-C  **git exists.** The project had no version control at all through five milestones.
              `.gitignore` had `/build` where it needed `build/` — the root-anchored form matched
              nothing while `app/build` alone held 257 MB. First commit `c189b22`, 428 files,
              audited: no keys, no `keystore.properties`, no `.jks`, no build output. APK audit on
              the real artifact: 3.6 MB, v2/v3 signed, zero owner data. **The signing key and its
              passwords stay out of git permanently** — losing the `.jks` means no user can ever
              upgrade in place.
2026-08-30-B  release build round 2 — **R8 passed** with the new rules, and the build moved on to
              fail at `lintVitalRelease`: "MainActivity must extend android.app.Activity". A
              false positive about a class Android has instantiated on the owner's phone dozens
              of times; `.BrainingApp` on the same manifest did not trip it, and it extends a
              framework class directly rather than a library one — resolution, not code.
              Silenced with `tools:ignore` on that single `<activity>` element, with the reasoning
              written beside it. §9 records it, §10 gains entry 44. `keystore.properties` now
              verified complete: both passwords present, no stray quotes, the `.jks` resolves
2026-08-30-A  first `assembleRelease` in the project's life — failed at R8, both causes found
              and fixed. (1) Four `com.google.errorprone.annotations.*` classes missing: they
              come with Tink under `androidx.security:security-crypto`, have compile-time
              retention and are never packaged — R8 wrote the `-dontwarn` lines itself into
              `missing_rules.txt` and they are now in `proguard-rules.pro`, with the documented
              JSR-305 / OkHttp-TLS / slf4j set beside them so the round trip does not repeat four
              times. (2) **`keystore.properties` had both passwords empty**: `keytool` asks six
              visible questions and two invisible ones, the owner copied the six visible ones —
              which belong inside the `.jks` and are ignored by this file — and left the two that
              matter blank. The file is rewritten so the two required lines are the only live
              ones. `.gitignore` widened to `keystore.properties*`, because a `.bak` holds the
              same secret the original does. §9 records R8's Kotlin-metadata warnings as noise
2026-08-28-G  review of the M5.1 tree before handover — seven defects found and fixed without a
              build: an unescaped apostrophe in an English string would have failed AAPT2 · the
              spoken-word highlight rebuilt the whole answer sixty times a second and starved its
              own follow-scroll (now drawn in the draw phase, not styled into the string) · the
              reader trimmed the text it spoke while the screen showed it untrimmed, shifting
              every highlight · a new answer did not silence the previous one · a model that
              omitted the title marker blanked a restored session's name · an empty message
              vanished from the readable request view · a tap was treated as a scroll and would
              have cancelled following on the very button that starts it. §10 gains entry 43
2026-08-28-F  **M5.1** — the owner's first M5 test round on the phone: §2/§3/§5/§9 pass, four
              findings fixed the same day (ANSWERS.md Part 12) · session names written by the
              model on the forge call, schema 1→2 with a real migration · Developer Mode's
              request body readable in Arabic with the raw one tap below · TTS contract rewritten
              around a typed status after a boolean reported a missing Arabic voice as an instant
              success · spoken word marked and followed, yielding to the reader's finger · manual
              fallback choice with «جرّب أي واحد» (reverses the 17 Aug automatic fallback) ·
              «فهم»/«Braining» by device language, and a «لغة الجهاز» option that finally makes
              the language choice reversible · §9's provider-detail item closed · 105 tests
2026-08-28-E  **M5 builds and its tests pass** — `:core-domain:test` green, `installDebug` green
              on the owner's machine after the Room bump. Nothing of M5's *behaviour* has been
              exercised yet: `docs/TESTS_PENDING.md` §٢–§١١ is the whole of what is outstanding
2026-08-28-D  first build of M5 — two failures, both fixed. (1) `:core-domain:test` 83/84: my own
              `ArabicNormalizerTest` expectation forgot that the object folds ة→ه; the code was
              right and the test was wrong, rewritten to compare against `normalize` of the
              trimmed form so it pins only the property in its name. (2) `:core-data:kspDebugKotlin`
              died on `unexpected jvm signature V` — **Room 2.6.1 has no KSP2 support**, and this
              build is KSP2. Raised to **2.8.4** and dropped `room-ktx` (empty since 2.7.0).
              §3 records the new pin, §10 gains entry 42
2026-08-28-C  adversarial review of the M5 tree before handover — nine defects found and fixed
              without a build: `debounce` is @FlowPreview not @ExperimentalCoroutinesApi ·
              `TextToSpeech.setLanguage` returns int so `tts.language = …` does not compile ·
              the re-run route built an empty path segment and would have crashed on every
              press · a restored run kept no `createdAt` and would have been re-dated to the
              epoch · regenerate on a restored run forged from an empty session because the
              engine was never opened · the delete-all dialog counted the *filtered* list while
              deleting everything · the record stored `displayName` instead of the stable
              `ProviderId.name` · `isAvailable()` answered before the TTS engine had initialised,
              so the no-Arabic-voice guard never fired · `VACUUM` was missing, so «احذف الكل»
              left the storage readout unchanged. **The review is the reason none of these
              reached the phone** — §10 gains entry 41
2026-08-28-B  **M5 CODE COMPLETE** — Room history with Arabic-folded search, re-run a saved
              session, delete with undo / delete-all with a counted confirmation, storage used ·
              history summaries feed CLARIFY and FORGE under one budget, and the «نبذة عنك» note
              stays beside them (Part 11 §K2 overrides §8's "replaces") · first-run flow with
              Gemini's regional refusal stated up front · opt-in TTS that works on the first
              press · shared empty/loading/error states, and Settings finally has a back button ·
              59 new tests (82 total) · proguard extended for Room and serialization · key-safety
              audit clean over 135 files · **nothing has run on a device, the release build has
              never been attempted, and the signing keystore does not exist yet**
2026-08-28-A  «مِداد» passed on the phone and the split control row compiled — both pre-M5
              blockers cleared · the five M5 questions answered in one reply, all five
              recommendations adopted (ANSWERS.md Part 11) · TESTS_PENDING.md is now wholly a
              record; three unexamined §٤ rows carried into the M5 test file instead of being
              marked passed by association · M5 execution begins
2026-08-18-I  handoff — §0 gains the working method (batch a milestone, tests in :core-domain,
              all checks at once) · §8 rewritten as a complete M5 brief with the five questions
              to ask in one message · the tree carries one unbuilt edit, named at the top of §8
2026-08-18-H  «امسح» was rendering one letter per line — five controls in a Row with no room;
              split into two rows by function
2026-08-18-G  build fixed — the button wrappers had dropped `contentPadding` and Clarify's
              option buttons set it
2026-08-18-F  «مِداد» built — neutral ground, four button weights with a press that answers,
              radii raised, the mark follows the theme · BRAND.md §2/§5/§5b rewritten ·
              ANSWERS.md Part 10
2026-08-18-E  «مِداد» adopted as the new identity — neutral ground, violet as the interactive
              colour · the falling-dot animation was asked for, previewed, and rejected on sight;
              nothing had been written for it
2026-08-18-D  **M4 CLOSED** · the owner asks for a complete visual redesign before M5 —
              the dark theme's deep violet, and buttons with no modern interaction
2026-08-18-C  the owner's test round passes — router, fallback, re-run, feedback and translate
              all confirmed on the phone · only §٣ز of TESTS_PENDING is unreported
2026-08-18-B  the prompt box vanished when emptied — an input drawn only while it had content ·
              «امسح» added, and it turns into «تراجع» while the box is empty
2026-08-18-A  first full test round on the phone — Build A, B and M4 nearly all pass · «إلغاء»
              kept the dictation and now restores the field · the assumption instruction had
              never been tested and this file had said it was · the translate procedure was
              fighting the forge's own Arabic contract; Developer Mode now prints the detector's
              percentage so the button's absence is readable
2026-08-17-C  **M4 code complete** — the routing decision is on screen, fallback is automatic and
              announced, translation is offered not forced, and the feedback loop continues the
              answer · first automated tests in the project's life (23, in :core-domain) ·
              docs/TESTS_PENDING.md becomes the single instrument for everything untested
2026-08-17-B  the «نبذة عنك» note reaches CLARIFY and FORGE · an empty balance is now named
              instead of «خطأ غير متوقّع» · the chosen provider survives a restart · the Gemini
              item in §9 was already fixed in code · docs made consistent for a fresh agent
2026-08-17-A  the recording sheet was modal — the conversation now reads and scrolls while you
              dictate into it · GitHub Models removed from the app · M3 CLOSED, gate runs 2 and 3
              cancelled · four rulings recorded in ANSWERS.md Part 8
2026-08-08-A  **M3 GATE PASSED** (owner's verdict) · voice in Clarify · app renamed «Braining» ·
              the engine told to assume the ordinary case instead of asking about it
2026-08-07-Q  the interrogation was too slow — batch/single turn shape, Anthropic, suggestions duty,
              reply now appears at once, one page scroll, chat copy/edit, options instrumented
2026-08-07-P  option buttons never appeared — a contradiction in my own prompt + an ASCII-only parser
2026-08-07-O  answer buttons built · «الجواب — بالإنجليزية» had outlived its behaviour
2026-08-07-N  gate run 1 · FORGE wrote a prompt that asked for a prompt · drift now announces itself
2026-08-07-M  gate blocked by 4 defects — endless questions (the predicted risk), English, no copy, cramped
2026-08-07-L  M3 code complete · docs/M3_GATE.md written · session timer on screen
2026-08-07-K  M3 loop runs · a test that could not fail · the Gemini premise I got wrong
2026-08-07-J  execute the forged prompt — M3's loop closes · retry() learns which action
2026-08-07-I  one compile error (StringBuilder.trim → CharSequence) · what the failure already proved
2026-08-07-H  FORGE — framework library as data, English prompt, the header-eating bug written twice
2026-08-07-G  the diagnostics panel had the wrong parent
2026-08-07-F  step 2b results · a Chinese reply that was not a bug
2026-08-07-E  DiagnosticsPanel → core-ui · Clarify's system prompt becomes visible
2026-08-07-D  M3 step 2 — the interrogation runs
2026-08-07-C  M3 slice 1 — the state machine, no screen
2026-08-07-B  M3 design note SIGNED — five rulings, one overriding BRAINING.md §5
2026-08-07-A  M3 design note written, unsigned
2026-08-06-T  handoff sweep before the M3 session
2026-08-06-S  **M2 CLOSED**
2026-08-06-R  key-safety audit PASS · the font licence ships
2026-08-06-Q  Deepgram verified · a wrong key was blamed on the network
2026-08-06-P  Deepgram works · three faults, one of them my own yardstick
2026-08-06-O  the Deepgram engine — one socket, no seams
2026-08-06-N  key card verified · plain-language rule added to §0
2026-08-06-M  the Deepgram key card — and four decisions I took back
2026-08-06-L  Deepgram design note written
2026-08-06-K  engine ruled: cloud STT, BYOK · Deepgram recommended (Whisper measured worst for Arabic)
2026-08-06-J  EXTRA_SEGMENTED_SESSION ignored · the app now times itself
2026-08-06-I  the dialect test written down as an instrument
2026-08-06-H  the dialect test — words are not wrong, they are missing
2026-08-06-G  CORRECTION — my diagnosis in -B was wrong; there was no offline pack at all
2026-08-06-F  the M2 gate ran — truncation passes, 66 % accuracy is a different problem
2026-08-06-E  M2 step 3 closed · docs/M2_GATE.md written
2026-08-06-D  the accuracy fix works · and the diagnostic that lied about it
2026-08-06-C  ar-SY leads · and the device's region leads it
2026-08-06-B  the Arabic was bad because we never used the good model  ⚠ corrected by -G
2026-08-06-A  three rulings · Anthropic diagnosed · build status corrected from the tree
2026-08-04-I  "Arabic is not installed" — on a device that had it
2026-08-04-H  M2 step 3b — the voice UI, and the mark comes alive
2026-08-04-G  M2 step 3a — the speech engine, no UI
2026-08-04-F  font wired — and my own instruction was wrong
2026-08-04-E  Phase 0 items 1–2 verified on device
2026-08-04-D  the error card glared — derived error tones
2026-08-04-C  Phase 0 — BRAND applied, and dynamicColor was why it never had been
2026-08-04-B  §9 sibling dependency cleared — AiErrorMessage → core-ui
2026-08-04-A  M2 design note SIGNED — four rulings
2026-08-03-I  session handoff hygiene
2026-08-03-H  M1 closed · M2 design note
2026-08-03-G  Gemini streams — over a VPN — and is slow
2026-08-03-F  key-safety audit — PASS
2026-08-03-E  (OpenCode) A3 legible failures + Arabic/English toggle
2026-08-03-D  DeepSeek thinking mode ate the whole token budget
2026-08-03-C  the "null" bug — JsonNull.content returns "null"
2026-08-03-B  Gemini streaming desk-check + OpenCode handoff
2026-08-03-A  dead model name (deepseek-chat), model plumbing, Developer Mode placement
2026-07-29-D  Developer Mode
2026-07-29-C  localization structure — values/ is Arabic
2026-07-29-B  bidirectional text
2026-07-29-A  streaming — preparePost, never post()
pre-07-29     Compose BOM aligned → nav crash fixed · Keystore self-heal · verify() adopted
```

---

## 12. Report template — after every work unit

1. **What changed** — file by file, one line each.
2. **Why** — the diagnosis, not the edit.
3. **What the owner runs** — exact command, exact test input, one visible thing per row.
4. **What success looks like, and what a failure would mean** — so a bad result points at the next
   suspect instead of restarting the investigation.

Then confirm §7, §8 and §11 are updated.
