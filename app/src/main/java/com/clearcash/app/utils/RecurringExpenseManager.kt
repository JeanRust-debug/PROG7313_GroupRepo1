package com.clearcash.app.utils

import android.util.Log
import com.clearcash.app.data.repository.ClearCashRepository
import java.util.Calendar

object RecurringExpenseManager {

    private const val TAG = "RecurringExpMgr"

    /**
     * For each recurring expense owned by [userId], checks whether a copy has
     * already been inserted in the current period. If not, auto-inserts one.
     * Failures per-expense are caught individually so one bad record doesn't
     * block the rest.
     */
    suspend fun process(repo: ClearCashRepository, userId: Long) {
        val recurringList = try {
            repo.getRecurringExpenses(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load recurring expenses: ${e.message}")
            return
        }

        for (template in recurringList) {
            try {
                val (periodStart, periodEnd) = getPeriodBounds(template.recurrenceType)
                val existing = repo.getExpensesByPeriodSync(userId, periodStart, periodEnd)
                val alreadyLogged = existing.any {
                    it.description == template.description &&
                    it.categoryId  == template.categoryId &&
                    it.amount      == template.amount &&
                    it.isRecurring
                }
                if (!alreadyLogged) {
                    val newExpense = template.copy(
                        id          = 0,
                        date        = System.currentTimeMillis(),
                        receiptPath = null,
                        createdAt   = System.currentTimeMillis()
                    )
                    repo.addExpense(newExpense)
                    Log.d(TAG, "Auto-inserted recurring: ${template.description}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process recurring id=${template.id}: ${e.message}")
            }
        }
    }

    private fun getPeriodBounds(recurrenceType: String): Pair<Long, Long> {
        return when (recurrenceType) {
            "WEEKLY" -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val end   = start + 7 * 24 * 60 * 60 * 1000L - 1
                Pair(start, end)
            }
            else -> {
                val m = DateUtils.getCurrentMonth(); val y = DateUtils.getCurrentYear()
                Pair(DateUtils.getStartOfMonth(m, y), DateUtils.getEndOfMonth(m, y))
            }
        }
    }
}
