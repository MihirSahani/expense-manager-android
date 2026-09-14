package com.example.core.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.example.core.database.entity.Transaction
import com.example.core.database.models.TransactionType
import com.example.core.database.projection.DailyExpense
import com.example.core.database.projection.PayeeSummary
import com.example.core.database.projection.TransactionWithCategory
import com.example.core.database.projection.UnresolvedAccountSummary
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `transactions` table.
 */
@Dao
abstract class TransactionDAO {
    // -------------------------------- Fetching Transactions --------------------------------
    /**
     * Fetches a single transaction by its primary key.
     *
     * @param id the transaction id.
     * @return the matching [Transaction], or `null` if none exists.
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    abstract suspend fun getTransaction(id: Int): Transaction?

    /**
     * Observes a single transaction by its primary key.
     *
     * @param id the transaction id.
     * @return a [Flow] emitting the matching [Transaction] (or `null`) whenever it changes.
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    abstract fun getTransactionFlow(id: Int): Flow<Transaction?>

    /**
     * Fetches transactions within a datetime window, newest first, as a paged source.
     *
     * @param start inclusive start of the window, in epoch seconds. Defaults to the beginning of
     * time.
     * @param end inclusive end of the window, in epoch seconds. Defaults to the end of time.
     * @return a [PagingSource] over the matching transactions.
     */
    @Query("" +
            "SELECT * " +
            "FROM transactions " +
            "WHERE datetime BETWEEN :start AND :end " +
            "ORDER BY datetime DESC"
    )
    abstract fun getTransactionsBetween(
        start: Long=0, end: Long= Long.MAX_VALUE
    ): PagingSource<Int, Transaction>

    /**
     * Fetches transactions with their joined category info within a datetime window, newest
     * first, as a paged source. Used to render transaction history rows.
     *
     * @param start inclusive start of the window, in epoch seconds. Defaults to the beginning of
     * time.
     * @param end inclusive end of the window, in epoch seconds. Defaults to the end of time.
     * @return a [PagingSource] over the matching [TransactionWithCategory] rows.
     */
    @Query("" +
            "SELECT " +
                "t.id, " +
                "t.payee, " +
                "t.amount, " +
                "t.transaction_type AS transactionType, " +
                "t.datetime, " +
                "c.icon AS categoryIcon, " +
                "c.name AS categoryName, " +
                "c.color AS categoryColor " +
            "FROM " +
                "transactions t " +
            "LEFT JOIN " +
                "categories c " +
            "ON " +
                "t.category_id = c.id " +
            "WHERE " +
                "t.datetime BETWEEN :start AND :end " +
            "ORDER BY " +
                "t.datetime DESC"
    )
    abstract fun getTransactionsWithCategoryBetween(
        start: Long=0, end: Long= Long.MAX_VALUE
    ): PagingSource<Int, TransactionWithCategory>

    /**
     * Fetches transactions with their joined category info matching the transaction-history
     * filter criteria, newest first, as a paged source. The category restriction (if any) matches
     * transactions in [categoryIds] and/or, when [includeUncategorized] is `true`, transactions
     * with no assigned category; it is skipped entirely when [categoryCount] is 0 and
     * [includeUncategorized] is `false`.
     *
     * @param start inclusive lower bound on datetime, in epoch seconds.
     * @param end inclusive upper bound on datetime, in epoch seconds.
     * @param minAmount inclusive lower bound on amount, or `null` for no lower bound.
     * @param maxAmount inclusive upper bound on amount, or `null` for no upper bound.
     * @param payeeQuery a lowercased substring to match against payee names, or `null` for no
     * payee restriction.
     * @param categoryIds category ids to restrict results to.
     * @param categoryCount the size of [categoryIds]; 0 (together with [includeUncategorized]
     * being `false`) disables the category restriction.
     * @param includeUncategorized whether transactions with no assigned category should also
     * match.
     * @return a [PagingSource] over the matching [TransactionWithCategory] rows.
     */
    @Query("" +
            "SELECT " +
                "t.id, " +
                "t.payee, " +
                "t.amount, " +
                "t.transaction_type AS transactionType, " +
                "t.datetime, " +
                "c.icon AS categoryIcon, " +
                "c.name AS categoryName, " +
                "c.color AS categoryColor " +
            "FROM " +
                "transactions t " +
            "LEFT JOIN " +
                "categories c " +
            "ON " +
                "t.category_id = c.id " +
            "WHERE " +
                "t.datetime BETWEEN :start AND :end " +
                "AND (:minAmount IS NULL OR t.amount >= :minAmount) " +
                "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) " +
                "AND (:payeeQuery IS NULL OR t.payee LIKE '%' || :payeeQuery || '%') " +
                "AND (" +
                    "(:categoryCount = 0 AND :includeUncategorized = 0) " +
                    "OR t.category_id IN (:categoryIds) " +
                    "OR (:includeUncategorized = 1 AND t.category_id IS NULL)" +
                ") " +
            "ORDER BY " +
                "t.datetime DESC"
    )
    abstract fun getFilteredTransactionsWithCategory(
        start: Long,
        end: Long,
        minAmount: Long?,
        maxAmount: Long?,
        payeeQuery: String?,
        categoryIds: Set<Int>,
        categoryCount: Int,
        includeUncategorized: Boolean
    ): PagingSource<Int, TransactionWithCategory>


