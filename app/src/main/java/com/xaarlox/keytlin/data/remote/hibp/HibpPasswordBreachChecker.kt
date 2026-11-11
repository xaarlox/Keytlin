package com.xaarlox.keytlin.data.remote.hibp

import com.xaarlox.keytlin.domain.breach.PasswordBreachChecker
import com.xaarlox.keytlin.domain.breach.PasswordBreachResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Password check via Pwned Passwords using k-anonymity:
 *
 * 1. SHA-1(password) → 40 hex characters, e.g., 5BAA6 1E4C9B93F3F0682250B6CF8331B7EE68FD8.
 * 2. Only the prefix 5BAA6 (1 out of 1,048,576 possible) is sent to the server — the password and full hash never leave the device.
 * 3. The server returns ~800–1000 suffixes with this prefix; matching is performed locally.
 *
 * Responses are cached in memory by prefix to avoid repeating requests while typing
 */
class HibpPasswordBreachChecker(
    private val api: PwnedPasswordsApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val cacheSize: Int = 64
) : PasswordBreachChecker {

    private val mutex = Mutex()
    private val cache = object : LinkedHashMap<String, Map<String, Long>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Map<String, Long>>?) =
            size > cacheSize
    }

    override suspend fun check(password: String): PasswordBreachResult {
        if (password.isEmpty()) return PasswordBreachResult.NotFound
        val hash = sha1Hex(password)
        val prefix = hash.substring(0, PREFIX_LENGTH)
        val suffix = hash.substring(PREFIX_LENGTH)

        val suffixes = try {
            mutex.withLock { cache[prefix] } ?: fetchRange(prefix).also { result ->
                mutex.withLock { cache[prefix] = result }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return PasswordBreachResult.Unavailable
        }

        val count = suffixes[suffix] ?: 0L
        return if (count > 0) PasswordBreachResult.Found(count) else PasswordBreachResult.NotFound
    }

    private suspend fun fetchRange(prefix: String): Map<String, Long> = withContext(ioDispatcher) {
        api.range(prefix).use { body -> parseRange(body.string()) }
    }

    companion object {
        const val PREFIX_LENGTH = 5

        fun sha1Hex(password: String): String {
            val digest =
                MessageDigest.getInstance("SHA-1").digest(password.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02X".format(it) }
        }

        fun parseRange(body: String): Map<String, Long> =
            body.lineSequence()
                .mapNotNull { line ->
                    val separator = line.indexOf(':')
                    if (separator <= 0) return@mapNotNull null
                    val suffix = line.substring(0, separator).trim().uppercase()
                    val count = line.substring(separator + 1).trim().toLongOrNull()
                        ?: return@mapNotNull null
                    if (count > 0) suffix to count else null
                }
                .toMap()
    }
}