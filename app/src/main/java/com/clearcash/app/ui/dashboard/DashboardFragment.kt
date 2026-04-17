package com.clearcash.app.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.FragmentDashboardBinding
import com.clearcash.app.ui.expense.AddExpenseActivity
import com.clearcash.app.utils.CurrencyFormatter
import com.clearcash.app.utils.DateUtils
import com.clearcash.app.utils.SessionManager

class DashboardFragment : Fragment() {

    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!
    private lateinit var vm: DashboardViewModel
    private lateinit var session: SessionManager

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: android.os.Bundle?) =
        FragmentDashboardBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        vm = ViewModelProvider(this,
            DashboardViewModel.Factory(ClearCashRepository(AppDatabase.getDatabase(requireContext())))
        )[DashboardViewModel::class.java]

        vm.data.observe(viewLifecycleOwner) { d ->
            Log.d("Dashboard", "Data updated spent=${d.totalSpent}")
            b.tvWelcome.text        = "Welcome, ${session.getUsername()}!"
            b.tvMonthLabel.text     = DateUtils.getCurrentMonthLabel()
            b.tvSpentAmount.text    = CurrencyFormatter.format(d.totalSpent)
            val budget = d.budget
            if (budget != null) {
                b.tvMonthlyBudget.text  = CurrencyFormatter.format(budget.maxGoal)
                b.tvRemainingAmount.text = CurrencyFormatter.format((budget.maxGoal - d.totalSpent).coerceAtLeast(0.0))
                b.progressBar.progress  = d.progressPercent
            } else {
                b.tvMonthlyBudget.text   = "Not set"
                b.tvRemainingAmount.text = "Not set"
                b.progressBar.progress   = 0
            }
            b.tvOverspendingAlert.visibility =
                if (d.isOverBudget) View.VISIBLE else View.GONE
        }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            b.progressLoading.visibility = if (loading) View.VISIBLE else View.GONE
        }

        b.fabAddExpense.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpenseActivity::class.java))
        }

        load()
    }

    override fun onResume() { super.onResume(); load() }
    private fun load() { vm.load(session.getUserId()) }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}