    /**
     * Fetches the datetime of the most recent transaction against an `INCOME` category. Used to
     * detect the most recent salary-date cycle boundary.
     *
     * @return the datetime in epoch seconds, or `null` if no income transaction exists.
     */
    @Query("""
        SELECT t.datetime 
        FROM transactions t 
        JOIN categories c ON t.category_id = c.id 
        WHERE c.type = 'INCOME' 
        ORDER BY t.datetime DESC 
        LIMIT 1
    """)
    abstract suspend fun getLatestIncomeTransactionTime(): Long?

    /**
     * Fetches the datetime of the second most recent transaction against an `INCOME` category.
     * Used together with [getLatestIncomeTransactionTime] to derive the length of the current
     * salary-date cycle.
     *
     * @return the datetime in epoch seconds, or `null` if fewer than two income transactions
     * exist.
     */
    @Query("""
        SELECT t.datetime 
        FROM transactions t 
        JOIN categories c ON t.category_id = c.id 
        WHERE c.type = 'INCOME' 
        ORDER BY t.datetime DESC 
        LIMIT 1 
        OFFSET 1
    """)
    abstract suspend fun getSecondLatestIncomeTransactionTime(): Long?

    /**
     * Observes total expense spend grouped by local epoch day within a datetime window. Debit
     * amounts add to the day's spend and credit amounts (refunds) subtract from it.
     *
     * @param start inclusive start of the window, in epoch seconds.
     * @param end inclusive end of the window, in epoch seconds.
     * @param offset the local time-zone offset (in seconds) applied before bucketing into days.
     * @return a [Flow] emitting the list of [DailyExpense] rows whenever the underlying data
     * changes.
     */
    @Query("" +
            "SELECT " +
                "((t.datetime + :offset) / 86400) AS epoch_day, " +
                "SUM(CASE WHEN t.transaction_type = 'DEBIT' THEN t.amount ELSE -t.amount END) AS spent " +
            "FROM " +
                "transactions t " +
            "JOIN " +
                "categories c " +
            "ON " +
                "t.category_id = c.id " +
            "WHERE " +
                "c.type = 'EXPENSE' AND t.datetime BETWEEN :start AND :end " +
            "GROUP BY " +
                "epoch_day " +
            "ORDER BY " +
                "epoch_day"
    )
    abstract fun getDailyExpensesBetween(
        start: Long, end: Long, offset: Long
    ): Flow<List<DailyExpense>>

    /**
     * Observes every distinct raw account/card number seen on transactions that has not yet been
     * linked to an account, along with how many transactions carry it. Used to drive
     * account-discovery/debit-card-tagging UI.
     *
     * @return a [Flow] emitting the list of [UnresolvedAccountSummary] rows whenever the
     * underlying data changes.
     */
    @Query(
        "SELECT " +
            "raw_account_no, " +
            "COUNT(*) AS transaction_count " +
            "FROM transactions " +
            "WHERE account_id IS NULL AND raw_account_no IS NOT NULL " +
            "GROUP BY raw_account_no"
    )
    abstract fun getUnresolvedRawAccountNumbers(): Flow<List<UnresolvedAccountSummary>>

