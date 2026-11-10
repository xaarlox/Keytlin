package com.xaarlox.keytlin.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.xaarlox.keytlin.data.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: VaultRepository) : ViewModel() {

    /** Clears decrypted data from memory; all subscribed screens will receive an empty list */
    fun lockVault() = repository.lock()
}