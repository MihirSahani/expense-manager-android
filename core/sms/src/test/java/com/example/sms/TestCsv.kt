package com.example.sms

/** Minimal RFC-4180-style CSV reader: handles quoted fields containing commas and "" escapes. */
internal object TestCsv {
    fun parseCsvRows(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var field = StringBuilder()
        var row = mutableListOf<String>()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                inQuotes && c == '"' && i + 1 < text.length && text[i + 1] == '"' -> {
                    field.append('"'); i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    row.add(field.toString()); field = StringBuilder()
                }
                (c == '\n' || c == '\r') && !inQuotes -> {
                    if (field.isNotEmpty() || row.isNotEmpty()) {
                        row.add(field.toString()); field = StringBuilder()
                        rows.add(row); row = mutableListOf()
                    }
                    if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString()); rows.add(row)
        }
        return rows
    }
}
