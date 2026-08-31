package com.braining.ai.providers.di

import com.braining.core.domain.provider.AiProvider
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.routing.DefaultModelRouter
import com.braining.core.domain.routing.ModelRouter
import com.braining.ai.providers.anthropic.AnthropicProvider
import com.braining.ai.providers.deepseek.DeepSeekProvider
import com.braining.ai.providers.gemini.GeminiProvider
import com.braining.ai.providers.ollama.OllamaProvider
import com.braining.ai.providers.openai.OpenAiProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvidersModule {

    @Provides
    @IntoMap
    @StringKey("ANTHROPIC")
    fun provideAnthropic(impl: AnthropicProvider): AiProvider = impl

    @Provides
    @IntoMap
    @StringKey("OPENAI")
    fun provideOpenAi(impl: OpenAiProvider): AiProvider = impl

    @Provides
    @IntoMap
    @StringKey("DEEPSEEK")
    fun provideDeepSeek(impl: DeepSeekProvider): AiProvider = impl

    @Provides
    @IntoMap
    @StringKey("GEMINI")
    fun provideGemini(impl: GeminiProvider): AiProvider = impl

    /**
     * The user's own machine.
     *
     * Bound the same way as the four remote providers, so every screen that iterates the map gets
     * it for free — the provider menu, the fallback chooser, Settings. Its differences (no key,
     * an address from preferences, a PC that can be asleep) live inside the class, not in the
     * graph, which is what keeps the callers from having to know about them.
     *
     * **Also bound by its concrete type**, below: the Settings screen needs `OllamaProvider.probe`
     * to test the connection, and `AiProvider` has no such method — nor should it, since no other
     * provider can be *asleep*.
     */
    @Provides
    @IntoMap
    @StringKey("OLLAMA")
    fun provideOllama(impl: OllamaProvider): AiProvider = impl

    /**
     * The router — **bound here, implemented in `:core-domain`.**
     *
     * It chooses between the providers this module supplies, so this is where it belongs on the
     * graph. The class itself is pure Kotlin with no Hilt annotations and lives in the domain
     * module, for one reason that outweighs tidiness: `:core-domain` already declares a JUnit
     * test dependency, so the router could be unit-tested without touching a single build file.
     * `ANSWERS.md` Part 1 §9 asked for exactly that, and this build has twice been broken by
     * dependency changes.
     *
     * Stateless, so a singleton costs nothing and saves an allocation per screen.
     */
    @Provides
    @Singleton
    fun provideModelRouter(): ModelRouter = DefaultModelRouter()
}
