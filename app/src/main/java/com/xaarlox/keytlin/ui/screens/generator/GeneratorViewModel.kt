package com.xaarlox.keytlin.ui.screens.generator

import androidx.lifecycle.ViewModel
import com.xaarlox.keytlin.domain.models.GeneratorOptions
import com.xaarlox.keytlin.domain.service.PasswordGenerator
import com.xaarlox.keytlin.domain.service.PasswordStrengthEvaluator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class GeneratorViewModel @Inject constructor(
    private val generator: PasswordGenerator,
    private val evaluator: PasswordStrengthEvaluator
) : ViewModel() {

    private val _uiState = MutableStateFlow(stateFor(GeneratorOptions()))
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    private val options: GeneratorOptions get() = _uiState.value.options

    fun onLengthChange(length: Int) = updateOptions(
        options.copy(
            length = length.coerceIn(
                GeneratorOptions.MIN_LENGTH,
                GeneratorOptions.MAX_LENGTH
            )
        )
    )

    fun onUppercaseChange(enabled: Boolean) = toggle(enabled) { copy(useUpper = enabled) }

    fun onLowercaseChange(enabled: Boolean) = toggle(enabled) { copy(useLower = enabled) }

    fun onDigitsChange(enabled: Boolean) = toggle(enabled) { copy(useDigits = enabled) }

    fun onSymbolsChange(enabled: Boolean) = toggle(enabled) { copy(useSymbols = enabled) }

    fun regenerate() {
        _uiState.value = stateFor(options)
    }

    /** Prevents disabling the last enabled character set */
    private fun toggle(enabled: Boolean, change: GeneratorOptions.() -> GeneratorOptions) {
        if (!enabled && options.enabledSetsCount <= 1) return
        updateOptions(options.change())
    }

    private fun updateOptions(newOptions: GeneratorOptions) {
        if (newOptions == options) return
        _uiState.value = stateFor(newOptions)
    }

    private fun stateFor(options: GeneratorOptions): GeneratorUiState {
        val password = generator.generate(options)
        return GeneratorUiState(options, password, evaluator.analyze(password))
    }
}