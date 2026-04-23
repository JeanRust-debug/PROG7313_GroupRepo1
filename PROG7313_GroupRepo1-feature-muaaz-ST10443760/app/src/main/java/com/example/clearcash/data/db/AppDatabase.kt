package com.example.clearcash.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.clearcash.data.db.dao.BudgetGoalDao
import com.example.clearcash.data.db.dao.CategoryDao
import com.example.clearcash.data.db.dao.ExpenseDao
import com.example.clearcash.data.db.dao.UserDao
import com.example.clearcash.data.db.entities.BudgetGoal
import com.example.clearcash.data.db.entities.Category
import com.example.clearcash.data.db.entities.Expense
import com.example.clearcash.data.db.entities.User

/**
 * Main Room Database class for ClearCash.
 * Singleton pattern ensures only one instance exists at a time.
 * Updated to include User entity for authentication.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 * Author: Diya Maharaj ST10327888
 */
@Database(
    entities = [Category::class, Expense::class, BudgetGoal::class, User::class],
    version = 2, // Incremented version due to new User table
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Abstract DAO accessors
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun userDao(): UserDao // New DAO for authentication

    companion object {
        // Volatile ensures changes are visible across threads immediately
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance.
         * Creates it if it doesn't exist yet.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clearcash_database"
                )
                    .fallbackToDestructiveMigration() // Wipe and rebuild on schema change during dev
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}