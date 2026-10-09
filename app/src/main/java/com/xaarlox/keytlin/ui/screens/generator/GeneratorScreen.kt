package com.xaarlox.keytlin.ui.screens.generator

import android.content.ClipData
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.domain.models.GeneratorOptions
import com.xaarlox.keytlin.domain.service.PasswordStrengthEvaluator
import com.xaarlox.keytlin.domain.service.PasswordGenerator
import com.xaarlox.keytlin.ui.components.common.PrimaryButton
import com.xaarlox.keytlin.ui.components.common.label
import com.xaarlox.keytlin.ui.components.common.uiColor
import com.xaarlox.keytlin.ui.components.layout.MainScaffold
import com.xaarlox.keytlin.ui.components.list.ToggleListItem
import kotlinx.coroutines.launch

@Composable
fun GeneratorScreen(
    onNavigateToVault: () -> Unit, onNavigateToSettings: () -> Unit
) {
    val generator = remember { PasswordGenerator() }
    val evaluator = remember { PasswordStrengthEvaluator() }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    var options by remember { mutableStateOf(GeneratorOptions()) }
    var generatedPassword by remember {
        mutableStateOf(
            generator.generate(GeneratorOptions())
        )
    }

    fun update(newOptions: GeneratorOptions) {
        options = newOptions
        generatedPassword = generator.generate(newOptions)
    }

    fun canChange(newValue: Boolean) = newValue || options.enabledSetsCount > 1

    val analysis = remember(generatedPassword) { evaluator.analyze(generatedPassword) }

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
                strengthLabel = analysis.strength.label.uppercase(),
                strengthColor = analysis.strength.uiColor(),
                onCopyClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(ClipData.newPlainText("password", generatedPassword))
                        )
                    }
                })

            Spacer(modifier = Modifier.height(32.dp))

            LabeledSlider(
                label = "Password Length",
                value = options.length.toFloat(),
                onValueChange = { update(options.copy(length = it.toInt())) },
                valueRange = GeneratorOptions.MIN_LENGTH.toFloat()..GeneratorOptions.MAX_LENGTH.toFloat(),
                steps = GeneratorOptions.MAX_LENGTH - GeneratorOptions.MIN_LENGTH - 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            ToggleListItem(
                title = "Uppercase",
                subtitle = "A-Z",
                checked = options.useUpper,
                onCheckedChange = {
                    if (canChange(it)) update(options.copy(useUpper = it))
                })
            ToggleListItem(
                title = "Lowercase",
                subtitle = "a-z",
                checked = options.useLower,
                onCheckedChange = {
                    if (canChange(it)) update(options.copy(useLower = it))
                })
            ToggleListItem(
                title = "Numbers",
                subtitle = "0-9",
                checked = options.useDigits,
                onCheckedChange = {
                    if (canChange(it)) update(options.copy(useDigits = it))
                })
            ToggleListItem(
                title = "Symbols",
                subtitle = "@#[$",
                checked = options.useSymbols,
                onCheckedChange = {
                    if (canChange(it)) update(options.copy(useSymbols = it))
                })

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Generate New",
                onClick = { generatedPassword = generator.generate(options) })
        }
    }
}