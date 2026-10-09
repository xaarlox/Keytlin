package com.xaarlox.keytlin.domain.models

data class GeneratorOptions(
    val length: Int = 13,
    val useUpper: Boolean = true,
    val useLower: Boolean = true,
    val useDigits: Boolean = true,
    val useSymbols: Boolean = true
) {
    val enabledSetsCount: Int
        get() = listOf(useUpper, useLower, useDigits, useSymbols).count { it }

    companion object {
        const val MIN_LENGTH = 8
        const val MAX_LENGTH = 64
    }
}