package com.xaarlox.keytlin.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.ui.components.AppTextField
import com.xaarlox.keytlin.ui.components.PrimaryButton
import com.xaarlox.keytlin.ui.components.ScreenColumn

@Composable
fun EnterSyncCodeScreen(
    code: String,
    onCodeChange: (String) -> Unit,
    isError: Boolean,
    onContinueClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onBackClick: () -> Unit
) {
    ScreenColumn(verticalPadding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Restore Vault",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Enter your sync code exactly as shown on your active device to download and decrypt your vault data safely.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Justify
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppTextField(
            value = code,
            onValueChange = onCodeChange,
            label = "Sync code",
            isError = isError,
            errorText = "Code not found",
            trailingIcon = {
                IconButton(onClick = onScanQrClick) {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = "Scan QR code")
                }
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        PrimaryButton(text = "Continue", onClick = onContinueClick)
    }
}