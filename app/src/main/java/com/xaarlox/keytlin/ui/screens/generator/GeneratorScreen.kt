package com.xaarlox.keytlin.ui.screens.generator

import android.content.ClipData
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.domain.service.PasswordGenerator
import com.xaarlox.keytlin.ui.components.common.PrimaryButton
import com.xaarlox.keytlin.ui.components.layout.MainScaffold
import com.xaarlox.keytlin.ui.components.list.ToggleListItem
import com.xaarlox.keytlin.ui.theme.LocalExtendedColors
import kotlinx.coroutines.launch

private const val MIN_LENGTH = 8
private const val MAX_LENGTH = 64
private const val DEFAULT_LENGTH = 13
private const val STRONG_ENTROPY_BITS = 40.0

@Composable
fun GeneratorScreen(
    onNavigateToVault: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val generator = remember { PasswordGenerator() }

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val extendedColors = LocalExtendedColors.current

    var length by remember { mutableFloatStateOf(DEFAULT_LENGTH.toFloat()) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }

    var generatedPassword by remember {
        mutableStateOf(
            generator.generate(DEFAULT_LENGTH, useUpper, useLower, useDigits, useSymbols)
        )
    }

    fun regenerate() {
        generatedPassword =
            generator.generate(length.toInt(), useUpper, useLower, useDigits, useSymbols)
    }

    val enabledCount = listOf(useUpper, useLower, useDigits, useSymbols).count { it }
    fun canChange(newValue: Boolean) = newValue || enabledCount > 1

    val entropyBits = generator.calculateEntropyBits(
        length.toInt(), useUpper, useLower, useDigits, useSymbols
    )
    val isStrong = entropyBits >= STRONG_ENTROPY_BITS
    val strengthLabel = if (isStrong) "SECURE" else "WEAK"
    val strengthColor =
        if (isStrong) extendedColors.passwordStrong else MaterialTheme.colorScheme.error

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
            GeneratedPasswordCard(
                password = generatedPassword,
                strengthLabel = strengthLabel,
                strengthColor = strengthColor,
                onCopyClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(ClipData.newPlainText("password", generatedPassword))
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            LabeledSlider(
                label = "Password Length",
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
                    if (canChange(it)) {
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
                    if (canChange(it)) {
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
                    if (canChange(it)) {
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
                    if (canChange(it)) {
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