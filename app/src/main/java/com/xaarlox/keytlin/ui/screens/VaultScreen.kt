package com.xaarlox.keytlin.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xaarlox.keytlin.domain.models.VaultEntry
import com.xaarlox.keytlin.ui.components.MainScaffold
import com.xaarlox.keytlin.ui.components.VaultListItem

val dummyVaultData = listOf(
    VaultEntry(id = "1", title = "Google", username = "sarah.connor@gmail.com"),
    VaultEntry(id = "2", title = "GitHub", username = "git-connor"),
    VaultEntry(id = "3", title = "Netflix", username = "connor.family@netflix.com"),
    VaultEntry(id = "4", title = "Slack Workspace", username = "sarah@cyberdyne.io"),
    VaultEntry(id = "5", title = "Amazon Web Services", username = "aws-admin")
)

@Composable
fun VaultScreen(
    onItemClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onNavigateToGenerator: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    MainScaffold(
        title = "Vault",
        selectedTab = 0,
        onNavigateToGenerator = onNavigateToGenerator,
        onNavigateToSettings = onNavigateToSettings,
        topBarActions = {
            IconButton(onClick = { /* TODO: Search */ }) {
                Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
            }
            IconButton(onClick = { /* TODO: Sort/Filter */ }) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort")
            }
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
        }
    ) { innerPadding ->
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