package com.xaarlox.keytlin.domain.service

import com.xaarlox.keytlin.domain.models.CharacterSets
import com.xaarlox.keytlin.domain.models.PasswordAnalysis
import com.xaarlox.keytlin.domain.models.PasswordStrength
import kotlin.math.ln

class PasswordStrengthEvaluator {
    fun analyze(password: String): PasswordAnalysis {
        val bits = entropyBits(password)
        val strength = when {
            bits >= STRONG_BITS -> PasswordStrength.STRONG
            bits >= MEDIUM_BITS -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
        return PasswordAnalysis(bits, strength, password.lowercase() in COMMON_PASSWORDS)
    }

    fun entropyBits(password: String): Double {
        if (password.isEmpty()) return 0.0
        var pool = 0
        if (password.any { it.isUpperCase() }) pool += CharacterSets.UPPER.length
        if (password.any { it.isLowerCase() }) pool += CharacterSets.LOWER.length
        if (password.any { it.isDigit() }) pool += CharacterSets.DIGITS.length
        if (password.any { !it.isLetterOrDigit() }) pool += CharacterSets.SYMBOLS.length
        if (pool == 0) return 0.0
        return password.length * (ln(pool.toDouble()) / ln(2.0))
    }

    companion object {
        const val MEDIUM_BITS = 40.0
        const val STRONG_BITS = 60.0
        private val COMMON_PASSWORDS = setOf("12345678", "password", "qwerty123", "11111111")
    }
}