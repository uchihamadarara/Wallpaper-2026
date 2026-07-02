package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.WallpaperRepository
import com.example.data.local.WallpaperEntity
import kotlinx.coroutines.launch

class AdminStudioViewModel(
    private val repository: WallpaperRepository
) : ViewModel() {

    fun saveWallpaper(entity: WallpaperEntity) {
        viewModelScope.launch {
            repository.insertWallpaper(entity)
        }
    }
}
