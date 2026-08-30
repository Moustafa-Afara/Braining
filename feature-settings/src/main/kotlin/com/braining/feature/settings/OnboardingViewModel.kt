package com.braining.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.provider.AiProvider
import com.braining.core.domain.store.AppPreferences
import com.braining.core.domain.store.EncryptedKeyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    /**
     * Which provider the newcomer is setting up.
     *
     * Defaults to Gemini on the standing ruling — `ANSWERS.md` Part 3 §B: it is the free-tier
     * path, and free-tier onboarding is what makes distribution to friends actually work. The
     * 2026-08-03 amendment to that ruling is why the screen also names the regional refusal in
     * plain Arabic instead of letting a friend meet a bare `HTTP 400`.
     */
    val provider: ProviderId = ProviderId.GEMINI,
    val key: String = "",
    val verifying: Boolean = false,
    /** Null = not tried yet. The three states are distinct and the screen shows all three. */
    val verified: Boolean? = null,
    val error: AiError? = null,
)

/**
 * The first-run flow — `ANSWERS.md` Part 3 §A and Part 11 §K4.
 *
 * **Separate from `SettingsViewModel` rather than a mode of it.** Settings manages four providers
 * at once, a Deepgram key, a language, a note, a switch and a licence; onboarding does one thing
 * for one provider. Folding them together would put an `if (onboarding)` through every branch of
 * a screen that is already the largest in the app.
 *
 * It writes to the same `EncryptedKeyStore`, so a key entered here **is** the key Settings shows.
 * There is no separate copy and no hand-off step to forget.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val keyStore: EncryptedKeyStore,
    private val appPreferences: AppPreferences,
    private val providers: Map<String, @JvmSuppressWildcards AiProvider>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectProvider(providerId: ProviderId) {
        // The key field is cleared with the provider. Leaving a Gemini key in the box after
        // switching to Anthropic would offer to verify one provider's key against another's
        // endpoint, and the 401 that came back would read as "your key is wrong".
        _uiState.update {
            it.copy(provider = providerId, key = "", verified = null, error = null)
        }
    }

    fun updateKey(text: String) {
        // Trimmed like every key field in this app: a pasted key routinely carries a trailing
        // newline, and an untrimmed one fails auth in a way that reads as a wrong key.
        _uiState.update { it.copy(key = text.trim(), verified = null, error = null) }
    }

    /**
     * Store the key and ask the provider whether it works.
     *
     * **Stored before it is verified, deliberately.** A user who types a good key and loses the
     * network should not have to type it again; the verification tells them whether it works, it
     * does not decide whether it is kept.
     */
    fun verify() {
        val state = _uiState.value
        val key = state.key.trim()
        if (key.isEmpty() || state.verifying) return

        _uiState.update { it.copy(verifying = true, error = null, verified = null) }
        viewModelScope.launch {
            keyStore.saveKey(state.provider.name, key)
            val provider = providers.values.find { it.id == state.provider }
            val error = provider?.verify(key)
                ?: AiError.Unknown(state.provider, status = null, detail = "Provider not found")
            _uiState.update {
                it.copy(verifying = false, verified = error == null, error = error)
            }
        }
    }

    /**
     * Leave the flow, whether the user finished it or skipped it.
     *
     * **Both routes call this, and that is the design.** `PROJECT_STATE.md` §10 entry 26: an
     * escape hatch removed without a replacement is a trap, and an onboarding screen a user
     * cannot leave is the worst version of one. Skipping is not a failure state — it is someone
     * who wants to look around first.
     */
    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            appPreferences.setOnboardingDismissed(true)
            onDone()
        }
    }
}
