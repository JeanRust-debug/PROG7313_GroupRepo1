package com.clearcash.app

import com.clearcash.app.data.db.entities.Budget
import com.clearcash.app.data.db.entities.Category
import com.clearcash.app.ui.dashboard.CategorySpending
import com.clearcash.app.utils.InsightsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InsightsEngineTest {

    private fun budget(min: Double, max: Double) =
        Budget(userId = 1L, minGoal = min, maxGoal = max, month = 1, year = 2026)

    @Test fun `returns no budget message when budget is null`() {
        val result = InsightsEngine.generate(
            totalSpent = 0.0, budget = null, categorySpending = emptyList(), progressPercent = 0
        )
        assertEquals("Set a monthly budget to start tracking your goals.", result)
    }

    @Test fun `returns no expenses message when nothing logged`() {
        val result = InsightsEngine.generate(
            totalSpent = 0.0, budget = budget(1000.0, 5000.0),
            categorySpending = emptyList(), progressPercent = 0
        )
        assertEquals("Log your first expense to see insights here.", result)
    }

    @Test fun `returns category over limit message`() {
        val cat = Category(id = 1L, userId = 1L, name = "Groceries", limit = 500.0)
        val spending = listOf(CategorySpending(cat, spent = 600.0, isOver = true))
        val result = InsightsEngine.generate(
            totalSpent = 600.0, budget = budget(0.0, 5000.0),
            categorySpending = spending, progressPercent = 12
        )
        assertEquals("Your Groceries spending is over its limit this month.", result)
    }

    @Test fun `returns approaching budget message at 80 percent`() {
        val result = InsightsEngine.generate(
            totalSpent = 4000.0, budget = budget(0.0, 5000.0),
            categorySpending = emptyList(), progressPercent = 80
        )
        assertEquals("You've used 80% of your budget — spend carefully!", result)
    }

    @Test fun `returns smart spender message when under 70 percent`() {
        val result = InsightsEngine.generate(
            totalSpent = 3000.0, budget = budget(0.0, 5000.0),
            categorySpending = emptyList(), progressPercent = 60
        )
        assertEquals("You're on track to earn the Smart Spender badge this month!", result)
    }

    @Test fun `returns null when no rule matches`() {
        val result = InsightsEngine.generate(
            totalSpent = 3600.0, budget = budget(0.0, 5000.0),
            categorySpending = emptyList(), progressPercent = 72
        )
        assertNull(result)
    }
}
