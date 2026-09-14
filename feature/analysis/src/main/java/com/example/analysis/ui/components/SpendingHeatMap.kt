package com.example.analysis.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analysis.ui.viewmodel.HeatmapDay
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText
import com.example.common.utils.abbreviateAmount
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import androidx.compose.ui.platform.LocalLocale

private val dayLabelWidth = 14.dp
private val cellSpacing = 3.dp
private val headerHeight = 14.dp
private const val heatmapRows = 7
private val heatmapTitleSpacing = 6.dp

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SpendingHeatmap(days: List<HeatmapDay>, cycleStart: LocalDate, cycleEnd: LocalDate, modifier: Modifier = Modifier) {
    val spentByDate = remember(days) { days.associate { it.date to it.spent } }
    val maxSpent = remember(days) {
        days.maxOfOrNull { it.spent }?.coerceAtLeast(1L) ?: 1L
    }

    val today = remember { LocalDate.now() }
    val rangeEnd = remember(cycleEnd, today) { minOf(cycleEnd, today) }
    val rangeStart = remember(cycleStart) { cycleStart.with(DayOfWeek.MONDAY) }
    val totalDays = remember(rangeStart, rangeEnd) {
        (ChronoUnit.DAYS.between(rangeStart, rangeEnd).toInt() + 1).coerceAtLeast(1)
    }
    val weekColumns = remember(rangeStart, totalDays) {
        (0 until totalDays).map { rangeStart.plusDays(it.toLong()) }.chunked(heatmapRows)
    }

    val baseColor = MaterialTheme.colorScheme.primary
    val refundColor = MaterialTheme.colorScheme.tertiary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(heatmapTitleSpacing)) {
        MyText.SecondaryHeader("Daily Spending")

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cellSize = 24.dp
            val rowWidth = (cellSize * weekColumns.size) + (cellSpacing * (weekColumns.size - 1))

            Column {
                Row(modifier = Modifier.width(dayLabelWidth + rowWidth)) {
                    Spacer(modifier = Modifier.width(dayLabelWidth))
                    Row(modifier = Modifier.width(rowWidth), horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
                        weekColumns.forEachIndexed { index, week ->
                            val firstDay = week.first()
                            val showLabel = index == 0 ||
                                    firstDay.month != weekColumns[index - 1].first().month
                            Box(modifier = Modifier.width(cellSize).height(headerHeight), contentAlignment = Alignment.Center) {
                                if (showLabel) {
                                    MyText.RowBody(
                                        text = firstDay.month
                                            .getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale),
                                        fontSize = 8.sp,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(cellSpacing))
                Row {
                    Column(verticalArrangement = Arrangement.spacedBy(cellSpacing)) {
                        DayOfWeek.entries.forEach { dow ->
                            Box(Modifier.height(cellSize).width(dayLabelWidth), contentAlignment = Alignment.Center) {
                                if (dow.value % 2 == 1) {
                                    MyText.RowBody(
                                        text = dow.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale).take(1),
                                        fontSize = 8.sp,
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                }
                            }
                        }
                    }
                    Row(modifier = Modifier.width(rowWidth), horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
                        weekColumns.forEach { week ->
                            Column(verticalArrangement = Arrangement.spacedBy(cellSpacing)) {
                                for (dowIndex in 0 until 7) {
                                    val date = week.getOrNull(dowIndex)
                                    val inCycle = date != null &&
                                            !date.isBefore(cycleStart) && !date.isAfter(rangeEnd)
                                    HeatCell(
                                        spent = date?.let { spentByDate[it] },
                                        maxSpent = maxSpent,
                                        baseColor = baseColor,
                                        refundColor = refundColor,
                                        emptyColor = emptyColor,
                                        visible = inCycle,
                                        size = cellSize
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatCell(
    spent: Long?,
    maxSpent: Long,
    baseColor: Color,
    refundColor: Color,
    emptyColor: Color,
    visible: Boolean,
    size: Dp = 32.dp
) {
    if (!visible) {
        Spacer(modifier = Modifier.size(size))
        return
    }

    val intensity = when {
        spent == null || spent == 0L -> 0f
        spent < 0L -> -1f
        else -> (spent.toFloat() / maxSpent.toFloat()).coerceIn(0f, 1f)
    }

    val cellColor = when {
        intensity < 0f -> refundColor.copy(alpha = 0.5f)
        intensity == 0f -> emptyColor
        else -> baseColor.copy(alpha = 0.25f + 0.75f * intensity)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(cellColor),
        contentAlignment = Alignment.Center
    ) {
        if (spent != null && spent != 0L) {
            MyText.RowBody(
                text = spent.abbreviateAmount(),
                fontSize = 7.sp,
                color = if (intensity >= 0.5f) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 1.dp)
            )
        }
    }
}

private val dummyHeatmapDays = (0..25L).map {
    HeatmapDay(
        date = LocalDate.now().minusDays(it),
        spent = ((it * 37) % 9) * 15000L - if (it % 11 == 0L) 30000L else 0L
    )
}

@Preview(showBackground = true)
@Composable
fun SpendingHeatmapPreview() {
    FinancesTheme {
        Surface {
            SpendingHeatmap(
                days = dummyHeatmapDays,
                cycleStart = LocalDate.now().withDayOfMonth(1),
                cycleEnd = LocalDate.now()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpendingHeatmapPreviewDark() {
    FinancesTheme(true) {
        Surface {
            SpendingHeatmap(
                days = dummyHeatmapDays,
                cycleStart = LocalDate.now().withDayOfMonth(1),
                cycleEnd = LocalDate.now()
            )
        }
    }
}