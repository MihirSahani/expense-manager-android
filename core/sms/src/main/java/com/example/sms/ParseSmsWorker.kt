package com.example.sms

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ParseSmsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val smsParser: SmsParser,
    private val intent: Intent
) : CoroutineWorker(context, workerParams) {

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