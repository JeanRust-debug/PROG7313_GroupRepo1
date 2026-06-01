package com.clearcash.app.utils

import com.clearcash.app.data.db.entities.Budget
import com.clearcash.app.ui.dashboard.CategorySpending

object InsightsEngine {

    /**
     * Evaluates spending data and returns one actionable insight string,
     * or null if no rule matches. Rules are checked in priority order.
     */
    fun generate(
        totalSpent: Double,
        budget: Budget?,
        categorySpending: List<CategorySpending>,
        progressPercent: Int
    ): String? {
        if (budget == null) return "Set a monthly budget to start tracking your goals."
        if (totalSpent == 0.0) return "Log your first expense to see insights here."

        // Priority 1: any category over its limit
        val overLimit = categorySpending.firstOrNull { it.isOver }
        if (overLimit != null) return "Your ${overLimit.category.name} spending is over its limit this month."

        // Priority 2: approaching or exceeding max budget (>= 80%)
        if (progressPercent >= 80) return "You've used $progressPercent% of your budget — spend carefully!"

        // Priority 3: on track for Smart Spender badge (<= 70% of max)
        val maxGoal = budget.maxGoal
        if (maxGoal > 0 && totalSpent <= maxGoal * 0.70) {
            return "You're on track to earn the Smart Spender badge this month!"
        }

        return null
    }
}
