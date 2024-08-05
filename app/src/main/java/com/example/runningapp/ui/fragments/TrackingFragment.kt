package com.example.runningapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.db.Run
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.other.Constants.KEY_STREAK_DAYS
import com.example.runningapp.other.Constants.KEY_WEIGHT
import com.example.runningapp.other.Constants.MAP_ZOOM
import com.example.runningapp.other.Constants.POLYLINE_COLOR
import com.example.runningapp.other.Constants.POLYLINE_WIDTH
import com.example.runningapp.other.TrackingUtility.calculatePolylineLength
import com.example.runningapp.other.TrackingUtility.getFormattedStopWatchTime
import com.example.runningapp.other.TrackingUtility.getRunBYDate
import com.example.runningapp.services.PolyLine
import com.example.runningapp.services.TrackingService
import com.example.runningapp.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    var map: GoogleMap? = null

    private var curTimeInMillis = 0L

    private var isTracking = false
    private var pathPoints = mutableListOf<PolyLine>()

//    private var menu: Menu? = null

    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        val mapStyleOptions =
            MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_map)

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        btnFinishRun.setOnClickListener {
            if ((!isTracking && curTimeInMillis > 0L) || isTracking) {  //stop case
                zoomToSeeWholeTrack()
                endRunAndSaveToDb()
            }
        }

        btnExit.setOnClickListener {
            if (curTimeInMillis > 0L) {
                showCancelTrackingDialog()
            }
        }

        mapView.getMapAsync {
            map = it
            map?.setMapStyle(mapStyleOptions)
            addAllPolylines()
        }
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            updateScreenLatest()
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = getFormattedStopWatchTime(curTimeInMillis)
            tvTimer.text = formattedTime
        })
    }

    private var currentMeters = 0L
    private var lastMillis = 0L

    private fun updateScreenLatest() { //here updating calories, avg speed, distance
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val polyLine = pathPoints.last()
            val newPolyLine: PolyLine =
                mutableListOf(polyLine.last(), polyLine[polyLine.lastIndex - 1])
            currentMeters += calculatePolylineLength(newPolyLine).toInt()
            val distanceKm = currentMeters / 1000f
            tvDistanceKM.text = distanceKm.toString()

            val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
            tvCaloriesKcal.text = (distanceKm * weight).toInt().toString()

            val timeDiff = (System.currentTimeMillis() - lastMillis)
            val avgSpeed =
                kotlin.math.round(distanceKm / (timeDiff / 1000f / 60 / 60) * 10) / 10f
            tvSpeedKmh.text = avgSpeed.toString()
        }
        lastMillis = System.currentTimeMillis()
    }

    private fun toggleRun() {
        if (isTracking) {
//            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
//        this.menu = menu
//    }
//
//    override fun onPrepareOptionsMenu(menu: Menu) {
//        super.onPrepareOptionsMenu(menu)
//        if(curTimeInMillis > 0L) {
//            this.menu?.getItem(0)?.isVisible = true
//        }
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId) {
//            R.id.miCancelTracking -> {
//                showCancelTrackingDialog()
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("yes") { _, _ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun() {
        tvTimer.text = "00:00:00:00"
        tvDistanceKM.text = "0.0"
        tvCaloriesKcal.text = "0"
        tvSpeedKmh.text = "0.0"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    @SuppressLint("SetTextI18n")
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            btnToggleRun.text = "Start"
//            btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            btnToggleRun.text = "Stop"
//            menu?.getItem(0)?.isVisible = true
//            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        updateStreakRunningDays()
        map?.snapshot { bmp ->
            var distanceINMeters = 0
            for (polyline in pathPoints) {
                distanceINMeters += calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed =
                kotlin.math.round((distanceINMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceINMeters / 1000f) * weight).toInt()
            val run =
                Run(bmp, dateTimestamp, avgSpeed, distanceINMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateStreakRunningDays() {
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        val todayCal = Calendar.getInstance()
        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DATE, -1)

        val today = dateFormat.format(todayCal.time)
        val yesterday = dateFormat.format(yesterdayCal.time)

        viewModel.runs.value?.let {
            if (getRunBYDate(it, today).isEmpty()) {
                var value = 1
                if (getRunBYDate(it, yesterday).isNotEmpty()) {
                    value += sharedPreferences.getInt(KEY_STREAK_DAYS, 0)
                }
                sharedPreferences.edit().putInt(KEY_STREAK_DAYS, value).apply()
            }
        }
    }

    private fun addAllPolylines() { //to draw lines when user press the notification
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}