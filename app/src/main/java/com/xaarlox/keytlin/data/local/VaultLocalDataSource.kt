package com.xaarlox.keytlin.data.local

import com.xaarlox.keytlin.domain.models.EncryptedVault
import kotlinx.coroutines.flow.Flow

/** Encrypted vault along with the local version number */
class StoredVault(
    val encrypted: EncryptedVault,
    val localVersion: Long,
    val updatedAt: Long
)

/**
 * Local storage for encrypted data. Has no knowledge of the master password
 * or decryption — operates strictly with [EncryptedVault] bytes
 */
interface VaultLocalDataSource {

    fun observeHasVault(): Flow<Boolean>

    suspend fun hasVault(): Boolean

    suspend fun load(): StoredVault?

    /**
     * Saves a new version of the vault.
     * @return the new localVersion (incremented by 1 on each save)
     */
    suspend fun save(vault: EncryptedVault): Long

    suspend fun clear()
}