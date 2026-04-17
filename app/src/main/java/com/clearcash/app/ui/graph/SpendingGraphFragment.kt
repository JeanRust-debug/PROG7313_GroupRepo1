package com.clearcash.app.ui.graph

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.FragmentSpendingGraphBinding
import com.clearcash.app.utils.CurrencyFormatter
import com.clearcash.app.utils.DateUtils
import com.clearcash.app.utils.SessionManager
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import java.util.*

class SpendingGraphFragment : Fragment() {

    private var _b: FragmentSpendingGraphBinding? = null
    private val b get() = _b!!
    private lateinit var vm: GraphViewModel
    private lateinit var session: SessionManager
    private var start = DateUtils.getStartOfMonth(DateUtils.getCurrentMonth(), DateUtils.getCurrentYear())
    private var end   = DateUtils.getEndOfMonth(DateUtils.getCurrentMonth(),   DateUtils.getCurrentYear())

    private val COLORS = listOf(
        Color.parseColor("#1B2A6B"), Color.parseColor("#3F51B5"), Color.parseColor("#4FC3F7"),
        Color.parseColor("#4CAF50"), Color.parseColor("#FF9800"), Color.parseColor("#F44336"),
        Color.parseColor("#9C27B0"), Color.parseColor("#00BCD4")
    )

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentSpendingGraphBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        session = SessionManager(requireContext())
        vm = ViewModelProvider(this, GraphViewModel.Factory(
            ClearCashRepository(AppDatabase.getDatabase(requireContext()))
        ))[GraphViewModel::class.java]

        b.pieChart.apply {
            description.isEnabled = false; isDrawHoleEnabled = true; holeRadius = 40f
            setHoleColor(Color.TRANSPARENT); setUsePercentValues(true)
            setEntryLabelColor(Color.WHITE); setEntryLabelTextSize(11f)
        }

        b.btnStart.setOnClickListener { pickDate(true) }
        b.btnEnd.setOnClickListener   { pickDate(false) }

        vm.data.observe(viewLifecycleOwner) { d ->
            b.tvTotal.text = "Total: ${CurrencyFormatter.format(d.total)}"
            if (d.labels.isEmpty()) { b.pieChart.visibility = View.GONE; b.tvNoData.visibility = View.VISIBLE; b.tvCatList.text = "" }
            else {
                b.pieChart.visibility = View.VISIBLE; b.tvNoData.visibility = View.GONE
                val entries = d.labels.mapIndexed { i, name -> PieEntry(d.values[i], name) }
                val ds = PieDataSet(entries, "").apply {
                    colors = COLORS.take(entries.size)
                    valueFormatter = PercentFormatter(b.pieChart)
                    valueTextColor = Color.WHITE; valueTextSize = 11f
                }
                b.pieChart.data = PieData(ds); b.pieChart.invalidate()
                b.tvCatList.text = d.labels.mapIndexed { i, name ->
                    "• $name: ${CurrencyFormatter.format(d.values[i].toDouble())}"
                }.joinToString("\n")
            }
        }

        updateBtns(); load()
    }

    private fun pickDate(isStart: Boolean) {
        val ts = if (isStart) start else end
        val c  = Calendar.getInstance().also { it.timeInMillis = ts }
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val nc = Calendar.getInstance().also { it.set(y, m, d) }
            if (isStart) start = DateUtils.getStartOfDay(nc.timeInMillis)
            else         end   = DateUtils.getEndOfDay(nc.timeInMillis)
            updateBtns(); load()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateBtns() {
        b.btnStart.text = DateUtils.formatForDisplay(start)
        b.btnEnd.text   = DateUtils.formatForDisplay(end)
    }
    private fun load() = vm.load(session.getUserId(), start, end)
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}