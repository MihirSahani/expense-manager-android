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
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DebitCardPreferenceRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var accountRepository: AccountRepository
    private lateinit var repository: DebitCardPreferenceRepository

    @Before
    fun createRepository() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        accountRepository = AccountRepository(database.accountDao())
        repository = DebitCardPreferenceRepository(
            database.debitCardPreferenceDao(),
            accountRepository,
            database
        )
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun savesOneNormalizedMappingPerCardIdentifier() = runBlocking {
        accountRepository.createAccount(account(1, "Primary", "1111"))
        accountRepository.createAccount(account(2, "Secondary", "2222"))

        repository.save("Card XX00-123", 1)
        assertEquals(1, repository.getAllPreferences().single().accountId)

        repository.save("00 123", 2)

        val preferences = repository.getAllPreferences()
        assertEquals(2, preferences.single().accountId)
        assertEquals("00123", preferences.single().cardNumber)
    }

    @Test
    fun rejectsTargetsWithoutAccountIdentifiers() = runBlocking {
        accountRepository.createAccount(account(1, "Cash", null))

        try {
            repository.save("1234", 1)
            fail("Expected target account without an identifier to be rejected")
        } catch (error: DebitCardPreferenceValidationException) {
            assertEquals("Debit card preferences require an account number", error.message)
        }
    }

    @Test
    fun deletingAnAccountDeletesItsCardPreferences() = runBlocking {
        accountRepository.createAccount(account(1, "Primary", "1111"))
        repository.save("56789", 1)

        accountRepository.deleteAccount(1)

        assertTrue(repository.getAllPreferences().isEmpty())
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
