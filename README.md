# Braining — فهم

> أداة تحكّم صوتية عربية تُحوّل طلباتك المنطوقة إلى أفكار مكتملة، ثم إلى أوامر
> احترافية بالإنجليزية، ثم تُنفّذها، وتُعيد لك النتائج بالعربية — بقيادة عدة أدوات
> ذكاء اصطناعي، كلٌّ في موضع تفوّقه.
>
> An Arabic voice-commanded AI orchestrator: speak a request → it clarifies the
> idea with you → forges a professional English prompt → executes it → returns the
> result in Arabic. Shipped as a standalone Android APK.

---

## ما هذا المشروع؟ (Arabic)

**فهم (Braining)** تطبيق أندرويد شخصي يعمل بالصوت. تُملي طلبك، فيفرّغه إلى نص عربي،
ثم — وهذا جوهره — **يناقشك ويحلّل الفكرة معك** بالأسئلة والاقتراحات والملاحظات حتى
تنضج، ثم يصوغ **أمراً احترافياً بالإنجليزية** بأفضل أطر هندسة الأوامر، وينفّذه، ثم
يترجم لك النتيجة إلى العربية ويستقبل تغذيتك الراجعة صوتاً.

يعمل عبر **مسارين**:
- **المسار «أ» — عبر الـ API مباشرة:** بحث، تحليل، تلخيص، استشارة، نقاش، توليد ملفات.
  يعمل دائماً، **حتى وحاسبك مطفأ**.
- **المسار «ب» — جسر الحاسب:** بناء مشاريع برمجية على ملفاتك الحقيقية، وتوكيل مهام
  تعمل على القرص. يعمل فقط حين يكون حاسبك مُشغَّلاً، عبر OpenCode داخل شبكة Tailscale.

يُسلَّم كـ **APK مستقل** يمكن تثبيته على أي جهاز أندرويد، ويربط كل مستخدم بحاسبه هو.

## For an agent picking this repo up — START HERE

**Read `PROJECT_STATE.md`, in full, before anything else.** It is the live state of the build,
the working protocol, and the next step. It is the only file that knows what is finished.

Then, and only if your task needs them:

- `ANSWERS.md` — the owner's binding rulings. **Highest authority for decisions.**
- `BRAINING.md` — the original master prompt. Historically first, and **superseded by
  `ANSWERS.md` wherever they disagree.** Read it for intent, not for status.
- `docs/` — the design notes, the gates, the framework library.

The four §0 deliverables of `BRAINING.md` (Understanding Brief, Questions, Suggestions,
Permissions) **were produced and answered in 2026-07**; the answers are `ANSWERS.md`. Do not
produce them again. `scripts/install-skills.sh` belongs to that same first session.

**Status, 2026-08-17:** M1, M2 and M3 are closed — providers and streaming, Arabic voice capture,
and the CLARIFY + FORGE core. M4 (router, translate, feedback) is next. `PROJECT_STATE.md` §8 has
the exact next step, and §0 has the rules you are expected to work by — including the one that
matters most: **you edit, the owner builds.**

## Repository map

| Path | Purpose |
|---|---|
| `PROJECT_STATE.md` | **The live state, the protocol, the next step. Start here.** |
| `ANSWERS.md` | The owner's rulings. Highest authority for decisions. |
| `BRAINING.md` | The original master build prompt. Superseded by `ANSWERS.md` on conflict. |
| `README.md` | This overview. |
| `docs/ARCHITECTURE.md` | System design, modules, the two execution paths, data flow. |
| `docs/PROMPT_FRAMEWORKS.md` | The prompt-framework library + selection heuristics. |
| `docs/SKILLS.md` | Full agent-skill catalogue by domain, with install commands. |
| `docs/SETUP.md` | End-user setup (Arabic + English): install APK, keys, pair PC. |
| `scripts/install-skills.sh` | Installs all required OpenCode agent skills. |
| `scripts/setup.ps1` | One-command Windows bridge bootstrap + QR pairing. |
| `.opencode/instructions.md` | Persistent build rules for the agent. |

## Core principle

> **"An hour of analysis saves days of execution."**
> ساعة تحليل توفّر أياماً من التنفيذ.

The app over-clarifies before acting, and so does the agent building it. Analyze
deeply, execute once.

## Tech at a glance

Kotlin · Jetpack Compose · Material 3 (full RTL/Arabic) · Clean Architecture + MVVM ·
Hilt · Ktor (SSE streaming) · Room · DataStore · Keystore-encrypted keys · min SDK 26.
PC bridge: Tailscale + OpenCode headless.

## Providers

Anthropic (Claude) · OpenAI (ChatGPT) · DeepSeek · Google Gemini. Each user brings their own
keys, entered at runtime and stored encrypted on the device.

*GitHub Models replaced Copilot in the original plan (Copilot has no general text-generation
API), then proved closed to new sign-ups, and was removed from the app on 2026-08-17 —
`ANSWERS.md` Part 8 §D1. Gemini is the free-tier starting point recommended to new users.*
