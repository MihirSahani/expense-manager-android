package com.example.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.AccountRepository
import com.example.common.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Exposes aggregate net worth and salary-cycle refresh for the home screen. */
@HiltViewModel
class HomeViewModel @Inject constructor(
    repo: AccountRepository,
    private val transactionRepo: TransactionRepository
): ViewModel() {

    /** Sum of every account's balance. */
    val netWorth: Flow<Long> = repo.accounts
        .map { accounts -> accounts.sumOf { it.balance } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0L
        )

    /** Refreshes the persisted salary credit time from the latest income transaction. */
    fun refreshSalaryCreditTime() {
        viewModelScope.launch {
            transactionRepo.updateSalaryCreditTime()
        }
    }
}