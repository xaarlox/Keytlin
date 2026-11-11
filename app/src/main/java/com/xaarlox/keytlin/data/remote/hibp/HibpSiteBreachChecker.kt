package com.xaarlox.keytlin.data.remote.hibp

import com.xaarlox.keytlin.domain.breach.DomainMatcher
import com.xaarlox.keytlin.domain.breach.SiteBreach
import com.xaarlox.keytlin.domain.breach.SiteBreachChecker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Checks websites against the HIBP breach list
 *
 * The full list is downloaded (one request per day), and entry addresses are matched
 * on the device. Consequently, HIBP does not learn which websites the user has accounts on —
 * unlike making ?domain=... queries for each entry
 */
class HibpSiteBreachChecker(
    private val api: HibpBreachesApi,
    private val clock: () -> Long = System::currentTimeMillis,
    private val cacheTtlMs: Long = CACHE_TTL_MS
) : SiteBreachChecker {

    private val mutex = Mutex()
    private var cached: List<SiteBreach>? = null
    private var cachedAt = 0L

    override suspend fun breachesFor(url: String): List<SiteBreach> {
        val host = DomainMatcher.hostOf(url) ?: return emptyList()
        val all = loadBreaches() ?: return emptyList()
        return all
            .filter { DomainMatcher.belongsTo(host, it.domain) }
            .sortedByDescending { it.breachDate }
    }

    private suspend fun loadBreaches(): List<SiteBreach>? = mutex.withLock {
        val now = clock()
        cached?.takeIf { now - cachedAt < cacheTtlMs }?.let { return@withLock it }

        try {
            api.allBreaches().mapNotNull { it.toSiteBreach() }.also {
                cached = it
                cachedAt = now
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            cached
        }
    }

    companion object {
        const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L

        fun BreachDto.toSiteBreach(): SiteBreach? {
            if (domain.isBlank() || isFabricated || isSpamList) return null
            return SiteBreach(
                name = name,
                title = title,
                domain = domain,
                breachDate = DomainMatcher.parseDate(breachDate) ?: return null,
                affectedAccounts = pwnCount,
                dataClasses = dataClasses,
                isVerified = isVerified
            )
        }
    }
}