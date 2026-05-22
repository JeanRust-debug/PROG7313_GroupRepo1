package com.clearcash.app.data.repository

import android.util.Log
import com.clearcash.app.data.db.entities.Budget
import com.clearcash.app.data.db.entities.Category
import com.clearcash.app.data.db.entities.Expense
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Handles all Firestore sync operations
// Room is the source of truth — this syncs data to the cloud in the background
class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    companion object { private const val TAG = "FirestoreRepo" }

    // ── Categories ────────────────────────────────────────────────────────────
    suspend fun syncCategory(firebaseUid: String, category: Category) {
        try {
            db.collection("users")
                .document(firebaseUid)
                .collection("categories")
                .document(category.id.toString())
                .set(mapOf(
                    "id"        to category.id,
                    "name"      to category.name,
                    "limit"     to category.limit,
                    "createdAt" to category.createdAt
                )).await()
            Log.d(TAG, "Category synced id=${category.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Category sync failed: ${e.message}")
        }
    }

    suspend fun deleteCategory(firebaseUid: String, categoryId: Long) {
        try {
            db.collection("users")
                .document(firebaseUid)
                .collection("categories")
                .document(categoryId.toString())
                .delete().await()
            Log.d(TAG, "Category deleted from Firestore id=$categoryId")
        } catch (e: Exception) {
            Log.e(TAG, "Category delete failed: ${e.message}")
        }
    }

    // ── Expenses ──────────────────────────────────────────────────────────────
    suspend fun syncExpense(firebaseUid: String, expense: Expense) {
        try {
            db.collection("users")
                .document(firebaseUid)
                .collection("expenses")
                .document(expense.id.toString())
                .set(mapOf(
                    "id"          to expense.id,
                    "categoryId"  to expense.categoryId,
                    "amount"      to expense.amount,
                    "date"        to expense.date,
                    "startTime"   to expense.startTime,
                    "endTime"     to expense.endTime,
                    "description" to expense.description,
                    "receiptPath" to expense.receiptPath,
                    "createdAt"   to expense.createdAt
                )).await()
            Log.d(TAG, "Expense synced id=${expense.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Expense sync failed: ${e.message}")
        }
    }

    suspend fun deleteExpense(firebaseUid: String, expenseId: Long) {
        try {
            db.collection("users")
                .document(firebaseUid)
                .collection("expenses")
                .document(expenseId.toString())
                .delete().await()
            Log.d(TAG, "Expense deleted from Firestore id=$expenseId")
        } catch (e: Exception) {
            Log.e(TAG, "Expense delete failed: ${e.message}")
        }
    }

    // ── Budgets ───────────────────────────────────────────────────────────────
    suspend fun syncBudget(firebaseUid: String, budget: Budget) {
        try {
            val docId = "${budget.month}_${budget.year}"
            db.collection("users")
                .document(firebaseUid)
                .collection("budgets")
                .document(docId)
                .set(mapOf(
                    "id"        to budget.id,
                    "minGoal"   to budget.minGoal,
                    "maxGoal"   to budget.maxGoal,
                    "month"     to budget.month,
                    "year"      to budget.year,
                    "updatedAt" to budget.updatedAt
                )).await()
            Log.d(TAG, "Budget synced $docId")
        } catch (e: Exception) {
            Log.e(TAG, "Budget sync failed: ${e.message}")
        }
    }
}