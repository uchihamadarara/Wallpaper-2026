package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(entities = [WallpaperEntity::class, WallpaperScheduleEntity::class], version = 4, exportSchema = false)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao
    abstract fun wallpaperScheduleDao(): WallpaperScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getDatabase(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "nova_database"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).wallpaperDao()
                            val seedData = listOf(
                                WallpaperEntity(UUID.randomUUID().toString(), "Cosmic Depth", "3D", "Space", "url_here", isFavorite = false, isDownloaded = true, downloadedAtTimestamp = System.currentTimeMillis()),
                                WallpaperEntity(UUID.randomUUID().toString(), "Neon Rain", "VIDEO", "Cyberpunk", "url_here"),
                                WallpaperEntity(UUID.randomUUID().toString(), "Abstract Waves", "STATIC", "Minimal", "url_here")
                            )
                            seedData.forEach { dao.insertWallpaper(it) }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
