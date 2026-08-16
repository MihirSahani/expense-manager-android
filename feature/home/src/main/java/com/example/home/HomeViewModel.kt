package com.example.home

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
class HomeViewModel @Inject constructor(repo: AccountRepository): ViewModel() {

    val netWorth: Flow<Long> = repo.accounts
        .map { accounts -> accounts.sumOf { it.balance } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0L
        )
}