package com.example.transaction.ui.screens

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.TransactionType
import com.example.core.database.projection.TransactionWithCategory
import com.example.transaction.ui.components.ListOfTransactions
import com.example.transaction.ui.viewmodel.TransactionListItem
import com.example.transaction.ui.viewmodel.TransactionViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun TransactionHistoryScreen(onTransactionClick: (Int) -> Unit) {
    val viewModel: TransactionViewModel = hiltViewModel()
    TransactionHistoryScreen(
        items = viewModel.transactions.collectAsLazyPagingItems(),
        onTransactionClick = onTransactionClick
    )
}

@Composable
fun TransactionHistoryScreen(
    items: LazyPagingItems<TransactionListItem>,
    onTransactionClick: (Int) -> Unit,
) {
    ScreenScaffold (
        title = "Transaction History",
    ) { modifier ->
        ListOfTransactions(
            modifier = modifier,
            items = items,
            onTransactionClick = { transactionId -> onTransactionClick(transactionId) }
        )
    }
}

private val sample = listOf(
    TransactionListItem.DateHeader("Mon, 5 May 2025"),
    TransactionListItem.TransactionItem(
        TransactionWithCategory(
            id = 1,
            payee = "Grocery Store",
            amount = 45_00000,
            datetime = 0L,
            transactionType = TransactionType.DEBIT,
            categoryIcon = CategoryIcon.GROCERIES,
            categoryName = "Groceries",
            categoryColor = 0xFFFF0000.toInt()
        )
    ),
    TransactionListItem.TransactionItem(
        TransactionWithCategory(
            id = 2,
            payee = "Government of India",
            amount = 120_021,
            datetime = 0L,
            transactionType = TransactionType.CREDIT,
            categoryIcon = CategoryIcon.SALARY,
            categoryName = "Salary",
            categoryColor = 0xFF0000FF.toInt()
        )
    ),
    TransactionListItem.DateHeader("Sun, 4 May 2025"),
    TransactionListItem.TransactionItem(
        TransactionWithCategory(
            id = 3,
            payee = "Coffee",
            amount = 4_500,
            datetime = 0L,
            transactionType = TransactionType.DEBIT,
            categoryIcon = CategoryIcon.FOOD,
            categoryName = "Food",
            categoryColor = 0xFF00FF00.toInt()
        )
    )
)

@Preview(showBackground = true)
@Composable
fun TransactionHistoryScreenPreview() {
    FinancesTheme {
        TransactionHistoryScreen(
            items = flowOf(PagingData.from(sample)).collectAsLazyPagingItems(),
            onTransactionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionHistoryScreenPreviewDark() {
    FinancesTheme(darkTheme = true) {
        Surface {
            TransactionHistoryScreen(
                items = flowOf(PagingData.from(sample)).collectAsLazyPagingItems(),
                onTransactionClick = {}
            )
        }
    }
}