package com.example.transaction.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.TransactionRepository
import com.example.core.database.entity.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the single-transaction view/edit screen, identified by the `id` saved-state argument.
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repo: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val transactionId: Int = checkNotNull(savedStateHandle["id"])

    /** The transaction being viewed/edited. */
    val transaction = repo.getTransactionFlow(transactionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** All accounts, for account-picker UI. */
    val accounts = repo.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** All categories, for category-picker UI. */
    val categories = repo.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Persists an updated transaction (including any account reassignment). If the reassignment
     * introduces a raw account/card number that doesn't match the target account's own number,
     * it is remembered as a debit card and backfilled onto other matching unresolved
     * transactions (see [TransactionRepository.reassignAccount]).
     *
     * @param updatedTransaction the transaction with updated field values.
     */
    fun updateTransactionAccount(updatedTransaction: Transaction) {
        viewModelScope.launch {
            repo.reassignAccount(updatedTransaction)
        }
    }

    /**
     * Updates the current transaction's category, optionally remembering the choice for future
     * transactions from the same payee and retroactively applying it to existing ones.
     *
     * @param categoryId the category id to assign.
     * @param updateForAllTransactions when `true`, remembers and retroactively applies the
     * category to every transaction with the same payee and transaction type; when `false`,
     * updates only the current transaction.
     */
    fun updateTransactionCategory(categoryId: Int, updateForAllTransactions: Boolean) {
        viewModelScope.launch {
            val currentTransaction = checkNotNull(transaction.value) {
                "Transaction is null when trying to update category"
            }
            if (updateForAllTransactions) {
                repo.updateAndRememberCategoryForPayee(
                    currentTransaction.payee,
                    currentTransaction.transactionType,
                    categoryId
                )
            } else {
                repo.updateTransactionCategory(currentTransaction.id, categoryId)
            }
        }
    }

    /**
     * Creates a new transaction.
     *
     * @param transaction the transaction to create.
     */
    fun createTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repo.create(transaction)
        }
    }

    /**
     * Deletes a transaction by id.
     *
     * @param id the transaction id to delete.
     */
    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repo.deleteTransaction(id)
        }
    }
}