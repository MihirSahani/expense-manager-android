package com.example.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.datastore.model.Currency
import com.example.datastore.model.CycleType
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
    val cycleType: Flow<CycleType> = getSetting { preferences ->
        CycleType.valueOf(preferences[SettingKey.CYCLE_TYPE] ?: "MONTHLY")
    }
    val currency: Flow<Currency> = getSetting { preferences ->
        Currency.valueOf(preferences[SettingKey.CURRENCY] ?: "INR")
    }
    val isOnboardingDone: Flow<Boolean> = getSetting { preferences ->
        preferences[SettingKey.IS_ONBOARDING_DONE] ?: false
    }

    suspend fun setUserFirstName(firstName: String) {
        setSetting(SettingKey.USER_FIST_NAME, firstName)
    }

    suspend fun setUserLastName(lastName: String) {
        setSetting(SettingKey.USER_LAST_NAME, lastName)
    }

    suspend fun setCycleType(cycleType: CycleType) {
        setSetting(SettingKey.CYCLE_TYPE, cycleType.name)
    }

    suspend fun setCurrency(currency: Currency) {
        setSetting(SettingKey.CURRENCY, currency.name)
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