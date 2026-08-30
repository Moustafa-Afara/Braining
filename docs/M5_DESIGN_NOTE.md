# M5 DESIGN NOTE — history, polish, and the first shareable APK

**Written 2026-08-28, after the owner answered the five questions of `PROJECT_STATE.md` §8 in one
reply. His rulings are `ANSWERS.md` Part 11 and are treated here as settled.**

This note exists because §0 rules 10–12 require a milestone to be designed once, built as one
batch, and handed over as one test file. It records the decisions that are *not* in his five
answers — the ones an agent would otherwise take silently — so that a later reader can tell a
deliberate choice from an accident.

---

## 1. What the owner ruled, restated in one table

| | Ruling | Consequence in this note |
|---|---|---|
| K1 | Every finished session saved, searchable · CLARIFY reads a **short summary** of the last few | §3, §4 |
| K2 | The «نبذة عنك» note **stays** alongside history | §4.3 |
| K3 | TTS ships now, **off by default** | §7 |
| K4 | The APK ships **with** the first-run flow | §6 |
| K5 | **On-device only**, no sync | §2 |

---

## 2. What "history" is, and the one thing it is not

A **session record** is one complete run: the dictated idea, the interrogation that followed, the
English prompt that was forged from it, and the answer that came back. It is written when the
answer completes, and updated in place when the user asks for more — a feedback round, a re-run on
another provider, a translation.

**It is not a log of everything the user did.** Three things are deliberately excluded:

- **Plain chat.** Chat is the owner's instrument for testing a provider, a key or a model
  (`ANSWERS.md` Part 7 §M3-1). Recording it would make the instrument accumulate state, and every
  measurement taken with it would then depend on how much of it had been used before.
- **An abandoned interrogation.** Gate run 2 was abandoned and §10 entry 29 calls that the most
  useful result of the three — but it is useful *to the project*, not to the user's history. A run
  with no answer has nothing to re-run and nothing to search; a list full of them is a list the
  user stops opening.
- **Raw audio, ever.** `ANSWERS.md` Part 6 §M2-10, and the microphone rationale says so in both
  locales. **That sentence must stay literally true**, and Room arriving in the project is exactly
  the moment someone would think of "just keeping the audio for playback".

**Nothing leaves the device.** K5. There is no network code anywhere in the history layer, and
that is the property to check if the question is ever reopened.

---

## 3. The record, and where each field earns its place

| Field | Why it exists |
|---|---|
| `id` | Room's key. Held by `ClarifyViewModel` after the first save so later updates hit the same row rather than writing a second one |
| `createdAt` | Sorting, and the «منذ يومين» label. Epoch millis |
| `idea` | The transcript **as dictated**, errors included — the same rule `ClarifySession.originalIdea` has kept since M3 |
| `turnsJson` | The interrogation, serialized. Needed to re-open a session and to show it in detail |
| `frameworkId` · `forgedPrompt` | What re-run actually re-runs |
| `answer` · `providerName` · `model` | The result, and who produced it |
| `summary` | **The short line CLARIFY reads back.** §4 |
| `searchText` | The normalized haystack. §5 |

`turnsJson` is the first serialized type in this project, so `ClarifyTurn`, `ClarifyState` and
`ClarifySession` gain `@Serializable`. All three carry a KDoc saying the annotation was withheld
until a real schema existed; **that condition is now met**, and the KDocs are updated rather than
left contradicting the code (§0 rule 5).

---

## 4. What CLARIFY reads back — the piece that needed the most thought

K1 says a **short summary of the last few sessions**, not their full text. The question the ruling
leaves open is *where the summary comes from*, and there are only two answers.

**Rejected: ask a model to write it.** It is one extra call per session, spent on text the user
will rarely read, on a milestone whose whole point is that the app stops needing the network for
things it already knows.

**Chosen: the engine already wrote it.** `ClarifyTurn.Enough` is the engine's own two-line summary
of the matured idea — it exists since M3, it is in Arabic, and it is written at the exact moment
the idea is settled. Saving it costs nothing. When a session has no `Enough` turn (the user
declared the idea mature before the engine ran out of questions), the fallback is the opening
line of the idea itself.

This is `SessionSummary.of()` in `:core-domain`, pure and unit-tested.

### 4.1 The budget is a design decision, not a number

The summary block rides on **every Clarify turn**, exactly like the «نبذة عنك» note. So it gets
the same treatment the note got: **one constant, in one place, that both the builder and any
future reader use.** `HistoryContext.MAX_SESSIONS = 5` and `MAX_CHARS = 1200`. Oldest entries are
dropped first when the budget binds — the newest sessions say the most about who is speaking now.

### 4.2 It ships with prohibitions, and that is not padding

§10 entry 32: *a fact handed to a model that has nothing else to hold onto becomes the subject.*
The «نبذة عنك» note needed three explicit prohibitions for that reason. **History is the same
hazard, larger** — five previous topics are five ways to drag a new question somewhere it did not
ask to go. So `HistoryContext` carries the same three prohibitions, worded for history: do not
assume the new idea continues an old one, do not quote them, do not steer toward them.

### 4.3 The note and history are not redundant, and §8 was wrong to say so

