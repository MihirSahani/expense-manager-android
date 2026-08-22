package com.example.core.database.models

enum class AccountType {
    SAVINGS, CHECKING, CREDIT_CARD, CASH, INVESTMENT, OTHER;

    fun display(): String =
    name.uppercase().replace("_", " ")
}