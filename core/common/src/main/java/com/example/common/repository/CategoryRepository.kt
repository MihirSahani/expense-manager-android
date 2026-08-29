package com.example.common.repository

import com.example.core.database.dao.CategoryDAO
import com.example.core.database.projection.CategoryWithInfo
import com.example.datastore.Setting
import com.example.datastore.model.CycleType
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import java.time.YearMonth
import java.time.ZoneId

/**
 * Mediates access to [Category] data, combining it with per-cycle spend/budget aggregates that
 * depend on the user's chosen cycle type ([CycleType]).
 */
class CategoryRepository @Inject constructor(
    private val dao: CategoryDAO,
    private val setting: Setting
) {
    /** Observes every category. */
    val categories = dao.getAllCategoriesFlow()

    /**
     * Observes every category with its remaining budget and amount spent for the current cycle,
     * switching reactively between monthly and salary-date windows based on [Setting.cycleType].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val getCategoriesWithInfo: Flow<List<CategoryWithInfo>> =
        setting.cycleType.flatMapLatest { cycleType ->
            when(cycleType) {
                CycleType.MONTHLY -> getCategoriesWithInfoForCurrentMonth()
                CycleType.SALARY_DATE -> setting.salaryCreditTime.flatMapLatest { salaryCreditTime ->
                    getCategoriesWithInfoForCurrentCycle(salaryCreditTime)
                }
            }
        }

    /**
     * Observes a single category by id.
     *
     * @param id the category id.
     * @return a [Flow] emitting the matching category, or `null` if it doesn't exist.
     */
    fun getCategoryById(id: Int) = dao.getCategoryFlow(id)

    /**
     * Updates only the per-cycle budget of a category.
     *
     * @param id the category id.
     * @param budget the new budget per cycle, or `null` to clear it.
     */
    suspend fun updateCategoryBudget(id: Int, budget: Long?) = dao.updateBudget(id, budget)

    /** Observes categories with info scoped to the current calendar month. */
    private fun getCategoriesWithInfoForCurrentMonth(): Flow<List<CategoryWithInfo>> {
        val start = YearMonth.now()
            .atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond()
        val end = YearMonth.now()
            .atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toEpochSecond()

        return dao.getCategoriesWithInfoFlow(start, end)
    }

    /**
     * Observes categories with info scoped to the current salary-date cycle.
     *
     * @param salaryCreditTime the epoch-second start of the current cycle.
     */
    private fun getCategoriesWithInfoForCurrentCycle(salaryCreditTime: Long): Flow<List<CategoryWithInfo>> {
        return dao.getCategoriesWithInfoFlow(start = salaryCreditTime)
    }
}