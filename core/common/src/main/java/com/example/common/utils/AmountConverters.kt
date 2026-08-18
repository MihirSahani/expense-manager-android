package com.example.common.utils

import java.text.NumberFormat
import java.util.Locale
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