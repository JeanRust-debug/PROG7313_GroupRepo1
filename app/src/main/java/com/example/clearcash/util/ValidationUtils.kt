package com.example.clearcash.util

/**
 * Utility object for reusable input validation across the app.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
object ValidationUtils {

    /**
     * Checks if a string is not empty.
     * @return error message if invalid, null if valid
     */
    fun validateRequired(value: String, fieldName: String): String? {
        return if (value.trim().isEmpty()) "$fieldName is required" else null
    }

    /**
     * Validates a monetary amount — must be a positive number.
     * @return error message if invalid, null if valid
     */
    fun validateAmount(value: String): String? {
        if (value.trim().isEmpty()) return "Amount is required"
        val amount = value.toDoubleOrNull() ?: return "Enter a valid number"
        if (amount <= 0) return "Amount must be greater than zero"
        return null
    }

    /**
     * Validates that max is strictly greater than min.
     * @return error message if invalid, null if valid
     */
    fun validateMinMax(min: Double, max: Double): String? {
        return if (max <= min) "Maximum must be greater than minimum" else null
    }

    /**
     * Validates a category name — not empty and reasonable length.
     * @return error message if invalid, null if valid
     */
    fun validateCategoryName(name: String): String? {
        if (name.trim().isEmpty()) return "Category name is required"
        if (name.trim().length < 2) return "Name must be at least 2 characters"
        if (name.trim().length > 30) return "Name must be less than 30 characters"
        return null
    }
}