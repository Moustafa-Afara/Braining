> # ✅ نُفِّذ بالكامل — 2026-08-03. لا تُعِد تشغيله.
>
> نفّذته جلسة OpenCode، وبناه المالك، ونجحت اختبارات القبول السبعة كلها على الجهاز.
> التفاصيل في `PROJECT_STATE.md` §10 مدخل `2026-08-03-E`، والنتيجة في §7.
> يبقى هذا الملف للأرشيف ولأن سجلّ التغيير يشير إليه.
>
> **الشيء الوحيد المتبقّي منه:** جلسة OpenCode أضافت `implementation(project(":feature-chat"))`
> إلى `feature-settings/build.gradle.kts`، وهو اعتماد بين وحدتَي ميزة يجب فكّه — انظر §9.

---

# HANDOFF → OpenCode · A3 «الأخطاء المقروءة» + مفتاح اللغة

**صادر عن:** جلسة Claude (Cowork)، 2026-08-03
**إلى:** وكيل OpenCode، جلسة جديدة
**اقرأ أولاً:** `PROJECT_STATE.md` كاملاً — خاصة §0 (البروتوكول)، §4 (القيود الصارمة)، §7 (الحالة)،
و§10 مدخل `2026-08-03-A`. لا تكرّر ما فيها هنا.

---

## 0. حدودك في هذه المهمة

1. **أنت تحرّر. المالك يبني.** لا تشغّل `gradlew` ولا تزامن ولا تثبّت. انتهِ من التحرير، بلّغ، وتوقّف.
2. **لا تلمس** `gradle-wrapper.properties`، `gradle.properties`، `keystore.properties`، أو ملفات
   `package.xml` داخل Android SDK. لا تفتح مساعد الترقية في Android Studio.
3. **لا تضف أي تبعية** غير المذكورة صراحة في المهمة ٢ أدناه. `io.ktor:ktor-client-sse` **غير موجود** —
   إن أوحى لك أي شيء بإضافته فهو خطأ كلّف يوماً كاملاً من قبل.
4. **قاعدة التوقّف:** ١٥ دقيقة أو ٣ محاولات دون تقدّم قابل للقياس على مشكلة واحدة ← توقّف وبلّغ عمّا يعيقك.
5. **حدّث `PROJECT_STATE.md` §7 و§8 و§10 في نفس وحدة العمل**، لا كمهمة لاحقة. مدخل سجل التغيير
   إضافة فقط — لا تحذف ولا تعِد كتابة مدخل وكيل آخر.

---

## المهمة ١ — A3: أخطاء مصنّفة تُترجَم في الواجهة

### المشكلة

كل الأعطال تصل المستخدم كسلسلة إنجليزية واحدة مبنيّة في الطبقة الخطأ:

- `BaseHttpProvider.complete()` يبعث `"No API key configured for ${id.displayName}"`
- `BaseHttpProvider.describeHttpError()` يُرجع `"HTTP nnn — message"`
- `SettingsViewModel.verifyProvider()` يبعث `"No API key configured"`

طبقتا النطاق والبيانات لا يجوز أن تعرفا كيف تُصاغ الجملة للمستخدم. المستخدم عربي، والنص إنجليزي،
وترجمة هذه السلاسل في مكانها **تُرسّخ المعمارية الخاطئة** — لا تفعل ذلك.

### المطلوب

**أ) نوع خطأ مصنّف في `core-domain`.** أنشئ `core-domain/model/AiError.kt`:

