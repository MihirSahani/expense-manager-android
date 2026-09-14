package com.example.analysis.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class SpendingHeatMapTest {

    @Test
    fun `cell size grows to fill leftover width instead of the gap`() {
        // availableWidth 100dp, dayLabelWidth 14dp -> rowWidth 86dp, 2 columns, 3dp gap:
        // (86 - 3) / 2 = 41.5dp
        val cellSize = calculateHeatmapCellSize(availableWidth = 100.dp, weekCount = 2)

        assertEquals(41.5.dp, cellSize)
    }

    @Test
    fun `cell size is floored so cells never become illegibly small`() {
        val cellSize = calculateHeatmapCellSize(availableWidth = 40.dp, weekCount = 7)

        assertEquals(12.dp, cellSize)
    }
}
