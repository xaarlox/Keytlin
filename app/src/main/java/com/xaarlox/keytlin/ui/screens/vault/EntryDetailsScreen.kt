package com.xaarlox.keytlin.ui.screens.vault

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.domain.models.VaultEntry
import com.xaarlox.keytlin.domain.service.PasswordGenerator
import com.xaarlox.keytlin.ui.components.input.AppTextField
import com.xaarlox.keytlin.ui.components.input.PasswordTextField
import com.xaarlox.keytlin.ui.theme.LocalExtendedColors
import java.util.UUID
import kotlin.text.ifEmpty

private const val STRONG_ENTROPY_BITS = 40.0
private const val MAX_ENTROPY_BITS = 100.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailsScreen(
    entry: VaultEntry? = null,
    onBackClick: () -> Unit,
    onSaveClick: (VaultEntry) -> Unit,
    onDeleteClick: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf(entry?.title ?: "") }
    var username by rememberSaveable { mutableStateOf(entry?.username ?: "") }
    var url by rememberSaveable { mutableStateOf(entry?.url ?: "") }
    var notes by rememberSaveable { mutableStateOf(entry?.notes ?: "") }
    var password by remember { mutableStateOf(entry?.password ?: "") }

    val generator = remember { PasswordGenerator() }
    val extendedColors = LocalExtendedColors.current

    val isEditing = entry != null
    val screenTitle = if (isEditing) title.ifEmpty { "Unnamed Entry" } else "New Entry"

    val entropyBits = generator.calculateEntropyBits(
        length = password.length,
        useUpper = password.any { it.isUpperCase() },
        useLower = password.any { it.isLowerCase() },
        useDigits = password.any { it.isDigit() },
        useSymbols = password.any { !it.isLetterOrDigit() }
    )
    val isStrong = entropyBits >= STRONG_ENTROPY_BITS
    val strengthColor =
        if (isStrong) extendedColors.passwordStrong else MaterialTheme.colorScheme.error
    val strengthText = if (isStrong) "Strong Password" else "Weak / Reused Password"
    val isBreached = password == "12345678" || password.lowercase() == "password"

    Scaffold(
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
                    IconButton(onClick = {
                        onSaveClick(
                            VaultEntry(
                                id = entry?.id ?: UUID.randomUUID().toString(),
                                title = title,
                                username = username,
                                password = password,
                                url = url,
                                notes = notes
                            )
                        )
                    }) {
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
                value = title,
                onValueChange = { title = it },
                label = "Website/App Name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username/Email"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                trailingActions = { reveal ->
                    IconButton(onClick = {
                        password = generator.generate(
                            length = 16,
                            useUpper = true,
                            useLower = true,
                            useDigits = true,
                            useSymbols = true
                        )
                        reveal()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Casino,
                            contentDescription = "Generate password"
                        )
                    }
                }
            )

            if (password.isNotEmpty()) {
                PasswordStrengthIndicator(
                    progress = (entropyBits / MAX_ENTROPY_BITS).toFloat(),
                    label = strengthText,
                    color = strengthColor,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
                )

                if (isBreached) {
                    Spacer(modifier = Modifier.height(16.dp))
                    BreachedPasswordCard(
                        onSaveAnyway = { /* TODO: Save anyway */ },
                        onChangePassword = { /* TODO: Change password */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = url,
                onValueChange = { url = it },
                label = "URL",
                keyboardType = KeyboardType.Uri
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                singleLine = false,
                modifier = Modifier.height(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isEditing) {
                TextButton(
                    onClick = onDeleteClick,
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