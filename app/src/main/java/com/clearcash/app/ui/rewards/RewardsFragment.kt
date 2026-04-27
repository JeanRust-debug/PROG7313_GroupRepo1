package com.clearcash.app.ui.rewards

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.FragmentRewardsBinding
import com.clearcash.app.utils.DateUtils
import com.clearcash.app.utils.SessionManager
import kotlinx.coroutines.launch

class RewardsFragment : Fragment() {

    private var _b: FragmentRewardsBinding? = null
    private val b get() = _b!!
    private lateinit var session: SessionManager

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentRewardsBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        session = SessionManager(requireContext())
        checkBadges()
    }

    private fun checkBadges() {
        val repo = ClearCashRepository(AppDatabase.getDatabase(requireContext()))
        val uid  = session.getUserId()
        lifecycleScope.launch {
            val m = DateUtils.getCurrentMonth(); val y = DateUtils.getCurrentYear()
            val start = DateUtils.getStartOfMonth(m, y); val end = DateUtils.getEndOfMonth(m, y)
            val spent    = repo.getTotalByPeriod(uid, start, end)
            val budget   = repo.getBudgetByMonth(uid, m, y)
            val expenses = repo.getExpensesByPeriodSync(uid, start, end)

            val budgetSaver  = budget != null && spent > 0 && spent <= budget.maxGoal
            val expLogger    = expenses.size >= 7
            val smartSpender = budget != null && budget.maxGoal > 0 && spent <= budget.maxGoal * 0.70 && spent > 0

            Log.d("Rewards", "budgetSaver=$budgetSaver expLogger=$expLogger smart=$smartSpender")

            requireActivity().runOnUiThread {
                b.ivBadge1.alpha      = if (budgetSaver)  1f else 0.25f
                b.tvBadge1Status.text = if (budgetSaver)  "✓ EARNED! Stay under budget 1 month" else "Stay under budget this month"
                b.ivBadge2.alpha      = if (expLogger)    1f else 0.25f
                b.tvBadge2Status.text = if (expLogger)    "✓ EARNED! Logged for 7 days" else "Log expenses for 7 days (${expenses.size}/7)"
                b.ivBadge3.alpha      = if (smartSpender) 1f else 0.25f
                b.tvBadge3Status.text = if (smartSpender) "✓ EARNED! Saved 30% of budget" else "Save 30% of your budget this month"
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}