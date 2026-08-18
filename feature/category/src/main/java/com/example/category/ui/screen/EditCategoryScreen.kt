package com.example.category.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.category.ui.viewmodel.CategoryViewModel
import com.example.core.database.models.DefaultColors
import com.example.common.ui.component.ItemAndDivider
import com.example.common.ui.component.ListOfItems
import com.example.common.ui.component.ListWrapper
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.core.database.entity.Category
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType

@Composable
fun EditCategoryScreen(onDismiss: () -> Unit, vm: CategoryViewModel = hiltViewModel()) {
    val category by vm.category.collectAsStateWithLifecycle()

    EditCategoryContent(
        category = category,
        onSave = { vm.updateCategoryBudget(it); onDismiss() }
    )
}

@Composable
fun EditCategoryContent(
    category: Category?,
    onSave: (Long?) -> Unit
) {
    ScreenScaffold(
        "Edit Category",
        isLoading = category == null
    ) { paddingValues ->
        if (category == null) throw IllegalStateException("Category cannot be null when loading is false")

        val name by remember { mutableStateOf(category.name) }
        val type by remember { mutableStateOf(category.type) }
        var budget by remember { mutableStateOf(category.budgetPerCycle?.toDouble()?.div(100.0)?.toString()) }

        ListWrapper(paddingValues) {
            ListOfItems {
                ItemAndDivider(true) {
                    MyText.RowBody("Category Name")
                    MyText.RowHeader(name)
                }

                ItemAndDivider {
                    MyText.RowBody("Category Type")
                    MyText.RowHeader(
                        type.display(),
                        color = if (type == CategoryType.EXPENSE) Color.Red else Color.Green
                    )
                }

                ItemAndDivider {
                    MyText.RowBody("Enable Budget Per Cycle")
                    MyInput.Switch(
                        checked = budget != null,
                        onCheckedChange = { budget = if (it) "0.0" else null }
                    )
                }
            }

            MyInput.TextField(
                value = budget ?: "",
                onValueChange = { budget = it },
                label = "Budget Per Cycle",
                enabled = budget != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            MyInput.Button(
                text = "Save",
                onClick = { onSave(budget?.toDoubleOrNull()?.times(100)?.toLong()) },
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun EditCategoryScreenPreview() {
    FinancesTheme {
        EditCategoryContent(
            category = Category(
                id = 1,
                name = "Food",
                type = CategoryType.EXPENSE,
                budgetPerCycle = 5000L,
                color = DefaultColors.ORANGE.hexValue,
                icon = CategoryIcon.FOOD
            ),
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditCategoryScreenPreviewNoBudget() {
    FinancesTheme(true) {
        EditCategoryContent(
            category = Category(
                id = 1,
                name = "Food",
                type = CategoryType.EXPENSE,
                budgetPerCycle = null,
                color = DefaultColors.ORANGE.hexValue,
                icon = CategoryIcon.FOOD
            ),
            onSave = {}
        )
    }
}