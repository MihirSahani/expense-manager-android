package com.example.transaction.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.common.utils.toDateString
import com.example.transaction.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@HiltViewModel
class TransactionViewModel @Inject constructor(repo: TransactionRepository) : ViewModel() {
    val transactions: Flow<PagingData<TransactionListItem>> = repo.getCurrentCycleTransactions()
        .map { pagingData ->
            pagingData
                .map { transactionWithCategory ->
                    TransactionListItem.TransactionItem(transactionWithCategory)
                }
                .insertSeparators { before, after ->
                    val beforeLabel = before?.transactionWithCategory?.datetime?.toDateString()
                    val afterLabel = after?.transactionWithCategory?.datetime?.toDateString()

                    when {
                        afterLabel != null && beforeLabel != afterLabel -> {
                            TransactionListItem.DateHeader(afterLabel)
                        }
                        else -> {
                            null
                        }
                    }
                }
        }.cachedIn(viewModelScope)
}
