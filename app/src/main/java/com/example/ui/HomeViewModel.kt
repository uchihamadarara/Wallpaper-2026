package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WallpaperEntity
import com.example.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: WallpaperRepository) : ViewModel() {
    private val _trendingWallpapers = MutableStateFlow<List<WallpaperEntity>>(emptyList())
    val trendingWallpapers: StateFlow<List<WallpaperEntity>> = _trendingWallpapers.asStateFlow()

    init {
        fetchTrending()
    }

    private fun fetchTrending() {
        viewModelScope.launch {
            repository.getTrendingWallpapers().collect {
                _trendingWallpapers.value = it
            }
        }
    }
}
