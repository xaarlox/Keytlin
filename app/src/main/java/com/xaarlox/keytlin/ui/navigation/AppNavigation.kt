package com.xaarlox.keytlin.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xaarlox.keytlin.data.FakeVaultRepository
import com.xaarlox.keytlin.data.VaultRepository
import com.xaarlox.keytlin.ui.screens.auth.CreateVaultScreen
import com.xaarlox.keytlin.ui.screens.auth.EnterSyncCodeScreen
import com.xaarlox.keytlin.ui.screens.vault.EntryDetailsScreen
import com.xaarlox.keytlin.ui.screens.generator.GeneratorScreen
import com.xaarlox.keytlin.ui.screens.settings.SettingsScreen
import com.xaarlox.keytlin.ui.screens.auth.VaultLockedScreen
import com.xaarlox.keytlin.ui.screens.vault.VaultScreen
import com.xaarlox.keytlin.ui.screens.welcome.WelcomeChoiceScreen
import com.xaarlox.keytlin.ui.screens.vault.dummyVaultData

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // DI later (Hilt/Koin/Dagger)
    val vaultRepository: VaultRepository = remember { FakeVaultRepository() }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "welcome"
            ) {
                composable("welcome") {
                    WelcomeChoiceScreen(
                        onCreateVaultClick = {
                            navController.navigate("create_vault")
                        },
                        onRestoreVaultClick = {
                            navController.navigate("enter_sync_code")
                        }
                    )
                }
                composable("enter_sync_code") {
                    var syncCode by rememberSaveable { mutableStateOf("") }
                    var isCodeError by rememberSaveable { mutableStateOf(false) }

                    EnterSyncCodeScreen(
                        code = syncCode,
                        onCodeChange = { newValue ->
                            syncCode = newValue
                            isCodeError = false
                        },
                        isError = isCodeError,
                        onContinueClick = {
                            val isValid = vaultRepository.validateSyncCode(syncCode)
                            isCodeError = !isValid

                            if (isValid) {
                                navController.navigate("vault_locked") {
                                    popUpTo("welcome") {
                                        inclusive = false
                                    }
                                }
                            }
                        },
                        onScanQrClick = { /* TODO */ },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
                composable("create_vault") {
                    var masterPassword by remember { mutableStateOf("") }
                    var confirmPassword by remember { mutableStateOf("") }

                    var isPasswordError by rememberSaveable { mutableStateOf(false) }

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
                            if (masterPassword != confirmPassword || masterPassword.isEmpty()) {
                                isPasswordError = true
                            } else {
                                navController.navigate("vault_main") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable("vault_locked") {
                    var password by rememberSaveable { mutableStateOf("") }
                    var isPasswordError by rememberSaveable { mutableStateOf(false) }

                    VaultLockedScreen(
                        password = password,
                        onPasswordChange = {
                            password = it
                            isPasswordError = false
                        },
                        isError = isPasswordError,
                        onUnlockClick = {
                            if (vaultRepository.validateMasterPassword(password)) {
                                navController.navigate("vault_main") {
                                    popUpTo("vault_locked") { inclusive = true }
                                }
                            } else {
                                isPasswordError = true
                            }
                        }
                    )
                }
                composable("vault_main") {
                    VaultScreen(
                        onItemClick = { itemId ->
                            navController.navigate("entry_details/$itemId")
                        },
                        onAddClick = {
                            navController.navigate("entry_details/new")
                        },
                        onNavigateToGenerator = {
                            navController.navigateToTab("generator")
                        },
                        onNavigateToSettings = {
                            navController.navigateToTab("settings")
                        }
                    )
                }
                composable("generator") {
                    GeneratorScreen(
                        onNavigateToVault = {
                            navController.navigateToTab("vault_main")
                        },
                        onNavigateToSettings = {
                            navController.navigateToTab("settings")
                        }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        onNavigateToVault = {
                            navController.navigateToTab("vault_main")
                        },
                        onNavigateToGenerator = {
                            navController.navigateToTab("generator")
                        },
                        onChangePasswordClick = { /* TODO */ },
                        onAutoLockClick = { /* TODO */ },
                        onLockVaultClick = {
                            navController.navigate("vault_locked") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onSyncCodeClick = { /* TODO */ },
                        onSyncNowClick = { /* TODO */ },
                        onExportClick = { /* TODO */ },
                        onDeleteClick = { /* TODO */ }
                    )
                }
                composable("entry_details/new") {
                    EntryDetailsScreen(
                        entry = null,
                        onBackClick = { navController.popBackStack() },
                        onSaveClick = { newEntry ->
                            // TODO: Save newEntry
                            navController.popBackStack()
                        },
                        onDeleteClick = {}
                    )
                }
                composable(
                    route = "entry_details/{entryId}",
                    arguments = listOf(navArgument("entryId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getString("entryId")

                    // TODO
                    val entry = dummyVaultData.find { it.id == entryId }

                    EntryDetailsScreen(
                        entry = entry,
                        onBackClick = { navController.popBackStack() },
                        onSaveClick = { updatedEntry ->
                            // TODO
                            navController.popBackStack()
                        },
                        onDeleteClick = {
                            // TODO
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}