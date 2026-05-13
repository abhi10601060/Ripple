package com.app.ripple.util

import java.text.SimpleDateFormat
import java.util.*

fun millisToDateTime(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}