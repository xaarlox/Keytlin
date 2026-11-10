package com.xaarlox.keytlin.domain.crypto

import com.xaarlox.keytlin.domain.models.EncryptedPayload
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM: authenticated encryption. Any modification of the ciphertext,
 * IV, tag, or AAD (header) results in an [AEADBadTagException] during decryption —
 * just like an incorrect key
 */
class AesGcmCipher(
    private val random: SecureRandom = SecureRandom()
) {

    fun encrypt(plaintext: ByteArray, key: ByteArray, aad: ByteArray): EncryptedPayload {
        require(key.size == KeyDerivationFunction.KEY_LENGTH_BYTES) { "Key must be 256-bit" }

        // A new random IV for each encryption
        // Reusing an IV with the same key is not allowed in GCM

        val iv = ByteArray(IV_LENGTH_BYTES).also(random::nextBytes)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(key, ALGORITHM), GCMParameterSpec(TAG_LENGTH_BITS, iv)
            )
            updateAAD(aad)
        }

        // JCA returns ciphertext || tag

        val output = cipher.doFinal(plaintext)
        val tagStart = output.size - TAG_LENGTH_BYTES
        return EncryptedPayload(
            iv = iv,
            ciphertext = output.copyOfRange(0, tagStart),
            authTag = output.copyOfRange(tagStart, output.size)
        ).also { output.fill(0) }
    }

    /** @throws AEADBadTagException if the key is incorrect or the data is corrupted */
    fun decrypt(payload: EncryptedPayload, key: ByteArray, aad: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key, ALGORITHM),
                GCMParameterSpec(TAG_LENGTH_BITS, payload.iv)
            )
            updateAAD(aad)
        }
        return cipher.doFinal(payload.ciphertext + payload.authTag)
    }

    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH_BYTES = 12
        const val TAG_LENGTH_BYTES = 16
        private const val TAG_LENGTH_BITS = TAG_LENGTH_BYTES * 8
    }
}