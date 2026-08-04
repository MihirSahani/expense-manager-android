package com.example.transaction.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.common.ui.component.IconAndRow
import com.example.common.ui.theme.FinancesTheme
import com.example.common.ui.theme.SamsungTextGrayDark
import com.example.common.ui.theme.SamsungTextGrayLight
import com.example.common.utils.MyText
import com.example.common.utils.toTimeString
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.TransactionType
import com.example.core.database.projection.TransactionWithCategory

@Composable
fun TransactionItem(
    item: TransactionWithCategory,
    onClick: (Int) -> Unit,
) {
    IconAndRow(icon = item.categoryIcon?.imageVector, bgColor = item.categoryColor) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(item.id) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText.RowHeader(text = item.payee)
                MyText.TransactionAmount(item.amount, item.transactionType)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText.RowBody(
                    text = item.categoryName ?: "Uncategorized",
                    color = if (item.categoryName == null) MaterialTheme.colorScheme.error
                    else if (isSystemInDarkTheme()) SamsungTextGrayDark else SamsungTextGrayLight
                )
                MyText.RowBody(item.datetime.toTimeString())
            }
        }
    }
}

private val previewItem = TransactionWithCategory(
    id = 1,
    payee = "Groceries",
    amount = 25_000,
    datetime = System.currentTimeMillis(),
    transactionType = TransactionType.DEBIT,
    categoryIcon = CategoryIcon.GROCERIES,
    categoryName = "Shopping",
    categoryColor = 0xFF00FF00.toInt()
)

@Preview(showBackground = true)
@Composable
fun TransactionItemPreview() {
    FinancesTheme {
        TransactionItem(item = previewItem, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionItemPreviewDark() {
    FinancesTheme(darkTheme = true) {
        Surface {
            TransactionItem(item = previewItem, onClick = {})
        }
    }
}
