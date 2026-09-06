package com.example.analysis.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.analysis.ui.components.BudgetGroupCard
import com.example.analysis.ui.components.SpendingDonutChart
import com.example.analysis.ui.components.SpendingHeatmap
import com.example.analysis.ui.viewmodel.AnalysisGroup
import com.example.analysis.ui.viewmodel.AnalysisUiState
import com.example.analysis.ui.viewmodel.AnalysisViewModel
import com.example.analysis.ui.viewmodel.HeatmapDay
import com.example.common.ui.component.IconAndRow
import com.example.common.ui.component.ListWrapper
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.component.SingleRowItem
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

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

            MyText.SecondaryHeader("Budget Utilization")

            BudgetGroupCard(state.necessities, MaterialTheme.colorScheme.primary)
            BudgetGroupCard(state.disposables, MaterialTheme.colorScheme.secondary)
            BudgetGroupCard(state.investments, MaterialTheme.colorScheme.tertiary)

            SpendingHeatmap(state.heatmap, state.cycleStart, state.cycleEnd)
        }
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
                category = Category(
                    1,
                    "Groceries",
                    CategoryType.EXPENSE,
                    300,
                    null,
                    CategoryIcon.FOOD
                ),
                remainingBalance = 50,
                spent = 250
            ),
            CategoryWithInfo(
                category = Category(
                    2,
                    "Utilities",
                    CategoryType.EXPENSE,
                    200,
                    null,
                    CategoryIcon.UTILITIES
                ),
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
                category = Category(
                    3,
                    "Shopping",
                    CategoryType.EXPENSE,
                    200,
                    null,
                    CategoryIcon.SHOPPING
                ),
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
                category = Category(
                    4,
                    "Investment",
                    CategoryType.EXPENSE,
                    500,
                    null,
                    CategoryIcon.INVESTMENT
                ),
                remainingBalance = 0,
                spent = 500
            )
        )
    ),
    totalSpent = 1100,
    heatmap = (0..25L).map {
        HeatmapDay(
            date = LocalDate.now().minusDays(it),
            spent = ((it * 37) % 9) * 15000L - if (it % 11 == 0L) 30000L else 0L
        )
    },
    cycleStart = LocalDate.now().withDayOfMonth(1),
    cycleEnd = LocalDate.now()
)
