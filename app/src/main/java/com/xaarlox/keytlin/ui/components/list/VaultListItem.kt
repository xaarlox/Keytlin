package com.xaarlox.keytlin.ui.components.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.domain.models.VaultEntry
import com.xaarlox.keytlin.ui.components.common.LeadingCircle
import com.xaarlox.keytlin.ui.components.common.ListItemDivider

@Composable
fun VaultListItem(
    entry: VaultEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Бізнес-логіка, яка буде винесена пізніше
    val initials = remember(entry.title) {
        val title = entry.title.trim()
        if (title.isEmpty()) return@remember "V"

        var result = title[0].uppercaseChar().toString()

        val words = title.split(Regex("\\s+"))
        if (words.size > 1 && words[1].isNotEmpty()) {
            result += words[1][0].uppercaseChar()
        } else {
            val secondCapital = title.drop(1).firstOrNull { it.isUpperCase() }
            if (secondCapital != null) {
                result += secondCapital
            }
        }
        result.take(2)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LeadingCircle {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ListItemDivider()
    }
}