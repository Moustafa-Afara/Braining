plugins {
    id("braining.android.library")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.braining.core.data"
}

// Room writes the schema of every version here so the migration to version 2 can be written
// against the real version 1 instead of someone's memory of it. `BrainingDatabase` deliberately
// has no destructive fallback, which makes that file the only safe route to a schema change.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":core-domain"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    // M5 history.
    //
    // **Room 2.8.4, not the 2.6.1 that sat unused in the catalog.** `PROJECT_STATE.md` §8 said
    // "use it; do not upgrade it", and that instruction was written before Room had ever been
    // asked to run in this build. It cannot: 2.6.1 is from 2023 and has no KSP2 support, and this
    // project's KSP is KSP2. The first `installDebug` proved it in one line —
    // `IllegalStateException: unexpected jvm signature V` out of `KspAAWorkerAction`.
    //
    // **No `room-ktx`.** Since 2.7.0 that artifact is empty and Room's own release note asks for
    // it to be removed; its APIs are in `room-runtime`.
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

}
