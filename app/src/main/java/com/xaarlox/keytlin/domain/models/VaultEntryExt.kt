package com.xaarlox.keytlin.domain.models

private const val MAX_INITIALS = 3
private const val FALLBACK_INITIALS = "V"

val VaultEntry.initials: String
    get() {
        val words = title.trim().split(Regex("[\\s_\\-.]+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) return FALLBACK_INITIALS

        val result = if (words.size > 1) {
            words.take(MAX_INITIALS).map { it.first() }
        } else {
            val word = words.first()
            val humps = word.windowed(size = 2)
                .filter { it[0].isLowerCase() && it[1].isUpperCase() }
                .map { it[1] }
            listOf(word.first()) + humps
        }

        return result
            .take(MAX_INITIALS)
            .joinToString("") { it.uppercaseChar().toString() }
    }