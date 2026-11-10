package com.xaarlox.keytlin.data.remote

import com.xaarlox.keytlin.data.DemoVaultData
import com.xaarlox.keytlin.domain.crypto.VaultBlobCodec
import com.xaarlox.keytlin.domain.crypto.VaultCrypto
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Vault record on the server: encrypted blob and its version */
class RemoteVault(
    val blob: ByteArray,
    val version: Long
)

/**
 * Remote data source (sync server, /api/vaults/).
 * The server stores only encrypted bytes and the version number
 */
interface SyncRemoteDataSource {
    suspend fun download(token: String): RemoteVault?
}

/** Server simulation until a real backend is available */
class FakeSyncRemoteDataSource(
    private val crypto: VaultCrypto,
    private val codec: VaultBlobCodec,
    private val networkDelayMs: Long = 300L
) : SyncRemoteDataSource {

    private val mutex = Mutex()
    private var demoVault: RemoteVault? = null

    override suspend fun download(token: String): RemoteVault? {
        delay(networkDelayMs)
        if (token.trim() != DEMO_TOKEN) return null
        return mutex.withLock { demoVault ?: createDemoVault().also { demoVault = it } }
    }

    private fun createDemoVault(): RemoteVault {
        val header = crypto.newHeader()
        val key = crypto.deriveKey(DemoVaultData.MASTER_PASSWORD, header)
        try {
            val encrypted = crypto.seal(DemoVaultData.vault, key, header)
            return RemoteVault(blob = codec.encode(encrypted), version = 1)
        } finally {
            key.fill(0)
        }
    }

    companion object {
        const val DEMO_TOKEN = "1234"
    }
}