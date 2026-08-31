package com.braining.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braining.core.domain.model.ProviderId
import com.braining.core.ui.components.PrimaryButton
import com.braining.core.ui.components.QuietButton
import com.braining.core.ui.components.TonalButton
import com.braining.core.ui.error.KeyFixNotice
import com.braining.core.ui.error.toUserMessage

/**
 * The first thing a brand-new user sees.
 *
 * **It exists because an APK a friend cannot set up is not a product** — `ANSWERS.md` Part 3 §A
 * makes the newcomer a first-class goal and Part 11 §K4 puts this in the shipped build rather
 * than in a sixth milestone.
 *
 * Three things it must do, and each is a line on screen rather than an intention:
 * say what the app needs, say where to get it, and let the user leave.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onDone: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                // The app's own name, from `:core-ui`. It follows the device's language, so the
                // greeting cannot drift from what is written under the launcher icon.
                text = stringResource(
                    R.string.onboarding_title,
                    stringResource(com.braining.core.ui.R.string.app_name),
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.onboarding_what),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.onboarding_why_key),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // The provider choice. Gemini first and labelled as the free start —
            // `ANSWERS.md` Part 3 §B.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProviderId.entries.forEach { pid ->
                    FilterChip(
                        selected = state.provider == pid,
                        onClick = { viewModel.selectProvider(pid) },
                        label = { Text(pid.displayName) },
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(whereToGetKey(state.provider)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // **The regional refusal is stated up front, not discovered.** The owner's
                    // own location gets `HTTP 400 — User location is not supported` from Gemini
                    // (`PROJECT_STATE.md` §6), and every friend in his country will meet it. The
                    // 2026-08-03 amendment to Part 3 §B requires it to be said plainly in Arabic
                    // with another provider named — saying it *before* the failure is cheaper
                    // still.
                    if (state.provider == ProviderId.GEMINI) {
                        Text(
                            text = stringResource(R.string.onboarding_gemini_region),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }

                    // The same component Settings shows, on purpose. §10 entry 47: a capability
                    // that lives on one screen is a capability on one screen — and onboarding is
                    // where a friend meets this question *first*, before they have any idea that
                    // Settings exists.
                    ProviderKeyGuide(state.provider)

                    OutlinedTextField(
                        value = state.key,
                        onValueChange = viewModel::updateKey,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.settings_label_api_key)) },
                        // A key is a secret being typed in a room with other people in it.
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    )

                    // **The most valuable four lines on this screen.** A newcomer whose paste
                    // inserted an em dash would otherwise meet «حدث خطأ غير متوقّع» on their
                    // first attempt at the app and never come back — which is what happened to
                    // the owner on 2026-08-30, and he had Developer Mode and two days.
                    KeyFixNotice(state.keyFixes)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TonalButton(
                            onClick = viewModel::verify,
                            enabled = state.key.isNotBlank() && !state.verifying,
                        ) {
                            Text(stringResource(R.string.settings_action_verify))
                        }
                        if (state.verifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(4.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    }

                    // Three outcomes, three sentences. A verification whose success and failure
                    // look the same is not a verification (`PROJECT_STATE.md` §10 entry 7).
                    when {
                        state.verified == true -> Text(
                            text = stringResource(R.string.settings_key_valid),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        state.error != null -> Text(
                            text = state.error!!.toUserMessage(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.onboarding_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PrimaryButton(
                    onClick = { viewModel.finish(onDone) },
                    enabled = true,
                ) {
                    Text(stringResource(R.string.onboarding_start))
                }
                // **Always enabled.** §10 entry 26 — the way out is not a reward for finishing.
                QuietButton(onClick = { viewModel.finish(onDone) }) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }
        }
    }
}

/**
 * Where this provider's key comes from.
 *
 * One string per provider rather than one sentence with the name substituted in, because the
 * places genuinely differ — a console, a dashboard, a platform page — and a generic sentence
 * would send a newcomer looking for something that is not there.
 */
private fun whereToGetKey(provider: ProviderId): Int = when (provider) {
    ProviderId.GEMINI -> R.string.onboarding_where_gemini
    ProviderId.ANTHROPIC -> R.string.onboarding_where_anthropic
    ProviderId.OPENAI -> R.string.onboarding_where_openai
    ProviderId.DEEPSEEK -> R.string.onboarding_where_deepseek
}
