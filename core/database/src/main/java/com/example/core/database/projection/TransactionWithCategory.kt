package com.example.core.database.projection

import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.TransactionType

data class TransactionWithCategory(
    val id: Int,
    val payee: String,
    val amount: Long,
    val transactionType: TransactionType,
    val datetime: Long,
    val categoryIcon: CategoryIcon?,
    val categoryName: String?,
    val categoryColor: Int?,
)