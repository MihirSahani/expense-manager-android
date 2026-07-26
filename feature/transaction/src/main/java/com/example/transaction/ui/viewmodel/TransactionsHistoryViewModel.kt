package com.example.transaction.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.common.utils.toDateString
import com.example.common.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@HiltViewModel
class TransactionsHistoryViewModel @Inject constructor(private val repo: TransactionRepository) : ViewModel() {
    private val _showPastCycle = MutableStateFlow(false)
    val showPastCycle: StateFlow<Boolean> = _showPastCycle.asStateFlow()

    fun toggleCycle() {
        _showPastCycle.value = !_showPastCycle.value
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: Flow<PagingData<TransactionListItem>> = _showPastCycle
        .flatMapLatest { showPast ->
            if (showPast) {
                repo.getPastCycleTransactionsWithCategory()
            } else {
                repo.getCurrentCycleTransactions()
            }
        }
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
