package com.example.common.repository

import com.example.core.database.dao.CategoryDAO
import jakarta.inject.Inject

class CategoryRepository @Inject constructor(
    private val dao: CategoryDAO
) {
    val categories = dao.getAllCategoriesFlow()
}