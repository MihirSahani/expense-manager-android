package com.example.transaction.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.common.model.TransactionFilter
import com.example.common.utils.toDateString
import com.example.common.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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

    private val _filter = MutableStateFlow(TransactionFilter())
    /** The currently applied transaction filter. Empty (see [TransactionFilter.isEmpty]) means no filter is active. */
    val filter: StateFlow<TransactionFilter> = _filter.asStateFlow()

    /** All categories, for the filter bottom sheet's category picker. */
    val categories = repo.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Replaces the currently applied filter. */
    fun updateFilter(filter: TransactionFilter) {
        _filter.value = filter
    }

    /** Clears the currently applied filter, restoring the cycle-based (see [showPastCycle]) view. */
    fun clearFilter() {
        _filter.value = TransactionFilter()
    }

    /**
     * Paged transaction list, with date-header separators inserted between days, cached in
     * [viewModelScope]. Shows the selected cycle (see [showPastCycle]) when no filter is active,
     * or every transaction matching [filter] (ignoring cycle boundaries) otherwise.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: Flow<PagingData<TransactionListItem>> = combine(_showPastCycle, _filter) { showPast, filter ->
        showPast to filter
    }
        .flatMapLatest { (showPast, filter) ->
            if (!filter.isEmpty()) {
                repo.getFilteredTransactionsWithCategory(filter)
            } else if (showPast) {
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
