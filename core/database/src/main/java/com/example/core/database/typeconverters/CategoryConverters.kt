package com.example.core.database.typeconverters

import androidx.room3.ColumnTypeConverter
import com.example.core.database.models.CategoryIcon

class CategoryConverters {
    @ColumnTypeConverter
    fun fromCategoryIcons(categoryIcons: CategoryIcon?): String? = categoryIcons?.name

    @ColumnTypeConverter
    fun toCategoryIcons(value: String): CategoryIcon = CategoryIcon.valueOf(value)
}