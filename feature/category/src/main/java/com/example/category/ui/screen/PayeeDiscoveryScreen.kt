package com.example.category.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.category.ui.viewmodel.PayeeDiscoveryViewModel
import com.example.common.ui.component.IconAndRow
import com.example.common.ui.component.LazyListOfItems
import com.example.common.ui.component.ListWrapper
import com.example.common.ui.component.PickerDialog
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.core.database.entity.Category
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import com.example.core.database.models.TransactionType
import com.example.core.database.projection.PayeeSummary

/**
 * Lets the user categorize every uncategorized payee seen in the current spending cycle.
 * Tapping a payee opens a category picker; selecting a category remembers it and retroactively
 * re-tags every matching transaction, dropping the payee off this list.
 *
 * @param onFinish invoked when the user taps the "Skip" button in the top bar (only shown when
 * [fromOnboarding] is true, since a manual visit from the Categories screen can just be backed
 * out of instead), or automatically as soon as there are no uncategorized payees left (e.g.
 * everything was already categorized).
 * @param fromOnboarding whether this screen is being shown as part of initial SMS-import
 * onboarding, right after account-discovery — controls whether the "Skip" button is shown.
 */
@Composable
fun PayeeDiscoveryScreen(
    onFinish: () -> Unit,
    fromOnboarding: Boolean,
    vm: PayeeDiscoveryViewModel = hiltViewModel()
) {
    val payees by vm.payees.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()

    // `null` means the list hasn't loaded yet; only a loaded, empty list means there is
    // genuinely nothing left to categorize, so only then should we skip straight to onFinish.
    LaunchedEffect(payees) {
        if (payees?.isEmpty() == true) {
            onFinish()
        }
    }

    PayeeDiscoveryContent(
        payees = payees.orEmpty(),
        categories = categories,
        onAssignCategory = vm::assignCategory,
        onFinish = onFinish,
        fromOnboarding = fromOnboarding
    )
}

@Composable
fun PayeeDiscoveryContent(
    payees: List<PayeeSummary>,
    categories: List<Category>,
    onAssignCategory: (PayeeSummary, Int) -> Unit,
    onFinish: () -> Unit,
    fromOnboarding: Boolean
) {
    ScreenScaffold(
        title = "Categorize Payees",
        icon = {
            if (fromOnboarding) {
                MyInput.SmallButton(text = "Skip", onClick = onFinish)
            }
        }
    ) { paddingValues ->
        ListWrapper(paddingValues) {
            LazyListOfItems(payees, "Uncategorized Payees") { payee ->
                var showPicker by remember(payee.payee, payee.transactionType) { mutableStateOf(false) }

                // Every category is offered regardless of the payee's transaction type: a
                // CREDIT can be a refund against any expense category (e.g. a Zomato refund is
                // still Food), not just an income category.
                PickerDialog(
                    title = "Select Category",
                    show = showPicker,
                    items = categories,
                    onDismiss = { showPicker = false },
                    onItemSelected = { category -> onAssignCategory(payee, category.id) },
                    itemContent = { category ->
                        IconAndRow(category.icon.imageVector, category.color) {
                            MyText.RowHeader(category.name)
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPicker = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        MyText.RowHeader(payee.payee)
                        MyText.RowBody("${payee.transactionCount} transactions")
                    }
                    MyText.TransactionAmount(payee.totalAmount, payee.transactionType)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PayeeDiscoveryContentPreview() {
    FinancesTheme {
        PayeeDiscoveryContent(
            payees = previewPayees(),
            categories = previewCategories(),
            onAssignCategory = { _, _ -> },
            onFinish = {},
            fromOnboarding = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PayeeDiscoveryContentPreviewDark() {
    FinancesTheme(darkTheme = true) {
        PayeeDiscoveryContent(
            payees = previewPayees(),
            categories = previewCategories(),
            onAssignCategory = { _, _ -> },
            onFinish = {},
            fromOnboarding = true
        )
    }
}

private fun previewPayees() = listOf(
    PayeeSummary("zomato", TransactionType.DEBIT, 5, 125000L, 1723190400L),
    PayeeSummary("arjun rao", TransactionType.CREDIT, 2, 500000L, 1723194000L)
)

private fun previewCategories() = listOf(
    Category(id = 1, name = "Food", type = CategoryType.EXPENSE, budgetPerCycle = null, color = null, icon = CategoryIcon.FOOD),
    Category(id = 2, name = "Salary", type = CategoryType.INCOME, budgetPerCycle = null, color = null, icon = CategoryIcon.SALARY)
)
