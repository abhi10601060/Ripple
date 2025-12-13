package com.app.ripple.presentation.shared

import java.text.SimpleDateFormat
import java.util.*

fun formatTimestamp(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    val now = calendar.timeInMillis
    val today = calendar.get(Calendar.DAY_OF_YEAR)
    val yesterday = today - 1

    calendar.timeInMillis = timestamp
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val year = calendar.get(Calendar.YEAR)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    return when {
        dayOfYear == today -> {
            // Format as HH:MM for today
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
        dayOfYear == yesterday && year == currentYear -> {
            "Yesterday"
        }
        else -> {
            // Format as dd/MM/yy for older dates
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
