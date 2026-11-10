package com.xaarlox.keytlin.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xaarlox.keytlin.data.VaultRepository
import com.xaarlox.keytlin.domain.models.VaultEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Combines the flow of records from the repository with search and sorting parameters.
 * Any change in data or filters automatically generates a new [VaultListUiState]
 */
@HiltViewModel
class VaultListViewModel @Inject constructor(
    repository: VaultRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val isSearchActive = MutableStateFlow(false)
    private val sortOrder = MutableStateFlow(VaultSortOrder.TITLE)

    val uiState: StateFlow<VaultListUiState> = combine(
        repository.observeEntries(),
        query,
        isSearchActive,
        sortOrder
    ) { entries, currentQuery, searchActive, order ->
        VaultListUiState(
            entries = entries.filterBy(currentQuery).sortedBy(order),
            query = currentQuery,
            isSearchActive = searchActive,
            sortOrder = order,
            isLoading = false
        )
    }
        .catch { e ->
            emit(
                VaultListUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load vault"
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = VaultListUiState()
        )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onSearchToggle() {
        val active = !isSearchActive.value
        isSearchActive.value = active
        if (!active) query.value = ""
    }

    fun onSortToggle() {
        sortOrder.value = when (sortOrder.value) {
            VaultSortOrder.TITLE -> VaultSortOrder.RECENTLY_MODIFIED
            VaultSortOrder.RECENTLY_MODIFIED -> VaultSortOrder.TITLE
        }
    }

    private fun List<VaultEntry>.filterBy(query: String): List<VaultEntry> {
        val q = query.trim()
        if (q.isEmpty()) return this
        return filter {
            it.title.contains(q, ignoreCase = true) ||
                    it.username.contains(q, ignoreCase = true) ||
                    it.url.contains(q, ignoreCase = true)
        }
    }

    private fun List<VaultEntry>.sortedBy(order: VaultSortOrder): List<VaultEntry> = when (order) {
        VaultSortOrder.TITLE -> sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        VaultSortOrder.RECENTLY_MODIFIED -> sortedByDescending { it.modifiedAt }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}