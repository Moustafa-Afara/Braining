package com.braining.feature.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.provider.ProviderGuide
import com.braining.core.ui.text.BidiText

/**
 * «كيف أحصل على المفتاح؟» — the answer, under the field that asks for it.
 *
 * ## The question this removes from the owner's inbox
 *
 * The APK goes to friends (`ANSWERS.md` Part 3 §A), and a friend opening Settings meets four
 * cards each demanding an API key. They do not know what one is, where it lives, which of the
 * four is free, or why the free one might refuse them. **Every one of them asks the owner**, and
 * he answers the same four questions by hand, forever.
 *
 * ## Collapsed by default, and that is the whole design
 *
 * Four expanded help panels would bury the settings they are attached to, and the person who
 * needs the help reads it **once**. So it costs one line until it is asked for. §10 entry 40 is
 * the same lesson from the other direction: a screen that shows everything shows nothing.
 *
 * ## What it says, and what it refuses to say
 *
 * Steps, requirements, the shape of the key, and — for Gemini — the regional refusal, stated
 * **before** the user spends ten minutes getting a key that will not work for them.
 *
 * **No prices and no quota numbers.** A figure compiled into an APK does not become old, it
 * becomes **wrong**, and wrong about money. Anything carrying a number is one tap away on the
 * vendor's own page, which is always current by construction. See [ProviderGuide].
 *
 * The «copy it now» line is there because all four vendors show a new key exactly once, and the
 * mistake is unrecoverable in the sense that matters: the user must go back and make another one,
 * having already closed the only page that could have told them so.
 */
@Composable
fun ProviderKeyGuide(
    providerId: ProviderId,
    modifier: Modifier = Modifier,
) {
    val guide = ProviderGuide.of(providerId)
    var expanded by remember(providerId) { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        TextButton(
            onClick = { expanded = !expanded },
            contentPadding = PaddingValues(0.dp),
        ) {
            // The icon shows the CURRENT state's action, and the label follows it. The same rule
            // the key-visibility eye was corrected to obey in M4.
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            BidiText(
                text = stringResource(
                    if (expanded) R.string.key_guide_close else R.string.key_guide_open,
                ),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        if (expanded) {
            Column(
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BidiText(
                    text = stringResource(stepsFor(providerId)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                BidiText(
                    text = stringResource(
                        if (guide.freeTier) R.string.key_guide_free else R.string.key_guide_paid,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (guide.freeTier) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )

                // **Said before the key is fetched, not after it fails.** `ANSWERS.md` Part 3 §B as
                // amended 2026-08-03: the regional refusal must be stated plainly in Arabic. Ten
                // minutes creating a Google account is a poor way to learn it.
                if (guide.regionLimited) {
                    BidiText(
                        text = stringResource(R.string.key_guide_region),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }

                // **The fix for this line is in the string, not here.** An Arabic sentence
                // ending in `sk-ant-` renders as «-sk-ant» without help: the letters form an LTR
                // run, but the *trailing hyphen* is a neutral at the end of an RTL paragraph, so
                // it takes the paragraph's direction and reorders to the far left. The Arabic
                // resource wraps the placeholder in FSI…PDI. `forced = Ltr` would be the wrong
                // tool — it belongs on a whole line that IS a URL or a JSON body, not on Arabic
                // prose that happens to quote one.
                BidiText(
                    text = stringResource(R.string.key_guide_prefix, guide.keyPrefix),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                BidiText(
                    text = stringResource(R.string.key_guide_copy_once),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OpenLinkButton(url = guide.keyUrl, label = R.string.key_guide_button)
                    OpenLinkButton(url = guide.docsUrl, label = R.string.key_guide_docs)
                }
            }
        }
    }
}

/**
 * Opens [url] in whatever browser the phone has.
 *
 * `runCatching` around the launch, not because a browser is likely to be missing, but because
 * `startActivity` throws [ActivityNotFoundException] when it is — and a help button that crashes
 * the app is a worse outcome than a help button that does nothing. `FLAG_ACTIVITY_NEW_TASK` is
 * required because the context here may not be an Activity.
 *
 * No `<queries>` entry is needed: web intents are exempt from the package-visibility filtering
 * Android 11 introduced.
 */
@Composable
private fun OpenLinkButton(url: String, label: Int) {
    val context = LocalContext.current
    TextButton(
        onClick = {
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        },
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
    ) {
        Icon(
            // **AutoMirrored, not Filled.** This arrow points out of the page, so in an RTL
            // layout it has to point the other way — an arrow aimed at the wrong edge of an
            // Arabic screen reads as "go back". The compiler deprecated the non-mirrored one for
            // exactly this reason, and this app is Arabic first.
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        BidiText(
            text = stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

/**
 * One string per provider rather than one sentence with the name substituted in.
 *
 * The places genuinely differ — a studio, a console, a platform, a dashboard — and each names its
 * button something else. A generic sentence would send a newcomer looking for a control that is
 * not there, which is the specific failure this whole component exists to prevent.
 */
private fun stepsFor(provider: ProviderId): Int = when (provider) {
    ProviderId.GEMINI -> R.string.key_guide_gemini
    ProviderId.ANTHROPIC -> R.string.key_guide_anthropic
    ProviderId.OPENAI -> R.string.key_guide_openai
    ProviderId.DEEPSEEK -> R.string.key_guide_deepseek
    ProviderId.OLLAMA -> R.string.key_guide_ollama
    ProviderId.OPENROUTER -> R.string.key_guide_openrouter
}
