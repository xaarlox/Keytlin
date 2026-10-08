package com.xaarlox.keytlin.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.ui.components.BrandHeader
import com.xaarlox.keytlin.ui.components.PrimaryButton
import com.xaarlox.keytlin.ui.components.ScreenColumn

@Composable
fun WelcomeChoiceScreen(
    onCreateVaultClick: () -> Unit,
    onRestoreVaultClick: () -> Unit
) {
    ScreenColumn(
        verticalPadding = 64.dp,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrandHeader(
            icon = Icons.Rounded.Key,
            iconRotation = 45f,
            title = "Welcome",
            subtitle = "Securing your passwords, credentials, and private notes with zero-knowledge encryption"
        )

        Spacer(modifier = Modifier.height(48.dp))

        PrimaryButton(text = "Create New Vault", onClick = onCreateVaultClick)

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = onRestoreVaultClick,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Already have a vault on another device? Restore with sync code",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}