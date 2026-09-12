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

/**
 * Exposes the transaction history for the current or past cycle as a paged, date-grouped list,
 * for the transaction history screen.
 */
@HiltViewModel
class TransactionsHistoryViewModel @Inject constructor(private val repo: TransactionRepository) : ViewModel() {
    private val _showPastCycle = MutableStateFlow(false)
    /** Whether the history is currently showing the past cycle rather than the current one. */
    val showPastCycle: StateFlow<Boolean> = _showPastCycle.asStateFlow()

    /** Toggles between showing the current cycle and the past cycle. */
    fun toggleCycle() {
        _showPastCycle.value = !_showPastCycle.value
    }

    /**
     * Paged transaction list for the selected cycle (see [showPastCycle]), with date-header
     * separators inserted between days, cached in [viewModelScope].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: Flow<PagingData<TransactionListItem>> = _showPastCycle
        .flatMapLatest { showPast ->
            if (showPast) {
                repo.getPastCycleTransactionsWithCategory()
            } else {
                repo.getCurrentCycleTransactionsWithCategory()
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
