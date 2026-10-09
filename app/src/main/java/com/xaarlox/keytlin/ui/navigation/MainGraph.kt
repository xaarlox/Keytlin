package com.xaarlox.keytlin.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.xaarlox.keytlin.ui.screens.generator.GeneratorScreen
import com.xaarlox.keytlin.ui.screens.settings.SettingsScreen
import com.xaarlox.keytlin.ui.screens.vault.EntryDetailsScreen
import com.xaarlox.keytlin.ui.screens.vault.VaultScreen
import com.xaarlox.keytlin.ui.screens.vault.dummyVaultData

fun NavGraphBuilder.mainGraph(navController: NavController) {
    composable(Route.Vault.path) {
        VaultScreen(
            onItemClick = { id ->
                navController.navigate(Route.EntryDetails.create(id))
            },
            onAddClick = { navController.navigate(Route.NewEntry.path) },
            onNavigateToGenerator = { navController.navigateToTab(Route.Generator) },
            onNavigateToSettings = { navController.navigateToTab(Route.Settings) }
        )
    }

    composable(Route.Generator.path) {
        GeneratorScreen(
            onNavigateToVault = { navController.navigateToTab(Route.Vault) },
            onNavigateToSettings = { navController.navigateToTab(Route.Settings) }
        )
    }

    composable(Route.Settings.path) {
        SettingsScreen(
            onNavigateToVault = { navController.navigateToTab(Route.Vault) },
            onNavigateToGenerator = { navController.navigateToTab(Route.Generator) },
            onChangePasswordClick = { /* TODO */ },
            onAutoLockClick = { /* TODO */ },
            onLockVaultClick = {
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
        EntryDetailsScreen(
            entry = null,
            onBackClick = { navController.popBackStack() },
            onSaveClick = { /* TODO: save */ navController.popBackStack() },
            onDeleteClick = {}
        )
    }

    composable(
        route = Route.EntryDetails.path,
        arguments = listOf(
            navArgument(Route.ARG_ENTRY_ID) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val entryId = backStackEntry.arguments?.getString(Route.ARG_ENTRY_ID)
        val entry = dummyVaultData.find { it.id == entryId } // TODO: repository

        EntryDetailsScreen(
            entry = entry,
            onBackClick = { navController.popBackStack() },
            onSaveClick = { /* TODO: update */ navController.popBackStack() },
            onDeleteClick = { /* TODO: delete */ navController.popBackStack() }
        )
    }
}