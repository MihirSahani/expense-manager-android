package com.example.analysis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.ui.component.ListWrapper
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.display
import com.example.core.database.entity.Category
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import com.example.core.database.projection.CategoryWithInfo
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel
import com.patrykandpatrick.vico.compose.pie.rememberPieChart

@Composable
fun AnalysisScreen(vm: AnalysisViewModel = hiltViewModel()) {
    val state by vm.analysisState.collectAsStateWithLifecycle()

    AnalysisContent(state)
}

@Composable
fun AnalysisContent(state: AnalysisUiState) {
    ScreenScaffold("Analysis") { paddingValues ->
        ListWrapper(paddingValues, scrollable = true) {
            SpendingDonutChart(state)

            Text(
                text = "Budget Utilization",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            BudgetGroupCard(state.necessities, MaterialTheme.colorScheme.primary)
            BudgetGroupCard(state.disposables, MaterialTheme.colorScheme.secondary)
            BudgetGroupCard(state.investments, MaterialTheme.colorScheme.tertiary)
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SpendingDonutChart(state: AnalysisUiState) {
    val necessitiesColor = MaterialTheme.colorScheme.primary
    val disposablesColor = MaterialTheme.colorScheme.secondary
    val investmentsColor = MaterialTheme.colorScheme.tertiary

    val pieChart = rememberPieChart(
        sliceProvider = PieChart.SliceProvider.series(
            listOf(
                PieChart.Slice(fill = Fill(necessitiesColor)),
                PieChart.Slice(fill = Fill(disposablesColor)),
                PieChart.Slice(fill = Fill(investmentsColor))
            )
        ),
        innerSize = PieSize.Inner.fixed(80.dp),
        spacing = 4.dp
    )

    val model = remember(state) {
        PieChartModel.build(
            state.necessities.spent.toFloat(),
            state.disposables.spent.toFloat(),
            state.investments.spent.toFloat()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PieChartHost(
            modifier = Modifier.size(240.dp),
            chart = pieChart,
            model = model
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Total Spent",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "₹ ${state.totalSpent.display()}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        DonutLegend(
            listOf(
                LegendItem("Necessities", necessitiesColor, state.necessities.spent),
                LegendItem("Disposables", disposablesColor, state.disposables.spent),
                LegendItem("Investments", investmentsColor, state.investments.spent)
            )
        )
    }
}

data class LegendItem(val label: String, val color: Color, val amount: Long)

@Composable
fun DonutLegend(items: List<LegendItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(item.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = item.label, style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "₹ ${item.amount.display() ?: "0"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetGroupCard(group: AnalysisGroup, color: Color) {
    var expanded by remember { mutableStateOf(false) }
    val progress = if (group.budget > 0) group.spent.toFloat() / group.budget.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val text = "₹ ${group.spent.display() ?: "0"} ${if (group.budget != 0L) "of ₹ ${group.budget.display() ?: "0"} budget" else ""}"
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (progress > 1f) MaterialTheme.colorScheme.error else color,
                trackColor = color.copy(alpha = 0.2f)
            )

            if (progress > 1f) {
                Text(
                    text = "Overspent by $${group.spent - group.budget}!",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    group.categories.forEach { category ->
                        CategoryBreakdownItem(category)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownItem(category: CategoryWithInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = category.category.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.category.icon.imageVector,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = category.category.name, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = "₹ ${category.spent.display() ?: "0"}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnalysisPreview() {
    FinancesTheme {
        AnalysisContent(dummyAnalysisState)
    }
}


@Preview(showBackground = true)
@Composable
fun AnalysisPreviewDark() {
    FinancesTheme(true) {
        AnalysisContent(dummyAnalysisState)
    }
}

private val dummyAnalysisState = AnalysisUiState(
    necessities = AnalysisGroup(
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
    ),
    disposables = AnalysisGroup(
        name = "Disposables",
        spent = 150,
        budget = 200,
        categories = listOf(
            CategoryWithInfo(
                category = Category(3, "Shopping", CategoryType.EXPENSE, 200, null, CategoryIcon.SHOPPING),
                remainingBalance = 50,
                spent = 150
            )
        )
    ),
    investments = AnalysisGroup(
        name = "Investments",
        spent = 500,
        budget = 500,
        categories = listOf(
            CategoryWithInfo(
                category = Category(4, "Investment", CategoryType.EXPENSE, 500, null, CategoryIcon.INVESTMENT),
                remainingBalance = 0,
                spent = 500
            )
        )
    ),
    totalSpent = 1100
)
