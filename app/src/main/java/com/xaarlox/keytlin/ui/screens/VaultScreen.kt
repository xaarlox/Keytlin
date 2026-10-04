package com.xaarlox.keytlin.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.xaarlox.keytlin.domain.models.VaultEntry
import com.xaarlox.keytlin.ui.components.VaultListItem

val dummyVaultData = listOf(
    VaultEntry(id = "1", title = "Google", username = "sarah.connor@gmail.com"),
    VaultEntry(id = "2", title = "GitHub", username = "git-connor"),
    VaultEntry(id = "3", title = "Netflix", username = "connor.family@netflix.com"),
    VaultEntry(id = "4", title = "Slack Workspace", username = "sarah@cyberdyne.io"),
    VaultEntry(id = "5", title = "Amazon Web Services", username = "aws-admin")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    onItemClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onNavigateToGenerator: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier = Modifier.height(72.dp), title = {
                    Text(
                        text = "Vault", style = MaterialTheme.typography.headlineMedium
                    )
                }, actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* TODO: Sort/Filter */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = "Sort"
                        )
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add new item")
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(72.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                windowInsets = WindowInsets(0.dp)
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    icon = {
                        Icon(
                            Icons.Outlined.AdminPanelSettings, contentDescription = "Vault"
                        )
                    },
                    label = { Text("Vault") })
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = {
                        selectedBottomTab = 1
                        onNavigateToGenerator()
                    },
                    icon = { Icon(Icons.Outlined.Autorenew, contentDescription = "Generator") },
                    label = { Text("Generator") })
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = {
                        selectedBottomTab = 2
                        onNavigateToSettings()
                    },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") })
            }
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(dummyVaultData) { entry ->
                VaultListItem(
                    entry = entry, onClick = { onItemClick(entry.id) })
            }
        }
    }
}