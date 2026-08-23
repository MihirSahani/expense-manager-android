package com.example.common.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.core.database.dao.LoanDAO
import com.example.core.database.entity.Loan
import com.example.core.database.models.LoanType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Mediates access to [Loan] data.
 */
class LoanRepository @Inject constructor(
    val dao: LoanDAO,
) {
    /**
     * Fetches a single loan by id.
     *
     * @param id the loan id, or `null` to match any row.
     * @return the matching loan, or `null` if none exists.
     */
    suspend fun getLoan(id: Int?): Loan? = dao.getLoan(id)

    /**
     * Observes a single loan by id.
     *
     * @param id the loan id, or `null` to match any row.
     * @return a [Flow] emitting the matching loan whenever it changes.
     */
    fun getLoanFlow(id: Int?): Flow<Loan?> = dao.getLoanFlow(id)

    /**
     * Observes loans as paged data, optionally filtered by type.
     *
     * @param loanType the [LoanType] to filter by, or `null` to include all loans.
     * @return a [Flow] of [PagingData] over the matching loans.
     */
    fun getLoansPaged(loanType: LoanType? = null): Flow<PagingData<Loan>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { dao.getLoansPaged(loanType) }
        ).flow
    }

    /**
     * Creates a new loan.
     *
     * @param loan the loan to create.
     */
    suspend fun create(loan: Loan) = dao.create(loan)

    /**
     * Updates an existing loan.
     *
     * @param loan the loan with updated field values.
     */
    suspend fun update(loan: Loan) = dao.update(loan)

    /**
     * Deletes a loan.
     *
     * @param loan the loan to delete.
     */
    suspend fun delete(loan: Loan) = dao.delete(loan)

    /**
     * Deletes a loan by id.
     *
     * @param id the loan id to delete.
     */
    suspend fun deleteById(id: Int) = dao.deleteById(id)
}