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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** DataStore instance backing all app-wide preferences, scoped to the file "app-setting". */
val Context.settingDataStore: DataStore<Preferences> by preferencesDataStore("app-setting")

/**
 * Wraps [Context.settingDataStore], exposing each preference as a [Flow] with a sensible
 * default and providing setters for updating them. Only [IOException] while reading is converted
 * to an empty preferences set; any other error is rethrown.
 */
@Singleton
class Setting @Inject constructor(@param:ApplicationContext val context: Context) {
    /** The user's first name, defaulting to `"Jane"`. */
    val userFirstName: Flow<String> = getSetting { preferences ->
        preferences[SettingKey.USER_FIST_NAME] ?: "Jane"
    }
    /** The user's last name, or `null` if unset. */
    val userLastName: Flow<String?> = getSetting { preferences ->
        preferences[SettingKey.USER_LAST_NAME]
    }
    /** The user's preferred budget cycle type, defaulting to `MONTHLY`. */
    val cycleType: Flow<CycleType> = getSetting { preferences ->
        CycleType.valueOf(preferences[SettingKey.CYCLE_TYPE] ?: "MONTHLY")
    }
    /** The user's preferred display currency, defaulting to `INR`. */
    val currency: Flow<Currency> = getSetting { preferences ->
        Currency.valueOf(preferences[SettingKey.CURRENCY] ?: "INR")
    }
    /** Whether the user has completed the onboarding flow. */
    val isOnboardingDone: Flow<Boolean> = getSetting { preferences ->
        preferences[SettingKey.IS_ONBOARDING_DONE] ?: false
    }

    /** The epoch-second start of the current salary-date cycle, defaulting to `0`. */
    val salaryCreditTime: Flow<Long> = getSetting { preferences ->
        preferences[SettingKey.SALARY_CREDIT_TIME] ?: 0L
    }

    /** The epoch-second timestamp up to which SMS messages have already been parsed. */
    val smsReadTime: Flow<Long> = getSetting { preferences ->
        preferences[SettingKey.SMS_READ_TIME] ?: 0L
    }

    /** Updates the stored user first name. */
    suspend fun setUserFirstName(firstName: String) {
        setSetting(SettingKey.USER_FIST_NAME, firstName)
    }

    /** Updates the stored user last name, or removes it if [lastName] is `null`. */
    suspend fun setUserLastName(lastName: String?) {
        when (lastName) {
            null -> removeSetting(SettingKey.USER_LAST_NAME)
            else -> setSetting(SettingKey.USER_LAST_NAME, lastName)
        }
    }

    /** Updates the stored budget cycle type. */
    suspend fun setCycleType(cycleType: CycleType) {
        setSetting(SettingKey.CYCLE_TYPE, cycleType.name)
    }

    /** Updates the stored display currency. */
    suspend fun setCurrency(currency: Currency) {
        setSetting(SettingKey.CURRENCY, currency.name)
    }

    /** Updates whether onboarding has been completed. */
    suspend fun setOnboardingDone(isOnboardingDone: Boolean) {
        setSetting(SettingKey.IS_ONBOARDING_DONE, isOnboardingDone)
    }

    /** Updates the stored salary-date cycle start time. */
    suspend fun setSalaryCreditTime(salaryCreditTime: Long) {
        setSetting(SettingKey.SALARY_CREDIT_TIME, salaryCreditTime)
    }

    /** Updates the stored SMS read watermark. */
    suspend fun setSmsReadTime(smsReadTime: Long) {
        setSetting(SettingKey.SMS_READ_TIME, smsReadTime)
    }

    /**
     * Observes preferences and maps them through [transform], recovering from [IOException] by
     * substituting an empty preferences set.
     *
     * @param transform extracts a typed value from the raw [Preferences].
     */
    private fun <T> getSetting(transform: (Preferences) -> T): Flow<T> =
        context.settingDataStore.data
            .catch { throwable ->
                if (throwable is IOException) emit(emptyPreferences()) else throw throwable
            }
            .map { preferences: Preferences ->
                transform(preferences)
            }

    /**
     * Writes a single preference value.
     *
     * @param key the preference key.
     * @param value the value to store.
     */
    private suspend fun <T> setSetting(key: Preferences.Key<T>, value: T) {
        context.settingDataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    /**
     * Removes a single preference value.
     *
     * @param key the preference key to remove.
     */
    private suspend fun <T> removeSetting(key: Preferences.Key<T>) {
        context.settingDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}