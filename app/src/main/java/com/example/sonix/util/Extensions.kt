package com.example.sonix.util
import java.util.concurrent.TimeUnit

fun Long.toFormattedDuration(): String {
    if (this <= 0L) return "0:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return "%d:%02d".format(minutes, seconds)
}

fun Long.toFormattedDurationLong(): String {
    if (this <= 0L) return "0:00:00"
    val hours   = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}