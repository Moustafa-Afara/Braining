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
