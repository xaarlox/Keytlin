package com.xaarlox.keytlin.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The vault table: one row — one encrypted vault on the device
 *
 * There are no decrypted passwords in the database: only the unencrypted header (salt, KDF parameters)
 * and AES-GCM encrypted content. The structure mirrors the server's `VaultRecord` table,
 * so synchronization is simply transferring these fields as a single blob
 */
@Entity(tableName = "vault")
class VaultEntity(
    @PrimaryKey
    val id: Int = SINGLE_ROW_ID,

    @ColumnInfo(name = "format_version")
    val formatVersion: Int,

    @ColumnInfo(name = "kdf_memory_kib")
    val kdfMemoryKib: Int,

    @ColumnInfo(name = "kdf_iterations")
    val kdfIterations: Int,

    @ColumnInfo(name = "kdf_parallelism")
    val kdfParallelism: Int,

    @ColumnInfo(name = "salt", typeAffinity = ColumnInfo.BLOB)
    val salt: ByteArray,

    @ColumnInfo(name = "iv", typeAffinity = ColumnInfo.BLOB)
    val iv: ByteArray,

    @ColumnInfo(name = "auth_tag", typeAffinity = ColumnInfo.BLOB)
    val authTag: ByteArray,

    @ColumnInfo(name = "ciphertext", typeAffinity = ColumnInfo.BLOB)
    val ciphertext: ByteArray,

    /** Local changes counter: +1 on each save. Compared with sync_state.synced_local_version */
    @ColumnInfo(name = "local_version")
    val localVersion: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}