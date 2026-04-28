package com.clearcash.app

import com.clearcash.app.data.db.entities.Category
import com.clearcash.app.data.db.entities.Expense
import com.clearcash.app.data.db.entities.Budget
import com.clearcash.app.utils.ValidationUtils
import com.clearcash.app.utils.DateUtils
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ClearCash app.
 * Tests validation logic, entity creation, and date utilities.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class ClearCashUnitTest {

    // ─── ValidationUtils Tests ───────────────────────────────────────

    @Test
    fun `validateRequired returns error when empty`() {
        val result = ValidationUtils.validateRequired("", "Name")
        assertNotNull(result)
    }

    @Test
    fun `validateRequired returns null when valid`() {
        val result = ValidationUtils.validateRequired("Groceries", "Name")
        assertNull(result)
    }

    @Test
    fun `validateAmount returns error when empty`() {
        val result = ValidationUtils.validateAmount("")
        assertNotNull(result)
    }

    @Test
    fun `validateAmount returns error when zero`() {
        val result = ValidationUtils.validateAmount("0")
        assertNotNull(result)
    }

    @Test
    fun `validateAmount returns error when negative`() {
        val result = ValidationUtils.validateAmount("-5")
        assertNotNull(result)
    }

    @Test
    fun `validateAmount returns null when valid`() {
        val result = ValidationUtils.validateAmount("150.50")
        assertNull(result)
    }

    @Test
    fun `validateAmount returns error when not a number`() {
        val result = ValidationUtils.validateAmount("abc")
        assertNotNull(result)
    }

    @Test
    fun `validateMinMax returns error when max less than min`() {
        val result = ValidationUtils.validateMinMax(500.0, 200.0)
        assertNotNull(result)
    }

    @Test
    fun `validateMinMax returns error when equal`() {
        val result = ValidationUtils.validateMinMax(500.0, 500.0)
        assertNotNull(result)
    }

    @Test
    fun `validateMinMax returns null when max greater than min`() {
        val result = ValidationUtils.validateMinMax(200.0, 500.0)
        assertNull(result)
    }

    @Test
    fun `validateCategoryName returns error when too short`() {
        val result = ValidationUtils.validateCategoryName("A")
        assertNotNull(result)
    }

    @Test
    fun `validateCategoryName returns error when too long`() {
        val result = ValidationUtils.validateCategoryName("A".repeat(31))
        assertNotNull(result)
    }

    @Test
    fun `validateCategoryName returns null when valid`() {
        val result = ValidationUtils.validateCategoryName("Groceries")
        assertNull(result)
    }

    // ─── Entity Creation Tests ────────────────────────────────────────

    @Test
    fun `Category entity created with correct values`() {
        val category = Category(userId = 1L, name = "Transport", limit = 500.0)
        assertEquals("Transport", category.name)
        assertEquals(500.0, category.limit, 0.0)
        assertEquals(0, category.id)
    }

    @Test
    fun `Category default limit is zero`() {
        val category = Category(userId = 1L, name = "Misc")
        assertEquals(0.0, category.limit, 0.0)
    }

    @Test
    fun `Expense entity created with correct values`() {
        val expense = Expense(
            userId = 1L,
            amount = 250.0,
            date = System.currentTimeMillis(),
            description = "Woolworths",
            categoryId = 1L,
            receiptPath = null
        )
        assertEquals(250.0, expense.amount, 0.0)
        assertEquals("Woolworths", expense.description)
        assertEquals(1L, expense.categoryId)
        assertNull(expense.receiptPath)
    }

    @Test
    fun `Expense receiptPath can be set`() {
        val expense = Expense(
            userId = 1L,
            amount = 100.0,
            date = System.currentTimeMillis(),
            description = "Checkers",
            categoryId = 2L,
            receiptPath = "/storage/receipt.jpg"
        )
        assertNotNull(expense.receiptPath)
        assertEquals("/storage/receipt.jpg", expense.receiptPath)
    }

    @Test
    fun `Budget created with correct values`() {
        val budget = Budget(
            userId = 1L,
            minGoal = 1000.0,
            maxGoal = 5000.0,
            month = 4,
            year = 2026
        )
        assertEquals(4, budget.month)
        assertEquals(2026, budget.year)
        assertEquals(1000.0, budget.minGoal, 0.0)
        assertEquals(5000.0, budget.maxGoal, 0.0)
    }

    @Test
    fun `Budget min is less than max`() {
        val budget = Budget(
            userId = 1L,
            minGoal = 1000.0,
            maxGoal = 5000.0,
            month = 4,
            year = 2026
        )
        assertTrue(budget.minGoal < budget.maxGoal)
    }

    // ─── DateUtils Tests ──────────────────────────────────────────────

    @Test
    fun `getCurrentMonthYearString returns correct format`() {
        val month = DateUtils.getCurrentMonthYearString()
        // Should match YYYY-MM format
        assertTrue(month.matches(Regex("\\d{4}-\\d{2}")))
    }

    @Test
    fun `formatDate returns non-empty string`() {
        val formatted = DateUtils.formatDate(System.currentTimeMillis())
        assertTrue(formatted.isNotEmpty())
    }

    @Test
    fun `getStartOfCurrentMonth is before end of month`() {
        val start = DateUtils.getStartOfCurrentMonth()
        val end = DateUtils.getEndOfCurrentMonth()
        assertTrue(start < end)
    }

    @Test
    fun `getStartOfDay is before getEndOfDay`() {
        val timestamp = System.currentTimeMillis()
        val start = DateUtils.getStartOfDay(timestamp)
        val end = DateUtils.getEndOfDay(timestamp)
        assertTrue(start < end)
    }
}
