package com.braining.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.braining.ai.providers.ollama.OllamaProvider
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.net.LocalEndpoint
import com.braining.core.ui.components.TonalButton
import com.braining.core.ui.text.BidiDirection
import com.braining.core.ui.text.BidiText

/**
 * Ollama — the provider that is a computer rather than a company.
 *
 * ## Why it does not reuse `ProviderCard`
 *
 * `ProviderCard` is built around an API key: a masked field, an eye toggle, a repair notice, a
 * verify button that spends a real request. Ollama has no key at all. Forcing it through that
 * card would have meant a disabled key field and a verify button that verifies nothing —
 * a screen apologising for its own shape. What it needs instead is an **address**, a **test**,
 * and a **model picked from what the machine actually has**.
 *
 * ## The card is visible to everyone, and says why
 *
 * The owner's ruling of 2026-08-31 (`ANSWERS.md` Part 15 §O4). His friends cannot reach his
 * computer and most will never use this, but a feature hidden until it is configured is a
 * feature nobody discovers — and the one-line subtitle costs a reader two seconds while a
 * hidden card costs them the whole capability. Anyone who does not want it scrolls past.
 *
 * ## Four outcomes from one button
 *
 * `PROJECT_STATE.md` §10 entry 1: a platform error names the symptom the platform saw, not the
 * cause. "It did not work" is what sent the owner to three settings screens that were all already
 * correct. So the test distinguishes *not configured* from *not an address* from *not a private
 * address* from *nothing answered* — and the last one names its three causes in the order they
 * actually occur, because the user cannot see which of them they are in.
 */
@Composable
fun OllamaCard(
    url: String,
    probe: OllamaProvider.Probe?,
    testing: Boolean,
    selectedModel: String,
    tunnel: Boolean,
    onUrlChange: (String) -> Unit,
    onTest: () -> Unit,
    onModelSelected: (String) -> Unit,
    onTunnelChange: (Boolean) -> Unit,
) {
    val models = (probe as? OllamaProvider.Probe.Reachable)?.models.orEmpty()
    var menuOpen by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                BidiText(
                    text = stringResource(R.string.ollama_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            BidiText(
                text = stringResource(R.string.ollama_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                label = { Text(stringResource(R.string.ollama_label_address)) },
                placeholder = {
                    Text(
                        stringResource(
                            if (tunnel) R.string.ollama_hint_tunnel else R.string.ollama_hint_address,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // ── the tunnel affirmation ───────────────────────────────────────────────────
            //
            // **This is a statement by the user, not a preference.** Off, the app reaches only
            // the local network — the safe default, and the only one whose safety the app can
            // verify by itself. On, the user is saying a Tailscale tunnel exists, which is what
            // makes an address the app cannot vouch for safe to speak plainly to: the packets
            // are inside WireGuard whatever this app sends.
            //
            // The switch is here rather than buried because reaching the PC from outside the
            // house is the whole reason the owner installed Ollama, and a capability nobody can
            // find is a capability nobody has (§10 entry 47).
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BidiText(
                        text = stringResource(R.string.ollama_tunnel_title),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    BidiText(
                        text = stringResource(R.string.ollama_tunnel_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = tunnel, onCheckedChange = onTunnelChange)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TonalButton(
                    onClick = onTest,
                    // Nothing to test with an empty field, and the button must not look
                    // available while it would only produce "type an address first".
                    enabled = !testing && url.isNotBlank(),
                ) {
                    Text(
                        stringResource(
                            if (testing) R.string.ollama_testing else R.string.ollama_test,
                        ),
                    )
                }
                if (testing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }

            // The four sentences. Rendered only after a test — before that the card says nothing
            // about a connection nobody has checked.
            probe?.let { ProbeResult(it) }

            // The picker appears only when there is something to pick from: an empty dropdown is
            // a control that teaches the user their tap did nothing.
            if (models.isNotEmpty()) {
                Column {
                    BidiText(
                        text = stringResource(R.string.ollama_label_model),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = { menuOpen = true }) {
                        BidiText(
                            text = selectedModel.ifBlank { stringResource(R.string.ollama_pick_model) },
                            style = MaterialTheme.typography.labelLarge,
                            fallback = BidiDirection.Ltr,
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        models.forEach { model ->
                            DropdownMenuItem(
                                text = {
                                    // Forced LTR: a model name is an identifier, not prose, and
                                    // `qwen2.5:7b-instruct` reordered by content detection is a
                                    // name the user cannot match against their own machine.
                                    BidiText(text = model, forced = BidiDirection.Ltr)
                                },
                                onClick = {
                                    onModelSelected(model)
                                    menuOpen = false
                                },
                            )
                        }
                    }
                }
            }

            BidiText(
                text = stringResource(R.string.ollama_setup_steps),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ProviderKeyGuide(ProviderId.OLLAMA)
        }
    }
}

/**
 * One sentence per outcome, in the colour that matches what it means.
 *
 * The unreachable case is the long one on purpose: it is the commonest failure and the only one
 * whose three causes are indistinguishable from the phone. Naming them in likelihood order is the
 * difference between a user who restarts Ollama and a user who reinstalls the app.
 */
@Composable
private fun ProbeResult(probe: OllamaProvider.Probe) {
    val (text, colour) = when (probe) {
        is OllamaProvider.Probe.Reachable ->
            if (probe.models.isEmpty()) {
                stringResource(R.string.ollama_ok_empty) to MaterialTheme.colorScheme.tertiary
            } else {
                stringResource(R.string.ollama_ok, probe.models.size) to
                    MaterialTheme.colorScheme.primary
            }

        is OllamaProvider.Probe.NotConfigured ->
            stringResource(R.string.ollama_not_configured) to
                MaterialTheme.colorScheme.onSurfaceVariant

        is OllamaProvider.Probe.BadAddress -> when (val reason = probe.reason) {
            // Two different sentences, because they are two different mistakes. "Not an address"
            // sends the user back to their typing; "not a private address" has to explain that
            // the app is refusing on purpose, or they will assume it is broken and look for a
            // way around it.
            is LocalEndpoint.Result.NotPrivate ->
                stringResource(R.string.ollama_not_private, reason.host) to
                    MaterialTheme.colorScheme.error
            else ->
                stringResource(R.string.ollama_bad_address) to MaterialTheme.colorScheme.error
        }

        is OllamaProvider.Probe.Unreachable ->
            stringResource(R.string.ollama_unreachable) to MaterialTheme.colorScheme.error
    }

    BidiText(text = text, style = MaterialTheme.typography.bodySmall, color = colour)

    // The transport's own words — "connect timed out", "Connection refused" — and the single
    // line that separates a sleeping PC from a firewall refusing the port. `probe()` captures it
    // deliberately; before this it was captured and then read by nobody, which is the same
    // defect §9 carried about `AiError.Unknown.detail` for eleven days.
    //
    // Not behind Developer Mode: there is no key here to redact and no account to expose, so
    // the reason to hide it does not exist. Forced LTR — it is an English transport message.
    if (probe is OllamaProvider.Probe.Unreachable && probe.detail.isNotBlank()) {
        BidiText(
            text = probe.detail,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            forced = BidiDirection.Ltr,
        )
    }
}
