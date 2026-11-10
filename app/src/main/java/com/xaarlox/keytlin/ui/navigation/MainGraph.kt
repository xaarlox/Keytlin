package com.xaarlox.keytlin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.xaarlox.keytlin.ui.screens.generator.GeneratorScreen
import com.xaarlox.keytlin.ui.screens.generator.GeneratorViewModel
import com.xaarlox.keytlin.ui.screens.settings.SettingsScreen
import com.xaarlox.keytlin.ui.screens.settings.SettingsViewModel
import com.xaarlox.keytlin.ui.screens.vault.EntryDetailsScreen
import com.xaarlox.keytlin.ui.screens.vault.EntryDetailsViewModel
import com.xaarlox.keytlin.ui.screens.vault.VaultListViewModel
import com.xaarlox.keytlin.ui.screens.vault.VaultScreen

fun NavGraphBuilder.mainGraph(navController: NavController) {
    composable(Route.Vault.path) {
        val viewModel: VaultListViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        VaultScreen(
            uiState = uiState,
            onQueryChange = viewModel::onQueryChange,
            onSearchToggle = viewModel::onSearchToggle,
            onSortToggle = viewModel::onSortToggle,
            onItemClick = { id -> navController.navigate(Route.EntryDetails.create(id)) },
            onAddClick = { navController.navigate(Route.NewEntry.path) },
            onNavigateToGenerator = { navController.navigateToTab(Route.Generator) },
            onNavigateToSettings = { navController.navigateToTab(Route.Settings) }
        )
    }

    composable(Route.Generator.path) {
        val viewModel: GeneratorViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        GeneratorScreen(
            uiState = uiState,
            onLengthChange = viewModel::onLengthChange,
            onUppercaseChange = viewModel::onUppercaseChange,
            onLowercaseChange = viewModel::onLowercaseChange,
            onDigitsChange = viewModel::onDigitsChange,
            onSymbolsChange = viewModel::onSymbolsChange,
            onRegenerateClick = viewModel::regenerate,
            onNavigateToVault = { navController.navigateToTab(Route.Vault) },
            onNavigateToSettings = { navController.navigateToTab(Route.Settings) }
        )
    }

    composable(Route.Settings.path) {
        val viewModel: SettingsViewModel = hiltViewModel()

        SettingsScreen(
            onNavigateToVault = { navController.navigateToTab(Route.Vault) },
            onNavigateToGenerator = { navController.navigateToTab(Route.Generator) },
            onChangePasswordClick = { /* TODO */ },
            onAutoLockClick = { /* TODO */ },
            onLockVaultClick = {
                viewModel.lockVault()
                navController.navigate(Route.VaultLocked.path) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onSyncCodeClick = { /* TODO */ },
            onSyncNowClick = { /* TODO */ },
            onExportClick = { /* TODO */ },
            onDeleteClick = { /* TODO */ }
        )
    }

    composable(Route.NewEntry.path) {
        EntryDetailsDestination(navController)
    }

    composable(
        route = Route.EntryDetails.path,
        arguments = listOf(
            navArgument(Route.ARG_ENTRY_ID) { type = NavType.StringType }
        )
    ) {
        // entryId is passed to the ViewModel via SavedStateHandle
        EntryDetailsDestination(navController)
    }
}

@Composable
private fun EntryDetailsDestination(navController: NavController) {
    val viewModel: EntryDetailsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) navController.popBackStack()
    }

    EntryDetailsScreen(
        uiState = uiState,
        onTitleChange = viewModel::onTitleChange,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onUrlChange = viewModel::onUrlChange,
        onNotesChange = viewModel::onNotesChange,
        onGeneratePasswordClick = viewModel::generatePassword,
        onBackClick = { navController.popBackStack() },
        onSaveClick = viewModel::save,
        onDeleteClick = viewModel::delete,
        onErrorShown = viewModel::onErrorShown
    )
}