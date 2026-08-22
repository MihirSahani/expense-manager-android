package com.example.core.database.models

fun String?.normalizeAccountIdentifier(): String? =
    this?.filter { it in '0'..'9' }?.ifBlank { null }
