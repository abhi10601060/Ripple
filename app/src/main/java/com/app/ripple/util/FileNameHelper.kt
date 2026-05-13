package com.app.ripple.util

import kotlin.random.Random

fun getRippleFormattedFileName(fileName: String) : String{
    if (fileName.isEmpty()) return ""

    val splits = fileName.split(".")

    return "${splits[0]}_Ripple_${getUniqueStringFromTime()}.${splits[1]}"
}

fun getUniqueStringFromTime(): String {
    val timestamp = System.currentTimeMillis()
    val randomSuffix = Random.nextInt(10000, 99999)  // 5-digit random for extra uniqueness
    return timestamp.toString(16) + randomSuffix.toString(16).padStart(4, '0')
}