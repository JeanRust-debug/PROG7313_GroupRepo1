package com.clearcash.app.ui.expense

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.ActivityViewReceiptBinding
import com.clearcash.app.utils.CurrencyFormatter
import com.clearcash.app.utils.DateUtils
import java.io.File

class ViewReceiptActivity : AppCompatActivity() {

    private lateinit var b: ActivityViewReceiptBinding
    private lateinit var vm: ExpenseViewModel // im just testing commit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityViewReceiptBinding.inflate(layoutInflater)
        setContentView(b.root)
        supportActionBar?.run { title = "Receipt"; setDisplayHomeAsUpEnabled(true) }

        vm = ViewModelProvider(this, ExpenseViewModel.Factory(
            ClearCashRepository(AppDatabase.getDatabase(this))
        ))[ExpenseViewModel::class.java]

        // Retrieve the expense ID passed by Intent
        val id = intent.getLongExtra("expense_id", -1L)
        if (id == -1L) { Toast.makeText(this, "Invalid expense", Toast.LENGTH_SHORT).show(); finish(); return }

        vm.loadById(id)
        vm.detail.observe(this) { expense ->
            if (expense == null) { finish(); return@observe }
            Log.d("ViewReceipt", "Showing expense id=${expense.id}")
            b.tvAmount.text   = CurrencyFormatter.format(expense.amount)
            b.tvDate.text     = DateUtils.formatForDisplay(expense.date)
            b.tvDesc.text     = expense.description
            if (!expense.receiptPath.isNullOrEmpty()) {
                try {
                    val uri = if (expense.receiptPath.startsWith("/")) Uri.fromFile(File(expense.receiptPath))
                    else Uri.parse(expense.receiptPath)
                    b.ivReceipt.setImageURI(uri)
                    b.ivReceipt.visibility  = View.VISIBLE
                    b.tvNoReceipt.visibility = View.GONE
                } catch (e: Exception) {
                    b.ivReceipt.visibility  = View.GONE
                    b.tvNoReceipt.visibility = View.VISIBLE
                }
            } else {
                b.ivReceipt.visibility  = View.GONE
                b.tvNoReceipt.visibility = View.VISIBLE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}