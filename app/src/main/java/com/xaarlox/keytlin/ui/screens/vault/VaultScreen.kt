package com.xaarlox.keytlin.ui.screens.vault

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.ui.components.input.AppTextField
import com.xaarlox.keytlin.ui.components.layout.MainScaffold
import com.xaarlox.keytlin.ui.components.list.VaultListItem

@Composable
fun VaultScreen(
    uiState: VaultListUiState,
    onQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onSortToggle: () -> Unit,
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
            IconButton(onClick = onSearchToggle) {
                Icon(
                    imageVector = if (uiState.isSearchActive) Icons.Rounded.Close else Icons.Rounded.Search,
                    contentDescription = if (uiState.isSearchActive) "Close search" else "Search"
                )
            }
            IconButton(onClick = onSortToggle) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                    contentDescription = when (uiState.sortOrder) {
                        VaultSortOrder.TITLE -> "Sorted by title"
                        VaultSortOrder.RECENTLY_MODIFIED -> "Sorted by last modified"
                    }
                )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isSearchActive) {
                AppTextField(
                    value = uiState.query,
                    onValueChange = onQueryChange,
                    label = "Search",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            when {
                uiState.isLoading -> CenteredBox { CircularProgressIndicator() }

                uiState.errorMessage != null -> CenteredBox {
                    Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                }

                uiState.isEmpty -> CenteredBox {
                    Text(
                        text = if (uiState.query.isNotBlank()) "Nothing found" else "Your vault is empty",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.entries, key = { it.id }) { entry ->
                        VaultListItem(entry = entry, onClick = { onItemClick(entry.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        content()
    }
}