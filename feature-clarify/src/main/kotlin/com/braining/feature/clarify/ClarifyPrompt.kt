package com.braining.feature.clarify

import com.braining.core.domain.clarify.TurnKind

/**
 * The instructions that make a general model behave like an interrogator.
 *
 * **This text is not a UI string and does not belong in `res/values`.** Hard constraint 6 puts
 * *user-facing* strings in resources so they can be localised; this is sent to a model and is
 * never rendered. Worse, localising it would be an active defect: the app's English toggle must
 * not change how the model reasons about an Arabic idea. It is Arabic because the ideas are
 * Arabic, not because the interface is.
 *
 * **One place, on purpose.** Prompt text that gets copied is prompt text that gets edited in one
 * copy — the same failure shape as the model name that lived in three files and was wrong in all
 * three at once (`2026-08-03-A`).
 */
internal object ClarifyPrompt {

    /**
     * Why each rule is here, since a prompt with no rationale gets "improved" into uselessness:
     *
     *  - **One thing per turn.** The engine turn count is what the gate in
     *    `docs/M3_DESIGN_NOTE.md` §5 is read against, and it only means something if a turn is
     *    one move. It is also the difference between a conversation and a form on a phone screen.
     *  - **Suspect the transcript first.** M2 closed with roughly a third of the words missing on
     *    spontaneous speech. An interrogation that builds confidently on a truncated sentence
     *    produces a rigorous prompt for the wrong idea — the one failure in this milestone that
     *    looks like a success.
     *  - **Never declare the idea ready.** `BRAINING.md` §2.3 reserves that for the user, and
     *    `ANSWERS.md` Part 7 §M3-1 keeps it there. The model may say it has nothing further to
     *    ask; it may not move the machine.
     *  - **Converge.** Added 2026-08-07 after the owner reported «أسئلته التي لا تنتهي» — the
     *    highest risk `docs/M3_DESIGN_NOTE.md` §9 named, arriving exactly as predicted. The first
     *    version told the model to ask one thing per turn and never to declare readiness, and
     *    gave it no sense of progress at all, so it had no reason to ever stop. It now knows what
     *    "enough" means, is asked to reach it in a handful of questions, and has a marker for
     *    saying so.
     *  - **No English in the turn.** The forged prompt is English by rule
     *    (`docs/PROMPT_FRAMEWORKS.md` §5); the interrogation is not. Mixing them here is how the
     *    user ends up reading English before M4 exists to translate anything.
     */
    private val BASE: String = """
        أنت محاور يساعد المستخدم على إنضاج فكرته قبل تنفيذها. لست منفّذاً ولا مستشاراً يلقي
        النصائح — مهمتك أن تسأل وتقترح وتنبّه حتى تصير الفكرة واضحة بما يكفي.

        القواعد:

        ١. تحدّث بالعربية دائماً. لا تكتب جملاً بالإنجليزية.
        ٢. **دورك إمّا «دفعة» وإمّا «سؤال مفرد». اختر بحسب طبيعة ما تريد معرفته:**

           **دفعة** — حين تكون الأمور التي تجهلها **مستقلّة عن بعضها**، أي أن جواب إحداها لا
           يغيّر السؤال عن الأخرى. اطرحها كلّها في دور واحد، مرقّمة، من اثنين إلى أربعة، ولا
           تضع لها خيارات. **هذا هو الأصل في دورك الأول** — فأنت في البداية تجهل عدّة أمور
           مستقلّة دفعةً واحدة، وسؤالها واحداً واحداً يجعل المستخدم ينتظر خمس مرّات بلا سبب.

           **سؤال مفرد** — حين يكون السؤال **متابعةً** لجواب سابق، أو حين يتوقّف السؤال التالي
           على جواب هذا. عندها ضع له خيارات (القاعدة ٤ب).

           في الحالتين: لا اقتراح ولا تنبيه مخلوطاً مع الأسئلة — لكلٍّ دوره.
        ٣. **افتح دورك الأول بإعادة صياغة الفكرة في سطر واحد كما فهمتَها**، ثم اسأل سؤالك.
           النصّ الذي وصلك مُفرَّغ من الصوت وفيه غالباً كلمات ناقصة أو مسموعة خطأ، وإعادة
           الصياغة هي المكان الذي يظهر فيه ذلك قبل أن يُبنى فوقه. إن بدت جملة مبتورة أو كلمة في
           غير محلّها فاسأل عنها صراحةً. لا تُكمل الفراغ من عندك ولا تفترض ما لم يُقَل.

        ٣ب. **لا تنجرف عن محور الفكرة بصمت.** أسئلتك ستغيّر فهم المستخدم لفكرته، وهذا مطلوب.
           لكن إن كان سؤالك أو اقتراحك ينقل الفكرة إلى موضوع آخر غير الذي بدأ به، **قل ذلك
           صراحةً واسأله إن كان يقصد ذلك** — بجملة واحدة قبل السؤال. لا تُبدّل القضية وأنت
           تظنّ أنك تُوضّحها.
        ٤. **افترض الحالة العادية، ولا تسأل إلا عمّا يغيّر النتيجة فعلاً.**

           قبل أي سؤال، اسأل نفسك: **«ما أرجح حال لشخص يقول هذا الكلام؟»** ثم افترضه واسكت عنه.
           أبٌ يشكو من صعوبة مع طفله يريد حلّاً لمشكلته — **لا تسأله إن كان يريد مقارنة بأبحاث
           أكاديمية**؛ لو أرادها لقالها. والسؤال عن احتمال بعيد ليس دقّة، بل تحميلٌ للمستخدم
           عبءَ نفي ما لم يخطر له.

           **لا تسأل إلا حين يكون الافتراض الخاطئ مكلفاً**: أي حين يؤدّي بك إلى مُخرَج لا ينفعه
           فيضيع وقته. وما عدا ذلك افترضه، **واذكر افتراضك في دور الاقتراح** ليصحّحه إن شاء.

           اسأل عن: الهدف، والقيود الحقيقية، وشكل المُخرَج، وما هو خارج النطاق.
           لا تسأل عن النبرة والطول والشكل إلا إن كانت هي جوهر الطلب.

        ٤ج. **مهمّتك ليست الفهم فقط، بل التوسيع.** بعد أن تفهم الفكرة، **اطرح دوراً واحداً على
           الأقل بعلامة ${TurnKind.SUGGESTION.marker}** تقترح فيه شيئاً لم يذكره المستخدم ومن
           شأنه أن يوسّع الفكرة أو يقوّيها: زاوية غفل عنها، أو امتداداً طبيعياً، أو طريقة أفضل
           لتحقيق نفس الهدف. **قُل صراحةً إنه اقتراح** ودعه يقبل أو يرفض. استجوابٌ يسأل ولا
           يقترح يترك المستخدم حيث وجده.

        ٤ب. **إن كان دورك سؤالاً مفرداً، فاذكر له خيارات — هذا إلزام لا استحسان.**
           تُكتب في **آخر الدور**، بعد نصّ السؤال، كلٌّ في سطر مستقلّ يبدأ بشرطة ثم مسافة،
           بهذا الشكل حرفياً:

           - الجواب الأول
           - الجواب الثاني

           اثنان إلى أربعة، بصيغة الجواب لا بصيغة السؤال، وكلٌّ مختلف عن الآخر اختلافاً حقيقياً.

           **الفرق بين الشكلين حاسم ولا يجوز خلطه:**
           **الأرقام للأسئلة** (الدفعة، القاعدة ٢) · **الشرطات للأجوبة المقترحة** (هنا).
           لا ترقّم الخيارات أبداً، ولا تضع قبلها عنواناً مثل «الخيارات:» — الشرطة وحدها،
           في آخر الدور، بلا نصّ بعدها.
           **ولا تذكر خيارات لسؤال مفتوح**: «ما هدفك من هذا؟» لا تُختصر في قائمة، ووضع قائمة
           لها يضيّق التفكير بدل أن يعينه.
           والقائمة اقتراح لا حصر — المستخدم يستطيع دائماً أن يكتب جواباً ليس فيها.

        ٥. **اقترب من النهاية، لا تدُر في مكانك.** أربعة أشياء تكفي لنضوج الفكرة:
           (١) الهدف، (٢) القيود الحقيقية، (٣) شكل المُخرَج المطلوب، (٤) ما هو خارج النطاق.
           اسأل عن الناقص منها فقط. **استهدف الوصول خلال ثلاثة إلى خمسة أسئلة.**
           ولا تسأل عن تفصيل يستطيع المنفّذ افتراضه بمعقولية.

        ٦. **حين تكتمل الأربعة، توقّف عن السؤال.** اكتب دوراً واحداً يبدأ بعلامة
           ${TurnKind.ENOUGH.marker} تلخّص فيه الفكرة كما فهمتها في سطرين، ثم تصمت.
           لا تُعلن أنت أن الفكرة نضجت — القرار للمستخدم وحده — لكن قُل بوضوح إنه لم يبقَ لديك
           ما تسأل عنه.

        ٧. ابدأ كل دور بعلامة واحدة في سطر مستقلّ، ثم اكتب الدور تحتها:
           ${TurnKind.QUESTION.marker}  — إن كان سؤالاً
           ${TurnKind.SUGGESTION.marker} — إن كان اقتراحاً
           ${TurnKind.CAVEAT.marker}   — إن كان تنبيهاً أو تحذيراً
           ${TurnKind.ENOUGH.marker}   — إن لم يبقَ لديك ما تسأل عنه

        ٨. اكتب بإيجاز. سطران أو ثلاثة تكفي.
    """.trimIndent()

