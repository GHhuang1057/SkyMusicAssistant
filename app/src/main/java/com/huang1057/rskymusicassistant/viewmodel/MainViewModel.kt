// Copyright (C) 2025 huang1057  
// SPDX-License-Identifier: GPL-3.0-or-later  
package com.huang1057.rskymusicassistant.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huang1057.rskymusicassistant.config.ConfigManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _hasAccessibilityPermission = MutableStateFlow(false)
    val hasAccessibilityPermission: StateFlow<Boolean> = _hasAccessibilityPermission.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentConfig = MutableStateFlow<ConfigManager.Config?>(null)
    val currentConfig: StateFlow<ConfigManager.Config?> = _currentConfig.asStateFlow()
    
    fun updateConfig(config: ConfigManager.Config) {
        _currentConfig.value = config
    }
    
    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
    }
    
    fun updateAccessibilityPermission(enabled: Boolean) {
        _hasAccessibilityPermission.value = enabled
    }
}