package com.xaarlox.keytlin.ui.navigation

sealed class Route(val path: String) {
    data object Welcome : Route("welcome")
    data object CreateVault : Route("create_vault")
    data object EnterSyncCode : Route("enter_sync_code")
    data object VaultLocked : Route("vault_locked")

    data object Vault : Route("vault_main")
    data object Generator : Route("generator")
    data object Settings : Route("settings")

    data object NewEntry : Route("entry_details/new")
    data object EntryDetails : Route("entry_details/{$ARG_ENTRY_ID}") {
        fun create(id: String) = "entry_details/$id"
    }

    companion object {
        const val ARG_ENTRY_ID = "entryId"
    }
}