package com.example.pushoflife.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Emergency에 사용하는 DataStore
val Context.emergencyDataStore by preferencesDataStore(name = "emergency_preferences")

object EmergencyPreferencesKeys {
    val LATITUDE_KEY = doublePreferencesKey("latitude")
    val LONGITUDE_KEY = doublePreferencesKey("longitude")
}

class EmergencyPreferences(private val context: Context) {

    // 위치 정보 저장 함수
    suspend fun saveLocation(latitude: Double, longitude: Double) {
        context.emergencyDataStore.edit { preferences ->
            preferences[EmergencyPreferencesKeys.LATITUDE_KEY] = latitude
            preferences[EmergencyPreferencesKeys.LONGITUDE_KEY] = longitude
        }
    }

    // 위치 정보 가져오는 함수 (Flow로 반환)
    fun getLocation(): Flow<Pair<Double?, Double?>> {
        return context.emergencyDataStore.data.map { preferences ->
            val latitude = preferences[EmergencyPreferencesKeys.LATITUDE_KEY]
            val longitude = preferences[EmergencyPreferencesKeys.LONGITUDE_KEY]
            Pair(latitude, longitude)
        }
    }

    // 위치 정보 삭제 함수
    suspend fun clearLocation() {
        context.emergencyDataStore.edit { preferences ->
            preferences.remove(EmergencyPreferencesKeys.LATITUDE_KEY)
            preferences.remove(EmergencyPreferencesKeys.LONGITUDE_KEY)
        }
    }
}
