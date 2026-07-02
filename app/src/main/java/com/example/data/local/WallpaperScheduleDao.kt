package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperScheduleDao {
    @Query("SELECT * FROM wallpaper_schedules")
    fun getAllSchedules(): Flow<List<WallpaperScheduleEntity>>
    
    @Query("SELECT * FROM wallpaper_schedules WHERE isActive = 1")
    fun getActiveSchedules(): Flow<List<WallpaperScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: WallpaperScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: WallpaperScheduleEntity)

    @Query("UPDATE wallpaper_schedules SET isActive = :isActive WHERE id = :id")
    suspend fun updateScheduleStatus(id: Long, isActive: Boolean)
    
    @Query("UPDATE wallpaper_schedules SET lastAppliedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateLastAppliedTimestamp(id: Long, timestamp: Long)
}
