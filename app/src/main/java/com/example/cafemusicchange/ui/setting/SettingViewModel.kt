package com.example.cafemusicchange.ui.setting

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor() : ViewModel() {
    private val _isDarkMode = MutableStateFlow(false) // 🔥 Mặc định là Light Mode
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }
}
