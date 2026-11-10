package com.xaarlox.keytlin.data

import com.xaarlox.keytlin.domain.models.VaultEntry
import kotlinx.coroutines.flow.Flow

/**
 * Single point of access to vault data for the presentation layer
 *
 * Reads occur via [Flow]: any data change (addition, editing,
 * deletion, locking) automatically propagates to all subscribed screens
 */
interface VaultRepository {

    val isUnlocked: Flow<Boolean>

    fun observeHasVault(): Flow<Boolean>

    fun observeEntries(): Flow<List<VaultEntry>>

    fun observeEntry(id: String): Flow<VaultEntry?>

    suspend fun getEntry(id: String): VaultEntry?

    suspend fun hasVault(): Boolean

    suspend fun createVault(masterPassword: String)

    suspend fun unlock(masterPassword: String): Boolean

    fun lock()

    suspend fun saveEntry(entry: VaultEntry)

    suspend fun deleteEntry(id: String)

    suspend fun restoreFromSyncCode(code: String): Boolean
}

class VaultLockedException : IllegalStateException("Vault is locked")