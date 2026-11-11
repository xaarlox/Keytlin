package com.xaarlox.keytlin.domain.breach

/** Result of checking a password against the breach database */
sealed interface PasswordBreachResult {
    data object NotFound : PasswordBreachResult

    data class Found(val occurrences: Long) : PasswordBreachResult

    data object Unavailable : PasswordBreachResult
}

/**
 * Checks whether the password has appeared in public data breaches.
 * The implementation must not transmit the password outside the device
 */
interface PasswordBreachChecker {
    suspend fun check(password: String): PasswordBreachResult
}