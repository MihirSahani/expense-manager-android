package com.example.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.example.core.database.entity.Transaction
import com.example.core.database.models.TransactionType
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

class SmsParser(private val context: Context) {

    init {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            throw Exception("Permission READ_SMS is not granted. Please request the permission before using SmsParser.")
        }
    }

    fun parseAllSmsData(): ArrayList<Transaction> {
        val sms = ArrayList<Transaction>()

        val cursor: Cursor? = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(
                Telephony.Sms.Inbox.SUBSCRIPTION_ID,
                Telephony.Sms.Inbox.ADDRESS,
                Telephony.Sms.Inbox.DATE,
                Telephony.Sms.Inbox.BODY
            ),
            null, null, Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )
        val totalSMS: Int = cursor?.count ?: 0
        if (cursor != null && cursor.moveToFirst()) {
            for (i in 0 until totalSMS) {
                val address = cursor.getString(1)
                val date = cursor.getLong(2)
                val body = cursor.getString(3)
                val transaction = parseSmsData(body, address, date)
                if (transaction != null) {
                    sms.add(transaction)
                }
                cursor.moveToNext()
            }
        } else {
            cursor?.close()
            return ArrayList()
        }
        cursor.close()
        return sms
    }

    fun parseSingleSmsData(): Transaction? {
        val cursor: Cursor? = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(
                Telephony.Sms.Inbox.SUBSCRIPTION_ID,
                Telephony.Sms.Inbox.ADDRESS,
                Telephony.Sms.Inbox.DATE,
                Telephony.Sms.Inbox.BODY
            ),
            null, null, Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )
        if (cursor != null && cursor.moveToFirst()) {
            val address = cursor.getString(1)
            val date = cursor.getLong(2)
            val body = cursor.getString(3)
            val transaction = parseSmsData(body, address, date)
            cursor.close()
            return transaction
        }
        cursor?.close()
        return null
    }

    private fun findCreditCardOrDebitCard(msg: String, sender: String): String {
        if (sender.trim().contains("+918586980859", true)
            || sender.contains("08586980869", true)
            || sender.contains("085869", true)
            || sender.contains("ICICIB", true)
            || sender.contains("HDFCBK", true)
            || sender.contains("SBMSMS", true)
            || sender.contains("SBIINB", true)
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
            || sender.contains("INDUSB", true)
        ) {
            return if (msg.contains("CREDIT CARD", ignoreCase = true) ||
                msg.contains("SBICARD", ignoreCase = true)
            ) {
                "credit card"
            } else {
                "debit card"
            }
        }
        return ""

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

    private fun getFirstWord(text: String): String {
        val index = text.indexOf(' ')
        return if (index > -1) { // Check if there is more than one word.
            text.substring(0, index).trim { it <= ' ' } // Extract first word.
        } else {
            text// Text is the first word itself.
        }
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
            transactionType = null,
            referenceId = null,
            description = body
        )
        val m = regEx.matcher(body)
        if (m.find()) {
            try {
                if (checkSenderIsValid(sender)) {
                    if (!body.contains("stmt", true)) {
                        // found out debit and credit
                        getAmountAndType(body, transaction, m)
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
                            && transaction.transactionType != null
                        ) {
                            // bank wise filter
                            // getAvailableBalance(transaction)
                            transaction.referenceId = getRefNumber(body)
                            transaction.payee = getPayee(body, transaction.transactionType!!)
                            // val cardType =
                            //     findCreditCardOrDebitCard(body, sender)
                            // transaction.cardType = cardType
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

    private fun getAvailableBalance(transaction: Transaction, body: String) {
        val regEx =
            Pattern.compile("(?i)(?:RS|INR|MRP)?(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)+")
        // Find instance of pattern matches
        if (body.contains("curr o/s - ", true)) {
            var newBody = body.split("o/s - ")
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
            var newBody = body.split("The Balance is ")
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
            var newBody = body.split("The Available Balance is ")
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
            var newBody = body.split("Avbl Lmt:")
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
            var newBody = body.split("Avlbal")
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
            var newBody = body.lowercase(Locale.getDefault()).split("balance is ")
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
            var newBody = body.lowercase(Locale.getDefault()).split("avbl bal: ")
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
            var newBody = body.lowercase(Locale.getDefault()).split("avl. bal:")
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
                var newBody = body.lowercase(Locale.getDefault()).split("avl. bal:")
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
                var newBody = body.lowercase(Locale.getDefault()).split("avl bal ")
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
            var newBody = body.lowercase(Locale.getDefault()).split("avail bal ")
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
            var newBody = body.lowercase(Locale.getDefault()).split("bal is ")
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
            var newBody = body.lowercase(Locale.getDefault()).split("balance in ")
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
            var newBody = body.lowercase(Locale.getDefault()).split("available balance:")
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

    private fun getRefNumber(body: String): String {
        //Info, At, Linked to, NEFT, Ref, transfer from, transfer to, for, of, IMPS
        //Till dot Space, on, has
        var refNumber = ""
        if (body.contains("NetBanking", true)) {
            // refNumber = " NetBanking"
            if (body.lowercase(Locale.getDefault()).contains(" to ", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split(" to ")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                refNumber = when {
                    data.contains(". ", true) -> {
                        data.split(". ")[0]
                    }

                    data.contains(" on ", true) -> {
                        data.split(" on ")[0]
                    }

                    data.contains(")", true) -> {
                        data.split(")")[0]
                    }

                    else -> {
                        //data
                        "NetBanking"
                    }
                }
            } else if (body.lowercase(Locale.getDefault()).contains(" for ", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split(" for ")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                refNumber = when {
                    data.contains(". ", true) -> {
                        data.split(". ")[0]
                    }

                    data.contains(" on ", true) -> {
                        data.split(" on ")[0]
                    }

                    data.contains(")", true) -> {
                        data.split(")")[0]
                    }

                    else -> {
                        //data
                        "NetBanking"
                    }
                }
            }

        } else if (body.contains("Cash Deposit", true)) {
            refNumber = "Cash Deposit"
        } else if (body.contains("withdrawn", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split(" at ")
            val data = if (dataList.size > 1) {
                dataList[1]
            } else {
                dataList[0]
            }
            refNumber = "withdrawn at " + when {
                data.contains("on", true) -> {
                    data.split(" on")[0]
                }

                data.contains(".", true) -> {
                    data.split(". ")[0]
                }

                data.contains(")", true) -> {
                    data.split(")")[0]
                }

                else -> {
                    data
                }
            }
        } else if (body.contains("towards", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split("towards ")
            val data = if (dataList.size > 1) {
                dataList[1]
            } else {
                dataList[0]
            }
            refNumber = when {
                data.contains(" avl ", true) -> {
                    data.split(" avl ")[0]
                }

                data.contains(". ", true) -> {
                    data.split(". ")[0]
                }

                data.contains("on", true) -> {
                    data.split(" on")[0]
                }

                data.contains(")", true) -> {
                    data.split(")")[0]
                }

                else -> {
                    data
                }
            }

        } else if (body.contains("thru", true)) {
            if (!body.contains("thru clg", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("thru ")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                refNumber = when {
                    data.contains(". ", true) -> {
                        data.split(".")[0]
                    }

                    data.contains("on", true) -> {
                        data.split(" on")[0]
                    }

                    data.contains(")", true) -> {
                        data.split(")")[0]
                    }

                    else -> {
                        data
                    }
                }
            }

        } else if (body.contains("Credit card ending", true)) {
            if (body.contains("has been", true) && body.contains("from", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("from ")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                refNumber = when {
                    data.contains(" on", true) -> {
                        data.split(" on")[0]
                    }

                    data.contains(". ", true) -> {
                        data.split(". ")[0]
                    }

                    data.contains(")", true) -> {
                        data.split(")")[0]
                    }

                    else -> {
                        data
                    }
                }
            } else if (body.contains("has been", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("has been ")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                refNumber = when {
                    data.contains("on", true) -> {
                        data.split(" on")[0]
                    }

                    data.contains(")", true) -> {
                        data.split(")")[0]
                    }

                    data.contains(". ", true) -> {
                        data.split(".")[0]
                    }

                    else -> {
                        data
                    }
                }
            } else {
                val dataList = body.lowercase(Locale.getDefault()).split("from ")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                refNumber = when {
                    data.contains("on", true) -> {
                        data.split(" on")[0]
                    }

                    data.contains(")", true) -> {
                        data.split(")")[0]
                    }

                    data.contains(". ", true) -> {
                        data.split(".")[0]
                    }

                    else -> {
                        data
                    }
                }
            }

        } else if (body.contains("NEFT", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split("neft")
            //val p1 = Pattern.compile("([0-9]+).*")
            val data = if (dataList.size > 1) {
                dataList[1].trim()
            } else {
                dataList[0].trim()
            }
            refNumber = "NEFT " + when {
                data.contains(")", true) -> {
                    data.split(")")[0]
                }

                data.contains(".", true) -> {
                    data.split(". ")[0]
                }

                data.contains("-", true) -> {
                    data.split("-")[0]
                }

                else -> {
                    data
                }
            }
        } else if (body.contains("IMPS", true)) {
            if (body.contains("Ref no")) {
                val dataList = body.split("Ref no")
                val p1 = Pattern.compile("([0-9]+).*")
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
                refNumber = when {
                    data.contains(")", true) -> {
                        "IMPS Ref no" + data.split(")")[0]
                    }

                    data.contains(".", true) -> {
                        "IMPS Ref no" + data.split(".")[0]
                    }

                    else -> {
                        data
                    }
                }
            } else {
                val dataList = body.split("IMPS")
                val p1 = Pattern.compile("([0-9]+).*")
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
                when {
                    data.contains(")", true) -> {
                        refNumber = "IMPS " + data.split(")")[0]
                    }

                    data.contains(".", true) -> {
                        refNumber = "IMPS " + data.split(". ")[0]
                    }

                    else -> {
                        refNumber = data
                    }
                }
            }


        } else if (body.contains("RefNo", true)) {
            val dataList = body.split("RefNo")
            val p1 = Pattern.compile("([0-9]+).*")
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
            when {
                data.contains(")", true) -> {
                    refNumber = "RefNo " + data.split(")")[0]
                }

                data.contains(".", true) -> {
                    refNumber = "RefNo " + data.split(". ")[0]
                }

                data.contains("on", true) -> {
                    refNumber = "RefNo " + data.lowercase(Locale.getDefault()).split(" on")[0]
                }

                data.contains("has", true) -> {
                    refNumber = "RefNo " + data.lowercase(Locale.getDefault()).split(" has")[0]
                }

                else -> {
                    refNumber = data
                }
            }

        } else if (body.contains("Ref no", true)) {
            if (body.contains("VPA", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("vpa ")
                var data = ""
                data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                when {
                    data.contains(".", true) -> {
                        refNumber = "VPA " + data.split(". ")[0]
                    }

                    data.contains(")", true) -> {
                        refNumber = "VPA " + data.split(")")[0]
                    }

                    data.contains("on", true) -> {
                        refNumber = "VPA " + data.split(" on")[0]
                    }

                    data.contains("has", true) -> {
                        refNumber = "VPA " + data.split(" has")[0]
                    }

                    else -> {
                        refNumber = data
                    }
                }
            } else {
                val dataList = body.lowercase(Locale.getDefault()).split("ref no")
                var data = ""
                data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                when {
                    data.contains(")", true) -> {
                        refNumber = "Ref no" + data.split(")")[0]
                    }

                    data.contains(".", true) -> {
                        refNumber = "Ref no" + data.split(". ")[0]
                    }

                    data.contains("on", true) -> {
                        refNumber = "Ref no" + data.split(" on")[0]
                    }

                    data.contains("has", true) -> {
                        refNumber = "Ref no" + data.split(" has")[0]
                    }

                    else -> {
                        refNumber = data
                    }
                }
            }


        } else if (body.contains("Ref#", true)) {
            val dataList = body.split("Ref#")
            //val p1 = Pattern.compile("([0-9]+).*")
            var data = ""
            if (dataList.size == 2) {
                data = dataList[1]
            } else {
                data = dataList[0]
            }
            when {

                data.replaceFirstChar { it.lowercase() }.contains("on", true) -> {
                    refNumber = "Ref no" + data.split(" on")[0]
                }

                data.replaceFirstChar { it.lowercase() }.contains("has", true) -> {
                    refNumber = "Ref no" + data.split(" has")[0]
                }

                data.contains(")", true) -> {
                    refNumber = "Ref no" + data.split(")")[0]
                }

                data.contains(".", true) -> {
                    refNumber = "Ref no" + data.split(".")[0]
                }

                else -> {
                    refNumber = data
                }
            }
        } else if (body.contains("Info", true)) {
            val dataList = body.split("Info")
            //val p1 = Pattern.compile("([0-9]+).*")
            val data = if (dataList.size == 2) {
                dataList[1]
            } else {
                dataList[0]
            }
            refNumber = when {
                data.contains(")", true) -> {
                    data.split(")")[0]
                }

                data.contains(".", true) -> {
                    data.split(".")[0]
                }

                else -> {
                    data
                }
            }
        } else if (body.contains("Received", true)) {
            if (body.contains("via", true)) {
                var dataList = body.split("via")
                //val p1 = Pattern.compile("([0-9]+).*")
                var data = ""
                data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                refNumber = "VIA " + when {
                    data.replaceFirstChar { it.lowercase() }.contains("on", true) -> {
                        data.split(" on")[0]
                    }

                    data.contains(")", true) -> {
                        data.split(")")[0]
                    }

                    data.contains(". ", true) -> {
                        data.split(".")[0]
                    }

                    else -> {
                        data
                    }
                }

            } else if (body.contains("has been", true)) {
                val dataList = body.split("has ")
                //val p1 = Pattern.compile("([0-9]+).*")
                var data = ""
                data = if (dataList.size > 1) {
                    dataList[2]
                } else {
                    dataList[0]
                }
                refNumber = when {
                    data.replaceFirstChar { it.lowercase() }.contains(" on", true) -> {
                        data.split("on")[0]
                    }

                    data.contains(")", true) -> {
                        data.split(")")[0]
                    }

                    data.contains(". ", true) -> {
                        data.split(".")[0]
                    }

                    else -> {
                        data
                    }
                }
            }

        } else if (body.contains("ATM", true)) {
            if (body.contains("txn#", true)) {
                val dataList = body.split("ATM")
                //val p1 = Pattern.compile("([0-9]+).*")
                var data = ""
                if (dataList.size > 1) {
                    if (dataList.size > 2) {
                        data = dataList[2]
                    } else {
                        data = dataList[1]
                    }
                } else {
                    data = dataList[0]
                }
                when {
                    data.replaceFirstChar { it.lowercase() }.contains("fm", true) -> {
                        refNumber = data.split("fm")[0]
                    }

                    data.replaceFirstChar { it.lowercase() }.contains("has", true) -> {
                        refNumber = data.split(" has")[0]
                    }

                    data.contains(")", true) -> {
                        refNumber = data.split(")")[0]
                    }

                    data.contains(".", true) -> {
                        refNumber = data.split(". ")[0]
                    }

                    else -> {
                        refNumber = data
                    }
                }
            } else if (body.contains("tx", true)) {
                val dataList = body.split("tx#")
                //val p1 = Pattern.compile("([0-9]+).*")
                var data = ""
                data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                when {
                    data.replaceFirstChar { it.lowercase() }.contains("fm ", true) -> {
                        refNumber = "ATM " + data.split("fm ")[0]
                    }

                    data.replaceFirstChar { it.lowercase() }.contains("for", true) -> {
                        refNumber = "ATM " + data.split("for ")[0]
                    }

                    data.contains(")", true) -> {
                        refNumber = "ATM " + data.split(")")[0]
                    }

                    data.contains(".", true) -> {
                        refNumber = "ATM " + data.split(". ")[0]
                    }

                    data.contains("has", true) -> {
                        refNumber = "ATM " + data.split(" has")[0]
                    }

                    else -> {
                        refNumber = data
                    }
                }
            } else if (body.contains("withdrawn", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("at ")
                //val p1 = Pattern.compile("([0-9]+).*")
                val data = if (dataList.size > 1) {
                    if (dataList.size > 2) {
                        dataList[2]
                    } else {
                        dataList[1]
                    }
                } else {
                    dataList[0]
                }
                when {
                    data.lowercase(Locale.getDefault()).contains("on", true) -> {
                        refNumber = data.split(" on")[0]
                    }

                    data.lowercase(Locale.getDefault()).contains("has", true) -> {
                        refNumber = data.split(" has")[0]
                    }

                    data.contains(")", true) -> {
                        refNumber = data.split(")")[0]
                    }

                    data.contains(".", true) -> {
                        refNumber = data.split(". ")[0]
                    }

                    else -> {
                        refNumber = "ATM $data"
                    }
                }
            } else if (body.contains("tx", true)) {
                val dataList = body.split("tx#")
                //val p1 = Pattern.compile("([0-9]+).*")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                when {
                    data.contains("fm ", true) -> {
                        refNumber = "ATM " + data.split("fm ")[0]
                    }

                    data.contains("for", true) -> {
                        refNumber = "ATM " + data.split("for ")[0]
                    }

                    data.contains(")", true) -> {
                        refNumber = "ATM ${data.split(")")[0]}"
                    }

                    data.contains(".", true) -> {
                        refNumber = "ATM ${data.split(". ")[0]}"
                    }

                    data.contains("has", true) -> {
                        refNumber = "ATM ${data.split(" has")[0]}"
                    }

                    else -> {
                        refNumber = data
                    }
                }
            } else if (body.contains("has been", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("by")
                val data = if (dataList.size > 1) {
                    dataList[1].trim()
                } else {
                    dataList[0].trim()
                }
                refNumber = when {
                    data.contains(" on", true) -> {
                        data.split(" on")[0]
                    }

                    data.contains(". ", true) -> {
                        data.split(". ")[0]
                    }

                    data.contains(")", true) -> {
                        data.split(")")[0]
                    }

                    else -> {
                        data
                    }
                }
            }

        } else if (body.contains("by transfer", true)) {
            if (body.contains("Deposit by", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("deposit by ")
                //val p1 = Pattern.compile("([0-9]+).*")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                refNumber = when {
                    data.contains(" avl ", true) -> {
                        data.split(" avl ")[0]
                    }

                    data.contains(".", true) -> {
                        data.split(".")[0]
                    }

                    data.contains("-", true) -> {
                        data.split("-")[0]
                    }

                    data.contains(")", true) -> {
                        data.split(")")[0]
                    }

                    else -> {
                        data
                    }
                }
            } else {
                refNumber = "Transfer"
            }
        } else if (body.contains("for UPI", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split("upi-")
            //val p1 = Pattern.compile("([0-9]+).*")
            val data = if (dataList.size == 2) {
                dataList[1]
            } else {
                dataList[0].trim()
            }
            refNumber = when {
                data.contains(")", true) -> {
                    data.split(")")[0]
                }

                data.contains(".", true) -> {
                    data.split(". ")[0]
                }

                else -> {
                    data
                }
            }
        } else if (body.contains("Credit Card", true)) {
            if (body.contains("Credit card ending", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("from ")
                //val p1 = Pattern.compile("([0-9]+).*")
                val data = if (dataList.size > 1) {
                    dataList[1].trim()
                } else {
                    dataList[0].trim()
                }
                when {
                    data.contains("on", true) -> {
                        refNumber = getFirstWord(data.split(" on")[0])
                    }

                    data.contains(")", true) -> {
                        refNumber = getFirstWord(data.split(")")[0])
                    }

                    data.contains(".", true) -> {
                        refNumber = getFirstWord(data.split(". ")[0])
                    }

                    else -> {
                        refNumber = getFirstWord(data)
                    }
                }

            } else if (body.contains("form", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("from ")
                //val p1 = Pattern.compile("([0-9]+).*")
                val data = if (dataList.size == 2) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                when {
                    data.contains("on", true) -> {
                        refNumber = data.split(" on")[0]
                    }

                    data.contains(")", true) -> {
                        refNumber = data.split(")")[0]
                    }

                    data.contains(".", true) -> {
                        refNumber = data.split(". ")[0]
                    }

                    else -> {
                        refNumber = data
                    }
                }
            } else if (body.contains("spent", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("at ")
                //val p1 = Pattern.compile("([0-9]+).*")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                when {
                    data.contains(" on ", true) -> {
                        refNumber = data.split(" on ")[0]
                    }

                    data.contains(")", true) -> {
                        refNumber = data.split(")")[0]
                    }

                    data.contains(".", true) -> {
                        refNumber = data.split(".")[0]
                    }

                    else -> {
                        refNumber = data
                    }
                }
            } else {
                val dataList = body.lowercase(Locale.getDefault()).split("at")
                //val p1 = Pattern.compile("([0-9]+).*")
                val data = if (dataList.size > 1) {
                    dataList[1]
                } else {
                    dataList[0]
                }
                when {
                    data.contains(" on ", true) -> {
                        refNumber = data.split(" on ")[0]
                    }

                    data.contains(")", true) -> {
                        refNumber = data.split(")")[0]
                    }

                    data.contains(".", true) -> {
                        refNumber = data.split(". ")[0]
                    }

                    else -> {
                        refNumber = data
                    }
                }
            }

        } else if (body.contains("payment", true) && !body.contains("spent", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split("for")
            //val p1 = Pattern.compile("([0-9]+).*")
            val data = if (dataList.size > 1) {
                dataList[1].trim()
            } else {
                dataList[0].trim()
            }
            refNumber = when {
                data.contains("-", true) -> {
                    data.split("-")[0]
                }

                data.contains(")", true) -> {
                    data.split(")")[0]
                }

                data.contains(".", true) -> {
                    data.split(".")[0]
                }

                else -> {
                    data
                }
            }
        } else if (body.contains("spent", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split(" at ")
            //val p1 = Pattern.compile("([0-9]+).*")
            val data = if (dataList.size > 1) {
                dataList[1].trim()
            } else {
                dataList[0].trim()
            }
            when {
                data.contains(" on ", true) -> {
                    refNumber = data.split(" on ")[0]
                }

                data.contains(".", true) -> {
                    refNumber = data.split(". ")[0]
                }

                data.contains(")", true) -> {
                    refNumber = data.split(")")[0]
                }

                else -> {
                    refNumber = data
                }
            }
        } else if (body.contains("cheque Number", true)
            || body.contains("cheque No", true)
        ) {
            if (body.contains("cheque No", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("cheque no ")
                //val p1 = Pattern.compile("([0-9]+).*")
                val data = if (dataList.size == 2) {
                    dataList[1].trim()
                } else {
                    dataList[0].trim()
                }
                val temp = when {
                    data.contains(".", true) -> {
                        data.split(".")[0]
                    }

                    data.contains("-", true) -> {
                        data.split("-")[0]
                    }

                    data.contains(")", true) -> {
                        data.split(")")[0]
                    }

                    else -> {
                        data
                    }
                }
                refNumber = "Cheque No " + getFirstWord(temp.trim())
            } else if (body.contains("cheque Number", true)) {
                val dataList = body.lowercase(Locale.getDefault()).split("cheque number ")
                //val p1 = Pattern.compile("([0-9]+).*")
                val data = if (dataList.size == 2) {
                    dataList[1].trim()
                } else {
                    dataList[0].trim()
                }
                when {
                    data.contains(".", true) -> {
                        refNumber = data.split(".")[0]
                    }

                    data.contains("-", true) -> {
                        refNumber = data.split("-")[0]
                    }

                    data.contains(")", true) -> {
                        refNumber = data.split(")")[0]
                    }

                    else -> {
                        refNumber = data
                    }
                }
            }


        } else if (body.contains("credit for", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split("credit for ")
            //val p1 = Pattern.compile("([0-9]+).*")
            val data = if (dataList.size == 2) {
                dataList[1].trim()
            } else {
                dataList[0].trim()
            }
            refNumber = "Credit " + when {
                data.contains(" of ", true) -> {
                    data.split(" of ")[0]
                }

                data.contains(".", true) -> {
                    data.split(".")[0]
                }

                data.contains("-", true) -> {
                    data.split("-")[0]
                }

                data.contains(")", true) -> {
                    data.split(")")[0]
                }

                else -> {
                    data
                }
            }
        } else if (body.contains("Deposit by ", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split("Deposit by ")
            //val p1 = Pattern.compile("([0-9]+).*")
            val data = if (dataList.size > 1) {
                dataList[1].trim()
            } else {
                dataList[0].trim()
            }
            refNumber = when {
                data.contains(" avl ", true) -> {
                    data.split(" of ")[0]
                }

                data.contains(".", true) -> {
                    data.split(".")[0]
                }

                data.contains("-", true) -> {
                    data.split("-")[0]
                }

                data.contains(")", true) -> {
                    data.split(")")[0]
                }

                else -> {
                    data
                }
            }
        } else if (body.contains("ref", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split("ref")
            //val p1 = Pattern.compile("([0-9]+).*")
            val data = if (dataList.size > 1) {
                dataList[1].trim()
            } else {
                dataList[0]
            }

            refNumber = "Ref " + getFirstWord(data)
        } else if (body.contains("cheque of", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split("cheque of ")

            refNumber = "Cheque"
        } else if (body.contains("UPI", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split("upi")
            //val p1 = Pattern.compile("([0-9]+).*")
            val data = if (dataList.size > 1) {
                dataList[1].trim()
            } else {
                dataList[0].trim()
            }
            refNumber = "UPI" + when {
                data.contains(".", true) -> {
                    data.split(".")[0]
                }

                data.contains("-", true) -> {
                    data.split("-")[0]
                }

                data.contains(")", true) -> {
                    data.split(")")[0]
                }

                else -> {
                    data
                }
            }
        } else if (body.contains("Credited", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split(" account of ")
            //val p1 = Pattern.compile("([0-9]+).*")
            val data = if (dataList.size > 1) {
                dataList[1].trim()
            } else {
                dataList[0].trim()
            }
            refNumber = "Credited:" + when {
                data.contains("a/c", true) -> {
                    data.split("a/c")[0]
                }

                data.contains(".", true) -> {
                    data.split(".")[0]
                }

                data.contains("-", true) -> {
                    data.split("-")[0]
                }

                data.contains(")", true) -> {
                    data.split(")")[0]
                }

                else -> {
                    data
                }
            }
        } else if (body.contains("deducted", true)) {
            val dataList = body.lowercase(Locale.getDefault()).split(" for ")
            //val p1 = Pattern.compile("([0-9]+).*")
            val data = if (dataList.size > 1) {
                dataList[1].trim()
            } else {
                dataList[0].trim()
            }
            refNumber = "Credited:" + when {
                data.contains("a/c", true) -> {
                    data.split("a/c")[0]
                }

                data.contains(".", true) -> {
                    data.split(".")[0]
                }

                data.contains("-", true) -> {
                    data.split("-")[0]
                }

                data.contains(")", true) -> {
                    data.split(")")[0]
                }

                else -> {
                    data
                }
            }
        }

        // var dataList = smsDto.body.split("Ref")

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

    private fun getAmountAndType(body: String, transaction: Transaction, m: Matcher) {
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
            transaction.amount = amount.toDoubleOrNull()?.times(100)?.let { Math.round(it) } ?: 0L
            transaction.transactionType = TransactionType.DEBIT
        } else if (body.contains("credited", true)
            || body.contains("cr", true)
            || body.contains("deposited", true)
            || body.contains("deposit", true)
            || body.contains("received", true)
            && !body.contains("otp", true)
            && !body.contains("emi", true)
        ) {
            var amount = m.group(1).replace(",".toRegex(), "")
            transaction.amount = amount.toDoubleOrNull()?.times(100)?.let { Math.round(it) } ?: 0L
            when {
                body.contains("UPDATE:AVAILABLE Bal in", true) -> {
                }

                body.contains("UPDATE: AVAILABLE Bal in", true) -> {
                }

                else -> {
                    transaction.transactionType = TransactionType.CREDIT
                }
            }

        }
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