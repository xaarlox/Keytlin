package com.xaarlox.keytlin.domain.models

import kotlinx.serialization.Serializable

/**
 * Decrypted content of the vault
 *
 * [entries] also contains deleted entries (tombstones) — they are needed for merging
 * between devices;
 * [activeEntries] is used for displaying to the user
 */
@Serializable
data class Vault(
    val entries: List<VaultEntry> = emptyList()
) {
    val activeEntries: List<VaultEntry> get() = entries.filterNot { it.isDeleted }

    fun withEntry(entry: VaultEntry): Vault = copy(entries = entries + entry)

    fun withUpdated(entry: VaultEntry): Vault = copy(
        entries = entries.map {
            if (it.id == entry.id) entry.copy(modifiedAt = System.currentTimeMillis()) else it
        }
    )

    fun withDeleted(id: String, now: Long = System.currentTimeMillis()): Vault = copy(
        entries = entries.map { if (it.id == id && !it.isDeleted) it.toTombstone(now) else it }
    )

    fun find(id: String): VaultEntry? = entries.find { it.id == id && !it.isDeleted }
}