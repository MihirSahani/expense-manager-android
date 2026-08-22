package com.example.core.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.example.core.database.entity.Account
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `accounts` table.
 */
@Dao
abstract class AccountDAO {
    // ----------------------------- Fetching Accounts -----------------------------
    /**
     * Fetches a single account by its primary key.
     *
     * @param id the account id.
     * @return the matching [Account], or `null` if none exists.
     */
    @Query("SELECT * FROM accounts WHERE id = :id")
    abstract suspend fun getAccountById(id: Int): Account?

    /**
     * Observes a single account by its primary key.
     *
     * @param id the account id.
     * @return a [Flow] emitting the matching [Account] (or `null`) whenever it changes.
     */
    @Query("SELECT * FROM accounts WHERE id = :id")
    abstract fun getAccountByIdFlow(id: Int): Flow<Account?>

    /**
     * Observes every account in the table.
     *
     * @return a [Flow] emitting the full account list whenever it changes.
     */
    @Query("SELECT * FROM accounts")
    abstract fun getAllAccountsFlow(): Flow<List<Account>>

    /**
     * Fetches every account in the table.
     *
     * @return the current list of all accounts.
     */
    @Query("SELECT * FROM accounts")
    abstract suspend fun getAllAccounts(): List<Account>

    /**
     * Fetches every account in the table as a paged source, for use with Paging 3.
     *
     * @return a [PagingSource] over all accounts.
     */
    @Query("SELECT * FROM accounts")
    abstract fun getAllAccountsPaged(): PagingSource<Int, Account>

    // -------------------------------- Creating Accounts ---------------------------
    /**
     * Inserts a new account, ignoring the insert if a conflict occurs.
     *
     * @param account the account to insert.
     * @return the row id of the inserted account, or `-1` if the insert was ignored.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun create(account: Account): Long

    // ----------------------------------- Updating Accounts ------------------------
    /**
     * Updates an existing account, ignoring the update if a conflict occurs.
     *
     * @param account the account with updated field values.
     * @return the number of rows updated (0 or 1).
     */
    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun update(account: Account): Int

    /**
     * Adjusts an account's balance by a relative amount, applied atomically at the database level.
     *
     * @param accountId the id of the account to adjust.
     * @param delta the signed amount (in the smallest currency unit) to add to the current balance.
     * @return the number of rows updated (0 or 1).
     */
    @Query("UPDATE accounts SET balance = balance + :delta WHERE id = :accountId")
    abstract suspend fun adjustBalance(accountId: Int, delta: Long): Int

    // ----------------------------------- Deleting Accounts ------------------------
    /**
     * Deletes an account by its primary key.
     *
     * @param id the account id to delete.
     */
    @Query("DELETE FROM accounts WHERE id = :id")
    abstract suspend fun deleteById(id: Int)
}