package com.example.runningapp.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import timber.log.Timber

class TrackingService: LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("service started")
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("service paused")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("service stpped")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}