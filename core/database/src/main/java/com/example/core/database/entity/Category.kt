package com.example.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity("categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("type")
    val type: Int,
    @ColumnInfo("budget_per_cycle")
    val budgetPerCycle: Long,
    @ColumnInfo("color")
    val color: Int?,
    @ColumnInfo("icon")
    val icon: Int?
)