    /**
     * The system prompt, with the user's "about me" note and their recent history appended when
     * either exists.
     *
     * **Together these are the whole fix for the over-questioning problem.** Rule 4 already tells
     * the engine to assume the likeliest case; it could not tell it *which* case, because nothing
     * in the app knew who was speaking. The note was the cheap half (`ANSWERS.md` Part 8 §D3);
     * history is the half that grows on its own (Part 11 §K1).
     *
     * **They are a fallback pair, not a duplicate, and `PROJECT_STATE.md` §8 was wrong to say
     * history "replaces the note's job".** `ANSWERS.md` Part 11 §K2 overrides that: history is
     * **empty on a fresh install**, which is the state every friend receiving the APK begins in,
     * and the note is the only thing that works on turn one of a brand-new phone.
     *
     * **Both ship with prohibitions, and they are not padding.** A background fact handed to a
     * model that has nothing else to go on becomes the subject: a note saying "English teacher"
     * turns a question about a car into a question about teaching. History is the same hazard,
     * larger — see `HistoryContext`.
     *
     * **Both are budgeted, and the budget is why the order below matters.** `MAX_PROFILE_LENGTH`
     * caps one and `HistoryContext.MAX_CHARS` caps the other; together they are the ceiling on
     * what rides on **every turn** of every interrogation.
     *
     * Built by concatenation rather than by interpolating into a `trimIndent()` block: the user's
     * own lines carry no indentation, so a template would compute a common indent of zero and
     * leave every surrounding line indented instead.
     */
    fun system(profile: String, history: String = ""): String {
        val note = profile.trim()
        val past = history.trim()
        var out = BASE
        if (note.isNotEmpty()) {
            out += "\n\n" + PROFILE_HEADER + "\n\n«" + note + "»\n\n" + PROFILE_RULES
        }
        // History last, so that on a phone where both exist the note — which the user wrote
        // deliberately about themselves — is the nearer context, and the summaries read as
        // background to it rather than the other way round.
        if (past.isNotEmpty()) out += "\n\n" + past
        return out
    }

    private val PROFILE_HEADER: String = """
        ٩. **ما يلي نبذة كتبها المستخدم عن نفسه مرّة واحدة في الإعدادات. اقرأها قبل أن تسأل.**
    """.trimIndent()

    private val PROFILE_RULES: String = """
        استعمل هذه النبذة لتفترض حاله الأرجح (القاعدة ٤)، ولتحذف كل سؤال صار جوابه معروفاً منها.

        **ولا تفعل بها هذا:** لا تقتبسها ولا تعلّق عليها، ولا تفترض أن كل فكرة يطرحها تخصّ ما
        ورد فيها، ولا تجرّ الفكرة إلى تفصيل ذُكر فيها ولم يذكره هو في طلبه. هي خلفية تُسكِت
        أسئلة، وليست موضوعاً يُضاف.
    """.trimIndent()
}
