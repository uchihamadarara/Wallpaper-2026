package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: String, // "3D", "VIDEO", "AI", "STATIC", "PACKAGE"
    val category: String,
    val url: String,
    val homeVideoUrl: String? = null,
    val lockVideoUrl: String? = null,
    val chargingVideoUrl: String? = null,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadedAtTimestamp: Long = 0L,
    val lastUsedTimestamp: Long = 0L,
    val downloadedPath: String? = null,
    val layersJson: String? = null // JSON Array of layer configs
)
