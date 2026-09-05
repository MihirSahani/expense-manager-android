package com.example.loan.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.common.ui.component.SingleRowItem
import com.example.common.ui.theme.FinancesTheme
import com.example.common.ui.theme.SamsungTextGrayDark
import com.example.common.ui.theme.SamsungTextGrayLight
import com.example.common.utils.MyText
import com.example.common.utils.toTimeString
import com.example.core.database.entity.Loan
import com.example.core.database.models.LoanType
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Composable
fun LoanRow(
    item: Loan,
    onClick: (Int) -> Unit,
) {
    SingleRowItem {
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
                MyText.TransactionAmount(item.amount, item.loanType)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText.RowBody(
                    text = when (item.loanType) {
                        LoanType.DEBT -> "Expected Return: "
                        LoanType.CREDIT -> "Expected Payback: "
                    },
                    color = if (isSystemInDarkTheme()) SamsungTextGrayDark else SamsungTextGrayLight
                )
                MyText.RowBody(item.expectedReturnDatetime.toTimeString() )
            }
        }
    }
}

private val previewItem = Loan(
    id = 1,
    payee = "John Doe",
    amount = 25_000_00,
    loanType = LoanType.DEBT,
    loanedDatetime = Clock.System.now().minus(7.days).epochSeconds,
    expectedReturnDatetime = Clock.System.now().plus(7.days).epochSeconds
)

@Preview(showBackground = true)
@Composable
fun TransactionItemPreview() {
    FinancesTheme {
        LoanRow(item = previewItem, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionItemPreviewDark() {
    FinancesTheme(darkTheme = true) {
        Surface {
            LoanRow(item = previewItem, onClick = {})
        }
    }
}
