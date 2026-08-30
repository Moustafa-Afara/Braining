package com.braining.core.domain.store

interface EncryptedKeyStore {
    suspend fun saveKey(providerId: String, apiKey: String)
    suspend fun getKey(providerId: String): String?
    suspend fun deleteKey(providerId: String)
    suspend fun getAllKeys(): Map<String, String>
    suspend fun clear()
}
