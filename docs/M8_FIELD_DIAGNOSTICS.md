# M8 — Field diagnostics (PROPOSAL, for discussion)

**Status:** proposal only, 2026-09-05. The owner asked for this as *the last stage* and asked that
it be written down now as a heading with a proposal to discuss when we reach it. **Nothing here is
decided.** This is the model's detailing of the idea so the discussion has something concrete to
cut.

**Owner instruction (2026-09-05):** after the app is released, be able to see what a user's
experience actually was when something goes wrong — device specs, network strength, location,
"everything about the experience" — by having the app **generate an error file** so the owner can
tell what happened on any error, failure, or bad fit for that user.

## The one-line goal

When a user hits a problem, the app produces **one shareable file** that carries enough context for
the owner to understand it without being in the room — and the user chooses to send it. No silent
upload.

## Why "the user chooses to send it" is the spine, not a detail

This app's whole posture is BYOK and no server: keys never leave the phone, nothing is collected.
Silent telemetry would break that promise. So the proposed mechanism is **local capture + a share
sheet the user taps** — the same `FileExport` M6 builds. The owner receives files the way any
friend sends a file, not through a backend. This keeps the audit ("the shipped APK contains zero
owner-specific data, sends nothing home") true.

## What the file could contain (to argue about)

- **App:** version, build, the selected provider and model *(names, never keys)*.
- **Device:** model, Android version, ABI, RAM class, screen size/density.
- **Network at the moment of failure:** Wi-Fi vs cellular, online/offline, a reachability + latency
  probe to the endpoint that failed (this is often the real story for a provider timeout).
- **The error:** message, provider, HTTP status, and the **already-redacted** request diagnostics
  (`RequestDiagnostics` + `redactSecrets` exist and are proven — reuse, do not reinvent).
- **Recent history:** the last ~200 `Diag` lines. Phase 1 of this — the `Diag` tag and the uncaught
  handler — **already shipped 2026-09-05** for the black screen. M8 adds an in-memory ring buffer
  so a report can include recent activity without trying to read logcat (an app cannot read its own
  full logcat on modern Android without a special permission).
- **Locale/language**, timestamp, and whether the failure was a crash, a provider error, or the
  user tapping "something is wrong".

## The three real questions for discussion

1. **Location.** The owner named it. It is the most sensitive item here: real-time location is
   never stored by policy, and even coarse location needs an explicit, opt-in permission with a
   truthful rationale. Proposal: **off by default**; if the owner wants it, city-level at most,
   behind a consent toggle, and the file states plainly that it is present. To be decided together.
2. **Crash auto-capture.** The uncaught handler could write a crash file on the *next* launch and
   offer to share it ("Braining closed unexpectedly last time — send a report?"). Opt-in per send.
3. **Format & size.** Markdown for a human to read, or JSON for the owner to diff across users?
   How much request detail is too much? How long to keep old reports on the device?

## Dependency

M8 sits on **M6**: it assembles the context above into a file and hands it to M6's `FileExport`.
Build order is M6 → M8. Phase 1 (`Diag`) is already in the tree.

## What is explicitly NOT proposed

A server, an analytics SDK, background collection, any automatic upload, raw audio (never — hard
constraint), or anything that puts user data anywhere the user did not personally send it.

---

## Build order (added 2026-09-05, so a fresh session can execute this)

**Phase 1 — DONE, already in the tree.** `core-ui/diagnostics/Diag.kt`: one tag (`BRAINING`), and
an uncaught-exception handler installed in `BrainingApp` that stamps any crash with that tag and
still chains to the platform handler. This shipped for the black-screen hunt and is the seam
everything below hangs on.

**Phase 2 — the ring buffer.** Give `Diag` an in-memory ring of the last ~200 lines. An app cannot
read its own full logcat on modern Android without a special permission, so the report must carry
its own history. Bounded, no disk, cleared on process death.

**Phase 3 — the context collector.** One pure function that assembles: app version and build; the
selected provider and model **by name, never the key**; device model, Android version, ABI, screen
size and density; network type (Wi-Fi / cellular / offline) and a reachability + latency probe to
the endpoint that failed; locale; timestamp; and the failure itself — message, provider, HTTP
status, plus the **already-redacted** `RequestDiagnostics` (`redactSecrets` exists and is proven —
reuse it, do not reinvent it).

**Phase 4 — the report file and the hand-off.** Render the above as Markdown and hand it to **M6's
`FileExport`**. That is the whole delivery mechanism: the user taps share, and it goes wherever they
choose. No server, no upload, no background collection.

**Phase 5 — the entry points.** A "شيء ما لا يعمل" action in Settings, and an offer on any provider
failure. Plus the crash case: the handler writes a marker, and on the *next* launch the app offers
once — "Braining closed unexpectedly last time — send a report?"

## Test list

1. A report generated after a provider failure contains the provider, the status and the redacted
   request — and **no API key**. Open the file and read it.
2. The same report contains the last N `Diag` lines.
3. Network fields are correct on Wi-Fi, on cellular, and with the phone offline.
4. A forced crash produces the marker, and the next launch offers the report exactly **once**.
5. Sharing the report uses M6's share sheet and arrives intact.
6. With every optional field declined, the report still generates and is still useful.

## The owner's decisions — all four answered 2026-09-05 (`ANSWERS.md` Part 20)

1. **Location: OFF.** Not collected at all — not even city-level. The question is closed.
2. **Format: Markdown.** He is the only reader; a file he can read beats one he must parse.
3. **Crash auto-offer: yes.** After a crash the next launch offers the report once.
4. **Retention: until the fault is dealt with — and they aggregate.** This is the substantive one,
   and it adds work to the milestone rather than merely answering it. See below.

## The aggregate report — `ANSWERS.md` Part 20 §M8-4

Reports are not aged out on a timer; they are kept **until handled**. Alongside them the app
maintains **one combined report** so a pile of individual files stays reviewable:

- **`reports/index.md`** — one row per incident: date, time, anonymous user id, app version,
  provider, error type, and a one-line summary. Exportable as a single file through M6's
  `FileExport`, which is the point: the owner reviews and produces statistics from one document
  rather than opening twenty.
- **An anonymous user id.** The app is given to friends, so "which user" has to be answerable. A
  **random UUID generated on first run** and stored locally does it. Explicitly **not** a name, an
  email, a phone number, or an advertising/device identifier — the hard constraint is that the
  shipped APK carries no personal data, and a random number distinguishes a device without
  identifying its owner. It is sufficient for exactly what he asked for: reports and statistics.
- Marking an incident handled removes it from the active set; the index keeps the row.
