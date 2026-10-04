package com.xaarlox.keytlin.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Sync
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.ui.components.SettingsListItem
import com.xaarlox.keytlin.ui.components.SettingsSectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToVault: () -> Unit,
    onNavigateToGenerator: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onAutoLockClick: () -> Unit,
    onLockVaultClick: () -> Unit,
    onSyncCodeClick: () -> Unit,
    onSyncNowClick: () -> Unit,
    onExportClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var selectedBottomTab by remember { mutableIntStateOf(2) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier = Modifier.height(72.dp),
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(72.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                windowInsets = WindowInsets(0.dp)
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = {
                        selectedBottomTab = 0
                        onNavigateToVault()
                    },
                    icon = {
                        Icon(
                            Icons.Outlined.AdminPanelSettings,
                            contentDescription = "Vault"
                        )
                    },
                    label = { Text("Vault") }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = {
                        selectedBottomTab = 1
                        onNavigateToGenerator()
                    },
                    icon = { Icon(Icons.Outlined.Autorenew, contentDescription = "Generator") },
                    label = { Text("Generator") }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2 },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                SettingsSectionHeader("Security")
                SettingsListItem(
                    title = "Change Master Password",
                    icon = Icons.Outlined.ManageAccounts,
                    onClick = onChangePasswordClick
                )
                SettingsListItem(
                    title = "Auto-lock Timeout",
                    subtitle = "Lock after 5 minutes",
                    icon = Icons.Outlined.Schedule,
                    onClick = onAutoLockClick
                )
                SettingsListItem(
                    title = "Lock Vault Now",
                    icon = Icons.Rounded.Cancel,
                    onClick = onLockVaultClick
                )
            }

            item {
                SettingsSectionHeader("Sync")
                SettingsListItem(
                    title = "Sync Code",
                    subtitle = "Show device token / QR",
                    icon = Icons.Outlined.QrCodeScanner,
                    onClick = onSyncCodeClick
                )
                SettingsListItem(
                    title = "Sync Now",
                    subtitle = "Last synced today at 14:30",
                    icon = Icons.Rounded.Sync,
                    onClick = onSyncNowClick
                )
            }

            item {
                SettingsSectionHeader("Data")
                SettingsListItem(
                    title = "Export Backup",
                    icon = Icons.Outlined.CloudUpload,
                    onClick = onExportClick
                )
                SettingsListItem(
                    title = "Delete All Data",
                    icon = Icons.Outlined.Delete,
                    isDestructive = true,
                    onClick = onDeleteClick,
                    showDivider = false
                )
            }
        }
    }
}