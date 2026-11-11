package com.xaarlox.keytlin.domain.breach

import java.net.URI
import java.util.Calendar
import java.util.TimeZone

/** Known website data breach */
data class SiteBreach(
    val name: String,
    val title: String,
    val domain: String,
    val breachDate: Long,
    val affectedAccounts: Long,
    val dataClasses: List<String>,
    val isVerified: Boolean
) {
    val includesPasswords: Boolean
        get() = dataClasses.any {
            it.equals(
                "Passwords",
                ignoreCase = true
            )
        }
}

/** Finds known breaches for the website associated with the entry */
interface SiteBreachChecker {
    suspend fun breachesFor(url: String): List<SiteBreach>
}

object DomainMatcher {

    fun hostOf(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return null
        val withScheme = if ("://" in trimmed) trimmed else "https://$trimmed"
        val host = try {
            URI(withScheme).host
        } catch (e: Exception) {
            null
        } ?: return null
        return host.lowercase().removePrefix("www.").takeIf { '.' in it }
    }

    fun belongsTo(host: String, breachDomain: String): Boolean {
        val domain = breachDomain.lowercase().removePrefix("www.")
        if (domain.isEmpty()) return false
        return host == domain || host.endsWith(".$domain")
    }

    fun parseDate(date: String): Long? {
        val parts = date.split("-").mapNotNull { it.toIntOrNull() }
        if (parts.size != 3) return null
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).run {
            clear()
            set(parts[0], parts[1] - 1, parts[2])
            timeInMillis
        }
    }
}