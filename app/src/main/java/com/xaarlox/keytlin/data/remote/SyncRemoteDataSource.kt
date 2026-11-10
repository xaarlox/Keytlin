package com.xaarlox.keytlin.data.remote

import kotlinx.coroutines.delay

/**
 * Remote data source (synchronization server).
 * The actual implementation will be added along with the backend
 */
interface SyncRemoteDataSource {
    suspend fun validateSyncCode(code: String): Boolean
}

class FakeSyncRemoteDataSource(
    private val networkDelayMs: Long = 300L
) : SyncRemoteDataSource {

    override suspend fun validateSyncCode(code: String): Boolean {
        delay(networkDelayMs)
        return code.trim() == VALID_CODE
    }

    companion object {
        const val VALID_CODE = "1234"
    }
}