package com.xaarlox.keytlin.domain.models

/**
 * Encrypted vault as stored in the database
 * and transmitted to the server: unencrypted header + encrypted content
 */
data class EncryptedVault(
    val header: VaultHeader,
    val payload: EncryptedPayload
)