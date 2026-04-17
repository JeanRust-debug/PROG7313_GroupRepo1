package com.clearcash.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.db.entities.Expense
import com.clearcash.app.data.repository.ClearCashRepository
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryInstrumentedTest {

    @get:Rule val rule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var repo: ClearCashRepository

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repo = ClearCashRepository(db)
    }
    @After fun tearDown() { db.close() }

    @Test fun registerAndLogin() = runTest {
        val reg = repo.registerUser("alice", "alice@test.com", "pass123")
        assertTrue(reg.isSuccess)

        val login = repo.loginUser("alice", "pass123")
        assertTrue(login.isSuccess)
        assertEquals("alice", login.getOrNull()?.username)
    }

    @Test fun duplicateUsernameFails() = runTest {
        repo.registerUser("bob", "bob@test.com", "pass123")
        val dupe = repo.registerUser("bob", "bob2@test.com", "pass456")
        assertTrue(dupe.isFailure)
        assertEquals("Username already exists", dupe.exceptionOrNull()?.message)
    }

    @Test fun wrongPasswordFails() = runTest {
        repo.registerUser("carol", "carol@test.com", "correct")
        val bad = repo.loginUser("carol", "wrong")
        assertTrue(bad.isFailure)
    }

    @Test fun addAndRetrieveCategory() = runTest {
        val user = repo.registerUser("dave", "dave@test.com", "pass").getOrNull()!!
        val result = repo.addCategory(user.id, "Groceries", 2000.0)
        assertTrue(result.isSuccess)
        val cats = repo.getCategoriesByUserSync(user.id)
        assertEquals(1, cats.size)
        assertEquals("Groceries", cats[0].name)
        assertEquals(2000.0, cats[0].limit, 0.01)
    }

    @Test fun addAndRetrieveExpense() = runTest {
        val user = repo.registerUser("eve", "eve@test.com", "pass").getOrNull()!!
        val expense = Expense(userId=user.id, categoryId=null, amount=250.0,
            date=System.currentTimeMillis(), description="Coffee")
        val id = repo.addExpense(expense)
        assertTrue(id > 0)
        val fetched = repo.getExpenseById(id)
        assertNotNull(fetched)
        assertEquals(250.0, fetched!!.amount, 0.01)
    }

    @Test fun saveBudgetAndRetrieve() = runTest {
        val user = repo.registerUser("frank", "frank@test.com", "pass").getOrNull()!!
        repo.saveBudget(user.id, 1000.0, 5000.0, 4, 2026)
        val budget = repo.getBudgetByMonth(user.id, 4, 2026)
        assertNotNull(budget)
        assertEquals(1000.0, budget!!.minGoal, 0.01)
        assertEquals(5000.0, budget.maxGoal, 0.01)
    }

    @Test fun updateBudgetUpserts() = runTest {
        val user = repo.registerUser("grace", "grace@test.com", "pass").getOrNull()!!
        repo.saveBudget(user.id, 500.0, 3000.0, 4, 2026)
        repo.saveBudget(user.id, 800.0, 4000.0, 4, 2026) // Update
        val budget = repo.getBudgetByMonth(user.id, 4, 2026)
        assertEquals(4000.0, budget!!.maxGoal, 0.01)
    }
}