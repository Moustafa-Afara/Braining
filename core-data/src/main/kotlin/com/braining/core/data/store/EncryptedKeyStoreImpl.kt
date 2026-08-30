package com.braining.core.data.store

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.braining.core.domain.store.EncryptedKeyStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BYOK key store backed by EncryptedSharedPreferences.
 *
 * Hard requirement: this class MUST NOT throw. A BYOK app that is distributed to other
 * people runs on Keystore implementations of very uneven quality (MIUI, EMUI, custom
 * ROMs, restored backups). If the encrypted blob can no longer be decrypted the correct
 * behaviour is "the saved keys are gone, ask the user to re-enter them" — never a crash
 * loop. Every public method therefore degrades to null / emptyMap instead of failing.
 */
@Singleton
class EncryptedKeyStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : EncryptedKeyStore {

    @Volatile
    private var cachedPrefs: SharedPreferences? = null

    // MasterKey.Builder is idempotent: it creates the AndroidKeyStore key only if the
    // alias is empty, otherwise it reuses the existing one. We must NOT generate the key
    // ourselves — doing so rotated the key on every cold start and made the previously
    // written keyset undecryptable (javax.crypto.AEADBadTagException on the next launch).
    private fun buildMasterKey(): MasterKey =
        MasterKey.Builder(context, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private fun openPrefs(): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE,
        buildMasterKey(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    /** Drops both halves of the broken pair: the Tink keyset file and the master key. */
    private fun resetStore() {
        runCatching { context.deleteSharedPreferences(PREFS_FILE) }
            .onFailure { Log.w(TAG, "Could not delete $PREFS_FILE", it) }
        runCatching {
            KeyStore.getInstance(ANDROID_KEYSTORE)
                .apply { load(null) }
                .deleteEntry(MASTER_KEY_ALIAS)
        }.onFailure { Log.w(TAG, "Could not delete master key alias", it) }
    }

    /**
     * Returns the encrypted prefs, or null if this device cannot provide them at all.
     * On the first failure the store is reset once and re-created; a second failure is
     * logged and swallowed.
     */
    private fun prefs(): SharedPreferences? {
        cachedPrefs?.let { return it }
        return synchronized(this) {
            var store = cachedPrefs
            if (store == null) {
                store = runCatching { openPrefs() }
                    .onFailure { Log.w(TAG, "Encrypted key store unreadable — resetting it", it) }
                    .getOrNull()
            }
            if (store == null) {
                resetStore()
                store = runCatching { openPrefs() }
                    .onFailure { Log.e(TAG, "Encrypted key store unavailable on this device", it) }
                    .getOrNull()
            }
            cachedPrefs = store
            store
        }
    }

    override suspend fun saveKey(providerId: String, apiKey: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                prefs()?.edit()?.putString(KEY_PREFIX + providerId, apiKey)?.apply()
            }.onFailure { Log.w(TAG, "saveKey failed for $providerId", it) }
        }
    }

    override suspend fun getKey(providerId: String): String? = withContext(Dispatchers.IO) {
        runCatching { prefs()?.getString(KEY_PREFIX + providerId, null) }
            .onFailure { Log.w(TAG, "getKey failed for $providerId", it) }
            .getOrNull()
    }

    override suspend fun deleteKey(providerId: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                prefs()?.edit()?.remove(KEY_PREFIX + providerId)?.apply()
            }.onFailure { Log.w(TAG, "deleteKey failed for $providerId", it) }
        }
    }

    override suspend fun getAllKeys(): Map<String, String> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<String, String>()
        runCatching {
            val all = prefs()?.all
            if (all != null) {
                for ((entryKey, entryValue) in all) {
                    if (entryKey.startsWith(KEY_PREFIX) && entryValue is String) {
                        result[entryKey.removePrefix(KEY_PREFIX)] = entryValue
                    }
                }
            }
        }.onFailure { Log.w(TAG, "getAllKeys failed", it) }
        result
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            runCatching { prefs()?.edit()?.clear()?.apply() }
                .onFailure { Log.w(TAG, "clear failed", it) }
        }
    }

    companion object {
        private const val TAG = "EncryptedKeyStore"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "braining_master_key"
        private const val PREFS_FILE = "braining_encrypted_prefs"
        private const val KEY_PREFIX = "api_key_"
    }
}
