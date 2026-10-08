package com.xaarlox.keytlin.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GppGood
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.ui.components.BrandHeader
import com.xaarlox.keytlin.ui.components.PasswordTextField
import com.xaarlox.keytlin.ui.components.PrimaryButton
import com.xaarlox.keytlin.ui.components.ScreenColumn

@Composable
fun VaultLockedScreen(
    password: String,
    onPasswordChange: (String) -> Unit,
    isError: Boolean,
    onUnlockClick: () -> Unit
) {
    ScreenColumn(
        verticalPadding = 64.dp,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrandHeader(
            icon = Icons.Rounded.GppGood,
            title = "Vault Locked",
            subtitle = "Enter password to decrypt your data"
        )

        Spacer(modifier = Modifier.height(24.dp))

        PasswordTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Master Password",
            isError = isError,
            errorText = "Incorrect password"
        )

        Spacer(modifier = Modifier.height(48.dp))

        PrimaryButton(text = "Unlock", onClick = onUnlockClick)
    }
}