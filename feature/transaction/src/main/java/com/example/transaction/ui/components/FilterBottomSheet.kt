package com.example.transaction.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.common.model.TransactionFilter
import com.example.common.ui.component.DateTimePickerDialog
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.common.utils.toDateString
import com.example.core.database.entity.Category
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import java.time.Instant

/**
 * Bottom sheet for building a [TransactionFilter]: category multi-select, a date range, an
 * amount range, and a payee search. Initialized from [initialFilter]; [onApply] is invoked with
 * the edited filter, [onClear] resets it to empty, and [onDismiss] closes the sheet without
 * changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    initialFilter: TransactionFilter,
    categories: List<Category>,
    onApply: (TransactionFilter) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCategoryIds by remember { mutableStateOf(initialFilter.categoryIds) }
    var includeUncategorized by remember { mutableStateOf(initialFilter.includeUncategorized) }
    var startDate by remember { mutableStateOf(initialFilter.startDate) }
    var endDate by remember { mutableStateOf(initialFilter.endDate) }
    var minAmountText by remember { mutableStateOf(initialFilter.minAmount?.let { (it / 100).toString() } ?: "") }
    var maxAmountText by remember { mutableStateOf(initialFilter.maxAmount?.let { (it / 100).toString() } ?: "") }
    var payeeQuery by remember { mutableStateOf(initialFilter.payeeQuery ?: "") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            MyText.SecondaryHeader("Filter Transactions")

            CategoryFilterSection(
                categories = categories,
                selectedCategoryIds = selectedCategoryIds,
                includeUncategorized = includeUncategorized,
                onToggleCategory = { categoryId ->
                    selectedCategoryIds = if (categoryId in selectedCategoryIds) {
                        selectedCategoryIds - categoryId
                    } else {
                        selectedCategoryIds + categoryId
                    }
                },
                onToggleUncategorized = { includeUncategorized = !includeUncategorized }
            )

            DateRangeFilterSection(
                startDate = startDate,
                endDate = endDate,
                onStartDateClick = { showStartDatePicker = true },
                onEndDateClick = { showEndDatePicker = true },
            )

            AmountRangeFilterSection(
                minAmountText = minAmountText,
                maxAmountText = maxAmountText,
                onMinAmountChange = { minAmountText = it },
                onMaxAmountChange = { maxAmountText = it },
            )

            MyInput.TextField(
                value = payeeQuery,
                onValueChange = { payeeQuery = it },
                label = "Payee",
                placeholder = "Search payee name",
            )

            FilterActionRow(
                onClear = {
                    selectedCategoryIds = emptySet()
                    includeUncategorized = false
                    startDate = null
                    endDate = null
                    minAmountText = ""
                    maxAmountText = ""
                    payeeQuery = ""
                    onClear()
                },
                onApply = {
                    onApply(
                        TransactionFilter(
                            categoryIds = selectedCategoryIds,
                            includeUncategorized = includeUncategorized,
                            startDate = startDate,
                            endDate = endDate,
                            minAmount = minAmountText.toLongOrNull()?.times(100),
                            maxAmount = maxAmountText.toLongOrNull()?.times(100),
                            payeeQuery = payeeQuery.trim().takeIf { it.isNotEmpty() },
                        )
                    )
                }
            )
        }
    }

    DateTimePickerDialog(
        showDialog = showStartDatePicker,
        title = "Select Start Date",
        initialEpochSeconds = startDate ?: Instant.now().epochSecond,
        onDismiss = { showStartDatePicker = false },
        onConfirm = {
            startDate = it
            showStartDatePicker = false
        }
    )

    DateTimePickerDialog(
        showDialog = showEndDatePicker,
        title = "Select End Date",
        initialEpochSeconds = endDate ?: Instant.now().epochSecond,
        onDismiss = { showEndDatePicker = false },
        onConfirm = {
            endDate = it
            showEndDatePicker = false
        }
    )
}

/** Multi-select row of category chips, plus an "Uncategorised" chip for uncategorized transactions. */
@Composable
private fun CategoryFilterSection(
    categories: List<Category>,
    selectedCategoryIds: Set<Int>,
    includeUncategorized: Boolean,
    onToggleCategory: (Int) -> Unit,
    onToggleUncategorized: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MyText.RowBody("Categories")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                FilterChip(
                    selected = category.id in selectedCategoryIds,
                    onClick = { onToggleCategory(category.id) },
                    label = { Text(category.name) }
                )
            }
            item {
                FilterChip(
                    selected = includeUncategorized,
                    onClick = onToggleUncategorized,
                    label = { Text("Uncategorised") }
                )
            }
        }
    }
}

/** Side-by-side "from"/"to" date fields, each opening [onStartDateClick]/[onEndDateClick]. */
@Composable
private fun DateRangeFilterSection(
    startDate: Long?,
    endDate: Long?,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MyText.RowBody("Date Range")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateFilterField(
                modifier = Modifier.weight(1f),
                label = "From",
                date = startDate,
                onClick = onStartDateClick,
            )
            DateFilterField(
                modifier = Modifier.weight(1f),
                label = "To",
                date = endDate,
                onClick = onEndDateClick,
            )
        }
    }
}

/** A read-only date field that opens a date picker when tapped anywhere on it. */
@Composable
private fun DateFilterField(
    label: String,
    date: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        MyInput.TextField(
            value = date?.toDateString() ?: "",
            onValueChange = {},
            readOnly = true,
            label = label,
            trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

/** Side-by-side min/max numeric amount fields (entered in rupees). */
@Composable
private fun AmountRangeFilterSection(
    minAmountText: String,
    maxAmountText: String,
    onMinAmountChange: (String) -> Unit,
    onMaxAmountChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MyText.RowBody("Amount Range")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MyInput.TextField(
                modifier = Modifier.weight(1f),
                value = minAmountText,
                onValueChange = onMinAmountChange,
                label = "Min",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            MyInput.TextField(
                modifier = Modifier.weight(1f),
                value = maxAmountText,
                onValueChange = onMaxAmountChange,
                label = "Max",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
    }
}

/** "Clear"/"Apply" action buttons at the bottom of the sheet. */
@Composable
private fun FilterActionRow(
    onClear: () -> Unit,
    onApply: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MyInput.Button(modifier = Modifier.weight(1f), text = "Clear", onClick = onClear)
        MyInput.Button(modifier = Modifier.weight(1f), text = "Apply", onClick = onApply)
    }
}

private val previewCategories = listOf(
    Category(id = 1, name = "Groceries", type = CategoryType.EXPENSE, budgetPerCycle = null, color = null, icon = CategoryIcon.GROCERIES),
    Category(id = 2, name = "Salary", type = CategoryType.INCOME, budgetPerCycle = null, color = null, icon = CategoryIcon.SALARY),
    Category(id = 3, name = "Transport", type = CategoryType.EXPENSE, budgetPerCycle = null, color = null, icon = CategoryIcon.TRANSPORT),
)

@Preview(showBackground = true)
@Composable
private fun FilterBottomSheetPreview() {
    FinancesTheme {
        FilterBottomSheet(
            initialFilter = TransactionFilter(),
            categories = previewCategories,
            onApply = {},
            onClear = {},
            onDismiss = {},
        )
    }
}
