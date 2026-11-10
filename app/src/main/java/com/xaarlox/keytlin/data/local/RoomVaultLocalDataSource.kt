package com.xaarlox.keytlin.data.local

import com.xaarlox.keytlin.data.local.db.VaultDao
import com.xaarlox.keytlin.data.local.db.VaultEntity
import com.xaarlox.keytlin.domain.models.EncryptedPayload
import com.xaarlox.keytlin.domain.models.EncryptedVault
import com.xaarlox.keytlin.domain.models.KdfParams
import com.xaarlox.keytlin.domain.models.VaultHeader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class RoomVaultLocalDataSource(
    private val dao: VaultDao,
    private val clock: () -> Long = System::currentTimeMillis
) : VaultLocalDataSource {

    override fun observeHasVault(): Flow<Boolean> = dao.observeExists().distinctUntilChanged()

    override suspend fun hasVault(): Boolean = dao.get() != null

    override suspend fun load(): StoredVault? = dao.get()?.toStoredVault()

    override suspend fun save(vault: EncryptedVault): Long =
        dao.saveNextVersion(vault.toEntity(updatedAt = clock()))

    override suspend fun clear() = dao.deleteAll()
}

private fun VaultEntity.toStoredVault() = StoredVault(
    encrypted = EncryptedVault(
        header = VaultHeader(
            version = formatVersion,
            salt = salt,
            kdfParams = KdfParams(
                memoryKib = kdfMemoryKib,
                iterations = kdfIterations,
                parallelism = kdfParallelism
            )
        ),
        payload = EncryptedPayload(iv = iv, ciphertext = ciphertext, authTag = authTag)
    ),
    localVersion = localVersion,
    updatedAt = updatedAt
)

/** localVersion is not important here — it is set by [VaultDao.saveNextVersion] */
private fun EncryptedVault.toEntity(updatedAt: Long) = VaultEntity(
    formatVersion = header.version,
    kdfMemoryKib = header.kdfParams.memoryKib,
    kdfIterations = header.kdfParams.iterations,
    kdfParallelism = header.kdfParams.parallelism,
    salt = header.salt,
    iv = payload.iv,
    authTag = payload.authTag,
    ciphertext = payload.ciphertext,
    localVersion = 0,
    updatedAt = updatedAt
)