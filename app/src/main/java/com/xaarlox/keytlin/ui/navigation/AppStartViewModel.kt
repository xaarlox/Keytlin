package com.xaarlox.keytlin.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xaarlox.keytlin.data.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Determines the initial screen: if a vault already exists in the database — unlock screen,
 * otherwise — welcome screen; null while the database has not been read yet
 */
@HiltViewModel
class AppStartViewModel @Inject constructor(
    repository: VaultRepository
) : ViewModel() {

    val startDestination: StateFlow<String?> = flow {
        val hasVault = repository.observeHasVault().first()
        emit(if (hasVault) Route.VaultLocked.path else Route.Welcome.path)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
}