package com.braining.ai.providers

import com.braining.ai.providers.ollama.OllamaProvider
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.store.EncryptedKeyStore
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Which providers could actually answer right now.
 *
 * ## Why this is a class and not a method on a ViewModel
 *
 * It was a private `keyedProviders()` in `ChatViewModel` **and** an identical one in
 * `ClarifyViewModel`. Two copies of a rule that decides what the user is offered is exactly the
 * shape `PROJECT_STATE.md` §10 entry 47 is about: the day one of them learns something the other
 * does not, the two screens disagree and both look correct in isolation. Adding Ollama would
 * have been the day, so the duplication is removed first.
 *
 * ## Holding a key is no longer the same question as being able to answer
 *
 * For four providers it is: a stored key means the account exists and the request will at least
 * be attempted. Ollama broke that equivalence — it has no key, and it has a state the others
 * cannot enter, which is **switched off**. So the question this class answers is the one the
 * caller actually has ("who can answer?"), not the one that used to be a good proxy for it
 * ("whose key do I hold?").
 */
@Singleton
class ProviderAvailability @Inject constructor(
    private val keyStore: EncryptedKeyStore,
    private val ollama: OllamaProvider,
) {

    /**
     * The providers worth offering, right now.
     *
     * @param probeLocal whether to ask the user's PC — which costs a network round trip, so the
     *   **default is not to**. Callers on the fallback path pass true explicitly: the owner's
     *   ruling of 2026-08-31 (`ANSWERS.md` Part 15 §O3) is that Ollama is offered only if it
     *   answers, because a chip leading to a timeout on a sleeping machine is worse than no
     *   chip. Defaulting to true would have handed that cost silently to every future caller.
     *
     * Never returns for a reason the caller did not ask for: an unreadable key store or an
     * unreachable machine each cost one candidate, never the caller's error card. Cancellation
     * is **not** swallowed — see [isLocalReachable].
     */
    suspend fun available(probeLocal: Boolean = false): Set<ProviderId> {
        val keyed = try {
            keyStore.getAllKeys()
                .filterValues { it.isNotBlank() }
                .keys
                .mapNotNull { name -> ProviderId.entries.firstOrNull { it.name == name } }
                .toSet()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptySet()
        }

        // Ollama is never in the key store — it has no key — so it is added here or not at all.
        val local = if (probeLocal && isLocalReachable()) setOf(ProviderId.OLLAMA) else emptySet()

        // Whatever a stale key store may claim, Ollama's membership is decided by the probe
        // alone: `- OLLAMA` first so a leftover entry from an earlier build cannot smuggle it in.
        return (keyed - ProviderId.OLLAMA) + local
    }

    /**
     * Reachable **and useful** — which are two conditions, not one.
     *
     * A machine that answers with an empty model list is running Ollama and has pulled nothing.
     * Offering it as a fallback would trade the failure the user already has for a second one
     * they understand less, so an empty list is not availability.
     *
     * `try/catch`, not `runCatching`: the latter catches `Throwable`, which includes
     * `CancellationException` — and `probe()` goes to deliberate trouble to rethrow it. Catching
     * it here would have undone that, told the fallback list "your PC is asleep" because the
     * user navigated away, and left the parent coroutine believing a cancelled child was alive.
     */
    private suspend fun isLocalReachable(): Boolean = try {
        val probe = ollama.probe(OllamaProvider.FALLBACK_PROBE_TIMEOUT_MS)
        probe is OllamaProvider.Probe.Reachable && probe.models.isNotEmpty()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        false
    }
}
