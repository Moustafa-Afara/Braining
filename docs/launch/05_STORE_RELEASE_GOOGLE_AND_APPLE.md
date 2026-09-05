# L5 — Store release: Google Play in full, and an honest word about Apple

## Part A — Google Play (feasible, and this is the plan)

### Goal
Braining on Google Play, installable by anyone, updatable without sending APKs by hand.

### What already exists
A proven signed release build (2026-08-30: `assembleRelease`, v2/v3 signed, audited). The signing
key `braining-release.jks` — **the single most important file in the project; back it up off the
machine before anything else in this brief.**

### Steps
1. **Play Console developer account** — one-time fee (about US$25), identity verification. Takes a
   few days; start first.
2. **Play App Signing.** Decide: *enrol* (Google holds the app-signing key; the local `.jks` becomes
   the *upload* key) or *opt out*. **Recommended: enrol** — it is the only way the app survives a lost
   laptop. Record the decision in `ANSWERS.md`.
3. **Build an `.aab`** (`bundleRelease`), not an APK — Play requires bundles. Same signing config.
4. **Store listing** (AR + EN): title, short and full description, 2–8 phone screenshots per
   language (take them on the real device), a 512×512 icon, a 1024×500 feature graphic, category
   (Productivity), contact email, **the privacy-policy URL from L4**.
5. **Data Safety form** — answered from `PRIVACY.md`: no data collected, no data shared, keys
   stored on device, optional user-sent diagnostic file. Truthful and short.
6. **Content rating** questionnaire; **target audience** (adults); declarations (no ads).
7. **Permissions review**: `INTERNET`, `RECORD_AUDIO` (with the rationale already in the app),
   `ACCESS_NETWORK_STATE`. Nothing else. Any new permission is a red flag to reviewers.
8. **Target SDK**: Play requires targeting a recent API level (check the current requirement at
   submission time; the project is compileSdk 35).
9. **Internal testing track first** — the owner and friends; then closed testing; then production.
   Play may require a closed-testing period with a minimum number of testers before production for
   new personal accounts — plan for it.
10. **Release notes** in AR + EN, versionCode incremented every upload.

### Acceptance
Installed from Play on a fresh phone; update arrives from Play; Data Safety matches `PRIVACY.md`;
nothing in the listing contradicts the app.

### What the owner must provide
The developer account (his identity, his payment), the App Signing decision, the listing texts to
review, and the screenshots' approval.

## Part B — Apple App Store: a feasibility assessment, not a submission plan

**Braining is a Kotlin / Jetpack Compose Android application. It cannot be submitted to the App
Store.** There is no packaging step, converter or wrapper that turns it into an iOS app. This has to
be said plainly, because a plan that pretends otherwise wastes months.

### What an iOS version would require
1. **A Mac, an Apple Developer account (US$99/year), Xcode**, and the iOS review guidelines.
2. **A second UI**, one of:
   - **Compose Multiplatform + Kotlin Multiplatform (recommended if pursued).** `core-domain` is
     already pure Kotlin and would move to a shared module nearly as-is (`LocalEndpoint`,
     `ModelCatalog`, `FileRequestDetector`, the provider models). The Android-specific layers
     (Keystore, Room, Ktor engine, FileProvider, speech) need iOS counterparts (Keychain, SQLDelight
     or Room KMP, Darwin engine, `UIActivityViewController`, Speech framework). The UI would be
     rebuilt in Compose Multiplatform, sharing most composables.
   - **Native SwiftUI rewrite.** Cleaner iOS feel; nothing shared; twice the maintenance.
3. **Apple-specific constraints** to check before writing a line: BYOK apps are permitted, but Apple
   scrutinises apps whose core function is an AI chat client; a **PC bridge that executes commands
   on a computer** will need careful description; speech and clipboard permissions have their own
   review rules.
4. Realistic effort: **a milestone on the scale of M1–M4 combined**, not an add-on.

### Recommendation
Treat iOS as **a separate future milestone (M-iOS) gated on Play traction**: ship Android, learn from
real users, then decide. If the owner wants it regardless, the first deliverable is a two-week KMP
feasibility spike — move `core-domain` into a shared module and compile it for iOS — before any
commitment.

### Ruled 2026-09-05
**After Play results.** No iOS work before then; the feasibility spike is the first step when it starts.
