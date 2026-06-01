package com.clearcash.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.clearcash.app.data.db.entities.SavingsGoal
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.ui.goals.GoalsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {

    @get:Rule val rule = InstantTaskExecutorRule()

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: ClearCashRepository
    private lateinit var vm: GoalsViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = mock()
        vm = GoalsViewModel(repo)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    private fun goal(id: Long = 1L, saved: Double = 0.0, target: Double = 1000.0) =
        SavingsGoal(id = id, userId = 1L, name = "Holiday", targetAmount = target, savedAmount = saved)

    @Test fun `addGoal posts success when repo succeeds`() = runTest {
        whenever(repo.addGoal(any())).thenReturn(1L)
        vm.addGoal("Holiday", 1000.0, userId = 1L)
        val result = vm.saveResult.value
        assertNotNull(result)
        assertTrue(result!!.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test fun `addGoal posts failure when repo throws`() = runTest {
        whenever(repo.addGoal(any())).thenThrow(RuntimeException("DB error"))
        vm.addGoal("Holiday", 1000.0, userId = 1L)
        val result = vm.saveResult.value
        assertNotNull(result)
        assertTrue(result!!.isFailure)
    }

    @Test fun `addFunds calls applyToGoal on repo`() = runTest {
        val g = goal()
        vm.addFunds(g, 200.0)
        verify(repo).applyToGoal(g, 200.0)
    }

    @Test fun `deleteGoal calls deleteGoal on repo`() = runTest {
        val g = goal()
        vm.deleteGoal(g)
        verify(repo).deleteGoal(g)
    }

    @Test fun `getGoals returns LiveData from repo`() {
        val liveData = MutableLiveData<List<SavingsGoal>>(listOf(goal()))
        whenever(repo.getGoals(1L)).thenReturn(liveData)
        val result = vm.getGoals(1L)
        assertEquals(liveData, result)
    }
}
