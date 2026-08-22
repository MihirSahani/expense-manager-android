package com.example.core.database.models

import org.junit.Assert.assertEquals
import org.junit.Test

class PayeeNormalizationTest {
    @Test
    fun `normalization trims whitespace and lowercases with root locale`() {
        assertEquals("épic store", "\t ÉPIC STORE \n".normalizePayee())
    }
}
