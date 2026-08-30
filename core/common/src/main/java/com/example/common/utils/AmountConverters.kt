package com.example.common.utils

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private val amountFormat = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
    maximumFractionDigits = 2
}

fun Long?.display(): String? {
    return this?.let { amountFormat.format(it.toDouble() / 100) }
}

fun String?.toAmountLong(): Long? {
    return this?.replace(",", "")?.toDoubleOrNull()?.times(100)?.roundToLong()
}

fun Long.abbreviateAmount(): String {
    val rupees = abs(this) / 100.0
    return when {
        rupees >= 100_000 -> trimZero(rupees / 100_000) + "L"
        rupees >= 1_000 -> trimZero(rupees / 1_000) + "k"
        else -> rupees.roundToInt().toString()
    }
}


private fun trimZero(value: Double): String {
    val formatted = "%.1f".format(value)
    return formatted.removeSuffix(".0")
}