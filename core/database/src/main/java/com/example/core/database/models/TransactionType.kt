package com.example.core.database.models

enum class TransactionType {
    CREDIT, DEBIT;

    fun display(): String = name.lowercase().replaceFirstChar { it.uppercase() }
}