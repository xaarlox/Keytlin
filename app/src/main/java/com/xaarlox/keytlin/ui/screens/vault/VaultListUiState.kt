package com.xaarlox.keytlin.ui.screens.vault

import com.xaarlox.keytlin.domain.models.VaultEntry

data class VaultListUiState(
    val entries: List<VaultEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)