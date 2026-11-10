package com.xaarlox.keytlin.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class VaultEntry(
    val id: String,
    val title: String,
    val username: String,
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    val isDeleted: Boolean get() = deletedAt != null

    /** Converts an entry into a tombstone: keeps only the ID and timestamp, erasing the content */
    fun toTombstone(now: Long): VaultEntry = VaultEntry(
        id = id,
        title = "",
        username = "",
        createdAt = createdAt,
        modifiedAt = now,
        deletedAt = now
    )
}