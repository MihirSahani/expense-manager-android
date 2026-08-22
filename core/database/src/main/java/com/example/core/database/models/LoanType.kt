package com.example.core.database.models

enum class LoanType {
    CREDIT, DEBT;

    fun display(): String = name
}