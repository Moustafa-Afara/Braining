package com.braining.core.domain.routing

import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.ProviderId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The router, pinned.
 *
 * `ANSWERS.md` Part 1 §9 gives unit tests to the router and the provider layer. These run on the
 * JVM with `gradlew :core-domain:test` — no device, no network, no keys, no owner.
 *
 * **What these tests are really protecting.** Two of this project's most expensive incidents were
 * silent substitutions: a provider that answered instead of the selected one, and an attempt
 * ladder whose order made the strongest engine unreachable. Fallback is exactly that shape of
 * code, so its order and its refusals are pinned here rather than described in a comment.
 */
class DefaultModelRouterTest {

    private val router = DefaultModelRouter()
    private val all = ProviderId.entries.toSet()

    // ── route ────────────────────────────────────────────────────────────────────────────

    @Test
    fun `route returns the selected provider, unchanged`() {
        val decision = router.route(ProviderId.DEEPSEEK, "deepseek-v4-flash")

        assertTrue(decision is RoutingDecision.Direct)
        decision as RoutingDecision.Direct
        assertEquals(ProviderId.DEEPSEEK, decision.provider)
        assertEquals("deepseek-v4-flash", decision.model)
        assertEquals(RouteReason.SELECTED_BY_USER, decision.reason)
        assertNull(decision.replacing)
    }

    @Test
    fun `route never invents a model name`() {
        // ProviderId.defaultModel is the single source of model names (2026-08-03-A). The router
        // passes through whatever it is handed and must never substitute a default of its own.
        val decision = router.route(ProviderId.ANTHROPIC, "a-model-the-user-typed")
        assertEquals("a-model-the-user-typed", (decision as RoutingDecision.Direct).model)
    }

    @Test
    fun `route never returns Path B before the bridge exists`() {
        ProviderId.entries.forEach { pid ->
            assertTrue(router.route(pid, pid.defaultModel) is RoutingDecision.Direct)
        }
    }

    // ── isRecoverable ────────────────────────────────────────────────────────────────────

    @Test
    fun `a provider-side failure is worth trying elsewhere`() {
        assertTrue(router.isRecoverable(AiError.Timeout(ProviderId.DEEPSEEK)))
        assertTrue(router.isRecoverable(AiError.RateLimited(ProviderId.DEEPSEEK, 429)))
        assertTrue(router.isRecoverable(AiError.ProviderDown(ProviderId.DEEPSEEK, 503)))
        assertTrue(router.isRecoverable(AiError.RegionBlocked(ProviderId.GEMINI, 400)))
        assertTrue(router.isRecoverable(AiError.InsufficientCredit(ProviderId.ANTHROPIC, 400)))
    }

    @Test
    fun `a setup failure is never routed around`() {
        // Falling back here would spend a second key to hide a problem the user must fix, and
        // they would never learn the first key was wrong.
        assertFalse(router.isRecoverable(AiError.MissingKey(ProviderId.OPENAI)))
        assertFalse(router.isRecoverable(AiError.InvalidKey(ProviderId.OPENAI, 401)))
        assertFalse(router.isRecoverable(AiError.Forbidden(ProviderId.OPENAI, 403)))
    }

    @Test
    fun `a dead network is not a provider problem`() {
        assertFalse(router.isRecoverable(AiError.NoNetwork(ProviderId.DEEPSEEK)))
    }

    @Test
    fun `an unclassified failure is not retried on someone else's key`() {
        assertFalse(router.isRecoverable(AiError.Unknown(ProviderId.DEEPSEEK, 418, "teapot")))
    }

