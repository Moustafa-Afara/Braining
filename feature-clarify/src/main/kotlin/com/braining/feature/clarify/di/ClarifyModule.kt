package com.braining.feature.clarify.di

import com.braining.core.domain.clarify.ClarifyEngine
import com.braining.core.domain.clarify.PromptForge
import com.braining.feature.clarify.ClarifyEngineImpl
import com.braining.feature.clarify.PromptForgeImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds the CLARIFY engine.
 *
 * **Deliberately unscoped — there is no `@Singleton` here and that is the whole point.**
 * `ClarifyEngineImpl` holds the session. A singleton would survive the ViewModel that opened it,
 * so the next interrogation would start carrying the previous one's turns, and the user would be
 * answering questions about an idea they had already abandoned. `ANSWERS.md` Part 7 §M3-4 rules
 * that a Clarify session does **not** outlive its owner in M3; an unscoped binding is how that
 * ruling is enforced by the graph rather than by remembering to clear state.
 *
 * This is the mirror image of `SpeechModule`, which *is* `@Singleton` — one microphone, one
 * engine, no per-caller state. The difference is not style: it is whether the object holds
 * anything that belongs to one conversation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ClarifyModule {

    @Binds
    abstract fun bindClarifyEngine(impl: ClarifyEngineImpl): ClarifyEngine

    /**
     * Unscoped for the same reason, one step ahead of the problem: it holds no session state
     * today, but it is created beside something that does, and a `@Singleton` here would let
     * state be added later without anyone noticing the change in lifetime.
     */
    @Binds
    abstract fun bindPromptForge(impl: PromptForgeImpl): PromptForge
}
