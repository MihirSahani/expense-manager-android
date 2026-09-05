package com.example.sms

import android.content.ContextWrapper
import android.content.pm.PackageManager
import com.example.common.repository.TransactionRepository
import com.example.datastore.Setting
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * SoT regression test: pins [SmsParser.parseSmsData]'s `referenceId` extraction (backed by the
 * private `getRefNumber` rule table) against a snapshot recorded from every row of `out.csv`
 * (`refnumber_snapshot.csv`, generated once from the pre-refactor if/else implementation). This
 * exists so a future rewrite of the reference-number extraction logic can't silently change
 * behavior for any of the recorded real-world SMS bodies.
 */
class SmsParserRefNumberRegressionTest {

    private class GrantingContext : ContextWrapper(null) {
        override fun checkPermission(permission: String, pid: Int, uid: Int) =
            PackageManager.PERMISSION_GRANTED
        override fun checkSelfPermission(permission: String) =
            PackageManager.PERMISSION_GRANTED
    }

    private val parser = SmsParser(GrantingContext(), mockk<TransactionRepository>(relaxed = true), mockk<Setting>(relaxed = true))

    @TestFactory
    fun `referenceId matches recorded snapshot`(): List<DynamicTest> {
        val samples = loadCsv("/out.csv").drop(1).filter { it.isNotEmpty() && it.size >= 8 }
        val snapshot = loadCsv("/refnumber_snapshot.csv").drop(1).filter { it.isNotEmpty() && it.size >= 3 }
        check(samples.size == snapshot.size) {
            "out.csv (${samples.size} rows) and refnumber_snapshot.csv (${snapshot.size} rows) are out of sync"
        }

        return samples.zip(snapshot).map { (cols, snapshotCols) ->
            val sender = cols[0]
            val body = cols[1]
            val timestamp = cols[2].trim().toLong()
            val expectedRef = snapshotCols[2]
            val oneLine = body.replace(Regex("\\s+"), " ").trim()
            val name = "$sender | ${if (oneLine.length > 60) oneLine.take(57) + "..." else oneLine}"

            DynamicTest.dynamicTest(name) {
                val actualRef = parser.parseSmsData(body, sender, timestamp)?.referenceId ?: ""
                assertEquals(expectedRef, actualRef) { "referenceId for '$body'" }
            }
        }
    }

    private fun loadCsv(resource: String): List<List<String>> {
        val stream = javaClass.getResourceAsStream(resource) ?: error("$resource not found on test classpath")
        return stream.bufferedReader().use { it.readText() }.let(TestCsv::parseCsvRows)
    }
}
