package com.example.core.database.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountIdentifierTest {
    @Test
    fun `normalization retains only digits and preserves leading zeroes`() {
        assertEquals("00123", "Card XX00-123".normalizeAccountIdentifier())
        assertEquals("123456", " 12 34-56 ".normalizeAccountIdentifier())
    }

    @Test
    fun `normalization returns null when no digits are present`() {
        assertNull(null.normalizeAccountIdentifier())
        assertNull("".normalizeAccountIdentifier())
        assertNull("not assigned".normalizeAccountIdentifier())
    }
}
