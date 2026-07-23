package com.example.common.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun toString(pattern: String, time: Long): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return Instant
        .ofEpochSecond(time)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

fun Long.toDateString(): String {
    return toString("dd MMM yyyy", this)
}

fun Long.toTimeString(): String {
    return toString("HH:mm", this)
}