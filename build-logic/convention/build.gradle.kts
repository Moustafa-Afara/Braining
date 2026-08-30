import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.plugin.use.PluginDependency

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.plugins.android.application.toDependency())
    implementation(libs.plugins.android.library.toDependency())
    implementation(libs.plugins.kotlin.android.toDependency())
    implementation(libs.plugins.kotlin.compose.toDependency())
    implementation(libs.plugins.ksp.toDependency())
    implementation(libs.plugins.kotlin.serialization.toDependency())
    implementation(libs.plugins.hilt.toDependency())
}

fun Provider<*>.toDependency(): String {
    val plugin = get()
    val pluginId = when (plugin) {
        is PluginDependency -> plugin.pluginId
        else -> plugin.toString()
    }
    return when {
        pluginId.contains("com.android.application") ->
            "com.android.tools.build:gradle:${libs.versions.agp.get()}"
        pluginId.contains("com.android.library") ->
            "com.android.tools.build:gradle:${libs.versions.agp.get()}"
        pluginId.contains("org.jetbrains.kotlin.android") ->
            "org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}"
        pluginId.contains("org.jetbrains.kotlin.plugin.compose") ->
            "org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin.get()}"
        pluginId.contains("com.google.devtools.ksp") ->
            "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}"
        pluginId.contains("com.google.dagger.hilt") ->
            "com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.get()}"
        pluginId.contains("org.jetbrains.kotlin.plugin.serialization") ->
            "org.jetbrains.kotlin:kotlin-serialization:${libs.versions.kotlin.get()}"
        else -> error("Unknown plugin: $pluginId")
    }
}
