package com.clearcash.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.clearcash.app.data.db.entities.Budget
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule val rule = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()

    @Mock lateinit var repo: ClearCashRepository
    private lateinit var vm: DashboardViewModel

    @Before fun setUp() { MockitoAnnotations.openMocks(this); Dispatchers.setMain(dispatcher); vm = DashboardViewModel(repo) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `overspending is flagged when spent exceeds max`() = runTest {
        whenever(repo.getTotalByPeriod(any(), any(), any())).thenReturn(6000.0)
        whenever(repo.getBudgetByMonth(any(), any(), any())).thenReturn(Budget(userId=1L, minGoal=1000.0, maxGoal=5000.0, month=4, year=2026))
        whenever(repo.getCategoriesByUserSync(any())).thenReturn(emptyList())
        whenever(repo.getCategoryTotals(any(), any(), any())).thenReturn(emptyList())

        vm.load(1L)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.data.value!!.isOverBudget)
        assertEquals(6000.0, vm.data.value!!.totalSpent, 0.01)
    }

    @Test fun `progress is 50 percent when spent is half of max`() = runTest {
        whenever(repo.getTotalByPeriod(any(), any(), any())).thenReturn(2500.0)
        whenever(repo.getBudgetByMonth(any(), any(), any())).thenReturn(Budget(userId=1L, minGoal=0.0, maxGoal=5000.0, month=4, year=2026))
        whenever(repo.getCategoriesByUserSync(any())).thenReturn(emptyList())
        whenever(repo.getCategoryTotals(any(), any(), any())).thenReturn(emptyList())

        vm.load(1L)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(50, vm.data.value!!.progressPercent)
        assertFalse(vm.data.value!!.isOverBudget)
    }
}