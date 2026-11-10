package com.xaarlox.keytlin.ui.screens.vault

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xaarlox.keytlin.data.VaultRepository
import com.xaarlox.keytlin.domain.models.GeneratorOptions
import com.xaarlox.keytlin.domain.models.PasswordAnalysis
import com.xaarlox.keytlin.domain.models.VaultEntry
import com.xaarlox.keytlin.domain.service.PasswordGenerator
import com.xaarlox.keytlin.domain.service.PasswordStrengthEvaluator
import com.xaarlox.keytlin.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Loads the entry by `entryId` from the navigation arguments (or prepares an empty form
 * for a new entry), saves changes, and deletes the entry via [VaultRepository]
 */
@HiltViewModel
class EntryDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VaultRepository,
    private val generator: PasswordGenerator,
    private val evaluator: PasswordStrengthEvaluator
) : ViewModel() {

    private val entryId: String? = savedStateHandle[Route.ARG_ENTRY_ID]
    private var original: VaultEntry? = null

    private val _uiState = MutableStateFlow(
        EntryDetailsUiState(isEditing = entryId != null, isLoading = entryId != null)
    )
    val uiState: StateFlow<EntryDetailsUiState> = _uiState.asStateFlow()

    init {
        entryId?.let(::loadEntry)
    }

    private fun loadEntry(id: String) {
        viewModelScope.launch {
            val entry = repository.getEntry(id)
            if (entry == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Entry not found") }
                return@launch
            }
            original = entry
            _uiState.update {
                it.copy(
                    title = entry.title,
                    username = entry.username,
                    password = entry.password,
                    url = entry.url,
                    notes = entry.notes,
                    analysis = analyze(entry.password),
                    isLoading = false
                )
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, titleError = null) }

    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, analysis = analyze(value)) }

    fun onUrlChange(value: String) = _uiState.update { it.copy(url = value) }

    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun generatePassword() {
        onPasswordChange(generator.generate(GeneratorOptions(length = GENERATED_PASSWORD_LENGTH)))
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.isLoading) return
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            return
        }

        val entry = original?.copy(
            title = state.title.trim(),
            username = state.username.trim(),
            password = state.password,
            url = state.url.trim(),
            notes = state.notes
        ) ?: VaultEntry(
            id = UUID.randomUUID().toString(),
            title = state.title.trim(),
            username = state.username.trim(),
            password = state.password,
            url = state.url.trim(),
            notes = state.notes
        )

        runAndFinish("Failed to save entry") { repository.saveEntry(entry) }
    }

    fun delete() {
        val id = original?.id ?: return
        if (_uiState.value.isSaving) return
        runAndFinish("Failed to delete entry") { repository.deleteEntry(id) }
    }

    fun onErrorShown() = _uiState.update { it.copy(errorMessage = null) }

    private fun runAndFinish(errorMessage: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                action()
                _uiState.update { it.copy(isSaving = false, isFinished = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = errorMessage) }
            }
        }
    }

    private fun analyze(password: String): PasswordAnalysis? =
        if (password.isEmpty()) null else evaluator.analyze(password)

    private companion object {
        const val GENERATED_PASSWORD_LENGTH = 16
    }
}