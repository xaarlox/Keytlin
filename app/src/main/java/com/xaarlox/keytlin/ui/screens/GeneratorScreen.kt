package com.xaarlox.keytlin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.domain.service.PasswordGenerator
import com.xaarlox.keytlin.ui.components.MainScaffold
import com.xaarlox.keytlin.ui.components.PrimaryButton
import com.xaarlox.keytlin.ui.components.ToggleListItem
import com.xaarlox.keytlin.ui.theme.LocalExtendedColors

private const val MIN_LENGTH = 8
private const val MAX_LENGTH = 64

@Composable
fun GeneratorScreen(
    onNavigateToVault: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val generator = remember { PasswordGenerator() }
    val clipboardManager = LocalClipboardManager.current

    var length by remember { mutableFloatStateOf(13f) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }

    var generatedPassword by remember {
        mutableStateOf(
            generator.generate(length.toInt(), useUpper, useLower, useDigits, useSymbols)
        )
    }

    val extendedColors = LocalExtendedColors.current

    val entropyBits = generator.calculateEntropyBits(
        length.toInt(), useUpper, useLower, useDigits, useSymbols
    )
    val isStrong = entropyBits >= 40.0
    val strengthLabel = if (isStrong) "SECURE" else "WEAK"
    val strengthColor =
        if (isStrong) extendedColors.passwordStrong else MaterialTheme.colorScheme.error

    fun canDisable(current: Boolean): Boolean {
        val enabledCount = listOf(useUpper, useLower, useDigits, useSymbols).count { it }
        return !(current && enabledCount <= 1)
    }

    fun regenerate() {
        generatedPassword =
            generator.generate(length.toInt(), useUpper, useLower, useDigits, useSymbols)
    }

    MainScaffold(
        title = "Generator",
        selectedTab = 1,
        onNavigateToVault = onNavigateToVault,
        onNavigateToSettings = onNavigateToSettings
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = strengthColor
                        ) {
                            Text(
                                text = strengthLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(generatedPassword))
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy password",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = generatedPassword,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Password Length",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = length.toInt().toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = length,
                onValueChange = {
                    length = it
                    regenerate()
                },
                valueRange = MIN_LENGTH.toFloat()..MAX_LENGTH.toFloat(),
                steps = MAX_LENGTH - MIN_LENGTH - 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            ToggleListItem(
                title = "Uppercase",
                subtitle = "A-Z",
                checked = useUpper,
                onCheckedChange = {
                    if (canDisable(useUpper) || it) {
                        useUpper = it
                        regenerate()
                    }
                }
            )
            ToggleListItem(
                title = "Lowercase",
                subtitle = "a-z",
                checked = useLower,
                onCheckedChange = {
                    if (canDisable(useLower) || it) {
                        useLower = it
                        regenerate()
                    }
                }
            )
            ToggleListItem(
                title = "Numbers",
                subtitle = "0-9",
                checked = useDigits,
                onCheckedChange = {
                    if (canDisable(useDigits) || it) {
                        useDigits = it
                        regenerate()
                    }
                }
            )
            ToggleListItem(
                title = "Symbols",
                subtitle = "@#[$",
                checked = useSymbols,
                onCheckedChange = {
                    if (canDisable(useSymbols) || it) {
                        useSymbols = it
                        regenerate()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(text = "Generate New", onClick = { regenerate() })
        }
    }
}