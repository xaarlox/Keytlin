package com.xaarlox.keytlin.data.local

import com.xaarlox.keytlin.domain.models.Vault
import com.xaarlox.keytlin.domain.service.VaultSerializer
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest

/**
 * A temporary implementation of a local data source that keeps the storage in memory
 * in a serialized format (JSON bytes), exactly as it will be stored
 * in the database. Data is lost upon application restart
 */
class InMemoryVaultLocalDataSource(
    private val serializer: VaultSerializer = VaultSerializer(),
    initialMasterPassword: String? = null,
    initialVault: Vault = Vault()
) : VaultLocalDataSource {

    private val mutex = Mutex()
    private var masterPassword: String? = initialMasterPassword
    private var storedBytes: ByteArray? =
        initialMasterPassword?.let { serializer.toBytes(initialVault) }

    override suspend fun hasVault(): Boolean = mutex.withLock { storedBytes != null }

    override suspend fun create(masterPassword: String, vault: Vault) = mutex.withLock {
        this.masterPassword = masterPassword
        storedBytes = serializer.toBytes(vault)
    }

    override suspend fun read(masterPassword: String): Vault? = mutex.withLock {
        val bytes = storedBytes ?: return@withLock null
        val expected = this.masterPassword ?: return@withLock null
        if (!constantTimeEquals(expected, masterPassword)) return@withLock null
        serializer.fromBytes(bytes)
    }

    override suspend fun write(vault: Vault) = mutex.withLock {
        check(storedBytes != null) { "Vault has not been created" }
        storedBytes = serializer.toBytes(vault)
    }

    override suspend fun clear() = mutex.withLock {
        storedBytes?.fill(0)
        storedBytes = null
        masterPassword = null
    }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))
}