package com.example.core.database.projection

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import com.example.core.database.entity.Category

data class CategoryWithInfo(
    @Embedded
    val category: Category,
    @ColumnInfo(name = "remaining_balance")
    val remainingBalance: Long?,
    @ColumnInfo(name = "spent")
    val spent: Long?
)