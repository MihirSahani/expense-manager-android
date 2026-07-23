package com.example.core.database.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.PhonePaused
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.vector.ImageVector

enum class CategoryIcon(val imageVector: ImageVector) {
    FOOD(Icons.Filled.LocalDining),
    TRANSPORT(Icons.Filled.LocalTaxi),
    SALARY(Icons.Filled.Badge),
    GROCERIES(Icons.Filled.EmojiFoodBeverage),
    UTILITIES(Icons.Filled.PhonePaused),
    FRIENDS_AND_FAMILIES(Icons.Filled.Groups),
    SHOPPING(Icons.Filled.LocalGroceryStore),
    INVESTMENT(Icons.Filled.Savings);
}