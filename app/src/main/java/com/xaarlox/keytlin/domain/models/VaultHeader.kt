package com.xaarlox.keytlin.domain.models

/**
 * Unencrypted (plaintext) part of the vault: everything needed to derive
 * the same key from the master password on any device. It is not secret,
 * but is protected against tampering — it is included in the encryption AAD
 */
class VaultHeader(
    val version: Int = CURRENT_VERSION,
    val salt: ByteArray,
    val kdfParams: KdfParams = KdfParams()
) {
    override fun equals(other: Any?): Boolean =
        other is VaultHeader &&
                version == other.version &&
                salt.contentEquals(other.salt) &&
                kdfParams == other.kdfParams

    override fun hashCode(): Int =
        31 * (31 * version + salt.contentHashCode()) + kdfParams.hashCode()

    companion object {
        const val CURRENT_VERSION = 1
        const val SALT_LENGTH = 16
    }
}