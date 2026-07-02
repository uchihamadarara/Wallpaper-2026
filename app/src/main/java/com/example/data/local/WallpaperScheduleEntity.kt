package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpaper_schedules")
data class WallpaperScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleName: String,
    val intervalMinutes: Int, // e.g., 60 for 1 hour, 1440 for 1 day
    val applyTarget: String, // "HOME_SCREEN", "LOCK_SCREEN", "BOTH"
    val categoryFilter: String? = null, // Optional category to restrict random wallpapers
    val isActive: Boolean = true,
    val lastAppliedTimestamp: Long = 0L
)
