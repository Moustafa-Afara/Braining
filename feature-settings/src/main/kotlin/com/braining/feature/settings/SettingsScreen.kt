package com.braining.feature.settings

import com.braining.core.ui.error.ProviderErrorDetail
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.model.ProviderState
import com.braining.core.domain.store.AppPreferences
import com.braining.core.ui.components.PrimaryButton
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.braining.core.domain.text.StorageSize
import com.braining.core.ui.components.TonalButton
import com.braining.core.ui.error.toUserMessage
import com.braining.core.ui.text.BidiDirection
import com.braining.core.ui.text.BidiText
import com.braining.core.ui.text.ProvideBidiDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    /**
     * **Added in M5, closing a `PROJECT_STATE.md` §9 item.** The screen had no back button at
     * all: the only way out was the system gesture, which on this device is the same edge swipe
     * several launchers claim. A screen with no visible exit is the same defect as an unlabelled
     * control (§10 entry 26) — the way out existed and had to be guessed at.
     */
    onBack: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // The effective app language: AppCompatDelegate.setApplicationLocales overrides the
    // configuration, so the active locale is the source of truth for the toggle.
    val appLanguage = LocalConfiguration.current.locales[0].language

    // **Whether the user has overridden the device at all**, which the resolved locale alone
    // cannot say: Arabic-because-the-phone-is-Arabic and Arabic-because-I-chose-it look
    // identical from the configuration. An empty list is AppCompat's own "no override".
    //
    // Read directly rather than collected: changing it recreates the activity, so there is no
    // window in which this value can be stale while the screen is alive.
    val followsSystem = AppCompatDelegate.getApplicationLocales().isEmpty

    // The history size is read here rather than only in `init`: this screen stays on the back
    // stack while the user is in the history list deleting things, and would otherwise come back
    // showing a number that is no longer true.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshHistoryStorage() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // AutoMirrored — hard constraint 6: directional icons mirror in RTL.
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Developer Mode sits first because it was previously last, below four provider
            // cards and the GitHub stub (since removed) — roughly 1200dp of content in an
            // ~840dp viewport, with nothing on screen to suggest anything followed. He reported the
            // feature as missing entirely. A diagnostic nobody can find is not shipped.
            //
            // This trades against onboarding: `ANSWERS.md` Part 3 wants a first-run flow that
            // greets a new user with provider setup, not a debug switch. Revisit when that
            // flow is built in M5 — the right answer then is probably a collapsed "advanced"
            // section, not a return to burying it.
            DeveloperModeCard(
                enabled = uiState.developerMode,
                onToggle = viewModel::setDeveloperMode,
            )

            // Arabic ⇄ English in-app switch (M1 checklist). minSdk 26 has no platform
            // per-app language API, so the app hosts AppCompat and switches via
            // AppCompatDelegate.setApplicationLocales; the activity recreates on change.
            LanguageCard(
                appLanguage = appLanguage,
                followsSystem = followsSystem,
                onSelect = viewModel::setLanguage,
                onFollowSystem = viewModel::followSystemLanguage,
            )

            SpeechKeyCard(
                apiKey = uiState.deepgramKey,
                onKeyChange = viewModel::updateDeepgramKey,
            )

            AboutMeCard(
                text = uiState.userProfile,
                onTextChange = viewModel::updateUserProfile,
            )

            // M5. History and readback sit next to the note deliberately: all three are about
            // what the app remembers and what it does with it, and a user asking "what does it
            // keep about me" should find the answer in one place rather than three.
            HistoryCard(
                storage = uiState.historyStorage,
                onOpen = onNavigateToHistory,
            )

            ReadbackCard(
                enabled = uiState.ttsEnabled,
                onToggle = viewModel::setTtsEnabled,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.settings_providers_heading),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.settings_providers_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Gemini first (free tier recommendation)
            val geminiState = uiState.providers[ProviderId.GEMINI]
            if (geminiState != null) {
                ProviderCard(
                    state = geminiState,
                    developerMode = uiState.developerMode,
                    recommendation = stringResource(R.string.settings_gemini_recommendation),
                    onKeyChange = { viewModel.updateApiKey(ProviderId.GEMINI, it) },
                    onToggle = { viewModel.toggleProvider(ProviderId.GEMINI) },
                    onVerify = { viewModel.verifyProvider(ProviderId.GEMINI) },
                    onModelChange = { viewModel.updateModel(ProviderId.GEMINI, it) },
                )
            }

            // Other providers
            ProviderId.entries.filter { it != ProviderId.GEMINI }.forEach { pid ->
                val state = uiState.providers[pid]
                if (state != null) {
                    ProviderCard(
                        state = state,
                        developerMode = uiState.developerMode,
                        onKeyChange = { viewModel.updateApiKey(pid, it) },
                        onToggle = { viewModel.toggleProvider(pid) },
                        onVerify = { viewModel.verifyProvider(pid) },
                        onModelChange = { viewModel.updateModel(pid, it) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FontLicenceCard()
        }
    }
}

/**
 * Developer Mode switch (`ANSWERS.md` Part 2 §9).
 *
 * The description states plainly that keys are redacted, because a feature that prints
 * outgoing requests must say what it does not print.
 */
@Composable
private fun DeveloperModeCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_developer_heading),
                style = MaterialTheme.typography.titleSmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.settings_developer_label),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
            Text(
                text = stringResource(R.string.settings_developer_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Arabic ⇄ English switch. Two explicit rows rather than a single toggle so the current
 * language is always visible; switching recreates the activity with the new locale, so
 * the whole app flips immediately (and the layout direction with it).
 */
@Composable
private fun LanguageCard(
    appLanguage: String,
    followsSystem: Boolean,
    onSelect: (String) -> Unit,
    onFollowSystem: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_language_heading),
                style = MaterialTheme.typography.titleSmall,
            )
            // **First, and it is the default.** A user who never touches this card is
            // following their phone; the row says so instead of leaving them to infer it from
            // which of the other two happens to be ticked.
            LanguageRow(
                label = stringResource(R.string.settings_language_system),
                selected = followsSystem,
                onClick = onFollowSystem,
            )
            LanguageRow(
                label = stringResource(R.string.settings_language_arabic),
                selected = !followsSystem && appLanguage == "ar",
                onClick = { onSelect("ar") },
            )
            LanguageRow(
                label = stringResource(R.string.settings_language_english),
                selected = !followsSystem && appLanguage == "en",
                onClick = { onSelect("en") },
            )
        }
    }
}

