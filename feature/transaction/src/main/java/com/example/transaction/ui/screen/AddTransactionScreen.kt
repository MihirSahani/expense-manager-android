package com.example.transaction.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.core.database.entity.Account
import com.example.core.database.entity.Category
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import com.example.transaction.ui.components.AddTransactionContent
import com.example.transaction.ui.viewmodel.TransactionViewModel

@Composable
fun AddEditTransactionScreen(onDismiss: () -> Unit) {
    val vm: TransactionViewModel = hiltViewModel()

    val categories by vm.categories.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()

    ScreenScaffold("Add Transaction") { padding ->

        AddTransactionContent(
            categories = categories,
            accounts = accounts,
            onAddTransaction = { transaction ->
                vm.createTransaction(transaction)
            },
            onDismiss = {
                onDismiss()
            },
            padding = padding
        )
    }
}

private val previewCategories = listOf(
    Category(id = 1, name = "Groceries", type = CategoryType.EXPENSE, budgetPerCycle = null, color = null, icon = CategoryIcon.GROCERIES),
    Category(id = 2, name = "Salary", type = CategoryType.INCOME, budgetPerCycle = null, color = null, icon = CategoryIcon.SALARY),
    Category(id = 3, name = "Transport", type = CategoryType.EXPENSE, budgetPerCycle = null, color = null, icon = CategoryIcon.TRANSPORT),
)

private val previewAccounts = listOf(
    Account(id = 1, name = "Checking", balance = 250000, type = AccountType.CHECKING, accountNumber = "1234", color = null, icon = AccountIcon.CHECKING),
    Account(id = 2, name = "Cash", balance = 5000, type = AccountType.CASH, accountNumber = null, color = null, icon = AccountIcon.CASH),
)

@Composable
private fun AddTransactionScreenPreviewContent() {
    ScreenScaffold("Add Transaction") { padding ->
        AddTransactionContent(
            categories = previewCategories,
            accounts = previewAccounts,
            onAddTransaction = {},
            onDismiss = {},
            padding = padding
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    FinancesTheme {
        AddTransactionScreenPreviewContent()
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreviewDark() {
    FinancesTheme(darkTheme = true) {
        AddTransactionScreenPreviewContent()
    }
}