```kotlin
sealed interface AiError {
    val provider: ProviderId
    data class MissingKey(override val provider: ProviderId) : AiError
    data class InvalidKey(override val provider: ProviderId, val status: Int) : AiError
    data class Forbidden(override val provider: ProviderId, val status: Int) : AiError
    data class RateLimited(override val provider: ProviderId, val status: Int) : AiError
    data class ProviderDown(override val provider: ProviderId, val status: Int) : AiError
    data class NoNetwork(override val provider: ProviderId) : AiError
    data class Timeout(override val provider: ProviderId) : AiError
    /** المزوّد لا يخدم منطقة المستخدم. مؤكَّد على الجهاز 2026-08-03 مع Gemini. */
    data class RegionBlocked(override val provider: ProviderId, val status: Int) : AiError
    /** ما لا ينطبق عليه ما سبق. `detail` نصّ المزوّد الخام، للعرض في وضع المطوّر فقط. */
    data class Unknown(override val provider: ProviderId, val status: Int?, val detail: String?) : AiError
}
```

**ب) `AiChunk.Error` يحمل `AiError` لا `String`.** غيّر التوقيع، وعدّل `BaseHttpProvider` ليصنّف:
401 → `InvalidKey` · 403 → `Forbidden` · 429 → `RateLimited` · 5xx → `ProviderDown` ·
`UnknownHostException`/`ConnectException` → `NoNetwork` · `HttpRequestTimeoutException`
و`SocketTimeoutException` → `Timeout`. احتفظ بنصّ `error.message` الخام داخل `Unknown.detail` فقط.

**الحالة الصعبة — `RegionBlocked`.** رمز HTTP وحده لا يكفي هنا. Google تردّ على الحجب الإقليمي بـ
**400**، وهو رمز يعني عادةً «طلب معطوب» أي عطل برمجي — بينما هذا قيد لا حيلة للمستخدم فيه إلا تبديل
المزوّد. لذلك يجب فحص **نصّ** `error.message` لا الرمز فقط. النصّ المؤكَّد على الجهاز 2026-08-03:

> `User location is not supported for the API use.`

طابق على `"location is not supported"` (غير حسّاس لحالة الأحرف) وليس على الجملة كاملة — الصياغة تتغيّر
بين المزوّدين والإصدارات. **لا تصنّف كل 400 على أنه حجب إقليمي**؛ 400 يبقى `Unknown` ما لم يطابق النصّ.
راجع `ANSWERS.md` جزء ٣ §ب (تعديل 2026-08-03) — رسالة هذه الحالة يجب أن تقترح مزوّداً آخر صراحةً،
لأن هذا هو المسار الوحيد المتاح للمستخدم.

**ج) الترجمة في الواجهة.** أضف في `feature-chat` دالة `@Composable` تحوّل `AiError` إلى نص من
`stringResource`. كل رسالة **تذكر اسم المزوّد ورمز HTTP** — «Gemini: تجاوزت الحصة المسموحة (429).
جرّب لاحقاً أو بدّل المزوّد.» أضف النصوص إلى `feature-chat/res/values/strings.xml` (عربي، الافتراضي)
و`values-en/strings.xml`. سوابق التسمية `chat_` إلزامية — موارد المكتبات تندمج في فضاء اسم واحد.

**د) `SettingsViewModel` كذلك.** `verifyProvider` يخزّن `AiError` في `ProviderState`، والشاشة تترجمه.
هذا يعني أن `ProviderState.error` يتغيّر نوعه من `String?` إلى `AiError?`.

**هـ) `HttpTimeout` في نفس المرور.** في `core-data/di/CoreDataModule.kt` أضف
`install(HttpTimeout)` مع `requestTimeoutMillis` سخيّ (البثّ طويل) و`connectTimeoutMillis`
و`socketTimeoutMillis` معقولين. حالياً تُطبَّق افتراضيات OkHttp (~١٠ ثوانٍ قراءة)، فمزوّد يتلكّأ قبل
أول توكن يفشل باستثناء مقبس بدل رسالة مفهومة. **`HttpTimeout` جزء من `ktor-client-core` — لا تبعية جديدة.**

### أين لا تذهب

`BaseHttpProvider.redactSecrets` هو كل ما يفصل مفتاح API عن لوحة وضع المطوّر (القيد الصارم ٣).
إن عرضت `Unknown.detail` في أي مكان، فيجب أن يكون قد مرّ عبرها. لا تضف مساراً جديداً يعرض بيانات
الطلب دون تعقيم.

