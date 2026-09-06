package com.example.analysis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import kotlin.collections.first
import androidx.compose.ui.platform.LocalLocale

@Composable
fun SpendingHeatmap(days: List<HeatmapDay>, cycleStart: LocalDate, cycleEnd: LocalDate) {
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
        (0 until totalDays).map { rangeStart.plusDays(it.toLong()) }.chunked(7)
    }

    val baseColor = MaterialTheme.colorScheme.primary
    val refundColor = MaterialTheme.colorScheme.tertiary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MyText.SecondaryHeader("Daily Spending")

        val scrollState = rememberScrollState()

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Spacer(modifier = Modifier.width(20.dp))
                    weekColumns.forEachIndexed { index, week ->
                        val firstDay = week.first()
                        val showLabel = index == 0 ||
                                firstDay.month != weekColumns[index - 1].first().month
                        Box(modifier = Modifier.width(32.dp).height(16.dp), contentAlignment = Alignment.Center) {
                            if (showLabel) {
                                MyText.RowBody(
                                    text = firstDay.month
                                        .getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale),
                                    fontSize = 9.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DayOfWeek.entries.forEach { dow ->
                            Box(Modifier.height(32.dp).width(24.dp), contentAlignment = Alignment.Center) {
                                if (dow.value % 2 == 1) {
                                    MyText.RowBody(
                                        text = dow.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale).take(3),
                                        fontSize = 9.sp,
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                }
                            }
                        }
                    }
                    Row(Modifier.horizontalScroll(scrollState), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        weekColumns.forEach { week ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                                        visible = inCycle
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
                fontSize = 8.sp,
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