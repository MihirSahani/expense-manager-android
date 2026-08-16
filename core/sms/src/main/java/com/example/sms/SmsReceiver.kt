package com.example.sms

import android.content.BroadcastReceiver
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: android.content.Intent?) {
        if (intent?.action == android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val request = OneTimeWorkRequestBuilder<ParseSmsWorker>()
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "parse_sms_worker",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request
            )
        }
    }
}