---

## المهمة ٢ — مفتاح عربي/إنجليزي داخل التطبيق

**المالك وافق على `androidx.appcompat` صراحةً بتاريخ 2026-08-03.** هذه التبعية الوحيدة المسموح لك بإضافتها.

- أضف `appcompat` إلى `gradle/libs.versions.toml` بإصدار مثبّت، ثم إلى `app/build.gradle.kts`.
- حوّل `MainActivity` إلى مضيف قائم على AppCompat.
- شغّل التبديل بـ `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))`
  أو `"en"`. minSdk 26 يجعل هذا المسار الوحيد المتاح.
- ضع المفتاح في `feature-settings`. بنية الموارد جاهزة بالفعل (`values/` عربي، `values-en/` إنجليزي).

**تحذير:** إن اضطررت لتغيير أي إصدار آخر في `libs.versions.toml` لجعل appcompat يتوافق — **توقّف وبلّغ.**
سلسلة الأدوات في §3 مثبّتة بشقّ الأنفس، و Compose BOM مكرّر في موضعين يجب أن يبقيا متطابقين.

---

## ما لا تلمسه في هذه الجلسة

- أسماء النماذج. تعيش في `ProviderId.defaultModel` وحدها، وقد صُحّحت للتوّ. إن رأيت اسم نموذج مكتوباً
  في أي موضع آخر فذلك عطل — **بلّغ عنه في §9، لا تصلحه هنا.**
- موضع بطاقة وضع المطوّر في `SettingsScreen`. نُقلت للتوّ إلى الأعلى عمداً؛ التعليق في الملف يشرح المقايضة.
- `BaseHttpProvider.complete()` — لا تُعِده إلى `httpClient.post()` أبداً. انظر §10 مدخل `2026-07-29-A`.
- أي شيء في §9 (طابور التأجيل) ما لم يُسنَد إليك صراحة.

---

## تقريرك عند الانتهاء

اتبع قالب §11:

1. **ما تغيّر** — ملفاً ملفاً، سطر لكل ملف.
2. **لماذا** — التشخيص لا التعديل.
3. **ما يشغّله المالك** — الأمر بالضبط ومُدخل الاختبار بالضبط.
4. **كيف يبدو النجاح، وماذا يعني الفشل** — بحيث تشير النتيجة السيئة إلى المشتبه التالي بدل إعادة التحقيق.

ثم أكّد أنك حدّثت §7 و§8 و§10.

### اختبارات القبول التي يجب أن يمرّ بها عملك

| الحالة | كيف تُنتَج | المتوقّع |
|---|---|---|
| مفتاح مفقود | امسح مفتاح المزوّد من الإعدادات وأرسل رسالة | رسالة عربية تسمّي المزوّد، دون رمز HTTP |
| مفتاح خاطئ (401) | غيّر حرفاً في المفتاح ثم أرسل | رسالة عربية + «401» + اسم المزوّد |
| تجاوز حصة (429) | Gemini على الطبقة المجانية تحت ضغط | رسالة عربية + «429» |
| لا شبكة | وضع الطيران ثم أرسل | رسالة عربية، **دون** رمز HTTP |
| مهلة | مزوّد يتلكّأ | رسالة مهلة عربية، لا استثناء مقبس |
| حجب إقليمي | أرسل على Gemini من جهاز المالك | رسالة عربية تشرح أن الخدمة غير متاحة في المنطقة **وتقترح مزوّداً بديلاً** — لا «HTTP 400» |
| تبديل اللغة | Settings → إنجليزي | كل الشاشات تنقلب فوراً، والاتجاه يصبح LTR |

**لا تعلن الانتهاء وأي سطر أعلاه لم يُختبر.** إن تعذّر إنتاج حالة على جهازك، قل ذلك صراحةً بدل تخمين النتيجة.
