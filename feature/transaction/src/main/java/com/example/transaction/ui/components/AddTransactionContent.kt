package com.example.transaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.common.ui.component.SingleRowItem
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.common.utils.toDateTimeString
import com.example.core.database.entity.Account
import com.example.core.database.entity.Category
import com.example.core.database.entity.Transaction
import com.example.core.database.models.TransactionType
import kotlin.text.equals

@Composable
fun AddTransactionContent(
    categories: List<Category>,
    accounts: List<Account>,
    onAddTransaction: (Transaction) -> Unit,
    onDismiss: () -> Unit,
    padding: PaddingValues
) {
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
        .padding(top = padding.calculateTopPadding())
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