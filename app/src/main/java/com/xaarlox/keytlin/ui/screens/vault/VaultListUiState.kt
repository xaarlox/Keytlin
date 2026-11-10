package com.xaarlox.keytlin.ui.screens.vault

import com.xaarlox.keytlin.domain.models.VaultEntry

enum class VaultSortOrder { TITLE, RECENTLY_MODIFIED }

data class VaultListUiState(
    val entries: List<VaultEntry> = emptyList(),
    val query: String = "",
    val isSearchActive: Boolean = false,
    val sortOrder: VaultSortOrder = VaultSortOrder.TITLE,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && entries.isEmpty()
}