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
    private val transactionId: Int? = savedStateHandle["id"]

    val transaction = (transactionId?.let { repo.getTransactionFlow(transactionId) } ?: flowOf(blankTransaction))
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

    fun updateTransactionCategory(updatedTransaction: Transaction, updateForAllTransactions: Boolean) {
        viewModelScope.launch {
            if (updateForAllTransactions) {
                repo.updateTransactionsCategory(updatedTransaction.categoryId, updatedTransaction.categoryId)
            } else {
                repo.updateTransaction(updatedTransaction)
            }
        }

    }

    fun createTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repo.createTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repo.deleteTransaction(id)
        }
    }

    companion object {
        val blankTransaction = Transaction(
            amount = 0L,
            categoryId = null,
            accountId = null,
            datetime = System.currentTimeMillis(),
            rawAccountNo = "",
            payee = "",
            transactionType = TransactionType.DEBIT,
            referenceId = null,
            description = null
        )
    }
}