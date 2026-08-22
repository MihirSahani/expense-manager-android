package com.example.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.core.database.entity.PayeeCategoryPreference
import com.example.core.database.models.TransactionType

/**
 * Data Access Object for the `payee_category_preferences` table, which remembers the last
 * category chosen for a given payee/transaction-type combination.
 */
@Dao
abstract class PayeeCategoryPreferenceDAO {
    /**
     * Looks up the remembered category for a payee and transaction type.
     *
     * @param payee the normalized payee name.
     * @param transactionType the [TransactionType] the preference was recorded for.
     * @return the remembered category id, or `null` if no preference exists.
     */
    @Query(
        "SELECT category_id FROM payee_category_preferences " +
            "WHERE payee = :payee AND transaction_type = :transactionType"
    )
    abstract suspend fun getCategoryId(
        payee: String,
        transactionType: TransactionType
    ): Int?

    /**
     * Fetches every stored payee/category preference.
     *
     * @return the current list of all preferences.
     */
    @Query("SELECT * FROM payee_category_preferences")
    abstract suspend fun getAllPreferences(): List<PayeeCategoryPreference>

    /**
     * Inserts a preference, replacing any existing row for the same payee and transaction type.
     *
     * @param preference the preference to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(preference: PayeeCategoryPreference)

    /**
     * Deletes the preference for a payee and transaction type.
     *
     * @param payee the normalized payee name.
     * @param transactionType the [TransactionType] the preference was recorded for.
     */
    @Query(
        "DELETE FROM payee_category_preferences " +
            "WHERE payee = :payee AND transaction_type = :transactionType"
    )
    abstract suspend fun delete(
        payee: String,
        transactionType: TransactionType
    )
}
