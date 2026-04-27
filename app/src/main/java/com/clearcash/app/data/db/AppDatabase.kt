package com.clearcash.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.clearcash.app.data.db.dao.*
import com.clearcash.app.data.db.entities.*

@Database(
    entities = [User::class, Category::class, Expense::class, Budget::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clearcash_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }

        // Used only in tests — injects an in-memory database
        fun setTestDatabase(db: AppDatabase) { INSTANCE = db }
    }
}