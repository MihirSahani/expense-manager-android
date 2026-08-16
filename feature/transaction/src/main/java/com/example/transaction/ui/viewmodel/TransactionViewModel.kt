package com.example.transaction.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.TransactionRepository
import com.example.core.database.entity.Transaction
import com.example.core.database.models.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repo: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val transactionId: Int = checkNotNull(savedStateHandle["id"])

    val transaction = repo.getTransactionFlow(transactionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val accounts = repo.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = repo.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateTransactionAccount(updatedTransaction: Transaction) {
        viewModelScope.launch {
            repo.updateTransaction(updatedTransaction)
        }
    }

    fun updateTransactionCategory(categoryId: Int?, updateForAllTransactions: Boolean) {
        viewModelScope.launch {
            if (transaction.value == null) throw IllegalStateException("Transaction is null when trying to update category")
            if (updateForAllTransactions) {
                repo.updateTransactionsCategoryByPayee(transaction.value!!.payee, categoryId)
            } else {
                repo.updateTransactionCategory(transaction.value!!.id, categoryId)
            }
        }

    }

    fun createTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repo.create(transaction)
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repo.deleteTransaction(id)
        }
    }
}