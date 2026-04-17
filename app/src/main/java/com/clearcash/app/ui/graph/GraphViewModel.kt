package com.clearcash.app.ui.graph

import androidx.lifecycle.*
import com.clearcash.app.data.repository.ClearCashRepository
import kotlinx.coroutines.launch

data class GraphData(val labels: List<String>, val values: List<Float>, val total: Double)

class GraphViewModel(private val repo: ClearCashRepository) : ViewModel() {

    private val _data = MutableLiveData<GraphData>()
    val data: LiveData<GraphData> = _data

    fun load(userId: Long, start: Long, end: Long) = viewModelScope.launch {
        val cats   = repo.getCategoriesByUserSync(userId)
        val totals = repo.getCategoryTotals(userId, start, end)
        val labels = mutableListOf<String>(); val values = mutableListOf<Float>(); var total = 0.0
        cats.forEach { cat ->
            val spent = totals.find { it.categoryId == cat.id }?.total ?: 0.0
            if (spent > 0) { labels.add(cat.name); values.add(spent.toFloat()); total += spent }
        }
        val uncat = totals.find { it.categoryId == null }?.total ?: 0.0
        if (uncat > 0) { labels.add("Other"); values.add(uncat.toFloat()); total += uncat }
        _data.postValue(GraphData(labels, values, total))
    }

    class Factory(private val repo: ClearCashRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>): T = GraphViewModel(repo) as T
    }
}