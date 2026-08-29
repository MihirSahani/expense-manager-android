package com.example.common.repository

import androidx.room3.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.core.database.AppDatabase
import com.example.core.database.entity.Account
import com.example.core.database.entity.Category
import com.example.core.database.entity.Transaction
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import com.example.core.database.models.TransactionType
import com.example.datastore.Setting
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var accountRepository: AccountRepository
    private lateinit var debitCardPreferenceRepository: DebitCardPreferenceRepository
    private lateinit var preferenceRepository: PayeeCategoryPreferenceRepository
    private lateinit var repository: TransactionRepository

    @Before
    fun createRepository() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val setting = Setting(context)
        preferenceRepository = PayeeCategoryPreferenceRepository(
            database.payeeCategoryPreferenceDao()
        )
        accountRepository = AccountRepository(database.accountDao())
        debitCardPreferenceRepository = DebitCardPreferenceRepository(
            database.debitCardPreferenceDao(),
            accountRepository,
            database
        )
        repository = TransactionRepository(
            setting = setting,
            dao = database.transactionDao(),
            accountRepo = accountRepository,
            categoryRepo = CategoryRepository(database.categoryDao(), setting),
            payeeCategoryPreferenceRepo = preferenceRepository,
            debitCardPreferenceRepo = debitCardPreferenceRepository,
            database = database
        )
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun remembersAndAppliesCategoriesByPayeeAndTransactionType() = runBlocking {
        val food = category(1, "Food")
        val transport = category(2, "Transport")
        database.categoryDao().create(food)
        database.categoryDao().create(transport)
        repository.create(
            listOf(
                transaction(1, "Café", TransactionType.DEBIT),
                transaction(2, "\t CAFÉ \n", TransactionType.DEBIT),
                transaction(3, "CAFÉ", TransactionType.CREDIT)
            )
        )

        repository.updateAndRememberCategoryForPayee(
            payee = " Café ",
            transactionType = TransactionType.DEBIT,
            categoryId = food.id
        )

        assertEquals(food.id, database.transactionDao().getTransaction(1)?.categoryId)
        assertEquals(food.id, database.transactionDao().getTransaction(2)?.categoryId)
        assertNull(database.transactionDao().getTransaction(3)?.categoryId)
        assertEquals("café", database.transactionDao().getTransaction(1)?.payee)
        assertEquals("café", database.transactionDao().getTransaction(2)?.payee)

        repository.updateAndRememberCategoryForPayee(
            payee = "CAFÉ",
            transactionType = TransactionType.CREDIT,
            categoryId = transport.id
        )
        repository.create(transaction(4, " café ", TransactionType.DEBIT))
        repository.create(
            listOf(
                transaction(5, "CAFÉ", TransactionType.CREDIT),
                transaction(6, "Other", TransactionType.DEBIT),
                transaction(
                    id = 7,
                    payee = "café",
                    transactionType = TransactionType.DEBIT,
                    categoryId = transport.id
                )
            )
        )

        assertEquals(transport.id, database.transactionDao().getTransaction(3)?.categoryId)
        assertEquals(food.id, database.transactionDao().getTransaction(4)?.categoryId)
        assertEquals(transport.id, database.transactionDao().getTransaction(5)?.categoryId)
        assertNull(database.transactionDao().getTransaction(6)?.categoryId)
        assertEquals(transport.id, database.transactionDao().getTransaction(7)?.categoryId)
        assertEquals("other", database.transactionDao().getTransaction(6)?.payee)

        database.categoryDao().delete(food)

        assertNull(
            preferenceRepository.getCategoryId("café", TransactionType.DEBIT)
        )
        assertNull(database.transactionDao().getTransaction(1)?.categoryId)
        assertNull(database.transactionDao().getTransaction(4)?.categoryId)
    }

    @Test
    fun directAccountMatchTakesPrecedenceAndUpdatesBalance() = runBlocking {
        accountRepository.createAccount(account(1, "Primary", 1_000, "00123"))
        accountRepository.createAccount(account(2, "Secondary", 2_000, "9999"))
        debitCardPreferenceRepository.save("00-123", 2)

        repository.create(
            transaction(
                id = 10,
                payee = "Store",
                transactionType = TransactionType.DEBIT,
                amount = 100,
                rawAccountNo = "A/c XX00-123"
            )
        )

        assertEquals(1, database.transactionDao().getTransaction(10)?.accountId)
        assertEquals(900, database.accountDao().getAccountById(1)?.balance)
        assertEquals(2_000, database.accountDao().getAccountById(2)?.balance)
    }

    @Test
    fun cardPreferencesAffectOnlyTransactionsCreatedAfterThePreference() = runBlocking {
        accountRepository.createAccount(account(1, "Primary", 1_000, "1111"))
        repository.create(
            transaction(
                id = 20,
                payee = "Before",
                transactionType = TransactionType.DEBIT,
                rawAccountNo = "Card 56789"
            )
        )

        debitCardPreferenceRepository.save("56-789", 1)
        repository.create(
            transaction(
                id = 21,
                payee = "After",
                transactionType = TransactionType.CREDIT,
                amount = 250,
                rawAccountNo = "Card 56789"
            )
        )

        assertNull(database.transactionDao().getTransaction(20)?.accountId)
        assertEquals(1, database.transactionDao().getTransaction(21)?.accountId)
        assertEquals(1_250, database.accountDao().getAccountById(1)?.balance)
    }

    @Test
    fun explicitAccountsAndBatchNetDeltasArePreserved() = runBlocking {
        accountRepository.createAccount(account(1, "Primary", 1_000, "1234"))
        accountRepository.createAccount(account(2, "Secondary", 2_000, "9999"))
        debitCardPreferenceRepository.save("56789", 1)

        repository.create(
            listOf(
                transaction(
                    id = 30,
                    payee = "Income",
                    transactionType = TransactionType.CREDIT,
                    amount = 300,
                    rawAccountNo = "A/c 1234"
                ),
                transaction(
                    id = 31,
                    payee = "Card purchase",
                    transactionType = TransactionType.DEBIT,
                    amount = 100,
                    rawAccountNo = "Card XX56789"
                ),
                transaction(
                    id = 32,
                    payee = "Unknown",
                    transactionType = TransactionType.DEBIT,
                    amount = 50,
                    rawAccountNo = "0000"
                ),
                transaction(
                    id = 33,
                    payee = "Explicit",
                    transactionType = TransactionType.DEBIT,
                    amount = 200,
                    rawAccountNo = "1234",
                    accountId = 2
                )
            )
        )

        assertEquals(1, database.transactionDao().getTransaction(30)?.accountId)
        assertEquals(1, database.transactionDao().getTransaction(31)?.accountId)
        assertNull(database.transactionDao().getTransaction(32)?.accountId)
        assertEquals(2, database.transactionDao().getTransaction(33)?.accountId)
        assertEquals(1_200, database.accountDao().getAccountById(1)?.balance)
        assertEquals(1_800, database.accountDao().getAccountById(2)?.balance)
    }

    @Test
    fun updateAndDeleteReversePreviousBalanceEffects() = runBlocking {
        accountRepository.createAccount(account(1, "Primary", 1_000, "1234"))
        accountRepository.createAccount(account(2, "Secondary", 500, "9999"))
        repository.create(
            transaction(
                id = 40,
                payee = "Purchase",
                transactionType = TransactionType.DEBIT,
                amount = 100,
                accountId = 1
            )
        )

        val persistedTransaction = checkNotNull(database.transactionDao().getTransaction(40))
        repository.updateTransaction(
            persistedTransaction.copy(
                amount = 250,
                transactionType = TransactionType.CREDIT,
                accountId = 2
            )
        )

        assertEquals(1_000, database.accountDao().getAccountById(1)?.balance)
        assertEquals(750, database.accountDao().getAccountById(2)?.balance)

        repository.deleteTransaction(40)

        assertEquals(500, database.accountDao().getAccountById(2)?.balance)
        assertNull(database.transactionDao().getTransaction(40))
    }

    @Test
    fun failedBatchRollsBackTransactionsAndBalances() = runBlocking {
        accountRepository.createAccount(account(1, "Primary", 1_000, "1234"))

        try {
            repository.create(
                listOf(
                    transaction(
                        id = 50,
                        payee = "Valid",
                        transactionType = TransactionType.DEBIT,
                        amount = 100,
                        accountId = 1
                    ),
                    transaction(
                        id = 51,
                        payee = "Missing account",
                        transactionType = TransactionType.DEBIT,
                        amount = 100,
                        accountId = 999
                    )
                )
            )
            fail("Expected the invalid account reference to fail the batch")
        } catch (_: RuntimeException) {
            // Room must roll back the valid insert together with the invalid one.
        }

        assertNull(database.transactionDao().getTransaction(50))
        assertNull(database.transactionDao().getTransaction(51))
        assertEquals(1_000, database.accountDao().getAccountById(1)?.balance)
    }

    @Test
    fun getPayeesBetweenAggregatesUncategorizedPayeesAndDropsThemOnceCategorized() = runBlocking {
        val food = category(1, "Food")
        database.categoryDao().create(food)

        repository.create(
            listOf(
                transaction(1, "Zomato", TransactionType.DEBIT, amount = 100),
                transaction(2, "Zomato", TransactionType.DEBIT, amount = 200),
                transaction(3, "Arjun Rao", TransactionType.CREDIT, amount = 500),
                // Already categorized -- must not appear in the uncategorized-only results.
                transaction(4, "Salary Inc", TransactionType.CREDIT, categoryId = food.id)
            )
        )

        val payees = database.transactionDao().getPayeesBetween(0, Long.MAX_VALUE).first()
            .associateBy { it.payee to it.transactionType }

        assertEquals(2, payees.size)
        val zomato = checkNotNull(payees["zomato" to TransactionType.DEBIT])
        assertEquals(2, zomato.transactionCount)
        assertEquals(300, zomato.totalAmount)
        assertEquals(2L, zomato.lastTransactionTime)
        assertNull(payees["salary inc" to TransactionType.CREDIT])

        repository.updateAndRememberCategoryForPayee(
            payee = "Zomato",
            transactionType = TransactionType.DEBIT,
            categoryId = food.id
        )

        val payeesAfterTagging = database.transactionDao().getPayeesBetween(0, Long.MAX_VALUE).first()
        assertNull(payeesAfterTagging.find { it.payee == "zomato" })
        assertEquals(1, payeesAfterTagging.size)
    }

    private fun category(id: Int, name: String) = Category(
        id = id,
        name = name,
        type = CategoryType.EXPENSE,
        budgetPerCycle = null,
        color = null,
        icon = CategoryIcon.FOOD
    )

    private fun transaction(
        id: Int,
        payee: String,
        transactionType: TransactionType,
        categoryId: Int? = null,
        amount: Long = 100,
        rawAccountNo: String? = null,
        accountId: Int? = null
    ) = Transaction(
        id = id,
        amount = amount,
        categoryId = categoryId,
        datetime = id.toLong(),
        rawAccountNo = rawAccountNo,
        accountId = accountId,
        payee = payee,
        transactionType = transactionType,
        referenceId = null,
        description = null
    )

    private fun account(
        id: Int,
        name: String,
        balance: Long,
        accountNumber: String?
    ) = Account(
        id = id,
        name = name,
        balance = balance,
        type = AccountType.OTHER,
        accountNumber = accountNumber,
        color = null,
        icon = AccountIcon.OTHER
    )
}
