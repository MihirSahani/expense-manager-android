package com.example.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.settingDataStore: DataStore<Preferences> by preferencesDataStore("app-setting")

class Setting(val context: Context) {
    val userFirstName: Flow<String> = getSetting { preferences ->
        preferences[SettingKey.USER_FIST_NAME] ?: "Jane"
    }
    val userLastName: Flow<String?> = getSetting { preferences ->
        preferences[SettingKey.USER_LAST_NAME]
    }
    val cycleType: Flow<String> = getSetting { preferences ->
        preferences[SettingKey.CYCLE_TYPE] ?: "MONTHLY"
    }
    val currency: Flow<String> = getSetting { preferences ->
        preferences[SettingKey.CURRENCY] ?: "INR"
    }
    val isOnboardingDone: Flow<Boolean> = getSetting { preferences ->
        preferences[SettingKey.IS_ONBOARDING_DONE] ?: false
    }

    // NOTE: These accept raw strings here, its responsibility of the caller to validate the input

    suspend fun setUserFirstName(firstName: String) {
        setSetting(SettingKey.USER_FIST_NAME, firstName)
    }

    suspend fun setUserLastName(lastName: String) {
        setSetting(SettingKey.USER_LAST_NAME, lastName)
    }

    suspend fun setCycleType(cycleType: String) {
        setSetting(SettingKey.CYCLE_TYPE, cycleType)
    }

    suspend fun setCurrency(currency: String) {
        setSetting(SettingKey.CURRENCY, currency)
    }

    suspend fun setOnboardingDone(isOnboardingDone: Boolean) {
        setSetting(SettingKey.IS_ONBOARDING_DONE, isOnboardingDone)
    }

    private fun <T> getSetting(transform: (Preferences) -> T): Flow<T> =
        context.settingDataStore.data
            .catch { throwable ->
                if (throwable is IOException) emit(emptyPreferences()) else throw throwable
            }
            .map { preferences: Preferences ->
                transform(preferences)
            }

    private suspend fun <T> setSetting(key: Preferences.Key<T>, value: T) {
        context.settingDataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}