@Composable
private fun LanguageRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * The bundled font's licence notice.
 *
 * **This is a licence obligation, not a courtesy.** IBM Plex Sans Arabic ships under the SIL
 * Open Font License, which requires the copyright notice and licence text to travel with the
 * binary — and `ANSWERS.md` Part 3 makes handing the APK to a friend a first-class goal, so the
 * APK is a distribution. Ordered by the owner in Part 6 §M2-7.
 *
 * The text lives in `res/raw/`, not `res/font/`: AAPT2 accepts only font resources there and
 * rejects the filename besides (`2026-08-04-F`). It is read on demand rather than held in a
 * string resource, because 4 KB of licence has no business in every locale's string table.
 */
@Composable
private fun FontLicenceCard() {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Read only when opened — the file is bundled either way, which is what the licence
    // actually requires; showing it is what makes the notice reachable.
    val licence = remember(expanded) {
        if (!expanded) "" else runCatching {
            context.resources.openRawResource(R.raw.ibm_plex_sans_arabic_ofl)
                .bufferedReader()
                .use { it.readText() }
        }.getOrDefault("")
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_licences_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.settings_font_attribution),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    stringResource(
                        if (expanded) R.string.settings_licence_hide else R.string.settings_licence_show,
                    ),
                )
            }
            if (expanded && licence.isNotBlank()) {
                // Forced LTR: the licence is English and the app's default direction is RTL,
                // which would otherwise push its punctuation to the wrong side. core-ui/BidiText
                // is the single place that decides direction — this is why it takes an override.
                BidiText(
                    text = licence,
                    forced = BidiDirection.Ltr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

/**
 * The Deepgram transcription key.
 *
 * Sits with Developer Mode and Language rather than among the provider cards, because it is
 * **not** a chat provider: it turns speech into text and never answers a question. Putting it in
 * that list would invite the next reader to make it a `ProviderId`, which would drag it into the
 * chat selector and the `AiProvider` multibinding map.
 *
 * No verify button yet — verifying costs a real connection, and the engine that opens it is the
 * next work unit. A tick that means nothing is worse than no tick (`2026-08-03-A`).
 */
@Composable
private fun SpeechKeyCard(
    apiKey: String,
    onKeyChange: (String) -> Unit,
) {
    var keyVisible by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_speech_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.settings_speech_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = onKeyChange,
                label = { Text(stringResource(R.string.settings_label_api_key)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            if (keyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(
                                if (keyVisible) R.string.settings_key_hide else R.string.settings_key_show,
                            ),
                        )
                    }
                },
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            // The one piece of feedback this card can honestly give today: the key is stored.
            // §9 has "Settings gives no feedback that a key was saved" open against the
            // provider cards; there is no reason to ship a fifth instance of it.
            if (apiKey.isNotBlank()) {
                Text(
                    text = stringResource(R.string.settings_speech_saved),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * The "about me" note — `ANSWERS.md` Part 8 §D3.
 *
 * **The card states where the text goes, because nobody could infer it.** This box rides on every
 * interrogation and on every forged prompt, and on no chat message at all. A control whose reach
 * is invisible is the class of thing Developer Mode exists to expose, so it is written on the card
 * rather than left to be discovered.
 *
 * The counter is not decoration either: the note costs tokens on **every** Clarify turn, so the
 * ceiling is a real constraint and the user is shown where they are against it.
 */
@Composable
private fun AboutMeCard(
    text: String,
    onTextChange: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_profile_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.settings_profile_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Through the direction provider, like every other free-text field in the app: this
            // one will hold Arabic with English words in it, which is exactly the case
            // core-ui/text/BidiText.kt exists for.
            ProvideBidiDirection(text = text) { direction ->
                OutlinedTextField(
                    value = text,
                    // Refuse the keystroke rather than accept it and drop it in the store. A
                    // field that keeps taking characters it will not keep is lying to the user.
                    onValueChange = {
                        if (it.length <= AppPreferences.MAX_PROFILE_LENGTH) onTextChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.settings_profile_placeholder)) },
                    minLines = 3,
                    maxLines = 6,
                    textStyle = LocalTextStyle.current.copy(
                        textDirection = direction.textDirection,
                        textAlign = TextAlign.Start,
                    ),
                )
            }

            Text(
                text = stringResource(
                    R.string.settings_profile_counter,
                    text.length,
                    AppPreferences.MAX_PROFILE_LENGTH,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@Composable
private fun ProviderCard(
    state: ProviderState,
    /**
     * Passed in rather than read here, because a card is not the place to know about a global
     * setting — and because this is the only thing on the card that changes with it: the
     * provider's own words under a failure the classifier could not place.
     */
    developerMode: Boolean = false,
    recommendation: String? = null,
    onKeyChange: (String) -> Unit,
    onToggle: () -> Unit,
    onVerify: () -> Unit,
    onModelChange: (String) -> Unit,
) {
    var keyVisible by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.providerId.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                if (state.isValid == true) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.settings_key_valid),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                } else if (state.isValid == false) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = stringResource(R.string.settings_key_invalid),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            recommendation?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            OutlinedTextField(
                value = state.selectedModel,
                onValueChange = onModelChange,
                label = { Text(stringResource(R.string.settings_label_model)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                // Must be bound to real state. A hard-coded "" made this a write-only
                // field: every keystroke was thrown away on recomposition, the eye
                // toggle revealed nothing, and an empty callback could wipe the
                // previously saved key.
                value = state.apiKey,
                onValueChange = onKeyChange,
                label = { Text(stringResource(R.string.settings_label_api_key)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        // The icon shows the CURRENT state, not the pending action.
                        // Showing "eye" while the text was masked read backwards to
                        // the user: open eye = the key is visible right now.
                        Icon(
                            if (keyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(
                                if (keyVisible) R.string.settings_key_hide else R.string.settings_key_show,
                            ),
                        )
                    }
                },
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PrimaryButton(
                    onClick = onVerify,
                    enabled = state.hasKey && !state.isValidating,
                    modifier = Modifier.weight(1f),
                ) {
                    if (state.isValidating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.settings_action_verify))
                    }
                }
            }

            state.error?.let {
                Text(
                    text = it.toUserMessage(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                // **This is the card the owner was standing in front of** when Gemini refused
                // his key with «حدث خطأ غير متوقّع». Developer Mode now prints the vendor's own
                // sentence underneath it, which is the only thing that separates a wrong model
                // name from an unentitled key from an exhausted quota.
                ProviderErrorDetail(it, developerMode)
            }
        }
    }
}

/**
 * History: what it is, what it costs, and the way into it.
 *
 * **The size is here rather than only in the list**, because this is the screen someone opens
 * when they are worrying about storage. `ANSWERS.md` Part 1 §10 replaced a hard cap with exactly
 * this readout, which only works if it is where the question is asked.
 */
@Composable
private fun HistoryCard(
    storage: StorageSize.Formatted,
    onOpen: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_history_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.settings_history_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.settings_history_storage,
                    storage.value,
                    stringResource(
                        when (storage.unit) {
                            StorageSize.Unit.BYTES -> R.string.settings_unit_bytes
                            StorageSize.Unit.KILOBYTES -> R.string.settings_unit_kilobytes
                            StorageSize.Unit.MEGABYTES -> R.string.settings_unit_megabytes
                            StorageSize.Unit.GIGABYTES -> R.string.settings_unit_gigabytes
                        },
                    ),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TonalButton(onClick = onOpen) {
                Text(stringResource(R.string.settings_history_open))
            }
        }
    }
}

/**
 * Readback — `ANSWERS.md` Part 1 §7 and Part 11 §K3.
 *
 * **The description says what the switch does and what it does not.** Turning it on makes a
 * button appear; it never makes the phone speak on its own. A user who reads "read answers aloud"
 * and expects silence to end is a user who turns it off again and does not come back.
 */
@Composable
private fun ReadbackCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_readback_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.settings_readback_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
