package com.xaarlox.keytlin.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The sync_state table: synchronization state with the server (single row)
 *
 * - encrypted_token — vault token on the server, encrypted with a key from Android Keystore;
 * - server_version — server version from which the local copy originates
 *   (sent in PUT as base_version; if the server version is newer — 409 conflict and merge);
 * - synced_local_version — vault.local_version value at the time of the last synchronization.
 *   If vault.local_version > synced_local_version, there are unsent changes
 */
@Entity(tableName = "sync_state")
class SyncStateEntity(
    @PrimaryKey
    val id: Int = SINGLE_ROW_ID,

    @ColumnInfo(name = "encrypted_token", typeAffinity = ColumnInfo.BLOB)
    val encryptedToken: ByteArray,

    @ColumnInfo(name = "server_version")
    val serverVersion: Long,

    @ColumnInfo(name = "synced_local_version")
    val syncedLocalVersion: Long,

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long?
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}