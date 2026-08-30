package com.braining.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
                     * Null until both facts are known.
                     *
                     * **The graph is not built until then, and that is the point.** `NavHost`
                     * takes its start destination once, at construction; choosing one before the
                     * answer arrives would show the chat for a frame and then jump to onboarding,
                     * or worse, keep the wrong one. `PROJECT_STATE.md` §10 entry 21 is exactly
                     * this: a screen that fires on open cannot rely on a flow still warming up.
                     *
                     * The cost is one frame of empty background on cold start — both reads are
                     * local, and `SharedPreferences` is already in memory by the time Hilt has
                     * built the graph.
                     */
                    val start by produceState<String?>(initialValue = null) {
                        val dismissed = appPreferences.onboardingDismissed.first()

                        // **Provider keys only.** A Deepgram key transcribes speech and cannot
                        // answer a question, so a user holding only that one has not finished
                        // setting the app up — treating it as "configured" would drop them into
                        // a chat that fails on the first message.
                        val hasProviderKey = runCatching {
                            val names = ProviderId.entries.map { it.name }.toSet()
                            keyStore.getAllKeys()
                                .any { (id, key) -> id in names && key.isNotBlank() }
                        }.getOrDefault(false)

                        // Both conditions, never either. `docs/M5_DESIGN_NOTE.md` §6: a user who
                        // skipped onboarding and later deleted their only key must not be dragged
                        // back through it, and a user who already has keys has onboarded whatever
                        // the flag says.
                        value = if (!dismissed && !hasProviderKey) {
                            Routes.ONBOARDING
                        } else {
                            Routes.CHAT
                        }
                    }

                    start?.let { destination ->
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
}
