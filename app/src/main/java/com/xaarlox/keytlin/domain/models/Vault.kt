package com.xaarlox.keytlin.domain.models

data class Vault(
    val entries: MutableList<VaultEntry> = mutableListOf()
) {
    fun addEntry(entry: VaultEntry) {
        entries.add(entry)
    }

    fun updateEntry(entry: VaultEntry) {
        val index = entries.indexOfFirst { it.id == entry.id }
        if (index != -1) {
            entries[index] = entry
        }
    }

    fun clear() {
        entries.clear()
    }
}
