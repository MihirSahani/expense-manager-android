package com.example.common.repository

import com.example.core.database.dao.CategoryDAO
import com.example.core.database.entity.Category
import jakarta.inject.Inject

class CategoryRepository @Inject constructor(
    private val dao: CategoryDAO
) {
    val categories = dao.getAllCategoriesFlow()

    fun getCategoryById(id: Int) = dao.getCategoryFlow(id)

    suspend fun updateCategoryBudget(id: Int, budget: Long?) = dao.updateBudget(id, budget)
}