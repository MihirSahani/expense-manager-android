package com.example.core.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.example.core.database.entity.Transaction
import com.example.core.database.projection.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TransactionDAO {
    // -------------------------------- Fetching Transactions --------------------------------
    @Query("SELECT * FROM transactions WHERE id = :id")
    abstract suspend fun getTransaction(id: Int): Transaction?

    @Query("SELECT * FROM transactions WHERE id = :id")
    abstract fun getTransactionFlow(id: Int): Flow<Transaction?>

    @Query("" +
            "SELECT * " +
            "FROM transactions " +
            "WHERE datetime BETWEEN :start AND :end " +
            "ORDER BY datetime DESC"
    )
    abstract fun getTransactionsBetween(
        start: Long=0, end: Long= Long.MAX_VALUE
    ): PagingSource<Int, Transaction>

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

    // ---------------------------------- Creating Transactions ---------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun create(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun create(transactions: List<Transaction>)

    // ----------------------------------- Updating Transactions ----------------------------------
    @Update
    abstract suspend fun update(transaction: Transaction)

    @Query("UPDATE transactions SET category_id = :categoryId WHERE id = :transactionId")
    abstract suspend fun updateTransactionCategory(transactionId: Int, categoryId: Int?)

    @Query("" +
            "UPDATE transactions " +
            "SET category_id = :newCategoryId " +
            "WHERE payee = :payee"
    )
    abstract suspend fun updateTransactionsCategoryByPayee(payee: String, newCategoryId: Int?)

    // ----------------------------------- Deleting Transactions -----------------------------------
    @Query("DELETE FROM transactions WHERE id = :id")
    abstract suspend fun deleteById(id: Int)
}