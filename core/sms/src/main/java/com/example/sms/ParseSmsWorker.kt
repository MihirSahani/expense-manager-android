package com.example.sms

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker that parses transactions out of the SMS messages carried by [intent]
 * (typically an `SMS_RECEIVED` broadcast) and persists them via [smsParser].
 */
@HiltWorker
class ParseSmsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val smsParser: SmsParser,
    private val intent: Intent
) : CoroutineWorker(context, workerParams) {

    /**
     * Extracts SMS messages from [intent] and saves any transactions found in them.
     *
     * @return [Result.success] once transactions are saved, or [Result.retry] if an error occurs.
     */
    override suspend fun doWork(): Result {
        return try {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            smsParser.saveTransactionsFromMessages(messages)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}