package com.example.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.CategoryRepository
import com.example.core.database.projection.CategoryWithInfo
import com.example.core.database.models.CategoryType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AnalysisGroup(
    val name: String,
    val spent: Long,
    val budget: Long,
    val categories: List<CategoryWithInfo>
)

data class AnalysisUiState(
    val necessities: AnalysisGroup,
    val disposables: AnalysisGroup,
    val investments: AnalysisGroup,
    val totalSpent: Long
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(repo: CategoryRepository): ViewModel() {
    private val necessitiesNames = listOf("Groceries", "Utilities", "Transport")
    private val investmentNames = listOf("Investment")

    val analysisState = repo.getCategoriesWithInfo
        .map { allCategories ->
            val categories = allCategories.filter { it.category.type == CategoryType.EXPENSE }

            val necessities = categories.filter { it.category.name in necessitiesNames }
            val investments = categories.filter { it.category.name in investmentNames }
            val disposables = categories.filter { 
                it.category.name !in necessitiesNames && it.category.name !in investmentNames 
            }

            val necessitiesGroup = AnalysisGroup(
                name = "Necessities",
                spent = necessities.sumOf { it.spent ?: 0L },
                budget = necessities.sumOf { it.category.budgetPerCycle ?: 0L },
                categories = necessities
            )

            val disposablesGroup = AnalysisGroup(
                name = "Disposables",
                spent = disposables.sumOf { it.spent ?: 0L },
                budget = disposables.sumOf { it.category.budgetPerCycle ?: 0L },
                categories = disposables
            )

            val investmentsGroup = AnalysisGroup(
                name = "Investments",
                spent = investments.sumOf { it.spent ?: 0L },
                budget = investments.sumOf { it.category.budgetPerCycle ?: 0L },
                categories = investments
            )

            AnalysisUiState(
                necessities = necessitiesGroup,
                disposables = disposablesGroup,
                investments = investmentsGroup,
                totalSpent = necessitiesGroup.spent + disposablesGroup.spent + investmentsGroup.spent
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AnalysisUiState(
                AnalysisGroup("Necessities", 0, 0, emptyList()),
                AnalysisGroup("Disposables", 0, 0, emptyList()),
                AnalysisGroup("Investments", 0, 0, emptyList()),
                0
            )
        )
}
