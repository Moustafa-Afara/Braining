plugins {
    id("braining.android.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.braining.feature.history"
}

// A module of its own, and the boundary is earned rather than guessed.
//
// `feature-clarify/build.gradle.kts` argues that a boundary drawn before there is a second
// consumer is a guess. This one has two consumers on day one: chat opens the list, and clarify is
// re-entered from it. It also has its own state, its own screen and its own ViewModel, and it
// reads the history repository that no other feature module touches.
//
// **It does NOT depend on :feature-clarify**, and must not — feature modules are siblings (hard
// constraint 8, paid for twice). Re-running a saved session is done by navigating with the
// session id, which the app module's NavGraph routes; the two features never see each other.
dependencies {
    implementation(project(":core-domain"))
    implementation(project(":core-ui"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
