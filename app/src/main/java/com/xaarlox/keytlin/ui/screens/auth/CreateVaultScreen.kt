package com.xaarlox.keytlin.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.ui.components.common.PrimaryButton
import com.xaarlox.keytlin.ui.components.common.ScreenColumn
import com.xaarlox.keytlin.ui.components.common.WarningBanner
import com.xaarlox.keytlin.ui.components.input.PasswordTextField

@Composable
fun CreateVaultScreen(
    masterPassword: String,
    onMasterPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    errorText: String?,
    onCreateClick: () -> Unit
) {
    ScreenColumn {
        Text(
            text = "Create Vault",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        WarningBanner(text = "Master Password cannot be recovered if lost.")

        Spacer(modifier = Modifier.height(24.dp))

        PasswordTextField(
            value = masterPassword,
            onValueChange = onMasterPasswordChange,
            label = "Master Password",
            isError = errorText != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm Password",
            isError = errorText != null,
            errorText = errorText
        )

        Spacer(modifier = Modifier.height(48.dp))

        PrimaryButton(text = "Create", onClick = onCreateClick)
    }
}