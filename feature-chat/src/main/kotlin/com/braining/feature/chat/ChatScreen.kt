package com.braining.feature.chat

import com.braining.core.ui.error.ProviderErrorDetail
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.braining.core.domain.model.MessageRole
import com.braining.core.domain.model.ProviderId
import com.braining.core.ui.error.toUserMessage
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.braining.core.ui.diagnostics.DiagnosticsPanel
import com.braining.core.ui.input.submitOnCtrlEnter
import com.braining.core.ui.text.BidiDirection
import com.braining.core.ui.text.BidiText
import com.braining.core.ui.text.ProvideBidiDirection
import com.braining.core.ui.voice.MicPermissionDialog
import com.braining.core.ui.voice.VoiceCapturePanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    /**
     * Open the saved-session list. M5.
     *
     * A callback for the same reason [onOpenClarify] is one: `:feature-chat` must not know that
     * `:feature-history` exists. Feature modules are siblings (hard constraint 8), and the app
     * module is the only place allowed to know both.
     */
    onNavigateToHistory: () -> Unit = {},
    /**
     * Open CLARIFY on the text currently in the input field.
     *
     * A callback, **never a dependency on `:feature-clarify`.** Feature modules are siblings —
     * the rule `2026-08-04-B` spent a morning establishing — so chat hands the idea and the
     * selected provider outward and `:app`'s NavGraph decides where they go. The provider
     * travels with it because `ANSWERS.md` Part 7 §M3-3 makes Clarify run on whatever the chat
     * had selected, and that selection lives only in this ViewModel's memory.
     */
    onOpenClarify: (idea: String, provider: String) -> Unit = { _, _ -> },
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // --- M2 voice capture ---------------------------------------------------------------
    val context = LocalContext.current
    var showPermissionRationale by remember { mutableStateOf(false) }

    // Recognition follows the app's own language toggle, so a user reading an English UI
    // dictates in English without hunting for a second setting.
    val languageTag = LocalConfiguration.current.locales[0].language

    val micPermissionGranted = {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chat_title)) },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                    ) {
                        Text(
                            text = uiState.selectedProvider.displayName,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .padding(8.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            ProviderId.entries.forEach { pid ->
                                DropdownMenuItem(
                                    text = { Text(pid.displayName) },
                                    onClick = {
                                        viewModel.selectProvider(pid)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                    // History before Settings: it is the one a user reaches for repeatedly,
                    // and the rightmost positions in an RTL app bar are the hardest to reach
                    // with a thumb.
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = stringResource(R.string.chat_action_history),
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.chat_action_settings),
                        )
                    }
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.chat_action_clear),
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
            // Token usage bar. Fallback Ltr: this line is a pure Latin/numeric readout,
            // so if it ever renders before any strong character exists it should not be
            // pushed to the right edge.
            uiState.tokenUsage?.let { usage ->
                BidiText(
                    text = usage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fallback = BidiDirection.Ltr,
                )
            }

            // Error display
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
                        // Error text is the worst case for bidirectional layout: Arabic
                        // prose wrapped around a Latin provider name and an HTTP status.
                        // The typed AiError is resolved to a sentence here, in the UI,
                        // from string resources — the provider layer never phrases it.
                        BidiText(
                            text = error.toUserMessage(),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        // A copy button, always — the owner's ruling of 2026-08-30: a friend
                        // who hits an error cannot retype an English sentence, but can paste one.
                        // The provider's own raw words stay behind Developer Mode.
                        ProviderErrorDetail(error, uiState.developerMode)
                        // A failed request discards its assistant bubble, so its
                        // diagnostics are surfaced here instead — a failure is precisely
                        // when the endpoint and the outgoing body are worth reading.
                        uiState.lastDiagnostics?.let { diagnostics ->
                            if (uiState.developerMode) DiagnosticsPanel(diagnostics)
                        }

                        // ── who else could answer this ──────────────────────────────────
                        //
                        // Clarify has had this since 2026-08-28; **chat did not**, and the owner
                        // found the gap the first time Gemini returned 429: the card named the
                        // failure and offered him nothing to do about it. The app still never
                        // switches provider by itself (his ruling of 28 August, reversing his own
                        // of 17 August) — it names what failed, lists the providers he holds keys
                        // for, and waits.
                        //
                        // **Empty means a hop would be wrong**, not that he has no other keys: a
                        // missing key, a rejected key, a dead network and an unclassified failure
                        // are all refused by `DefaultModelRouter.isRecoverable`. Then only
                        // «أعد المحاولة» remains, which is the honest offer for a problem another
                        // provider cannot solve.
                        if (uiState.fallbackOptions.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            BidiText(
                                text = stringResource(R.string.chat_fallback_prompt),
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
                                Text(stringResource(R.string.chat_retry))
                            }
                            // One tap for a user who does not care which provider answers, only
                            // that one does. It takes the head of the same list the chips are
                            // built from, so it can never choose something they were not offered.
                            if (uiState.fallbackOptions.isNotEmpty()) {
                                TextButton(onClick = { viewModel.tryAnyFallback() }) {
                                    Text(stringResource(R.string.chat_fallback_any))
                                }
                            }
                        }
                    }
                }
            }

            // Speech failures get their own card rather than sharing the provider one: they
            // are a different subsystem with different remedies, and overwriting a provider
            // error with a microphone error would hide the reason a message never sent.
            uiState.voice.error?.let { sttError ->
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

            // The number the gate is judged on (ANSWERS.md Part 5 §M2-3): more than one
            // segment means the engine restarted mid-paragraph. Behind Developer Mode because
            // it is a measurement, not something a user needs.
            if (uiState.developerMode && uiState.voice.segments > 0) {
                Text(
                    text = stringResource(
                        R.string.voice_dev_segments,
                        uiState.voice.segments,
                        uiState.voice.durationSeconds,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            // Which rung of the attempt ladder the engine accepted. Kept beside the segment
            // count because the two answer different halves of "why was that transcript bad":
            // segments say whether the engine kept restarting, this says which model and which
            // dialect produced the words. Survives after the sheet closes, deliberately — it is
            // read *after* looking at the text, not during dictation.
            //
            // **Only shown while it still describes the current language.** `engineTag` lives in
            // the ViewModel, which survives the activity recreation that the language toggle
            // causes — so after switching to Arabic this line went on displaying `en-US` until
            // the next recording overwrote it (owner's report, 2026-08-06). The recognition
            // language itself was never stale: `languageTag` above is read from the live
            // configuration on every recomposition. Only the label lagged, which is worse than
            // it sounds — this line exists so the engine's behaviour can be trusted, and a
            // diagnostic that is confidently wrong is more harmful than none at all.
            //
            // Matching on the language subtag rather than clearing the state keeps the reading
            // through a rotation, which does not change what the engine will be asked for.
            if (uiState.developerMode) {
                val accepted = uiState.voice.engineTag
                    ?.takeIf { it.substringBefore('-') == languageTag }
                Text(
                    text = if (accepted != null) {
                        stringResource(
                            R.string.voice_dev_engine,
                            accepted,
                            stringResource(
                                if (uiState.voice.engineOffline) {
                                    R.string.voice_dev_engine_offline
                                } else {
                                    R.string.voice_dev_engine_online
                                },
                            ),
                        )
                    } else {
                        // The tag that *will* be requested. Shown before the first recording in
                        // a language so the toggle visibly takes effect at the moment it is
                        // flipped — which is what the owner expected and was right to expect.
                        // Which region the ladder settles on cannot be known until the engine
                        // accepts a rung, so this deliberately names the language, not the
                        // dialect, and does not pretend otherwise.
                        stringResource(R.string.voice_dev_engine_pending, languageTag)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                itemsIndexed(uiState.messages) { index, message ->
                    MessageBubble(
                        message = message,
                        showDiagnostics = uiState.developerMode,
                        onEdit = { viewModel.editMessage(index) },
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // **The recording panel takes the input row's place; it does not cover the page.**
            //
            // The sheet was modal until 2026-08-17, which meant the messages behind it could be
            // neither read nor scrolled, and the first touch on them cancelled the recording.
            // See the KDoc on VoiceCapturePanel. Clarify carries the identical change.
            if (uiState.voice.isRecording) {
                VoiceCapturePanel(
                    amplitude = uiState.voice.amplitude,
                    transcript = uiState.inputText,
                    partial = uiState.voice.partial,
                    onDone = { viewModel.stopVoice() },
                    onCancel = { viewModel.cancelVoice() },
                )
            } else {
                // Input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    // `weight` is a RowScope extension, so the modifier is built here and
                    // handed into the direction-providing wrapper, which is not a RowScope.
                    // CompositionLocalProvider emits no layout node, so the field stays a
                    // direct child of the Row and the weight still applies.
                    val fieldModifier = Modifier
                        .weight(1f)
                        .submitOnCtrlEnter { viewModel.sendMessage() }
                    ProvideBidiDirection(text = uiState.inputText) { direction ->
                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = { viewModel.updateInput(it) },
                            modifier = fieldModifier,
                            placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                            maxLines = 4,
                            textStyle = LocalTextStyle.current.copy(
                                textDirection = direction.textDirection,
                                textAlign = TextAlign.Start,
                            ),
                        )
                    }
                    // Hidden, not disabled, when the device has no engine: docs/M2_DESIGN_NOTE.md
                    // §6 — a button that is present and always fails is worse than one that is
                    // absent. Also hidden mid-generation, where the input field is not in use.
                    if (uiState.voice.engineAvailable && !uiState.isGenerating) {
                        IconButton(
                            onClick = {
                                if (micPermissionGranted()) {
                                    viewModel.startVoice(languageTag)
                                } else {
                                    showPermissionRationale = true
                                }
                            },
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = stringResource(R.string.chat_action_voice),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    // CLARIFY entry. `ANSWERS.md` Part 7 §M3-1: an interrogation is a mode you
                    // choose, not the only road — plain chat is untouched, and a five-question
                    // interrogation of «مرحبا» is the friction that teaches people to route around
                    // a feature. Hidden rather than disabled when there is nothing to interrogate:
                    // the route carries the idea as an argument, so an empty one is meaningless.
                    if (uiState.inputText.isNotBlank() && !uiState.isGenerating) {
                        IconButton(
                            onClick = {
                                onOpenClarify(
                                    uiState.inputText.trim(),
                                    uiState.selectedProvider.name,
                                )
                            },
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = stringResource(R.string.chat_action_clarify),
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (uiState.isGenerating) {
                        FloatingActionButton(
                            onClick = { viewModel.cancelGeneration() },
                            containerColor = MaterialTheme.colorScheme.error,
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = stringResource(R.string.chat_action_stop),
                            )
                        }
                    } else {
                        FloatingActionButton(
                            onClick = { viewModel.sendMessage() },
                            containerColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.chat_action_send),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessageUi,
    showDiagnostics: Boolean,
    onEdit: (() -> Unit)? = null,
) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val clipboard = LocalClipboardManager.current
    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(containerColor = containerColor),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Hoisted out of `ifEmpty`: stringResource is @Composable and reads more
                // clearly resolved once than inside a lambda.
                val pendingPlaceholder = stringResource(R.string.chat_message_pending)
                // One direction resolved for the entire message, then forced on every
                // paragraph of it. Resolving per paragraph — Compose's default — is what
                // made Arabic answers containing English terms interleave.
                BidiText(
                    text = message.content.ifEmpty { pendingPlaceholder },
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (message.isStreaming) {
                    Spacer(modifier = Modifier.height(4.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                    )
                }
                // Copy — and, on your own messages, edit.
                //
                // The text is inside a plain `Text`, so long-press selection does not reach it;
                // the owner asked for buttons and buttons are what a phone can actually hit.
                // Hidden while a reply is still arriving: copying half an answer, or editing a
                // question whose answer is still being written, are both requests for a mess.
                if (!message.isStreaming && message.content.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { clipboard.setText(AnnotatedString(message.content)) },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.chat_action_copy),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        if (isUser && onEdit != null) {
                            TextButton(
                                onClick = onEdit,
                                contentPadding = PaddingValues(horizontal = 8.dp),
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.chat_action_edit),
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
                message.diagnostics?.let { diagnostics ->
                    if (showDiagnostics) DiagnosticsPanel(diagnostics)
                }
            }
        }
    }
}

// DiagnosticsPanel and DiagnosticsField moved to `core-ui/diagnostics/` on 2026-08-07, when
// CLARIFY became the second screen that shows them. §9's standing rule from `2026-08-04-B`:
// feature modules are siblings, and anything two of them need goes to core-ui, never to a peer.
// The strings moved with it and were renamed `chat_dev_*` → `dev_*`. Do not reintroduce either
// here.
