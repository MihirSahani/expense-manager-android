package com.example.core.database.typeconverters

import androidx.room3.ColumnTypeConverter
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType

class CategoryConverters {
    @ColumnTypeConverter
    fun fromCategoryIcons(categoryIcons: CategoryIcon?): String? = categoryIcons?.name

    @ColumnTypeConverter
    fun toCategoryIcons(value: String): CategoryIcon = CategoryIcon.valueOf(value)

    @ColumnTypeConverter
    fun fromCategoryType(categoryType: CategoryType?): String? = categoryType?.name

    @ColumnTypeConverter
    fun toCategoryType(value: String): CategoryType = CategoryType.valueOf(value)
}