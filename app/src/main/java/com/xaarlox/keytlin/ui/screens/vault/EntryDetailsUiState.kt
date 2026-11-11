package com.xaarlox.keytlin.ui.screens.vault

import com.xaarlox.keytlin.domain.breach.SiteBreach
import com.xaarlox.keytlin.domain.models.PasswordAnalysis

/** State of the online password check via Have I Been Pwned */
sealed interface PasswordBreachStatus {
    data object Idle : PasswordBreachStatus

    data object Checking : PasswordBreachStatus

    data object Safe : PasswordBreachStatus

    data class Breached(val occurrences: Long?) : PasswordBreachStatus

    data object Unavailable : PasswordBreachStatus
}

/** Breach on the entry's website + whether the saved password could have been compromised */
data class SiteBreachWarning(
    val breach: SiteBreach,
    val passwordMayBeAffected: Boolean
)

data class EntryDetailsUiState(
    val title: String = "",
    val username: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val isEditing: Boolean = false,
    val analysis: PasswordAnalysis? = null,
    val passwordBreach: PasswordBreachStatus = PasswordBreachStatus.Idle,
    val siteBreaches: List<SiteBreachWarning> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val errorMessage: String? = null,
    val isFinished: Boolean = false
) {
    override fun toString(): String =
        "EntryDetailsUiState(title=$title, isEditing=$isEditing, isLoading=$isLoading, " +
                "isSaving=$isSaving, passwordBreach=$passwordBreach, isFinished=$isFinished)"
}