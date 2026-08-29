package com.example.common.repository

import androidx.room3.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.core.database.AppDatabase
import com.example.core.database.entity.Account
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: AccountRepository

    @Before
    fun createRepository() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AccountRepository(database.accountDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun normalizesAccountIdentifiersAndAllowsMissingIdentifiers() = runBlocking {
        repository.createAccount(account(1, "Primary", "A/c XX00-123"))
        repository.createAccount(account(2, "Cash", null))
        repository.createAccount(account(3, "Investment", " "))

        assertEquals("00123", database.accountDao().getAccountById(1)?.accountNumber)
        assertNull(database.accountDao().getAccountById(2)?.accountNumber)
        assertNull(database.accountDao().getAccountById(3)?.accountNumber)
    }

    @Test
    fun rejectsDuplicateNormalizedAccountIdentifiers() = runBlocking {
        repository.createAccount(account(1, "Primary", "00-123"))

        try {
            repository.createAccount(account(2, "Duplicate", "00123"))
            fail("Expected duplicate account identifier to be rejected")
        } catch (error: AccountValidationException) {
            assertEquals("Account number is already used by another account", error.message)
        }
    }

    @Test
    fun rejectsNonBlankIdentifiersWithoutDigits() = runBlocking {
        try {
            repository.createAccount(account(1, "Invalid", "not assigned"))
            fail("Expected invalid account identifier to be rejected")
        } catch (error: AccountValidationException) {
            assertEquals("Account number must contain at least one digit", error.message)
        }
    }

    @Test
    fun metadataUpdatesPreserveTheLatestBalanceWhenBalanceWasNotEdited() = runBlocking {
        val original = account(1, "Primary", "1234").copy(balance = 1_000)
        repository.createAccount(original)
        repository.adjustBalance(1, -100)

        repository.updateAccount(original.copy(name = "Renamed"), updateBalance = false)

        val updated = checkNotNull(database.accountDao().getAccountById(1))
        assertEquals("Renamed", updated.name)
        assertEquals(900, updated.balance)
    }

    private fun account(id: Int, name: String, accountNumber: String?) = Account(
        id = id,
        name = name,
        balance = 0,
        type = AccountType.OTHER,
        accountNumber = accountNumber,
        color = null,
        icon = AccountIcon.OTHER
    )
}
