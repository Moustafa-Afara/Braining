plugins {
    id("braining.android.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.braining.feature.settings"
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":core-data"))
    implementation(project(":ai-providers"))
    // AiError.toUserMessage() lives here (com.braining.core.ui.error). It was briefly in
    // feature-chat, which forced a sibling dependency from this module onto that one; removed
    // 2026-08-04, PROJECT_STATE.md §10 entry 2026-08-04-B. Feature modules stay siblings —
    // anything two of them share belongs in core-ui, never in one of them.
    implementation(project(":core-ui"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // The language switch uses AppCompatDelegate.setApplicationLocales — the only in-app
    // locale path on minSdk 26. Same library the app module hosts; no new external dep.
    implementation(libs.androidx.appcompat)
}
