import java.util.Properties

plugins {
    id("braining.android.application")
    id("com.google.devtools.ksp")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(keystorePropertiesFile.inputStream())
    }
}

android {
    namespace = "com.braining.app"

    defaultConfig {
        applicationId = "com.braining.app"

        // ─────────────────────────────────────────────────────────────────────────────────
        // **RAISE BOTH BEFORE EVERY BUILD YOU SHARE.** This is the only place they are written.
        //
        // `versionCode` is the number **Android** compares: an update whose code is not higher
        // than the installed one is refused, silently, as "already installed". `versionName` is
        // the number a **person** reads — it appears in Settings so that a friend reporting a
        // fault can say which build they have.
        //
        // They were left at 1 / "1.0.0" in the convention plugin until 2026-08-30, which meant
        // every release the owner produced claimed to be the same one. That is harmless while
        // nothing is distributed and unrecoverable once it is: two friends on two different
        // builds, both reporting "1.0.0", and no update able to reach either.
        //
        // 2 / "1.1.0" — the M5.1 batch plus sharing. Version 1 / "1.0.0" was the first signed
        // APK, built 2026-08-30.
        // ─────────────────────────────────────────────────────────────────────────────────
        versionCode = 2
        versionName = "1.1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreProperties["storeFile"]?.let { file(it as String) }
            storePassword = keystoreProperties["storePassword"] as? String ?: ""
            keyAlias = keystoreProperties["keyAlias"] as? String ?: ""
            keyPassword = keystoreProperties["keyPassword"] as? String ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":core-data"))
    implementation(project(":core-ui"))
    implementation(project(":ai-providers"))
    // M2. The app module is where the SpeechModule binding has to land: feature-chat consumes
    // the SpeechToText interface from core-domain and must not know which engine implements it
    // — that indirection is the whole reason Vosk can replace SpeechRecognizer without the UI
    // changing (ANSWERS.md Part 1 §1).
    implementation(project(":speech"))
    implementation(project(":feature-settings"))
    implementation(project(":feature-chat"))
    // M3. Same reasoning as :speech above — the app module is where the Hilt binding lands so
    // that no feature module has to know which class implements ClarifyEngine. Nothing calls it
    // yet; this slice deliberately ships the module and the binding on their own, exactly as M2
    // step 3a shipped the speech engine before any screen used it.
    implementation(project(":feature-clarify"))
    // M5. Same reasoning again: the app module is the only place that may know both features
    // exist, because it is the only place that routes between them. `:feature-history` does not
    // depend on `:feature-clarify` and must never be made to — re-running a saved session travels
    // through the NavGraph as an id, not as a call.
    implementation(project(":feature-history"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    // Hosts AppCompatActivity so AppCompatDelegate.setApplicationLocales can drive the
    // in-app Arabic/English switch (the only per-app locale path on minSdk 26).
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
}