    // ── fallback ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `no fallback for a failure that is not recoverable`() {
        val next = router.fallback(
            failed = ProviderId.OPENAI,
            error = AiError.InvalidKey(ProviderId.OPENAI, 401),
            keyed = all,
            alreadyTried = setOf(ProviderId.OPENAI),
        )
        assertNull(next)
    }

    @Test
    fun `fallback follows declaration order, not chance`() {
        val next = router.fallback(
            failed = ProviderId.DEEPSEEK,
            error = AiError.Timeout(ProviderId.DEEPSEEK),
            keyed = all,
            alreadyTried = setOf(ProviderId.DEEPSEEK),
        )
        // ANTHROPIC is first in ProviderId. A "best available" heuristic would make the same
        // failure fall back differently on two runs and turn every comparison into a measurement
        // of the router.
        assertEquals(ProviderId.ANTHROPIC, next)
    }

    @Test
    fun `fallback never returns a provider with no key`() {
        val next = router.fallback(
            failed = ProviderId.DEEPSEEK,
            error = AiError.Timeout(ProviderId.DEEPSEEK),
            keyed = setOf(ProviderId.DEEPSEEK, ProviderId.GEMINI),
            alreadyTried = setOf(ProviderId.DEEPSEEK),
        )
        assertEquals(ProviderId.GEMINI, next)
    }

    @Test
    fun `fallback never returns the provider that just failed`() {
        val next = router.fallback(
            failed = ProviderId.ANTHROPIC,
            error = AiError.ProviderDown(ProviderId.ANTHROPIC, 503),
            keyed = setOf(ProviderId.ANTHROPIC),
            alreadyTried = setOf(ProviderId.ANTHROPIC),
        )
        assertNull(next)
    }

    @Test
    fun `fallback stops instead of looping between two providers`() {
        // The bug this pins: with `alreadyTried` ignored, a two-provider setup ping-pongs forever
        // and every round spends the user's money.
        val next = router.fallback(
            failed = ProviderId.OPENAI,
            error = AiError.Timeout(ProviderId.OPENAI),
            keyed = setOf(ProviderId.ANTHROPIC, ProviderId.OPENAI),
            alreadyTried = setOf(ProviderId.ANTHROPIC, ProviderId.OPENAI),
        )
        assertNull(next)
    }

    @Test
    fun `fallback returns null when the user has only one key`() {
        val next = router.fallback(
            failed = ProviderId.DEEPSEEK,
            error = AiError.RateLimited(ProviderId.DEEPSEEK, 429),
            keyed = setOf(ProviderId.DEEPSEEK),
            alreadyTried = setOf(ProviderId.DEEPSEEK),
        )
        assertNull(next)
    }

    @Test
    fun `a full chain terminates`() {
        // Walk the whole ladder the way the ViewModel does and prove it ends.
        var failed = ProviderId.ANTHROPIC
        val tried = mutableSetOf(failed)
        var hops = 0
        while (true) {
            val next = router.fallback(failed, AiError.Timeout(failed), all, tried) ?: break
            tried += next
            failed = next
            hops++
            if (hops > 10) break
        }
        assertEquals(ProviderId.entries.size - 1, hops)
    }

    // ── fallbackCandidates · the manual choice, added 2026-08-28 ─────────────────────────
    //
    // The owner reversed his own ruling of 17 August: the app no longer hops by itself, it
    // offers the list and waits. **The head of this list and `fallback` must never disagree** —
    // if they did, the automatic "try any" button would pick a provider the chips did not offer,
    // and nothing on screen would explain why.

    @Test
    fun `fallback is exactly the head of the candidate list`() {
        val error = AiError.Timeout(ProviderId.ANTHROPIC)
        val head = router.fallbackCandidates(ProviderId.ANTHROPIC, error, all, setOf(ProviderId.ANTHROPIC))
            .firstOrNull()
        val single = router.fallback(ProviderId.ANTHROPIC, error, all, setOf(ProviderId.ANTHROPIC))
        assertEquals(single, head)
    }

    @Test
    fun `candidates exclude the failed provider and everything already tried`() {
        val tried = setOf(ProviderId.ANTHROPIC, ProviderId.OPENAI)
        val out = router.fallbackCandidates(
            failed = ProviderId.ANTHROPIC,
            error = AiError.ProviderDown(ProviderId.ANTHROPIC, 503),
            keyed = all,
            alreadyTried = tried,
        )
        assertFalse(out.contains(ProviderId.ANTHROPIC))
        assertFalse(out.contains(ProviderId.OPENAI))
        assertEquals(ProviderId.entries.size - 2, out.size)
    }

    @Test
    fun `candidates are only providers the user holds a key for`() {
        // Offering a chip for a provider with no key trades one failure for a worse one: the
        // second error names a missing key and reads as if the user misconfigured something.
        val keyed = setOf(ProviderId.DEEPSEEK)
        val out = router.fallbackCandidates(
            failed = ProviderId.ANTHROPIC,
            error = AiError.Timeout(ProviderId.ANTHROPIC),
            keyed = keyed,
            alreadyTried = setOf(ProviderId.ANTHROPIC),
        )
        assertEquals(listOf(ProviderId.DEEPSEEK), out)
    }

    @Test
    fun `an unrecoverable error offers nothing at all`() {
        // **Empty is a statement, not an absence.** A rejected key, a missing key and a dead
        // network are facts about the user's setup; a chip row offering to spend a second key on
        // them would hide a problem they have to fix either way.
        for (error in listOf(
            AiError.InvalidKey(ProviderId.ANTHROPIC, 401),
            AiError.MissingKey(ProviderId.ANTHROPIC),
            AiError.Forbidden(ProviderId.ANTHROPIC, 403),
            AiError.NoNetwork(ProviderId.ANTHROPIC),
            AiError.Unknown(ProviderId.ANTHROPIC, 400, "?"),
        )) {
            assertTrue(
                "expected no candidates for $error",
                router.fallbackCandidates(ProviderId.ANTHROPIC, error, all, setOf(ProviderId.ANTHROPIC)).isEmpty(),
            )
        }
    }

    @Test
    fun `candidates keep declaration order, so the offered list never shuffles`() {
        val out = router.fallbackCandidates(
            failed = ProviderId.GEMINI,
            error = AiError.RegionBlocked(ProviderId.GEMINI, 400),
            keyed = all,
            alreadyTried = setOf(ProviderId.GEMINI),
        )
        assertEquals(ProviderId.entries.filter { it != ProviderId.GEMINI }, out)
    }
}
