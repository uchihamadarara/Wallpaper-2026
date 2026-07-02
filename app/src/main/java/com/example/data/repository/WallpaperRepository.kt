package com.example.data.repository

import com.example.data.local.WallpaperDao
import com.example.data.local.WallpaperEntity
import kotlinx.coroutines.flow.Flow

class WallpaperRepository(private val dao: WallpaperDao) {

    fun getTrendingWallpapers(): Flow<List<WallpaperEntity>> = dao.getAllWallpapers()

    fun getFavoriteWallpapers(): Flow<List<WallpaperEntity>> = dao.getFavoriteWallpapers()
    
    fun getDownloadedWallpapers(): Flow<List<WallpaperEntity>> = dao.getDownloadedWallpapers()
    
    fun getRecentWallpapers(): Flow<List<WallpaperEntity>> = dao.getRecentWallpapers()
    
    fun getCurrentWallpaper(): Flow<WallpaperEntity?> = dao.getCurrentWallpaper()
    
    suspend fun insertWallpaper(wallpaper: WallpaperEntity) = dao.insertWallpaper(wallpaper)
    
    suspend fun toggleFavorite(wallpaper: WallpaperEntity) {
        dao.updateFavoriteStatus(wallpaper.id, !wallpaper.isFavorite)
    }
    
    suspend fun updateLastUsed(id: String) {
        dao.updateLastUsed(id, System.currentTimeMillis())
    }
}
