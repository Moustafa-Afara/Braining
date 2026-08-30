package com.braining.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braining.core.domain.history.SessionRepository
import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.model.ProviderState
import com.braining.core.domain.provider.AiProvider
import com.braining.core.domain.store.AppPreferences
import com.braining.core.domain.store.EncryptedKeyStore
import com.braining.core.domain.text.StorageSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val providers: Map<ProviderId, ProviderState> = emptyMap(),
    val defaultBrain: ProviderId = ProviderId.ANTHROPIC,
    val developerMode: Boolean = false,
    /**
     * The Deepgram transcription key.
     *
     * **Deliberately not a [ProviderId].** Deepgram turns speech into text; it never answers a
     * question. Adding it to that enum would put it in the chat provider selector, give it a
     * `defaultModel`, and hand it to the Hilt multibinding map of `AiProvider` — three wrong
     * things to fix later for one line saved now. `EncryptedKeyStore` takes a plain `String`
     * id, so `"DEEPGRAM"` needs no interface change.
     */
    val deepgramKey: String = "",

    /**
     * The "about me" note. Read by CLARIFY and FORGE only — `ANSWERS.md` Part 8 §D3.
     *
     * Held here as plain text with no trim anywhere on the path: the store keeps it verbatim so
     * that the collector below cannot round-trip a trimmed value back into a field the user is
     * still typing in. `updateModel` documents the same trap.
     */
    val userProfile: String = "",

    // ── M5 ───────────────────────────────────────────────────────────────────────────────
    /** Readback. Off until the user turns it on — `ANSWERS.md` Part 11 §K3. */
    val ttsEnabled: Boolean = false,
    /**
     * What history costs on disk.
     *
     * **This readout is what stands in place of a size cap.** `ANSWERS.md` Part 1 §10 rules that
     * text is kept indefinitely until the user deletes it, with no hard limit — which only works
     * if the user can see what they are accumulating. A number they cannot find is the same as
     * no number.
     */
    val historyStorage: StorageSize.Formatted = StorageSize.format(0),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val keyStore: EncryptedKeyStore,
    private val appPreferences: AppPreferences,
    private val providers: Map<String, @JvmSuppressWildcards AiProvider>,
    /** M5. Read for one number: how much disk the history occupies. */
    private val sessions: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Model names come from ProviderId, not from a local map. The previous local map
        // still said "deepseek-chat" — a name DeepSeek shut down on 2026-07-24 — because a
        // duplicated constant only ever gets updated where someone happens to be looking.
        val initial = ProviderId.entries.associateWith { pid ->
            ProviderState(providerId = pid, selectedModel = pid.defaultModel)
        }
        _uiState.value = SettingsUiState(providers = initial)
        loadKeys()

        viewModelScope.launch {
            appPreferences.developerMode.collect { enabled ->
                _uiState.update { it.copy(developerMode = enabled) }
            }
        }

        viewModelScope.launch {
            appPreferences.userProfile.collect { note ->
                _uiState.update { it.copy(userProfile = note) }
            }
        }

        viewModelScope.launch {
            appPreferences.ttsEnabled.collect { on ->
                _uiState.update { it.copy(ttsEnabled = on) }
            }
        }

        refreshHistoryStorage()

        // Overlay the user's saved overrides on top of the defaults. Collected rather than
        // read once so that Settings and Chat cannot drift apart while both are alive.
        viewModelScope.launch {
            appPreferences.selectedModels.collect { overrides ->
                _uiState.update { state ->
                    state.copy(
                        providers = state.providers.mapValues { (pid, ps) ->
                            ps.copy(selectedModel = overrides[pid.name] ?: pid.defaultModel)
                        },
                    )
                }
            }
        }
    }

    /**
     * Readback on or off.
     *
     * No local state update, for the reason [setDeveloperMode] documents: `AppPreferences` is the
     * single source of truth and the collector above pushes the value back. Writing it in both
     * places is how a switch ends up disagreeing with the rest of the app.
     */
    fun setTtsEnabled(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setTtsEnabled(enabled) }
    }

    /**
     * Re-read the history size.
     *
     * **Called on every resume, not once on open.** This screen has a button into the history
     * list, and `navigate()` leaves it — and this ViewModel — on the back stack. Delete
     * everything, press back, and a figure read only in `init` would still be showing the old
     * size. Two different numbers for the same thing is worse than no number
     * (`PROJECT_STATE.md` §10 entry 6). A file-length read is cheap enough to repeat.
     */
    fun refreshHistoryStorage() {
        viewModelScope.launch {
            val bytes = sessions.storageBytes()
            _uiState.update { it.copy(historyStorage = StorageSize.format(bytes)) }
        }
    }

    fun setDeveloperMode(enabled: Boolean) {
        // No local state update here: AppPreferences is the single source of truth and the
        // collector above pushes the new value back. Writing it in both places is how a
        // switch ends up disagreeing with what the rest of the app believes.
        viewModelScope.launch { appPreferences.setDeveloperMode(enabled) }
    }

    private fun loadKeys() {
        viewModelScope.launch {
            val keys = keyStore.getAllKeys()
            _uiState.update { state ->
                state.copy(
                    providers = state.providers.mapValues { (pid, ps) ->
                        val stored = keys[pid.name].orEmpty()
                        ps.copy(
                            apiKey = stored,
                            hasKey = stored.isNotBlank(),
                            isEnabled = stored.isNotBlank(),
                        )
                    },
                    deepgramKey = keys[DEEPGRAM_KEY_ID].orEmpty(),
                )
            }
        }
    }

    fun updateApiKey(providerId: ProviderId, key: String) {
        // Pasted keys routinely carry a trailing newline or space; an untrimmed key
        // produces an auth header that fails with a confusing 401.
        val cleaned = key.trim()

        // Update the field first and synchronously, so the text the user typed is what
        // the text field shows. The store write follows in the background.
        _uiState.update { state ->
            state.copy(
                providers = state.providers.toMutableMap().apply {
                    val current = get(providerId) ?: return@apply
                    put(providerId, current.copy(
                        apiKey = cleaned,
                        hasKey = cleaned.isNotBlank(),
                        isEnabled = cleaned.isNotBlank(),
                        isValid = null,
                        error = null,
                    ))
                }
            )
        }

        viewModelScope.launch {
            if (cleaned.isBlank()) {
                keyStore.deleteKey(providerId.name)
            } else {
                keyStore.saveKey(providerId.name, cleaned)
            }
        }
    }

    /**
     * Stores the Deepgram transcription key. Same shape as [updateApiKey] on purpose — the
     * trim is not cosmetic: a pasted key routinely carries a trailing newline, and an untrimmed
     * one produces an auth failure that reads like a wrong key.
     */
    fun updateDeepgramKey(key: String) {
        val cleaned = key.trim()
        _uiState.update { it.copy(deepgramKey = cleaned) }
        viewModelScope.launch {
            if (cleaned.isBlank()) keyStore.deleteKey(DEEPGRAM_KEY_ID)
            else keyStore.saveKey(DEEPGRAM_KEY_ID, cleaned)
        }
    }

    /**
     * Stores the "about me" note.
     *
     * **Not trimmed, unlike every key field on this screen.** A key is pasted and a stray newline
     * breaks the auth header; this is prose being typed, and trimming each keystroke would eat
     * the space the moment it was pressed. The cap is [AppPreferences.MAX_PROFILE_LENGTH] and it
     * is enforced in the store, which is the only place that cannot be bypassed.
     */
    fun updateUserProfile(text: String) {
        _uiState.update { it.copy(userProfile = text) }
        viewModelScope.launch { appPreferences.setUserProfile(text) }
    }

    fun toggleProvider(providerId: ProviderId) {
        _uiState.update { state ->
            state.copy(
                providers = state.providers.toMutableMap().apply {
                    val current = get(providerId)!!
                    put(providerId, current.copy(isEnabled = !current.isEnabled))
                }
            )
        }
    }

    fun verifyProvider(providerId: ProviderId) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    providers = state.providers.toMutableMap().apply {
                        put(providerId, get(providerId)!!.copy(
                            isValidating = true,
                            error = null,
                        ))
                    }
                )
            }

            val provider = providers.values.find { it.id == providerId }
            val apiKey = keyStore.getKey(providerId.name)

            val error: AiError? = when {
                provider == null -> AiError.Unknown(
                    provider = providerId,
                    status = null,
                    detail = "Provider not found",
                )
                apiKey == null -> AiError.MissingKey(providerId)
                else -> provider.verify(apiKey)
            }

            _uiState.update { state ->
                state.copy(
                    providers = state.providers.toMutableMap().apply {
                        put(providerId, get(providerId)!!.copy(
                            isValidating = false,
                            isValid = error == null,
                            error = error,
                        ))
                    }
                )
            }
        }
    }

    /**
     * Switches the whole app's locale and persists it.
     *
     * minSdk 26 rules out the platform per-app language API (API 33+), so the switch runs
     * through [AppCompatDelegate.setApplicationLocales] — which is why MainActivity hosts
     * AppCompat. The delegate stores the choice, recreates the activities with the new
     * configuration, and applies it again on cold start.
     */
    fun setLanguage(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }

    /**
     * Hand the choice back to the device — **and this is the fix, not the feature.**
     *
     * The app already follows the phone's language on a fresh install: `values/` is Arabic and
     * `values-en/` overrides it, so an English phone opens in English with nothing configured.
     * But [setLanguage] writes a choice that **outlives the device's own setting for good**, and
     * until now there was no way back. A user who tapped «الإنجليزية» once to see what it looked
     * like had permanently overridden their phone.
     *
     * `PROJECT_STATE.md` §10 entry 26: an escape hatch that does not exist is a trap. The empty
     * locale list is AppCompat's own "no override" value.
     */
    fun followSystemLanguage() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    fun updateModel(providerId: ProviderId, model: String) {
        // Echo the keystroke immediately so the field is editable, then persist. Without the
        // local update the collector above would fight the user's typing: every character
        // would be trimmed and round-tripped through disk before it could come back.
        _uiState.update { state ->
            state.copy(
                providers = state.providers.toMutableMap().apply {
                    put(providerId, get(providerId)!!.copy(selectedModel = model))
                }
            )
        }

        // This write is what makes the field mean anything. Previously it existed only in
        // this ViewModel's memory, so ChatViewModel never saw it and the value was discarded
        // when the screen closed.
        viewModelScope.launch { appPreferences.setSelectedModel(providerId, model) }
    }

    companion object {
        /**
         * The [EncryptedKeyStore] id for the Deepgram key. A constant, in one place, because
         * the store is keyed by free-form strings — a typo here and in the reader would store
         * the key under one name and look for it under another, and both halves would compile.
         * `ProviderId.defaultModel` exists for the same reason (`2026-08-03-A`).
         */
        const val DEEPGRAM_KEY_ID = "DEEPGRAM"
    }
}
