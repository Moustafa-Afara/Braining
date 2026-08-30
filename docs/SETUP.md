# Setup Guide — Braining (فهم)

Two languages below. العربية أولاً، ثم English.

---

# دليل التثبيت (بالعربية)

**فهم (Braining)** يعمل على مسارين. المسار «أ» (بحث، تحليل، نقاش، توليد ملفات) يعمل
بمجرد تثبيت التطبيق وإدخال مفاتيح الـ API — **لا يحتاج حاسباً**. المسار «ب» (بناء
مشاريع برمجية على ملفاتك) يحتاج ربط التطبيق بحاسبك، ويعمل فقط حين يكون حاسبك مُشغَّلاً.

## الجزء الأول: تثبيت التطبيق (دقيقتان)

1. انقل ملف `braining.apk` إلى هاتفك (أو نزّله عليه مباشرة).
2. عند فتحه سيطلب هاتفك السماح بتثبيت التطبيقات من هذا المصدر — فعّل الخيار.
   (الإعدادات ← التطبيقات ← تثبيت تطبيقات غير معروفة ← فعّل للمتصفّح أو مدير الملفات.)
3. اضغط «تثبيت»، ثم افتح التطبيق.
4. سيطلب التطبيق إذن **الميكروفون** (ضروري للصوت) و**الإشعارات** — اسمح بهما.

## الجزء الثاني: إدخال مفتاح واحد على الأقل (ثلاث دقائق)

**عند أول فتح يرشدك التطبيق بنفسه** (M5): يشرح ما يحتاجه، ويقترح Google Gemini لأن فيه خطّة
مجانية بلا بطاقة ائتمان، ويأخذ منك المفتاح ويتحقّق منه في الشاشة نفسها. تستطيع تخطّي هذه
الشاشة في أي لحظة والعودة إليها من الإعدادات.

المزوّدون الأربعة: **Claude · ChatGPT · DeepSeek · Google Gemini**.
(أُزيل GitHub Models من التطبيق في ١٧ آب ٢٠٢٦ — لم يكن يجيب.)

لإضافة مفاتيح أخرى لاحقاً:

1. افتح **الإعدادات ← مزوّدو الذكاء الاصطناعي**.
2. ألصق مفتاح كل خدمة تملكها. المفاتيح تُشفَّر على جهازك ولا تغادره إلا إلى خدمتها.
3. اضغط «تحقّق» بجانب كل مفتاح.
4. جاهز — المسار «أ» يعمل بالكامل (تحدّث بطلبك وابدأ).

> **ملاحظة على Gemini:** ترفض جوجل الطلبات من بعض الدول برسالة «المنطقة غير مدعومة». إن حدث
> ذلك فبدّل إلى مزوّد آخر — التطبيق يعمل بأيّها. لا يفترض التطبيق وجود VPN ولا يحتاجه.

## الجزء الثاني-ب: ما يحفظه التطبيق عنك

- **صوتك لا يُحفظ أبداً** — يُحذَف التسجيل لحظة عودة النصّ، بلا خيار ولا استثناء.
- **جلساتك تُحفظ على جهازك وحده.** كل فكرة نضجت ونُفِّذت تدخل السجلّ: تبحث فيها وتعيد
  تنفيذها. لا خادم ولا مزامنة ولا نسخة عند أحد.
- **حجم السجلّ ظاهر** في الإعدادات، ومعه «احذف الكل». حذف جلسة واحدة له زرّ «تراجع».
- **قراءة الجواب بصوت** خيار مطفأ افتراضياً؛ فعّله من الإعدادات إن أردت.

## الجزء الثالث (اختياري): ربط حاسبك للمسار «ب» (خطوة واحدة)

تحتاجه فقط لبناء مشاريع برمجية تعمل على ملفات حاسبك.

**على حاسبك (ويندوز):**
1. تأكّد من تثبيت: Node.js و Tailscale و OpenCode. (سيخبرك السكربت بأي ناقص.)
2. شغّل أمراً واحداً في PowerShell داخل مجلد المشروع:
   ```powershell
   ./scripts/setup.ps1
   ```
