package com.example.core.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.core.database.entity.Loan
import com.example.core.database.models.LoanType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `loan` table.
 */
@Dao
abstract class LoanDAO {
    /**
     * Fetches a single loan by its primary key.
     *
     * @param id the loan id, or `null` to match any row (returns the first one found).
     * @return the matching [Loan], or `null` if none exists.
     */
    @Query("SELECT * FROM loan WHERE (:id IS NULL OR id = :id)")
    abstract suspend fun getLoan(id: Int?): Loan?

    /**
     * Observes a single loan by its primary key.
     *
     * @param id the loan id, or `null` to match any row (returns the first one found).
     * @return a [Flow] emitting the matching [Loan] (or `null`) whenever it changes.
     */
    @Query("SELECT * FROM loan WHERE (:id IS NULL OR id = :id)")
    abstract fun getLoanFlow(id: Int?): Flow<Loan?>

    /**
     * Fetches loans as a paged source, optionally filtered by type, for use with Paging 3.
     *
     * @param loanType the [LoanType] to filter by, or `null` to include all loans.
     * @return a [PagingSource] over the matching loans.
     */
    @Query("SELECT * FROM loan WHERE (:loanType IS NULL OR loan_type = :loanType)")
    abstract fun getLoansPaged(loanType: LoanType? = null): PagingSource<Int, Loan>

    /**
     * Inserts a new loan.
     *
     * @param loan the loan to insert.
     */
    @Insert
    abstract suspend fun create(loan: Loan)

    /**
     * Updates an existing loan.
     *
     * @param loan the loan with updated field values.
     */
    @Update
    abstract suspend fun update(loan: Loan)

    /**
     * Deletes a loan.
     *
     * @param loan the loan to delete.
     */
    @Delete
    abstract suspend fun delete(loan: Loan)

    /**
     * Deletes a loan by its primary key.
     *
     * @param id the loan id to delete.
     */
    @Query("DELETE FROM loan WHERE id = :id")
    abstract suspend fun deleteById(id: Int)
}