package com.xaarlox.keytlin.domain.crypto

import com.xaarlox.keytlin.domain.models.KdfParams

/**
 * Derives an encryption key from the master password (password-based key derivation).
 * Implemented using Argon2id on-device; the interface allows substituting a fast
 * implementation in unit tests
 */
interface KeyDerivationFunction {
    /** @return a key of length [KEY_LENGTH_BYTES] bytes */
    fun deriveKey(password: ByteArray, salt: ByteArray, params: KdfParams): ByteArray

    companion object {
        const val KEY_LENGTH_BYTES = 32 // AES-256
    }
}