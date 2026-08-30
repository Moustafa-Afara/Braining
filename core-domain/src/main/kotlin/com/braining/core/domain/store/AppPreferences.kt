package com.braining.core.domain.store

import com.braining.core.domain.model.ProviderId
import kotlinx.coroutines.flow.Flow

/**
 * Non-secret user preferences.
 *
 * Deliberately separate from [EncryptedKeyStore]: that one exists to protect API keys and
 * pays a real cost for it (Android Keystore, Tink keysets, a self-heal path for ROMs whose
 * Keystore loses the master key). Putting a debug toggle behind that machinery would mean
 * a corrupted keyset could take the toggle with it, and would encrypt something that is
 * not a secret.
 *
 * M5 added the TTS opt-in and the first-run flag, as this file predicted it would.
 *
 * **The "retain raw audio" toggle was never added and must not be.** `ANSWERS.md` Part 6 §M2-10
 * settled it the other way: audio is deleted the moment the transcript returns, with **no
 * toggle**, and the microphone rationale promises that in both locales. A preference here would
 * be the first step to making that sentence false.
 */
interface AppPreferences {

    /**
     * Developer Mode — `ANSWERS.md` Part 2 §9. When on, each reply carries the resolved
     * endpoint, the outgoing request body, first-chunk latency, total latency, chunk count
     * and token usage.
     */
    val developerMode: Flow<Boolean>

    suspend fun setDeveloperMode(enabled: Boolean)

    /**
     * The model the user picked per provider, keyed by [ProviderId.name]. A provider absent
     * from the map has not been overridden and uses [ProviderId.defaultModel].
     *
     * This is shared state rather than screen state on purpose. The model field lived in
     * `SettingsViewModel`'s in-memory UI state, which `ChatViewModel` — a separate object with
     * its own lifecycle — could not see, so editing the field changed nothing and the value
     * died with the screen. Chat and Settings are two views onto one setting, so the setting
     * has to outlive both of them.
     *
     * It also matters operationally: when a vendor retires a model name, being able to type a
     * replacement in Settings is the difference between a working app and waiting for a new
     * build.
     */
    val selectedModels: Flow<Map<String, String>>

    suspend fun setSelectedModel(providerId: ProviderId, model: String)

    /**
     * The "about me" note — a few lines the user writes once about themselves.
     *
     * **It exists to stop the interrogation asking questions it has no business asking.** The
     * owner's example, and it is the sharpest diagnosis in `PROJECT_STATE.md`: a father
     * struggling with his small child should never be asked whether he wants the results
     * compared against academic research. That question is only reachable because nothing tells
     * the engine who is speaking. Half of that was fixed by instruction; this is the other half.
     *
     * **Read by CLARIFY and FORGE only** — `ANSWERS.md` Part 8 §D3. Plain chat does not receive
     * it, because chat is the owner's instrument for testing a provider, a key or a model, and
     * anything added to its request changes what it measures.
     *
     * Stored **verbatim, untrimmed**, capped at [MAX_PROFILE_LENGTH]. Trimming on write would
     * fight the user's typing: the space they just pressed would be eaten before the character
     * after it arrived — the same trap `updateModel` documents in `SettingsViewModel`. The trim
     * belongs at the point of use.
     *
     * It never leaves the device except inside the two system prompts, and it is not a secret,
     * so it lives here rather than in [EncryptedKeyStore] — the split this file's header
     * describes.
     */
    val userProfile: Flow<String>

    suspend fun setUserProfile(text: String)

    /**
     * The provider selected in chat, by [ProviderId.name]. Null means the user has never chosen.
     *
     * It was in-memory only until 2026-08-17, so every restart silently reset the app to Gemini
     * — which the owner's own location refuses with a regional block. That made it a measurement
     * hazard as much as an annoyance: three gate comparisons must run on the same provider, and
     * nothing on screen said the provider had changed underneath them.
     */
    val selectedProvider: Flow<String?>

    suspend fun setSelectedProvider(providerId: ProviderId)

    /**
     * Read the answer aloud when the user asks. **Off until they turn it on** — `ANSWERS.md`
     * Part 1 §7 and Part 11 §K3.
     *
     * The default is the ruling, not a convention: a phone that starts talking in a quiet room is
     * a worse first impression than one that never offers to. The switch only makes the button
     * *appear*; nothing is ever spoken without a press.
     */
    val ttsEnabled: Flow<Boolean>

    suspend fun setTtsEnabled(enabled: Boolean)

    /**
     * True once the user has finished — or skipped — the first-run flow.
     *
     * **It is one of two conditions, never the only one.** The flow is shown when this is false
     * *and* no provider key is stored. A user who skipped it and later deleted their only key
     * must not be dragged back through onboarding, and a user who already has keys has onboarded
     * whatever this flag says. `docs/M5_DESIGN_NOTE.md` §6.
     */
    val onboardingDismissed: Flow<Boolean>

    suspend fun setOnboardingDismissed(dismissed: Boolean)

    companion object {
        /**
         * The cap on the note, in characters. Here rather than in the store or the screen
         * because both need it and a limit enforced in one of two places is not a limit.
         * It is a token budget: this text rides on **every** Clarify turn.
         */
        const val MAX_PROFILE_LENGTH = 600
    }
}
