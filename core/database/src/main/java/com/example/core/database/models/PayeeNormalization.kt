package com.example.core.database.models

import java.util.Locale

fun String.normalizePayee(): String = trim().lowercase(Locale.ROOT)
