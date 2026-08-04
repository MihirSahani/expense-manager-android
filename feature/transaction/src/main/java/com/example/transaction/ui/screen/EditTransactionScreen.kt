package com.example.transaction.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.ui.component.ScreenScaffold
import com.example.transaction.ui.components.TransactionDetails
import com.example.transaction.ui.viewmodel.TransactionViewModel

@Composable
fun TransactionScreen(viewModel: TransactionViewModel = hiltViewModel()) {
    val transaction by viewModel.transaction.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    ScreenScaffold(
        "Transaction Details",
        {
            IconButton(onClick = { transaction?.let { viewModel.deleteTransaction(it.id) } }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Transaction")
            }
        },
        isLoading = transaction == null
    ) { padding ->
        TransactionDetails(
            transaction = transaction,
            accounts = accounts,
            categories = categories,
            onAccountUpdate = { updatedTransaction ->
                viewModel.updateTransactionAccount(updatedTransaction)
            },
            onCategoryUpdate = { categoryId, updateForAllTransactions ->
                viewModel.updateTransactionCategory(categoryId, updateForAllTransactions)
            },
            padding = padding
        )
    }
}