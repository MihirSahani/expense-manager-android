package com.example.core.database.models

enum class AccountType {
    SAVINGS, CHECKING, CREDIT_CARD, CASH, INVESTMENT, OTHER;

    fun display(): String =
    name.split("_").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}