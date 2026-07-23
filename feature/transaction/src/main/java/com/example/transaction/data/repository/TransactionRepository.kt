package com.example.transaction.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.core.database.dao.TransactionDAO
import com.example.core.database.entity.Transaction
import com.example.core.database.projection.TransactionWithCategory
import com.example.datastore.Setting
import com.example.datastore.model.CycleType
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import java.time.YearMonth
import java.time.ZoneId

class TransactionRepository @Inject constructor(val setting: Setting, val dao: TransactionDAO) {
    suspend fun getTransaction(id: Int): Transaction? {
        return dao.getTransaction(id)
    }

    fun getTransactionFlow(id: Int): Flow<Transaction?> {
        return dao.getTransactionFlow(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCurrentCycleTransactions(): Flow<PagingData<TransactionWithCategory>> {
        return setting.cycleType.flatMapLatest { cycleType ->
            when (cycleType) {
                CycleType.MONTHLY -> getCurrentMonthTransactionsWithCategory()
                CycleType.SALARY_DATE -> setting.salaryCreditTime.flatMapLatest { salaryCreditTime ->
                    getCurrentCycleTransactionsWithCategory(salaryCreditTime)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPastCycleTransactionsWithCategory(): Flow<PagingData<TransactionWithCategory>> {
        return setting.cycleType.flatMapLatest { cycleType ->
            when (cycleType) {
                CycleType.MONTHLY -> getPastMonthsTransactionsWithCategory()
                CycleType.SALARY_DATE -> setting.salaryCreditTime.flatMapLatest { salaryCreditTime ->
                    getPastCycleTransactionsWithCategory(salaryCreditTime)
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
    private fun getCurrentMonthTransactionsWithCategory(): Flow<PagingData<TransactionWithCategory>> {
        val start = YearMonth.now()
            .atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond()
        val end = YearMonth.now()
            .atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toEpochSecond()
        return getTransactionsWithCategoryBetween(start, end)
    }

    private fun getPastMonthsTransactionsWithCategory(): Flow<PagingData<TransactionWithCategory>> {
        val end = YearMonth.now()
            .minusMonths(1)
            .atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toEpochSecond()
        return getTransactionsWithCategoryBetween(end = end)
    }

    private fun getCurrentCycleTransactionsWithCategory(salaryCreditTime: Long): Flow<PagingData<TransactionWithCategory>> {
        return getTransactionsWithCategoryBetween(start = salaryCreditTime)
    }

    private fun getPastCycleTransactionsWithCategory(salaryCreditTime: Long): Flow<PagingData<TransactionWithCategory>> {
        return getTransactionsWithCategoryBetween(end = salaryCreditTime - 1)
    }

    private fun getTransactionsWithCategoryBetween(
        start: Long = 0,
        end: Long = Long.MAX_VALUE
    ): Flow<PagingData<TransactionWithCategory>> {
        return Pager(
            config = PagingConfig(15),
            pagingSourceFactory = { dao.getTransactionsWithCategoryBetween(start, end) }
        ).flow
    }
}