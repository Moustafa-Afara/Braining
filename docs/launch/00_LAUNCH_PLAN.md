# Braining — Launch Program (L1 → L9)

**Written 2026-09-05 on the owner's instruction.** Nine items, in the order he gave. Each has its own
brief in this folder, written so that an agent who has never seen this project can read it, ask its
questions, and start. **The owner's order is binding**; the gates below are dependencies the order
already satisfies, made explicit so nobody trips on them.

**Preconditions.** M7 (PC bridge) and M8 (field diagnostics) are finished first —
`docs/M7_PC_BRIDGE.md`, `docs/M8_FIELD_DIAGNOSTICS.md`. The exception is **L1's testing regime**,
which should start immediately and run alongside everything, because it is how every later phase is
verified.

## How to use these briefs

Every brief has the same shape: *Goal · Why it is here · What already exists · Constraints that bind
· Deliverables · Steps · Acceptance · What the owner must provide · Open questions.* Read the whole
brief before touching anything. If a brief and the code disagree, the code is the fact and the brief
is corrected — never the other way round (`PROJECT_STATE.md` §10 entries 33, 34).

**Hard constraints that apply to every phase** (from `PROJECT_STATE.md` §4, repeated because a new
agent will not have read it yet): BYOK — keys are never hard-coded, logged, committed, or sent
anywhere but the provider; the shipped APK contains zero owner-specific data; raw audio is never
kept; `LocalEndpoint` is the only cleartext guard and is a release blocker; the signing key and
`keystore.properties*` never enter git; never touch `gradle-wrapper.properties`, `gradle.properties`,
the SDK `package.xml` files, or `keystore.properties`; the debug probes in `ChatScreen.kt` /
`NavGraph.kt` must be removed before any release APK.

## The nine phases

| # | Brief | One line | Gate |
|---|-------|----------|------|
| L1 | `01_SUPPORT_AND_TESTING.md` | Support model + the automated/manual testing regime + complaint reproduction | starts now; **remote-access question needs the owner's ruling** |
| L2 | `02_SECURITY_AUDIT.md` | Threat model and a strict audit before anything ships publicly | after M7/M8; **incompatible with a remote-access backdoor** |
| L3 | `03_USER_GUIDE_AND_VIDEO.md` | The comprehensive PDF guide + the video script that mirrors it | after L2 (guide documents the final behaviour) |
| L4 | `04_LEGAL_AND_OWNERSHIP.md` | License, terms, **privacy policy**, About screen with the owner's name → Telegram | before L5 — **Play requires a hosted privacy-policy URL** |
| L5 | `05_STORE_RELEASE_GOOGLE_AND_APPLE.md` | Google Play plan in full; **Apple: an honest feasibility assessment — the app is Android-only** | after L4 |
| L6 | `06_WEBSITE.md` | A calm, modern, interactive site: features, downloads, guide, policies, contact | hosts L3's PDF and L4's policies; GitHub Pages fits the no-server posture |
| L7 | `07_TECHNICAL_NARRATIVE_AR.md` | The complete technical story in simple Arabic, environment → store | **last of the build phases** — it documents everything before it |
| L8 | `08_MARKETING_AND_SUPPORT_AUTOMATION.md` | Free + paid marketing, launch sequence, and agent-run support so no team is needed | after L5/L6 |
| L9 | `09_NEXT_VERSION_PROPOSAL.md` | A full proposal for Version 2, for later | last |

## Three things the owner is asked to rule on before L1 begins

They are listed in full inside the briefs and in `ANSWERS.md` Part 21. In one line each:

1. **L1 — remote access to users' phones over Tailscale.** The briefs recommend **against** it and
   propose a user-initiated support session plus M8's reports instead. It needs a ruling.
2. **L5 — Apple.** This is a Kotlin/Compose Android app; there is no path to the App Store without
   an iOS build. The brief says what that would take. It needs a ruling on whether to pursue it.
3. **L4 — public source with all rights reserved, or a private repository.** "Exclusive ownership"
   is compatible with either; the choice changes what the license says.
