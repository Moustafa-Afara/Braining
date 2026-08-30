plugins {
    id("braining.android.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.braining.feature.clarify"
}

// One module for CLARIFY and FORGE together, not two.
//
// `docs/ARCHITECTURE.md` §2 names `:feature-clarify` and `:feature-forge` separately. That
// boundary has nothing on the other side of it: nothing calls FORGE except CLARIFY, and no
// screen shows it except CLARIFY's. `:speech` set the precedent in the other direction — two
// interchangeable engines and a router in ONE module, because a boundary drawn before there is
// a second consumer is a guess (`docs/M3_DESIGN_NOTE.md` §3.1).
//
// And deliberately NOT inside `feature-chat`. Ruling M2-4 put the voice UI there because that
// flow ends as text in the chat input, and said in the same breath that a new boundary "before
// M3's shape is known" was premature. M3's shape is known now, and it is not chat input: its
// own state machine, its own screen, and the only screen `ANSWERS.md` Part 1 §9 gives Compose
// UI tests to.
//
// The `braining.android.compose` plugin is applied now, while there is still no composable, so
// that the slice which adds the CLARIFY screen touches Kotlin only and its result is
// unambiguous — the same reason M2 split 3a from 3b.
dependencies {
    implementation(project(":core-domain"))
    // For `Throwable.toAiError` — Clarify fails in exactly the ways a provider call fails, so it
    // reuses A3's classifier rather than growing a second error vocabulary.
    implementation(project(":ai-providers"))
    implementation(project(":core-ui"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    // Reads the FORGE library from `res/raw/prompt_frameworks.json`. Not a new artifact — it is
    // already in the version catalog and already in the APK via :core-data and :ai-providers;
    // this only puts it on this module's compile path. Hard constraint 2 is about inventing
    // dependencies, and this one was checked against the catalog rather than assumed.
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
