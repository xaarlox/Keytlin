package com.xaarlox.keytlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.ui.components.PasswordTextField
import com.xaarlox.keytlin.ui.components.PrimaryButton
import com.xaarlox.keytlin.ui.components.ScreenColumn
import com.xaarlox.keytlin.ui.theme.LocalExtendedColors

@Composable
fun CreateVaultScreen(
    masterPassword: String,
    onMasterPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    isError: Boolean,
    onCreateClick: () -> Unit
) {
    val warningColors = LocalExtendedColors.current

    ScreenColumn {
        Text(
            text = "Create Vault",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(warningColors.warningContainer)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = "Warning",
                tint = warningColors.onWarningContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Master Password cannot be recovered if lost.",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = warningColors.onWarningContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        PasswordTextField(
            value = masterPassword,
            onValueChange = onMasterPasswordChange,
            label = "Master Password",
            isError = isError
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm Password",
            isError = isError,
            errorText = if (masterPassword.isEmpty()) "Password cannot be empty" else "Passwords do not match"
        )

        Spacer(modifier = Modifier.height(48.dp))

        PrimaryButton(text = "Create", onClick = onCreateClick)
    }
}