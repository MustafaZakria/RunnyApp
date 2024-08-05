package com.example.runningapp.ui.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.other.Constants.KEY_STREAK_DAYS
import com.example.runningapp.other.TrackingUtility.getFormattedStopWatchTime
import com.example.runningapp.other.TrackingUtility.getRunBYDate
import com.example.runningapp.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {


    private val viewModel: StatisticsViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
        setStreakRunningDays()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setStreakRunningDays() {
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        val todayCal = Calendar.getInstance()
        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DATE, -1)

        val today = dateFormat.format(todayCal.time)
        val yesterday = dateFormat.format(yesterdayCal.time)

        var days = sharedPreferences.getInt(KEY_STREAK_DAYS, 0)
        viewModel.runsSortedByDate.value?.let {
            if (getRunBYDate(it, yesterday).isEmpty() && getRunBYDate(it, today).isEmpty()) {
                days = 0
            }
            val text = "$days /"
            tvDay.text = text
        }

    }

    private fun setupBarChart() {
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.apply {
//            description.text = "Avg Speed Over Time"
            legend.isEnabled = false
        }
    }

    private fun subscribeToObservers() {
        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            val days = sharedPreferences.getInt(KEY_STREAK_DAYS, 0)
            val text = "$days /"
            tvDay.text = text
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
                val totalDistanceString = "${totalDistance}km"
                tvTotalDistance.text = totalDistanceString
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = kotlin.math.round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                tvAverageSpeed.text = avgSpeedString
            }
        })
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories = "${it}kcal"
                tvTotalCalories.text = totalCalories
            }
        })
        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeeds =
                    it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
                val allDistance =
                    it.indices.map { i -> BarEntry(i.toFloat(), it[i].distanceInMeters / 1000f) }
                val barDataSetSpeed = BarDataSet(allAvgSpeeds, "").apply {
                    valueTextColor = R.color.off_white
                    color = ContextCompat.getColor(requireContext(), R.color.green)
                }
                val barDataSetDistance = BarDataSet(allDistance, "").apply {
                    valueTextColor = R.color.off_white
                    color = Color.GREEN
                }
                barChart.data = BarData(barDataSetDistance, barDataSetSpeed)
//                barChart.marker = CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
                barChart.invalidate()
            }
        })
    }
}