package com.example.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingKey {
    val USER_FIST_NAME = stringPreferencesKey("user_first_name") // *Jane / ...
    val USER_LAST_NAME = stringPreferencesKey("user_last_name") // *null / ...

    val CYCLE_TYPE = stringPreferencesKey("cycle_type") // *MONTHLY / SALARY_DATE

    val CURRENCY = stringPreferencesKey("currency") // *INR

    val IS_ONBOARDING_DONE = booleanPreferencesKey("is_onboarding_done") // *false / true

    val SALARY_CREDIT_TIME = longPreferencesKey("salary_credit_time") // *0 / ...

    val SMS_READ_TIME = longPreferencesKey("sms_read_time") // *0 / ...
}