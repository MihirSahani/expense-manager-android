package com.example.loan.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import com.example.common.ui.component.Date
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText
import com.example.core.database.entity.Loan
import com.example.core.database.models.LoanType
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Composable
fun ListOfLoanWithDate(
    modifier: Modifier = Modifier,
    items: LazyPagingItems<LoanListItem>,
    loanType: LoanType,
    onLoanClick: (Int) -> Unit = {},
) {
    if (items.itemCount == 0) return

    // Header for type of loan. The list always starts with a DateHeader, so the type can't be
    // reliably inferred by peeking at the first item; the caller knows it upfront instead.
    MyText.SecondaryHeader(
        when (loanType) {
            LoanType.DEBT -> "Loans Taken"
            LoanType.CREDIT -> "Loans Given"
        },
        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
    )

    LazyColumn(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        // contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { it.key() },
            contentType = items.itemContentType { it.contentType() }
        ) { index ->
            when (val item = items[index]) {
                is LoanListItem.DateHeader ->
                    Date(item.date, index)
                is LoanListItem.LoanItem -> {
                    val prev = if (index > 0) items.peek(index - 1) else null
                    val next = if (index < items.itemCount - 1) items.peek(index + 1) else null

                    val isFirst = prev is LoanListItem.DateHeader || prev == null
                    val isLast = next is LoanListItem.DateHeader || next == null

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
                        LoanRow(item.loan, onLoanClick)
                    }


                }
                null -> {} // placeholder while loading
            }
        }
    }

    Spacer(modifier = Modifier.padding(bottom = 16.dp))
}


private fun LoanListItem.key(): String = when (this) {
    is LoanListItem.DateHeader -> "header-${date}"
    is LoanListItem.LoanItem -> "loan-${loan.id}"
}

private fun LoanListItem.contentType(): String = when (this) {
    is LoanListItem.DateHeader -> "header"
    is LoanListItem.LoanItem -> "loan"
}

// ------------------------------------------ Preview ----------------------------------------------

private val sampleCredits = listOf(
    LoanListItem.DateHeader("Tue, 22 Sept 2025"),
    LoanListItem.LoanItem(
        Loan(
            id = 101,
            payee = "Rahul",
            amount = 30_000,
            loanType = LoanType.CREDIT,
            loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
            expectedReturnDatetime = Clock.System.now().plus(30.days).epochSeconds
        )
    ),
    LoanListItem.LoanItem(
        Loan(
            id = 102,
            payee = "Bank of Baroda",
            amount = 75_500,
            loanType = LoanType.CREDIT,
            loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
            expectedReturnDatetime = Clock.System.now().plus(30.days).epochSeconds
        )
    ),
    LoanListItem.DateHeader("Wed, 23 Sept 2025"),
    LoanListItem.LoanItem(
        Loan(
            id = 103,
            payee = "Priya",
            amount = 12_500,
            loanType = LoanType.CREDIT,
            loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
            expectedReturnDatetime = Clock.System.now().plus(31.days).epochSeconds
        )
    ),
    LoanListItem.LoanItem(
        Loan(
            id = 104,
            payee = "ICICI Bank",
            amount = 2_00_000,
            loanType = LoanType.CREDIT,
            loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
            expectedReturnDatetime = Clock.System.now().plus(32.days).epochSeconds
        )
    )
)

@Composable
private fun LoanAndDateRowForPreview(item: LoanListItem, onClick: (Int) -> Unit) {
    when (item) {
        is LoanListItem.DateHeader -> Date(item.date)
        is LoanListItem.LoanItem ->
            LoanRow(item = item.loan, onClick = onClick)

    }
}

@Preview(showBackground = true)
@Composable
private fun ListOfRowItemsWithDateRowForPreviewPreview() {
    FinancesTheme {
        Surface {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleCredits, key = { it.key() }) { item ->
                    LoanAndDateRowForPreview(item, onClick = {})
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListOfRowItemsWithDateRowForPreviewPreviewDark() {
    FinancesTheme(darkTheme = true) {
        Surface {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleCredits, key = { it.key() }) { item ->
                    LoanAndDateRowForPreview(item, onClick = {})
                }
            }
        }
    }
}