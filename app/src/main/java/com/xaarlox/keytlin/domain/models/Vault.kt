package com.xaarlox.keytlin.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Vault(
    val entries: List<VaultEntry> = emptyList()
) {
    fun withEntry(entry: VaultEntry): Vault = copy(entries = entries + entry)

    fun withUpdated(entry: VaultEntry): Vault = copy(
        entries = entries.map {
            if (it.id == entry.id) entry.copy(modifiedAt = System.currentTimeMillis()) else it
        }
    )

    fun without(id: String): Vault = copy(entries = entries.filterNot { it.id == id })

    fun find(id: String): VaultEntry? = entries.find { it.id == id }
}