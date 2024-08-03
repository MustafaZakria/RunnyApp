package com.example.runningapp.ui.fragments

import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runningapp.R
import com.example.runningapp.adapters.RunAdapter
import com.example.runningapp.db.Run
import com.example.runningapp.other.Constants
import com.example.runningapp.other.Constants.KEY_NAME
import com.example.runningapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.runningapp.other.SortType
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import kotlinx.android.synthetic.main.fragment_run.ivProfile
import kotlinx.android.synthetic.main.fragment_run.tvCalories
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.item_run.*
import kotlinx.android.synthetic.main.item_run.view.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
        setUpRecyclerView()
        setUserInfo()
        setRecentRun()

        when (viewModel.sortType) {
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                when (pos) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })

        btnStartRun.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

    }

    private fun setRecentRun() {
        viewModel.recentRuns.observe(viewLifecycleOwner) { runs ->
            val run = runs.first()

            val calender = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            tvCalendar.text = dateFormat.format(calender.time)

            val distanceInKm = "${run.distanceInMeters / 1000f}km"
            tvRecentKm.text = distanceInKm

            val caloriesBurned = "${run.caloriesBurned}kcal"
            tvCalories.text = caloriesBurned

            tvTimer.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
        }
    }

    private fun setUserInfo() {
        val userName = sharedPreferences.getString(KEY_NAME, "")
        val pictureUri = sharedPreferences.getString(Constants.KEY_PICTURE_URI, "")

        val text = "Hello, $userName"
        tvHello.text = text
        if (pictureUri?.isNotEmpty() == true) {
            val uri = Uri.parse(pictureUri)
            ivProfile.setImageURI(uri)
        }
    }

    private fun deleteRun(id: Int) {
        lifecycleScope.launch{
            val run = viewModel.getRunById(id)
            viewModel.deleteRun(run)
        }
    }

    private fun setUpRecyclerView() = rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())

        runAdapter.id.observe(viewLifecycleOwner, Observer {
            it?.let {
                deleteRun(it)
            }
        })
    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}