package com.xaarlox.keytlin.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.xaarlox.keytlin.data.FakeVaultRepository
import com.xaarlox.keytlin.data.VaultRepository

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // DI later (Hilt/Koin/Dagger)
    val vaultRepository: VaultRepository = remember { FakeVaultRepository() }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Welcome.path,
            modifier = Modifier.padding(innerPadding)
        ) {
            authGraph(navController, vaultRepository)
            mainGraph(navController)
        }
    }
}