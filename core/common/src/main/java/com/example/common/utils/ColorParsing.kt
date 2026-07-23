package com.example.common.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

fun parseColor(hex: String): Color {
    return try {
        Color(hex.toColorInt())
    } catch (e: Exception) {
        Color.Gray
    }
}
