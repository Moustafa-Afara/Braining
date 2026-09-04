package com.braining.core.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The half of the model listing that can actually be got wrong.
 *
 * Every case here is a shape a provider has sent or plausibly will: a price as a decimal string,
 * a model with no name, a listing that is not a listing at all.
 */
class ModelCatalogTest {

    private fun parse(raw: String) =
        ModelCatalog.parse(Json.parseToJsonElement(raw).jsonObject)

    @Test
    fun `a normal listing becomes rows`() {
        val out = parse(
            """
            {"data":[
              {"id":"anthropic/claude-sonnet-4","name":"Claude Sonnet 4",
               "pricing":{"prompt":"0.000003","completion":"0.000015"}}
            ]}
            """,
        )
        assertEquals(1, out.size)
        assertEquals("anthropic/claude-sonnet-4", out[0].id)
        assertEquals("Claude Sonnet 4", out[0].label)
        assertFalse(out[0].free)
    }

    @Test
    fun `the free suffix marks a model free`() {
        val out = parse("""{"data":[{"id":"qwen/qwen3-8b:free","name":"Qwen3 8B"}]}""")
        assertTrue(out[0].free)
    }

    @Test
    fun `a zero price marks a model free even without the suffix`() {
        // The second signal. A provider that does not use OpenRouter's `:free` convention still
        // has to say the price, and zero is zero.
        val out = parse(
            """{"data":[{"id":"x/y","name":"Y","pricing":{"prompt":"0","completion":"0"}}]}""",
        )
        assertTrue(out[0].free)
    }

    @Test
    fun `a decimal zero is still zero`() {
        // **The case a literal string comparison would get wrong.** `"0.0"` is not `"0"`, and a
        // test for the literal would quietly reclassify every free model as paid the day a
        // provider changed its formatting — with nothing on screen to say anything had changed.
        for (zero in listOf("0.0", "0.00", "0e0", "-0")) {
            val out = parse(
                """{"data":[{"id":"x/y","pricing":{"prompt":"$zero","completion":"$zero"}}]}""",
            )
            assertTrue("price $zero should read as free", out[0].free)
        }
    }

    @Test
    fun `a model is not free when only one side of the price is zero`() {
        // Free input and paid output is a real pricing shape, and it is not a free model.
        val out = parse(
            """{"data":[{"id":"x/y","pricing":{"prompt":"0","completion":"0.000015"}}]}""",
        )
        assertFalse(out[0].free)
    }

    @Test
    fun `a missing price is not evidence of being free`() {
        // Absence is not zero. Guessing "free" here would understate someone's bill.
        val out = parse("""{"data":[{"id":"x/y","name":"Y"}]}""")
        assertFalse(out[0].free)
    }

    @Test
    fun `a model with no name falls back to its id`() {
        // An unlabelled row is a row nobody can choose.
        val out = parse("""{"data":[{"id":"x/y"}]}""")
        assertEquals("x/y", out[0].label)
    }

    @Test
    fun `free models sort first, then by id`() {
        val out = parse(
            """
            {"data":[
              {"id":"z/paid","pricing":{"prompt":"1","completion":"1"}},
              {"id":"b/free:free"},
              {"id":"a/paid","pricing":{"prompt":"1","completion":"1"}},
              {"id":"a/free:free"}
            ]}
            """,
        )
        assertEquals(listOf("a/free:free", "b/free:free", "a/paid", "z/paid"), out.map { it.id })
    }

    @Test
    fun `entries without an id are dropped, not turned into blank rows`() {
        val out = parse("""{"data":[{"name":"nameless"},{"id":""},{"id":"x/y"}]}""")
        assertEquals(listOf("x/y"), out.map { it.id })
    }

    @Test
    fun `a duplicate id appears once`() {
        val out = parse("""{"data":[{"id":"x/y"},{"id":"x/y"}]}""")
        assertEquals(1, out.size)
    }

    @Test
    fun `nonsense returns an empty list rather than throwing`() {
        // An unreadable listing costs the user a picker. An exception would cost them the screen.
        assertTrue(parse("""{}""").isEmpty())
        assertTrue(parse("""{"data":"not an array"}""").isEmpty())
        assertTrue(parse("""{"data":[1,2,3]}""").isEmpty())
        assertTrue(parse("""{"data":[]}""").isEmpty())
    }
}
