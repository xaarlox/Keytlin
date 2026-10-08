package com.xaarlox.keytlin.ui.components.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    title: String,
    selectedTab: Int,
    onNavigateToVault: () -> Unit = {},
    onNavigateToGenerator: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    topBarActions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier = Modifier.height(72.dp), title = {
                    Text(
                        text = title, style = MaterialTheme.typography.headlineMedium
                    )
                }, actions = topBarActions, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = floatingActionButton,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(72.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                windowInsets = WindowInsets(0.dp)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { if (selectedTab != 0) onNavigateToVault() },
                    icon = {
                        Icon(
                            Icons.Outlined.AdminPanelSettings, contentDescription = "Vault"
                        )
                    },
                    label = { Text("Vault") })
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { if (selectedTab != 1) onNavigateToGenerator() },
                    icon = { Icon(Icons.Outlined.Autorenew, contentDescription = "Generator") },
                    label = { Text("Generator") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { if (selectedTab != 2) onNavigateToSettings() },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        content = content
    )
}