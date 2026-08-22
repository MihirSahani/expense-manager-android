package com.example.core.database.projection

import androidx.room3.ColumnInfo

data class DailyExpense(
    @ColumnInfo(name = "epoch_day")
    val epochDay: Long,
    @ColumnInfo(name = "spent")
    val spent: Long
)
