package com.braining.core.data.di

import android.content.Context
import androidx.room.Room
import com.braining.core.data.history.BrainingDatabase
import com.braining.core.data.history.SessionDao
import com.braining.core.data.history.SessionRepositoryImpl
import com.braining.core.data.store.AppPreferencesImpl
import com.braining.core.data.store.EncryptedKeyStoreImpl
import com.braining.core.domain.history.SessionRepository
import com.braining.core.domain.store.AppPreferences
import com.braining.core.domain.store.EncryptedKeyStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreDataModule {

    @Binds
    @Singleton
    abstract fun bindEncryptedKeyStore(impl: EncryptedKeyStoreImpl): EncryptedKeyStore

    @Binds
    @Singleton
    abstract fun bindAppPreferences(impl: AppPreferencesImpl): AppPreferences

    /**
     * M5. Singleton because it wraps one database handle — the opposite of `ClarifyModule`'s
     * unscoped binding, and for the reason recorded there: this object holds nothing that belongs
     * to one conversation.
     */
    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    companion object {
        /**
         * The history database.
         *
         * **No `fallbackToDestructiveMigration`** — see `BrainingDatabase`'s KDoc. That one line
         * would trade a loud developer-side failure for the silent deletion of every session the
         * user has, against a ruling that keeps their text until *they* delete it.
         */
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): BrainingDatabase =
            Room.databaseBuilder(context, BrainingDatabase::class.java, BrainingDatabase.NAME)
                // Every migration, listed. A version bump without its migration here fails
                // loudly on the developer's next run — which is the whole reason there is no
                // destructive fallback to swallow it.
                .addMigrations(BrainingDatabase.MIGRATION_1_2)
                .build()

        @Provides
        @Singleton
        fun provideSessionDao(db: BrainingDatabase): SessionDao = db.sessionDao()

        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        }

        @Provides
        @Singleton
        fun provideHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json)
            }
            install(SSE)

            // Without this, OkHttp's ~10 s read default applies, so a provider that stalls
            // before its first token fails with a raw socket exception instead of a legible
            // message. HttpTimeout lives in ktor-client-core — no new dependency (hard
            // constraint 2). The request budget is deliberately generous: streaming reads
            // for minutes; the socket budget is the real guard against a stalled provider,
            // and its timeout surfaces as the typed AiError.Timeout in the UI.
            install(HttpTimeout) {
                requestTimeoutMillis = 600_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 60_000
            }
        }
    }
}
