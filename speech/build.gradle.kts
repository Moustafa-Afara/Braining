plugins {
    id("braining.android.library")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.braining.speech"
}

// A module of its own, by analogy with :ai-providers — a family of interchangeable engines
// behind one domain interface. `docs/M2_DESIGN_NOTE.md` §3 is explicit that this must NOT go
// into :core-data: Vosk, the pre-approved fallback, weighs ~30 MB, and core-data is imported
// by everything. A heavy optional dependency belongs where it can be swapped, not where it is
// unavoidable.
dependencies {
    implementation(project(":core-domain"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Deepgram live transcription. The WebSocket artifact is the only new dependency in the
    // repo for this feature; the client and engine are the same ones :core-data already uses,
    // so no second HTTP stack enters the APK.
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.websockets)
    implementation(libs.kotlinx.serialization.json)
}
