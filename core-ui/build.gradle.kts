plugins {
    id("braining.android.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.braining.core.ui"
}

dependencies {
    // `api`, not `implementation`: core-ui's public surface now *exposes* a core-domain type —
    // AiError is the receiver of AiErrorMessage.kt's toUserMessage(). Consumers must be able
    // to resolve it. Both current consumers happen to declare core-domain themselves, so
    // `implementation` would compile today and break for the next module that does not.
    api(project(":core-domain"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.navigation.compose)
}
