package com.braining.core.domain.provider

import com.braining.core.domain.model.ProviderId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * These are cheap, and they exist because the failure they prevent is expensive: a broken link on
 * a help screen is worse than no help screen, since it costs the user a tap **and** their trust
 * in the rest of the page.
 */
class ProviderGuideTest {

    @Test
    fun `every provider has a guide`() {
        // `when` over an enum is exhaustive at compile time, so this cannot fail today. It
        // exists for the day a fifth provider joins the enum — OpenRouter and a network Ollama
        // are both queued — when the compiler points at `of` and this test says why it matters.
        for (id in ProviderId.entries) {
            val guide = ProviderGuide.of(id)
            assertTrue("$id has no key URL", guide.keyUrl.isNotBlank())
            assertTrue("$id has no docs URL", guide.docsUrl.isNotBlank())
        }
    }

    @Test
    fun `every URL is https`() {
        // A key page reached over http is a key page that can be tampered with in transit, and
        // this app's whole job on that screen is to be trusted with a credential.
        for (id in ProviderId.entries) {
            val guide = ProviderGuide.of(id)
            assertTrue("$id keyUrl is not https: ${guide.keyUrl}", guide.keyUrl.startsWith("https://"))
            assertTrue("$id docsUrl is not https: ${guide.docsUrl}", guide.docsUrl.startsWith("https://"))
        }
    }

    @Test
    fun `no URL carries a query string or a fragment`() {
        // A tracking parameter or a stale anchor is how a link rots without looking rotten.
        for (id in ProviderId.entries) {
            val guide = ProviderGuide.of(id)
            assertFalse("$id keyUrl carries a query: ${guide.keyUrl}", guide.keyUrl.contains('?'))
            assertFalse("$id keyUrl carries a fragment: ${guide.keyUrl}", guide.keyUrl.contains('#'))
            // Both of them. The test was named for two URLs and checked one — which is how a
            // `?utm_source=` gets into `docsUrl` under a green test.
            assertFalse("$id docsUrl carries a query: ${guide.docsUrl}", guide.docsUrl.contains('?'))
            assertFalse("$id docsUrl carries a fragment: ${guide.docsUrl}", guide.docsUrl.contains('#'))
        }
    }

    @Test
    fun `gemini is the free one and the region-limited one`() {
        // Both halves matter and they pull opposite ways: it is the only provider a friend can
        // start with for nothing, and the only one that may refuse them outright.
        val gemini = ProviderGuide.of(ProviderId.GEMINI)
        assertTrue(gemini.freeTier)
        assertTrue(gemini.regionLimited)
    }

    @Test
    fun `exactly these providers can be used without paying`() {
        // The claim that costs real money if it is wrong, so it is pinned as a **set** rather
        // than a subtract-chain. Written as `entries - GEMINI - OLLAMA` it broke the moment
        // OpenRouter arrived, and the fix was to lengthen the chain — which is a test being
        // edited to agree with the code instead of checking it. A set makes the next provider
        // fail here loudly and forces someone to decide, on purpose, which list it joins.
        //
        // Verified against each vendor's own documentation:
        //   GEMINI     — a working key with no payment method at all (2026-08-30)
        //   OLLAMA     — not a vendor. Hardware the user already owns; nobody can bill them
        //   OPENROUTER — free models reachable on a key with no balance (2026-09-04)
        val free = ProviderId.entries.filter { ProviderGuide.of(it).freeTier }.toSet()
        assertEquals(
            setOf(ProviderId.GEMINI, ProviderId.OLLAMA, ProviderId.OPENROUTER),
            free,
        )
    }

    @Test
    fun `the key prefixes are the ones the vendors actually issue`() {
        assertEquals("sk-ant-", ProviderGuide.of(ProviderId.ANTHROPIC).keyPrefix)
        assertEquals("AIza", ProviderGuide.of(ProviderId.GEMINI).keyPrefix)
    }
}
