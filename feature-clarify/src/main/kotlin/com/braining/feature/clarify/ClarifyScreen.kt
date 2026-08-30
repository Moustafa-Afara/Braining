package com.braining.feature.clarify

import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.geometry.Offset
import android.content.Intent
import com.braining.core.ui.text.SpokenText
import com.braining.core.domain.speech.ReaderStatus
import com.braining.core.domain.speech.ReaderFailure
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import com.braining.core.ui.error.ProviderErrorDetail
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.braining.core.domain.clarify.ClarifyState
import com.braining.core.domain.clarify.ClarifyTurn
import com.braining.core.domain.clarify.FrameworkOption
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.routing.RouteReason
import com.braining.core.domain.text.ScriptDetector
import com.braining.core.ui.components.InsightButton
import com.braining.core.ui.components.PrimaryButton
import com.braining.core.ui.components.QuietButton
import com.braining.core.ui.components.TonalButton
import com.braining.core.ui.diagnostics.DiagnosticsPanel
import com.braining.core.ui.error.toUserMessage
import com.braining.core.ui.input.submitOnCtrlEnter
import com.braining.core.ui.routing.toUserMessage
import com.braining.core.ui.voice.MicPermissionDialog
import com.braining.core.ui.state.BrainingEmptyState
import com.braining.core.ui.state.BrainingLoadingState
import com.braining.core.ui.voice.VoiceCapturePanel
import com.braining.core.ui.text.BidiDirection
import com.braining.core.ui.text.BidiText
import com.braining.core.ui.text.ProvideBidiDirection

