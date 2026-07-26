package com.example.core.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.example.core.database.entity.Account
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AccountDAO {
    // ----------------------------- Fetching Accounts -----------------------------
    @Query("SELECT * FROM accounts WHERE id = :id")
    abstract suspend fun getAccount(id: Int): Account?

    @Query("SELECT * FROM accounts WHERE id = :id")
    abstract fun getAccountFlow(id: Int): Flow<Account?>

    @Query("SELECT * FROM accounts")
    abstract fun getAllAccountsFlow(): Flow<List<Account>>

    @Query("SELECT * FROM accounts")
    abstract fun getAllAccounts(): PagingSource<Int, Account>

    // -------------------------------- Creating Accounts ---------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun create(account: Account)

    // ----------------------------------- Updating Accounts ------------------------
    @Update
    abstract suspend fun update(account: Account)

    // ----------------------------------- Deleting Accounts ------------------------
    @Query("DELETE FROM accounts WHERE id = :id")
    abstract suspend fun deleteById(id: Int)
}