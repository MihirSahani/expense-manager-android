package com.example.analysis.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.analysis.ui.viewmodel.AnalysisUiState

/** Lays out [SpendingDonutChart] and [SpendingHeatmap] side by side in a single row. */
@Composable
fun AnalysisTopSection(state: AnalysisUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SpendingDonutChart(state, modifier = Modifier.weight(0.5f))
        SpendingHeatmap(state.heatmap, state.cycleStart, state.cycleEnd, modifier = Modifier.weight(0.5f))
    }
}
