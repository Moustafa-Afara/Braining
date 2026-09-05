package com.braining.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.braining.app.navigation.BrainingNavGraph
import com.braining.app.navigation.Routes
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.store.AppPreferences
import com.braining.core.domain.store.EncryptedKeyStore
import com.braining.core.ui.theme.BrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /**
     * Field injection rather than a ViewModel, deliberately.
     *
     * The only question these answer is *which screen opens first*, which is decided once before
     * anything is composed. A ViewModel would add `hilt-navigation-compose` to the app module to
     * hold two booleans for one frame.
     */
    @Inject lateinit var appPreferences: AppPreferences

    @Inject lateinit var keyStore: EncryptedKeyStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrainingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    /**
                     * Which screen opens first. Null means *not decided yet*.
                     *
                     * **`rememberSaveable`, and that is the whole point of 2026-09-05.**
                     * `MainActivity` declares no `android:configChanges`, so Android destroys
                     * and recreates it on every configuration change — and MIUI fires
                     * `configuration_changed: 0x20000500`
                     * (WINDOW_CONFIGURATION | SCREEN_SIZE | SCREEN_LAYOUT) on its own account:
                     * ten of them in one evening's logcat, six while this app was not even
                     * running. Six of those recreated this activity, always in pairs, and every
                     * pair was followed within two minutes by the owner swiping the app dead.
                     *
                     * With `produceState` the decision restarted from nothing on each of those
                     * recreations, and until it finished the entire app was
                     * `Surface(color = background)` and nothing else — no graph, no chat, no top
                     * bar. In the dark scheme that background is `#0E0D14`. **A black screen,
                     * alive, focused, taking touches and drawing nothing, with no crash and no
                     * ANR to explain it.** That is exactly what the owner reported and exactly
                     * what the logs show: last app log line at 03:03:52.504, two touches on this
                     * activity's own window after it, no response to either, then silence.
                     *
                     * Saved state survives the recreation, so from the second composition on
                     * there is no decision to make and no gap to fall into.
                     *
                     * **What is still not proven** is why the gap lasted rather than flashing;
                     * the read is off the main thread and should return in milliseconds. The two
                     * guards below make that question moot rather than answered, which is the
                     * honest description of them.
                     */
                    var start by rememberSaveable { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        // Restored across a recreation: nothing to decide, nothing to wait for.
                        if (start != null) return@LaunchedEffect

                        /**
                         * **Guard one: this can no longer wait forever.**
                         *
                         * Both reads are local — a `MutableStateFlow` already holding its value,
                         * and an `EncryptedSharedPreferences` read on `Dispatchers.IO`. Neither
                         * has any business taking a second. If one ever does, the timeout lands
                         * on CHAT rather than leaving the screen empty.
                         *
                         * **CHAT is the deliberate fallback, in both branches.** A first-run user
                         * who times out here misses onboarding and lands in a chat with no key —
                         * which is a *recoverable* state: the first message returns the missing
                         * key error, which already carries a link to Settings. A black screen is
                         * not recoverable by any action the user can take.
                         */
                        val dismissed = withTimeoutOrNull(START_DECISION_TIMEOUT_MS) {
                            appPreferences.onboardingDismissed.first()
                        } ?: true

                        // **Provider keys only.** A Deepgram key transcribes speech and cannot
                        // answer a question, so a user holding only that one has not finished
                        // setting the app up — treating it as "configured" would drop them into
                        // a chat that fails on the first message.
                        val hasProviderKey = withTimeoutOrNull(START_DECISION_TIMEOUT_MS) {
                            runCatching {
                                val names = ProviderId.entries.map { it.name }.toSet()
                                keyStore.getAllKeys()
                                    .any { (id, key) -> id in names && key.isNotBlank() }
                            }.getOrDefault(false)
                        } ?: false

                        // Both conditions, never either. `docs/M5_DESIGN_NOTE.md` §6: a user who
                        // skipped onboarding and later deleted their only key must not be dragged
                        // back through it, and a user who already has keys has onboarded whatever
                        // the flag says.
                        start = if (!dismissed && !hasProviderKey) {
                            Routes.ONBOARDING
                        } else {
                            Routes.CHAT
                        }
                    }

                    /**
                     * **Guard two: an empty screen is no longer a legal state.**
                     *
                     * The old code rendered *nothing* while deciding, which is indistinguishable
                     * from a dead app — and that is the entire reason four hours went into
                     * telling a crash, an ANR and a draw fault apart. A spinner says "working",
                     * costs one frame on cold start, and turns any future stall of this kind into
                     * a visible symptom instead of a black void.
                     */
                    val destination = start
                    if (destination == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val navController = rememberNavController()
                        BrainingNavGraph(
                            navController = navController,
                            startDestination = destination,
                        )
                    }
                }
            }
        }
    }

    private companion object {
        /**
         * How long the start-destination decision may take before it gives up and opens CHAT.
         *
         * Two seconds is not a performance budget — it is roughly a hundred times what these two
         * local reads need. It exists so that "the screen is empty" can never again mean "and it
         * will stay that way".
         */
        const val START_DECISION_TIMEOUT_MS = 2_000L
    }
}
