package com.braining.core.data.store

import android.content.Context
import android.content.SharedPreferences
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.store.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plain [SharedPreferences]. No DataStore, no new dependency.
 *
 * A handful of booleans does not justify adding a library to a build that has previously
 * been broken by dependency changes and is pinned to an exact, hard-won toolchain. The
 * interface is the seam: if these preferences ever grow into something DataStore is
 * actually better at, only this file changes.
 *
 * Like [EncryptedKeyStoreImpl], this class must not throw — a preferences file that cannot
 * be read should cost the user a toggle, not the app.
 */
@Singleton
class AppPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppPreferences {

    // Reads the preferences file. This is a handful of bytes and the platform caches the
    // result process-wide, so doing it once at construction is cheap — but it IS disk I/O on
    // whatever thread Hilt builds the graph on, which is why nothing here is allowed to throw.
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    private val _developerMode = MutableStateFlow(
        runCatching { prefs.getBoolean(KEY_DEVELOPER_MODE, false) }.getOrDefault(false),
    )

    override val developerMode: Flow<Boolean> = _developerMode.asStateFlow()

    private val _selectedModels = MutableStateFlow(readSelectedModels())

    override val selectedModels: Flow<Map<String, String>> = _selectedModels.asStateFlow()

    private val _userProfile = MutableStateFlow(
        runCatching { prefs.getString(KEY_USER_PROFILE, "") }.getOrNull().orEmpty(),
    )

    override val userProfile: Flow<String> = _userProfile.asStateFlow()

    private val _selectedProvider = MutableStateFlow(
        runCatching { prefs.getString(KEY_SELECTED_PROVIDER, null) }.getOrNull(),
    )

    override val selectedProvider: Flow<String?> = _selectedProvider.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(
        runCatching { prefs.getBoolean(KEY_TTS_ENABLED, false) }.getOrDefault(false),
    )

    override val ttsEnabled: Flow<Boolean> = _ttsEnabled.asStateFlow()

    private val _onboardingDismissed = MutableStateFlow(
        runCatching { prefs.getBoolean(KEY_ONBOARDING_DISMISSED, false) }.getOrDefault(false),
    )

    override val onboardingDismissed: Flow<Boolean> = _onboardingDismissed.asStateFlow()

    private val _ollamaUrl = MutableStateFlow(
        runCatching { prefs.getString(KEY_OLLAMA_URL, "") }.getOrNull().orEmpty(),
    )

    override val ollamaUrl: Flow<String> = _ollamaUrl.asStateFlow()

    private val _ollamaTunnel = MutableStateFlow(
        runCatching { prefs.getBoolean(KEY_OLLAMA_TUNNEL, false) }.getOrDefault(false),
    )

    override val ollamaTunnel: Flow<Boolean> = _ollamaTunnel.asStateFlow()

    override suspend fun setDeveloperMode(enabled: Boolean) {
        // State first so the switch in Settings answers immediately; the write follows.
        _developerMode.value = enabled
        withContext(Dispatchers.IO) {
            runCatching { prefs.edit().putBoolean(KEY_DEVELOPER_MODE, enabled).apply() }
        }
    }

    override suspend fun setSelectedModel(providerId: ProviderId, model: String) {
        val cleaned = model.trim()
        val key = modelKey(providerId)

        // A blank field means "use the default", which is an absence, not an empty string —
        // otherwise clearing the field would pin the provider to a model named "" and every
        // request would fail with an obscure 404 from the vendor.
        _selectedModels.value = _selectedModels.value.toMutableMap().apply {
            if (cleaned.isBlank()) remove(providerId.name) else put(providerId.name, cleaned)
        }

        withContext(Dispatchers.IO) {
            runCatching {
                val editor = prefs.edit()
                if (cleaned.isBlank()) editor.remove(key) else editor.putString(key, cleaned)
                editor.apply()
            }
        }
    }

    /**
     * Stored **verbatim**, only capped. See the interface's KDoc: a trim here would eat the
     * space the user just typed, because the collector in Settings would round-trip the
     * trimmed value straight back into the field they are still typing in.
     */
    override suspend fun setUserProfile(text: String) {
        val capped = text.take(AppPreferences.MAX_PROFILE_LENGTH)
        _userProfile.value = capped
        withContext(Dispatchers.IO) {
            runCatching {
                val editor = prefs.edit()
                if (capped.isBlank()) editor.remove(KEY_USER_PROFILE) else editor.putString(KEY_USER_PROFILE, capped)
                editor.apply()
            }
        }
    }

    override suspend fun setTtsEnabled(enabled: Boolean) {
        _ttsEnabled.value = enabled
        withContext(Dispatchers.IO) {
            runCatching { prefs.edit().putBoolean(KEY_TTS_ENABLED, enabled).apply() }
        }
    }

    override suspend fun setOnboardingDismissed(dismissed: Boolean) {
        _onboardingDismissed.value = dismissed
        withContext(Dispatchers.IO) {
            runCatching { prefs.edit().putBoolean(KEY_ONBOARDING_DISMISSED, dismissed).apply() }
        }
    }

    override suspend fun setOllamaTunnel(enabled: Boolean) {
        _ollamaTunnel.value = enabled
        withContext(Dispatchers.IO) {
            runCatching { prefs.edit().putBoolean(KEY_OLLAMA_TUNNEL, enabled).apply() }
        }
    }

    override suspend fun setOllamaUrl(url: String) {
        // Stored verbatim, exactly as typed. `LocalEndpoint` validates on read — normalising
        // here would round-trip a rewritten value back into a field the user is mid-way through
        // typing, which is the trap `setUserProfile` and `setSelectedModel` both document.
        _ollamaUrl.value = url
        withContext(Dispatchers.IO) {
            runCatching {
                val editor = prefs.edit()
                if (url.isBlank()) editor.remove(KEY_OLLAMA_URL) else editor.putString(KEY_OLLAMA_URL, url)
                editor.apply()
            }
        }
    }

    override suspend fun setSelectedProvider(providerId: ProviderId) {
        _selectedProvider.value = providerId.name
        withContext(Dispatchers.IO) {
            runCatching { prefs.edit().putString(KEY_SELECTED_PROVIDER, providerId.name).apply() }
        }
    }

    private fun readSelectedModels(): Map<String, String> = runCatching {
        ProviderId.entries.mapNotNull { pid ->
            prefs.getString(modelKey(pid), null)
                ?.takeIf { it.isNotBlank() }
                ?.let { pid.name to it }
        }.toMap()
    }.getOrDefault(emptyMap())

    private fun modelKey(providerId: ProviderId) = "$KEY_MODEL_PREFIX${providerId.name}"

    private companion object {
        const val PREFS_FILE = "braining_app_prefs"
        const val KEY_DEVELOPER_MODE = "developer_mode"
        const val KEY_MODEL_PREFIX = "model_"
        const val KEY_USER_PROFILE = "user_profile"
        const val KEY_SELECTED_PROVIDER = "selected_provider"
        const val KEY_TTS_ENABLED = "tts_enabled"
        const val KEY_ONBOARDING_DISMISSED = "onboarding_dismissed"
        const val KEY_OLLAMA_URL = "ollama_url"
        const val KEY_OLLAMA_TUNNEL = "ollama_tunnel"
    }
}
