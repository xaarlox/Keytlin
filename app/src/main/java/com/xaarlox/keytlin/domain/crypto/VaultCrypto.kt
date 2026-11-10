package com.xaarlox.keytlin.domain.crypto

import com.xaarlox.keytlin.domain.models.EncryptedVault
import com.xaarlox.keytlin.domain.models.KdfParams
import com.xaarlox.keytlin.domain.models.Vault
import com.xaarlox.keytlin.domain.models.VaultHeader
import com.xaarlox.keytlin.domain.service.VaultSerializer
import java.security.SecureRandom
import javax.crypto.AEADBadTagException

/**
 * Full vault encryption (zero-knowledge): master password → Argon2id → key →
 * AES-256-GCM over JSON content. Header (salt, KDF parameters) is included in AAD,
 * so it cannot be tampered with unnoticed
 */
class VaultCrypto(
    private val kdf: KeyDerivationFunction,
    private val cipher: AesGcmCipher,
    private val serializer: VaultSerializer,
    private val codec: VaultBlobCodec,
    private val random: SecureRandom = SecureRandom(),
    private val defaultKdfParams: KdfParams = KdfParams()
) {

    /** New header with a random salt — used when creating a vault or changing the password */
    fun newHeader(): VaultHeader = VaultHeader(
        salt = ByteArray(VaultHeader.SALT_LENGTH).also(random::nextBytes),
        kdfParams = defaultKdfParams
    )

    /** Slow operation (Argon2id): do not call on the main thread */
    fun deriveKey(masterPassword: String, header: VaultHeader): ByteArray {
        val passwordBytes = masterPassword.toByteArray(Charsets.UTF_8)
        return try {
            kdf.deriveKey(passwordBytes, header.salt, header.kdfParams)
        } finally {
            passwordBytes.fill(0)
        }
    }

    fun seal(vault: Vault, key: ByteArray, header: VaultHeader): EncryptedVault {
        val plaintext = serializer.toBytes(vault)
        return try {
            EncryptedVault(header, cipher.encrypt(plaintext, key, codec.headerBytes(header)))
        } finally {
            plaintext.fill(0)
        }
    }

    /** @return decrypted vault or null if the key (password) is incorrect or data is corrupted */
    fun open(encrypted: EncryptedVault, key: ByteArray): Vault? {
        val plaintext = try {
            cipher.decrypt(encrypted.payload, key, codec.headerBytes(encrypted.header))
        } catch (e: AEADBadTagException) {
            return null
        }
        return try {
            serializer.fromBytes(plaintext)
        } finally {
            plaintext.fill(0)
        }
    }
}