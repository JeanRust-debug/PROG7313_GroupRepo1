package com.clearcash.app.utils

object ValidationUtils {

    fun validateRequired(value: String, fieldName: String): String? {
        return if (value.isBlank()) "$fieldName is required" else null
    }

    fun validateAmount(value: String): String? {
        val amount = value.toDoubleOrNull()
        return when {
            amount == null -> "Invalid amount"
            amount <= 0 -> "Amount must be greater than zero"
            else -> null
        }
    }

    fun validateMinMax(min: Double, max: Double): String? {
        return if (max <= min) "Maximum goal must be greater than minimum goal" else null
    }

    fun validateCategoryName(name: String): String? {
        return when {
            name.length < 2 -> "Category name too short"
            name.length > 30 -> "Category name too long"
            else -> null
        }
    }
}
