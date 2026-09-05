# tools/ — the checks and device scripts the project actually uses

Rescued into the repository on 2026-09-05; until then they lived in session-scoped locations and
would have vanished with the session that wrote them.

## Static checks (run from the repo root, before every build)
```
python tools/check.py      # braces, duplicate imports, cross-module R.string, format specifiers,
                           # AAPT2 escaping ('"' stripped silently, "'" is a hard failure)
python tools/imports.py <file.kt>...   # missing-import pass (lesson 63)
```
Both must print `PROBLEMS: 0` / `possible missing imports: 0`. They are not a compiler — the owner's
build is — but every problem they catch is one the compiler would have found a build later.

## Device scripts (`device-tests/`, PowerShell, need a phone on adb)
Always through `C:\Users\ASUS\AppData\Local\Android\Sdk\platform-tools-2\adb.exe` — lesson 67:
three adb binaries at two versions live on this machine and the wrong one kills the server mid-push.

| script | what it does |
|---|---|
| `braining_batch.ps1 -Cycles N -Start K` | mixed loop: settings→back→menu→switch provider / background+return / rotate; reports the semantics-tree size per cycle (healthy ≈ 12 500, blank ≈ 3 000) |
| `braining_death.ps1` | background → `am kill` → relaunch, six times: process-death restore |
| `braining_content.ps1` | sends a real message, then runs the mixed loop with content present |
| `braining_watchdog.ps1` | polls every 90 s; on a blank screen captures screenshot, `gfxinfo`, window state, UI tree |
| `braining_capture.bat` | continuous filtered logcat to `log_black.txt` (git-ignored) |

Coordinates inside them are for the owner's phone (1220×2712): Settings (228,200), provider menu
(957,200), menu rows y = 356/500/644/788/932/1076, Send (132,2580). Re-derive with `uiautomator dump`
on any other device. L1 (`docs/launch/01_SUPPORT_AND_TESTING.md`) turns these into first-class tests.
