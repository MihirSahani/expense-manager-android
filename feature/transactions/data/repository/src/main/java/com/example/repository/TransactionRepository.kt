package com.example.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.core.database.dao.TransactionDAO
import com.example.core.database.entity.Transaction
import com.example.datastore.Setting
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import java.time.ZoneId
import com.example.datastore.model.CycleType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest


class TransactionRepository(val setting: Setting, val dao: TransactionDAO) {
    suspend fun getTransaction(id: Int): Transaction? {
        return dao.getTransaction(id)
    }

    fun getTransactionFlow(id: Int): Flow<Transaction?> {
        return dao.getTransactionFlow(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCurrentCycleTransactions(): Flow<PagingData<Transaction>> {
        return setting.cycleType.flatMapLatest { cycleType ->
            when (cycleType) {
                CycleType.MONTHLY -> getCurrentMonthTransactions()
                CycleType.SALARY_DATE -> setting.salaryCreditTime.flatMapLatest { salaryCreditTime ->
                    getCurrentCycleTransactions(salaryCreditTime)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPastCycleTransactions(): Flow<PagingData<Transaction>> {
        return setting.cycleType.flatMapLatest { cycleType ->
            when (cycleType) {
                CycleType.MONTHLY -> getPastMonthsTransactions()
                CycleType.SALARY_DATE -> setting.salaryCreditTime.flatMapLatest { salaryCreditTime ->
                    getPastCycleTransactions(salaryCreditTime)
                }
            }
        }
    }

    suspend fun createTransaction(transaction: Transaction) {
        dao.create(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        dao.update(transaction)
    }

    suspend fun updateTransactionCategory(transactionId: Int, categoryId: Int) {
        dao.updateTransactionCategory(transactionId, categoryId)
    }

    suspend fun updateTransactionsCategory(oldCategoryId: Int, newCategoryId: Int) {
        dao.updateTransactionsCategory(oldCategoryId, newCategoryId)
    }

    suspend fun deleteTransaction(id: Int) {
        dao.deleteById(id)
    }
    private fun getCurrentMonthTransactions(): Flow<PagingData<Transaction>> {
        val start = YearMonth.now()
            .atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond()
        val end = YearMonth.now()
            .atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toEpochSecond()
        return getTransactionsBetween(start, end)
    }

    private fun getPastMonthsTransactions(): Flow<PagingData<Transaction>> {
        val end = YearMonth.now()
            .minusMonths(1)
            .atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toEpochSecond()
        return getTransactionsBetween(end)
    }

    private fun getCurrentCycleTransactions(salaryCreditTime: Long): Flow<PagingData<Transaction>> {
        val start = salaryCreditTime
        return getTransactionsBetween(start)
    }

    private fun getPastCycleTransactions(salaryCreditTime: Long): Flow<PagingData<Transaction>> {
        val end = salaryCreditTime - 1
        return getTransactionsBetween(end)
    }

    private fun getTransactionsBetween(
        start: Long = 0,
        end: Long = Long.MAX_VALUE
    ): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(15),
            pagingSourceFactory = { dao.getTransactionsBetween(start, end) }
        ).flow
    }
}