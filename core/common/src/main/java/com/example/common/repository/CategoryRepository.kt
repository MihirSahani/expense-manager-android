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

class CategoryRepository @Inject constructor(
    private val dao: CategoryDAO,
    private val setting: Setting
) {
    val categories = dao.getAllCategoriesFlow()

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

    fun getCategoryById(id: Int) = dao.getCategoryFlow(id)

    suspend fun updateCategoryBudget(id: Int, budget: Long?) = dao.updateBudget(id, budget)

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

    private fun getCategoriesWithInfoForCurrentCycle(salaryCreditTime: Long): Flow<List<CategoryWithInfo>> {
        return dao.getCategoriesWithInfoFlow(start = salaryCreditTime)
    }
}