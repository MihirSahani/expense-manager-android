package com.example.category.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.category.ui.component.UpdateCategoryContent
import com.example.category.ui.viewmodel.CategoryViewModel
import com.example.common.ui.component.ScreenScaffold

@Composable
fun EditCategoryScreen(vm: CategoryViewModel) {
    val category by vm.category.collectAsStateWithLifecycle()

    ScreenScaffold(
        "Edit Category",
        isLoading = category == null
    ) { paddingValues ->
        UpdateCategoryContent(
            category!!,
            // Safe to use !! because isLoading will prevent this from being called when category is null
            { vm.updateCategoryBudget(it) },
            padding = paddingValues
        )
    }
}