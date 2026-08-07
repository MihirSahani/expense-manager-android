package com.example.transaction.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.component.SingleRowItem
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.common.utils.toDateTimeString
import com.example.core.database.entity.Account
import com.example.core.database.entity.Category
import com.example.core.database.entity.Transaction
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import com.example.core.database.models.TransactionType
import com.example.transaction.ui.components.SimpleDialog
import com.example.transaction.ui.viewmodel.TransactionViewModel

@Composable
fun AddTransactionScreen(onDismiss: () -> Unit) {
    val vm: TransactionViewModel = hiltViewModel()

    val categories by vm.categories.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()

    AddTransactionContent(
        categories = categories,
        accounts = accounts,
        onAddTransaction = { transaction ->
            vm.createTransaction(transaction)
        },
        onDismiss = {
            onDismiss()
        }
    )
}

@Composable
fun AddTransactionContent(
    categories: List<Category>,
    accounts: List<Account>,
    onAddTransaction: (transaction: Transaction) -> Unit,
    onDismiss: () -> Unit,
) {
    ScreenScaffold("Add Transaction") { paddingValues ->
        var payee by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var transactionType by remember { mutableStateOf(TransactionType.CREDIT) }

        var selectedCategory by remember { mutableStateOf<Category?>(null) }
        var showCategoryDialog by remember { mutableStateOf(false) }

        var selectedAccount by remember { mutableStateOf<Account?>(null) }
        var showAccountDialog by remember { mutableStateOf(false) }

        var transactionDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
        var showDatePickerDialog by remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = transactionDate)

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(top = paddingValues.calculateTopPadding())
            .padding(horizontal = 16.dp),
            // .verticalScroll(rememberScrollState())
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            MyInput.TextField(
                value = payee,
                onValueChange = { payee = it },
                label = "Payee",
            )

            MyInput.TextField(
                value = amount,
                onValueChange = { amount = it },
                label = "Amount",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            SingleRowItem {
                MyText.RowBody("Type:")
                Row {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = transactionType == TransactionType.DEBIT, onClick = {
                            transactionType = TransactionType.DEBIT
                            selectedCategory = null
                        })
                        MyText.RowBody("Expense")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = transactionType == TransactionType.CREDIT, onClick = {
                            transactionType = TransactionType.CREDIT
                            selectedCategory = null
                        })
                        MyText.RowBody("Income")
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                MyInput.TextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = "Category",
                    placeholder = "Select Category",
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showCategoryDialog = true }
                )
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                MyInput.TextField(
                    value = selectedAccount?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = "Account",
                    placeholder = "Select Account",
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showAccountDialog = true }
                )
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                MyInput.TextField(
                    value = (transactionDate / 1000).toDateTimeString(),
                    onValueChange = {},
                    readOnly = true,
                    label = "Transaction Date",
                    trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePickerDialog = true }
                )
            }

            MyInput.TextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                singleLine = false,
            )

            Button(
                onClick = {
                    val newTransaction = Transaction(
                        amount = amount.toLong()*100,
                        categoryId = selectedCategory?.id,
                        datetime = transactionDate,
                        rawAccountNo = selectedAccount?.name ?: "",
                        accountId = selectedAccount?.id,
                        payee = payee,
                        transactionType = transactionType,
                        referenceId = null,
                        description = description,
                    )
                    onAddTransaction(newTransaction)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = payee.isNotBlank() && amount.isNotBlank() && selectedCategory != null && selectedAccount != null,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Transaction")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showCategoryDialog) {
                SimpleDialog(
                    title = "Select Category",
                    items = categories.map { Triple(it.icon.imageVector, it.name, it.color) },
                    onItemSelected = { index ->
                        selectedCategory = categories[index]
                        showCategoryDialog = false
                    },
                    onDismiss = { showCategoryDialog = false }
                )
            }

            if (showAccountDialog) {
                SimpleDialog(
                    title = "Select Account",
                    items = accounts.map { Triple(it.icon.imageVector, it.name, it.color) },
                    onItemSelected = { index ->
                        selectedAccount = accounts[index]
                        showAccountDialog = false
                    },
                    onDismiss = { showAccountDialog = false }
                )
            }

            if (showDatePickerDialog) {
                DatePickerDialog(
                    onDismissRequest = { showDatePickerDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { transactionDate = it }
                            showDatePickerDialog = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePickerDialog = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(datePickerState)
                }
            }
        }
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

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    FinancesTheme {
        AddTransactionContent(previewCategories, previewAccounts, onAddTransaction = {}, onDismiss = {})
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreviewDark() {
    FinancesTheme(darkTheme = true) {
        AddTransactionContent(previewCategories, previewAccounts, onAddTransaction = {}, onDismiss = {})
    }
}