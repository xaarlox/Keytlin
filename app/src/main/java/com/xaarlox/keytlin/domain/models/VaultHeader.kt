package com.xaarlox.keytlin.domain.models

class VaultHeader(
    val version: Int = CURRENT_VERSION,
    val salt: ByteArray,
    val kdfParams: KdfParams = KdfParams()
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}