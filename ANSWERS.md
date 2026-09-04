# ANSWERS & DECISIONS — Braining (فهم)

Owner's rulings on your 13 questions and 12 suggestions, plus three new
requirements. Treat this as an AMENDMENT to `BRAINING.md`: where this document and
`BRAINING.md` conflict, **this document wins**. Your understanding brief was
accurate — proceed on that basis.

---

## PART 1 — ANSWERS TO YOUR 13 QUESTIONS

### 1. On-device STT engine
Use Android's built-in **`SpeechRecognizer`** as the default (zero APK size, free,
no extra model download). BUT keep it strictly behind the `SpeechToText` interface.

**Mandatory validation gate in M2:** this app's requests are LONG spoken Arabic
paragraphs, not short commands. `SpeechRecognizer` is tuned for short utterances and
may truncate or time out. Before declaring M2 done, test with a **60–90 second
continuous Arabic utterance**. If it truncates, switch the default to **Vosk**
(offline, good Arabic, ~30MB) and report the tradeoff. Do not skip this test.

> **Refined by Part 5 §M2-3 (2026-08-04).** Auto-restarting the recogniser on silence
> and stitching the segments is an **allowed mitigation**, not an automatic failure —
> but the segment count must be reported, and the gate is still judged on whether the
> transcript reaches the end of the source text. Read Part 5 before running the gate.

`whisper.cpp`-on-PC remains the optional accuracy upgrade when a PC is paired (M6).

### 2. DeepSeek
Use the official API directly (`api.deepseek.com`). No proxy.

**The app is BYOK (bring-your-own-key) — always, for every provider.** The owner has
a DeepSeek key, but it is for **development testing only**.

**CRITICAL RULE — never violate:** Do NOT hardcode, embed, bake, or commit the
owner's API key (or any key) into the app source, resources, build config, or the
APK. Keys are entered at runtime by each user and stored Keystore-encrypted. A
shared APK containing the owner's key would bill the owner for every user's usage
and the key would be extractable from the APK. If you ever need a key to test, read
it from a local `local.properties` / environment variable that is gitignored and
NEVER shipped.

