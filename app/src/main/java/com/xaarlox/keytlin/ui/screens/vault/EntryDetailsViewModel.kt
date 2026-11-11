package com.xaarlox.keytlin.ui.screens.vault

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xaarlox.keytlin.data.VaultRepository
import com.xaarlox.keytlin.domain.breach.PasswordBreachChecker
import com.xaarlox.keytlin.domain.breach.PasswordBreachResult
import com.xaarlox.keytlin.domain.breach.SiteBreach
import com.xaarlox.keytlin.domain.breach.SiteBreachChecker
import com.xaarlox.keytlin.domain.models.GeneratorOptions
import com.xaarlox.keytlin.domain.models.PasswordAnalysis
import com.xaarlox.keytlin.domain.models.VaultEntry
import com.xaarlox.keytlin.domain.service.PasswordGenerator
import com.xaarlox.keytlin.domain.service.PasswordStrengthEvaluator
import com.xaarlox.keytlin.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.map

/**
 * Loads an entry by `entryId` from navigation arguments (or prepares an empty form
 * for a new entry), saves changes, and deletes the entry via [VaultRepository]
 *
 * During editing, checks via Have I Been Pwned:
 * - whether the password appears in known breaches ([PasswordBreachChecker]);
 * - whether there were breaches on the website specified in the URL field ([SiteBreachChecker]).
 * Both checks are triggered with a delay after the last typed character (debounced)
 * to avoid sending a request on every keystroke
 */
@HiltViewModel
class EntryDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VaultRepository,
    private val generator: PasswordGenerator,
    private val evaluator: PasswordStrengthEvaluator,
    private val passwordBreachChecker: PasswordBreachChecker,
    private val siteBreachChecker: SiteBreachChecker
) : ViewModel() {

    private val entryId: String? = savedStateHandle[Route.ARG_ENTRY_ID]
    private var original: VaultEntry? = null

    private var passwordCheckJob: Job? = null
    private var siteCheckJob: Job? = null
    private var siteBreaches: List<SiteBreach> = emptyList()

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
            // Збережений запис перевіряємо одразу, без затримки.
            schedulePasswordCheck(entry.password, delayMs = 0)
            scheduleSiteCheck(entry.url, delayMs = 0)
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, titleError = null) }

    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value) }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, analysis = analyze(value), siteBreaches = siteWarnings(value))
        }
        schedulePasswordCheck(value, CHECK_DEBOUNCE_MS)
    }

    fun onUrlChange(value: String) {
        _uiState.update { it.copy(url = value) }
        scheduleSiteCheck(value, CHECK_DEBOUNCE_MS)
    }

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

        val now = System.currentTimeMillis()
        val previous = original
        val entry = previous?.copy(
            title = state.title.trim(),
            username = state.username.trim(),
            password = state.password,
            url = state.url.trim(),
            notes = state.notes,
            passwordChangedAt = if (state.password != previous.password) now else previous.passwordChangedAt
        ) ?: VaultEntry(
            id = UUID.randomUUID().toString(),
            title = state.title.trim(),
            username = state.username.trim(),
            password = state.password,
            url = state.url.trim(),
            notes = state.notes,
            createdAt = now,
            modifiedAt = now,
            passwordChangedAt = now
        )

        runAndFinish("Failed to save entry") { repository.saveEntry(entry) }
    }

    fun delete() {
        val id = original?.id ?: return
        if (_uiState.value.isSaving) return
        runAndFinish("Failed to delete entry") { repository.deleteEntry(id) }
    }

    fun onErrorShown() = _uiState.update { it.copy(errorMessage = null) }

    private fun schedulePasswordCheck(password: String, delayMs: Long) {
        passwordCheckJob?.cancel()
        if (password.isEmpty()) {
            _uiState.update { it.copy(passwordBreach = PasswordBreachStatus.Idle) }
            return
        }
        passwordCheckJob = viewModelScope.launch {
            delay(delayMs)
            _uiState.update { it.copy(passwordBreach = PasswordBreachStatus.Checking) }
            val status = when (val result = passwordBreachChecker.check(password)) {
                is PasswordBreachResult.Found -> PasswordBreachStatus.Breached(result.occurrences)
                PasswordBreachResult.NotFound -> PasswordBreachStatus.Safe
                PasswordBreachResult.Unavailable ->
                    if (analyze(password)?.isBreachedLocally == true) PasswordBreachStatus.Breached(
                        null
                    )
                    else PasswordBreachStatus.Unavailable
            }
            _uiState.update { it.copy(passwordBreach = status) }
        }
    }

    private fun scheduleSiteCheck(url: String, delayMs: Long) {
        siteCheckJob?.cancel()
        siteCheckJob = viewModelScope.launch {
            delay(delayMs)
            siteBreaches = if (url.isBlank()) emptyList() else siteBreachChecker.breachesFor(url)
            _uiState.update { it.copy(siteBreaches = siteWarnings(it.password)) }
        }
    }

    /**
     * The password could have been compromised if the breach included passwords
     * and the saved password has not been changed since the breach date. A new (not yet saved)
     * password is considered safe
     */
    private fun siteWarnings(currentPassword: String): List<SiteBreachWarning> {
        val saved = original
        return siteBreaches.map { breach ->
            val passwordIsOld = saved != null &&
                    saved.password == currentPassword &&
                    saved.passwordChangedAt < breach.breachDate
            SiteBreachWarning(
                breach,
                passwordMayBeAffected = breach.includesPasswords && passwordIsOld
            )
        }
    }

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
        const val CHECK_DEBOUNCE_MS = 600L
    }
}