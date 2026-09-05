package com.braining.app

import android.app.Application
import com.braining.core.ui.diagnostics.Diag
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BrainingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // First line of the app's life: make sure any later death has our name on it.
        // `PROJECT_STATE.md` §10 entry 64 — an app that logs nothing cannot be debugged from a log.
        Diag.installCrashHandler()
    }
}
