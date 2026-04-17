package com.clearcash.app.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Uses Java NumberFormat with the South African locale (ZAR — "R").
 * Required by Part 2: "Use NumberFormat in the app."
 */
object CurrencyFormatter {
    private val zarFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    fun format(amount: Double): String = zarFormat.format(amount)
    fun formatPlain(amount: Double): String = String.format(Locale.getDefault(), "%.2f", amount)
}