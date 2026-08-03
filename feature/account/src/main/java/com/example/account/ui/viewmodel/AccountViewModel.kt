package com.example.account.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.AccountRepository
import com.example.core.database.entity.Account
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repo: AccountRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val accountId: Int? = savedStateHandle["id"]

    val account = (accountId?.let { repo.getAccountById(it) } ?: flowOf(blankAccount))
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    fun isNewAccount(): Boolean {
        return accountId == null
    }

    fun createAccount(account: Account) {
        viewModelScope.launch {
            repo.createAccount(account)
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repo.updateAccount(account)
        }
    }

    fun deleteAccount(id: Int) {
        viewModelScope.launch {
            repo.deleteAccount(id)
        }
    }
    companion object {
        private val blankAccount = Account(
            name = "",
            balance = 0,
            type = AccountType.OTHER,
            accountNumber = null,
            color = null,
            icon = AccountIcon.OTHER
        )
    }
}