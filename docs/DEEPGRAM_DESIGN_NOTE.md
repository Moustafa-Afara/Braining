# DEEPGRAM SPEECH-TO-TEXT — DESIGN NOTE

**Status: UNSIGNED.** §8 carries four decisions for the owner. No code until they are answered —
the same discipline `docs/M2_DESIGN_NOTE.md` followed, and it is why M2's build order never had
to be undone.

**Authority:** `ANSWERS.md` Part 6 §M2-10 (cloud STT, BYOK, raw audio deleted immediately) and
§M2-9 (`ar-SY` leads). `PROJECT_STATE.md` §10 `2026-08-06-K` carries the reasoning for choosing
Deepgram over Whisper, Vosk and ElevenLabs.

---

## 1. Why, in one paragraph

`AndroidSpeechToText` passes the gate and still loses words. It ends an utterance every few
seconds — measured at **6.0 and 4.8 words per segment** over two dictations — and each ending is
a seam where spontaneous speech disappears. It is structural: the silence hints are set,
`EXTRA_SEGMENTED_SESSION` is refused by Google's engine, and the restart is already as tight as
`Handler.post` allows. Deepgram holds one WebSocket open for the whole dictation. **There is no
seam, because there is no restart.**

---

## 2. The API key — where it lives and how it gets there

**Exactly like every other key in this app, and for the same reason.** Hard constraint 3: BYOK
always, entered at runtime, stored Keystore-encrypted, never hardcoded, never committed.

- `EncryptedKeyStore` already takes a plain `String` id, so `"DEEPGRAM"` needs **no interface
  change**. It is not a `ProviderId` — Deepgram is not an AI provider and must not appear in the
  chat provider selector.
- A new card in Settings, beside the provider keys: label, masked field, eye toggle, verify.
- **The owner types it on the phone.** It never passes through an agent, a file, or this repo.

### The one Deepgram-specific trap

Deepgram accepts its key **either** as an `Authorization: Token <key>` header **or** as a
`?token=` query parameter, because browser WebSocket clients cannot set headers. **Use the
header. Never the query parameter.** Hard constraint 3 names a key in a URL as a violation on
its own, and `BaseHttpProvider.redactSecrets` exists precisely because a key in a URL leaks into
every diagnostic that touches it. The Android client has no reason to take the browser's
compromise.

---

## 3. Audio — and a promise that got stronger

`AudioRecord`, PCM 16-bit, **16 kHz mono**, streamed to the socket in ~100 ms buffers.
Deepgram's `linear16` encoding takes raw PCM, so **no encoder, no codec, no media library.**

**The owner ruled that raw audio is deleted the moment the transcript returns. With streaming
there is nothing to delete: the audio never touches storage at all.** It goes from the
microphone buffer to the socket and is overwritten. That is a stronger guarantee than the ruling
asked for, and it should be stated in those terms rather than inherited quietly — «لا يحفظ
التطبيق الصوت» stops being a policy and becomes a fact about the code.

**The waveform keeps working, and improves.** `TranscriptionEvent.Amplitude` currently comes from
Google's `onRmsChanged`, whose scale the platform documents only loosely. We will own the PCM
buffer, so amplitude becomes a real RMS over real samples. `docs/BRAND.md` §6 calls the five-bar
mark "the signature interaction"; it gets more honest input, not less.

---

## 4. Transport

`io.ktor:ktor-client-websockets`, version ref `ktor` (3.5.1) — **verified to exist on Maven
Central at 3.5.0 before being written here.** Hard constraint 2 is in this file because an agent
once invented `io.ktor:ktor-client-sse` and it cost a day; the check is cheap and the habit is
the point.

One artifact, added to `gradle/libs.versions.toml` and to `:speech` only. Endpoint:
`wss://api.deepgram.com/v1/listen` with `model=nova-3`, `language=ar-SY`, `encoding=linear16`,
`sample_rate=16000`, `interim_results=true`.

`interim_results` is what keeps the live text: interim messages feed
`TranscriptionEvent.Partial`, and `is_final` messages feed `Segment`. **The existing UI needs no
change** — the same two events it already renders.

---

