package com.clearcash.app.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.db.dao.CategoryTotal
import com.clearcash.app.data.db.entities.*
import java.security.MessageDigest

open class ClearCashRepository(private val db: AppDatabase) {

    companion object { private const val TAG = "Repository" }

    // ── Auth ──────────────────────────────────────────────────────────────────
    open suspend fun registerUser(username: String, email: String, password: String): Result<User> {
        return try {
            if (db.userDao().usernameExists(username) > 0)
                return Result.failure(Exception("Username already exists"))
            if (db.userDao().emailExists(email) > 0)
                return Result.failure(Exception("Email is already registered"))
            val user = User(username = username, email = email, passwordHash = hash(password))
            val id = db.userDao().insert(user)
            Log.d(TAG, "Registered userId=$id")
            Result.success(user.copy(id = id))
        } catch (e: Exception) { Result.failure(e) }
    }

    open suspend fun loginUser(username: String, password: String): Result<User> {
        return try {
            val user = db.userDao().login(username, hash(password))
                ?: return Result.failure(Exception("Invalid username or password"))
            Log.d(TAG, "Login OK userId=${user.id}")
            Result.success(user)
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ── Categories ────────────────────────────────────────────────────────────
    open fun getCategoriesByUser(userId: Long): LiveData<List<Category>> =
        db.categoryDao().getCategoriesByUser(userId)

    open suspend fun getCategoriesByUserSync(userId: Long): List<Category> =
        db.categoryDao().getCategoriesByUserSync(userId)

    open suspend fun addCategory(userId: Long, name: String, limit: Double): Result<Long> {
        return try {
            if (db.categoryDao().categoryNameExists(userId, name) > 0)
                return Result.failure(Exception("Category '$name' already exists"))
            Result.success(db.categoryDao().insert(Category(userId = userId, name = name, limit = limit)))
        } catch (e: Exception) { Result.failure(e) }
    }

    open suspend fun deleteCategory(category: Category) = db.categoryDao().delete(category)
    open suspend fun getCategoryById(id: Long): Category? = db.categoryDao().getCategoryById(id)

    // ── Expenses ──────────────────────────────────────────────────────────────
    open fun getExpensesByUser(userId: Long): LiveData<List<Expense>> =
        db.expenseDao().getExpensesByUser(userId)

    open fun getExpensesByPeriod(userId: Long, start: Long, end: Long): LiveData<List<Expense>> =
        db.expenseDao().getExpensesByUserAndPeriod(userId, start, end)

    open suspend fun getExpensesByPeriodSync(userId: Long, start: Long, end: Long): List<Expense> =
        db.expenseDao().getExpensesByUserAndPeriodSync(userId, start, end)

    open suspend fun addExpense(expense: Expense): Long =
        db.expenseDao().insert(expense).also { Log.d(TAG, "Expense saved id=$it") }

    open suspend fun deleteExpense(expense: Expense) = db.expenseDao().delete(expense)

    open suspend fun getExpenseById(id: Long): Expense? = db.expenseDao().getExpenseById(id)

    open suspend fun getTotalByPeriod(userId: Long, start: Long, end: Long): Double =
        db.expenseDao().getTotalByPeriod(userId, start, end) ?: 0.0

    open suspend fun getCategoryTotals(userId: Long, start: Long, end: Long): List<CategoryTotal> =
        db.expenseDao().getCategoryTotals(userId, start, end)

    // ── Budget ────────────────────────────────────────────────────────────────
    open suspend fun getBudgetByMonth(userId: Long, month: Int, year: Int): Budget? =
        db.budgetDao().getBudgetByMonth(userId, month, year)

    open fun getBudgetByMonthLive(userId: Long, month: Int, year: Int): LiveData<Budget?> =
        db.budgetDao().getBudgetByMonthLive(userId, month, year)

    open suspend fun saveBudget(userId: Long, min: Double, max: Double, month: Int, year: Int): Long {
        val existing = db.budgetDao().getBudgetByMonth(userId, month, year)
        return if (existing != null) {
            db.budgetDao().update(existing.copy(minGoal = min, maxGoal = max))
            Log.d(TAG, "Budget updated $month/$year min=$min max=$max")
            existing.id
        } else {
            db.budgetDao().insert(Budget(userId = userId, minGoal = min, maxGoal = max, month = month, year = year))
        }
    }
}