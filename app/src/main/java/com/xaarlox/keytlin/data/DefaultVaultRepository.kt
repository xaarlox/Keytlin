package com.xaarlox.keytlin.data

import com.xaarlox.keytlin.data.local.VaultLocalDataSource
import com.xaarlox.keytlin.data.remote.SyncRemoteDataSource
import com.xaarlox.keytlin.domain.models.Vault
import com.xaarlox.keytlin.domain.models.VaultEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * A repository that holds the unlocked vault in [MutableStateFlow] (single source of truth)
 * and synchronously writes every change to [VaultLocalDataSource].
 *
 * Write order: data source first, then in-memory state. If the write
 * fails, the UI will not show changes that are not in the vault
 */
class DefaultVaultRepository(
    private val localDataSource: VaultLocalDataSource,
    private val remoteDataSource: SyncRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : VaultRepository {
    private val session = MutableStateFlow<Vault?>(null)
    private val writeMutex = Mutex()

    override val isUnlocked: Flow<Boolean> =
        session.map { it != null }.distinctUntilChanged()

    override fun observeEntries(): Flow<List<VaultEntry>> =
        session.map { it?.entries.orEmpty() }.distinctUntilChanged()

    override fun observeEntry(id: String): Flow<VaultEntry?> =
        session.map { it?.find(id) }.distinctUntilChanged()

    override suspend fun getEntry(id: String): VaultEntry? = session.value?.find(id)

    override suspend fun hasVault(): Boolean = withContext(ioDispatcher) {
        localDataSource.hasVault()
    }

    override suspend fun createVault(masterPassword: String) = withContext(ioDispatcher) {
        writeMutex.withLock {
            val vault = Vault()
            localDataSource.create(masterPassword, vault)
            session.value = vault
        }
    }

    override suspend fun unlock(masterPassword: String): Boolean = withContext(ioDispatcher) {
        val vault = localDataSource.read(masterPassword) ?: return@withContext false
        session.value = vault
        true
    }

    override fun lock() {
        session.value = null
    }

    override suspend fun saveEntry(entry: VaultEntry) = mutate { vault ->
        if (vault.find(entry.id) == null) vault.withEntry(entry) else vault.withUpdated(entry)
    }

    override suspend fun deleteEntry(id: String) = mutate { vault -> vault.without(id) }

    override suspend fun validateSyncCode(code: String): Boolean = withContext(ioDispatcher) {
        remoteDataSource.validateSyncCode(code)
    }

    private suspend fun mutate(transform: (Vault) -> Vault) = withContext(ioDispatcher) {
        writeMutex.withLock {
            val current = session.value ?: throw VaultLockedException()
            val updated = transform(current)
            localDataSource.write(updated)
            session.value = updated
        }
    }
}