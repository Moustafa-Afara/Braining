# L1 — Support model, and the testing regime

## Goal

Two things the owner asked for in one breath, and they are separated here on purpose:

- **(a)** A way for him to see and understand problems on users' phones, for support.
- **(b)** Comprehensive tests — automated where possible, then detailed manual suites he runs — so
  that when a user complains, the complaint can be reproduced and fixed with evidence.

(b) is unreservedly right and starts now. (a) as originally worded — *the owner connects over
Tailscale to any user's device that installs the app, after the user hands him a Tailscale key and
permission* — is the one place in this whole program where the recommendation is **do not build
this**, and the reasoning is set out so the owner can rule on it rather than take it on faith.

## Why (a) as worded should not be built

- **Tailscale is device-level, not app-level.** Joining a user's phone to the owner's tailnet gives
  network reachability to the *phone*, not to "the app only". There is no way to scope it to Braining.
- **To "use the app" remotely there would have to be a remote-control agent inside the shipped
  app**, listening for the owner. That is a backdoor by definition: a shipped, always-present path by
  which someone other than the phone's owner can operate it. It is precisely what L2 (security)
  exists to make impossible, and it would very likely fail Google Play review.
- **It contradicts the product's entire posture.** Braining is BYOK, no server, nothing leaves the
  phone. A support backdoor is the loudest possible exception to that promise, and the users are
  the owner's friends.
- **It makes the owner a liability holder.** If his laptop is ever compromised, every friend's phone
  is reachable through it.

## What achieves the owner's actual goal instead

The goal is *"to see the problems that happen on their side."* Two mechanisms deliver that with
nothing listening on anyone's phone:

1. **M8 field diagnostics — already specified and phase 1 shipped.** On any failure, the user taps
   once and a complete diagnostic report (device, network, provider, the redacted request, the last
   200 log lines, an anonymous user id) is shared to the owner over Telegram/WhatsApp. It carries more
   than a remote session would show, and the user chooses to send it.
2. **A user-initiated "support session" (new, small).** From Settings the user can turn on *live
   diagnostics for this session*: the app streams its `Diag` log to a file the user then shares, or
   — if a live view is truly wanted — publishes it to a channel the *user* opens and closes. The user
   starts it, the user ends it, nothing is inbound, nothing persists.

Both keep the promise. Neither requires Tailscale on the user's side at all.

## Part (b) — the testing regime

### What already exists
- `core-domain` unit tests (JUnit): `LocalEndpointTest` (24), `ModelCatalogTest` (11),
  `ProviderGuideTest`, `FileRequestDetectorTest` (9), `ApiKeySanitizer` tests. Run:
  `.\gradlew.bat :core-domain:test`.
- The manual test lists: `docs/TESTS_PENDING.md` (§1–16) and the seven-row list in
  `docs/M6_FILE_GENERATION.md` §8.
- **Device-driven scenario loops**, proven on 2026-09-05: `uiautomator dump` to locate controls by
  bounds, `input tap` to operate them, semantics-tree size as the health signal (healthy ≈ 12 500
  bytes, blank ≈ 3 000). Scripts lived in `%TEMP%` (`braining_batch.ps1`, `braining_death.ps1`);
  they should be brought into the repo under `tools/device-tests/`.
- `check.py` / `imports.py` (local static checks: braces, duplicate imports, cross-module `R`,
  AAPT2 escaping, missing imports).

### Deliverables
1. **`tools/device-tests/`** — the scenario loops as first-class scripts: settings/back/menu,
   rotation, background/return, process-death restore, with-content, plus one per feature. Each
   prints PASS/FAIL and leaves a log. Runnable by an agent with device access, unattended.
2. **Instrumented Compose UI tests** (`androidTest`) for the screens' critical paths — chat send,
   provider switch, Clarify flow, export buttons — so regressions are caught before a phone is
   touched. Run on the owner's device via `connectedDebugAndroidTest`.
3. **A static/dependency gate** in one command: `check.py`, lint, `:core-domain:test`, and a
   dependency-vulnerability scan (OWASP dependency-check or Gradle's `dependencyCheckAnalyze`).
4. **`docs/TEST_MATRIX.md`** — *one* consolidated manual matrix replacing the scattered lists: id,
   feature, steps, expected, last-passed date, device. Tagged by phase so a release runs a subset.
5. **`docs/COMPLAINT_PROTOCOL.md`** — how a user complaint becomes a fix: M8 report arrives → agent
   maps it to matrix ids → reproduces with a device loop → fixes → adds a regression row. Ten lines,
   followed every time.

### Steps
1. Move the scenario scripts into `tools/device-tests/`, parameterised, with a README.
2. Write the static gate script (`tools/gate.ps1`) and make it green.
3. Add `androidTest` dependencies (Compose UI test, Hilt testing) via the convention plugin; write
   the first four instrumented tests.
4. Consolidate the manual matrix; retire `TESTS_PENDING.md` by moving its rows in.
5. Write the complaint protocol; dry-run it on the black-screen history as the worked example.
6. Build the support-session toggle (small; reuses `Diag` + `FileExport`).

### Acceptance
- `tools/gate.ps1` runs green in one command on the owner's machine.
- Every scenario script produces PASS on the current build, unattended.
- Instrumented tests pass on the phone.
- The matrix has one row per user-visible feature and no row older than the last release.

### What the owner must provide
- **Ruled 2026-09-05: build the support session + M8 route. No remote access.** Closed.
- Nothing to install for (b).

### Open questions for the owner
- Should the support session be able to stream live at all, or is "generate and share a file" enough?
  *(Recommended: file only. Simpler, and nothing is ever open.)*
