# Braining ProGuard Rules

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.braining.**$$serializer { *; }
-keepclassmembers class com.braining.** {
    *** Companion;
}
-keepclasseswithmembers class com.braining.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ─────────────────────────────────────────────────────────────────────────────────────────
# M5 — added 2026-08-28, for the project's FIRST release build.
#
# Everything above this line has only ever run in a debug build, where R8 does not run at all.
# `PROJECT_STATE.md` §10 entry 3 is about publishing confident causes from partial evidence, so
# read this section as what it is: a set of rules that have not yet been proven by a build.
# `docs/TESTS_PENDING.md` asks for the release build as its own step for exactly that reason.
# ─────────────────────────────────────────────────────────────────────────────────────────

# Generic signatures. kotlinx-serialization resolves List<ClarifyTurn> at runtime through the
# generic type information; without Signature, a serialized list decodes as raw Any and throws.
-keepattributes Signature, RuntimeVisibleAnnotations, AnnotationDefault

# Room. Its own consumer rules cover most of this, but the generated implementation classes and
# the entity are named by reflection at open time — a renamed entity surfaces as "no such table".
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-keep class com.braining.core.data.history.** { *; }
-dontwarn androidx.room.paging.**

# The serialized domain types. `ClarifyTurn` is a sealed hierarchy written to disk; its
# `@SerialName` values are stable by design, but the generated serializers must survive.
-keep class com.braining.core.domain.clarify.** { *; }
-keepclassmembers class com.braining.core.domain.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# TextToSpeech's progress listener is called back by the platform.
-keep class * extends android.speech.tts.UtteranceProgressListener { *; }

# ─────────────────────────────────────────────────────────────────────────────────────────
# The first `assembleRelease` in this project's life, 2026-08-28. It failed at R8 with four
# missing classes, and R8 wrote the fix itself into
# `app/build/intermediates/mapping/release/minifyReleaseWithR8/missing_rules.txt`.
# What follows is that file, verbatim, plus the reasoning R8 does not print.
# ─────────────────────────────────────────────────────────────────────────────────────────

# **ErrorProne annotations, from Tink — and they are MEANT to be absent.**
#
# `androidx.security:security-crypto` (the encrypted key store) is built on Google Tink, whose
# classes carry ErrorProne annotations. Those annotations are a **compile-time** contract for
# Google's own static analyser: they have `CLASS`/`SOURCE` retention, they are never packaged
# into an APK, and nothing reads them at runtime. R8 sees the references, cannot resolve them,
# and refuses to continue without being told.
#
# **`-dontwarn` here is the documented fix, not a workaround.** It says "this class is genuinely
# not needed", which is true — as opposed to `-keep`, which would ask R8 to preserve something
# that does not exist in the first place. If any of these were real runtime dependencies, the
# app would already be crashing in debug.
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi

# The same family, in case Tink or Guava reaches for another one on a later build. A `-dontwarn`
# for a class nothing references is a no-op, so the cost of this line is zero and the cost of
# omitting it is another two-and-a-half-minute build.
-dontwarn com.google.errorprone.annotations.**

# **JSR-305, from Dagger/Hilt.** Same shape: `javax.annotation.Nullable` and friends are a
# compile-time nullability contract. Documented by Dagger as safe to suppress.
-dontwarn javax.annotation.**

# **OkHttp's optional TLS providers.** OkHttp compiles against Conscrypt, BouncyCastle and
# OpenJSSE so that an app *may* supply one; none of them is on this app's classpath and none is
# wanted. Square's own published rules are exactly these lines.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# **Ktor's optional logging facade.** No logging artifact is declared in this project, so the
# reference is dangling by design.
-dontwarn org.slf4j.**
