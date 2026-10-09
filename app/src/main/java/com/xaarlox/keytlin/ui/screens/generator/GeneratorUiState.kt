package com.xaarlox.keytlin.ui.screens.generator

import com.xaarlox.keytlin.domain.models.GeneratorOptions
import com.xaarlox.keytlin.domain.models.PasswordAnalysis

data class GeneratorUiState(
    val options: GeneratorOptions = GeneratorOptions(),
    val password: String = "",
    val analysis: PasswordAnalysis? = null
)