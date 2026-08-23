package com.example.common.model

import com.example.core.database.projection.DailyExpense

data class CycleDailyExpenses(
    val start: Long,
    val end: Long,
    val expenses: List<DailyExpense>
)

