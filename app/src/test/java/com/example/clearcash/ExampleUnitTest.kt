package com.example.clearcash

import com.example.clearcash.data.db.entities.BudgetGoal
import com.example.clearcash.data.db.entities.Category
import com.example.clearcash.data.db.entities.Expense
import com.example.clearcash.util.DateUtils
import com.example.clearcash.util.ValidationUtils
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
        val category = Category(name = "Transport", limit = 500.0)
        assertEquals("Transport", category.name)
        assertEquals(500.0, category.limit, 0.0)
        assertEquals(0, category.id)
    }

    @Test
    fun `Category default limit is zero`() {
        val category = Category(name = "Misc")
        assertEquals(0.0, category.limit, 0.0)
    }

    @Test
    fun `Expense entity created with correct values`() {
        val expense = Expense(
            amount = 250.0,
            date = System.currentTimeMillis(),
            description = "Woolworths",
            categoryId = 1,
            photoPath = null
        )
        assertEquals(250.0, expense.amount, 0.0)
        assertEquals("Woolworths", expense.description)
        assertEquals(1, expense.categoryId)
        assertNull(expense.photoPath)
    }

    @Test
    fun `Expense photoPath can be set`() {
        val expense = Expense(
            amount = 100.0,
            date = System.currentTimeMillis(),
            description = "Checkers",
            categoryId = 2,
            photoPath = "/storage/receipt.jpg"
        )
        assertNotNull(expense.photoPath)
        assertEquals("/storage/receipt.jpg", expense.photoPath)
    }

    @Test
    fun `BudgetGoal created with correct values`() {
        val goal = BudgetGoal(
            month = "2026-04",
            minGoal = 1000.0,
            maxGoal = 5000.0
        )
        assertEquals("2026-04", goal.month)
        assertEquals(1000.0, goal.minGoal, 0.0)
        assertEquals(5000.0, goal.maxGoal, 0.0)
    }

    @Test
    fun `BudgetGoal min is less than max`() {
        val goal = BudgetGoal(
            month = "2026-04",
            minGoal = 1000.0,
            maxGoal = 5000.0
        )
        assertTrue(goal.minGoal < goal.maxGoal)
    }

    // ─── DateUtils Tests ──────────────────────────────────────────────

    @Test
    fun `getCurrentMonth returns correct format`() {
        val month = DateUtils.getCurrentMonth()
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