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

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repo: CategoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val categoryId: Int = checkNotNull(savedStateHandle["id"])

    val category = repo.getCategoryById(categoryId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    fun updateCategoryBudget(budget: Long?) {
        viewModelScope.launch {
            repo.updateCategoryBudget(categoryId, budget)
        }
    }
}