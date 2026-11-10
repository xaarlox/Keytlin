package com.xaarlox.keytlin.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SyncStateDao {

    @Query("SELECT * FROM sync_state WHERE id = ${SyncStateEntity.SINGLE_ROW_ID}")
    suspend fun get(): SyncStateEntity?

    @Upsert
    suspend fun upsert(entity: SyncStateEntity)

    @Query(
        "UPDATE sync_state SET server_version = :serverVersion, " +
                "synced_local_version = :syncedLocalVersion, last_synced_at = :syncedAt " +
                "WHERE id = ${SyncStateEntity.SINGLE_ROW_ID}"
    )
    suspend fun markSynced(serverVersion: Long, syncedLocalVersion: Long, syncedAt: Long)

    @Query("DELETE FROM sync_state")
    suspend fun deleteAll()
}