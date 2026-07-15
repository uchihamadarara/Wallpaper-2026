package com.example.di

import com.example.BuildConfig
import com.example.data.local.NovaDatabase
import com.example.data.local.SettingsManager
import com.example.data.repository.WallpaperRepository
import com.example.ui.HomeViewModel
import com.example.ui.SettingsViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

import com.example.ui.WallpaperDetailViewModel

val appModule = module {
    single { NovaDatabase.getDatabase(androidContext()) }
    single { get<NovaDatabase>().wallpaperDao() }
    single { get<NovaDatabase>().wallpaperScheduleDao() }
    single { SettingsManager(androidContext()) }
    single { WallpaperRepository(get()) }
    
    // Networking
    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    viewModel { HomeViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { WallpaperDetailViewModel(get(), get()) }
}
