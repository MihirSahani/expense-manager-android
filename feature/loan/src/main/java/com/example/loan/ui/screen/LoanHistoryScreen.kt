package com.example.loan.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.common.ui.component.PreviewPagingData
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.core.database.entity.Loan
import com.example.core.database.models.LoanType
import com.example.loan.ui.component.ListOfLoanWithDate
import com.example.loan.ui.component.LoanListItem
import com.example.loan.ui.viewmodel.LoanViewModel
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Composable
fun LoanHistoryScreen(onLoanClick: (Int) -> Unit, addLoanScreen: () -> Unit, vm: LoanViewModel = hiltViewModel()) {
    val loansGiven = vm.loansGiven.collectAsLazyPagingItems()
    val loansTaken = vm.loansTaken.collectAsLazyPagingItems()

    LoanHistoryContent(loansGiven, loansTaken, onLoanClick, addLoanScreen)
}

@Composable
fun LoanHistoryContent(
    loansGiven: LazyPagingItems<LoanListItem>,
    loansTaken: LazyPagingItems<LoanListItem>,
    onLoanClick: (Int) -> Unit,
    addLoanScreen: () -> Unit
) {
    ScreenScaffold(
        "Loans",
        floatingActionButton = { modifier ->
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Loan",
                modifier = modifier
                    .clickable { addLoanScreen() }
            )
        },
    ) { padding ->
        // ScreenScaffold places content in a Box, so the two lists must be stacked in a Column
        // themselves; otherwise they overlap and the second list fully hides the first.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {

            ListOfLoanWithDate(
                items = loansGiven,
                loanType = LoanType.CREDIT,
                onLoanClick = { id -> onLoanClick(id) }
            )

            ListOfLoanWithDate(
                items = loansTaken,
                loanType = LoanType.DEBT,
                onLoanClick = { id -> onLoanClick(id) }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private val sampleCredit = flowOf(PreviewPagingData.from(listOf(
    LoanListItem.DateHeader("Tue, 22 Sept 2025"),
    LoanListItem.LoanItem(
        Loan(
            id = 1,
            payee = "Navin",
            amount = 45_000_00,
            loanType = LoanType.CREDIT,
            loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
            expectedReturnDatetime = Clock.System.now().plus(30.days).epochSeconds
        )
    ),
    LoanListItem.LoanItem(
        Loan(
            id = 2,
            payee = "Government of India",
            amount = 1200_21,
            loanType = LoanType.CREDIT,
            loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
            expectedReturnDatetime = Clock.System.now().plus(30.days).epochSeconds
        )
    ),
    LoanListItem.DateHeader("Wed, 23 Sept 2025"),
    LoanListItem.LoanItem(
        Loan(
            id = 3,
            payee = "Shashank",
            amount = 45_00,
            loanType = LoanType.CREDIT,
            loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
            expectedReturnDatetime = Clock.System.now().plus(31.days).epochSeconds
        )
    ),
    LoanListItem.LoanItem(
        Loan(
            id = 4,
            payee = "Amit",
            amount = 1_00_000,
            loanType = LoanType.CREDIT,
            loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
            expectedReturnDatetime = Clock.System.now().plus(32.days).epochSeconds
        )
    )
)
))

private val sampleDebts = flowOf(PreviewPagingData.from(listOf(
        LoanListItem.DateHeader("Tue, 22 Sept 2025"),
        LoanListItem.LoanItem(
            Loan(
                id = 1,
                payee = "Navin",
                amount = 45_000_00,
                loanType = LoanType.DEBT,
                loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
                expectedReturnDatetime = Clock.System.now().plus(30.days).epochSeconds
            )
        ),
        LoanListItem.LoanItem(
            Loan(
                id = 2,
                payee = "Government of India",
                amount = 1200_21,
                loanType = LoanType.DEBT,
                loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
                expectedReturnDatetime = Clock.System.now().plus(30.days).epochSeconds
            )
        ),
        LoanListItem.DateHeader("Wed, 23 Sept 2025"),
        LoanListItem.LoanItem(
            Loan(
                id = 3,
                payee = "Shashank",
                amount = 45_00,
                loanType = LoanType.DEBT,
                loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
                expectedReturnDatetime = Clock.System.now().plus(31.days).epochSeconds
            )
        ),
        LoanListItem.LoanItem(
            Loan(
                id = 4,
                payee = "Amit",
                amount = 1_00_000,
                loanType = LoanType.DEBT,
                loanedDatetime = Clock.System.now().minus(30.days).epochSeconds,
                expectedReturnDatetime = Clock.System.now().plus(32.days).epochSeconds
            )
        )
    )
))

@Preview(showBackground = true)
@Composable
fun LoanHistoryScreenPreview() {
    val loansGiven = sampleCredit.collectAsLazyPagingItems()
    val loansTaken = sampleDebts.collectAsLazyPagingItems()
    FinancesTheme {
        LoanHistoryContent(loansGiven = loansGiven, loansTaken = loansTaken, onLoanClick = { }, addLoanScreen = { })
    }
}

@Preview(showBackground = true)
@Composable
fun LoanHistoryScreenPreviewDark() {
    val loansGiven = sampleCredit.collectAsLazyPagingItems()
    val loansTaken = sampleDebts.collectAsLazyPagingItems()
    FinancesTheme(true) {
        LoanHistoryContent(loansGiven = loansGiven, loansTaken = loansTaken, onLoanClick = { }, addLoanScreen = { })
    }
}