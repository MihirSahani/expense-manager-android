package com.example.account.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val repo: AccountRepository,
) : ViewModel() {
    val accounts = repo.accounts
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val netWorth: Flow<Long> = repo.accounts
        .map { accounts -> accounts.sumOf { it.balance } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0L
        )
}