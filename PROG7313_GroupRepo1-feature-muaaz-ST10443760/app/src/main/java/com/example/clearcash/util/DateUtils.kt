package com.example.clearcash.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility object for date formatting and calendar operations.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
object DateUtils {

    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    /**
     * Formats a timestamp to a readable date string e.g. "22 Apr 2026"
     */
    fun formatDate(timestamp: Long): String {
        return displayFormat.format(Date(timestamp))
    }

    /**
     * Returns the current month as "YYYY-MM" e.g. "2026-04"
     */
    fun getCurrentMonth(): String {
        return monthFormat.format(Date())
    }

    /**
     * Returns the start of the current month as a timestamp.
     */
    fun getStartOfCurrentMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Returns the end of the current month as a timestamp.
     */
    fun getEndOfCurrentMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        return calendar.timeInMillis
    }

    /**
     * Returns start of a given day as timestamp.
     */
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Returns end of a given day as timestamp.
     */
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        return calendar.timeInMillis
    }
}