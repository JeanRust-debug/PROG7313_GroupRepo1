package com.clearcash.app

import com.clearcash.app.data.db.entities.Expense
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.utils.RecurringExpenseManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringExpenseManagerTest {

    @Mock lateinit var repo: ClearCashRepository

    @Before fun setUp() { MockitoAnnotations.openMocks(this) }

    private fun recurringExpense(recurrenceType: String, daysAgo: Long): Expense {
        val past = System.currentTimeMillis() - daysAgo * 24 * 60 * 60 * 1000L
        return Expense(
            id = 1L, userId = 1L, categoryId = null,
            amount = 100.0, date = past,
            description = "Rent", isRecurring = true, recurrenceType = recurrenceType
        )
    }

    @Test fun `monthly expense not logged this month triggers insert`() = runTest {
        val expense = recurringExpense("MONTHLY", 35)
        whenever(repo.getRecurringExpenses(1L)).thenReturn(listOf(expense))
        whenever(repo.getExpensesByPeriodSync(any(), any(), any())).thenReturn(emptyList())
        whenever(repo.addExpense(any())).thenReturn(2L)

        RecurringExpenseManager.process(repo, 1L)

        verify(repo).addExpense(any())
    }

    @Test fun `monthly expense already logged this month skips insert`() = runTest {
        val expense = recurringExpense("MONTHLY", 35)
        val existing = expense.copy(id = 2L, date = System.currentTimeMillis())
        whenever(repo.getRecurringExpenses(1L)).thenReturn(listOf(expense))
        whenever(repo.getExpensesByPeriodSync(any(), any(), any())).thenReturn(listOf(existing))

        RecurringExpenseManager.process(repo, 1L)

        verify(repo, never()).addExpense(any())
    }

    @Test fun `weekly expense not logged this week triggers insert`() = runTest {
        val expense = recurringExpense("WEEKLY", 9)
        whenever(repo.getRecurringExpenses(1L)).thenReturn(listOf(expense))
        whenever(repo.getExpensesByPeriodSync(any(), any(), any())).thenReturn(emptyList())
        whenever(repo.addExpense(any())).thenReturn(2L)

        RecurringExpenseManager.process(repo, 1L)

        verify(repo).addExpense(any())
    }

    @Test fun `weekly expense already logged this week skips insert`() = runTest {
        val expense = recurringExpense("WEEKLY", 9)
        val existing = expense.copy(id = 2L, date = System.currentTimeMillis())
        whenever(repo.getRecurringExpenses(1L)).thenReturn(listOf(expense))
        whenever(repo.getExpensesByPeriodSync(any(), any(), any())).thenReturn(listOf(existing))

        RecurringExpenseManager.process(repo, 1L)

        verify(repo, never()).addExpense(any())
    }
}
