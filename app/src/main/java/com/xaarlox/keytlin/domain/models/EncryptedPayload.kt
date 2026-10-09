package com.xaarlox.keytlin.domain.models

class EncryptedPayload(
    val iv: ByteArray,
    val ciphertext: ByteArray,
    val authTag: ByteArray
) {
    override fun equals(other: Any?): Boolean =
        other is EncryptedPayload &&
                iv.contentEquals(other.iv) &&
                ciphertext.contentEquals(other.ciphertext) &&
                authTag.contentEquals(other.authTag)

    override fun hashCode(): Int =
        31 * (31 * iv.contentHashCode() + ciphertext.contentHashCode()) + authTag.contentHashCode()

    fun clear() {
        iv.fill(0); ciphertext.fill(0); authTag.fill(0)
    }
}