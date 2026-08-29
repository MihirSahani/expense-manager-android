package com.example.common.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room3.withWriteTransaction
import com.example.common.model.CycleDailyExpenses
import com.example.core.database.AppDatabase
import com.example.core.database.dao.TransactionDAO
import com.example.core.database.entity.Transaction
import com.example.core.database.models.TransactionType
import com.example.core.database.models.normalizeAccountIdentifier
import com.example.core.database.models.normalizePayee
import com.example.core.database.projection.PayeeSummary
import com.example.core.database.projection.TransactionWithCategory
import com.example.datastore.Setting
import com.example.datastore.model.CycleType
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/**
 * Mediates access to [Transaction] data. Coordinates category-preference lookups and
 * account-balance adjustments atomically alongside transaction writes, and exposes cycle-aware
 * queries (monthly or salary-date, per [Setting.cycleType]) for the analysis and history screens.
 */
class TransactionRepository @Inject constructor(
    val setting: Setting,
    val dao: TransactionDAO,
    val accountRepo: AccountRepository,
    val categoryRepo: CategoryRepository,
    private val payeeCategoryPreferenceRepo: PayeeCategoryPreferenceRepository,
    private val debitCardPreferenceRepo: DebitCardPreferenceRepository,
    private val database: AppDatabase,
) {
    /**
     * Observes a single transaction by id.
     *
     * @param id the transaction id.
     * @return a [Flow] emitting the matching transaction, or `null` if it doesn't exist.
     */
    fun getTransactionFlow(id: Int): Flow<Transaction?> {
        return dao.getTransactionFlow(id)
    }

    /**
     * Observes daily expense totals for the current cycle, switching reactively between monthly
     * and salary-date windows based on [Setting.cycleType].
     *
     * @return a [Flow] emitting [CycleDailyExpenses] for the current cycle whenever it changes.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCurrentCycleDailyExpenses(): Flow<CycleDailyExpenses> {
        val zone = ZoneId.systemDefault()
        val offset = zone.rules.getOffset(Instant.now()).totalSeconds.toLong()
        return setting.cycleType.flatMapLatest { cycleType ->
            when (cycleType) {
                CycleType.MONTHLY -> {
                    val start = YearMonth.now().atDay(1).atStartOfDay(zone).toEpochSecond()
                    val end = YearMonth.now().atEndOfMonth().atTime(23, 59, 59)
                        .atZone(zone).toEpochSecond()
                    dao.getDailyExpensesBetween(start, end, offset)
                        .map { CycleDailyExpenses(start, end, it) }
                }
                CycleType.SALARY_DATE -> setting.salaryCreditTime.flatMapLatest { salaryTime ->
                    val end = Instant.now().epochSecond
                    dao.getDailyExpensesBetween(salaryTime, end, offset)
                        .map { CycleDailyExpenses(salaryTime, end, it) }
                }
            }
        }
    }

    /**
     * Observes every uncategorized payee/transaction-type pair for the current cycle, switching
     * reactively between monthly and salary-date windows based on [Setting.cycleType]. Used to
     * drive payee-category-discovery UI; a payee drops out as soon as it's assigned a category.
     *
     * @return a [Flow] emitting the list of [PayeeSummary] rows whenever it changes.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCurrentCyclePayees(): Flow<List<PayeeSummary>> {
        return setting.cycleType.flatMapLatest { cycleType ->
            when (cycleType) {
                CycleType.MONTHLY -> {
                    val start = YearMonth.now().atDay(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
                    val end = YearMonth.now().atEndOfMonth().atTime(23, 59, 59)
                        .atZone(ZoneId.systemDefault()).toEpochSecond()
                    dao.getPayeesBetween(start, end)
                }
                CycleType.SALARY_DATE -> setting.salaryCreditTime.flatMapLatest { salaryCreditTime ->
                    dao.getPayeesBetween(salaryCreditTime, Instant.now().epochSecond)
                }
            }
        }
    }

    /**
     * Observes transactions with category info for the current cycle as paged data, switching
     * reactively between monthly and salary-date windows based on [Setting.cycleType].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCurrentCycleTransactionsWithCategory(): Flow<PagingData<TransactionWithCategory>> {
        return setting.cycleType.flatMapLatest { cycleType ->
            when (cycleType) {
                CycleType.MONTHLY -> getCurrentMonthTransactionsWithCategory()
                CycleType.SALARY_DATE -> setting.salaryCreditTime.flatMapLatest { salaryCreditTime ->
                    getCurrentCycleTransactionsWithCategory(salaryCreditTime)
                }
            }
        }
    }

    /**
     * Observes transactions with category info from before the current cycle as paged data,
     * switching reactively between monthly and salary-date windows based on
     * [Setting.cycleType].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPastCycleTransactionsWithCategory(): Flow<PagingData<TransactionWithCategory>> {
        return setting.cycleType.flatMapLatest { cycleType ->
            when (cycleType) {
                CycleType.MONTHLY -> getPastMonthsTransactionsWithCategory()
                CycleType.SALARY_DATE -> setting.salaryCreditTime.flatMapLatest { salaryCreditTime ->
                    getPastCycleTransactionsWithCategory(salaryCreditTime)
                }
            }
        }
    }

    /**
     * Creates multiple transactions atomically: resolves each transaction's account, applies any
     * remembered payee/category preference (only when a transaction has no explicit category),
     * inserts the rows, and adjusts account balances accordingly. Refreshes the stored salary
     * credit time afterward.
     *
     * @param transactions the transactions to create.
     */
    suspend fun create(transactions: List<Transaction>) {
        database.withWriteTransaction {
            // Normalize fields and resolve account IDs for all transactions
            val transactionsWithAccount = resolveTransactionAccounts(
                transactions.map { it.withNormalizedFields() }
            )

            // Fetch all payee-category preferences and create a map for quick lookup
            val preferences = payeeCategoryPreferenceRepo.getAllPreferences()
                .associate { it.payee to it.transactionType to it.categoryId }

            val normalizedTransactions = transactionsWithAccount.map { transaction ->
                transaction.withRememberedCategory(
                    preferences[transaction.payee to transaction.transactionType]
                )
            }
            dao.create(normalizedTransactions)
            applyBalanceDeltas(normalizedTransactions)
        }
        updateSalaryCreditTime()
    }

    /**
     * Creates a single transaction atomically: resolves its account, applies any remembered
     * payee/category preference (only when the transaction has no explicit category), inserts
     * the row, and adjusts the account balance accordingly. Refreshes the stored salary credit
     * time afterward.
     *
     * @param transaction the transaction to create.
     */
    suspend fun create(transaction: Transaction) {
        database.withWriteTransaction {
            // Normalize fields and resolve account ID for the transaction
            val transactionWithAccount = resolveTransactionAccounts(
                listOf(transaction.withNormalizedFields())
            ).single()

            // Fetch the category ID for the payee and transaction type
            val categoryId = payeeCategoryPreferenceRepo.getCategoryId(
                transactionWithAccount.payee,
                transactionWithAccount.transactionType
            )

            val normalizedTransaction = transactionWithAccount.withRememberedCategory(categoryId)

            dao.create(normalizedTransaction)
            applyBalanceDeltas(listOf(normalizedTransaction))
        }
        updateSalaryCreditTime()
    }

    /**
     * Updates an existing transaction atomically. The account is never re-resolved (an explicit
     * `accountId` on [transaction] is always preserved); the old transaction's balance effect is
     * reversed and the updated transaction's effect is applied in its place. Refreshes the
     * stored salary credit time afterward.
     *
     * @param transaction the transaction with updated field values.
     * @throws IllegalStateException if the transaction does not exist or the update fails.
     */
    suspend fun updateTransaction(transaction: Transaction) {
        database.withWriteTransaction {
            // Retrieve the existing transaction to reverse its balance effect later
            val existingTransaction = dao.getTransaction(transaction.id)
                ?: throw IllegalStateException("Transaction ${transaction.id} does not exist")
            val normalizedTransaction = transaction.withNormalizedFields()

            // Update the transaction row in the database
            require(dao.update(normalizedTransaction) == 1) {
                throw IllegalStateException("Transaction ${transaction.id} could not be updated")
            }

            // Apply balance deltas: first reverse the effect of the existing transaction,
            // then apply the effect of the updated transaction
            applyBalanceDeltas(listOf(existingTransaction), reverse = true)
            applyBalanceDeltas(listOf(normalizedTransaction))
        }
        updateSalaryCreditTime()
    }

    /**
     * Reassigns a transaction's account atomically. If the transaction's raw account/card number
     * differs from the target account's own account number, the raw number is remembered as a
     * debit card belonging to that account (see [DebitCardPreferenceRepository.save]), and every
     * other still-unresolved transaction sharing that raw number is retroactively linked to the
     * same account via [linkRawAccountNoToAccount].
     *
     * @param transaction the transaction with its `accountId` already set to the chosen account.
     */
    suspend fun reassignAccount(transaction: Transaction) {
        database.withWriteTransaction {
            updateTransaction(transaction)

            val rawAccountNo = transaction.rawAccountNo
            val accountId = transaction.accountId
            if (rawAccountNo != null && accountId != null) {
                val account = accountRepo.getAccountById(accountId)
                if (account?.accountNumber != rawAccountNo) {
                    try {
                        debitCardPreferenceRepo.save(rawAccountNo, accountId)
                        linkRawAccountNoToAccount(rawAccountNo, accountId)
                    } catch (e: DebitCardPreferenceValidationException) {
                        // The account has no account number of its own, so it can't be a debit
                        // card target; the direct account reassignment above still applies.
                    }
                }
            }
        }
        updateSalaryCreditTime()
    }

    /**
     * Retroactively links every unresolved transaction (one with no `accountId` yet) sharing a
     * raw account/card number to an account, and applies their balance effect to it. Transactions
     * that already have an account assigned are left untouched.
     *
     * @param rawAccountNo the raw account/card number to match; normalized before matching.
     * @param accountId the account id to link matching transactions to.
     */
    suspend fun linkRawAccountNoToAccount(rawAccountNo: String, accountId: Int) {
        val normalizedRawAccountNo = rawAccountNo.normalizeAccountIdentifier() ?: return
        database.withWriteTransaction {
            val unresolvedTransactions = dao.getUnresolvedTransactionsForRawAccountNo(normalizedRawAccountNo)
            if (unresolvedTransactions.isEmpty()) return@withWriteTransaction

            dao.linkRawAccountNoToAccount(normalizedRawAccountNo, accountId)
            applyBalanceDeltas(unresolvedTransactions.map { it.copy(accountId = accountId) })
        }
    }

    /**
     * Updates only the category of a single transaction and refreshes the stored salary credit
     * time afterward.
     *
     * @param transactionId the transaction id.
     * @param categoryId the new category id, or `null` to clear it.
     */
    suspend fun updateTransactionCategory(transactionId: Int, categoryId: Int?) {
        dao.updateTransactionCategory(transactionId, categoryId)
        updateSalaryCreditTime()
    }

    /**
     * Atomically remembers a category preference for a payee/transaction-type combination and
     * retroactively applies it to every existing transaction that matches. Refreshes the stored
     * salary credit time afterward.
     *
     * @param payee the payee name to normalize and remember.
     * @param transactionType the [TransactionType] the preference applies to.
     * @param categoryId the category id to remember and apply.
     */
    suspend fun updateAndRememberCategoryForPayee(
        payee: String,
        transactionType: TransactionType,
        categoryId: Int
    ) {
        database.withWriteTransaction {
            val preference = payeeCategoryPreferenceRepo.insert(
                payee = payee,
                transactionType = transactionType,
                categoryId = categoryId
            )
            dao.updateTransactionsCategory(
                preference.payee,
                transactionType,
                categoryId
            )
        }
        updateSalaryCreditTime()
    }

    /**
     * Deletes a transaction atomically, reversing its balance effect on its account, and
     * refreshes the stored salary credit time afterward.
     *
     * @param id the transaction id to delete.
     * @throws IllegalStateException if the transaction does not exist or the delete fails.
     */
    suspend fun deleteTransaction(id: Int) {
        database.withWriteTransaction {
            // Retrieve the transaction to be deleted to apply balance deltas later
            val transaction = dao.getTransaction(id)
                ?: throw IllegalStateException("Transaction $id does not exist")

            require(dao.deleteById(id) == 1) {
                throw IllegalStateException("Transaction $id could not be deleted")
            }

            // Reverse the amount of the deleted transaction from the account balance
            applyBalanceDeltas(listOf(transaction), reverse = true)
        }
        updateSalaryCreditTime()
    }

    /** Observes every account. Delegates to [AccountRepository.accounts]. */
    val accounts get() = accountRepo.accounts
    /** Observes every category. Delegates to [CategoryRepository.categories]. */
    val categories get() = categoryRepo.categories
    /** Observes every unresolved raw account/card number, for account-discovery UI. */
    val unresolvedAccounts get() = dao.getUnresolvedRawAccountNumbers()
    /** Observes transactions with category info scoped to the current calendar month. */
    private fun getCurrentMonthTransactionsWithCategory(): Flow<PagingData<TransactionWithCategory>> {
        val start = YearMonth.now()
            .atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond()
        val end = YearMonth.now()
            .atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toEpochSecond()
        return getTransactionsWithCategoryBetween(start, end)
    }

    /** Observes transactions with category info from before the current calendar month. */
    private fun getPastMonthsTransactionsWithCategory(): Flow<PagingData<TransactionWithCategory>> {
        val end = YearMonth.now()
            .minusMonths(1)
            .atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toEpochSecond()
        return getTransactionsWithCategoryBetween(end = end)
    }

    /**
     * Observes transactions with category info scoped to the current salary-date cycle.
     *
     * @param salaryCreditTime the epoch-second start of the current cycle.
     */
    private fun getCurrentCycleTransactionsWithCategory(salaryCreditTime: Long): Flow<PagingData<TransactionWithCategory>> {
        return getTransactionsWithCategoryBetween(start = salaryCreditTime)
    }

    /**
     * Observes transactions with category info from before the current salary-date cycle.
     *
     * @param salaryCreditTime the epoch-second start of the current cycle (used as the
     * exclusive upper bound).
     */
    private fun getPastCycleTransactionsWithCategory(salaryCreditTime: Long): Flow<PagingData<TransactionWithCategory>> {
        return getTransactionsWithCategoryBetween(end = salaryCreditTime - 1)
    }

    /**
     * Builds a [Pager] over transactions with category info within a datetime window.
     *
     * @param start inclusive start of the window, in epoch seconds. Defaults to the beginning of
     * time.
     * @param end inclusive end of the window, in epoch seconds. Defaults to the end of time.
     */
    private fun getTransactionsWithCategoryBetween(
        start: Long = 0,
        end: Long = Long.MAX_VALUE
    ): Flow<PagingData<TransactionWithCategory>> {
        return Pager(
            config = PagingConfig(15),
            pagingSourceFactory = { dao.getTransactionsWithCategoryBetween(start, end) }
        ).flow
    }

    /**
     * Refreshes the persisted salary credit time from the most recent `INCOME` transaction, or
     * `0` if none exists. Called after every write so salary-date cycle boundaries stay current.
     */
    suspend fun updateSalaryCreditTime() {
        val time = dao.getLatestIncomeTransactionTime() ?: 0L
        setting.setSalaryCreditTime(time)
    }

    /**
     * Applies a remembered category to this transaction, but only if it currently has no
     * category set. An explicit category on the transaction always wins.
     *
     * @param categoryId the remembered category id, or `null` if none was found.
     * @return a copy of this transaction with the category applied, or this transaction
     * unchanged if it already had a category or none was remembered.
     */
    private fun Transaction.withRememberedCategory(categoryId: Int?): Transaction =
        if (this.categoryId == null && categoryId != null) copy(categoryId = categoryId) else this

    /**
     * Resolves the account for each transaction by matching its `rawAccountNo` against known
     * account numbers first, then debit-card preferences. Transactions with no match or no
     * `rawAccountNo` are left with a `null` account.
     *
     * @param transactions the transactions to resolve accounts for.
     * @return the transactions with `accountId` populated where a match was found.
     */
    private suspend fun resolveTransactionAccounts(
        transactions: List<Transaction>
    ): List<Transaction> {
        // Build a map of account numbers to account IDs for quick lookup
        val accountsByNumber = accountRepo
            .getAllAccounts()
            .filter { it.accountNumber != null }
            .associate { account -> account.accountNumber to account.id }

        // Build a map of debit card numbers to account IDs for quick lookup
        val accountsByCardNumber = debitCardPreferenceRepo
            .getAllPreferences()
            .associate { it.cardNumber to it.accountId }

        return transactions.map { transaction ->
            transaction.copy(
                accountId = transaction.rawAccountNo?.let { accountNumber ->
                    accountsByNumber[accountNumber] ?: accountsByCardNumber[accountNumber]
                }
            )
        }
    }

    /**
     * Applies each transaction's balance effect (credits add, debits subtract) to its account,
     * batching deltas per account and skipping transactions with no account.
     *
     * @param transactions the transactions whose effects should be applied.
     * @param reverse when `true`, negates every delta (used to undo a previously applied effect).
     */
    private suspend fun applyBalanceDeltas(
        transactions: List<Transaction>,
        reverse: Boolean = false
    ) {
        // Batch deltas by account to avoid multiple writes for the same account
        val deltasByAccount = mutableMapOf<Int, Long>()

        transactions.forEach { transaction ->
            transaction.accountId?.let { accountId ->
                // delta = transaction.balanceDelta() * (reverse ? -1 : 1)
                val delta = Math.multiplyExact(transaction.balanceDelta(), if (reverse) -1 else 1.toLong())

                // delta[accountId] = delta[accountId] + delta
                deltasByAccount[accountId] = Math.addExact(
                    deltasByAccount[accountId] ?: 0L,
                    delta
                )
            }
        }
        // Write the updated balances to the accounts in a single batch
        deltasByAccount.forEach { (accountId, delta) ->
            if (delta != 0L) {
                accountRepo.adjustBalance(accountId, delta)
            }
        }
    }

    /**
     * Computes this transaction's signed effect on an account balance: positive for credits,
     * negative for debits.
     */
    private fun Transaction.balanceDelta(): Long = when (transactionType) {
        TransactionType.CREDIT -> amount
        TransactionType.DEBIT -> Math.negateExact(amount)
    }

    /**
     * Normalizes this transaction's payee (trim + lowercase) and raw account number (digits
     * only, leading zeroes preserved) before persistence.
     */
    private fun Transaction.withNormalizedFields(): Transaction =
        copy(
            payee = payee.normalizePayee(),
            rawAccountNo = rawAccountNo.normalizeAccountIdentifier()
        )
}