    /**
     * Observes every distinct payee/transaction-type pair with no assigned category within a
     * datetime window, along with its transaction count, total amount, and most recent
     * transaction time. Used to drive payee-category-discovery UI: a payee drops out of the
     * results as soon as it is assigned a category (e.g. via
     * [updateTransactionsCategory]/`updateAndRememberCategoryForPayee`).
     *
     * @param start inclusive start of the window, in epoch seconds.
     * @param end inclusive end of the window, in epoch seconds.
     * @return a [Flow] emitting the list of [PayeeSummary] rows whenever the underlying data
     * changes.
     */
    @Query(
        "SELECT " +
            "payee, " +
            "transaction_type, " +
            "COUNT(*) AS transaction_count, " +
            "SUM(amount) AS total_amount, " +
            "MAX(datetime) AS last_transaction_time " +
            "FROM transactions " +
            "WHERE category_id IS NULL AND datetime BETWEEN :start AND :end " +
            "GROUP BY payee, transaction_type"
    )
    abstract fun getPayeesBetween(start: Long, end: Long): Flow<List<PayeeSummary>>

    // ---------------------------------- Creating Transactions ---------------------------------
    /**
     * Inserts a new transaction, aborting on conflict.
     *
     * @param transaction the transaction to insert.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun create(transaction: Transaction)

    /**
     * Inserts multiple new transactions in a single call, aborting on conflict.
     *
     * @param transactions the transactions to insert.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun create(transactions: List<Transaction>)

    // ----------------------------------- Updating Transactions ----------------------------------
    /**
     * Updates an existing transaction, aborting on conflict.
     *
     * @param transaction the transaction with updated field values.
     * @return the number of rows updated (0 or 1).
     */
    @Update(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun update(transaction: Transaction): Int

    /**
     * Updates only the category of a single transaction.
     *
     * @param transactionId the transaction id.
     * @param categoryId the new category id, or `null` to clear it.
     */
    @Query("UPDATE transactions SET category_id = :categoryId WHERE id = :transactionId")
    abstract suspend fun updateTransactionCategory(transactionId: Int, categoryId: Int?)

    /**
     * Updates the category of every transaction matching a payee and transaction type. Used to
     * retroactively apply a remembered category preference.
     *
     * @param payee the normalized payee name to match.
     * @param transactionType the [TransactionType] to match.
     * @param categoryId the category id to assign.
     */
    @Query(
        "UPDATE transactions SET category_id = :categoryId " +
            "WHERE payee = :payee AND transaction_type = :transactionType"
    )
    abstract suspend fun updateTransactionsCategory(
        payee: String,
        transactionType: TransactionType,
        categoryId: Int
    )

    /**
     * Fetches every transaction with a given raw account/card number that has not yet been
     * linked to an [com.example.core.database.entity.Account]. Used to recompute balance deltas
     * before bulk-linking them via [linkRawAccountNoToAccount].
     *
     * @param rawAccountNo the normalized raw account/card number to match.
     * @return the matching unresolved transactions.
     */
    @Query("SELECT * FROM transactions WHERE raw_account_no = :rawAccountNo AND account_id IS NULL")
    abstract suspend fun getUnresolvedTransactionsForRawAccountNo(rawAccountNo: String): List<Transaction>

    /**
     * Links every unresolved transaction with a given raw account/card number to an account.
     * Transactions that already have an `account_id` are left untouched.
     *
     * @param rawAccountNo the normalized raw account/card number to match.
     * @param accountId the account id to assign.
     * @return the number of rows updated.
     */
    @Query("UPDATE transactions SET account_id = :accountId WHERE raw_account_no = :rawAccountNo AND account_id IS NULL")
    abstract suspend fun linkRawAccountNoToAccount(rawAccountNo: String, accountId: Int): Int

    // ----------------------------------- Deleting Transactions -----------------------------------
    /**
     * Deletes a transaction by its primary key.
     *
     * @param id the transaction id to delete.
     * @return the number of rows deleted (0 or 1).
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    abstract suspend fun deleteById(id: Int): Int
}