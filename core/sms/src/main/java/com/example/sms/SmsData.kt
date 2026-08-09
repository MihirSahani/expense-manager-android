package com.example.sms

data class SmsData(
    val body: String,
    val sender: String,
    val timestamp: Long
)