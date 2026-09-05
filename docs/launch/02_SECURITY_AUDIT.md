# L2 — Security audit

## Goal
The owner's words: *"strict security tests so no one can hack it or use it illegitimately."* The
honest translation an agent must work from: **no application is unhackable.** What can be delivered
is a written threat model, every known class of weakness checked and closed, and a repeatable audit
that runs before each release. That is what "secure" means in practice, and it is what this brief
specifies.

## Why it is here
Braining holds users' API keys, talks to a local Ollama in cleartext on purpose, exposes a
`FileProvider`, and — after M7 — drives an agent that writes files on the owner's PC over a network.
Each of those is a place an audit must look, and the PC bridge is the one that can do real damage.

## What already exists (do not redo — verify)
- **BYOK in the Android Keystore** (`EncryptedKeyStoreImpl`, AES256-GCM, `MasterKey`).
- **`LocalEndpoint`** — the sole cleartext guard, unit-tested with 24 rows, most of them refusals
  (IPv6 literal validation closed a real hole: `fd00::1.evil.com`).
- **`redactSecrets`** on every request body shown in diagnostics.
- **`FileProvider`** scoped to `cache/exports/` only, `exported=false`.
- **Release build audited once** (2026-08-30): 3.6 MB, no key, no path, no device id, no password.
- **The M7 deny-list** (`ANSWERS.md` Part 20): signing key, `keystore.properties`, OpenCode's
  `auth.json`, `.ssh`/`.gnupg` — never readable or writable by the agent.

## Deliverables
1. **`docs/security/THREAT_MODEL.md`** — assets (keys, prompts, the PC, the signing key), actors
   (a curious friend, a lost phone, a compromised laptop, a malicious model output, a stranger on the
   same Wi-Fi), and for each pair: possible / mitigated by / residual. One page. Everything below
   traces to a row in it.
2. **`tools/security/audit.ps1`** — the repeatable pre-release audit, one command:
   - strings/`grep` of the release APK for key prefixes (`sk-`, `AIza`, `sk-ant-`), file-system
     paths, the owner's name/email, `keystore` — must be empty;
   - manifest review: no `usesCleartextTraffic` beyond the config, no exported components except the
     launcher, `allowBackup` decided deliberately (see open question);
   - `LocalEndpoint` tests + a live probe that `8.8.8.8:11434` is **refused**;
   - dependency-vulnerability scan; R8/ProGuard confirmed on; debug probes confirmed absent.
3. **PC-bridge penetration checklist** (`docs/security/BRIDGE_PENTEST.md`) — run against the real
   bridge: unauthenticated request refused; wrong token refused; request from a non-tailnet address
   refused; path escapes (`..`, absolute, symlink, UNC `\\server\share`, `C:\Windows\..\Users`)
   refused; deny-list paths refused for read *and* write; a destructive command pauses for approval
   and does nothing on refusal; audit log cannot be edited through the bridge; bridge never binds
   `0.0.0.0`; OpenCode never reachable except through the bridge.
4. **Prompt-injection review** — a model's output must not be able to widen its own permissions: the
   bridge, not the model, classifies destructive actions and produces the change report.
5. **Findings report** with severity, fix, and re-test — and a line in `PROJECT_STATE.md` §8 saying
   the audit passed on date X for build Y. A release without that line does not ship.

## Steps
1. Threat model first. Half a day. Nothing else in this brief is meaningful without it.
2. Write and run `audit.ps1` against the current release build; fix what it finds.
3. Run the bridge pentest against M7; fix; re-run until every row is a refusal.
4. Add a **tamper/debug check** decision (see open questions) and implement if chosen.
5. Record the pass in `PROJECT_STATE.md`. Repeat the whole thing for every release.

## Acceptance
Every row of the threat model has a mitigation or an accepted residual signed by the owner;
`audit.ps1` is green; every bridge-pentest row is a refusal; the debug probes are gone from the APK.

## What the owner must provide
- A decision on **remote access (L1)** first — a support backdoor cannot pass this audit.
- Whether `android:allowBackup` stays **true** (currently is): with it, the Keystore-encrypted
  prefs are backed up to the user's Google account. The keys themselves cannot be decrypted off the
  device, but the *recommendation is `false`* — a BYOK app has no business in anyone's cloud backup.
- Whether to add **root/debugger detection**. *(Recommended: no — it annoys developers and stops no
  determined attacker; the real defences are the Keystore and the deny-list.)*

## Open questions
- Should the bridge token rotate, and how does the phone learn the new one? *(Recommended: manual
  rotation from the PC, entered once in Settings — same as any provider key.)*
