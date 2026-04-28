package com.clearcash.app.ui.rewards

import android.graphics.Color
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

// Fragment that displays the user's achievement badges and their progress
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

    // Fetches spending data and evaluates which badges the user has earned
    private fun checkBadges() {
        val repo = ClearCashRepository(AppDatabase.getDatabase(requireContext()))
        val uid  = session.getUserId()
        lifecycleScope.launch {
            // Get the date range for the current month
            val m = DateUtils.getCurrentMonth(); val y = DateUtils.getCurrentYear()
            val start = DateUtils.getStartOfMonth(m, y); val end = DateUtils.getEndOfMonth(m, y)

            val spent    = repo.getTotalByPeriod(uid, start, end)
            val budget   = repo.getBudgetByMonth(uid, m, y)
            val expenses = repo.getExpensesByPeriodSync(uid, start, end)

            // Badge conditions
            val budgetSaver  = budget != null && spent > 0 && spent <= budget.maxGoal
            val expLogger    = expenses.size >= 7
            val smartSpender = budget != null && budget.maxGoal > 0 && spent <= budget.maxGoal * 0.70 && spent > 0

            // Total number of badges earned this month
            val earnedCount = listOf(budgetSaver, expLogger, smartSpender).count { it }

            // Calculate progress percentage for each badge
            val badge1Progress = if (budgetSaver) 100
                else if (budget != null && budget.maxGoal > 0) (100 - ((spent / budget.maxGoal) * 100).toInt()).coerceAtLeast(0)
                else 0
            val badge2Progress = expenses.size.coerceAtMost(7)   // out of 7 days
            val badge3Progress = if (smartSpender) 100
                else if (budget != null && budget.maxGoal > 0) ((1.0 - spent / budget.maxGoal) * 100).toInt().coerceIn(0, 100)
                else 0

            Log.d("Rewards", "budgetSaver=$budgetSaver expLogger=$expLogger smart=$smartSpender earned=$earnedCount")

            requireActivity().runOnUiThread {
                // Update the summary card at the top
                b.tvEarnedCount.text = "$earnedCount / 3 Achievements Unlocked"
                b.pbAchievementOverall.progress = earnedCount

                // Apply state to each badge card
                applyBadgeState(budgetSaver, b.ivBadge1, b.tvBadge1Status, b.tvBadge1Tag,
                    b.pbBadge1, badge1Progress,
                    earned = "✓ EARNED! Stay under budget 1 month",
                    locked = "Stay under budget this month")

                applyBadgeState(expLogger, b.ivBadge2, b.tvBadge2Status, b.tvBadge2Tag,
                    b.pbBadge2, badge2Progress,
                    earned = "✓ EARNED! Logged for 7 days",
                    locked = "Log expenses for 7 days (${expenses.size}/7)")

                applyBadgeState(smartSpender, b.ivBadge3, b.tvBadge3Status, b.tvBadge3Tag,
                    b.pbBadge3, badge3Progress,
                    earned = "✓ EARNED! Saved 30% of budget",
                    locked = "Save 30% of your budget this month")
            }
        }
    }

    // Updates a single badge card to show either earned (green) or locked (grey) state
    private fun applyBadgeState(
        isEarned: Boolean,
        icon: android.widget.ImageView,
        statusTv: android.widget.TextView,
        tagTv: android.widget.TextView,
        progressBar: android.widget.ProgressBar,
        progress: Int,
        earned: String,
        locked: String
    ) {
        // Full opacity when earned, faded when locked
        icon.alpha = if (isEarned) 1f else 0.25f
        statusTv.text = if (isEarned) earned else locked
        progressBar.progress = progress

        if (isEarned) {
            // Green styling for earned badges
            tagTv.text = "EARNED"
            tagTv.setTextColor(Color.parseColor("#2E7D32"))
            tagTv.setBackgroundColor(Color.parseColor("#E8F5E9"))
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            // Grey styling for locked badges
            tagTv.text = "LOCKED"
            tagTv.setTextColor(Color.parseColor("#757575"))
            tagTv.setBackgroundColor(Color.parseColor("#F0F0F0"))
        }
    }

    // Clean up the binding when the fragment view is destroyed
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
