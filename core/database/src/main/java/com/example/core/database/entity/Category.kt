package com.example.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType

@Entity(
    tableName="categories",
    indices = [
        Index(value = ["name", "type"], unique = true),
        Index(value = ["icon", "color"], unique = true)
    ]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("type")
    val type: CategoryType,
    @ColumnInfo("budget_per_cycle")
    val budgetPerCycle: Long?,
    @ColumnInfo("color")
    val color: Int?,
    @ColumnInfo("icon")
    val icon: CategoryIcon
)