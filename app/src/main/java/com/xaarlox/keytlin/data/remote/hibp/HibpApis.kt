package com.xaarlox.keytlin.data.remote.hibp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * Pwned Passwords API (https://api.pwnedpasswords.com/) — free, no key required
 *
 * k-Anonymity model: only the first 5 characters of the password's SHA-1 hash are sent,
 * returning all hash suffixes with that prefix (SUFFIX:COUNT per line).
 * Whether our password is among them is checked locally on the device
 */
interface PwnedPasswordsApi {

    /**
     * Add-Padding: true pads the response with dummy entries (COUNT = 0) up to 800–1000 lines
     * so that even the response size does not leak which prefix was queried
     */
    @Headers("Add-Padding: true")
    @GET("range/{prefix}")
    suspend fun range(@Path("prefix") hashPrefix: String): ResponseBody

    companion object {
        const val BASE_URL = "https://api.pwnedpasswords.com/"
    }
}

interface HibpBreachesApi {

    @GET("breaches")
    suspend fun allBreaches(): List<BreachDto>

    companion object {
        const val BASE_URL = "https://haveibeenpwned.com/api/v3/"
        const val ATTRIBUTION_URL = "https://haveibeenpwned.com"
    }
}

/** API breach model (field names in PascalCase, matching the server response) */
@Serializable
data class BreachDto(
    @SerialName("Name") val name: String,
    @SerialName("Title") val title: String,
    @SerialName("Domain") val domain: String = "",
    @SerialName("BreachDate") val breachDate: String = "",
    @SerialName("PwnCount") val pwnCount: Long = 0,
    @SerialName("DataClasses") val dataClasses: List<String> = emptyList(),
    @SerialName("IsVerified") val isVerified: Boolean = false,
    @SerialName("IsFabricated") val isFabricated: Boolean = false,
    @SerialName("IsSpamList") val isSpamList: Boolean = false,
    @SerialName("IsRetired") val isRetired: Boolean = false
)