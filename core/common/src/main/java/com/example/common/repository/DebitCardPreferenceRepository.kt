package com.example.common.repository

import androidx.room3.withWriteTransaction
import com.example.core.database.AppDatabase
import com.example.core.database.dao.DebitCardPreferenceDAO
import com.example.core.database.entity.DebitCardPreference
import com.example.core.database.models.normalizeAccountIdentifier
import jakarta.inject.Inject

/** Thrown when a debit-card preference fails validation. */
class DebitCardPreferenceValidationException(message: String) : IllegalArgumentException(message)

/**
 * Mediates access to [DebitCardPreference] data, which maps a debit card identifier to the
 * account it should be attributed to.
 */
class DebitCardPreferenceRepository @Inject constructor(
    private val dao: DebitCardPreferenceDAO,
    private val accountRepository: AccountRepository,
    private val database: AppDatabase
) {
    /** Fetches every stored debit-card preference. */
    suspend fun getAllPreferences(): List<DebitCardPreference> = dao.getAllPreferences()

    /**
     * Normalizes [cardNumber] and saves a preference mapping it to [accountId], atomically
     * validating that the account exists and has an account number.
     *
     * @param cardNumber the debit card identifier to normalize and store.
     * @param accountId the account the card should be attributed to.
     * @return the saved preference.
     * @throws DebitCardPreferenceValidationException if the card number contains no digits, the
     * account does not exist, or the account has no account number.
     */
    suspend fun save(cardNumber: String, accountId: Int): DebitCardPreference {
        val normalizedCardNumber = cardNumber.normalizeAccountIdentifier()
            ?: throw DebitCardPreferenceValidationException(
                "Debit card number must contain at least one digit"
            )
        val preference = DebitCardPreference(normalizedCardNumber, accountId)

        database.withWriteTransaction {
            // Validate that the account exists and has an account number
            val account = accountRepository.getAccountById(accountId)
                ?: throw DebitCardPreferenceValidationException("Account does not exist")

            require (account.accountNumber != null) {
                throw DebitCardPreferenceValidationException(
                    "Debit card preferences require an account number"
                )
            }
            dao.insert(preference)
        }
        return preference
    }

    /**
     * Normalizes [cardNumber] and deletes its preference, if any.
     *
     * @param cardNumber the debit card identifier to normalize and delete.
     * @return `true` if a preference was deleted, `false` if none existed.
     * @throws DebitCardPreferenceValidationException if the card number contains no digits.
     */
    suspend fun delete(cardNumber: String): Boolean {
        val normalizedCardNumber = cardNumber.normalizeAccountIdentifier()
            ?: throw DebitCardPreferenceValidationException(
                "Debit card number must contain at least one digit"
            )
        return dao.delete(normalizedCardNumber) == 1
    }
}
