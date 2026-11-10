package com.xaarlox.keytlin.data.security

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.xaarlox.keytlin.domain.crypto.KeyDerivationFunction
import com.xaarlox.keytlin.domain.models.KdfParams

/**
 * Argon2id via the native Argon2Kt library.
 * Argon2id is resistant both to GPU brute-forcing (memory-hard) and side-channel attacks.
 * With default parameters (64 MiB, 3 iterations), execution takes ~0.3–1 s — this is intentional
 */
class Argon2idKeyDerivation(
    private val argon2: Argon2Kt = Argon2Kt()
) : KeyDerivationFunction {

    override fun deriveKey(password: ByteArray, salt: ByteArray, params: KdfParams): ByteArray =
        argon2.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = password,
            salt = salt,
            tCostInIterations = params.iterations,
            mCostInKibibyte = params.memoryKib,
            parallelism = params.parallelism,
            hashLengthInBytes = KeyDerivationFunction.KEY_LENGTH_BYTES
        ).rawHashAsByteArray()
}