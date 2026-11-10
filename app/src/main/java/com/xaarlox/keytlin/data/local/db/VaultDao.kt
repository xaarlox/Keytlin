package com.xaarlox.keytlin.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class VaultDao {

    @Query("SELECT EXISTS(SELECT 1 FROM vault WHERE id = ${VaultEntity.SINGLE_ROW_ID})")
    abstract fun observeExists(): Flow<Boolean>

    @Query("SELECT * FROM vault WHERE id = ${VaultEntity.SINGLE_ROW_ID}")
    abstract suspend fun get(): VaultEntity?

    @Query("SELECT local_version FROM vault WHERE id = ${VaultEntity.SINGLE_ROW_ID}")
    abstract suspend fun getLocalVersion(): Long?

    @Upsert
    abstract suspend fun upsert(entity: VaultEntity)

    @Query("DELETE FROM vault")
    abstract suspend fun deleteAll()

    /**
     * Saves a new encrypted version and atomically increments local_version.
     * @return the new local_version value
     */
    @Transaction
    open suspend fun saveNextVersion(entity: VaultEntity): Long {
        val next = (getLocalVersion() ?: 0L) + 1
        upsert(
            VaultEntity(
                formatVersion = entity.formatVersion,
                kdfMemoryKib = entity.kdfMemoryKib,
                kdfIterations = entity.kdfIterations,
                kdfParallelism = entity.kdfParallelism,
                salt = entity.salt,
                iv = entity.iv,
                authTag = entity.authTag,
                ciphertext = entity.ciphertext,
                localVersion = next,
                updatedAt = entity.updatedAt
            )
        )
        return next
    }
}