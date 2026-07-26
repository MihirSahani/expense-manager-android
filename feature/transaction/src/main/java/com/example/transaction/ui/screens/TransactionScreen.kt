package com.example.transaction.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.core.database.entity.Account
import com.example.core.database.entity.Category
import com.example.core.database.entity.Transaction
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import com.example.core.database.models.TransactionType
import com.example.transaction.ui.components.TransactionDetails
import com.example.transaction.ui.viewmodel.TransactionViewModel
import java.time.Instant

@Composable
fun TransactionScreen(viewModel: TransactionViewModel = hiltViewModel()) {
    val transaction by viewModel.transaction.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    TransactionScreen(
        transaction = transaction,
        accounts = accounts,
        categories = categories,
        onAccountUpdate = { updatedTransaction ->
            viewModel.updateTransactionAccount(updatedTransaction)
        },
        onCategoryUpdate = { updatedTransaction, updateForAllTransactions ->
            viewModel.updateTransactionCategory(updatedTransaction, updateForAllTransactions)
        },
        deleteTransaction = { id ->
            viewModel.deleteTransaction(id)
        }
    )
}

@Composable
fun TransactionScreen(
    transaction: Transaction?,
    accounts: List<Account>,
    categories: List<Category>,
    onAccountUpdate: (Transaction) -> Unit,
    onCategoryUpdate: (Transaction, Boolean) -> Unit,
    deleteTransaction: (Int) -> Unit
) {
    ScreenScaffold(
        "Transaction Details",
        {
            IconButton(onClick = { transaction?.let { deleteTransaction(it.id) } }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Transaction")
            }
        }
    ) {
        transaction?.let {
            TransactionDetails(
                transaction = it,
                accounts = accounts,
                categories = categories,
                onAccountUpdate = onAccountUpdate,
                onCategoryUpdate = onCategoryUpdate
            )
        }
    }
}

private val sampleTransaction = Transaction(
    id = 1,
    amount = 10000,
    datetime = Instant.now().epochSecond,
    accountId = 1,
    categoryId = 1,
    payee = "Sample Payee",
    referenceId = 1,
    description = "Sample Description",
    transactionType = TransactionType.DEBIT,
    rawAccountNo = "1234"
)

private val sampleAccounts = listOf(
    Account(1, "Account 1", 0, AccountType.SAVINGS, "1234", 0xFF0000, AccountIcon.SAVINGS),
    Account(2, "Account 2", 0, AccountType.CHECKING, "5678", 0x00FF00, AccountIcon.CHECKING),
    Account(3, "Account 3", 0, AccountType.CASH, "9012", 0x0000FF, AccountIcon.CASH)
)

private val sampleCategories = listOf(
    Category(1, "Food", CategoryType.FOOD, null, 0xFF0000, CategoryIcon.FOOD),
    Category(2, "Transport", CategoryType.TRANSPORT, null, 0x00FF00, CategoryIcon.TRANSPORT),
    Category(3, "Shopping", CategoryType.SHOPPING, null, 0x0000FF, CategoryIcon.SHOPPING)
)

@Preview(showBackground = true)
@Composable
fun TransactionScreenPreview() {
    FinancesTheme {
        TransactionScreen(
            transaction = sampleTransaction,
            accounts = sampleAccounts,
            categories = sampleCategories,
            onAccountUpdate = {},
            onCategoryUpdate = { _, _ -> },
            deleteTransaction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionScreenPreviewDark() {
    FinancesTheme(darkTheme = true) {
        TransactionScreen(
            transaction = sampleTransaction,
            accounts = sampleAccounts,
            categories = sampleCategories,
            onAccountUpdate = {},
            onCategoryUpdate = { _, _ -> },
            deleteTransaction = {}
        )
    }
}
