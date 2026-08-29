package com.example.common.repository

import com.example.core.database.dao.PayeeCategoryPreferenceDAO
import com.example.core.database.entity.PayeeCategoryPreference
import com.example.core.database.models.TransactionType
import com.example.core.database.models.normalizePayee
import jakarta.inject.Inject

/**
 * Mediates access to [PayeeCategoryPreference] data, which remembers the last category chosen
 * for a given payee/transaction-type combination and normalizes payees before persisting them.
 */
class PayeeCategoryPreferenceRepository @Inject constructor(
    private val dao: PayeeCategoryPreferenceDAO
) {
    /**
     * Looks up the remembered category for a payee and transaction type.
     *
     * @param payee the payee name (not required to be pre-normalized).
     * @param transactionType the [TransactionType] the preference was recorded for.
     * @return the remembered category id, or `null` if no preference exists.
     */
    suspend fun getCategoryId(payee: String, transactionType: TransactionType): Int? =
        dao.getCategoryId(payee, transactionType)

    /** Fetches every stored payee/category preference. */
    suspend fun getAllPreferences(): List<PayeeCategoryPreference> = dao.getAllPreferences()

    /**
     * Normalizes [payee] and inserts/replaces the preference mapping it (with [transactionType])
     * to [categoryId].
     *
     * @param payee the payee name to normalize and store.
     * @param transactionType the [TransactionType] the preference applies to.
     * @param categoryId the category id to remember.
     * @return the saved preference.
     */
    suspend fun insert(
        payee: String,
        transactionType: TransactionType,
        categoryId: Int
    ): PayeeCategoryPreference {
        val preference = PayeeCategoryPreference(
            payee = payee.normalizePayee(),
            transactionType = transactionType,
            categoryId = categoryId
        )
        dao.insert(preference)
        return preference
    }

    /**
     * Deletes the preference for a payee and transaction type.
     *
     * @param payee the payee name (not required to be pre-normalized).
     * @param transactionType the [TransactionType] the preference was recorded for.
     */
    suspend fun delete(payee: String, transactionType: TransactionType) {
        dao.delete(payee, transactionType)
    }
}
