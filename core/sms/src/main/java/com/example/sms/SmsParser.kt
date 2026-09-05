package com.example.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.core.content.ContextCompat
import com.example.common.repository.TransactionRepository
import com.example.core.database.entity.Transaction
import com.example.core.database.models.TransactionType
import com.example.datastore.Setting
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.roundToLong

class SmsParser @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repo: TransactionRepository,
    private val setting: Setting
) {

    val readingSmsMutex = Mutex()

    /** Backs [firstDigitsOnward]'s digit extraction in [getRefNumber]. */
    private val refNumberDigitsPattern = Pattern.compile("([0-9]+).*")

    init {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            throw Exception("Permission READ_SMS is not granted. Please request the permission before using SmsParser.")
        }
    }

    suspend fun parseSmses() = withContext(Dispatchers.IO) {
        readingSmsMutex.withLock {
            val lastSmsReadTime = setting.smsReadTime.first() + 1
            setting.setSmsReadTime(System.currentTimeMillis() / 1000)

            val cursor: Cursor? = getCursor(lastSmsReadTime)

            val rows = getArgsFromCursor(cursor)

            val transactions = coroutineScope {
                rows.map { (body, address, date) ->
                    async {
                        parseSmsData(body, address, date)
                    }
                }.awaitAll().filterNotNull()
            }
            saveToDatabase(transactions)
        }
    }

    private suspend fun saveToDatabase(transactions: List<Transaction>) {
        repo.create(transactions)
    }

    fun getCursor(startSeconds: Long = 0L): Cursor? {
        val startMillis = startSeconds * 1000
        val selection = "${Telephony.Sms.Inbox.DATE} >= ?"
        val selectionArgs = arrayOf(startMillis.toString())

        return context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(
                Telephony.Sms.Inbox.SUBSCRIPTION_ID,
                Telephony.Sms.Inbox.ADDRESS,
                Telephony.Sms.Inbox.DATE,
                Telephony.Sms.Inbox.BODY
            ),
            selection, selectionArgs, Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )
    }

    fun getArgsFromCursor(cursor: Cursor?): ArrayList<Triple<String, String, Long>> {
        val rows = ArrayList<Triple<String, String, Long>>()
        cursor?.use {
            while (it.moveToNext()) {
                val address = it.getString(1)
                val dateSeconds = it.getLong(2) / 1000
                val body = it.getString(3)
                rows.add(Triple(body, address, dateSeconds))
            }
        }
        return rows
    }

    suspend fun saveTransactionsFromMessages(messages: Array<out SmsMessage>) = withContext(Dispatchers.IO) {
        val transactions = coroutineScope {
            messages.map { message ->
                async {
                    parseSmsData(message.messageBody, message.originatingAddress ?: "", message.timestampMillis)
                }
            }.awaitAll().filterNotNull()
        }
        saveToDatabase(transactions)
    }

    fun parseSmsData(body: String, sender: String, timestamp: Long): Transaction? {
        val regEx = Pattern.compile("(?i)(?:RS|INR|MRP)?(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)+")
        // Find instance of pattern matches
        val transaction = Transaction(
            amount = 0L,
            categoryId = null,
            datetime = timestamp,
            rawAccountNo = null,
            accountId = null,
            payee = "",
            transactionType = TransactionType.CREDIT,
            referenceId = null,
            description = body
        )
        val m = regEx.matcher(body)
        if (m.find()) {
            try {
                if (checkSenderIsValid(sender)) {
                    if (!body.contains("stmt", true)) {
                        // found out debit and credit
                        val (transactionType, amount) = getAmountAndType(body, transaction, m)
                        if (transactionType == null) return null
                        transaction.transactionType = transactionType
                        transaction.amount = amount
                        // transaction.parsed = "1"
                        transaction.rawAccountNo = getRawAccountNumber(body)
                        // check message is otp or not
                        if (!body.contains("OTP", true)
                            && !body.contains("minimum", true)
                            && !body.contains("importance", true)
                            && !body.contains("request", true)
                            && !body.contains(Regex("(?i)\\blimit\\b"))
                            && !body.contains("convert", true)
                            && !body.contains("emi", true)
                            && !body.contains("avoid paying", true)
                            && !body.contains("autopay", true)
                            && !body.contains("declined", true)
                            && !body.contains("will be deducted", true)
                            && !body.contains("E-statement", true)
                            && !body.contains("funds are blocked", true)
                            && !body.contains("SmartPay", true)
                            && !body.contains("We are pleased to inform that", true)
                            && !body.contains("has been opened", true)
                            // && transaction.transactionType != null
                        ) {
                            // bank wise filter
                            // getAvailableBalance(transaction)
                            transaction.referenceId = getRefNumber(body)
                            transaction.payee = getPayee(body, transaction.transactionType!!)
                            return transaction
                        } else {
                            return null
                        }

                    } else {
                        return null
                    }
                } else {
                    return null
                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return null
            }
        } else {
            return null
        }
    }

    private fun checkSenderIsValid(sender: String): Boolean {
        return (sender.trim().contains("+918586980859", true)
                || sender.contains("08586980869", true)
                || sender.contains("085869", true)
                || sender.contains("ICICIB", true)
                || sender.contains("HDFCBK", true)
                || sender.contains("SBIINB", true)
                || sender.contains("SBMSMS", true)
                || sender.contains("SCISMS", true)
                || sender.contains("CBSSBI", true)
                || sender.contains("SBIPSG", true)
                || sender.contains("SBIUPI", true)
                || sender.contains("SBICRD", true)
                || sender.contains("ATMSBI", true)
                || sender.contains("QPMYAMEX", true)
                || sender.contains("IDFCFB", true)
                || sender.contains("UCOBNK", true)
                || sender.contains("CANBNK", true)
                || sender.contains("BOIIND", true)
                || sender.contains("AXISBK", true)
                || sender.contains("PAYTMB", true)
                || sender.contains("UnionB", true)
                || sender.contains("INDBNK", true)
                || sender.contains("KOTAKB", true)
                || sender.contains("CENTBK", true)
                || sender.contains("SCBANK", true)
                || sender.contains("PNBSMS", true)
                || sender.contains("DOPBNK", true)
                || sender.contains("YESBNK", true)
                || sender.contains("IDBIBK", true)
                || sender.contains("ALBANK", true)
                || sender.contains("CITIBK", true)
                || sender.contains("ANDBNK", true)
                || sender.contains("BOBTXN", true)
                || sender.contains("IOBCHN", true)
                || sender.contains("MAHABK", true)
                || sender.contains("OBCBNK", true)
                || sender.contains("RBLBNK", true)
                || sender.contains("RBLCRD", true)
                || sender.contains("SPRCRD", true)
                || sender.contains("HSBCBK", true)
                || sender.contains("HSBCIN", true)
                || sender.contains("INDUSB", true))
    }

    private fun getFirstWord(text: String) = text.substringBefore(' ').trimStart()

    private fun getAvailableBalance(transaction: Transaction, body: String) {
        val regEx =
            Pattern.compile("(?i)(?:RS|INR|MRP)?(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)+")
        // Find instance of pattern matches
        if (body.contains("curr o/s - ", true)) {
            val newBody = body.split("o/s - ")
            val m = regEx.matcher(newBody[1].trim())
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                // transaction.avlBal = amount
            }
        } else if (body.contains("The Balance is", true)) {
            val newBody = body.split("The Balance is ")
            val m = regEx.matcher(newBody[1].trim())
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                // transaction.avlBal = amount
            }
        } else if (body.contains("The Available Balance is", true)) {
            val newBody = body.split("The Available Balance is ")
            val m = regEx.matcher(newBody[1].trim())
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                // transaction.avlBal = amount
            }
        } else if (body.contains("Avbl Lmt:", true)) {
            val newBody = body.split("Avbl Lmt:")
            val m = regEx.matcher(newBody[1].trim())
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                // transaction.avlBal = amount
            }
        } else if (body.contains("Avlbal", true)) {
            val newBody = body.split("Avlbal")
            val m = regEx.matcher(newBody[1].trim())
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                // transaction.avlBal = amount
            }
        } else if (body.contains("balance is", true)) {
            val newBody = body.lowercase(Locale.getDefault()).split("balance is ")
            val m = regEx.matcher(newBody[1].trim())
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                // transaction.avlBal = amount
            }
        } else if (body.contains("AvBl Bal:", true)) {
            val newBody = body.lowercase(Locale.getDefault()).split("avbl bal: ")
            val m = regEx.matcher(newBody[1].trim())
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                // transaction.avlBal = amount
            }
        } else if (body.contains("Avl. Bal:", true)) {
            val newBody = body.lowercase(Locale.getDefault()).split("avl. bal:")
            val m = regEx.matcher(newBody[1].trim())
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                // transaction.avlBal = amount
            }
        } else if (body.contains("AVl BAL", true)) {
            if (body.contains("Avl. Bal:", true)) {
                val newBody = body.lowercase(Locale.getDefault()).split("avl. bal:")
                val m = regEx.matcher(newBody[1].trim())
                if (m.find()) {
                    var amount = m.group(0).replace("inr".toRegex(), "")
                    amount = amount.replace("rs".toRegex(), "")
                    amount = amount.replace("inr".toRegex(), "")
                    amount = amount.replace(" ".toRegex(), "")
                    amount = amount.replace(",".toRegex(), "")
                    // transaction.avlBal = amount
                }
            } else {
                val newBody = body.lowercase(Locale.getDefault()).split("avl bal ")
                val m = regEx.matcher(newBody[1].trim())
                if (m.find()) {
                    var amount = m.group(0).replace("inr".toRegex(), "")
                    amount = amount.replace("rs".toRegex(), "")
                    amount = amount.replace("inr".toRegex(), "")
                    amount = amount.replace(" ".toRegex(), "")
                    amount = amount.replace(",".toRegex(), "")
                    // transaction.avlBal = amount
                }
            }
        } else if (body.contains("Avail Bal", true)) {
            val newBody = body.lowercase(Locale.getDefault()).split("avail bal ")
            val m = regEx.matcher(newBody[1].trim().replace("\\s".toRegex(), ""))
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                // transaction.avlBal = amount
            }
        } else if (body.contains("The combine BAL is", true)) {
            val newBody = body.lowercase(Locale.getDefault()).split("bal is ")
            val m = regEx.matcher(newBody[1].trim())
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                // transaction.avlBal = amount
            }
        } else if (body.contains("The balance in", true)) {
            val newBody = body.lowercase(Locale.getDefault()).split("balance in ")
            val m = regEx.matcher(newBody[1].trim())
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                // transaction.avlBal = "N/A"
                transaction.amount = amount.toDoubleOrNull()?.times(100)?.toLong() ?: 0L
                // transaction.transactionType = "balance"
            }
        } else if (body.contains("Available balance:", true)) {
            val newBody = body.lowercase(Locale.getDefault()).split("available balance:")
            val m = regEx.matcher(newBody[1].trim())
            if (m.find()) {
                var amount = m.group(0).replace("inr".toRegex(), "")
                amount = amount.replace("rs".toRegex(), "")
                amount = amount.replace("inr".toRegex(), "")
                amount = amount.replace(" ".toRegex(), "")
                amount = amount.replace(",".toRegex(), "")
                if (body.contains("credited", true)
                    || body.contains("cash deposit", true)
                ) {
                    transaction.transactionType = TransactionType.CREDIT
                    // transaction.avlBal = amount
                } else if (body.contains("debited", true)
                    || body.contains("withdrawn", true)
                ) {
                    transaction.transactionType = TransactionType.DEBIT
                    // transaction.avlBal = amount
                } else {
                    // transaction.transactionType = "balance"
                    // transaction.avlBal = "N/A"
                    transaction.amount = amount.toDoubleOrNull()?.times(100)?.toLong() ?: 0L
                }
            }
        } else {
            // transaction.avlBal = "N/A"
        }

    }

    /**
     * The text after the first occurrence of [keyword] in [body] (optionally lowercased first).
     * Mirrors the `val parts = body.split(keyword); val data = if (parts.size > 1) parts[1] else
     * parts[0]` shape repeated throughout [getRefNumber]'s branches. When [requireExactlyTwoParts]
     * is `true`, a [keyword] occurring more than once falls back to the text *before* the first
     * occurrence instead (matching call sites that originally checked `parts.size == 2`).
     */
    private fun segmentAfter(
        body: String,
        keyword: String,
        lowercase: Boolean = true,
        requireExactlyTwoParts: Boolean = false,
        trim: Boolean = false
    ): String {
        val source = if (lowercase) body.lowercase(Locale.getDefault()) else body
        val parts = source.split(keyword)
        val data = if (requireExactlyTwoParts) {
            if (parts.size == 2) parts[1] else parts[0]
        } else {
            if (parts.size > 1) parts[1] else parts[0]
        }
        return if (trim) data.trim() else data
    }

    /**
     * Cuts [data] at the first terminator (checked case-insensitively) from [terminators] whose
     * (check, split) pair matches, prepending [prefix]; falls back to [fallback] (the untouched
     * [data] by default) if none match. Mirrors the `when { data.contains(x, true) ->
     * data.split(y)[0] ... else -> data }` shape repeated throughout [getRefNumber]'s branches.
     * Most terminators check and split on the same string; a few original branches checked for
     * one string but split on another, so entries are (checkString, splitString) pairs to
     * preserve that exactly.
     */
    private fun cutAtTerminator(
        data: String,
        vararg terminators: Pair<String, String>,
        prefix: String = "",
        fallback: String = data
    ): String {
        for ((check, splitOn) in terminators) {
            if (data.contains(check, ignoreCase = true)) {
                return prefix + data.split(splitOn)[0]
            }
        }
        return fallback
    }

    /** The substring of [s] starting at its first digit, mirroring the original
     * `Pattern.compile("([0-9]+).*")` + `Matcher.find()` digit-extraction used by the IMPS/RefNo
     * branches (greedy `.*` means it captures everything from the first digit to the end of that
     * line, not just the digits themselves). Returns `""` if [s] has no digit. */
    private fun firstDigitsOnward(s: String): String {
        val matcher = refNumberDigitsPattern.matcher(s)
        return if (matcher.find()) matcher.group() else ""
    }

    /**
     * Extracts a short human-readable reference string from a bank SMS. Each branch below
     * recognizes one keyword/phrase bank messages tend to use for their reference/transaction
     * number, pulls out the text around it, and cuts it at whichever terminator (". ", " on ",
     * ")", ...) appears first, using [segmentAfter]/[cutAtTerminator]/[firstDigitsOnward] to
     * factor out that repeated shape. Each branch still owns its exact keyword, terminator list,
     * and prefix/suffix wrapping, since those (and a handful of quirks like asymmetric
     * check/split strings or lowercase-only-for-splitting) differ per bank-message format and
     * are preserved exactly as before -- see [SmsParserRefNumberRegressionTest].
     */
    private fun getRefNumber(body: String): String {
        var refNumber = ""
        if (body.contains("NetBanking", true)) {
            val keyword = when {
                body.contains(" to ", true) -> " to "
                body.contains(" for ", true) -> " for "
                else -> null
            }
            if (keyword != null) {
                val data = segmentAfter(body, keyword)
                refNumber = cutAtTerminator(
                    data, ". " to ". ", " on " to " on ", ")" to ")",
                    fallback = "NetBanking"
                )
            }
        } else if (body.contains("Cash Deposit", true)) {
            refNumber = "Cash Deposit"
        } else if (body.contains("withdrawn", true)) {
            val data = segmentAfter(body, " at ")
            refNumber = "withdrawn at " + cutAtTerminator(data, "on" to " on", "." to ". ", ")" to ")")
        } else if (body.contains("towards", true)) {
            val data = segmentAfter(body, "towards ")
            refNumber = cutAtTerminator(data, " avl " to " avl ", ". " to ". ", "on" to " on", ")" to ")")
        } else if (body.contains("thru", true)) {
            if (!body.contains("thru clg", true)) {
                val data = segmentAfter(body, "thru ")
                refNumber = cutAtTerminator(data, ". " to ".", "on" to " on", ")" to ")")
            }
        } else if (body.contains("Credit card ending", true)) {
            refNumber = if (body.contains("has been", true) && body.contains("from", true)) {
                val data = segmentAfter(body, "from ")
                cutAtTerminator(data, " on" to " on", ". " to ". ", ")" to ")")
            } else if (body.contains("has been", true)) {
                val data = segmentAfter(body, "has been ")
                cutAtTerminator(data, "on" to " on", ")" to ")", ". " to ".")
            } else {
                val data = segmentAfter(body, "from ")
                cutAtTerminator(data, "on" to " on", ")" to ")", ". " to ".")
            }
        } else if (body.contains("NEFT", true)) {
            val data = segmentAfter(body, "neft", trim = true)
            refNumber = "NEFT " + cutAtTerminator(data, ")" to ")", "." to ". ", "-" to "-")
        } else if (body.contains("IMPS", true)) {
            refNumber = if (body.contains("Ref no")) {
                val parts = body.split("Ref no")
                val data = firstDigitsOnward(if (parts.size > 1) parts[1] else parts[0])
                cutAtTerminator(data, ")" to ")", "." to ".", prefix = "IMPS Ref no")
            } else {
                val parts = body.split("IMPS")
                val data = firstDigitsOnward(if (parts.size > 1) parts[1] else parts[0])
                cutAtTerminator(data, ")" to ")", "." to ". ", prefix = "IMPS ")
            }
        } else if (body.contains("RefNo", true)) {
            val parts = body.split("RefNo")
            val data = firstDigitsOnward(if (parts.size == 2) parts[1] else parts[0])
            refNumber = when {
                data.contains(")", true) -> "RefNo " + data.split(")")[0]
                data.contains(".", true) -> "RefNo " + data.split(". ")[0]
                data.contains("on", true) -> "RefNo " + data.lowercase(Locale.getDefault()).split(" on")[0]
                data.contains("has", true) -> "RefNo " + data.lowercase(Locale.getDefault()).split(" has")[0]
                else -> data
            }
        } else if (body.contains("Ref no", true)) {
            refNumber = if (body.contains("VPA", true)) {
                val data = segmentAfter(body, "vpa ")
                cutAtTerminator(data, "." to ". ", ")" to ")", "on" to " on", "has" to " has", prefix = "VPA ")
            } else {
                val data = segmentAfter(body, "ref no")
                cutAtTerminator(data, ")" to ")", "." to ". ", "on" to " on", "has" to " has", prefix = "Ref no")
            }
        } else if (body.contains("Ref#", true)) {
            val data = segmentAfter(body, "Ref#", lowercase = false, requireExactlyTwoParts = true)
            refNumber = cutAtTerminator(data, "on" to " on", "has" to " has", ")" to ")", "." to ".", prefix = "Ref no")
        } else if (body.contains("Info", true)) {
            val data = segmentAfter(body, "Info", lowercase = false, requireExactlyTwoParts = true)
            refNumber = cutAtTerminator(data, ")" to ")", "." to ".")
        } else if (body.contains("Received", true)) {
            if (body.contains("via", true)) {
                val data = segmentAfter(body, "via", lowercase = false)
                refNumber = "VIA " + cutAtTerminator(data, "on" to " on", ")" to ")", ". " to ".")
            } else if (body.contains("has been", true)) {
                val parts = body.split("has ")
                val data = if (parts.size > 1) parts[2] else parts[0]
                refNumber = cutAtTerminator(data, " on" to "on", ")" to ")", ". " to ".")
            }
        } else if (body.contains("ATM", true)) {
            if (body.contains("txn#", true)) {
                val parts = body.split("ATM")
                val data = if (parts.size > 1) {
                    if (parts.size > 2) parts[2] else parts[1]
                } else parts[0]
                refNumber = cutAtTerminator(data, "fm" to "fm", "has" to " has", ")" to ")", "." to ". ")
            } else if (body.contains("tx", true)) {
                val data = segmentAfter(body, "tx#", lowercase = false)
                refNumber = cutAtTerminator(
                    data, "fm " to "fm ", "for" to "for ", ")" to ")", "." to ". ", "has" to " has",
                    prefix = "ATM "
                )
            } else if (body.contains("withdrawn", true)) {
                val parts = body.lowercase(Locale.getDefault()).split("at ")
                val data = if (parts.size > 1) {
                    if (parts.size > 2) parts[2] else parts[1]
                } else parts[0]
                refNumber = when {
                    data.lowercase(Locale.getDefault()).contains("on", true) -> data.split(" on")[0]
                    data.lowercase(Locale.getDefault()).contains("has", true) -> data.split(" has")[0]
                    data.contains(")", true) -> data.split(")")[0]
                    data.contains(".", true) -> data.split(". ")[0]
                    else -> "ATM $data"
                }
            } else if (body.contains("has been", true)) {
                val data = segmentAfter(body, "by", trim = true)
                refNumber = cutAtTerminator(data, " on" to " on", ". " to ". ", ")" to ")")
            }
        } else if (body.contains("by transfer", true)) {
            refNumber = if (body.contains("Deposit by", true)) {
                val data = segmentAfter(body, "deposit by ")
                cutAtTerminator(data, " avl " to " avl ", "." to ".", "-" to "-", ")" to ")")
            } else {
                "Transfer"
            }
        } else if (body.contains("for UPI", true)) {
            val parts = body.lowercase(Locale.getDefault()).split("upi-")
            val data = if (parts.size == 2) parts[1] else parts[0].trim()
            refNumber = cutAtTerminator(data, ")" to ")", "." to ". ")
        } else if (body.contains("Credit Card", true)) {
            refNumber = if (body.contains("form", true)) {
                val data = segmentAfter(body, "from ", requireExactlyTwoParts = true)
                cutAtTerminator(data, "on" to " on", ")" to ")", "." to ". ")
            } else if (body.contains("spent", true)) {
                val data = segmentAfter(body, "at ")
                cutAtTerminator(data, " on " to " on ", ")" to ")", "." to ".")
            } else {
                val data = segmentAfter(body, "at")
                cutAtTerminator(data, " on " to " on ", ")" to ")", "." to ". ")
            }
        } else if (body.contains("payment", true) && !body.contains("spent", true)) {
            val data = segmentAfter(body, "for", trim = true)
            refNumber = cutAtTerminator(data, "-" to "-", ")" to ")", "." to ".")
        } else if (body.contains("spent", true)) {
            val data = segmentAfter(body, " at ", trim = true)
            refNumber = cutAtTerminator(data, " on " to " on ", "." to ". ", ")" to ")")
        } else if (body.contains("cheque Number", true) || body.contains("cheque No", true)) {
            refNumber = if (body.contains("cheque No", true)) {
                val data = segmentAfter(body, "cheque no ", requireExactlyTwoParts = true, trim = true)
                val temp = cutAtTerminator(data, "." to ".", "-" to "-", ")" to ")")
                "Cheque No " + getFirstWord(temp.trim())
            } else {
                val data = segmentAfter(body, "cheque number ", requireExactlyTwoParts = true, trim = true)
                cutAtTerminator(data, "." to ".", "-" to "-", ")" to ")")
            }
        } else if (body.contains("credit for", true)) {
            val data = segmentAfter(body, "credit for ", requireExactlyTwoParts = true, trim = true)
            refNumber = "Credit " + cutAtTerminator(data, " of " to " of ", "." to ".", "-" to "-", ")" to ")")
        } else if (body.contains("Deposit by ", true)) {
            val data = segmentAfter(body, "Deposit by ", trim = true)
            refNumber = cutAtTerminator(data, " avl " to " of ", "." to ".", "-" to "-", ")" to ")")
        } else if (body.contains("ref", true)) {
            val parts = body.lowercase(Locale.getDefault()).split("ref")
            val data = if (parts.size > 1) parts[1].trim() else parts[0]
            refNumber = "Ref " + getFirstWord(data)
        } else if (body.contains("cheque of", true)) {
            refNumber = "Cheque"
        } else if (body.contains("UPI", true)) {
            val data = segmentAfter(body, "upi", trim = true)
            refNumber = "UPI" + cutAtTerminator(data, "." to ".", "-" to "-", ")" to ")")
        } else if (body.contains("Credited", true)) {
            val data = segmentAfter(body, " account of ", trim = true)
            refNumber = "Credited:" + cutAtTerminator(data, "a/c" to "a/c", "." to ".", "-" to "-", ")" to ")")
        } else if (body.contains("deducted", true)) {
            val data = segmentAfter(body, " for ", trim = true)
            refNumber = "Credited:" + cutAtTerminator(data, "a/c" to "a/c", "." to ".", "-" to "-", ")" to ")")
        }

        return refNumber

    }

    private fun getPayee(body: String, transactionType: TransactionType): String {
        // Normalize whitespace (newlines/tabs -> spaces) so markers like " to " match even when
        // the counterparty starts on its own line, and drop the trailing security footer
        // ("Not You? Call ... to 1800258XXXX") that otherwise gets mistaken for the payee.
        var normalized = body.replace(Regex("\\s"), " ")
        Regex("(?i)\\bnot\\s*you\\??").find(normalized)?.let {
            normalized = normalized.substring(0, it.range.first)
        }

        // 1) A UPI VPA (e.g. zomato@ybl, john@okhdfcbank) is the most reliable payee, if present.
        Regex("[a-zA-Z0-9._]{2,}@[a-zA-Z]{2,}").find(normalized)?.value?.let { return it }

        // 2) NEFT/IMPS references embed the counterparty, e.g.
        //    "NEFT Cr-CITI0000006-EMPLOYER NAME-MIHIR SAHANI-CITIN99999999999" — after the bank
        //    code the first field is the remitter/company name we want.
        Regex("(?i)\\b(?:NEFT|IMPS|RTGS)\\s+(?:Cr|Dr)-([^.\\n]+)").find(normalized)?.let { match ->
            val segments = match.groupValues[1].split("-").map { it.trim() }.filter { it.isNotEmpty() }
            if (segments.size >= 2) return segments[1].take(60)
        }

        // 2b) POS credit refunds put the merchant after the masked card, e.g.
        //     "for CRV POS 435584*****XX9906   AMAZON PAY." — grab the name up to the period.
        Regex("(?i)\\bPOS\\s+[0-9Xx*]+\\s+([A-Za-z][^.\\n]*)").find(normalized)?.let { match ->
            val name = match.groupValues[1].trim()
            if (name.isNotEmpty()) return name.take(60)
        }

        // 3) Otherwise look for a marker that introduces the counterparty, then cut at a delimiter.
        val markers = if (transactionType == TransactionType.CREDIT) {
            listOf("received from ", "from vpa ", "by vpa ", "neft-", "imps-", "info:", "from ", " by ")
        } else {
            listOf("paid to ", "trf to ", "to vpa ", "for vpa ", " to ", " at ", "towards ")
        }

        // A delimiter ends the payee: a date ("On 26/03/26" / "On 2026-03-30" or a bare date),
        // structural keywords (Ref/Bal/Avl/UPI/Info), or punctuation. Note we deliberately do NOT
        // cut on a lowercase "on"/"from" so names like "Amazon Pay on Delivery" survive.
        val delimiter = Regex(
            "(?i)\\s+(?:On\\s+\\d|Ref\\b|Bal\\b|Avl\\b|Avbl\\b|UPI\\b|Info:|mandate\\b)" +
                "|\\b\\d{1,4}[/-]\\d{1,2}[/-]\\d{2,4}\\b|[.;,)\\n]"
        )

        val lower = normalized.lowercase(Locale.getDefault())
        for (marker in markers) {
            val idx = lower.indexOf(marker)
            if (idx == -1) continue

            // Take the text after the marker, in the ORIGINAL case (preserve merchant name).
            val after = normalized.substring(idx + marker.length)

            // Cut at the first delimiter that ends a payee.
            val payee = delimiter.split(after).first().trim().trim('-', ':', '/')

            // Skip account-number noise like "a/c XX1234" so we keep searching for a real name.
            if (payee.isNotBlank() && !payee.matches(Regex("(?i)(a/c\\s*)?x*\\d+"))) {
                return payee.take(60)
            }
        }
        return "Unknown"   // fallback
    }

    private fun getAmountAndType(body: String, transaction: Transaction, m: Matcher): Pair<TransactionType?, Long> {
        if (body.contains("withdrawn", true)
            || body.contains("debited", true)
            || body.contains("spent", true)
            || body.contains(Regex("(?i)\\bsent\\b"))
            || body.contains("paying", true)
            || body.contains("payment", true)
            || body.contains("deducted", true)
            || body.contains("debited", true)
            || body.contains("purchase", true)
            || body.contains("dr", true)
            && !body.contains("otp", true)
            || body.contains("txn", true)
            || body.contains("transfer", true)
            && !body.contains("We are pleased to inform that", true)
            && !body.contains("has been opened", true)
        ) {
            val amount = m.group(1).replace(",".toRegex(), "")
            val formattedAmount = amount.toDoubleOrNull()?.times(100)?.roundToLong() ?: 0L
            return Pair(TransactionType.DEBIT, formattedAmount)
        } else if (body.contains("credited", true)
            || body.contains("cr", true)
            || body.contains("deposited", true)
            || body.contains("deposit", true)
            || body.contains("received", true)
            && !body.contains("otp", true)
            && !body.contains("emi", true)
        ) {
            val amount = m.group(1).replace(",".toRegex(), "")
            val formattedAmount = amount.toDoubleOrNull()?.times(100)?.roundToLong() ?: 0L
            return when {
                body.contains("UPDATE:AVAILABLE Bal in", true) -> {
                    Pair(null, formattedAmount)
                }

                body.contains("UPDATE: AVAILABLE Bal in", true) -> {
                    Pair(null, formattedAmount)
                }

                else -> {
                    Pair(TransactionType.CREDIT, formattedAmount)
                }
            }
        }
        return Pair(null, 0L)
    }

    private fun getRawAccountNumber(body: String): String? {
        // The per-bank heuristics below return masked forms like "XX1234"/"4477";
        // callers want just the trailing digits, so drop mask chars (X/x/*) and spaces.
        val digits = resolveRawAccountNumber(body)?.filter { it.isDigit() }
        return digits?.ifBlank { null }
    }

    private fun resolveRawAccountNumber(body: String): String? {
        // Fast path for the common "A/c XX9906", "A/c 9385" and "Card XX9906" forms, covering both
        // masked (XX/**) and plain trailing-digit account references.
        Regex("(?i)\\b(?:A/?c|Card|Acct|Account)\\b\\.?\\s*(?:No\\.?\\s*)?((?:[Xx*]+\\d+)|\\d{3,})")
            .find(body)?.let { match ->
                val candidate = match.groupValues[1]
                if (candidate.any { it.isDigit() }) return candidate
            }
        when {
            body.contains("SBIDrCARD", true)
                    && body.contains("tx#", true) -> {
                val dataList = body.lowercase(Locale.getDefault()).split("sbidrcard ")
                val data: String = if (dataList.size > 1) {
                    dataList[1].trim()
                } else {
                    dataList[0]
                }
                return getFirstWord(data)
            }

            body.contains("Customer ID ", true) -> {
                val dataList = body.lowercase(Locale.getDefault()).split("customer id ")
                val data: String = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                return getFirstWord(data)
            }

            body.contains("Deposit No ", true) -> {
                val dataList = body.lowercase(Locale.getDefault()).split("deposit no ")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                return getFirstWord(data)
            }

            body.contains("credit card ending", true) -> {
                val dataList = body.split("ending ")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                return getFirstWord(data)
            }

            body.contains("UPI", true) -> {
                val dataList = body.split("frm")
                val p1 =
                    Pattern.compile("[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}")
                var data = ""
                if (dataList.size == 2) {
                    val m1 = p1.matcher(dataList[1])
                    while (m1.find()) {
                        data = m1.group()
                        break
                    }
                } else {
                    val m1 = p1.matcher(dataList[0])
                    while (m1.find()) {
                        data = m1.group()
                        break
                    }
                }
                return data
            }

            body.contains("a/c", true) -> {
                if (body.contains("no.", true)) {
                    val dataList = body.split("no.")
                    val p1 =
                        Pattern.compile("[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}")
                    var data = ""
                    if (dataList.size == 2) {
                        val m1 = p1.matcher(dataList[1])
                        while (m1.find()) {
                            data = m1.group()
                            break
                        }
                    } else {
                        val m1 = p1.matcher(dataList[0])
                        while (m1.find()) {
                            data = m1.group()
                            break
                        }
                    }
                    return data
                } else if (body.contains("A/c No", true)) {
                    if (body.contains("XX", true)) {
                        val dataList = body.split("A/c No")
                        val p1 =
                            Pattern.compile("[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}")
                        var data = ""
                        if (dataList.size == 2) {
                            val m1 = p1.matcher(dataList[1])
                            while (m1.find()) {
                                data = m1.group()
                                break
                            }
                        } else {
                            val m1 = p1.matcher(dataList[0])
                            while (m1.find()) {
                                data = m1.group()
                                break
                            }
                        }
                        return data
                    } else {
                        val dataList = body.split("a/c no ")
                        var data = ""
                        data = if (dataList.size > 1) {
                            dataList[1]
                        } else {
                            dataList[0]
                        }
                        return data.lowercase(Locale.getDefault()).split("as")[0]
                    }

                } else if (body.contains("a/c no ", true)) {
                    val dataList = body.split("a/c no ")
                    val data = if (dataList.size > 1) {
                        dataList[1]
                    } else {
                        dataList[0]
                    }
                    return data.lowercase(Locale.getDefault()).split("as")[0]

                } else if (body.contains("a/c", true)) {
                    if (!body.contains("xx", true)
                        && !body.contains("x", true)
                    ) {

                        val dataList = body.lowercase(Locale.getDefault()).split("a/c ")
                        val data = if (dataList.size > 1) {
                            dataList[1]
                        } else {
                            dataList[0]
                        }
                        if (data.contains("as")) {
                            return data.lowercase(Locale.getDefault()).split("as")[0]
                        } else {
                            return getFirstWord(
                                data.lowercase(Locale.getDefault()).split("\\s")[0]
                            ).filter { it.isDigit() }
                        }
                    } else {
                        val dataList = body.lowercase(Locale.getDefault()).split("a/c ")
                        val p1 =
                            Pattern.compile("[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}")
                        var data = ""
                        if (dataList.size > 1) {
                            val m1 = p1.matcher(dataList[1])
                            while (m1.find()) {
                                data = m1.group()
                                break
                            }
                        } else {
                            val m1 = p1.matcher(dataList[0])
                            while (m1.find()) {
                                data = m1.group()
                                break
                            }
                        }
                        return data
                    }

                }
            }

            body.contains("Acct", true) -> {
                var dataList = body.split("Acct ")
                val p1 =
                    Pattern.compile("[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}")
                var data = ""
                if (dataList.size > 1) {
                    val m1 = p1.matcher(dataList[1])
                    while (m1.find()) {
                        data = m1.group()
                        break
                    }
                } else {
                    val m1 = p1.matcher(dataList[0])
                    while (m1.find()) {
                        data = m1.group()
                        break
                    }
                }
                return data
            }

            body.contains("Card ending", true) -> {
                if (body.contains("ending", true)) {
                    if (body.contains("XX", true)) {
                        val dataList =
                            body.lowercase(Locale.getDefault()).split("ending ")
                        val p1 =
                            Pattern.compile("[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}")
                        var data = ""
                        if (dataList.size > 1) {
                            val m1 = p1.matcher(dataList[1])
                            while (m1.find()) {
                                data = m1.group()
                                break
                            }
                        } else {
                            val m1 = p1.matcher(dataList[0])
                            while (m1.find()) {
                                data = m1.group()
                                break
                            }
                        }
                        return data
                    } else {
                        val dataList =
                            body.lowercase(Locale.getDefault()).split("ending ")
                        val data = dataList[1].trim()
                        return getFirstWord(data)
                    }

                } else if (body.contains("end", true)) {
                    val dataList = body.lowercase(Locale.getDefault()).split("end")
                    val p1 =
                        Pattern.compile("[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}")
                    var data = ""
                    if (dataList.size == 2) {
                        val m1 = p1.matcher(dataList[1])
                        while (m1.find()) {
                            data = m1.group()
                            break
                        }
                    } else {
                        val m1 = p1.matcher(dataList[0])
                        while (m1.find()) {
                            data = m1.group()
                            break
                        }
                    }
                    return data
                }

            }

            body.contains("account", true) -> {
                val dataList = body.lowercase(Locale.getDefault()).split("account ")
                val p1 =
                    Pattern.compile("[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}")
                var data = ""
                if (dataList.size > 1) {
                    val m1 = p1.matcher(dataList[1])
                    while (m1.find()) {
                        data = m1.group()
                        break
                    }
                } else {
                    val m1 = p1.matcher(dataList[0])
                    while (m1.find()) {
                        data = m1.group()
                        break
                    }
                }
                return data
            }

            body.contains("Card", true) -> {
                var dataList = body.lowercase(Locale.getDefault()).split("card")
                val p1 =
                    Pattern.compile("[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}")
                var data = ""
                if (dataList.size == 2) {
                    val m1 = p1.matcher(dataList[1])
                    while (m1.find()) {
                        data = m1.group()
                        break
                    }
                } else {
                    val m1 = p1.matcher(dataList[0])
                    while (m1.find()) {
                        data = m1.group()
                        break
                    }
                }
                return data
            }

            body.contains("Ac", true) -> {
                val dataList = body.lowercase(Locale.getDefault()).split("ac ")
                val p1 =
                    Pattern.compile("[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}")
                var data = ""
                if (dataList.size == 2) {
                    val m1 = p1.matcher(dataList[1])
                    while (m1.find()) {
                        data = m1.group()
                        break
                    }
                } else {
                    val m1 = p1.matcher(dataList[0])
                    while (m1.find()) {
                        data = m1.group()
                        break
                    }
                }
                return data
            }
        }
        return null
    }
}