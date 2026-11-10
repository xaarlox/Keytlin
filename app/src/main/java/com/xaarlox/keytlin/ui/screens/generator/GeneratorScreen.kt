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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import com.xaarlox.keytlin.domain.models.GeneratorOptions
import com.xaarlox.keytlin.ui.components.common.PrimaryButton
import com.xaarlox.keytlin.ui.components.common.label
import com.xaarlox.keytlin.ui.components.common.uiColor
import com.xaarlox.keytlin.ui.components.layout.MainScaffold
import com.xaarlox.keytlin.ui.components.list.ToggleListItem
import kotlinx.coroutines.launch

@Composable
fun GeneratorScreen(
    uiState: GeneratorUiState,
    onLengthChange: (Int) -> Unit,
    onUppercaseChange: (Boolean) -> Unit,
    onLowercaseChange: (Boolean) -> Unit,
    onDigitsChange: (Boolean) -> Unit,
    onSymbolsChange: (Boolean) -> Unit,
    onRegenerateClick: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val options = uiState.options

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
                password = uiState.password,
                strengthLabel = uiState.analysis?.strength?.label?.uppercase().orEmpty(),
                strengthColor = uiState.analysis?.strength?.uiColor()
                    ?: MaterialTheme.colorScheme.onSurfaceVariant,
                onCopyClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(ClipData.newPlainText("password", uiState.password))
                        )
                    }
                })

            Spacer(modifier = Modifier.height(32.dp))

            LabeledSlider(
                label = "Password Length",
                value = options.length.toFloat(),
                onValueChange = { onLengthChange(it.toInt()) },
                valueRange = GeneratorOptions.MIN_LENGTH.toFloat()..GeneratorOptions.MAX_LENGTH.toFloat(),
                steps = GeneratorOptions.MAX_LENGTH - GeneratorOptions.MIN_LENGTH - 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            ToggleListItem(
                title = "Uppercase",
                subtitle = "A-Z",
                checked = options.useUpper,
                onCheckedChange = onUppercaseChange
            )
            ToggleListItem(
                title = "Lowercase",
                subtitle = "a-z",
                checked = options.useLower,
                onCheckedChange = onLowercaseChange
            )
            ToggleListItem(
                title = "Numbers",
                subtitle = "0-9",
                checked = options.useDigits,
                onCheckedChange = onDigitsChange
            )
            ToggleListItem(
                title = "Symbols",
                subtitle = "@#[$",
                checked = options.useSymbols,
                onCheckedChange = onSymbolsChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Generate New", onClick = onRegenerateClick
            )
        }
    }
}