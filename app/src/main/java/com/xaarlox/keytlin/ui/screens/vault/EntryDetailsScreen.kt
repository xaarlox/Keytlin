package com.xaarlox.keytlin.ui.screens.vault

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.ui.components.common.label
import com.xaarlox.keytlin.ui.components.common.uiColor
import com.xaarlox.keytlin.ui.components.input.AppTextField
import com.xaarlox.keytlin.ui.components.input.PasswordTextField
import kotlin.text.ifEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailsScreen(
    uiState: EntryDetailsUiState,
    onTitleChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onGeneratePasswordClick: () -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onErrorShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    val screenTitle =
        if (uiState.isEditing) uiState.title.ifEmpty { "Unnamed Entry" } else "New Entry"
    val analysis = uiState.analysis

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSaveClick,
                        enabled = !uiState.isLoading && !uiState.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = "Website/App Name",
                isError = uiState.titleError != null,
                errorText = uiState.titleError
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = "Username/Email"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Password",
                trailingActions = { reveal ->
                    IconButton(onClick = {
                        onGeneratePasswordClick()
                        reveal()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Casino,
                            contentDescription = "Generate password"
                        )
                    }
                }
            )

            if (analysis != null) {
                PasswordStrengthIndicator(
                    progress = analysis.progress,
                    label = "${analysis.strength.label} password",
                    color = analysis.strength.uiColor(),
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
                )

                PasswordBreachStatusText(
                    status = uiState.passwordBreach,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
                )

                val breach = uiState.passwordBreach
                if (breach is PasswordBreachStatus.Breached) {
                    Spacer(modifier = Modifier.height(16.dp))
                    BreachedPasswordCard(
                        occurrences = breach.occurrences,
                        onSaveAnyway = onSaveClick,
                        onChangePassword = onGeneratePasswordClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = uiState.url,
                onValueChange = onUrlChange,
                label = "URL",
                keyboardType = KeyboardType.Uri
            )

            uiState.siteBreaches.forEach { warning ->
                Spacer(modifier = Modifier.height(12.dp))
                SiteBreachCard(
                    warning = warning,
                    onChangePassword = onGeneratePasswordClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = uiState.notes,
                onValueChange = onNotesChange,
                label = "Notes",
                singleLine = false,
                modifier = Modifier.height(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isEditing) {
                TextButton(
                    onClick = onDeleteClick,
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Delete Entry",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}