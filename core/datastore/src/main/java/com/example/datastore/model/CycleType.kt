package com.example.datastore.model

enum class CycleType {
    MONTHLY, SALARY_DATE;

    fun display(): String =
        name.split("_").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}