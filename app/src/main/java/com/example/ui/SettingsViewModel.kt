package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SettingsManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {

    private val _googleAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    val googleAccount: StateFlow<GoogleSignInAccount?> = _googleAccount.asStateFlow()

    fun setGoogleAccount(account: GoogleSignInAccount?) {
        _googleAccount.value = account
    }

    val darkMode = settingsManager.darkMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val dynamicColor = settingsManager.dynamicColor.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val highQuality = settingsManager.highQuality.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val support120Hz = settingsManager.support120Hz.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val particleEffects = settingsManager.particleEffects.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val autoApply = settingsManager.autoApply.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val parallaxStrength = settingsManager.parallaxStrength.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f
    )
    val batterySaver = settingsManager.batterySaver.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    fun setParallaxStrength(value: Float) {
        viewModelScope.launch {
            settingsManager.setParallaxStrength(value)
        }
    }

    fun setBatterySaver(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setBatterySaver(enabled)
        }
    }
    
    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { settingsManager.setDarkMode(enabled) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { settingsManager.setDynamicColor(enabled) }
    fun setHighQuality(enabled: Boolean) = viewModelScope.launch { settingsManager.setHighQuality(enabled) }
    fun setSupport120Hz(enabled: Boolean) = viewModelScope.launch { settingsManager.setSupport120Hz(enabled) }
    fun setParticleEffects(enabled: Boolean) = viewModelScope.launch { settingsManager.setParticleEffects(enabled) }
    fun setAutoApply(enabled: Boolean) = viewModelScope.launch { settingsManager.setAutoApply(enabled) }
}
