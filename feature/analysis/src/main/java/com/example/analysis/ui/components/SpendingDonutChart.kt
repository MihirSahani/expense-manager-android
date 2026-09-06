package com.example.analysis.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analysis.ui.viewmodel.AnalysisGroup
import com.example.analysis.ui.viewmodel.AnalysisUiState
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel
import com.patrykandpatrick.vico.compose.pie.rememberPieChart

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
        innerSize = PieSize.Inner.fixed(52.dp),
        spacing = 3.dp
    )

    val model = remember(state) {
        PieChartModel.build(
            state.necessities.spent.toFloat(),
            state.disposables.spent.toFloat(),
            state.investments.spent.toFloat()
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PieChartHost(
            modifier = Modifier.size(140.dp),
            chart = pieChart,
            model = model
        )

        Column(modifier = Modifier.weight(1f)) {
            MyText.RowBody("Total Spent")
            MyText.TransactionAmount(
                amount = state.totalSpent,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            DonutLegend(
                listOf(
                    LegendItem("Necessities", necessitiesColor, state.necessities.spent),
                    LegendItem("Disposables", disposablesColor, state.disposables.spent),
                    LegendItem("Investments", investmentsColor, state.investments.spent)
                )
            )
        }
    }
}

data class LegendItem(val label: String, val color: Color, val amount: Long)

@Composable
fun DonutLegend(items: List<LegendItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(item.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    MyText.RowBody(item.label)
                }
                MyText.TransactionAmount(
                    amount = item.amount,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private val dummyDonutState = AnalysisUiState(
    necessities = AnalysisGroup("Necessities", spent = 450, budget = 500, categories = emptyList()),
    disposables = AnalysisGroup("Disposables", spent = 150, budget = 200, categories = emptyList()),
    investments = AnalysisGroup("Investments", spent = 500, budget = 500, categories = emptyList()),
    totalSpent = 1100
)

@Preview(showBackground = true)
@Composable
fun SpendingDonutChartPreview() {
    FinancesTheme {
        SpendingDonutChart(dummyDonutState)
    }
}

@Preview(showBackground = true)
@Composable
fun SpendingDonutChartPreviewDark() {
    FinancesTheme(true) {
        SpendingDonutChart(dummyDonutState)
    }
}