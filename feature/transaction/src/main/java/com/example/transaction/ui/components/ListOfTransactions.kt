package com.example.transaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.common.ui.theme.FinancesTheme
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.TransactionType
import com.example.core.database.projection.TransactionWithCategory
import com.example.transaction.ui.viewmodel.TransactionListItem

@Composable
fun ListOfTransactions(
    modifier: Modifier = Modifier,
    items: LazyPagingItems<TransactionListItem>,
    onTransactionClick: (Int) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { it.key() },
            contentType = items.itemContentType { it.contentType() }
        ) { index ->
            when (val item = items[index]) {
                is TransactionListItem.DateHeader ->
                    Date(item.date, index)
                is TransactionListItem.TransactionItem -> {
                    val prev = if (index > 0) items.peek(index - 1) else null
                    val next = if (index < items.itemCount - 1) items.peek(index + 1) else null

                    val isFirst = prev is TransactionListItem.DateHeader || prev == null
                    val isLast = next is TransactionListItem.DateHeader || next == null

                    val shape = RoundedCornerShape(
                        topStart = if (isFirst) 16.dp else 0.dp,
                        topEnd = if (isFirst) 16.dp else 0.dp,
                        bottomStart = if (isLast) 16.dp else 0.dp,
                        bottomEnd = if (isLast) 16.dp else 0.dp,
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (!isFirst) HorizontalDivider(
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        TransactionItem(item.transactionWithCategory, onTransactionClick)
                    }
                }
                null -> {} // placeholder while loading
            }
        }
    }
}

/** Renders a single list entry. Shared by the paging list and the preview. */
@Composable
private fun TransactionRow(item: TransactionListItem, onTransactionClick: (Int) -> Unit) {
    when (item) {
        is TransactionListItem.DateHeader -> Date(item.date)
        is TransactionListItem.TransactionItem ->
            TransactionItem(item = item.transactionWithCategory, onClick = onTransactionClick)

    }
}

private fun TransactionListItem.key(): String = when (this) {
    is TransactionListItem.DateHeader -> "header-${date}"
    is TransactionListItem.TransactionItem -> "txn-${transactionWithCategory.id}"
}

private fun TransactionListItem.contentType(): String = when (this) {
    is TransactionListItem.DateHeader -> "header"
    is TransactionListItem.TransactionItem -> "transaction"
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
private fun ListOfTransactionsPreview() {
    FinancesTheme {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sample, key = { it.key() }) { item ->
                TransactionRow(item, onTransactionClick = {})
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListOfTransactionsPreviewDark() {
    FinancesTheme(darkTheme = true) {
        Surface {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sample, key = { it.key() }) { item ->
                    TransactionRow(item, onTransactionClick = {})
                }
            }
        }
    }
}