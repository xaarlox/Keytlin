package com.xaarlox.keytlin.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.xaarlox.keytlin.ui.screens.auth.CreateVaultScreen
import com.xaarlox.keytlin.ui.screens.auth.CreateVaultViewModel
import com.xaarlox.keytlin.ui.screens.auth.EnterSyncCodeScreen
import com.xaarlox.keytlin.ui.screens.auth.RestoreVaultViewModel
import com.xaarlox.keytlin.ui.screens.auth.UnlockViewModel
import com.xaarlox.keytlin.ui.screens.auth.VaultLockedScreen
import com.xaarlox.keytlin.ui.screens.welcome.WelcomeChoiceScreen

fun NavGraphBuilder.authGraph(navController: NavController) {
    composable(Route.Welcome.path) {
        WelcomeChoiceScreen(
            onCreateVaultClick = { navController.navigate(Route.CreateVault.path) },
            onRestoreVaultClick = { navController.navigate(Route.EnterSyncCode.path) }
        )
    }

    composable(Route.EnterSyncCode.path) {
        val viewModel: RestoreVaultViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(uiState.isVerified) {
            if (uiState.isVerified) {
                navController.navigate(Route.VaultLocked.path) {
                    popUpTo(Route.Welcome.path) { inclusive = true }
                }
            }
        }

        EnterSyncCodeScreen(
            code = uiState.code,
            onCodeChange = viewModel::onCodeChange,
            isError = uiState.isError,
            onContinueClick = viewModel::verifyCode,
            onScanQrClick = { /* TODO */ },
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(Route.CreateVault.path) {
        val viewModel: CreateVaultViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(uiState.isCreated) {
            if (uiState.isCreated) {
                navController.navigate(Route.Vault.path) {
                    popUpTo(Route.Welcome.path) { inclusive = true }
                }
            }
        }

        CreateVaultScreen(
            masterPassword = uiState.masterPassword,
            onMasterPasswordChange = viewModel::onMasterPasswordChange,
            confirmPassword = uiState.confirmPassword,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            errorText = uiState.error?.message,
            onCreateClick = viewModel::createVault
        )
    }

    composable(Route.VaultLocked.path) {
        val viewModel: UnlockViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(uiState.isUnlocked) {
            if (uiState.isUnlocked) {
                navController.navigate(Route.Vault.path) {
                    popUpTo(Route.VaultLocked.path) { inclusive = true }
                }
            }
        }

        VaultLockedScreen(
            password = uiState.password,
            onPasswordChange = viewModel::onPasswordChange,
            isError = uiState.isError,
            onUnlockClick = viewModel::unlock
        )
    }
}