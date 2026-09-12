package com.example.category.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Backs the single-category edit screen, identified by the `id` saved-state argument. */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repo: CategoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val categoryId: Int = checkNotNull(savedStateHandle["id"])

    /** The category being edited, kept warm for 5s after the last subscriber unsubscribes. */
    val category = repo.getCategoryById(categoryId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    /**
     * Updates the per-cycle budget of the current category.
     *
     * @param budget the new budget per cycle, or `null` to clear it.
     */
    fun updateCategoryBudget(budget: Long?) {
        viewModelScope.launch {
            repo.updateCategoryBudget(categoryId, budget)
        }
    }
}