package com.xaarlox.keytlin.ui.screens.vault

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.data.remote.hibp.HibpBreachesApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Warning about a known breach on the entry's website.
 * Contains a required (CC BY 4.0) link to the source — Have I Been Pwned
 */
@Composable
fun SiteBreachCard(
    warning: SiteBreachWarning,
    onChangePassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    val breach = warning.breach
    val uriHandler = LocalUriHandler.current
    val containerColor =
        if (warning.passwordMayBeAffected) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surfaceVariant
    val contentColor =
        if (warning.passwordMayBeAffected) MaterialTheme.colorScheme.onErrorContainer
        else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = containerColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = if (warning.passwordMayBeAffected) MaterialTheme.colorScheme.error else contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${breach.title} was breached",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${formatBreachDate(breach.breachDate)} · " +
                        "${"%,d".format(breach.affectedAccounts)} accounts · " +
                        breach.dataClasses.take(MAX_DATA_CLASSES).joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )

            if (warning.passwordMayBeAffected) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your password for this site hasn't changed since the breach. Change it.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = contentColor
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { uriHandler.openUri(HibpBreachesApi.ATTRIBUTION_URL) }) {
                    Text("Source: Have I Been Pwned", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.weight(1f))
                if (warning.passwordMayBeAffected) {
                    TextButton(onClick = onChangePassword) {
                        Text("Change Password")
                    }
                }
            }
        }
    }
}

private const val MAX_DATA_CLASSES = 4

private fun formatBreachDate(millis: Long): String =
    SimpleDateFormat("MMM yyyy", Locale.getDefault())
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(millis))