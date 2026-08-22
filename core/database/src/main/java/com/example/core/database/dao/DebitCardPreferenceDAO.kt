package com.example.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.core.database.entity.DebitCardPreference

/**
 * Data Access Object for the `debit_card_preferences` table, which maps a debit card identifier
 * to the account it should be attributed to.
 */
@Dao
abstract class DebitCardPreferenceDAO {
    /**
     * Fetches every stored debit-card preference.
     *
     * @return the current list of all preferences.
     */
    @Query("SELECT * FROM debit_card_preferences")
    abstract suspend fun getAllPreferences(): List<DebitCardPreference>

    /**
     * Inserts a preference, replacing any existing row for the same card number.
     *
     * @param preference the preference to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(preference: DebitCardPreference)

    /**
     * Deletes the preference for a given card number.
     *
     * @param cardNumber the card identifier whose preference should be removed.
     * @return the number of rows deleted (0 or 1).
     */
    @Query("DELETE FROM debit_card_preferences WHERE card_number = :cardNumber")
    abstract suspend fun delete(cardNumber: String): Int
}