`PROJECT_STATE.md` §8 item 4 said history "replaces the «نبذة عنك» note's job". K2 overrides
that, and the reason is the one the owner named: **history is empty on a fresh install**, which is
the state every friend receiving the APK begins in. The note is the only thing that works on turn
one of a brand-new phone. They are a fallback pair, not a duplicate — and they are budgeted
together so that a user with both does not silently double the cost of every turn.

---

## 5. Search — the part that is genuinely Arabic-specific

A user who types `احمد` must find `أحمد`. A user who types `الاسئلة` must find `الأسئلة`. Arabic
substring matching without normalization finds neither, and the failure is invisible: the search
returns nothing and looks like an empty history rather than a broken match.

`ArabicNormalizer` in `:core-domain` folds what the user cannot reliably type:

- the alef family `أ إ آ ٱ` → `ا`
- `ة` → `ه`, `ى` → `ي`, `ؤ` → `و`, `ئ` → `ي`
- the harakat and tatweel are stripped entirely
- Arabic-Indic digits `٠–٩` → `0–9`
- Latin folded to lower case

**It is applied in exactly two places and they must be the same function:** once when the record
is written (into `searchText`) and once to the query. That is why it is a pure object in
`:core-domain` and not a helper next to the DAO — the failure mode of two nearly-identical
normalizers is a search that works for the developer and not for the user, and nothing on screen
would say so. `ProviderId.defaultModel` exists for the same reason.

SQLite then does a plain `LIKE` over the normalized column. No FTS table: it is a second schema to
migrate for a list of at most a few hundred rows on a phone.

---

## 6. The first-run flow

Shown once, when no key is stored **and** the user has not dismissed it. Both conditions, not
either: a user who skipped it and later deleted their only key should not be dragged back through
onboarding, and a user who has keys has already onboarded whatever the flag says.

`ANSWERS.md` Part 3 §B stands, including its 2026-08-03 amendment: **Gemini is the recommended
starting provider** because it is the free-tier path that makes distribution to friends work — and
the screen says plainly, in Arabic, that some regions are refused and names the alternative. That
sentence is not a hedge; it is the owner's own measured experience (`PROJECT_STATE.md` §6) and
every friend in his country will hit it.

**«تخطَّ» is always available.** §10 entry 26: an escape hatch removed without a replacement is a
trap. An onboarding screen a user cannot leave is the worst version of that.

---

## 7. TTS

`TextReader` in `:core-domain`, `AndroidTextReader` in `:speech` over the platform
`TextToSpeech`. **Off by default** (K3), a switch in Settings, and a button that appears under a
finished answer only when the switch is on.

**No new dependency.** `TextToSpeech` is in the platform. The one real hazard is that it is
asynchronous at construction — `speak()` before `onInit` silently does nothing — so the
implementation queues one pending utterance and flushes it on init rather than dropping it. A
button that works on the second press and not the first is §10 entry 7 wearing a new coat: pass
and fail would look identical.

---

## 8. Loading, empty and error states

`BRAINING.md` §12 asks for them "on every screen", which is not a specification. What it becomes
here: **every list that can be empty gets a sentence saying why it is empty and what to do**, and
every screen that can be waiting says what it is waiting for.

Three shared composables in `:core-ui` (`BrainingEmptyState`, `BrainingLoadingState`,
`BrainingErrorState`) rather than a handwritten column per screen. The reason is §10 entry 26 and
the two identical copy icons: a state drawn ad hoc in four screens is four different sentences
about the same situation.

---

## 9. The release APK — and the honest warning that belongs with it

**This project has never produced a release build.** Every build to date has been `installDebug`.
The release build differs in one way that matters: `isMinifyEnabled = true` runs R8, which strips
and renames what it believes is unused — and reflection-driven libraries are exactly what it gets
wrong. This project has four of them: kotlinx-serialization, Hilt, Ktor and now Room.

`app/proguard-rules.pro` is extended for Room and for serialization's generic signatures. **But a
proguard file is a claim about a build that has not been run**, and §10 entry 3 is about
publishing confident causes from partial evidence. So the test file asks for the release build as
its own step, with its own failure column, and says what a `ClassNotFoundException` or a
`SerializationException` in the release APK would mean and how to switch minification off in one
line if it happens.

**The key-safety audit (`ANSWERS.md` Part 3 §C) is a release blocker and is run as a grep over the
tree**, not as an assurance. Its exact commands are in the test file so the owner can re-run them
himself rather than trust a report.

---

## 10. What this note deliberately does not do

- **No Clarify session resumption after process death.** `ANSWERS.md` Part 7 §M3-4 stands: M5
  persists *finished* sessions. Resuming an interrupted interrogation is a different feature with
  a different failure surface, and the owner has not ruled on it.
- **No size cap and no auto-deletion.** `ANSWERS.md` Part 1 §10 — storage used is surfaced in
  Settings and the user decides. An app that silently deletes the user's thinking to save 40 MB
  has made a decision that was not its to make.
- **No FTS, no sync, no export.** Export was never asked for; it is noted in §9 of
  `PROJECT_STATE.md` rather than built.
