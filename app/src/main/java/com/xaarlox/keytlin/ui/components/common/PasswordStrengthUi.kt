package com.xaarlox.keytlin.ui.components.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.xaarlox.keytlin.domain.models.PasswordStrength
import com.xaarlox.keytlin.ui.theme.LocalExtendedColors

val PasswordStrength.label: String
    get() = when (this) {
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.MEDIUM -> "Medium"
        PasswordStrength.STRONG -> "Strong"
    }

@Composable
fun PasswordStrength.uiColor(): Color {
    val extended = LocalExtendedColors.current

    return when (this) {
        PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
        PasswordStrength.MEDIUM -> extended.passwordMedium
        PasswordStrength.STRONG -> extended.passwordStrong
    }
}