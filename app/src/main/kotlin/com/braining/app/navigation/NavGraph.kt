package com.braining.app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.braining.core.domain.history.SessionRecord
import com.braining.core.ui.diagnostics.Diag
import com.braining.feature.chat.ChatScreen
import com.braining.feature.clarify.ClarifyScreen
import com.braining.feature.clarify.ClarifyViewModel
import com.braining.feature.history.HistoryScreen
import com.braining.feature.settings.OnboardingScreen
import com.braining.feature.settings.SettingsScreen

object Routes {
    const val CHAT = "chat"
    const val SETTINGS = "settings"
    const val HISTORY = "history"

    /**
     * The first-run flow. `ANSWERS.md` Part 3 §A and Part 11 §K4.
     *
     * A destination rather than a dialog over the chat, because it is a *place* the user can be
     * sent to and can leave. A modal over a screen they have not seen yet would be the recording
     * sheet's mistake again (`2026-08-17-A`): the thing behind it is unreachable and the only way
     * out is a gesture nobody documented.
     */
    const val ONBOARDING = "onboarding"

    /**
     * CLARIFY carries the idea and the provider as path arguments.
     *
     * **Why arguments and not a shared object.** `ANSWERS.md` Part 7 §M3-3 runs Clarify on the
     * provider selected in the chat, and that selection lives only in `ChatViewModel`'s memory —
     * a second ViewModel cannot see it. A singleton handoff would work and would be exactly the
     * kind of global mutable state the unscoped `ClarifyEngine` binding was written to avoid.
     * The route makes the ruling literal and leaves nothing hidden.
     *
     * The idea is URL-encoded: an Arabic transcript contains spaces, newlines and `/`.
     *
     * **M5 adds `session` as an optional query argument**, so re-running a saved run travels the
     * same route as a fresh one. It is a query argument and not a fourth path segment precisely
     * so that every existing caller keeps compiling and none has to invent a value.
     */
    const val CLARIFY = "clarify"

    fun clarify(idea: String, provider: String): String =
        "$CLARIFY/${Uri.encode(idea)}/${Uri.encode(provider)}"

    /**
     * Re-run a saved session.
     *
     * **Both path segments carry [PLACEHOLDER] and neither may be empty.** Navigation matches a
     * path argument with `([^/]+?)`, so an empty segment produces `clarify///?session=5`, which
     * matches no destination and throws at `navigate()` — the re-run button would have crashed
     * every time it was pressed.
     *
     * The values themselves are never used: `ClarifyViewModel.restore` replaces the idea, the
     * provider and the model from the row it loads, and reports a missing row rather than
     * interrogating the placeholder.
     */
    fun clarifySaved(sessionId: Long): String =
        "${clarify(PLACEHOLDER, PLACEHOLDER)}?${ClarifyViewModel.ARG_SESSION_ID}=$sessionId"

    /** Non-empty by requirement, meaningless by design. See [clarifySaved]. */
    private const val PLACEHOLDER = "_"
}

@Composable
fun BrainingNavGraph(
    navController: NavHostController,
    /**
     * Where to start.
     *
     * Decided by `MainActivity` from two facts — no key stored **and** onboarding not dismissed
     * — rather than by this graph, because the graph is built once and those facts are read
     * asynchronously. A start destination chosen before the answer arrives would flash the wrong
     * screen (`PROJECT_STATE.md` §10 entry 21: a screen that fires on open cannot rely on a flow
     * still warming up).
     */
    startDestination: String = Routes.CHAT,
) {
    // TEMPORARY, 2026-09-05 — remove with the ChatScreen probe once the black screen is named.
    // Every screen the app lands on, in order, under one tag. The black screen was reported as
    // "Settings, back, open the menu"; this says plainly whether the app is even on CHAT when it
    // goes dark, or somewhere the reproduction did not mention.
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            Diag.log("nav -> ${entry.destination.route}")
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onDone = {
                    // popUpTo(inclusive) so Back from chat does not return to a setup flow the
                    // user has finished. An onboarding screen you can reach again by accident is
                    // an onboarding screen that has not ended.
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.CHAT) {
            ChatScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onOpenClarify = { idea, provider ->
                    navController.navigate(Routes.clarify(idea, provider))
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                // This layer does not read the database, so nothing real can travel in the
                // path. The id is the whole message; `ClarifyViewModel` recovers the rest.
                onRerun = { id -> navController.navigate(Routes.clarifySaved(id)) },
            )
        }
        composable(
            route = "${Routes.CLARIFY}/{${ClarifyViewModel.ARG_IDEA}}/{${ClarifyViewModel.ARG_PROVIDER}}" +
                "?${ClarifyViewModel.ARG_SESSION_ID}={${ClarifyViewModel.ARG_SESSION_ID}}",
            arguments = listOf(
                navArgument(ClarifyViewModel.ARG_SESSION_ID) {
                    type = NavType.LongType
                    defaultValue = SessionRecord.NEW
                },
            ),
        ) {
            ClarifyScreen(onBack = { navController.popBackStack() })
        }
    }
}
