package com.example.common.model

/**
 * Criteria for narrowing the transaction history list, applied by the transaction history
 * filter bottom sheet. An instance where every field is empty/`null`/`false` (see [isEmpty])
 * means no filter is active and the screen falls back to its default cycle-based query.
 *
 * @param categoryIds categories to restrict results to; empty means no category restriction
 * (unless [includeUncategorized] is set).
 * @param includeUncategorized when `true`, also matches transactions with no assigned category,
 * in addition to any [categoryIds].
 * @param startDate inclusive lower bound on transaction datetime, in epoch seconds, or `null` for
 * no lower bound.
 * @param endDate inclusive upper bound on transaction datetime, in epoch seconds, or `null` for
 * no upper bound.
 * @param minAmount inclusive lower bound on transaction amount (smallest currency unit), or
 * `null` for no lower bound.
 * @param maxAmount inclusive upper bound on transaction amount (smallest currency unit), or
 * `null` for no upper bound.
 * @param payeeQuery a case-insensitive substring to match against payee names, or `null`/blank
 * for no payee restriction.
 */
data class TransactionFilter(
    val categoryIds: Set<Int> = emptySet(),
    val includeUncategorized: Boolean = false,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val minAmount: Long? = null,
    val maxAmount: Long? = null,
    val payeeQuery: String? = null,
) {
    /** Whether no filter criteria are set. */
    fun isEmpty(): Boolean {
        return categoryIds.isEmpty() &&
            !includeUncategorized &&
            startDate == null &&
            endDate == null &&
            minAmount == null &&
            maxAmount == null &&
            payeeQuery.isNullOrBlank()
    }
}

