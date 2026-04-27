package com.clearcash.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val displayFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val monthFmt   = SimpleDateFormat("MMMM yyyy",   Locale.getDefault())

    fun formatForDisplay(ts: Long): String = displayFmt.format(Date(ts))
    fun getCurrentMonthLabel(): String     = monthFmt.format(Date())

    fun getStartOfMonth(month: Int, year: Int): Long {
        val c = Calendar.getInstance()
        c.set(year, month - 1, 1, 0, 0, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
    fun getEndOfMonth(month: Int, year: Int): Long {
        val c = Calendar.getInstance()
        c.set(year, month - 1, 1, 23, 59, 59); c.set(Calendar.MILLISECOND, 999)
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
        return c.timeInMillis
    }
    fun getStartOfDay(ts: Long): Long {
        val c = Calendar.getInstance(); c.timeInMillis = ts
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0);      c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
    fun getEndOfDay(ts: Long): Long {
        val c = Calendar.getInstance(); c.timeInMillis = ts
        c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59);       c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }
    fun getCurrentMonth() = Calendar.getInstance().get(Calendar.MONTH) + 1
    fun getCurrentYear()  = Calendar.getInstance().get(Calendar.YEAR)
}