### 3. GitHub Models → REPLACE with Google Gemini
**GitHub Models appears closed to new sign-ups as of June 2026** (existing accounts
migrate to Azure AI Foundry or Copilot's token-metered API). The owner is a new user,
so do not depend on it.

**Decision:** implement **Google Gemini (Google AI Studio)** as the fourth provider
instead. Rationale: it has a generous free tier with no credit card required, which
is essential because the owner will distribute this APK to friends (see Part 3) and
each friend needs their own keys. A free-tier provider makes that realistic.

Keep GitHub Models only as a clearly-labeled, non-blocking optional stub in Settings
for anyone with legacy access. Do NOT build a Copilot provider.

> **OVERRIDDEN by Part 8 §D1 (2026-08-17): the stub is REMOVED from the app entirely.** The enum
> entry, the Hilt binding, the Settings card and both locales' strings are gone. Four providers
> remain — Anthropic, OpenAI, DeepSeek, Gemini. The "do NOT build a Copilot provider" half of this
> ruling stands. Do not restore a fifth provider by citing the paragraph above; read Part 8 first.

### 4. OpenCode headless mode
**Defer to M6**, but spend 5 minutes NOW verifying (`opencode run --help` or the
equivalent in the installed version) and record the result in your build log. Do not
block M1–M5 on it.

### 5. PC bridge language
**Node.js.** It is already installed on the owner's machine and is the lowest-friction
option. Agreed with your reasoning.

### 6. Telegram integration
**No.** Android app only. This is not in the spec and is out of scope. Do not add it.

### 7. Arabic TTS readback
Android's **built-in TTS** (free, adequate Arabic). Opt-in, off by default, M5. No
cloud TTS — it adds cost and complexity for a non-core feature.

### 8. Min SDK 26
**Confirmed, acceptable.** Proceed with 26.

### 9. Testing strategy
- Unit tests: the router, all provider implementations, and the FORGE layer. Provide
  a fake `AiProvider`.
- Compose UI tests: **only for the CLARIFY screen** (the core of the product).
- Do not over-invest in UI tests for other screens during M1–M4.

### 10. Session persistence scope
- **Text is retained indefinitely** until the user deletes it.
- **Raw audio is deleted automatically after successful transcription** (default on),
  with a Settings toggle to keep it. Audio is the only real storage risk.

  > **Deferred by Part 5 §M2-2 (2026-08-04), not cancelled.** `SpeechRecognizer` never
  > hands the app an audio file, so on the M2 default engine there is no raw audio to
  > keep or delete and the toggle would be a control that does nothing. The intent above
  > stands and lands with the first engine that owns the audio stream (Vosk). Do not
  > ship the toggle before then.
- Provide: delete a single session, and "delete all history" in Settings.
- No hard size cap; surface storage used in Settings.

### 11. Package identity
**`com.braining.app`** — confirmed.

### 12. UI language
**Arabic-first with an English toggle.** Arabic is the default with full RTL. Build
proper string resources from **M1** — retrofitting localization later is painful. Never
hardcode user-facing strings.

> **Implementation note (added 2026-08-03, not an owner ruling).** This section originally
> said «`values-ar` default + `values-en`». Android has no such thing as a default
> qualified folder: the default locale is the unqualified `values/`, and a `values-ar/`
> would only apply *in addition* on Arabic devices. The intent — Arabic is the default —
> is implemented as `values/` = Arabic and `values-en/` = English overrides. See
> `PROJECT_STATE.md` §10 entry 2026-07-29-C. The wording above is corrected here so the
> next agent does not "fix" the working structure back into a broken one.

### 13. Release signing
**Generate a new keystore.** Set up a proper signing config:
- Create the keystore via Android Studio's Generate Signed APK wizard or `keytool`.
- Read credentials from a `keystore.properties` file that is **gitignored**.
- Wire `signingConfigs` in `build.gradle.kts` so `./gradlew assembleRelease` produces
  a signed APK.
- Document in `docs/SETUP.md`: the exact build command, the APK output path, and a
  prominent warning that **losing the keystore means friends must uninstall before
  they can install any future update.** Tell the owner to back it up.

---

## PART 2 — RULINGS ON YOUR 12 SUGGESTIONS

**APPROVED — implement as proposed:**
1. Minimal streaming chat screen first in M1. Yes.
3. Provider health check on startup. Yes.
4. Kotlin Serialization for all JSON. Yes.
5. **Clarify engine as a state machine** (`ANALYZING → ASKING → SUGGESTING →
   AWAITING_USER_DECISION → READY`). Strongly approved — this is the heart of the
   product and must be deterministic and testable.
6. `frameworkOverrides` on the Session. Yes.
7. REST-only bridge first, WebSocket later. Yes.
8. Sealed class for `RoutingDecision`. Yes.
9. **Developer Mode** showing the generated English prompt, raw calls, token counts,
   and latency before execution. Strongly approved — transparency is a core value of
   this project, not a debug nicety.
11. Always classify Arabic requests via an API call, never a local rule-based
    classifier. Approved — matches §5.
12. Tailscale friction: document clearly and add an in-app "What is Tailscale?"
    explainer. Approved.

**APPROVED WITH A CONDITION:**
2. `SpeechRecognizer` as default — yes, but subject to the 60–90s Arabic utterance
   test in answer #1 above. Do not treat this as settled until tested.

**DEFERRED — do not build now:**
10. "Quick voice note" feature. It adds scope before the core exists. Revisit after M5.

---

## PART 3 — THREE NEW REQUIREMENTS

### A. Distribution to friends is a first-class goal
The owner will share the signed APK with friends. Therefore:
- The APK must contain **zero** owner-specific data (already required by §4 —
  now treat it as a release blocker, and verify it before shipping the release APK).
- Onboarding must assume a **brand-new user who has never seen this app**: a guided
  first-run flow that explains what keys are needed, where to get them, and lets them
  start with a free-tier provider.
- Path A must be fully usable by a friend with **only** a free-tier Gemini key and no
  PC whatsoever.

### B. Recommend the free-tier provider during onboarding
In the setup screen, surface Google Gemini (free tier, no credit card) as the
suggested starting point, with the paid providers presented as optional upgrades.
This is what makes distribution to friends actually work.

**Amendment, 2026-08-03 — owner's ruling after Gemini returned a regional refusal.**
Gemini answers `HTTP 400 — User location is not supported for the API use.` at the
owner's location. **Gemini remains the recommended starting provider** for friends in
supported countries — Google's list covers nearly every Arabic-speaking country, so
the free-tier onboarding path still works for most recipients of this APK.

What changes:

1. The app must **say so plainly in Arabic** when a provider refuses on regional
   grounds, and point the user at another provider rather than showing a bare
   `HTTP 400`. This is a distinct error case in A3, not a generic bad-request.
2. The owner develops and tests on **DeepSeek**. Gemini cannot be the provider that
   proves streaming for M1, because the owner cannot reach it.
3. **No VPN.** `PROJECT_STATE.md` §6 stands: this project does not design around one,
   and the shipped APK must not assume one either.

### C. Key-safety audit before the release APK
Before producing any shareable APK, run and report an explicit check that no API key,
token, keystore password, Tailscale identity, or owner-specific endpoint exists
anywhere in the source, resources, build files, or the built artifact.

---

## PART 4 — PROCEED

You are approved to begin **Milestone 1**. Follow the working discipline in
`.opencode/instructions.md`:
- Write the short DESIGN NOTE for M1 first, then implement.
- Small, compilable commits. Working build at the end of the milestone.
- Surface assumptions as notes rather than guessing silently.
- Stop and ask if anything here conflicts with what you find in practice.

**M1 closed 2026-08-03.** `docs/M2_DESIGN_NOTE.md` is signed (Part 5 below) and M2 is
approved to begin.

---

## PART 5 — M2 RULINGS (2026-08-04)

These are the owner's answers to the four decisions in `docs/M2_DESIGN_NOTE.md` §7.
**The note is signed as of this ruling and is binding for M2.** All four went with the
note's recommendation; the reasoning is recorded here so a later agent can tell an
argued decision from a default.

### M2-1 · Phase 0 runs first — full scope

Execute Phase 0 (`docs/M2_DESIGN_NOTE.md` §2) **before** the recording sheet and the
waveform: the complete BRAND palette as a Compose `ColorScheme` in `core-ui` in **both
dark and light**, dark being the default; the app icon from `assets/logo/` with an
adaptive icon and a monochrome layer; and the embedded Arabic typeface
(IBM Plex Sans Arabic, weights 400 and 500 only, line height 1.7).

Why the full scope rather than the palette alone: `docs/BRAND.md` §6 defines the M2
waveform *in brand tokens*, so the palette is a hard blocker — but BRAND §3 warns that
relying on the device font breaks the layout on Xiaomi, and the test device is Xiaomi.
Deferring the font would mean discovering that on the one device that can prove M2.
This is **M1-era debt** owed since BRAND §7, not new M2 scope; it does not extend the
milestone's definition.

### M2-2 · The "retain raw audio" toggle is deferred, not cancelled

See the annotation on §10 above. Ship no setting that does nothing — a control that
promises the user authority it does not have is the same class of defect Developer Mode
exists to prevent. The toggle arrives with the first engine that owns the audio stream.

### M2-3 · Auto-restart on silence is an allowed mitigation, recorded as a shortfall

Restarting `SpeechRecognizer` on `onEndOfSpeech` and stitching `Segment`s may be used to
pass the 60–90 second gate. It is **not** counted as a clean pass:

- The **segment count of each of the three runs** is recorded in `PROJECT_STATE.md` §7.
  More than one segment means the engine restarted, and that must be visible.
- The gate still fails if any run's transcript ends before the source text does, or if
  accuracy is unusable — restart or no restart.
- The final judgement on Vosk is made **after** seeing the three runs' numbers, not
  before. Vosk is pre-approved (Part 1 §1) and needs no further decision, only figures:
  real Arabic model size, real APK delta, transcription time on the Redmi Note 13 Pro,
  and accuracy against what `SpeechRecognizer` produced.

Do not describe truncation as a platform limit. Report the exact word at which it stops.

### M2-4 · M2's UI lives in `feature-chat`, and `PROJECT_STATE.md` §9 is cleared first

The voice flow ends as text in the chat input field, so it is chat input; a new
`feature-voice` boundary drawn before M3's shape is known would be guesswork.

**Prerequisite, not a suggestion:** move `AiErrorMessage.kt` to `core-ui` with its
strings under a neutral `error_` prefix and drop
`implementation(project(":feature-chat"))` from `feature-settings` **before** any M2 code
enlarges `feature-chat`. This is the fix already described in `PROJECT_STATE.md` §9. It
is ~15 minutes now and grows with every screen that phrases an error.

---

## PART 6 — RULINGS (2026-08-06)

Three items had been sitting in `PROJECT_STATE.md` §9 waiting on the owner. All three are
now answered. They are recorded here rather than only in §9 because §9 is a queue, and a
queue is where a decision goes to be forgotten.

### M2-5 · The four derived error tones are RATIFIED

`ErrorLight` `#EC9393`, `ErrorDark` `#4A1C1C`, `ErrorPale` `#FBE4E4`, `ErrorDeep` `#631D1D`
in `core-ui/theme/Color.kt` (added `2026-08-04-D`) **stand**. They are tonal steps on
BRAND's own error hue — lightness and chroma move, hue does not — and they exist because
BRAND §2's single error hex was carrying two incompatible roles at once: legible *text* and
a full-card *fill*. One hex cannot do both.

`docs/BRAND.md` §1 forbids inventing colours and §2 lists error as one hex. This ruling does
not overturn either. It says: a tonal ramp derived from a BRAND hue is not an invention, and
the alternative — reverting — means accepting both the glare the owner reported and error
text below 4.5:1 in *both* themes.

**The rule this establishes, for whoever colours the M2 waveform next:** a contrast ratio
computed for text does not predict how the same colour reads as a large fill. Check the
role, not just the number. Five saturated bars animating against indigo900 is the same
failure waiting to happen.

### M2-6 · `darkTheme` follows the system setting — confirmed, no change

`BrainingTheme` keeps `isSystemInDarkTheme()`. BRAND §2 calls dark "the default" and light
"fully supported"; honouring the OS is how a user who chose light gets light. The
alternative — forcing dark always — was offered in `2026-08-04-C` and is declined. **No code
changes.** This is recorded so the question is not reopened by the next agent who reads that
entry's open offer.

### M2-7 · The IBM Plex Sans Arabic attribution is OWED AND ORDERED

SIL OFL requires the notice to travel with the binary, and Part 3 makes sharing the APK a
first-class goal — so this is a licence obligation, not housekeeping. The owner has ordered
it done.

**It is not done in the same build as an untested fix.** It is a new UI element in
`feature-settings`, and mixing it into the M2 step-3b rebuild would blur which change caused
which result — the isolation discipline that `PROJECT_STATE.md` §8 applies to every other
step. It ships as its own small work unit **immediately after M2 step 3b passes**, and does
**not** wait for M5's About screen.

Text: `licenses/IBM-Plex-Sans-Arabic-OFL.txt`. Placement: the foot of Settings, below the
existing cards. It must be in string resources in both locales like everything else.

### M2-8 · Recognition accuracy beats keeping the audio on the device

M2 step 3b passed on 2026-08-06 and the owner's verdict on the transcript was that the Arabic
was very bad.

**Cause: the attempt ladder led with the offline rungs.** It only advances on a *language*
error, so an installed on-device Arabic pack meant rung one always won and Google's network
model — markedly stronger for Arabic — was never reached.

That order was the agent's own reasoning in `2026-08-04-I` ("offline leads because it keeps
the audio on the device"), not the owner's ruling. **The owner had been asked a different
question**: offline-only-and-broken versus working-and-candid. Nobody asked him whether he
would accept worse Arabic in exchange for local audio. That was his to answer and it was
quietly answered for him.

**Ruling: the network model leads; the on-device model is the fallback.** The transcript is
the input to M3 Clarify, so an error here is magnified downstream — `docs/M2_DESIGN_NOTE.md`
§1 calls the editable text "not a detail" for exactly this reason.

**Two obligations follow, and neither is optional.**

1. **The permission rationale must say so.** Both locales now state that the engine uses its
   servers *first* because Arabic accuracy is higher, and falls back to the device when
   offline. This is the second time this string has been corrected for over-promising; the
   rule it keeps teaching is that the rationale describes what the code does, not what the
   author hoped it would do.
2. **A network failure must fall through to the offline rungs.** Without it, reversing the
   order would turn "dictation works with no signal" into "dictation fails with no signal" —
   a regression *introduced by the accuracy fix*, and invisible to anyone testing on Wi-Fi.

**Dialect: Levantine.** Bare `ar` now expands to Levantine region tags first. This is a guess
and it is safe to guess, because an unsupported tag comes back as a language error and the
ladder moves on. What is not safe is guessing silently — so the accepted tag and the winning
mode are surfaced in Developer Mode. If the engine settles on a Levantine tag and the Arabic
is still poor, the dialect was never the problem and Vosk's figures become the next question.

### M2-9 · `ar-SY` is the Arabic tag, first and last

Refines §M2-8, same day. The owner speaks **Syrian** Levantine and his device region is `SY`,
not `SA`. `ar-SY` leads the ladder.

**The list does not stop there, and that departs from "first and last" deliberately.** A
single-entry list is a dead microphone the moment the engine does not recognise the tag —
which is precisely the failure of `2026-08-04-I`, reintroduced by a different route. `ar-LB`,
`ar-PS`, `ar-JO`, bare `ar` and `ar-SA` remain below it. While `ar-SY` is accepted they are
unreachable and cost nothing; the Developer Mode line names the winning tag, so whether
`ar-SY` actually won is **observable rather than promised**. If it never wins, that is a fact
worth having, not a preference worth defending.

**A second thing this ruling forced out into the open.** Ranking dialects by the owner's own
speech would have made the shipped APK owner-specific, which Part 3 forbids in spirit — a
friend in Cairo should not dictate in Syrian. So the device's own region now leads the list
whenever it names an Arabic-speaking country. For the owner it resolves to `SY` and agrees
with the ruling; for everyone else it quietly does the right thing. A personal preference
belongs *behind* the user's own setting, never in front of it.

### M2-10 · Voice moves to a cloud transcription provider, BYOK

Ruled 2026-08-06, after the gate passed and the dialect tests showed why that was not enough.
`SpeechRecognizer` ends an utterance every few seconds — measured at 4.8 and 6.0 words per
segment across two runs — and every ending is a seam where spontaneous speech loses words. It
is structural: the silence hints are set, `EXTRA_SEGMENTED_SESSION` is refused by the engine,
and the restart is already as tight as the platform allows.

**The owner chose the cloud path over Vosk and over accepting the current quality.** Vosk
removes the seams but its Arabic model is MSA-trained and small, which would trade today's
deletions for tomorrow's substitutions in exactly the dialect that matters here.

**Raw audio is deleted the moment the transcript returns. No toggle, no setting.** The deferred
"retain raw audio" ruling (§M2-2) said it would arrive with the first engine that owns the audio
stream — this is that engine, and the owner's answer is that it does not arrive. This keeps the
one sentence the permission rationale has always ended with, «لا يحفظ التطبيق الصوت», literally
true, and it keeps it true for a friend who cannot read the code. A promise that survives an
architecture change is worth more than a setting.

**One consequence must not be quietly reversed later:** the app now records audio to a file and
sends it to a third party. That is a real change in what the app does with a user's voice, and
the permission rationale must say so plainly in both locales — the same string has been
corrected twice already for describing an intention rather than the code.

---

## PART 7 — M3 RULINGS (2026-08-07)

These are the owner's answers to the five decisions in `docs/M3_DESIGN_NOTE.md` §7.
**The note is signed as of this ruling and is binding for M3.** All five went with the note's
recommendation; the reasoning is recorded here so a later agent can tell an argued decision from
a default — the same reason Part 5 exists for M2.

They live here and not only in the note because `PROJECT_STATE.md` §2 makes this file the highest
authority for decisions, and a ruling recorded only inside a design note is invisible to an agent
that reads `ANSWERS.md` first, as §2 instructs it to.

### M3-1 · Clarify is a mode you enter, not the only path

An explicit control starts Clarify. **The plain chat built in M1 stays exactly as it is.**

Two reasons, and the second is the product one. Plain chat is the owner's only instrument for
testing a provider, a key or a model — an interrogation in front of every message would put five
turns between him and every diagnostic he runs. And a five-question interrogation of «مرحبا» is
the kind of friction that teaches a user to route around the feature; a feature users learn to
skip is dead however well it works.

**This narrows `BRAINING.md` §2, which reads "for every request".** The lifecycle there describes
the *full* path a request may take, not a path every keystroke must take. Recorded explicitly so
nobody later reads §2 literally and makes Clarify mandatory as a "fix".

### M3-2 · The answer comes back in English until M4 translates it

The forged prompt is English by rule (`docs/PROMPT_FRAMEWORKS.md` §5), so its answer returns in
English. **M3 does not translate it.** The screen says so in Arabic rather than leaving the user
to wonder.

Pulling a small translation step forward from M4 was offered and declined: it makes M3 usable a
little sooner and blurs the boundary between two milestones, which is the trade this project has
consistently refused. Stopping at the forged prompt without executing it was also declined — it
would make the gate in the note's §5 impossible, because two answers cannot be compared when one
of them does not exist.

### M3-3 · Clarify runs on the provider selected in the chat — this overrides `BRAINING.md` §5

`BRAINING.md` §5 names Claude the **default brain** for CLARIFY, FORGE and TRANSLATE, and adds
"this is a default, not a lock". **The ruling makes the selected provider the default and Claude
the recommendation**, which is a real change to that line and is recorded as such.

Three reasons:

1. **A friend with only a free-tier Gemini key gets a Clarify that works.** Part 3 makes that
   user a first-class goal. Pinning the core feature to a paid provider would hand him an app
   whose heart is switched off.
2. **One selector on screen, and it means what it says.** A hidden second provider choice is the
   class of defect Developer Mode exists to expose, not to create.
3. **Clarify is the first stage measured in money.** It is a full conversation ahead of every
   request, and Anthropic's promo credit expires **19 Sep 2026**. Pinning the most expensive
   stage to the account with an expiry date is a schedule problem disguised as a default.

**Claude stays the recommendation.** Onboarding may say so, and the owner may select it. What it
no longer is, is a silent override of what the screen shows.

### M3-4 · An interrupted Clarify session does not survive the process — M5 owns persistence

Nothing is written to storage in M3. If the app is killed mid-interrogation, the session is lost.

This does **not** weaken §10 above: text is still retained indefinitely once history exists, and
history is M5's job. The M1 exit checklist already records that the conversation is not persisted
and that M5 "must not treat this as already solved" — the same sentence now covers Clarify.

**The cost is stated rather than hidden:** the owner can lose an interrogation to a phone call.
That is accepted for M3 and is the first thing to revisit if a real session ever runs long enough
for the loss to hurt.

### M3-5 · The forged English prompt is a screen every user sees and can edit

Not a Developer Mode panel. The screen shows the prompt, the chosen framework, a one-line Arabic
rationale, and controls to edit, swap the framework, and regenerate.

Two documents already ask for this and neither is about debugging. Part 2 §9 approved Developer
Mode on the grounds that **transparency is a core value of this project, not a debug nicety** —
and a value that only developers can see is not a value. `docs/PROMPT_FRAMEWORKS.md` §3.7 goes
further and requires the framework be shown *and editable and swappable*, which is a sentence
about users, not about the person holding the debugger.

**What this costs, said plainly:** a whole screen, and one more step between the user and the
answer. That is the price of the product's stated position on transparency, and it was paid
knowingly.

---

**Order of work, from the note's §10 and not a preference:** the state machine alone with no
screen → Clarify → Forge → the gate in the note's §5. The first split follows the 3a/3b division
that made M2's failures unambiguous: a new Gradle module and a new Hilt binding are the parts most
likely to fail, and failing them on their own makes the cause impossible to mistake.

---

## PART 8 — RULINGS (2026-08-17)

Four rulings, given in one message after the owner tested the voice build. They are recorded here
rather than only in `PROJECT_STATE.md` §7 because §2 makes this file the highest authority for
decisions, and **§D1 overrides an earlier ruling in this same document** — an override recorded
only in a state file is invisible to an agent that reads `ANSWERS.md` first, as §2 instructs it to.

### D1 · GitHub Models is REMOVED from the app — this overrides Part 1 §3

Part 1 §3 asked for it to remain "a clearly-labeled, non-blocking optional stub in Settings for
anyone with legacy access". The owner's words: **«غير فعالة»**. It is removed: the enum entry, the
Hilt binding, the Settings card and both locales' strings. `GitHubModelsStub.kt` was moved to
`_to_delete/`.

The reason the original ruling is not merely superseded but **wrong in hindsight**: a provider
that can never answer is a control that teaches the user the list cannot be trusted. The legacy
holder it was kept for is a user this APK has never had, and Part 3 makes the *new* user the
first-class one.

**Four providers remain: Anthropic, OpenAI, DeepSeek, Gemini.** Do not restore a fifth by citing
Part 1 §3 — it is answered here.

### D2 · M3 is CLOSED, and gate runs 2 and 3 are cancelled

The owner closed the milestone on the strength of the verdict already recorded in
`PROJECT_STATE.md` §7 (two different cases succeeded; run 2 abandoned for fatigue, which produced
the batch/single turn shape rather than a failure).

**§8's standing request for two further gate runs is withdrawn, not deferred.** It is deleted from
§8 so that no later agent reads it as outstanding work. `docs/M3_GATE.md` remains as the record of
the instrument and of run 1; it is not a to-do.

### D3 · The "about me" note reaches CLARIFY and FORGE only

This is shape **أ** of the over-questioning problem in `PROJECT_STATE.md` §7 — the one that begins
"the engine over-specifies because it has no idea who it is talking to". One free-text box in
Settings, written once, injected into the two system prompts.

**Plain chat does not receive it.** Chat is the owner's only instrument for testing a provider, a
key or a model, and anything added to its request changes what it measures. Giving chat a system
prompt at all is a separate product call (it is in §9 as one) and he has not made it. Shape **ب**
— real session history — still arrives with M5 and is the permanent answer; the note is the cheap
half that works now.

### D4 · Work ships in TWO builds, not one

Build A: the recording panel and the GitHub removal. Build B: the note, the empty-credit message
and the small fixes.

The owner chose the split himself when offered the faster single build. It is the standing lesson
in `PROJECT_STATE.md` §10 entry 2 applied before the fact rather than after: five changes in one
build means a failure has five suspects, and this project has twice credited the wrong change for
an improvement.

---

## PART 9 — M4 RULINGS AND A CHANGE OF WORKING METHOD (2026-08-17)

Given the same day as Part 8, after the owner said the build was moving too slowly and proposed
writing M4, M5 and M6 in one pass. §E1 is the method; §E2–§E5 are M4's design.

### E1 · Work ships **one milestone per batch**, with automated tests — not three milestones blind

The owner's proposal was all three at once. What was agreed instead: **M4 whole, in one batch**,
then M5, then M6 — and the acceleration comes from somewhere else.

The diagnosis, recorded because it is the reason the method changed:

- **The agent cannot compile or run anything.** Every syntax error costs a full round trip through
  the owner. Enlarging the batch multiplies those round trips rather than removing them.
- **So the lever is not batch size — it is removing the human from checks that never needed one.**
  `ANSWERS.md` Part 1 §9 ordered unit tests in July and none were ever written. M4 shipped with 23
  of them, in `:core-domain`, which already declared a JUnit dependency — so the project's most
  fragile asset, its build, was not touched at all.
- **M5 is not independent of M4.** Session history replaces the "about me" note and its shape is
  decided by what M4's execution exchange carries. Writing it first is writing it twice.
- **M6 is a different machine** — a Node server on Windows, Tailscale, OpenCode headless. None of
  it can be exercised from the phone, and it needs setup work from the owner rather than code.

**The one-change-per-build rule is narrowed, not repealed.** §10 entry 2 is about *attributing* an
improvement to a cause. It applies to two changes touching the same behaviour; it does not mean a
milestone must arrive in eight installments. Independent work ships together.

**Automated tests are now part of "done".** A milestone whose logic could have been unit-tested and
was not is not finished. The rule has a hard edge so it cannot decay: **the tests live in
`:core-domain`**, whose build file already carries JUnit, and logic worth testing is moved there
rather than tested where it happens to sit.

### E2 · The router ships **without** a classification call

`BRAINING.md` §3 requires classifying every request as Path A or Path B, and Part 2 §11 requires
that classification to be an API call rather than a keyword rule. **Both stand — for M6.**

Until the PC bridge exists, that call would spend a network round trip and the user's money on
every single request to answer a question with exactly one possible answer, and would place a
branch nobody can test in the middle of the product's hot path.

What ships is the half that is real now: **the decision is visible and it is overridable.**
`RoutingDecision.NeedsPc` exists unused so that M6 adds a branch to a type that already has two,
instead of widening a type every caller has assumed is single-valued.

**The AI-router toggle (`BRAINING.md` §5) goes with it**, for the plain reason that it toggles
between two things that do not exist yet.

### E3 · Translation is **offered, not performed** — this supersedes Part 7 §M3-2

§M3-2 ruled that the answer returns in English until M4 translates it. That stopped being true on
2026-08-07, when the forged prompt began requiring an Arabic reply. A mandatory translate step
would now spend a second call and several seconds on nearly every answer to change nothing.

**The app detects instead.** `ScriptDetector` measures Arabic letters as a share of all letters; a
long answer below the threshold gets a «ترجم إلى العربية» button. The original is never destroyed
and the button flips back to it.

The failure this guards against is the false offer — a button proposing to translate text that is
already Arabic makes the app look as if it cannot read its own output. The threshold is low for
that reason, and both directions are pinned by `ScriptDetectorTest`.

### E4 · Feedback **continues** the answer; it does not re-forge the prompt

"Shorten it" is sent with the forged prompt and the answer already in the conversation, so the
model is adjusting its own work. Re-forging the English prompt from the note and executing again
was offered and declined: it is more accurate for a change of direction, and it throws away the
answer the user asked to *adjust*.

This is what `BRAINING.md` §2.7's "full session context" means concretely, and it is the reason
the exchange is kept as a list rather than a pair of strings.

### E5 · Fallback is **automatic and announced** — and it is not silent, and not in chat

On a provider-side failure the app tries the next provider the user holds a key for and writes a
line naming both: «تعذّر Google Gemini، فأجاب Claude». Silent fallback was declined — an answer
from a model the user did not choose, with nothing on screen saying so, is precisely the failure
Developer Mode exists to surface.

**It does not apply to a setup failure.** A missing key, a rejected key, a forbidden action and a
dead network are facts about the user's configuration; routing around them spends a second key to
hide a problem the user has to fix anyway, and they would never learn the first key was wrong.

**And it does not apply to plain chat.** Chat is the owner's instrument for testing a provider, a
key or a model (Part 7 §M3-1). An instrument that switches provider when the one under test fails
is measuring the wrong thing.

---

## PART 10 — THE «مِداد» IDENTITY (2026-08-18)

The owner asked for a complete visual redesign: the dark theme's deep violet did not please him,
the buttons had no modern interaction, and he wanted the whole thing calm and coherent. Three
directions were built as a live preview; he adopted **مِداد**. His verdict on seeing it:
«الآن صار التصميم أجمل فعلاً».

### F1 · The ground goes neutral and the colour moves to what is touched

`#26215C` as a full-screen ground is replaced by `#0E0D14` — near-black with a faint violet bias
— and `#8B84F7` becomes the interactive colour. **This overrides `docs/BRAND.md` §2 as it stood.**

The reasoning is recorded because it generalises: the eye reads chroma multiplied by area. A
saturated colour spread over a whole screen exhausts attention and leaves the accent nothing to
win against, which is why the owner reported both "the violet is heavy" and "nothing looks
interactive" — those were one problem, not two.

### F2 · Corner radius rises, and one shadow is permitted

16dp buttons, 20dp cards, 28dp sheets — overriding BRAND §5's "8dp controls, 12dp cards". And one
named exception to "no drop shadows": a soft shadow under the recording panel, so its height off
the page is legible while the conversation scrolls behind it. **No gradients, anywhere, still.**

### F3 · A press is answered physically

Every button shrinks to 96% on a spring while held, in addition to Material's ripple. On a dark
ground the ripple alone is almost invisible; the scale is what separates a control from a picture
of one. Four button weights are defined once and used everywhere — `PrimaryButton`, `TonalButton`,
`QuietButton`, `InsightButton` — and the amber one is reserved for «نضجت الفكرة» alone.

### F4 · The mark's colours follow the theme; its form does not

BRAND §1's "a logo does not re-tint itself" held while there was one saturated ground. With a
near-black dark theme and a white light theme, a single fixed violet glares on one and washes out
on the other. **The form is fixed and must not be redrawn; the two hues follow `primary` and
`tertiary`.**

### F5 · REJECTED — the dot falling onto the centre bar

The owner asked for it, it was built as a preview, and he rejected it on sight: «قم بإلغاء الفكرة
لم تعجبني». Nothing had been written to the repository for it.

**Recorded so it is not revived.** A later agent reading BRAND §1 — "the dot is the moment of
insight" — will find a landing animation an obvious idea. It was tried. The answer was no.

---

## PART 11 — M5 RULINGS (2026-08-28)

**All five questions of `PROJECT_STATE.md` §8 were answered in one reply: «موافق على ما قمت
بتوصيته».** The owner adopted every recommendation as written. They are restated here as rulings
because a recommendation accepted is a ruling, and the next agent must not re-open them.

### K1 · History remembers everything; CLARIFY reads a summary

Every finished session is saved, searchable, with no marking step by the user. **CLARIFY does not
read past sessions in full — it reads a short summary of the last few.** Two reasons, and both are
binding on the implementation: full text of many sessions would cost a fortune per turn, and it
would bury the current question under noise. The summary is pure logic and therefore belongs in
`:core-domain` where it can be unit-tested (§0 rule 11).

### K2 · The «نبذة عنك» note stays

History does not retire it. **The note is the only context that exists on a fresh install**, which
is precisely the state every friend receiving the APK starts in. `PROJECT_STATE.md` §8 item 4 said
history "replaces the note's job" — **that phrasing is now superseded**: history replaces the note
as the *primary* source of context, and the note remains the fallback when there is no history.
The three prohibitions of Part 8 (do not quote it, do not assume every idea belongs to it, do not
steer toward it) still apply, and now apply to the history summary too — the same failure mode
(§10 entry 32) is available to any background fact handed to a model.

### K3 · TTS ships in M5, off by default

Opt-in, a switch in Settings, silent until turned on. It is the last piece of the lifecycle in
`BRAINING.md` §2 and leaving it out would leave M5 one piece short of complete.

### K4 · The release APK ships with the first-run flow

Not a sixth milestone. `ANSWERS.md` Part 3 makes the new user a first-class goal, and **an APK a
friend cannot set up is not a distributable product — it is a file.** The flow must work for
someone with no keys, and must name where to get a free tier.

### K5 · History is on-device only. No sync, no server

No ruling anywhere asks for one. Adding a server changes what the app is: it would make the owner
the custodian of other people's data and it breaks the BYOK premise the whole product rests on
(constraint 3). **This is a closed question — do not re-open it as an "improvement".**

### The «مِداد» identity passed on the phone, 2026-08-28

The owner ran `:core-domain:test` and `installDebug`, and reported the app appeared as described.
**`docs/TESTS_PENDING.md` §٤ is closed on that verdict** and the whole file is now a record, not a
queue. The unbuilt edit named at the top of §8 — the forge control row split in two — **compiled
and shipped in that build**; the tree carries no uncompiled change.

**One caveat recorded honestly:** the owner confirmed the overall appearance, not fifteen rows
one at a time. That is a weaker signal than a row-by-row pass, and §10 entry 37 is exactly about
this shape — an invisible or unexamined check marked passed by association with visible ones. The
rows most likely to be wrong without being noticed are ٤.٨ (the «امسح» label rendering
horizontally — the row that proves the unbuilt edit), ٤.١١ (a disabled button must not shrink),
and ٤.١٤ (one shadow in the whole app). **They are carried into the M5 test file as three rows
rather than being called passed here.**

---

## PART 12 — RULINGS OF 2026-08-28, AFTER THE FIRST M5 TEST ROUND

The owner tested M5 on his phone and answered four questions. §2, §3, §5 and §9 of
`docs/TESTS_PENDING.md` passed; §4, §6, §7 and §8 produced the work below.

### L1 · Two batches, not one

Fixes first, providers second, **and the PC bridge stays M6.** His request contained a whole
milestone in one clause — "أتحكّم بالوكيل بالحاسب من خلال التطبيق" — sitting beside a request to
add a provider. They are not the same feature: one asks a model a question over the network, the
other lets a phone command an agent to edit files on a computer. Splitting them is §0 rule 10
applied to a request rather than to a plan.

### L2 · The app is «فهم» in Arabic and «Braining» in English. «مِداد» is not shown to the user

Three names had accumulated: `Braining` in the code, «فهم» in the spec, «مِداد» as the identity
adopted on 18 August. He ruled the first pair as the product's name and kept «مِداد» for
`docs/BRAND.md` alone.

**His message said the welcome screen should greet with «مِداد»; his answer to the question said
«مِداد» is a design name and does not appear to the user.** The contradiction was put to him
explicitly in the question text and he chose the latter, so the greeting reads the app's own name
from `core-ui`'s `app_name` and follows the device's language. Recorded because it reverses a
sentence he wrote an hour earlier, and a later reader would otherwise take the sentence for the
ruling.

### L3 · A locally-run model is a **provider**; commanding an agent is **M6**

Both were adopted, in that order. Ollama over the network is a fifth provider and nothing more —
it answers questions and never touches a file. The agent bridge keeps its design, its Tailscale
tunnel and its three guardrails, and it keeps its place in the plan.

**«OX-Alpha» is not a provider.** It is a model served through OpenRouter, so adding OpenRouter
supplies it and several hundred others under one key. One integration, not three.

### L4 · Manual fallback, with a one-tap escape — **this reverses Part 9's automatic fallback**

On 17 August he ruled that a provider-side failure should fall back automatically and announce
itself in red. On 28 August he reversed it: the app now names what failed, lists the providers he
holds keys for, and **waits**. A «جرّب أي واحد» button takes the router's own first choice for
whoever does not care.

The reasoning behind the original ruling is untouched — a silent substitution is the failure
Developer Mode exists to prevent — and the visible «تعذّر كذا، فأجاب كذا» line survives. What
changed is that announcing a purchase after it has been made is not the same as being asked
before it.

**The router still decides *whether* a fallback is appropriate at all.** A missing key, a rejected
key and a dead network are facts about the user's own setup, and `DefaultModelRouter.isRecoverable`
refuses to route around them; the list is then empty and only «أعد المحاولة» is offered. That is
not a gap — it is the ruling of 17 August that still stands.

### L5 · The session's name is written by the model, on a call that already happens

He asked for the history list to be titled by the model rather than by the first sixty characters
of the transcript. **A dedicated call was rejected** as one round trip and one bill per session
for a line read for half a second. The title rides on the forge request instead, as a
`[[العنوان]]` marker — zero extra calls, zero extra latency.

### L6 · The readable request body sits **above** the raw one, never in place of it

He asked for Developer Mode's request body to be organised, summarised and right-to-left. It now
is. **The raw JSON is still there, one tap below.** That panel has found three real faults in this
project precisely because it shows the bytes rather than a description of them; a missing
character survives a prettified view and dies in the raw. §10 entry 6 is the reason this is stated
as a ruling rather than left to taste.

---

## Part 13 · قرارات ٣٠ آب ٢٠٢٦ — الرابط وأزرار النسخ ومنظّف المفتاح

### M1 · زر المشاركة يشارك **رابط GitHub**، ولا يشارك ملف APK

طلب المالك أن يشارك الزر رابط تنزيل التطبيق من GitHub. الرابط هو
`share_download_url` في `feature-settings`، وقيمته الحالية عنوان فيه `USER/REPO`.

**ما دام `USER/REPO` موجوداً في النص، الزر لا يظهر أصلاً**، وتظهر مكانه جملة تقول ما يجب
تبديله. السبب قاعدة واحدة: **زر يشارك رابطاً مكسوراً أسوأ من زر غير موجود** — من يضغطه يرسل
الرابط إلى صديق، والصديق يرى صفحة 404، ولا أحد يعرف أين حدث الخطأ.

ولا يشارك الزر ملف الـ APK نفسه. رابط `releases/latest` يعطي **آخر إصدار دائماً**؛ ملف مُرسل
عبر واتساب يبقى عند صاحبه إلى الأبد بنسخته القديمة، ولا توجد طريقة لسحبه.

### M2 · زر النسخ على الخطأ **خارج وضع المطوّر**، والنص الخام داخله

طلب المالك زر نسخ لأي رسالة خطأ من أي مزوّد. الزر ظاهر دائماً لكل مستخدم.

هذا ليس تساهلاً في الخصوصية بل نتيجة خطة التوزيع: **الصديق الذي يقع عليه الخطأ لا يستطيع قراءة
رسالة إنجليزية ولا تفعيل وضع المطوّر ولا إعادة كتابة الجملة بدقة. يستطيع الضغط على «نسخ»
واللصق.** والنسخة الواحدة تحمل الجملتين معاً — العربية التي يقرأها والإنجليزية التي تشخّص العطل.

**أمّا عرض** النص الخام على الشاشة فيبقى داخل وضع المطوّر: هو إنجليزي غير منسّق كتبه المزوّد
لمهندسيه. والنص مُنقّى من المفتاح في طبقة المزوّد قبل أن يصل إلى الواجهة — القيد الصلب ٣ يجعل
تسريب المفتاح مانعاً للإصدار لا مسألة ذوق.

### M3 · المفتاح يُصلَح عند اللصق، ويُقال للمستخدم ما تغيّر

سبب رفض Gemini للمفتاح لم يكن الحصة اليومية ولا اسم النموذج ولا المنطقة: كان **حرفاً واحداً**.
شرطة `-` تحوّلت إلى `—` أثناء النسخ. `ApiKeySanitizer` صار يقطع الفراغات، ويحذف الأحرف غير
المرئية، ويستبدل الأشباه (سبع شرطات، الاقتباسات المنحنية، الأرقام العربية-الهندية).

وقاعدتان تحكمانه:

1. **لا يُصلَح إلا ما له جواب واحد ممكن.** أي حرف آخر غير ASCII **يُبلَّغ عنه ويُترك كما هو** —
   حذف حرف من داخل بيانات اعتماد لجعلها تبدو صحيحة يحوّل عطلاً مقروءاً إلى عطل غامض.
2. **يُقال ما تغيّر.** مفتاح يُعدَّل بصمت هو مفتاح لا يستطيع صاحبه التفكير فيه حين يفشل مرة
   أخرى. والسبب الأهم لأصدقاء المالك: **الضرر سيتكرّر** — من قيل له «الشرطة في مفتاحك كانت
   خاطئة» تعلّم شيئاً عن نسخ المفاتيح؛ ومن لم يُقل له شيء سيلصق المفتاح المكسور نفسه في التطبيق
   التالي.

**وهذا ليس تحقّقاً من صحة المفتاح.** المنظّف يثبت أن المفتاح *سليم الشكل* فقط. لا يجوز أن تُقرأ
رسالة الإصلاح على أنها «المفتاح يعمل».

### M4 · رقم الإصدار ظاهر في الإعدادات

`versionName` معروض تحت زر المشاركة. السبب عملي بحت: حين يقول صديق «التطبيق لا يعمل»، السؤال
الأول هو أي نسخة يحمل، ولا توجد طريقة أخرى ليقرأها من داخل التطبيق.

---

## Part 14 · قرارات ٣١ آب ٢٠٢٦ — دليل المفاتيح

### N1 · الدليل مطويّ افتراضياً

طلب المالك روابط تشرح كيفية جلب مفتاح كل مزوّد ومتطلّبات تهيئته. الدليل تحت حقل المفتاح في كل
بطاقة، **مطويّاً** خلف سطر واحد: «كيف أحصل على المفتاح؟».

أربع لوحات مفتوحة تدفن الإعدادات التي عُلّقت عليها، **ومن يحتاج الشرح يقرأه مرّة واحدة**. فالتكلفة
سطر واحد حتى يُطلب.

### N2 · لا أسعار ولا أرقام حصص داخل التطبيق — أبداً

الدليل يقول: أين المفتاح، وماذا يحتاج الحساب أوّلاً، وبماذا يبدأ المفتاح، وهل المزوّد يرفض بعض
الدول. **ولا يقول سعراً ولا حدّ حصّة ولا اسم نموذج.**

السبب ليس الكسل: **رقم مطبوع داخل نسخة ثبّتها صديق في آب وفتحها في آذار ليس «قديماً» بل خاطئاً،
وخاطئ في الاتجاه الذي يكلّف مالاً.** كل ما يحمل رقماً يُترك لصفحة المزوّد نفسه، وهي محدَّثة
بحكم كونها صفحته. زرّ «اقرأ الشروط» هو هذا الباب.

### N3 · القيد الجغرافي يُقال قبل المفتاح لا بعده

Gemini وحده يعطي مفتاحاً عاملاً بلا بطاقة ائتمان، **وهو وحده يرفض دولاً بأكملها**. الحقيقتان
تتجاذبان، فتُقالان معاً — وقبل أن يقضي المستخدم عشر دقائق في إنشاء حساب Google ليكتشف الثانية.
هذا تطبيق للتعديل ٣ آب على Part 3 §B، وتقديمه على الفشل أرخص من انتظاره.

### N4 · رسالة الشكل ليست تحقّقاً — ولا يوجد رفض تلقائي

الدليل يعرض بماذا يبدأ المفتاح (`sk-ant-` مثلاً) **ولا يرفض المفتاح إن لم يبدأ به**. مزوّد يضيف
صيغة جديدة غداً سيجعل التطبيق يرفض مفتاحاً صحيحاً — وهذا فشل أسوأ من الفشل الذي يمنعه. العرض
يكفي ليلاحظ المستخدم أنه لصق مفتاح مزوّد في بطاقة مزوّد آخر.

---

## Part 15 · قرارات ٣١ آب ٢٠٢٦ — Ollama

### O1 · العنوان يُكتب يدوياً، ومعه زرّ فحص

اختار المالك الكتابة اليدوية على البحث التلقائي في الشبكة. البحث التلقائي يبدو سحرياً لكنه بطيء،
وكثير من الشبكات تمنعه، **وحين يفشل لا يعرف المستخدم لماذا** — ويبقى محتاجاً للحقل اليدوي على أي
حال. حقلٌ وزرُّ فحص يعطيان جواباً مؤكّداً في ثانيتين.

### O2 · النموذج يُختار من قائمة يجلبها التطبيق من الحاسوب

كتابة اسم نموذج لم يُنزَّل تُنتج فشلاً يبدو كعطل في التطبيق. القائمة تأتي من `/v1/models` على
حاسوب المستخدم، فكل اسم فيها موجود بالضرورة. **وهذا هو المكان الوحيد الذي يختار فيه التطبيق
نيابةً عن المستخدم**: إن كان الاسم المحفوظ غير موجود على الجهاز، يُستبدل بأوّل اسم في القائمة —
لأن اسماً لا يعمل ليس اختياراً يستحقّ الحفاظ عليه.

### O3 · يُعرض كبديل **فقط إن استجاب**

Ollama هو المزوّد الوحيد الذي له حالة ثالثة: ليس «يعمل» ولا «فشل» بل **نائم**. لذا قبل عرضه
كبديل يُسأل الجهاز سؤالاً سريعاً بمهلة ١٫٢ ثانية.

ولماذا مهلة قصيرة بهذا الشكل: هذا الفحص يجري **بينما ينظر المستخدم إلى بطاقة خطأ** ينتظر أن
يُقال له من يستطيع الإجابة. حاسوب مستيقظ يردّ في أجزاء من الثانية على شبكة منزليّة؛ وأيّ شيء
أطول هو حاسوب نائم على الأرجح. إنفاق خمس ثوانٍ للتأكّد يجعل فشل **كلّ المزوّدين الآخرين** أبطأ.

**وقائمة فارغة من النماذج ليست جاهزيّة**: جهاز يعمل عليه Ollama بلا أي نموذج منزَّل لا يُعرض،
لأنه سيستبدل فشلاً يفهمه المستخدم بفشل لا يفهمه.

### O4 · البطاقة ظاهرة للجميع

أصدقاء المالك لا يستطيعون الوصول إلى حاسوبه، وأكثرهم لن يستعملوا هذه الميزة. ومع ذلك البطاقة
ظاهرة، وتحتها سطر يقول بوضوح ما تحتاجه. **ميزة مخفيّة حتى تُضبط هي ميزة لا يكتشفها أحد**؛ السطر
يكلّف القارئ ثانيتين، والإخفاء يكلّفه الميزة كلّها.

### O5 · النصّ غير المشفّر: الإذن في المانيفست، والقاعدة في الكود

Ollama يتكلّم `http://` عادياً، وأندرويد يمنع ذلك افتراضياً. وملفّ إعدادات الشبكة في أندرويد
**لا يستطيع** التعبير عن «العناوين المحليّة فقط» — فهو يطابق أسماء نطاقات، وعنوان حاسوبك غير
معروف وقت البناء أصلاً.

فالإذن في المانيفست واسع، **والقاعدة الحقيقيّة في `LocalEndpoint`**: وهو الشيء الوحيد الذي
يستطيع إنتاج عنوان لـ Ollama، ويرفض أي عنوان ليس على شبكة محليّة، وهو كود خالص مختبَر بتسعة
اختبارات للرفض وحده.

**وهذا التزام أقوى مما كان الملفّ سيقدّمه، لا أضعف.** كما أنه **لا يُضعف المزوّدين الأربعة
الآخرين**: عناوينهم ثوابت `https://` مكتوبة داخل التطبيق، والسماح بالكلام الصريح ليس أمراً
بالتنازل عن التشفير.

**ونطاق `100.64/10` مستثنى عمداً.** أُضيف أوّلاً لأن Tailscale يوزّع منه وM6 سيحتاجه، ثم أُزيل
في المراجعة: هذا النطاق هو أيضاً ما توزّعه **شركات الاتصالات**، فعلى شبكة الجوّال تكون تلك
العناوين أجهزة مشتركين آخرين. رقم مكتوب خطأً كان سيرسل فكرتك بلا تشفير إلى هاتف شخص غريب. يعود
مع M6، مربوطاً بالجسر الذي يحتاجه.

---

## Part 16 · قرارات ٤ أيلول ٢٠٢٦ — الوصول من خارج البيت

### P1 · لا فتح منافذ في الراوتر. أبداً.

**Ollama بلا أي مصادقة**: لا مفتاح، ولا كلمة سرّ، ولا حدّ استعمال. من يصل إليه يستطيع تشغيل
معالجك، وقراءة أسئلتك، وحقن ما يشاء في أجوبتك.

فتح المنفذ ١١٤٣٤ في الراوتر يضع ذلك أمام الإنترنت كلّه. **هذا ليس خياراً يُوازَن، بل باب مغلق**؛
ولا يُذكر في أي وثيقة كطريقة ممكنة، لأن ذكره وحده يجعله مطروحاً.

### P2 · النفق يُفتح باسم، لا برقم

Tailscale يعطي كل جهاز شيئين: **عنواناً** في نطاق `100.64/10`، و**اسماً** ينتهي بـ `.ts.net`.

اخترنا الاسم أساساً، والرقم بديلاً — والسبب هو الدرس ٥٢: نطاق `100.64/10` هو أيضاً ما توزّعه
شركات الاتصالات، فعلى شبكة الجوّال تكون تلك العناوين أجهزة مشتركين آخرين. **الرقم لا يستطيع أن
يقول أيّهما هو.** أمّا `.ts.net` فنطاق Tailscale وحدهم، ولا يُوصل إلا إلى جهاز على شبكتك أنت.

والقاعدة العامّة المستخلَصة: **حين يجب توسيع صلاحية، وسّعها بالشكل الذي لا يحتمل التأويل، لا
بالشكل المألوف.**

### P3 · المفتاح تصريح يقوله المالك، لا تفضيل

مطفأً: الشبكة المحليّة وحدها — وهي الحالة الوحيدة التي يستطيع التطبيق التحقّق من أمانها بنفسه.

مشغّلاً: **المستخدم يصرّح بوجود نفق مشفّر.** وما يجعل الاتصال آمناً ليس العنوان بل النفق —
Tailscale يشفّر كل شيء بـ WireGuard مهما أرسل التطبيق.

لهذا صيغة المفتاح جملة خبريّة — «أصل إلى حاسوبي عبر Tailscale» — لا أمراً ولا خياراً. والفرق
ليس لغوياً: خيارٌ يُضغط بلا تفكير، وتصريحٌ يُقرأ.

---

## Part 17 · قرارات ٤ أيلول ٢٠٢٦ — OpenRouter

### Q1 · «تحقّق» هنا لا يرسل طلباً حقيقياً — بخلاف المزوّدين الأربعة

عند كل مزوّد آخر، زرّ «تحقّق» يرسل رسالة قصيرة فعليّة. وهذا مقصود هناك: التحقّق بنموذج **مختلف**
عن الذي تستعمله يعطي علامة خضراء بينما تفشل المحادثة.

أمّا هنا فالعكس هو الصحيح، لثلاثة أسباب:

1. الطلب الحقيقي **يكلّف مالاً** على نموذج قد لا يكون المستخدم اختاره بعد.
2. السؤال الوحيد الذي يمكن للمفتاح أن يخطئ فيه — هل هو صالح؟ — تجيبه أي نقطة تحتاج مصادقة،
   وهذه لا تكلّف شيئاً.
3. **النموذج لا يكون مُختاراً وقت التحقّق أصلاً.** يُختار بعده، من القائمة التي أعادها نفس
   الطلب. فالتحقّق من نموذج لم يُختَر بعد علامة خضراء عن قرار لم يُتّخذ.

فالعلامة الخضراء هنا تعني «المفتاح يعمل»، وهو الادّعاء الوحيد الصادق في هذا الموضع.

### Q2 · قائمة النماذج تُجلَب ولا تُكتب

معرّفات OpenRouter تحمل بادئة المزوّد — `anthropic/claude-sonnet-4`، `qwen/qwen3-8b` — وعددها
بالمئات. كتابتها من الذاكرة تفشل بخطأ ٤٠٤ يبدو كعطل في التطبيق، ولا يملك المستخدم طريقاً
لاكتشاف الإملاء الصحيح.

فالتطبيق يجلب القائمة ويبحث فيها. **والبحث يطابق المعرّف والاسم معاً** — «claude» و«Anthropic»
يجدان نفس الصفوف، لأن المستخدم يعرف أحدهما عادةً لا كليهما، وأيّهما يعرف ليس شيئاً تستطيع
الشاشة توقّعه.

**والنماذج المجانيّة تُعلَّم وتُرتَّب أوّلاً.** لمن يقرّر إن كان سيجرّب التطبيق أصلاً، «لا يكلّف
شيئاً» هي الخاصّية الوحيدة التي تغيّر القرار.

### Q3 · التحليل في `:core-domain` لا في المزوّد

الاتصال بالشبكة لا يُختبَر بلا شبكة؛ **أمّا تحويل الجواب إلى صفوف فيُختبَر**، وهو النصف الذي
تقع فيه الأخطاء: حقل يُعاد تسميته، سعر يصل «0.0» بدل «0»، نموذج بلا اسم.

ووحدة `:ai-providers` لا تملك اختبارات إطلاقاً، بينما `:core-domain` تملكها. فبقي المقبس في
المزوّد، وانتقل التحليل إلى حيث يمكن فحصه — نفس الفصل الذي حظي به `DefaultModelRouter`
(Part 1 §9): **القرارات تذهب حيث يمكن التحقّق منها.**

وأهمّ اختبار فيها: السعر يُقرأ **رقماً** لا نصّاً. لو قارنّاه بالنصّ `"0"` حرفياً، فيوم يبدأ
مزوّد بإرسال `"0.0"` ستنقلب كل النماذج المجانيّة إلى مدفوعة بصمت — ولا شيء على الشاشة يقول
إن شيئاً تغيّر.

---

## Part 18 · قرار ٥ أيلول ٢٠٢٦ — الصمت ليس طريقة محايدة للتعبير عن حكم

### R1 · أزرار المزوّدين تظهر دائماً، والحكم يُقال بدل أن يُخفى

بُنيت أزرار «اختر مزوّداً يجيب بدلاً عنه» في الدردشة يوم ٣٠ آب. **ولم يرها المالك ولا مرّة
واحدة** حتى ٤ أيلول، فأبلغ أنها غير موجودة.

ولم تكن غائبة. كانت تظهر فقط حين يرى الموجّه أن التبديل سيفيد — ويرفض ذلك عند مفتاح ناقص أو
مرفوض أو شبكة ميتة أو عطل غير مصنّف. وتلك بالضبط الأعطال التي صادفها.

**والدرس:** زرٌّ يظهر بشروط لا يستطيع المستخدم توقّعها هو، من جهته، **زرٌّ غير موجود**. جلستان
من العمل، ولم يره صاحب التطبيق مرّة.

**والإصلاح ليس إضعاف القاعدة بل التوقّف عن إخفاء الزرّ.** الأزرار تظهر الآن كلّما وُجد مزوّد
آخر يمكن سؤاله، وحين لا يرشّح الموجّه التبديل **تقول البطاقة ذلك بجملة صريحة**: «هذا العطل في
إعدادك، والتبديل غالباً لن يفيد — لكن جرّب إن شئت».

وهذا يحفظ سبب قرار ٢٨ آب كاملاً (لا نخفي ما على المستخدم إصلاحه) ويسقط الجزء الذي أخطأ: **إخفاء
الأزرار كان هو نفسه إخفاءً للسبب.** الصمت لا يقول «هذا لن يفيدك»؛ الصمت يقول «التطبيق معطوب».

### R2 · «جرّب أي واحد» يبقى مربوطاً بترشيح الموجّه

الفرق بين الزرّين فرق في من يقرّر:

- **الأزرار** = المستخدم يختار. تظهر دائماً.
- **«جرّب أي واحد»** = التطبيق يختار. ولا يختار التطبيق إلا حيث لديه سبب يجعله يظنّ أن الاختيار
  سينفع.

فحيث يرفض الموجّه، تبقى الأزرار ويختفي «جرّب أي واحد» — وهذا هو المعنى بالضبط.

### R3 · قائمة الاختيار اليدوي تتجاهل «ما جُرِّب من قبل»

القائمة المرشَّحة تستبعد كل مزوّد فشل في هذه الدورة، منعاً للدوران بين اثنين.

أمّا القائمة اليدويّة فتستبعد **الفاشل الآن فقط**. من رأى مزوّدَين يفشلان قد يريد إعادة السؤال
على الأوّل — وهذا قراره هو، لا قرار الموجّه.

---

## Part 19 · تصحيح ٥ أيلول ٢٠٢٦ — قائمة صحيحة خير من تحذير دقيق

### S1 · حُذفت جملة «التبديل غالباً لن يفيد»

كتبتُها فوق أزرار المزوّدين كلّما رفض الموجّه ترشيح التبديل. **وأثبت المالك خطأها من أوّل
محاولة**: لم يكن لديه مفتاح ChatGPT، فضغط OpenRouter، فأجاب.

وطبعاً أجاب. **مفتاح ناقص أو مرفوض أو ممنوع هي أعطال في إعداد مزوّد واحد، والانتقال إلى مزوّد
آخر هو الدواء بعينه.** الجملة كانت صحيحة في حالة واحدة فقط — انقطاع الشبكة عن الهاتف — وعمّمتُها
على خمس.

### S2 · وحيث كان التحذير صحيحاً، ضاقت القائمة بدل أن يُهمَس التحذير

عند انقطاع الشبكة عن الهاتف، كل مزوّد بعيد يُوصَل عبر نفس الشبكة الميتة — فالتبديل عبثٌ فعلاً.

فبدل جملة تحذير، **تضيق القائمة نفسها**: تُعرض المزوّدات التي لا تحتاج إنترنت — أي Ollama على
حاسوبك. وهذا **جواب** لا اعتذار: «لا إنترنت، لكن حاسوبك هنا».

**والشكل الذي يستحقّ التذكّر: قائمة صحيحة خير من تحذير دقيق، وكلاهما خير من تحذير معقول
الظاهر.**

### S3 · اسم التطبيق خرج من شريط الدردشة

المحاولة الأولى نقلت اسم المزوّد إلى مكان العنوان وأبقت اسم التطبيق بجانبه. فتوقّف الانكسار
إلى حرفين... **وصار القطع عند ستّة أحرف** — لأن المساحة ما زالت مقسومة بين اسم ثابت وعنصر واحد
هو الذي يحتاجها.

**استبدال الالتفاف بنقاط الحذف ليس إصلاحاً؛ هو نفس الضيق بعلامة ترقيم مختلفة.**

فخرج الاسم. المستخدم **داخل** التطبيق: اسمه على شاشة هاتفه، وفي الإعدادات، وفي بطاقة المشاركة —
ولا واحد منها مكان يحاول فيه أن يقرأ أيّ عقل يجيبه.
