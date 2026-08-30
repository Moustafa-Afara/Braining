package com.braining.core.ui.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.braining.core.domain.model.SttError
import com.braining.core.ui.R

/**
 * Resolves a classified [SttError] into a user-facing sentence, from string resources.
 *
 * Deliberately the twin of `AiErrorMessage.kt`, in the same package, for the same reason: the
 * domain classifies, the UI phrases. `docs/M2_DESIGN_NOTE.md` §6 warns that rebuilding English
 * strings in the data layer would undo A3 within a day of finishing it — this file is what
 * stops that.
 *
 * Every sentence tells the user what to *do*. A recognition failure is almost always
 * recoverable — grant the permission, install the language, connect to a network, speak again
 * — so "an error occurred" would be a failure of this function, not of the engine.
 */
@Composable
fun SttError.toUserMessage(): String = when (this) {
    SttError.PermissionDenied -> stringResource(R.string.error_speech_permission_denied)
    SttError.NoEngine -> stringResource(R.string.error_speech_no_engine)
    SttError.NoSpeechDetected -> stringResource(R.string.error_speech_no_speech)
    SttError.MissingKey -> stringResource(R.string.error_speech_missing_key)
    SttError.InvalidKey -> stringResource(R.string.error_speech_invalid_key)
    SttError.NetworkRequired -> stringResource(R.string.error_speech_network_required)
    // The code is shown because the two causes need different remedies and look identical
    // otherwise: 12 means the engine did not accept the language tag, 13 means it knows the
    // language but cannot use it right now. Only reached after every fallback has failed.
    is SttError.LanguageUnavailable ->
        stringResource(R.string.error_speech_language_unavailable, languageTag, code)
    is SttError.EngineFailure -> stringResource(R.string.error_speech_engine_failure, code)
}
