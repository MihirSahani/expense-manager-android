package com.example.category.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.category.ui.component.CategoriesContent
import com.example.category.ui.viewmodel.CategoriesViewModel
import com.example.common.ui.component.ScreenScaffold
import com.example.core.database.entity.Category

@Composable
fun CategoriesScreen(
    onCategoryClick: (Category) -> Unit,
) {
    val viewmodel: CategoriesViewModel = hiltViewModel()
    val categoriesWithRemainingBalance by viewmodel.categoriesWithRemainingBalance.collectAsStateWithLifecycle(emptyList())

    ScreenScaffold(
        title = "Categories",
    ) { padding ->
        CategoriesContent(
            categoriesWithRemainingBalance = categoriesWithRemainingBalance,
            onCategoryClick = { category -> onCategoryClick(category) },
            padding = padding
        )
    }
}