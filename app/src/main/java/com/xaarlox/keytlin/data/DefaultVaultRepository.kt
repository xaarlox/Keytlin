package com.xaarlox.keytlin.data

import com.xaarlox.keytlin.data.local.SyncState
import com.xaarlox.keytlin.data.local.SyncStateLocalDataSource
import com.xaarlox.keytlin.data.local.VaultLocalDataSource
import com.xaarlox.keytlin.data.remote.SyncRemoteDataSource
import com.xaarlox.keytlin.domain.crypto.InvalidVaultFormatException
import com.xaarlox.keytlin.domain.crypto.VaultBlobCodec
import com.xaarlox.keytlin.domain.crypto.VaultCrypto
import com.xaarlox.keytlin.domain.models.Vault
import com.xaarlox.keytlin.domain.models.VaultEntry
import com.xaarlox.keytlin.domain.models.VaultHeader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Unlocked in-memory vault: decrypted content + key derived from the master password.
 * The key is retained for the session to avoid executing slow Argon2id on every save.
 * Compared by reference (not a data class) — this is used in compareAndSet
 */
private class Session(
    val vault: Vault,
    val key: ByteArray,
    val header: VaultHeader
) {
    fun withVault(vault: Vault) = Session(vault, key, header)
    fun wipe() = key.fill(0)
}

/**
 * Vault repository
 *
 * - Read: the decrypted vault is held in [MutableStateFlow] (single source of truth),
 *   screens receive updates via Flow.
 * - Write: modification → AES-GCM encryption with session key → save to database → state update.
 *   Database first, then memory: the UI will never present data that is not on disk.
 * - Only encrypted bytes are stored in the database
 */
class DefaultVaultRepository(
    private val localDataSource: VaultLocalDataSource,
    private val syncStateDataSource: SyncStateLocalDataSource,
    private val remoteDataSource: SyncRemoteDataSource,
    private val crypto: VaultCrypto,
    private val codec: VaultBlobCodec,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = System::currentTimeMillis
) : VaultRepository {

    private val session = MutableStateFlow<Session?>(null)
    private val writeMutex = Mutex()

    override val isUnlocked: Flow<Boolean> =
        session.map { it != null }.distinctUntilChanged()

    override fun observeHasVault(): Flow<Boolean> = localDataSource.observeHasVault()

    override fun observeEntries(): Flow<List<VaultEntry>> =
        session.map { it?.vault?.activeEntries.orEmpty() }.distinctUntilChanged()

    override fun observeEntry(id: String): Flow<VaultEntry?> =
        session.map { it?.vault?.find(id) }.distinctUntilChanged()

    override suspend fun getEntry(id: String): VaultEntry? = session.value?.vault?.find(id)

    override suspend fun hasVault(): Boolean = withContext(ioDispatcher) {
        localDataSource.hasVault()
    }

    override suspend fun createVault(masterPassword: String) = withContext(ioDispatcher) {
        writeMutex.withLock {
            val header = crypto.newHeader()
            val key = crypto.deriveKey(masterPassword, header)
            val vault = Vault()

            // The new vault is not associated with any vault on the server
            syncStateDataSource.clear()
            localDataSource.clear()
            localDataSource.save(crypto.seal(vault, key, header))

            session.getAndUpdate { Session(vault, key, header) }?.wipe()
            Unit
        }
    }

    override suspend fun unlock(masterPassword: String): Boolean = withContext(ioDispatcher) {
        val stored = localDataSource.load() ?: return@withContext false
        val header = stored.encrypted.header
        val key = crypto.deriveKey(masterPassword, header)
        val vault = crypto.open(stored.encrypted, key)
        if (vault == null) {
            key.fill(0)
            return@withContext false
        }
        session.getAndUpdate { Session(vault, key, header) }?.wipe()
        true
    }

    override fun lock() {
        val previous = session.getAndUpdate { null } ?: return
        if (writeMutex.tryLock()) {
            try {
                previous.wipe()
            } finally {
                writeMutex.unlock()
            }
        }
    }

    override suspend fun saveEntry(entry: VaultEntry) = mutate { vault ->
        if (vault.find(entry.id) == null) vault.withEntry(entry) else vault.withUpdated(entry)
    }

    override suspend fun deleteEntry(id: String) =
        mutate { vault -> vault.withDeleted(id, clock()) }

    override suspend fun restoreFromSyncCode(code: String): Boolean = withContext(ioDispatcher) {
        val token = code.trim()
        if (token.isEmpty()) return@withContext false
        val remote = remoteDataSource.download(token) ?: return@withContext false
        val encrypted = try {
            codec.decode(remote.blob)
        } catch (e: InvalidVaultFormatException) {
            return@withContext false
        }

        writeMutex.withLock {
            session.getAndUpdate { null }?.wipe()
            localDataSource.clear()
            val localVersion = localDataSource.save(encrypted)
            syncStateDataSource.save(
                SyncState(
                    token = token,
                    serverVersion = remote.version,
                    syncedLocalVersion = localVersion,
                    lastSyncedAt = clock()
                )
            )
        }
        true
    }

    private suspend fun mutate(transform: (Vault) -> Vault) = withContext(ioDispatcher) {
        writeMutex.withLock {
            val current = session.value ?: throw VaultLockedException()
            val updated = transform(current.vault)
            localDataSource.save(crypto.seal(updated, current.key, current.header))

            if (!session.compareAndSet(current, current.withVault(updated))) current.wipe()
        }
    }
}