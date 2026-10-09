package com.xaarlox.keytlin.domain.models

enum class PasswordStrength { WEAK, MEDIUM, STRONG }

data class PasswordAnalysis(
    val entropyBits: Double,
    val strength: PasswordStrength,
    val isBreachedLocally: Boolean
) {
    val progress: Float get() = (entropyBits / 100.0).coerceIn(0.0, 1.0).toFloat()
}