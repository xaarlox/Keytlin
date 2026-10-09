package com.xaarlox.keytlin.domain.service

import com.xaarlox.keytlin.domain.models.CharacterSets
import com.xaarlox.keytlin.domain.models.GeneratorOptions
import java.security.SecureRandom

class PasswordGenerator {
    private val random = SecureRandom()

    fun generate(options: GeneratorOptions): String {
        val sets = buildList {
            if (options.useUpper) add(CharacterSets.UPPER)
            if (options.useLower) add(CharacterSets.LOWER)
            if (options.useDigits) add(CharacterSets.DIGITS)
            if (options.useSymbols) add(CharacterSets.SYMBOLS)
        }.ifEmpty { listOf(CharacterSets.LOWER) }

        val pool = sets.joinToString("")

        val required = sets.map { it[random.nextInt(it.length)] }
        val rest = List((options.length - required.size).coerceAtLeast(0)) {
            pool[random.nextInt(pool.length)]
        }

        return (required + rest)
            .shuffled(random)
            .take(options.length)
            .joinToString("")

    }
}