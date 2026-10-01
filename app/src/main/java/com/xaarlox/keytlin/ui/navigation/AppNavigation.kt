package com.xaarlox.keytlin.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xaarlox.keytlin.data.FakeVaultRepository
import com.xaarlox.keytlin.data.VaultRepository
import com.xaarlox.keytlin.ui.screens.EnterSyncCodeScreen
import com.xaarlox.keytlin.ui.screens.WelcomeChoiceScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Пізніше - DI (Hilt/Koin/Dagger)
    val vaultRepository: VaultRepository = remember { FakeVaultRepository() }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "welcome"
            ) {
                composable("welcome") {
                    WelcomeChoiceScreen(
                        onCreateVaultClick = { /* TODO */ },
                        onRestoreVaultClick = {
                            navController.navigate("enter_sync_code")
                        }
                    )
                }
                composable("enter_sync_code") {
                    var syncCode by remember { mutableStateOf("") }
                    var isCodeError by remember { mutableStateOf(false) }

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
                                // TODO: Успішно! Переходимо на наступний екран
                            }
                        },
                        onScanQrClick = { /* TODO */ },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}