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