3. سيُظهر لك **رمز QR** ورمز اقتران قصير وعنوان الحاسب.

**على هاتفك:**
4. افتح **الإعدادات ← ربط الحاسب**، ثم امسح رمز QR أو أدخل الرمز يدوياً.
5. تم الربط. أبقِ حاسبك مُشغَّلاً أثناء تنفيذ مهام المسار «ب».

> ملاحظة مهمة: نستخدم **Tailscale** (مجاني للاستخدام الشخصي) لأنه يربط هاتفك بحاسبك
> عبر شبكة خاصة آمنة دون فتح أي منفذ على الإنترنت العام. لا تستخدم Phone Link أو
> TeamViewer لهذا الغرض — فهما غير مناسبين تقنياً لإرسال الأوامر للوكيل.

## كل مستخدم وحاسبه

هذا التطبيق مستقل: كل من يثبّته يربطه **بحاسبه هو** بنفس الخطوات أعلاه. لا شيء مربوط
مسبقاً بأي حاسب معيّن.

---

# Setup Guide (English)

**Braining** runs on two paths. Path A (research, analysis, discussion, file
generation) works as soon as you install the app and add API keys — **no PC needed**.
Path B (building software on your files) requires pairing the app with your PC and
only works while your PC is powered on.

## Part 1: Install the app (2 minutes)

1. Transfer `braining.apk` to your phone (or download it there).
2. Opening it, Android will ask to allow installing from this source — enable it.
   (Settings → Apps → Install unknown apps → enable for your browser/file manager.)
3. Tap Install, then open the app.
4. Grant the **Microphone** permission (required for voice) and **Notifications**.

## Part 2: Add at least one API key (3 minutes)

**On first launch the app guides you through this** (M5): it explains what it needs, suggests
Google Gemini because it has a free tier with no credit card, takes the key and verifies it on
the same screen. You can skip that screen at any point and return to it from Settings.

The four providers: **Claude · ChatGPT · DeepSeek · Google Gemini**.
(GitHub Models was removed from the app on 2026-08-17 — it could never answer.)

To add more keys later:

1. Open **Settings → AI providers**.
2. Paste a key for each service you have. Keys are encrypted on-device and leave only for their
   own provider.
3. Tap **Verify** next to each key.
4. Done — Path A is fully usable (speak your request and go).

> **A note on Gemini:** Google refuses requests from some countries with a "location is not
> supported" message. If that happens, switch to another provider — the app works with any of
> them. It does not assume a VPN and does not need one.

## Part 2b: What the app keeps

- **Your audio is never kept** — the recording is deleted the moment the transcript returns,
  with no toggle and no exception.
- **Your sessions are stored on this device only.** Every idea you mature and run enters the
  history: searchable, re-runnable. No server, no sync, no copy anywhere else.
- **The history's size is shown** in Settings, next to Delete all. Deleting a single session has
  an Undo.
- **Reading the answer aloud** is optional and off by default; turn it on in Settings.

## Part 3 (optional): Pair your PC for Path B (one step)

Only needed to build software that acts on your PC's files.

