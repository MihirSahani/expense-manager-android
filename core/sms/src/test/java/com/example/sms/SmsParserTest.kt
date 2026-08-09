package com.example.sms

import android.content.ContextWrapper
import android.content.pm.PackageManager
import com.example.core.database.entity.Transaction
import com.example.core.database.models.TransactionType
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Feeds a CSV of real-world Indian bank SMS through the real [SmsParser.parseSmsData]. Every
 * expectation lives in the CSV (`out.csv`) — the test hardcodes nothing, so new cases are added by
 * appending a row. Columns:
 *   sender, body, timestamp, is_transaction, exp_type, exp_amount_paise, exp_payee, exp_account
 * For non-transaction rows the expectation columns are blank; a blank exp_account means null.
 *
 * Each CSV row is surfaced as its own [ParameterizedTest] case, so a run reports one result per SMS
 * (and the report names the offending message) instead of collapsing everything into a single test.
 */
class SmsParserTest {

    /** A Context that reports every permission as granted (unit tests have no real one). */
    private class GrantingContext : ContextWrapper(null) {
        override fun checkPermission(permission: String, pid: Int, uid: Int) =
            PackageManager.PERMISSION_GRANTED

        override fun checkSelfPermission(permission: String) =
            PackageManager.PERMISSION_GRANTED
    }

    data class Sample(
        val sender: String,
        val body: String,
        val timestamp: Long,
        val isTransaction: Boolean,
        val expType: TransactionType?,
        val expAmountPaise: Long?,
        val expPayee: String?,
        val expAccount: String?
    ) {
        /** Short, single-line label used as the test-case name in reports. */
        override fun toString(): String {
            val oneLine = body.replace(Regex("\\s+"), " ").trim()
            val snippet = if (oneLine.length > 60) oneLine.take(57) + "..." else oneLine
            return "$sender | $snippet"
        }
    }

    private val parser = SmsParser(GrantingContext())

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("samples")
    fun `parses indian bank sms and rejects non transactions`(sample: Sample) {
        val txn = parser.parseSmsData(sample.body, sample.sender, sample.timestamp)

        if (!sample.isTransaction) {
            assertNull(txn) { "'${sample.body}' should be rejected" }
            return
        }

        assertNotNull(txn) { "'${sample.body}' should parse as a transaction" }
        assertAll(
            { assertEquals(sample.expType, txn!!.transactionType) { "type for '${sample.body}'" } },
            { assertEquals(sample.expAmountPaise, txn!!.amount) { "amount (paise) for '${sample.body}'" } },
            { assertEquals(sample.expPayee, txn!!.payee) { "payee for '${sample.body}'" } },
            { assertEquals(sample.expAccount, txn!!.rawAccountNo) { "rawAccountNo for '${sample.body}'" } },
            { assertEquals(sample.timestamp, txn!!.datetime) { "datetime for '${sample.body}'" } },
            { assertTruePositiveAmount(txn!!) }
        )
    }

    private fun assertTruePositiveAmount(txn: Transaction) =
        org.junit.jupiter.api.Assertions.assertTrue(txn.amount > 0) {
            "parsed transaction must have a positive amount"
        }

    @Test
    fun `parsed count matches the is_transaction column`() {
        val samples = loadSamples()
        val parsedCount = samples.count { parser.parseSmsData(it.body, it.sender, it.timestamp) != null }
        assertEquals(samples.count { it.isTransaction }, parsedCount) {
            "Parsed count must match is_transaction column"
        }
    }

    companion object {
        @JvmStatic
        fun samples(): List<Sample> = loadSamples()

        private fun loadSamples(): List<Sample> {
            val stream = SmsParserTest::class.java.getResourceAsStream("/out.csv")
                ?: error("out.csv not found on test classpath")
            val rows = stream.bufferedReader().use { it.readText() }.let(::splitCsvRows)
            return rows.drop(1).filter { it.isNotEmpty() && it.size >= 8 }.map { cols ->
                val isTxn = cols[3].trim().toBooleanStrict()
                Sample(
                    sender = cols[0],
                    body = cols[1],
                    timestamp = cols[2].trim().toLong(),
                    isTransaction = isTxn,
                    expType = cols[4].trim().ifBlank { null }?.let { TransactionType.valueOf(it) },
                    expAmountPaise = cols[5].trim().ifBlank { null }?.toLong(),
                    expPayee = if (isTxn) cols[6] else null,
                    expAccount = if (isTxn) cols[7].trim().ifBlank { null } else null
                )
            }
        }

        /** Minimal RFC-4180-style CSV reader: handles quoted fields containing commas and "" escapes. */
        private fun splitCsvRows(text: String): List<List<String>> {
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
}
