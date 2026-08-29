package com.example.common.repository

import com.example.core.database.dao.AccountDAO
import com.example.core.database.entity.Account
import com.example.core.database.models.normalizeAccountIdentifier
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

/** Thrown when an account fails validation (e.g. a duplicate or malformed account number). */
class AccountValidationException(message: String) : IllegalArgumentException(message)

/**
 * Mediates access to [Account] data, normalizing and validating account numbers before they
 * reach the database.
 */
class AccountRepository @Inject constructor(val dao: AccountDAO) {
    /** Observes every account. */
    val accounts = dao.getAllAccountsFlow()

    /**
     * Observes a single account by id.
     *
     * @param id the account id.
     * @return a [Flow] emitting the matching account, or `null` if it doesn't exist.
     */
    fun getAccountByIdFlow(id: Int): Flow<Account?> = dao.getAccountByIdFlow(id)

    /**
     * Fetches a single account by id.
     *
     * @param id the account id.
     * @return the matching account, or `null` if it doesn't exist.
     */
    suspend fun getAccountById(id: Int): Account? = dao.getAccountById(id)

    /** Fetches every account currently stored. */
    suspend fun getAllAccounts(): List<Account> = dao.getAllAccounts()

    /**
     * Creates a new account after normalizing and validating its account number.
     *
     * @param account the account to create.
     * @return the id of the newly created account.
     * @throws AccountValidationException if the account number is malformed or already used by
     * another account.
     */
    suspend fun createAccount(account: Account): Int {
        val insertedId = dao.create(account.normalizedAndValidated())

        require(insertedId != -1L) {
            throw AccountValidationException(
                "Account number is already used by another account"
            )
        }
        return insertedId.toInt()
    }

    /**
     * Updates an existing account.
     *
     * @param account the account with updated field values.
     * @param updateBalance when `true` (the default), persists `account.balance` as-is. When
     * `false`, keeps whatever balance is currently persisted instead — used for metadata-only
     * edits so a stale in-memory balance can't clobber a concurrent [adjustBalance] call.
     * @throws AccountValidationException if the account number is malformed or already used by
     * another account.
     */
    suspend fun updateAccount(account: Account, updateBalance: Boolean = true) {
        val normalizedAccount = account.normalizedAndValidated()
        // When metadata is edited without touching the balance field, keep whatever
        // balance is currently persisted instead of overwriting it with a possibly
        // stale value carried by the in-memory `account` (e.g. from a concurrent
        // adjustBalance() call while the edit screen was open).
        val accountToPersist = if (updateBalance) {
            normalizedAccount
        } else {
            val currentBalance = dao.getAccountById(account.id)?.balance ?: account.balance
            normalizedAccount.copy(balance = currentBalance)
        }
        val updatedRows = dao.update(accountToPersist)

        require(updatedRows == 1) {
            throw AccountValidationException(
                "Account number is already used by another account"
            )
        }
    }

    /**
     * Deletes an account by id.
     *
     * @param id the account id to delete.
     */
    suspend fun deleteAccount(id: Int) = dao.deleteById(id)

    /**
     * Adjusts an account's balance by a relative amount, applied atomically at the database
     * level.
     *
     * @param accountId the id of the account to adjust.
     * @param delta the signed amount (in the smallest currency unit) to add to the current
     * balance.
     * @throws AccountValidationException if no account with [accountId] exists.
     */
    suspend fun adjustBalance(accountId: Int, delta: Long) {
        val updatedRows = dao.adjustBalance(accountId, delta)

        require(updatedRows == 1) {
            throw AccountValidationException("Account $accountId does not exist")
        }
    }

    /**
     * Normalizes this account's [Account.accountNumber] (stripping non-digits while preserving
     * leading zeroes) and validates that it is either blank or contains at least one digit.
     *
     * @return a copy of this account with the normalized account number.
     * @throws AccountValidationException if the account number is non-blank but contains no
     * digits.
     */
    private fun Account.normalizedAndValidated(): Account {
        val normalizedNumber = accountNumber.normalizeAccountIdentifier()

        require(accountNumber.isNullOrBlank() || normalizedNumber != null) {
            throw AccountValidationException("Account number must contain at least one digit")
        }

        return copy(accountNumber = normalizedNumber)
    }
}