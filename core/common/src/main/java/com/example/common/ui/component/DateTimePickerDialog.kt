package com.example.common.ui.component

import android.widget.NumberPicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.example.common.utils.MyText
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

@Composable
fun DateTimePickerDialog(
    showDialog: Boolean,
    title: String,
    initialEpochSeconds: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    yearRange: IntRange = 2000..2100,
) {
    if (!showDialog) return

    val zoneId = remember { ZoneId.systemDefault() }
    val initialDateTime = remember(initialEpochSeconds, zoneId) {
        Instant.ofEpochSecond(initialEpochSeconds).atZone(zoneId).toLocalDateTime()
    }

    var year by remember(initialEpochSeconds) {
        mutableIntStateOf(initialDateTime.year.coerceIn(yearRange.first, yearRange.last))
    }
    var month by remember(initialEpochSeconds) { mutableIntStateOf(initialDateTime.monthValue) }
    var day by remember(initialEpochSeconds) { mutableIntStateOf(initialDateTime.dayOfMonth) }
    var hour by remember(initialEpochSeconds) { mutableIntStateOf(initialDateTime.hour) }
    var minute by remember(initialEpochSeconds) { mutableIntStateOf(initialDateTime.minute) }

    val maxDay = remember(year, month) { YearMonth.of(year, month).lengthOfMonth() }
    LaunchedEffect(maxDay) {
        if (day > maxDay) day = maxDay
    }

    val monthValues = remember { arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec") }
    val dayValues = remember(maxDay) { formattedValues(1..maxDay) }
    val hourValues = remember { formattedValues(0..23) }
    val minuteValues = remember { formattedValues(0..59) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { MyText.SecondaryHeader(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    WheelNumberPicker(
                        label = "Year",
                        value = year,
                        valueRange = yearRange,
                        onValueChange = { year = it },
                    )
                    WheelNumberPicker(
                        label = "Month",
                        value = month,
                        valueRange = 1..12,
                        displayedValues = monthValues,
                        onValueChange = { month = it },
                    )
                    WheelNumberPicker(
                        label = "Day",
                        value = day,
                        valueRange = 1..maxDay,
                        displayedValues = dayValues,
                        onValueChange = { day = it },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                ) {
                    WheelNumberPicker(
                        label = "Hour",
                        value = hour,
                        valueRange = 0..23,
                        displayedValues = hourValues,
                        onValueChange = { hour = it },
                    )
                    WheelNumberPicker(
                        label = "Minute",
                        value = minute,
                        valueRange = 0..59,
                        displayedValues = minuteValues,
                        onValueChange = { minute = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = LocalDateTime.of(year, month, day, hour, minute)
                        .atZone(zoneId)
                        .toEpochSecond()
                    onConfirm(selected)
                }
            ) {
                MyText.RowHeader("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                MyText.RowHeader("Cancel")
            }
        },
    )
}

@Composable
private fun WheelNumberPicker(
    label: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
    displayedValues: Array<String>? = null,
) {
    // Keep the listener reading the latest callback without ever reassigning it on the
    // NumberPicker itself, since re-attaching a listener mid-gesture (e.g. while a tap's
    // increment animation or a swipe's fling is in progress) corrupts NumberPicker's internal
    // touch state machine, causing it to auto-repeat endlessly or briefly render two values.
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MyText.RowBody(label)
        AndroidView(
            modifier = Modifier.width(88.dp),
            factory = { context ->
                NumberPicker(context).apply {
                    descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                    wrapSelectorWheel = false
                    setOnValueChangedListener { _, _, newValue ->
                        currentOnValueChange(newValue)
                    }
                }
            },
            update = { picker ->
                val rangeChanged = picker.minValue != valueRange.first || picker.maxValue != valueRange.last
                if (rangeChanged) {
                    // displayedValues must be cleared before resizing the range, otherwise
                    // NumberPicker throws if the old array's size doesn't match the new range.
                    picker.displayedValues = null
                    picker.minValue = valueRange.first
                    picker.maxValue = valueRange.last
                }
                if (rangeChanged || !picker.displayedValues.contentEquals(displayedValues)) {
                    picker.displayedValues = displayedValues
                }
                if (picker.value != value) picker.value = value
            },
        )
    }
}

private fun formattedValues(range: IntRange): Array<String> {
    return range.map { String.format(Locale.getDefault(), "%02d", it) }.toTypedArray()
}
