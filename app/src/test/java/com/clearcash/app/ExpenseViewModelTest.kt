package com.clearcash.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.clearcash.app.data.db.entities.Category
import com.clearcash.app.data.db.entities.Expense
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.ui.expense.ExpenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    @get:Rule val rule = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()

    @Mock lateinit var repo: ClearCashRepository
    private lateinit var vm: ExpenseViewModel

    @Before fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(dispatcher)
        vm = ExpenseViewModel(repo)
    }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `save expense posts success`() = runTest {
        val expense = Expense(userId=1L, categoryId=null, amount=150.0,
            date=System.currentTimeMillis(), description="Lunch")
        whenever(repo.addExpense(expense)).thenReturn(1L)

        vm.save(expense)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.saveResult.value!!.isSuccess)
        assertEquals(1L, vm.saveResult.value!!.getOrNull())
    }

    @Test fun `save expense posts failure on db error`() = runTest {
        val expense = Expense(userId=1L, categoryId=null, amount=150.0,
            date=System.currentTimeMillis(), description="Lunch")
        whenever(repo.addExpense(expense)).thenThrow(RuntimeException("DB error"))

        vm.save(expense)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.saveResult.value!!.isFailure)
    }

    @Test fun `loadCategories posts list`() = runTest {
        val cats = listOf(Category(id=1L, userId=1L, name="Food", limit=1000.0))
        whenever(repo.getCategoriesByUserSync(1L)).thenReturn(cats)

        vm.loadCategories(1L)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.categories.value!!.size)
        assertEquals("Food", vm.categories.value!![0].name)
    }
}