package com.xaarlox.keytlin.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xaarlox.keytlin.data.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UnlockUiState(
    val password: String = "",
    val isError: Boolean = false,
    val isLoading: Boolean = false,
    val isUnlocked: Boolean = false
) {
    override fun toString() =
        "UnlockUiState(isError=$isError, isLoading=$isLoading, isUnlocked=$isUnlocked)"
}

@HiltViewModel
class UnlockViewModel @Inject constructor(private val repository: VaultRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UnlockUiState())
    val uiState: StateFlow<UnlockUiState> = _uiState.asStateFlow()

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, isError = false) }

    fun unlock() {
        val state = _uiState.value
        if (state.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val success = repository.unlock(state.password)
            _uiState.update {
                it.copy(
                    password = if (success) "" else it.password,
                    isError = !success,
                    isLoading = false,
                    isUnlocked = success
                )
            }
        }
    }
}

// Vault creation
enum class CreateVaultError(val message: String) {
    EMPTY("Password cannot be empty"),
    TOO_SHORT("Use at least ${CreateVaultViewModel.MIN_PASSWORD_LENGTH} characters"),
    MISMATCH("Passwords do not match")
}

data class CreateVaultUiState(
    val masterPassword: String = "",
    val confirmPassword: String = "",
    val error: CreateVaultError? = null,
    val isLoading: Boolean = false,
    val isCreated: Boolean = false
) {
    override fun toString() =
        "CreateVaultUiState(error=$error, isLoading=$isLoading, isCreated=$isCreated)"
}

@HiltViewModel
class CreateVaultViewModel @Inject constructor(private val repository: VaultRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow(CreateVaultUiState())
    val uiState: StateFlow<CreateVaultUiState> = _uiState.asStateFlow()

    fun onMasterPasswordChange(value: String) =
        _uiState.update { it.copy(masterPassword = value, error = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, error = null) }

    fun createVault() {
        val state = _uiState.value
        if (state.isLoading) return

        val error = when {
            state.masterPassword.isEmpty() -> CreateVaultError.EMPTY
            state.masterPassword.length < MIN_PASSWORD_LENGTH -> CreateVaultError.TOO_SHORT
            state.masterPassword != state.confirmPassword -> CreateVaultError.MISMATCH
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.createVault(state.masterPassword)
            _uiState.value = CreateVaultUiState(isCreated = true)
        }
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}

// Restoration via sync code
data class RestoreVaultUiState(
    val code: String = "",
    val isError: Boolean = false,
    val isLoading: Boolean = false,
    val isVerified: Boolean = false
)

@HiltViewModel
class RestoreVaultViewModel @Inject constructor(private val repository: VaultRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow(RestoreVaultUiState())
    val uiState: StateFlow<RestoreVaultUiState> = _uiState.asStateFlow()

    fun onCodeChange(value: String) = _uiState.update { it.copy(code = value, isError = false) }

    fun verifyCode() {
        val state = _uiState.value
        if (state.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Downloads the encrypted vault from the server and saves it to the local database
            val isValid = repository.restoreFromSyncCode(state.code)
            _uiState.update { it.copy(isLoading = false, isError = !isValid, isVerified = isValid) }
        }
    }
}