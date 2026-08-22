package com.example.core.database.models

enum class CategoryType {
    EXPENSE, INCOME;

    fun display(): String = name
}