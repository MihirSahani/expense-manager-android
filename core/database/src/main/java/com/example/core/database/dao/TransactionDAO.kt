package com.example.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.example.core.database.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TransactionDAO {
    // -------------------------------- Fetching Transactions --------------------------------
    @Query("SELECT * FROM transactions WHERE id = :id")
    abstract suspend fun getTransaction(id: Int): Transaction?

    @Query("SELECT * FROM transactions WHERE id = :id")
    abstract suspend fun getTransactionFlow(id: Int): Flow<Transaction?>

    @Query("" +
            "SELECT * " +
            "FROM transactions " +
            "WHERE datetime BETWEEN :start AND :end " +
            "ORDER BY datetime DESC"
    )
    abstract suspend fun getTransactions(start: Long=0, end: Long= Long.MAX_VALUE): List<Transaction>

    // TODO: Add pagination for transactions

    // ---------------------------------- Creating Transactions ---------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun create(transaction: Transaction)

    // ----------------------------------- Updating Transactions ----------------------------------
    @Update
    abstract suspend fun update(transaction: Transaction)

    @Query("UPDATE transactions SET category_id = :categoryId WHERE id = :transactionId")
    abstract suspend fun updateTransactionCategory(transactionId: Int, categoryId: Int)

    @Query("" +
            "UPDATE transactions " +
            "SET category_id :oldCategoryId " +
            "WHERE category_id = :newCategoryId"
    )
    abstract suspend fun updateTransactionsCategory(oldCategoryId: Int, newCategoryId: Int)

    // ----------------------------------- Deleting Transactions -----------------------------------
    @Query("DELETE FROM transactions WHERE id = :id")
    abstract suspend fun deleteById(id: Int)
}