package com.xaarlox.keytlin.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.xaarlox.keytlin.data.VaultRepository
import com.xaarlox.keytlin.ui.screens.auth.CreateVaultScreen
import com.xaarlox.keytlin.ui.screens.auth.EnterSyncCodeScreen
import com.xaarlox.keytlin.ui.screens.auth.VaultLockedScreen
import com.xaarlox.keytlin.ui.screens.welcome.WelcomeChoiceScreen

fun NavGraphBuilder.authGraph(
    navController: NavController,
    vaultRepository: VaultRepository
) {
    composable(Route.Welcome.path) {
        WelcomeChoiceScreen(
            onCreateVaultClick = { navController.navigate(Route.CreateVault.path) },
            onRestoreVaultClick = { navController.navigate(Route.EnterSyncCode.path) }
        )
    }

    composable(Route.EnterSyncCode.path) {
        var syncCode by rememberSaveable { mutableStateOf("") }
        var isCodeError by rememberSaveable { mutableStateOf(false) }

        EnterSyncCodeScreen(
            code = syncCode,
            onCodeChange = {
                syncCode = it
                isCodeError = false
            },
            isError = isCodeError,
            onContinueClick = {
                val isValid = vaultRepository.validateSyncCode(syncCode)
                isCodeError = !isValid
                if (isValid) {
                    navController.navigate(Route.VaultLocked.path) {
                        popUpTo(Route.Welcome.path) { inclusive = true }
                    }
                }
            },
            onScanQrClick = { /* TODO */ },
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(Route.CreateVault.path) {
        var masterPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var isPasswordError by remember { mutableStateOf(false) }

        CreateVaultScreen(
            masterPassword = masterPassword,
            onMasterPasswordChange = {
                masterPassword = it
                isPasswordError = false
            },
            confirmPassword = confirmPassword,
            onConfirmPasswordChange = {
                confirmPassword = it
                isPasswordError = false
            },
            isError = isPasswordError,
            onCreateClick = {
                if (masterPassword.isEmpty() || masterPassword != confirmPassword) {
                    isPasswordError = true
                } else {
                    navController.navigate(Route.Vault.path) {
                        popUpTo(Route.Welcome.path) { inclusive = true }
                    }
                }
            }
        )
    }

    composable(Route.VaultLocked.path) {
        var password by remember { mutableStateOf("") }
        var isPasswordError by remember { mutableStateOf(false) }

        VaultLockedScreen(
            password = password,
            onPasswordChange = {
                password = it
                isPasswordError = false
            },
            isError = isPasswordError,
            onUnlockClick = {
                if (vaultRepository.validateMasterPassword(password)) {
                    navController.navigate(Route.Vault.path) {
                        popUpTo(Route.VaultLocked.path) { inclusive = true }
                    }
                } else {
                    isPasswordError = true
                }
            }
        )
    }
}