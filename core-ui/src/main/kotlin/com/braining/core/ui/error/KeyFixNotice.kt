package com.braining.core.ui.error

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.braining.core.domain.text.ApiKeySanitizer
import com.braining.core.ui.R
import com.braining.core.ui.text.BidiText

/**
 * Tells the user what was repaired in the key they just pasted — and what could not be.
 *
 * ## Why a repaired key still gets a sentence
 *
 * The app could have cleaned the key and said nothing; it would have worked. It does not, for the
 * reason `PROJECT_STATE.md` §10 entry 14 keeps arriving at from different directions: **a value
 * silently rewritten is a value the user cannot reason about.** If the key still fails after the
 * repair, someone who was never told it had been changed will spend their time on the wrong
 * question.
 *
 * There is a second reason, and it matters more for the friends this APK is meant for: **the
 * damage will happen again.** Someone told "the dash in your key was wrong" learns something
 * about copying credentials. Someone who is told nothing learns nothing and pastes the same
 * broken key into the next app.
 *
 * ## The two colours are two different situations
 *
 * A **repair** is good news in a neutral tone: it is already fixed and the key is probably fine.
 * A **suspicious character** is an error: nothing was changed, the key almost certainly will not
 * work, and only the user can fix it — so it is red and it names the position.
 *
 * Renders nothing when the key was clean, which is the overwhelmingly common case.
 */
@Composable
fun KeyFixNotice(fixes: List<ApiKeySanitizer.Fix>, modifier: Modifier = Modifier) {
    if (fixes.isEmpty()) return

    val repaired = fixes.count { it !is ApiKeySanitizer.Fix.Suspicious }
    val suspicious = fixes.filterIsInstance<ApiKeySanitizer.Fix.Suspicious>()

    Column(modifier = modifier.padding(top = 4.dp)) {
        if (repaired > 0) {
            // The first replaced character is named, because "3 characters were fixed" is a
            // report and «كانت شرطة طويلة (—)» is an explanation.
            val sample = fixes.filterIsInstance<ApiKeySanitizer.Fix.Replaced>().firstOrNull()
            BidiText(
                text = if (sample != null) {
                    stringResource(
                        R.string.key_fix_repaired_sample,
                        repaired,
                        sample.from.toString(),
                    )
                } else {
                    stringResource(R.string.key_fix_repaired_invisible, repaired)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        if (suspicious.isNotEmpty()) {
            BidiText(
                text = stringResource(
                    R.string.key_fix_suspicious,
                    suspicious.size,
                    suspicious.first().at + 1,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
