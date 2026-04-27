package com.clearcash.app.ui.dashboard

import androidx.lifecycle.*
import com.clearcash.app.data.db.entities.Budget
import com.clearcash.app.data.db.entities.Category
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.utils.DateUtils
import kotlinx.coroutines.launch

data class CategorySpending(val category: Category, val spent: Double, val isOver: Boolean)
data class DashboardData(
    val totalSpent: Double, val budget: Budget?,
    val categorySpending: List<CategorySpending>,
    val progressPercent: Int, val isOverBudget: Boolean
)

class DashboardViewModel(private val repo: ClearCashRepository) : ViewModel() {

    private val _data = MutableLiveData<DashboardData>()
    val data: LiveData<DashboardData> = _data

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun load(userId: Long) {
        _loading.value = true
        viewModelScope.launch {
            val m = DateUtils.getCurrentMonth(); val y = DateUtils.getCurrentYear()
            val start = DateUtils.getStartOfMonth(m, y); val end = DateUtils.getEndOfMonth(m, y)
            val total      = repo.getTotalByPeriod(userId, start, end)
            val budget     = repo.getBudgetByMonth(userId, m, y)
            val categories = repo.getCategoriesByUserSync(userId)
            val totals     = repo.getCategoryTotals(userId, start, end)
            val catSpend   = categories.map { cat ->
                val spent = totals.find { it.categoryId == cat.id }?.total ?: 0.0
                CategorySpending(cat, spent, cat.limit > 0 && spent > cat.limit)
            }
            val max = budget?.maxGoal ?: 0.0
            val pct = if (max > 0) ((total / max) * 100).toInt().coerceIn(0, 100) else 0
            _data.postValue(DashboardData(total, budget, catSpend, pct, max > 0 && total > max))
            _loading.postValue(false)
        }
    }

    class Factory(private val repo: ClearCashRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>): T = DashboardViewModel(repo) as T
    }
}