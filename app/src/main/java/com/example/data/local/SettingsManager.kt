package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val PARALLAX_STRENGTH = floatPreferencesKey("parallax_strength")
        val BATTERY_SAVER = booleanPreferencesKey("battery_saver")
        val BLOOM_ENABLED = booleanPreferencesKey("bloom_enabled")
        val PARTICLE_DENSITY = floatPreferencesKey("particle_density")
        
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val HIGH_QUALITY = booleanPreferencesKey("high_quality")
        val SUPPORT_120HZ = booleanPreferencesKey("support_120hz")
        val PARTICLE_EFFECTS = booleanPreferencesKey("particle_effects")
        val AUTO_APPLY = booleanPreferencesKey("auto_apply")
        
        val ACTIVE_HOME_VIDEO = androidx.datastore.preferences.core.stringPreferencesKey("active_home_video")
        val ACTIVE_LOCK_VIDEO = androidx.datastore.preferences.core.stringPreferencesKey("active_lock_video")
        val ACTIVE_CHARGING_VIDEO = androidx.datastore.preferences.core.stringPreferencesKey("active_charging_video")
    }

    val activeHomeVideo: Flow<String?> = context.dataStore.data.map { it[ACTIVE_HOME_VIDEO] }
    val activeLockVideo: Flow<String?> = context.dataStore.data.map { it[ACTIVE_LOCK_VIDEO] }
    val activeChargingVideo: Flow<String?> = context.dataStore.data.map { it[ACTIVE_CHARGING_VIDEO] }

    suspend fun setActiveHomeVideo(path: String?) {
        context.dataStore.edit { prefs -> 
            if (path == null) prefs.remove(ACTIVE_HOME_VIDEO) else prefs[ACTIVE_HOME_VIDEO] = path
        }
    }
    
    suspend fun setActiveLockVideo(path: String?) {
        context.dataStore.edit { prefs -> 
            if (path == null) prefs.remove(ACTIVE_LOCK_VIDEO) else prefs[ACTIVE_LOCK_VIDEO] = path
        }
    }
    
    suspend fun setActiveChargingVideo(path: String?) {
        context.dataStore.edit { prefs -> 
            if (path == null) prefs.remove(ACTIVE_CHARGING_VIDEO) else prefs[ACTIVE_CHARGING_VIDEO] = path
        }
    }

    val parallaxStrength: Flow<Float> = context.dataStore.data.map { it[PARALLAX_STRENGTH] ?: 0.5f }
    val batterySaver: Flow<Boolean> = context.dataStore.data.map { it[BATTERY_SAVER] ?: false }
    val bloomEnabled: Flow<Boolean> = context.dataStore.data.map { it[BLOOM_ENABLED] ?: true }
    val particleDensity: Flow<Float> = context.dataStore.data.map { it[PARTICLE_DENSITY] ?: 1.0f }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: true }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_COLOR] ?: true }
    val highQuality: Flow<Boolean> = context.dataStore.data.map { it[HIGH_QUALITY] ?: true }
    val support120Hz: Flow<Boolean> = context.dataStore.data.map { it[SUPPORT_120HZ] ?: true }
    val particleEffects: Flow<Boolean> = context.dataStore.data.map { it[PARTICLE_EFFECTS] ?: true }
    val autoApply: Flow<Boolean> = context.dataStore.data.map { it[AUTO_APPLY] ?: true }

    suspend fun setParallaxStrength(value: Float) {
        context.dataStore.edit { it[PARALLAX_STRENGTH] = value }
    }

    suspend fun setBatterySaver(enabled: Boolean) {
        context.dataStore.edit { it[BATTERY_SAVER] = enabled }
    }
    
    suspend fun setDarkMode(enabled: Boolean) { context.dataStore.edit { it[DARK_MODE] = enabled } }
    suspend fun setDynamicColor(enabled: Boolean) { context.dataStore.edit { it[DYNAMIC_COLOR] = enabled } }
    suspend fun setHighQuality(enabled: Boolean) { context.dataStore.edit { it[HIGH_QUALITY] = enabled } }
    suspend fun setSupport120Hz(enabled: Boolean) { context.dataStore.edit { it[SUPPORT_120HZ] = enabled } }
    suspend fun setParticleEffects(enabled: Boolean) { context.dataStore.edit { it[PARTICLE_EFFECTS] = enabled } }
    suspend fun setAutoApply(enabled: Boolean) { context.dataStore.edit { it[AUTO_APPLY] = enabled } }
}
