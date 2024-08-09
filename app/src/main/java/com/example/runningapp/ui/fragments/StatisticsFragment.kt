package com.example.runningapp.ui.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.db.Run
import com.example.runningapp.other.Constants.KEY_STREAK_DAYS
import com.example.runningapp.other.TrackingUtility.getFormattedStopWatchTime
import com.example.runningapp.other.TrackingUtility.getRunBYDate
import com.example.runningapp.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {


    private val viewModel: StatisticsViewModel by activityViewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setStreakRunningDays(runs: List<Run>) {
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        val todayCal = Calendar.getInstance()
        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DATE, -1)

        val today = dateFormat.format(todayCal.time)
        val yesterday = dateFormat.format(yesterdayCal.time)

        if (getRunBYDate(runs, yesterday).isEmpty() && getRunBYDate(runs, today).isEmpty()) {
            sharedPreferences.edit().putInt(KEY_STREAK_DAYS, 0).apply()
        }
        val days = sharedPreferences.getInt(KEY_STREAK_DAYS, 0)
        val text = "$days /"
        tvDay.text = text
    }

    private fun setupBarChart() {
        barChart.description.isEnabled = false
        barChart.setTouchEnabled(false)
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
            setDrawAxisLine(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(true)
            setDrawAxisLine(false)
            enableGridDashedLine(10f, 10f, 0f)
            axisMinimum = 0f
            mAxisRange = 15f
        }
        barChart.axisRight.isEnabled = false
        barChart.apply {
            legend.isEnabled = false
        }
    }

    inner class AxisFormatter(private val labels: List<String>) : IndexAxisValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val indexOfBar = value.toInt()
            return if (indexOfBar < labels.size) {
                labels[indexOfBar]
            } else {
                ""
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun subscribeToObservers() {
        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            setStreakRunningDays(it)
        })
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalDistance = kotlin.math.round((it / 1000f) * 10f) / 10f
                tvTotalDistance.text = totalDistance.toString()
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = kotlin.math.round(it * 10f) / 10f
                tvAverageSpeed.text = avgSpeed.toString()
            }
        })
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                tvTotalCalories.text = it.toString()
            }
        })
        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                var allAvgSpeeds =
                    it.take(6).indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
                var allDistance =
                    it.take(6).indices.map { i ->
                        BarEntry(
                            i.toFloat(),
                            it[i].distanceInMeters / 1000f
                        )
                    }
                if (allDistance.size < 6) {
                    allAvgSpeeds = allAvgSpeeds.toMutableList()
                    allDistance = allDistance.toMutableList()
                    var i = allDistance.size
                    while (i < 6) {
                        allAvgSpeeds.add(BarEntry(i.toFloat(), 0f))
                        allDistance.add(BarEntry(i.toFloat(), 0f))
                        i++
                    }
                }
                val labels = it.take(6).map { run ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = run.timestamp
                    }
                    val dateFormat = SimpleDateFormat("dd-MMM", Locale.getDefault())
                    dateFormat.format(calendar.time)
                }
                barChart.xAxis.valueFormatter = AxisFormatter(labels)
                val barDataSetSpeed = BarDataSet(allAvgSpeeds, "avg speed").apply {
                    valueTextColor = R.color.off_white
                    color = ContextCompat.getColor(requireContext(), R.color.green)
                }
                val barDataSetDistance = BarDataSet(allDistance, "distance").apply {
                    valueTextColor = R.color.off_white
                    color = ContextCompat.getColor(requireContext(), R.color.dark_green)
                }
                val bars = arrayListOf(barDataSetSpeed, barDataSetDistance)
                val bar1 = BarData(bars as List<IBarDataSet>?)
                bar1.barWidth = 0.2f
                bar1.setValueTextSize(0f)
//        bar1.setValueTextColor(ContextCompat.getColor(applicationContext, R.color.green))
                barChart.data = bar1
                barChart.groupBars(-0.51f, 0.5f, 0.05f)
//                barChart.marker = CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
                barChart.invalidate()
            }
        })
    }
}