package com.xaarlox.keytlin.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

/** Local application database (SQLite via Room) */
@Database(
    entities = [VaultEntity::class, SyncStateEntity::class],
    version = 1,
    exportSchema = true
)
abstract class KeytlinDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao
    abstract fun syncStateDao(): SyncStateDao

    companion object {
        const val NAME = "keytlin.db"
    }
}