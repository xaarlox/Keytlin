package com.xaarlox.keytlin.domain.service

import java.security.SecureRandom
import kotlin.math.ln

class PasswordGenerator {
    private val random = SecureRandom()

    private val upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val lowerChars = "abcdefghijklmnopqrstuvwxyz"
    private val digitChars = "0123456789"
    private val symbolChars = "!@#$%^&*()-_=+[]{}"

    fun generate(
        length: Int,
        useUpper: Boolean,
        useLower: Boolean,
        useDigits: Boolean,
        useSymbols: Boolean
    ): String {
        val pool = buildString {
            if (useUpper) append(upperChars)
            if (useLower) append(lowerChars)
            if (useDigits) append(digitChars)
            if (useSymbols) append(symbolChars)
        }.ifEmpty { lowerChars }

        return buildString {
            repeat(length) {
                append(pool[random.nextInt(pool.length)])
            }
        }
    }

    fun calculateEntropyBits(
        length: Int,
        useUpper: Boolean,
        useLower: Boolean,
        useDigits: Boolean,
        useSymbols: Boolean
    ): Double {
        var poolSize = 0

        if (useUpper) poolSize += upperChars.length
        if (useLower) poolSize += lowerChars.length
        if (useDigits) poolSize += digitChars.length
        if (useSymbols) poolSize += symbolChars.length
        if (poolSize == 0) poolSize = lowerChars.length

        return length * (ln(poolSize.toDouble()) / ln(2.0))
    }
}