## 5. `stop()` — the «تمّ» button

Deepgram is told the stream is finished by sending `{"type":"CloseStream"}`; it then flushes the
final transcript and closes. So `stop()` must **stop feeding audio, send CloseStream, and keep
reading** until the socket closes.

**The failure mode to avoid is the one `2026-08-04-H` already fixed once for Google:** tearing
the connection down on «تمّ» throws away the last sentence the user just spoke. Same rule here,
different mechanism.

---

## 6. Language tag

`ar-SY`, per §M2-9. Deepgram documents 16 Arabic variants including `ar-SY`, `ar-LB`, `ar-PS`,
`ar-JO` — **the tag the owner ruled on is one the service was built to serve.**

The attempt ladder in `AndroidSpeechToText` does **not** carry over. It exists because Android's
engine lies about language support; Deepgram publishes its list. One tag, no ladder, and the
Developer Mode line reports it exactly as it does today.

---

## 7. What must be said out loud

The permission rationale has been corrected **twice** for describing an intention rather than the
code. It changes again, and this time the change is material: the app will record audio and send
it to a third party.

Both locales must state: the microphone is used to turn speech into text; **the audio is sent to
Deepgram to be transcribed**; Braining stores no audio at any point. Nothing weaker, and nothing
that implies on-device processing.

`ANSWERS.md` Part 3 §C — the key-safety audit before any shareable APK — now has a second key
shape to look for.

---

## 8. DECISIONS FOR THE OWNER

### D1 · When is Deepgram used instead of Google?

The Google engine stays (owner's instruction, 2026-08-06). Something has to choose.

- **(a) Deepgram whenever a key exists, Google otherwise.** No setting, no thinking. The user who
  pays gets the good one.
- **(b) A choice in Settings, defaulting to Deepgram when a key exists.** Honest, and it lets the
  owner A/B the two engines against the dialect test without rebuilding — which is worth real
  money during evaluation.
- **(c) Google always, Deepgram only behind an explicit toggle.** Safest for cost, worst for the
  product.

*Recommendation: **(b)**, and specifically because the next month is an evaluation month. The
comparison this project keeps needing — same voice, same passage, two engines — costs a build
each time under (a).*

### D2 · The network drops mid-dictation. Then what?

- **(a) Fail with a clear Arabic message**, keep whatever was transcribed, let the user retry.
- **(b) Fall back to Google mid-recording** and stitch the two halves.

*Recommendation: **(a)**. (b) sounds generous and produces a transcript that is half one engine
and half another, with a seam exactly where the network died — unreproducible, and it would
poison every measurement taken afterwards.*

### D3 · Does `verify()` spend money?

Verifying a Deepgram key means opening a socket and sending a moment of silence. It costs a
fraction of a cent and it is the only way to answer honestly, the same standard `verify()` is
held to for every AI provider.

- **(a) Yes — a real check, a real answer.**
- **(b) No — accept any non-empty string, no verification.**

*Recommendation: **(a)**. `2026-08-03-A` recorded what a green tick on an unverified key costs:
Settings said valid, chat failed, and the next agent went hunting through the auth code.*

### D4 · What happens when Deepgram is chosen but the key is missing or wrong?

- **(a) Typed error, no transcription** — consistent with `AiError.MissingKey`, which is how the
  chat behaves.
- **(b) Silently use Google.**

*Recommendation: **(a)**. (b) is how a user ends up believing they are paying for accuracy they
are not receiving. Silent downgrades are the class of bug this project spent 2026-08-06 paying
for.*

---

## 9. Acceptance

1. Build, install, app unchanged with no Deepgram key.
2. Key entered in Settings, verified, survives a restart.
3. Dictate: live partial text appears **while speaking**, the waveform moves.
4. «تمّ» → the final sentence is not lost.
5. Developer Mode reports `ar-SY`, the duration, and **1–2 segments** rather than 13.
6. Airplane mode → the typed Arabic error of D2, no crash.
7. **`docs/M2_GATE.md`'s dialect test, unchanged**, so the number is directly comparable to
   **13 segments / 63 words** and **14 / 84**. That comparison is the entire point of this work.
