package com.xaarlox.keytlin.ui.screens.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xaarlox.keytlin.ui.components.layout.MainScaffold
import com.xaarlox.keytlin.ui.components.list.SettingsListItem
import com.xaarlox.keytlin.ui.components.list.SettingsSectionHeader

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
    MainScaffold(
        title = "Settings",
        selectedTab = 2,
        onNavigateToVault = onNavigateToVault,
        onNavigateToGenerator = onNavigateToGenerator
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