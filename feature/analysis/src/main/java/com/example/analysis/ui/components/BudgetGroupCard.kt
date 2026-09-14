package com.example.analysis.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analysis.ui.viewmodel.AnalysisGroup
import com.example.common.ui.component.IconAndRow
import com.example.common.ui.component.SingleRowItem
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText
import com.example.core.database.entity.Category
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import com.example.core.database.projection.CategoryWithInfo
import com.example.core.database.models.DefaultColors

@Composable
fun BudgetGroupCard(group: AnalysisGroup, color: Color, onCategoryClick: (Int) -> Unit = {}) {
    var expanded by remember { mutableStateOf(true) }
    val progress = if (group.budget > 0) group.spent.toFloat() / group.budget.toFloat() else 0f

    SingleRowItem(
        Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    MyText.RowHeader(group.name)
                    if (group.allCategoriesBudgeted) {
                        MyText.RowBody("Budget spent")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MyText.TransactionAmount(
                        amount = group.spent,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            if (group.allCategoriesBudgeted) {
                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress.coerceAtMost(1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (progress > 1f) MaterialTheme.colorScheme.error else color,
                    trackColor = color.copy(alpha = 0.2f),
                    gapSize = 0.dp
                )

                if (progress > 1f) {
                    MyText.RowBody(
                        text = "Overspent!",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))

                    group.categories.forEach { category ->
                        CategoryBreakdownItem(category, onCategoryClick)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownItem(category: CategoryWithInfo, onCategoryClick: (Int) -> Unit = {}) {
    val budget = category.category.budgetPerCycle
    val progress = if (budget != null && budget > 0) (category.spent ?: 0L).toFloat() / budget.toFloat() else null
    val categoryColor = Color(category.category.color ?: DefaultColors.GRAY.hexValue)

    IconAndRow(
        icon = category.category.icon.imageVector,
        bgColor = category.category.color,
        modifier = Modifier.clickable { onCategoryClick(category.category.id) }
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MyText.RowHeader(category.category.name)
                MyText.TransactionAmount(
                    amount = category.spent ?: 0L,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (progress != null) {
                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress.coerceAtMost(1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = if (progress > 1f) MaterialTheme.colorScheme.error else categoryColor,
                    trackColor = categoryColor.copy(alpha = 0.2f),
                    gapSize = 0.dp
                )
            }
        }
    }
}

private val dummyBudgetGroup = AnalysisGroup(
    name = "Necessities",
    spent = 450,
    budget = 500,
    categories = listOf(
        CategoryWithInfo(
            category = Category(1, "Groceries", CategoryType.EXPENSE, 300, null, CategoryIcon.FOOD),
            remainingBalance = 50,
            spent = 250
        ),
        CategoryWithInfo(
            category = Category(2, "Utilities", CategoryType.EXPENSE, 200, null, CategoryIcon.UTILITIES),
            remainingBalance = 0,
            spent = 200
        )
    )
)

private val dummyMixedBudgetGroup = AnalysisGroup(
    name = "Disposables",
    spent = 350,
    budget = 200,
    categories = listOf(
        CategoryWithInfo(
            category = Category(3, "Shopping", CategoryType.EXPENSE, 200, null, CategoryIcon.SHOPPING),
            remainingBalance = 50,
            spent = 150
        ),
        CategoryWithInfo(
            // No budget set for this category, so the group bar is hidden while this
            // category's own row also shows no bar.
            category = Category(4, "Friends & Family", CategoryType.EXPENSE, null, null, CategoryIcon.SHOPPING),
            remainingBalance = null,
            spent = 200
        )
    )
)

@Preview(showBackground = true)
@Composable
fun BudgetGroupCardPreview() {
    FinancesTheme {
        Column {
            BudgetGroupCard(dummyBudgetGroup, MaterialTheme.colorScheme.primary)
            BudgetGroupCard(dummyMixedBudgetGroup, MaterialTheme.colorScheme.secondary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetGroupCardPreviewDark() {
    FinancesTheme(true) {
        Column {
            BudgetGroupCard(dummyBudgetGroup, MaterialTheme.colorScheme.primary)
            BudgetGroupCard(dummyMixedBudgetGroup, MaterialTheme.colorScheme.secondary)
        }
    }
}