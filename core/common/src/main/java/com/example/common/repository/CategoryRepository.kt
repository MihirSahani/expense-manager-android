package com.example.common.repository

import com.example.core.database.dao.CategoryDAO
import com.example.core.database.entity.Category
import com.example.core.database.projection.CategoryWithRemainingBalance
import com.example.datastore.Setting
import com.example.datastore.model.CycleType
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import java.time.YearMonth
import java.time.ZoneId

class CategoryRepository @Inject constructor(
    private val dao: CategoryDAO,
    private val setting: Setting
) {
    val categories = dao.getAllCategoriesFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val getCategoriesWithRemainingBalance: Flow<List<CategoryWithRemainingBalance>> =
        setting.cycleType.flatMapLatest { cycleType ->
            when(cycleType) {
                CycleType.MONTHLY -> getCategoriesWithRemainingBalanceForCurrentMonth()
                CycleType.SALARY_DATE -> setting.salaryCreditTime.flatMapLatest { salaryCreditTime ->
                    getCategoriesWithRemainingBalanceForCurrentCycle(salaryCreditTime)
                }
            }
        }

    fun getCategoryById(id: Int) = dao.getCategoryFlow(id)

    suspend fun updateCategoryBudget(id: Int, budget: Long?) = dao.updateBudget(id, budget)

    private fun getCategoriesWithRemainingBalanceForCurrentMonth(): Flow<List<CategoryWithRemainingBalance>> {
        val start = YearMonth.now()
            .atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond()
        val end = YearMonth.now()
            .atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toEpochSecond()

        return dao.getCategoriesWithRemainingBalanceFlow(start, end)
    }

    private fun getCategoriesWithRemainingBalanceForCurrentCycle(salaryCreditTime: Long): Flow<List<CategoryWithRemainingBalance>> {
        return dao.getCategoriesWithRemainingBalanceFlow(start = salaryCreditTime)
    }
}