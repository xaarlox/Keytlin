package com.xaarlox.keytlin.domain.crypto

import com.xaarlox.keytlin.domain.models.EncryptedPayload
import com.xaarlox.keytlin.domain.models.EncryptedVault
import com.xaarlox.keytlin.domain.models.KdfParams
import com.xaarlox.keytlin.domain.models.VaultHeader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * Binary format of the vault file (the same blob that goes to the server).
 * Numbers are written in big-endian ([DataOutputStream])
 */
class VaultBlobCodec {

    fun encode(vault: EncryptedVault): ByteArray = write { out ->
        writeHeader(out, vault.header)
        out.writeBytesWithLength(vault.payload.iv)
        out.writeBytesWithLength(vault.payload.authTag)
        out.writeBytesWithLength(vault.payload.ciphertext)
    }

    /** @throws InvalidVaultFormatException if the data is not a Keytlin vault */
    fun decode(bytes: ByteArray): EncryptedVault = try {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            val header = readHeader(input)
            val iv = input.readBytesWithLength(MAX_SMALL_FIELD)
            val tag = input.readBytesWithLength(MAX_SMALL_FIELD)
            val ciphertext = input.readBytesWithLength(MAX_CIPHERTEXT)
            if (input.available() != 0) throw InvalidVaultFormatException("Trailing bytes")
            EncryptedVault(header, EncryptedPayload(iv, ciphertext, tag))
        }
    } catch (e: IOException) {
        throw InvalidVaultFormatException("Truncated vault data", e)
    }

    /** Header bytes — used as AAD for AES-GCM */
    fun headerBytes(header: VaultHeader): ByteArray = write { writeHeader(it, header) }

    private fun writeHeader(out: DataOutputStream, header: VaultHeader) {
        out.write(MAGIC)
        out.writeInt(header.version)
        out.writeInt(header.kdfParams.memoryKib)
        out.writeInt(header.kdfParams.iterations)
        out.writeInt(header.kdfParams.parallelism)
        out.writeBytesWithLength(header.salt)
    }

    private fun readHeader(input: DataInputStream): VaultHeader {
        val magic = ByteArray(MAGIC.size).also(input::readFully)
        if (!magic.contentEquals(MAGIC)) throw InvalidVaultFormatException("Not a Keytlin vault")
        val version = input.readInt()
        if (version != VaultHeader.CURRENT_VERSION) {
            throw InvalidVaultFormatException("Unsupported vault version $version")
        }
        val params = KdfParams(
            memoryKib = input.readInt(),
            iterations = input.readInt(),
            parallelism = input.readInt()
        )
        if (params.memoryKib !in 8..MAX_MEMORY_KIB || params.iterations !in 1..100 || params.parallelism !in 1..16) {
            throw InvalidVaultFormatException("Invalid KDF parameters")
        }
        val salt = input.readBytesWithLength(MAX_SMALL_FIELD)
        return VaultHeader(version, salt, params)
    }

    private fun write(block: (DataOutputStream) -> Unit): ByteArray {
        val buffer = ByteArrayOutputStream()
        DataOutputStream(buffer).use(block)
        return buffer.toByteArray()
    }

    private fun DataOutputStream.writeBytesWithLength(bytes: ByteArray) {
        writeInt(bytes.size)
        write(bytes)
    }

    private fun DataInputStream.readBytesWithLength(maxLength: Int): ByteArray {
        val length = readInt()
        if (length !in 0..maxLength) throw InvalidVaultFormatException("Invalid field length $length")
        return ByteArray(length).also(::readFully)
    }

    companion object {
        private val MAGIC =
            byteArrayOf('K'.code.toByte(), 'T'.code.toByte(), 'L'.code.toByte(), 'N'.code.toByte())
        private const val MAX_SMALL_FIELD = 64
        private const val MAX_CIPHERTEXT = 16 * 1024 * 1024 // 16 MiB
        private const val MAX_MEMORY_KIB = 1024 * 1024 // 1 GiB
    }
}

class InvalidVaultFormatException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)