**On your PC (Windows):**
1. Ensure installed: Node.js, Tailscale, OpenCode. (The script tells you what's missing.)
2. Run one command in PowerShell inside the project folder:
   ```powershell
   ./scripts/setup.ps1
   ```
3. It shows a **QR code**, a short pairing code, and the PC address.

**On your phone:**
4. Open **Settings → Pair PC**, then scan the QR or enter the code manually.
5. Paired. Keep your PC on while running Path B tasks.

> Important: we use **Tailscale** (free for personal use) because it links your phone
> to your PC over a secure private network without opening any public internet ports.
> Do not use Phone Link or TeamViewer for this — they are technically unsuitable as a
> command channel to the agent.

## Every user, their own PC

The app is standalone: whoever installs it pairs it with **their own** PC using the
same steps above. Nothing is pre-wired to any specific machine.

---

# للمالك فقط: بناء نسخة الإصدار الموقّعة

**هذه النسخة لم تُبنَ في هذا المشروع قط.** كل ما بُني حتى الآن هو `installDebug`، وهو لا يشغّل
R8 أصلاً. اقرأ `docs/TESTS_PENDING.md` قسم الإصدار قبل أن تبدأ.

## ١. أنشئ مفتاح التوقيع (مرّة واحدة في عمر التطبيق)

الملفّ `keystore.properties` موجود لكنّه **ما زال يحمل قيماً افتراضية، ولا يوجد ملفّ مفتاح
على القرص**. بدونه لا يمكن توقيع أي نسخة إصدار.

```
keytool -genkey -v -keystore braining-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias braining
```

> **كلمة السرّ لن تظهر وأنت تكتبها — ولا حتى نجوم.** هذا سلوك متعمّد في أدوات جافا، لا عطل.
> اكتبها عمياء (٦ محارف على الأقل) واضغط Enter، ثم اكتبها ثانيةً. الخطوات كاملة في
> `docs/TESTS_PENDING.md` §٨.

ثم املأ `keystore.properties` بالقيم الأربع: `storeFile`, `storePassword`, `keyAlias`,
`keyPassword`.

> **احتفظ بهذا الملفّ وبكلمة سرّه في مكان آمن.** فقدانه يعني أنك **لن تستطيع** إصدار تحديث
> لتطبيق ثبّته أصدقاؤك — أندرويد يرفض تحديثاً موقّعاً بمفتاح مختلف. لا نسخة احتياطية منه في
> أي مكان، والملفّ مستثنى من git عمداً.

## ٢. ابنِ

```
cd C:\Dev\Braining
.\gradlew.bat assembleRelease
```

الناتج: `app/build/outputs/apk/release/app-release.apk`

## ٣. إن فشل التشغيل بعد نجاح البناء

هذا هو الخطر الحقيقي في أول نسخة إصدار: R8 يحذف ما يظنّه غير مستعمَل، والمكتبات التي تعمل
بالانعكاس هي بالضبط ما يخطئ فيه. أضيفت قواعد لـ Room وللتسلسل في `app/proguard-rules.pro`،
**لكنّها لم تُختبر ببناء بعد**.

إن انهار التطبيق أو فرغ السجلّ في نسخة الإصدار وحدها، فالتشخيص سطر واحد في
`app/build.gradle.kts`:

```kotlin
isMinifyEnabled = false
```

هذا يوقف R8 تماماً. إن اختفت المشكلة فهي قاعدة ناقصة في proguard، لا عطل في الشيفرة — أرسل لي
اسم الصنف الذي ظهر في `ClassNotFoundException` أو `SerializationException` وأضيف القاعدة.

---

# For the owner only: the signed release build

**This has never been built in this project.** Everything to date was `installDebug`, which does
not run R8 at all. Read the release section of `docs/TESTS_PENDING.md` before starting.

## 1. Create the signing key (once in the app's lifetime)

`keystore.properties` exists but **still holds placeholder values, and no key file is on disk**.
Without it no release build can be signed.

```
keytool -genkey -v -keystore braining-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias braining
```

> **The password is not echoed as you type it — not even as asterisks.** That is deliberate in
> Java's tools, not a fault. Type it blind (6 characters minimum), press Enter, then type it
> again.

Then fill in the four values in `keystore.properties`.

> **Keep that file and its password safe.** Losing it means you **cannot ship an update** to an
> app your friends have installed — Android refuses an update signed with a different key. There
> is no backup anywhere, and the file is deliberately excluded from git.

## 2. Build

```
cd C:\Dev\Braining
.\gradlew.bat assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

## 3. If it builds but misbehaves

This is the real hazard in a first release build: R8 strips what it believes is unused, and
reflection-driven libraries are exactly what it gets wrong. Rules for Room and for serialization
were added to `app/proguard-rules.pro`, **but no build has yet proven them**.

If the app crashes, or history comes up empty in the release build only, the diagnosis is one
line in `app/build.gradle.kts`:

```kotlin
isMinifyEnabled = false
```

That disables R8 entirely. If the problem disappears, it is a missing proguard rule and not a
code fault — send the class name from the `ClassNotFoundException` or `SerializationException`
and the rule can be added.
