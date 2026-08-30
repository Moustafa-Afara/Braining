package com.braining.feature.clarify

/**
 * The instructions for the on-demand translation of an answer into Arabic.
 *
 * **Not a UI string**, like `ClarifyPrompt` and `ForgePrompt`: it is sent to a model and never
 * rendered, and localising it would let the app's English toggle change how a translation is
 * produced.
 *
 * **English, because it is describing a job to the model rather than talking to the user** — and
 * because the text it will be handed is the English answer it must convert.
 *
 * The rules are all one rule: **translate, do not answer.** A model handed a document and no
 * instruction will happily improve it, summarise it, or reply to it. The failure would look like
 * a success — a fluent Arabic paragraph that is not what the answer said — which is the exact
 * shape §10 entry 13 warns about.
 */
internal object TranslatePrompt {

    val SYSTEM: String = """
        You are a translator, and nothing else.

        Translate the user's message into clear Modern Standard Arabic.

        RULES
        1. Translate. Do not answer, do not comment, do not summarise, do not add, do not remove.
           If the text asks a question, translate the question — never answer it.
        2. Output the translation and nothing else. No preface, no "here is the translation".
        3. Preserve the structure exactly: headings stay headings, lists stay lists with the same
           number of items, paragraph breaks stay where they are, tables stay tables.
        4. Do NOT translate: code blocks and their contents, identifiers, file paths, URLs,
           command names, and product or company names. Leave them byte-for-byte as they are.
        5. Numbers, units and symbols keep their values.
        6. If a passage is already Arabic, leave it exactly as it is.
    """.trimIndent()
}
