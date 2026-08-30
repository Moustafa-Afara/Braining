package com.braining.speech.di

import com.braining.core.domain.speech.SpeechToText
import com.braining.core.domain.speech.TextReader
import com.braining.speech.AndroidTextReader
import com.braining.speech.RoutingSpeechToText
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.serialization.json.Json
import javax.inject.Named
import javax.inject.Singleton

/**
 * Binds the router, not an engine.
 *
 * Nothing outside this module knows there are two engines. `ChatViewModel` injects
 * `SpeechToText` exactly as it did when there was one, which is what the interface was created
 * for in `2026-08-04-G` — and why adding a cloud engine touched no screen.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechModule {

    @Binds
    @Singleton
    abstract fun bindSpeechToText(impl: RoutingSpeechToText): SpeechToText

    /**
     * M5 readback. `@Singleton` for the same reason the recogniser is one: there is one speaker
     * on the device, and a second engine instance would talk over the first.
     *
     * It lands in **this** module rather than beside the screen that uses it, because
     * `android.speech.tts` is the same platform area `:speech` already owns — and because a
     * feature module must never learn which class implements a domain interface (`ANSWERS.md`
     * Part 1 §1, the indirection that let Deepgram replace `SpeechRecognizer` with no screen
     * changing).
     */
    @Binds
    @Singleton
    abstract fun bindTextReader(impl: AndroidTextReader): TextReader
}

/**
 * A WebSocket-capable client for this module alone.
 *
 * **Why not reuse the shared client from `:core-data`.** Installing the WebSockets plugin there
 * would put it on the compile and runtime path of every module in the app — `core-data` is
 * imported by everything, which is the same reasoning that kept the speech engines out of it in
 * the first place (`docs/M2_DESIGN_NOTE.md` §3). A second client instance costs a little memory;
 * a dependency in the wrong module costs a refactor later.
 *
 * `@Named` so it cannot be injected by accident where the plain provider client is meant.
 */
@Module
@InstallIn(SingletonComponent::class)
object SpeechNetworkModule {

    @Provides
    @Singleton
    @Named("speech")
    fun provideSpeechHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(WebSockets)
    }

    @Provides
    @Singleton
    @Named("speech")
    fun provideSpeechJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}
