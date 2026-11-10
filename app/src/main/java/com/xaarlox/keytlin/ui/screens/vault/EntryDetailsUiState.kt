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
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val errorMessage: String? = null,
    val isFinished: Boolean = false
) {
    override fun toString(): String =
        "EntryDetailsUiState(title=$title, isEditing=$isEditing, isLoading=$isLoading, " +
                "isSaving=$isSaving, isFinished=$isFinished)"
}