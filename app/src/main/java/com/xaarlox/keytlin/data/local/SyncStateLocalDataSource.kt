package com.xaarlox.keytlin.data.local

import com.xaarlox.keytlin.data.local.db.SyncStateDao
import com.xaarlox.keytlin.data.local.db.SyncStateEntity
import com.xaarlox.keytlin.data.security.TokenCipher
import java.security.GeneralSecurityException

/** Decrypted synchronization state */
data class SyncState(
    val token: String,
    val serverVersion: Long,
    val syncedLocalVersion: Long,
    val lastSyncedAt: Long?
) {
    override fun toString() =
        "SyncState(serverVersion=$serverVersion, syncedLocalVersion=$syncedLocalVersion, lastSyncedAt=$lastSyncedAt)"
}

interface SyncStateLocalDataSource {

    suspend fun get(): SyncState?

    /** Binds the device to the server vault (after registration or recovery via code) */
    suspend fun save(state: SyncState)

    /** Records a successful synchronization */
    suspend fun markSynced(serverVersion: Long, syncedLocalVersion: Long, syncedAt: Long)

    suspend fun clear()
}

/** Saves the state in Room; the token is encrypted with a key from Android Keystore */
class RoomSyncStateLocalDataSource(
    private val dao: SyncStateDao,
    private val tokenCipher: TokenCipher
) : SyncStateLocalDataSource {

    override suspend fun get(): SyncState? {
        val entity = dao.get() ?: return null
        val token = try {
            tokenCipher.decrypt(entity.encryptedToken)
        } catch (e: GeneralSecurityException) {
            // Keystore key lost (e.g., database restored from backup on another device):
            // token cannot be read, the device must be rebound
            dao.deleteAll()
            return null
        }
        return SyncState(
            token = token,
            serverVersion = entity.serverVersion,
            syncedLocalVersion = entity.syncedLocalVersion,
            lastSyncedAt = entity.lastSyncedAt
        )
    }

    override suspend fun save(state: SyncState) = dao.upsert(
        SyncStateEntity(
            encryptedToken = tokenCipher.encrypt(state.token),
            serverVersion = state.serverVersion,
            syncedLocalVersion = state.syncedLocalVersion,
            lastSyncedAt = state.lastSyncedAt
        )
    )

    override suspend fun markSynced(serverVersion: Long, syncedLocalVersion: Long, syncedAt: Long) =
        dao.markSynced(serverVersion, syncedLocalVersion, syncedAt)

    override suspend fun clear() = dao.deleteAll()
}