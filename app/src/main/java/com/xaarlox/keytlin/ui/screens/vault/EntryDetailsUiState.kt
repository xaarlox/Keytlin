package com.xaarlox.keytlin.ui.screens.vault

import com.xaarlox.keytlin.domain.models.PasswordAnalysis

data class EntryDetailsUiState(
    val title: String = "",
    val username: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val isEditing: Boolean = false,
    val analysis: PasswordAnalysis? = null,
    val isSaving: Boolean = false
)