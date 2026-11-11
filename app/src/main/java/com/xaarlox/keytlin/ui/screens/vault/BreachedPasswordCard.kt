package com.xaarlox.keytlin.ui.screens.vault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BreachedPasswordCard(
    occurrences: Long?,
    onSaveAnyway: () -> Unit,
    onChangePassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Password Breached",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (occurrences != null) {
                    "This password has appeared ${"%,d".format(occurrences)} times in public data breaches. " +
                            "Attackers try such passwords first."
                } else {
                    "This is one of the most common passwords and is found in public data breaches."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onSaveAnyway,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Save Anyway")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onChangePassword,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Change Password")
                }
            }
        }
    }
}

/** Online check status line under the strength indicator */
@Composable
fun PasswordBreachStatusText(
    status: PasswordBreachStatus,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (status) {
        PasswordBreachStatus.Checking ->
            "Checking known data breaches…" to MaterialTheme.colorScheme.onSurfaceVariant

        PasswordBreachStatus.Safe ->
            "Not found in known data breaches" to MaterialTheme.colorScheme.primary

        PasswordBreachStatus.Unavailable ->
            "Couldn't check data breaches (offline)" to MaterialTheme.colorScheme.onSurfaceVariant

        PasswordBreachStatus.Idle, is PasswordBreachStatus.Breached -> return
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        modifier = modifier
    )
}