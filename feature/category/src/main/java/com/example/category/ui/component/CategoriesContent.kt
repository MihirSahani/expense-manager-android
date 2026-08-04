package com.example.category.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.common.ui.component.LazyListOfItems
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.core.database.entity.Category
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import com.example.core.database.projection.CategoryWithRemainingBalance

@Composable
fun CategoriesContent(
    categoriesWithRemainingBalance: List<CategoryWithRemainingBalance>,
    onCategoryClick: (Category) -> Unit,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = padding.calculateTopPadding())
            .padding(horizontal = 16.dp)
    ) {
        LazyListOfItems(categoriesWithRemainingBalance) { categoryWithRemainingBalance ->
            CategoryItem(
                categoryWithRemainingBalance,
                { onCategoryClick(categoryWithRemainingBalance.category) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoriesContentPreview() {
    val sampleCategories = listOf(
        CategoryWithRemainingBalance(Category(id = 1, name = "Food", type = CategoryType.EXPENSE, budgetPerCycle = null, color = 0xFFFF6200.toInt(), icon = CategoryIcon.FOOD), 1000_00L),
        CategoryWithRemainingBalance(Category(id = 2, name = "Transport", type = CategoryType.EXPENSE, budgetPerCycle = 50000L, color = 0xFFFF6200.toInt(), icon = CategoryIcon.TRANSPORT), 1000_00L),
        CategoryWithRemainingBalance(Category(id = 3, name = "Shopping", type = CategoryType.EXPENSE, budgetPerCycle = 75000L, color = 0xFFFF6200.toInt(), icon = CategoryIcon.SHOPPING), 1000_00L),
        CategoryWithRemainingBalance(Category(id = 4, name = "Groceries", type = CategoryType.EXPENSE, budgetPerCycle = null, color = 0xFFFF6200.toInt(), icon = CategoryIcon.GROCERIES), 1000_00L),
        CategoryWithRemainingBalance(Category(id = 5, name = "Utilities", type = CategoryType.EXPENSE, budgetPerCycle = 80000L, color = 0xFFFF6200.toInt(), icon = CategoryIcon.UTILITIES), 1000_00L)
    )
    FinancesTheme(darkTheme = true) {
        ScreenScaffold("Categories") { padding ->
            CategoriesContent(
                categoriesWithRemainingBalance = sampleCategories,
                onCategoryClick = {},
                padding = padding
            )
        }
    }
}