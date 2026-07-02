package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WallpaperEntity
import com.example.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

class WallpaperDetailViewModel(
    private val repository: WallpaperRepository,
    private val settingsManager: com.example.data.local.SettingsManager
) : ViewModel() {

    private val _wallpaper = MutableStateFlow<WallpaperEntity?>(null)
    val wallpaper: StateFlow<WallpaperEntity?> = _wallpaper.asStateFlow()

    fun loadWallpaper(id: String) {
        viewModelScope.launch {
            val all = repository.getTrendingWallpapers().firstOrNull() ?: emptyList()
            _wallpaper.value = all.find { it.id == id }
        }
    }

    fun applyWallpaper(id: String) {
        viewModelScope.launch {
            repository.updateLastUsed(id)
            val wp = _wallpaper.value ?: repository.getTrendingWallpapers().firstOrNull()?.find { it.id == id }
            if (wp != null) {
                settingsManager.setActiveHomeVideo(wp.homeVideoUrl ?: wp.url)
                settingsManager.setActiveLockVideo(wp.lockVideoUrl)
                settingsManager.setActiveChargingVideo(wp.chargingVideoUrl)
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _wallpaper.value?.let { wp ->
                repository.toggleFavorite(wp)
                _wallpaper.value = wp.copy(isFavorite = !wp.isFavorite)
            }
        }
    }
}
