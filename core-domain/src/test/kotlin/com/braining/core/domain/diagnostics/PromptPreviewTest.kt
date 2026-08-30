package com.braining.core.domain.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The readable view of a request body, pinned.
 *
 * **Four provider shapes and one rule: never lie about what was sent.** A preview that drops a
 * message, mislabels a role, or silently shortens is worse than the raw JSON it replaced
 * (`PROJECT_STATE.md` §10 entry 6). Every check below is one of those failures.
 */
class PromptPreviewTest {

    // ── the shapes ──────────────────────────────────────────────────────────────────────

    @Test
    fun `openai style messages are split by role`() {
        val body = """
            {"model":"gpt-4o","messages":[
              {"role":"system","content":"كن مختصراً"},
              {"role":"user","content":"ما رأيك؟"}
            ]}
        """.trimIndent()
        val out = PromptPreview.of(body)
        assertEquals(PromptPreview.Kind.MODEL, out[0].kind)
        assertEquals("gpt-4o", out[0].text)
        assertEquals(PromptPreview.Kind.SYSTEM, out[1].kind)
        assertEquals("كن مختصراً", out[1].text)
        assertEquals(PromptPreview.Kind.USER, out[2].kind)
    }

    @Test
    fun `anthropic puts the system prompt at the top level`() {
        val body = """{"model":"claude","system":"تعليمات","messages":[{"role":"user","content":"س"}]}"""
        val out = PromptPreview.of(body)
        assertTrue(out.any { it.kind == PromptPreview.Kind.SYSTEM && it.text == "تعليمات" })
    }

    @Test
    fun `anthropic block form is flattened too`() {
        // Some versions send the system prompt as a list of typed content blocks.
        val body = """{"system":[{"type":"text","text":"تعليمات"}],"messages":[]}"""
        val out = PromptPreview.of(body)
        assertTrue(out.any { it.text == "تعليمات" })
    }

    @Test
    fun `gemini contents and systemInstruction are read`() {
        val body = """
            {"systemInstruction":{"parts":[{"text":"تعليمات"}]},
             "contents":[{"role":"user","parts":[{"text":"سؤال"}]}]}
        """.trimIndent()
        val out = PromptPreview.of(body)
        assertTrue(out.any { it.kind == PromptPreview.Kind.SYSTEM && it.text == "تعليمات" })
        assertTrue(out.any { it.kind == PromptPreview.Kind.USER && it.text == "سؤال" })
    }

    @Test
    fun `the model role is labelled as the assistant, not as the user`() {
        // Gemini calls the assistant "model". Mislabelling it would show the reader the model's
        // own previous answer as something the user typed.
        val body = """{"contents":[{"role":"model","parts":[{"text":"جواب"}]}]}"""
        val out = PromptPreview.of(body)
        assertEquals(PromptPreview.Kind.ASSISTANT, out.first().kind)
    }

    // ── it never throws, and never disappears ───────────────────────────────────────────

    @Test
    fun `a body that is not json comes back whole`() {
        // A truncated capture must not produce an empty panel — that reads as "nothing was
        // captured", which is a different and wrong statement.
        val out = PromptPreview.of("not json at all")
        assertEquals(1, out.size)
        assertEquals(PromptPreview.Kind.RAW, out.first().kind)
        assertEquals("not json at all", out.first().text)
    }

    @Test
    fun `valid json with nothing recognisable comes back whole`() {
        val body = """{"something":"else"}"""
        assertEquals(PromptPreview.Kind.RAW, PromptPreview.of(body).first().kind)
    }

    @Test
    fun `an empty body produces no sections at all`() {
        assertTrue(PromptPreview.of("").isEmpty())
        assertTrue(PromptPreview.of("   ").isEmpty())
    }

    // ── readability, without changing meaning ───────────────────────────────────────────

    @Test
    fun `escaped newlines become real ones`() {
        // This is the owner's actual complaint: the raw body shows `\n` as two characters and
        // the whole prompt runs together on one line.
        val body = """{"messages":[{"role":"user","content":"سطر\nسطر ثانٍ"}]}"""
        val text = PromptPreview.of(body).first { it.kind == PromptPreview.Kind.USER }.text
        assertTrue(text.contains("\n"))
        assertFalse(text.contains("\\n"))
        assertEquals(2, text.lines().size)
    }

    @Test
    fun `markdown emphasis markers are dropped`() {
        val body = """{"messages":[{"role":"system","content":"**قاعدة** مهمة"}]}"""
        assertEquals("قاعدة مهمة", PromptPreview.of(body).first().text)
    }

    @Test
    fun `runs of blank lines collapse to one`() {
        val body = """{"messages":[{"role":"user","content":"أ\n\n\n\nب"}]}"""
        assertEquals(3, PromptPreview.of(body).first().text.lines().size)
    }

    @Test
    fun `sentences are not re-wrapped`() {
        // Deliberately NOT split on «.» — decimals, abbreviations and Arabic punctuation would
        // all break, and a preview that mangles its source is the diagnostic §10 entry 6 warns
        // about.
        val body = """{"messages":[{"role":"user","content":"جملة أولى. جملة ثانية."}]}"""
        assertEquals(1, PromptPreview.of(body).first().text.lines().size)
    }

    // ── the cap is announced, never silent ──────────────────────────────────────────────

    @Test
    fun `a long section is capped and says how much it dropped`() {
        val long = "ك".repeat(PromptPreview.MAX_SECTION_CHARS + 250)
        val body = """{"messages":[{"role":"system","content":"$long"}]}"""
        val section = PromptPreview.of(body).first()
        assertEquals(PromptPreview.MAX_SECTION_CHARS, section.text.length)
        assertEquals(250, section.truncated)
    }

    @Test
    fun `a short section reports nothing dropped`() {
        val body = """{"messages":[{"role":"user","content":"قصير"}]}"""
        assertEquals(0, PromptPreview.of(body).first().truncated)
    }

    @Test
    fun `an empty message is kept, not dropped`() {
        // **A turn that vanishes is the panel lying about the request.** The first version
        // filtered blank sections out, so a genuinely empty message left the reader counting N-1
        // turns with nothing to say one was missing.
        val body = """{"messages":[{"role":"user","content":""},{"role":"assistant","content":"جواب"}]}"""
        val out = PromptPreview.of(body)
        assertEquals(2, out.size)
        assertEquals(PromptPreview.Kind.USER, out[0].kind)
        assertTrue(out[0].text.isEmpty())
        assertEquals(PromptPreview.Kind.ASSISTANT, out[1].kind)
    }

    @Test
    fun `a message whose shape is unreadable is kept as an empty section`() {
        // Same rule from the other direction: a content shape this reader does not understand
        // must surface as "there was a message here I could not read", never as silence.
        val body = """{"messages":[{"role":"user","content":{"weird":1}}]}"""
        val out = PromptPreview.of(body)
        assertEquals(1, out.size)
        assertEquals(PromptPreview.Kind.USER, out[0].kind)
    }
}
