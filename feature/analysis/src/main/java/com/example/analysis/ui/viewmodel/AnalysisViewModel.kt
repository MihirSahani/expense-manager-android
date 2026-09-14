package com.example.analysis.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.CategoryRepository
import com.example.common.repository.TransactionRepository
import com.example.core.database.projection.CategoryWithInfo
import com.example.core.database.models.CategoryType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** A budget-vs-spend summary for one of the three top-level analysis groups. */
data class AnalysisGroup(
    val name: String,
    val spent: Long,
    val budget: Long,
    val categories: List<CategoryWithInfo>
) {
    /** True only when every category in this group has a per-cycle budget set. */
    val allCategoriesBudgeted: Boolean =
        categories.isNotEmpty() && categories.all { it.category.budgetPerCycle != null }
}

/** A single day's total expense, used to render the analysis heatmap. */
data class HeatmapDay(
    val date: LocalDate,
    val spent: Long
)

/** UI state for the analysis screen: spend grouped by necessities/disposables/investments plus a spending heatmap. */
data class AnalysisUiState(
    val necessities: AnalysisGroup,
    val disposables: AnalysisGroup,
    val investments: AnalysisGroup,
    val totalSpent: Long,
    val heatmap: List<HeatmapDay> = emptyList(),
    val cycleStart: LocalDate = LocalDate.now().withDayOfMonth(1),
    val cycleEnd: LocalDate = LocalDate.now()
)

/**
 * Builds [AnalysisUiState] for the current cycle by combining category budget/spend info with
 * daily expense totals, grouping expense categories into necessities, disposables, and
 * investments.
 */
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    repo: CategoryRepository,
    transactionRepo: TransactionRepository
) : ViewModel() {
    private val necessitiesNames = listOf("Groceries", "Utilities", "Transport")
    private val investmentNames = listOf("Investment")

    /** Current-cycle analysis state, kept warm for 5s after the last subscriber unsubscribes. */
    val analysisState = combine(
        repo.getCategoriesWithInfo,
        transactionRepo.getCurrentCycleDailyExpenses()
    ) { allCategories, cycleExpenses ->
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

        val zone = ZoneId.systemDefault()
        AnalysisUiState(
            necessities = necessitiesGroup,
            disposables = disposablesGroup,
            investments = investmentsGroup,
            totalSpent = necessitiesGroup.spent + disposablesGroup.spent + investmentsGroup.spent,
            heatmap = cycleExpenses.expenses.map {
                HeatmapDay(LocalDate.ofEpochDay(it.epochDay), it.spent)
            },
            cycleStart = Instant.ofEpochSecond(cycleExpenses.start).atZone(zone).toLocalDate(),
            cycleEnd = Instant.ofEpochSecond(cycleExpenses.end).atZone(zone).toLocalDate()
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
