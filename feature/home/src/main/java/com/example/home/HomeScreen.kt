package com.example.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme

@Composable
fun HomeScreen(vm: HomeViewModel = hiltViewModel()) {
    HomeContent()
}

@Composable
fun HomeContent() {
    ScreenScaffold("Home") { paddingValues ->

    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    FinancesTheme {
        HomeContent()
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreviewDark() {
    FinancesTheme(true) {
        HomeContent()
    }
}