/**
 * The CLARIFY screen — the core stage of the product.
 *
 * `ANSWERS.md` Part 1 §9 gives Compose UI tests to **this screen and no other**, which is worth
 * knowing while editing it: it is the one place in the app where behaviour is meant to be pinned
 * by tests rather than by a device run.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClarifyScreen(
    viewModel: ClarifyViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    // `collectAsState`, matching ChatScreen and SettingsScreen. Not the lifecycle-aware variant
    // — one collection idiom across the app is worth more here than the small saving, and a
    // second idiom is the kind of drift that makes two screens behave differently for no reason
    // anyone remembers.
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val ready = uiState.state == ClarifyState.READY

    // Pure view state — it belongs to the screen, not the session, and `rememberSaveable` keeps
    // it across a rotation without adding a field to the ViewModel that means nothing to it.
    var showConversation by rememberSaveable { mutableStateOf(false) }

    // --- voice: answer the question by speaking -------------------------------------------
    val context = LocalContext.current
    var showPermissionRationale by remember { mutableStateOf(false) }
    val languageTag = LocalConfiguration.current.locales[0].language

    val micGranted = {
        context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.startVoice(languageTag) else viewModel.onMicrophonePermissionDenied()
    }

    if (showPermissionRationale) {
        MicPermissionDialog(
            onAllow = {
                showPermissionRationale = false
                permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            },
            onDismiss = { showPermissionRationale = false },
        )
    }

    // Follow the conversation down as turns land, and as the streaming turn grows.
    //
    // Guarded on `ready` because «نضجت الفكرة» clears `streaming`, which changes a key of this
    // effect at the exact moment the list is being removed from composition — scrolling a list
    // that is on its way out.
    LaunchedEffect(uiState.turns.size, uiState.streaming.length, ready) {
        if (ready) return@LaunchedEffect
        val last = uiState.turns.size
        if (last > 0 || uiState.streaming.isNotEmpty()) listState.animateScrollToItem(last)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.clarify_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // AutoMirrored: hard constraint 6 requires directional icons to mirror,
                        // and this app's default direction is RTL.
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.clarify_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // The number the gate is read against (`docs/M3_DESIGN_NOTE.md` §5.3). Behind
            // Developer Mode because it is a measurement, not something a user needs — the same
            // placement as M2's segment count, and for the same reason: every real fault in M2
            // was found by a number that had been put on screen.
            // Only the turn counter is pinned here. It is one line and can never grow, so it
            // costs the conversation nothing to keep it always visible.
            //
            // **The diagnostics panel is NOT here, and that is a fix rather than a preference.**
            // It was, until 2026-08-07: expanded, its request body is roughly a thousand
            // characters of JSON, and this header sits *above* a `weight(1f)` list — so the
            // fixed children take their height first and the list is squeezed to nothing while
            // the JSON runs off the bottom of a region that does not scroll. `ChatScreen` never
            // had the problem because its panel lives inside a message bubble, inside the lazy
            // list. Same composable, same data, one wrong parent.
            if (uiState.developerMode) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    BidiText(
                        text = stringResource(
                            R.string.clarify_dev_turns,
                            uiState.engineTurns,
                            uiState.model,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    // The gate's second number, on screen rather than on a stopwatch. Appears
                    // only once «نضجت الفكرة» has been pressed, because until then it would be
                    // counting something that has not happened yet.
                    if (uiState.clarifySeconds > 0) {
                        BidiText(
                            text = stringResource(
                                R.string.clarify_dev_timing,
                                uiState.clarifySeconds,
                                uiState.totalSeconds,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // **Why the buttons did or did not appear.**
                    //
                    // The owner reported them working "but not always" and there was no way to
                    // tell whether the model had written no options, written them in a format the
                    // parser rejects, or written them and had them eaten. Guessing produced two
                    // wrong fixes in a row. This shows the count, and when it is zero, the last
                    // line the model actually wrote — which names the format in one glance.
                    (uiState.turns.lastOrNull() as? ClarifyTurn.Question)?.let { q ->
                        BidiText(
                            text = if (q.options.isNotEmpty()) {
                                stringResource(R.string.clarify_dev_options, q.options.size)
                            } else {
                                stringResource(
                                    R.string.clarify_dev_options_none,
                                    q.text.lines().lastOrNull { it.isNotBlank() }
                                        ?.trim()
                                        ?.take(60)
                                        .orEmpty(),
                                )
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Speech failures get their own card. A microphone problem and a provider problem
            // have different remedies, and letting one overwrite the other hides the reason an
            // answer never sent — the split `2026-08-04-H` made in chat, for the same reason.
            uiState.voiceError?.let { sttError ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { viewModel.dismissVoiceError() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    BidiText(
                        text = sttError.toUserMessage(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Same typed error, same resources, same sentence as the chat shows.
                        // Clarify fails the way a provider call fails, so it must not invent a
                        // second vocabulary — that is the defect A3 removed in `2026-08-03-E`.
                        BidiText(
                            text = error.toUserMessage(),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        // §9's item, closed 2026-08-28: what the provider actually said, when
                        // the classifier could not place it. Developer Mode only, and silent
                        // when there is nothing to add.
                        ProviderErrorDetail(uiState.error, uiState.developerMode)

                        // ── who else could answer this ───────────────────────────────────
                        //
                        // The owner's ruling of 2026-08-28, reversing his own of 17 August: the
                        // app no longer switches provider by itself. It names what failed, lists
                        // the providers he holds keys for, and waits.
                        //
                        // **The list is empty when a fallback would be wrong** — a missing key, a
                        // rejected key or a dead network are the user's own setup, and
                        // `DefaultModelRouter.isRecoverable` refuses to route around them. Then
                        // this whole block disappears and only «أعد المحاولة» remains, which is
                        // the correct offer for a problem another provider cannot solve.
                        if (uiState.fallbackOptions.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            BidiText(
                                text = stringResource(R.string.clarify_fallback_prompt),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelSmall,
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(uiState.fallbackOptions) { pid ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { viewModel.chooseFallback(pid) },
                                        label = { Text(pid.displayName) },
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = { viewModel.retry() }) {
                                Text(stringResource(R.string.clarify_retry))
                            }
                            // One tap for a user who does not care which provider answers, only
                            // that one does. It takes the head of the same list the chips are
                            // built from, so it can never choose something they would not have
                            // been offered.
                            if (uiState.fallbackOptions.isNotEmpty()) {
                                TextButton(onClick = { viewModel.tryAnyFallback() }) {
                                    Text(stringResource(R.string.clarify_fallback_any))
                                }
                            }
                        }
                    }
                }
            }

            // **Once the idea is mature the conversation gets out of the way.**
            //
            // It used to keep its `weight(1f)` beside the forge panel's, so the prompt and the
            // answer had a quarter of a phone screen each. The owner's report was that he could
            // not read the answer at all — «ضيق المجال المخصص للإجابة» — and he was right: two
            // long documents sharing half a screen is not a display, it is a keyhole. The
            // interrogation has already been read by the time it ends; what the user came for is
            // the result. It stays one tap away rather than being thrown out.
            // Reading a saved run out of the database. Distinct from `forging` and `executing`
            // because it is waiting on local disk, not on a provider — a screen that said
            // «أكتب البرومبت…» while reading a file would be describing the wrong thing.
            if (uiState.restoring) {
                BrainingLoadingState(
                    label = stringResource(R.string.clarify_restoring),
                    modifier = Modifier.weight(1f),
                )
            } else if (uiState.restoreFailed) {
                // The row was deleted between opening the list and tapping it. Said plainly,
                // because the alternative — quietly starting an interrogation on the route's
                // placeholder — is a screen that looks like it is working and is asking about
                // nothing (§10 entry 13).
                BrainingEmptyState(
                    title = stringResource(R.string.clarify_restore_failed_title),
                    hint = stringResource(R.string.clarify_restore_failed_hint),
                    modifier = Modifier.weight(1f),
                )
            } else if (!ready || showConversation) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                // One root composable per `item`, deliberately. A lazy item is a single
                // measurement slot: two siblings inside one `item { }` are placed at the same
                // offset and draw on top of each other. The spacing belongs to the item, not
                // beside it.
                item {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        // **Say where this came from.** A user who opened a saved run is looking
                        // at a full interrogation and a finished prompt they did not just write.
                        // Without this line the screen appears to have invented them, which is
                        // the "label that describes what the code used to do" defect in reverse
                        // (§10 entry 14): a screen that describes a state it is not in.
                        if (uiState.fromHistory) {
                            Text(
                                text = stringResource(R.string.clarify_from_history),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                        IdeaCard(uiState.idea)
                    }
                }

                items(uiState.turns) { turn -> TurnCard(turn) }

                // The turn currently arriving. Streaming is not decoration: M1 spent two work
                // units securing genuine token-by-token delivery, and a Clarify turn is longer
                // than a chat reply.
                if (uiState.streaming.isNotEmpty()) {
                    item { StreamingCard(uiState.streaming) }
                } else if (uiState.state == ClarifyState.ANALYZING) {
                    item {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Text(
                                text = stringResource(R.string.clarify_analyzing),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Developer Mode diagnostics — **inside the scrolling list**, and last.
                //
                // Inside, because expanded it is a thousand characters of JSON and only a lazy
                // list can carry that. Last, because appending rather than prepending leaves
                // every other item's index untouched, so the auto-scroll effect above keeps
                // working without being re-derived — and because after a turn finishes this is
                // where the eye already is.
                if (uiState.developerMode) {
                    uiState.lastDiagnostics?.let { item { DiagnosticsPanel(it) } }
                }

                item { Spacer(Modifier.height(8.dp)) }
                }
            }

            // **Nothing below runs on a failed restore, and this guard is the point of the
            // flag.** Without it the controls kept rendering underneath the "session no longer
            // exists" message — inert except for «نضجت الفكرة», which is deliberately ungated and
            // would have called `forge()` on a session that was never opened. A screen that
            // *looks* stopped and still has one live destructive control is the shape of §10
            // entry 13: the failure that looks like a success.
            if (uiState.restoreFailed) {
                // Nothing. The empty state above has already said what happened, and the only
                // useful action from here is the back arrow, which the top bar already carries.
            } else if (ready) {
                TextButton(
                    onClick = { showConversation = !showConversation },
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    Text(
                        stringResource(
                            if (showConversation) {
                                R.string.clarify_hide_conversation
                            } else {
                                R.string.clarify_show_conversation
                            },
                        ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                ForgePanel(
                    uiState = uiState,
                    frameworks = viewModel.frameworks,
                    onPromptChange = viewModel::updateForgedPrompt,
                    onSwapFramework = viewModel::swapFramework,
                    onRegenerate = viewModel::regenerate,
                    onExecute = viewModel::execute,
                    onTranslate = viewModel::translate,
                    onShowOriginal = viewModel::showOriginal,
                    onRerun = viewModel::rerunWith,
                    onToggleReadback = viewModel::toggleReadback,
                    onUserScrolled = viewModel::stopFollowingSpoken,
                    onResumeFollowing = viewModel::resumeFollowingSpoken,
                    onClearPrompt = viewModel::clearPrompt,
                    onUndoClearPrompt = viewModel::undoClearPrompt,
                    // The whole screen when the conversation is hidden, half when it is not.
                    modifier = Modifier.weight(1f),
                )

                // **The feedback loop** — `BRAINING.md` §2.7. Outside the scrolling panel above,
                // so it stays reachable however long the answer is: the note is about the answer,
                // and a control that scrolls away with its subject is a control that is not there.
                if (uiState.result.isNotBlank() || uiState.executing) {
                    if (uiState.recording) {
                        VoiceCapturePanel(
                            amplitude = uiState.amplitude,
                            transcript = uiState.inputText,
                            partial = uiState.partial,
                            onDone = { viewModel.stopVoice() },
                            onCancel = { viewModel.cancelVoice() },
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            val feedbackModifier = Modifier
                                .weight(1f)
                                .submitOnCtrlEnter { viewModel.sendFeedback() }
                            ProvideBidiDirection(text = uiState.inputText) { direction ->
                                OutlinedTextField(
                                    value = uiState.inputText,
                                    onValueChange = { viewModel.updateInput(it) },
                                    modifier = feedbackModifier,
                                    placeholder = { Text(stringResource(R.string.clarify_feedback_hint)) },
                                    maxLines = 3,
                                    textStyle = LocalTextStyle.current.copy(
                                        textDirection = direction.textDirection,
                                        textAlign = TextAlign.Start,
                                    ),
                                )
                            }
                            if (uiState.voiceAvailable) {
                                IconButton(
                                    onClick = {
                                        if (micGranted()) {
                                            viewModel.startVoice(languageTag)
                                        } else {
                                            showPermissionRationale = true
                                        }
                                    },
                                ) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = stringResource(R.string.clarify_feedback_voice),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.sendFeedback() },
                                enabled = uiState.inputText.isNotBlank() &&
                                    !uiState.executing &&
                                    !uiState.translating,
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = stringResource(R.string.clarify_feedback_send),
                                )
                            }
                        }
                    }
                }
            } else {
                // The options offered by the last question, as buttons.
                //
                // **Above the field and never instead of it.** The most valuable answers in gate
                // run 1 were the ones that were on no list — the interrogation's whole value was
                // that it moved somewhere the owner had not started from. A set of choices that
                // replaced free text would have removed exactly that.
                val options = (uiState.turns.lastOrNull() as? ClarifyTurn.Question)
                    ?.options
                    .orEmpty()
                if (options.isNotEmpty() && uiState.state == ClarifyState.AWAITING_USER_DECISION) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        options.forEach { option ->
                            QuietButton(
                                onClick = { viewModel.replyWith(option) },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                // Full width and left-aligned: an option is a sentence, not a
                                // word, and a centred label wraps into something unreadable.
                                BidiText(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.clarify_options_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // **The recording panel takes the input row's place; it does not cover the page.**
                //
                // Owner's report, 2026-08-17: while dictating an answer he could not scroll the
                // conversation back to re-read the question, and the first touch that tried to
                // ended the recording. Both were the modal sheet, not the recorder — see the
                // KDoc on VoiceCapturePanel. The list above keeps its weight and its scrolling.
                if (uiState.recording) {
                    VoiceCapturePanel(
                        amplitude = uiState.amplitude,
                        transcript = uiState.inputText,
                        partial = uiState.partial,
                        onDone = { viewModel.stopVoice() },
                        onCancel = { viewModel.cancelVoice() },
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        val fieldModifier = Modifier
                            .weight(1f)
                            .submitOnCtrlEnter { viewModel.reply() }
                        ProvideBidiDirection(text = uiState.inputText) { direction ->
                            OutlinedTextField(
                                value = uiState.inputText,
                                onValueChange = { viewModel.updateInput(it) },
                                modifier = fieldModifier,
                                placeholder = { Text(stringResource(R.string.clarify_input_hint)) },
                                maxLines = 4,
                                textStyle = LocalTextStyle.current.copy(
                                    textDirection = direction.textDirection,
                                    textAlign = TextAlign.Start,
                                ),
                            )
                        }
                        // Hidden, not disabled, when the device has no engine — and hidden while a
                        // turn is streaming, where the field is not in use anyway.
                        if (uiState.voiceAvailable &&
                            uiState.state == ClarifyState.AWAITING_USER_DECISION
                        ) {
                            IconButton(
                                onClick = {
                                    if (micGranted()) {
                                        viewModel.startVoice(languageTag)
                                    } else {
                                        showPermissionRationale = true
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = stringResource(R.string.clarify_voice),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.reply() },
                            enabled = uiState.inputText.isNotBlank() &&
                                uiState.state == ClarifyState.AWAITING_USER_DECISION,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.clarify_send),
                            )
                        }
                    }

                    // The only control in the app that may reach ClarifyState.READY — and the
                    // only amber button in the app, by BRAND §2's rule that amber marks the
                    // moment of understanding and nothing routine.
                    InsightButton(
                        onClick = { viewModel.declareReady() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        // Deliberately NOT gated on the engine having finished, or on a minimum
                        // number of turns. `BRAINING.md` §2.3 gives this decision to the user
                        // without conditions; an app that decides when someone has thought enough
                        // is deciding the one thing it was told not to.
                    ) {
                        Text(stringResource(R.string.clarify_ready))
                    }
                }
            }
        }
    }
}

/**
 * The FORGE result: the framework, why it was chosen, and the English prompt — **editable**.
 *
 * `ANSWERS.md` Part 7 §M3-5 ruled this a screen every user sees, not a Developer Mode panel:
 * Part 2 §9 approved Developer Mode on the grounds that transparency is a core value of the
 * project, and a value only developers can see is not one. `docs/PROMPT_FRAMEWORKS.md` §3.7 goes
 * further and requires the framework be shown *and swappable* — hence the row of chips.
 *
 * **One scroll for the whole panel, not a scrolling box inside a fixed page.**
 *
 * It used to pin the framework, the reason and the buttons in place and give the prompt and the
 * answer a scrolling box each. The owner asked for the opposite — *"شريط الصعود والهبوط ممدد على
 * الصفحة كاملة"* — and he is right: three scroll regions on one phone screen means every gesture
 * has to be aimed, and the pinned header ate the space the answer needed. Everything now scrolls
 * as one page and the fields grow to their content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgePanel(
    uiState: ClarifyUiState,
    frameworks: List<FrameworkOption>,
    onPromptChange: (String) -> Unit,
    onSwapFramework: (String?) -> Unit,
    onRegenerate: () -> Unit,
    onExecute: () -> Unit,
    onTranslate: () -> Unit,
    onShowOriginal: () -> Unit,
    onRerun: (ProviderId) -> Unit,
    onToggleReadback: () -> Unit,
    /** Fired by any touch on the page. See the `pointerInput` below. */
    onUserScrolled: () -> Unit,
    onResumeFollowing: () -> Unit,
    onClearPrompt: () -> Unit,
    onUndoClearPrompt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            // **A drag means the reader took over — a tap does not.**
            //
            // Observed on the Initial pass and never consumed, so scrolling, selecting and
            // tapping all behave exactly as before; this only watches. The alternative, inferring
            // it from `scrollState.isScrollInProgress`, cannot tell the user's drag from the
            // app's own animated scroll, so the app would switch itself off every time it
            // followed a word.
            //
            // **Movement is the test, not contact.** Every press of «استمع» or «تابع القراءة»
            // also lands here, and treating a tap as a scroll would have those two buttons
            // switching off the very thing they turn on — surviving today only because the
            // Initial pass runs before the click and the click happens to win. That is an
            // ordering accident, and ordering accidents are how a control ends up doing two
            // things at once. A finger that has not moved has not scrolled.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.any { it.positionChange() != Offset.Zero }) {
                            onUserScrolled()
                        }
                    }
                }
            }
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
    ) {
        // The choice and its reason, shown as soon as they are known — before the prompt body
        // has finished arriving. Watching an English wall of text appear with no idea why *that*
        // framework was picked is being shown the output and denied the reasoning.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.clarify_forge_framework),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = uiState.frameworkId ?: "…",
                style = MaterialTheme.typography.labelLarge,
                // Amber is BRAND's insight accent — «the idea is ready» — and this is the one
                // moment in the app that means exactly that. BRAND §2 keeps it scarce, which is
                // why it appears here and nowhere else on this screen.
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        if (uiState.frameworkRationale.isNotBlank()) {
            BidiText(
                text = uiState.frameworkRationale,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(8.dp))

        if (uiState.forging && uiState.forgedPrompt.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(
                    text = stringResource(R.string.clarify_forging),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // **Controls first, prompt last, and that ordering is structural rather than aesthetic.**
        // The prompt is unbounded — a filled skeleton runs to thousands of characters. Laid out
        // above the controls it would push the swap chips and «أعد الصياغة» off the bottom of the
        // screen on a short device, silently. Putting the short, fixed-height controls first and
        // giving the field `weight(1f)` means the prompt can only ever consume what is left over,
        // and it scrolls inside itself. Third layout fault in this screen from one root
        // (`2026-08-07-D`, `-G`), so this one is designed rather than discovered.
        if (frameworks.isNotEmpty() && !uiState.forging) {
            Text(
                text = stringResource(R.string.clarify_forge_swap),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(frameworks) { option ->
                    FilterChip(
                        selected = uiState.frameworkOverride == option.id,
                        onClick = {
                            // Tapping the active chip clears the override and lets the model
                            // choose again — a swap the user can undo without leaving the screen.
                            onSwapFramework(
                                if (uiState.frameworkOverride == option.id) null else option.id,
                            )
                        },
                        label = { Text(option.id, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
        }

        if (!uiState.forging && !uiState.executing) {
            // **Two rows, not one — and the second row is a bug fix, not a preference.**
            //
            // All five controls used to sit in one `Row`. A Row does not wrap: when its children
            // no longer fit it compresses them, and the last child — «امسح» — was squeezed to a
            // width narrower than one word, so Compose wrapped its label **one letter per line**.
            // The owner's report, 2026-08-18: «صار عمودياً… ا تحتها م تحتها س».
            //
            // The redesign did not cause it so much as reveal it: 16dp corners and real button
            // padding widened every child, and a layout with no slack fails at the first child
            // that asks for more. The split also happens to be the right hierarchy — the two
            // actions that produce something above, the ones that only move text around below.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (uiState.forgedPrompt.isNotBlank()) {
                    PrimaryButton(onClick = onExecute) {
                        Text(
                            text = stringResource(R.string.clarify_forge_execute),
                            maxLines = 1,
                        )
                    }
                }
                TonalButton(onClick = onRegenerate) {
                    Text(
                        text = stringResource(R.string.clarify_forge_regenerate),
                        maxLines = 1,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // **Clear, and un-clear.** Selecting two thousand characters of monospace by hand
                // on a phone is the part that does not work, so the app does it — and because one
                // tap can destroy a long prompt, the same button becomes «تراجع» while the box is
                // empty. An undo rather than a confirmation dialog: a dialog taxes every use to
                // protect the rare mistake.
                when {
                    uiState.forgedPrompt.isNotEmpty() -> TextButton(onClick = onClearPrompt) {
                        Text(stringResource(R.string.clarify_forge_clear), maxLines = 1)
                    }

                    uiState.clearedPrompt.isNotEmpty() -> TextButton(onClick = onUndoClearPrompt) {
                        Text(stringResource(R.string.clarify_forge_undo_clear), maxLines = 1)
                    }
                }
                if (uiState.forgedPrompt.isNotBlank()) {
                    CopyButton(uiState.forgedPrompt, "P", R.string.clarify_copy_prompt)
                }
                if (uiState.result.isNotBlank()) {
                    CopyButton(uiState.result, "R", R.string.clarify_copy_result)
                }
            }
            BidiText(
                text = stringResource(R.string.clarify_forge_english_note),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // **Unconditional. The box does not vanish when it is empty.**
        //
        // It used to be wrapped in `if (forgedPrompt.isNotEmpty())`, so deleting the last
        // character deleted the field itself and left the user with nothing to type into — the
        // owner hit this on 2026-08-18 while clearing the prompt by hand. An empty editable field
        // is also the honest state after a forge that failed, and it lets a user write their own
        // prompt from scratch, which ruling M3-5 already entitles them to.
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.forgedPrompt,
                onValueChange = onPromptChange,
                // No `weight` any more: inside a scrolling column the field grows to its content
                // and the page carries it. A weight here would recreate the inner scroll box the
                // owner asked to be rid of.
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.clarify_forge_prompt)) },
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    // Forced LTR, not detected. The prompt is English by rule, but it quotes the
                    // user's Arabic idea inside CONTEXT — and content detection would flip the
                    // whole field the moment the quoted Arabic outweighed the English around it.
                    // `core-ui/text/BidiText.kt` stays the single place direction is decided;
                    // this is the same `forced` escape hatch it exposes for JSON bodies.
                    textDirection = TextDirection.Ltr,
                    textAlign = TextAlign.Start,
                ),
            )
        }

        if (uiState.executing || uiState.result.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (uiState.executing) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                }
                Text(
                    text = stringResource(R.string.clarify_forge_result),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // **Who answered, and why.** `BRAINING.md` §5. Coloured as an error only when a
            // fallback happened, because that line is reporting an event the user did not ask
            // for — the provider they chose did not answer.
            uiState.route?.let { route ->
                Text(
                    text = route.toUserMessage(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (route.reason == RouteReason.FALLBACK_AFTER_FAILURE) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            if (uiState.developerMode && uiState.feedbackRounds > 0) {
                Text(
                    text = stringResource(R.string.clarify_dev_feedback, uiState.feedbackRounds),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // No weight and no scroll of its own — the page scrolls, the answer grows.
            Column(modifier = Modifier.fillMaxWidth()) {
                // **Selectable.** The owner could not copy the answer — it was plain `Text`, and
                // the prompt beside it was copyable only because a text field happens to be. An
                // output the user cannot take out of the app is an output they cannot use, and
                // the copy button above covers the case where selecting on a phone is fiddly.
                SelectionContainer {
                    // One composable, two behaviours, and the difference is only whether a word
                    // is marked — the direction resolution, the style and the selectability are
                    // identical either way. `fallback`, not `forced`: since 2026-08-07 the forged
                    // prompt's OUTPUT CONTRACT asks for an Arabic answer, so most answers will be
                    // Arabic and detection must be free to say so.
                    SpokenText(
                        text = uiState.result,
                        spoken = uiState.spokenRange.takeIf { uiState.reader.speaking },
                        follow = uiState.followSpoken,
                        style = MaterialTheme.typography.bodyMedium,
                        fallback = BidiDirection.Ltr,
                    )
                }
            }

            // **The detector's own number, in Developer Mode.**
            //
            // Added 2026-08-18. The owner tried to make the translate button appear, failed, and
            // could not tell whether the detector was wrong, the answer was Arabic, or the button
            // was never wired. A feature that decides silently is a feature nobody can debug —
            // §10 entry 5. The percentage says which of the three it is, in one glance, and it is
            // published before anyone needs it (§10 entry 9).
            if (uiState.developerMode && uiState.result.isNotBlank() && !uiState.executing) {
                Text(
                    text = stringResource(
                        R.string.clarify_dev_arabic_ratio,
                        (ScriptDetector.arabicRatio(uiState.result) * 100).toInt(),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── translate on demand ──────────────────────────────────────────────────────
            //
            // The button appears only when the answer does not look Arabic. Since 2026-08-07 the
            // forged prompt requires an Arabic reply, so the normal case needs nothing — a
            // mandatory translate step would spend a second call and several seconds on every
            // answer to change nothing (owner's ruling, 2026-08-17). `ScriptDetector` decides,
            // and its thresholds are pinned by `ScriptDetectorTest`.
            if (!uiState.executing && uiState.result.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    when {
                        uiState.translating -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                            )
                            Text(
                                text = stringResource(R.string.clarify_translating),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        uiState.showingTranslation -> TextButton(onClick = onShowOriginal) {
                            Text(stringResource(R.string.clarify_show_original))
                        }

                        ScriptDetector.looksNonArabic(uiState.result) -> {
                            TextButton(onClick = onTranslate) {
                                Text(stringResource(R.string.clarify_translate))
                            }
                        }
                    }

                    // ── readback ──────────────────────────────────────────────────────────
                    //
                    // M5, `ANSWERS.md` Part 1 §7 and Part 11 §K3. **Absent entirely unless the
                    // switch in Settings is on and the device actually has an Arabic voice** —
                    // `ClarifyViewModel` ands those two together, because a button that produces
                    // silence is the defect `docs/M2_DESIGN_NOTE.md` §6 already ruled worse than
                    // no button at all.
                    //
                    // One button with two labels rather than two buttons: the second press of a
                    // play-only control does nothing visible and teaches the user it is broken
                    // (§10 entry 26).
                    if (uiState.ttsEnabled) {
                        TextButton(onClick = onToggleReadback) {
                            Text(
                                stringResource(
                                    if (uiState.reader.speaking) {
                                        R.string.clarify_readback_stop
                                    } else {
                                        R.string.clarify_readback
                                    },
                                ),
                            )
                        }

                        // Offered only while it would do something: the audio is running and the
                        // page has stopped following it. A permanent «تابع القراءة» would be a
                        // control that does nothing nine times out of ten.
                        if (uiState.reader.speaking && !uiState.followSpoken) {
                            TextButton(onClick = onResumeFollowing) {
                                Text(stringResource(R.string.clarify_readback_follow))
                            }
                        }
                    }
                }

                // ── why it did not speak ──────────────────────────────────────────────
                //
                // The owner, 2026-08-28: the button flipped and came straight back, and later
                // ran with no sound. Both were one thing — his phone has no Arabic voice
                // installed — and the app had no way to say it. This is that sentence, and it
                // names the screen that fixes it rather than leaving him to hunt.
                if (uiState.ttsEnabled) {
                    ReaderFailureNotice(uiState.reader)
                }

                // The engine's own account of itself, for when the sentence above is not enough.
                // §10 entry 5: when a subsystem picks a strategy at runtime, the chosen strategy
                // is diagnostic output — and every real fault in M2 was found by a number put on
                // screen for a different reason.
                if (uiState.developerMode && uiState.ttsEnabled) {
                    Text(
                        text = stringResource(
                            R.string.clarify_dev_reader,
                            uiState.reader.engine.ifBlank { "—" },
                            uiState.reader.languageCode?.toString() ?: "—",
                            stringResource(
                                if (uiState.reader.reportsWords) {
                                    R.string.clarify_dev_reader_words_yes
                                } else {
                                    R.string.clarify_dev_reader_words_no
                                },
                            ),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // ── the manual override, and the re-run ──────────────────────────────────
                //
                // `BRAINING.md` §5: the user may override the model on any connected provider and
                // re-run. **For this answer only** — it does not change the chat's selection, and
                // a control that silently rewrote a setting behind another screen would be the
                // surprise Developer Mode exists to prevent.
                Text(
                    text = stringResource(R.string.clarify_rerun_with),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ProviderId.entries.filter { it != uiState.route?.provider }) { pid ->
                        FilterChip(
                            selected = false,
                            onClick = { onRerun(pid) },
                            enabled = !uiState.executing && !uiState.translating,
                            label = { Text(pid.displayName) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Puts [text] on the clipboard. Android shows its own confirmation, so this one stays silent.
 *
 * **[letter] is on the face of the button, not only in its description.** Two identical copy
 * icons sat side by side and the owner could not tell which took the prompt and which took the
 * answer — a content description solves that for a screen reader and for nobody else. `P` and
 * `R` are on screen for the same reason the turn labels are: **a control that needs to be
 * guessed at has not been labelled.**
 *
 * `description` is a plain `Int` rather than a `@StringRes Int`: the annotation is lint-only and
 * would pull `androidx.annotation` onto this module's compile path for nothing. It arrives
 * transitively today, which is exactly the kind of dependency that disappears without warning.
 */
@Composable
private fun CopyButton(text: String, letter: String, description: Int) {
    val clipboard = LocalClipboardManager.current
    TextButton(
        onClick = { clipboard.setText(AnnotatedString(text)) },
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        Icon(
            Icons.Default.ContentCopy,
            contentDescription = stringResource(description),
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.size(2.dp))
        Text(
            text = letter,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IdeaCard(idea: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.clarify_idea_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            // Every mixed-script string in this app goes through BidiText — it is the single
            // place direction is decided, and getting that wrong cost a day in M1.
            BidiText(
                text = idea,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TurnCard(turn: ClarifyTurn) {
    val label = when (turn) {
        is ClarifyTurn.Question -> R.string.clarify_kind_question
        is ClarifyTurn.Suggestion -> R.string.clarify_kind_suggestion
        is ClarifyTurn.Caveat -> R.string.clarify_kind_caveat
        is ClarifyTurn.Enough -> R.string.clarify_kind_enough
        is ClarifyTurn.UserReply -> R.string.clarify_kind_you
    }
    // A caveat is NOT amber and NOT an error colour. `docs/BRAND.md` §6 reserves amber for
    // insight — "the idea is ready" — so dressing a warning in it would invert the app's own
    // signal, which is exactly the defect `2026-08-04-D` found in the light error card. And a
    // caveat is not a failure, so the error container would over-state it too.
    val container = when (turn) {
        is ClarifyTurn.UserReply -> MaterialTheme.colorScheme.primaryContainer
        // The one moment BRAND §2 reserves amber for — «the idea is ready». It appears once per
        // interrogation, at most, which is what "amber is scarce by rule" means in practice.
        is ClarifyTurn.Enough -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = when (turn) {
        is ClarifyTurn.UserReply -> MaterialTheme.colorScheme.onPrimaryContainer
        is ClarifyTurn.Enough -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.labelSmall,
                color = content,
            )
            Spacer(Modifier.height(4.dp))
            BidiText(
                text = turn.text,
                style = MaterialTheme.typography.bodyMedium,
                color = content,
            )
        }
    }
}

@Composable
private fun StreamingCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        BidiText(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp),
        )
    }
}

/**
 * Why the answer was not read aloud — and, where the user can fix it, the way there.
 *
 * **This composable is the whole lesson of 2026-08-28.** The readback shipped with a boolean for
 * state and no vocabulary for failure, so a phone with no Arabic voice reported an utterance that
 * began and ended instantly — the owner saw the button flip twice and then run silently, and
 * neither symptom named its cause. `PROJECT_STATE.md` §10 entry 7: a failure that looks like a
 * success is not one anybody can act on.
 *
 * Each branch below is a different thing to do, which is why they are not one sentence:
 *
 *  - **No voice for Arabic** is the common one and the only one the user can repair. The button
 *    opens the system's text-to-speech screen directly, because "go to settings, languages, text
 *    to speech, install voice data" is four wrong turns on a Xiaomi and one tap here. §10 entry
 *    1 in reverse: do not hand someone a symptom and let them find the room.
 *  - **No engine at all** cannot be repaired from inside this app; the sentence says so rather
 *    than sending the user somewhere that will not help.
 *  - **A speak failure** carries the platform's own code, because it is rare, unclassified, and
 *    the number is the only thing that will make the next report diagnosable.
 *
 * Renders nothing when there is nothing wrong, so the caller places it unconditionally.
 */
@Composable
private fun ReaderFailureNotice(reader: ReaderStatus) {
    val failure = reader.failure ?: return
    val context = LocalContext.current

    Spacer(Modifier.height(4.dp))
    Column {
        BidiText(
            text = when (failure) {
                ReaderFailure.NoVoiceForLanguage ->
                    stringResource(R.string.clarify_readback_no_voice)
                ReaderFailure.NoEngine ->
                    stringResource(R.string.clarify_readback_no_engine)
                is ReaderFailure.SpeakFailed ->
                    stringResource(R.string.clarify_readback_failed, failure.code)
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )
        if (failure == ReaderFailure.NoVoiceForLanguage) {
            TextButton(
                onClick = {
                    // Wrapped: the action is documented but not guaranteed — some ROMs remove the
                    // screen entirely, and an unhandled intent would crash the app to save the
                    // user a tap. Failing to open a settings page must cost nothing.
                    runCatching {
                        context.startActivity(
                            Intent("com.android.settings.TTS_SETTINGS")
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
            ) {
                Text(stringResource(R.string.clarify_readback_install_voice))
            }
        }
    }
}
