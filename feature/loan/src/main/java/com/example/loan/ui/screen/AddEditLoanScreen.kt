package com.example.loan.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.ui.component.DateTimePickerDialog
import com.example.common.ui.component.ListWrapper
import com.example.common.ui.component.PickerDialog
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.component.SingleRowItem
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.common.utils.toDateTimeString
import com.example.core.database.entity.Loan
import com.example.core.database.models.LoanType
import com.example.loan.ui.viewmodel.AddEditLoanViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Composable
fun AddEditLoanScreen(
    onDismiss: () -> Unit,
    vm: AddEditLoanViewModel = hiltViewModel()
) {
    val loan by vm.loan.collectAsStateWithLifecycle()
    val saveError by vm.saveError.collectAsStateWithLifecycle()
    val isSaving by vm.isSaving.collectAsStateWithLifecycle()

    AddEditLoanContent(
        loan = loan,
        loanNumberError = saveError,
        isSaving = isSaving,
        onIconButtonClick = {
            if (!vm.isNewLoan()) {
                vm.deleteLoan()
            }
            onDismiss()
        },
        isNewLoan = vm::isNewLoan,
        onSave = { loan ->
            vm.saveLoan(loan)
            onDismiss()
        }
    )
}

@Composable
fun AddEditLoanContent(
    loan: Loan?,
    loanNumberError: String?,
    isSaving: Boolean,
    onIconButtonClick: () -> Unit,
    isNewLoan: () -> Boolean,
    onSave: (loan: Loan) -> Unit
) {
    ScreenScaffold(
        if(isNewLoan()) "Create Loan" else "Edit Loan",
        {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete Loan",
                modifier = Modifier.clickable { onIconButtonClick() }
            )
        },
        isLoading = loan == null
    ) { paddingValues ->
        if (loan == null) throw IllegalStateException("Loan is null in AddEditLoanContent, added this check to satisfy the compiler, but this should never happen")

        var payee by remember { mutableStateOf(loan.payee) }
        var amount by remember { mutableStateOf(loan.amount.toString()) }
        var loanType by remember { mutableStateOf(loan.loanType) }
        var expectedReturnDatetime by remember { mutableLongStateOf(loan.expectedReturnDatetime) }

        var showDialog by remember { mutableStateOf(false) }
        var showDateTimeDialog by remember { mutableStateOf(false) }

        ListWrapper(paddingValues) {
            PickerDialog(
                title = "Select Loan Type",
                show = showDialog,
                items = LoanType.entries.toList(),
                onDismiss = { showDialog = false },
                onItemSelected = {
                    loanType = it
                    showDialog = false
                }
            )

            MyInput.TextField(
                value = payee,
                onValueChange = { payee = it },
                label = "Payee",
            )

            MyInput.TextField(
                value = amount,
                onValueChange = { amount = it },
                label = "Amount",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            SingleRowItem(Modifier.clickable { showDialog = true }) {
                MyText.RowHeader(loanType.name)
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                MyInput.TextField(
                    value = expectedReturnDatetime.toDateTimeString(),
                    onValueChange = {},
                    readOnly = true,
                    label = "Expected Return Date & Time",
                    trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDateTimeDialog = true }
                )
            }

            DateTimePickerDialog(
                showDialog = showDateTimeDialog,
                title = "Select Expected Return",
                initialEpochSeconds = expectedReturnDatetime,
                onDismiss = { showDateTimeDialog = false },
                onConfirm = {
                    expectedReturnDatetime = it
                    showDateTimeDialog = false
                }
            )

            MyInput.Button(
                text = "Save",
                onClick = {
                    onSave(
                        loan.copy(
                            payee = payee,
                            amount = amount.toLongOrNull() ?: 0L,
                            loanType = loanType,
                            expectedReturnDatetime = expectedReturnDatetime,
                        )
                    )
                },
                enabled = !isSaving && payee.isNotBlank() && amount.isNotBlank()
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AddEditLoanContentPreview() {
    FinancesTheme {
        AddEditLoanContent(
            loan = Loan(
                id = 1,
                payee = "John Doe",
                amount = 1000L,
                loanType = LoanType.CREDIT,
                loanedDatetime = Clock.System.now().epochSeconds,
                expectedReturnDatetime = Clock.System.now().plus(7.days).epochSeconds
            ),
            loanNumberError = null,
            isSaving = false,
            onIconButtonClick = {},
            isNewLoan = { false },
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditLoanContentPreviewDark() {
    FinancesTheme(true) {
        AddEditLoanContent(
            loan = Loan(
                id = 1,
                payee = "John Doe",
                amount = 1000L,
                loanType = LoanType.CREDIT,
                loanedDatetime = Clock.System.now().epochSeconds,
                expectedReturnDatetime = Clock.System.now().plus(7.days).epochSeconds
            ),
            loanNumberError = null,
            isSaving = false,
            onIconButtonClick = {},
            isNewLoan = { false },
            onSave = {}
        )
    }
}