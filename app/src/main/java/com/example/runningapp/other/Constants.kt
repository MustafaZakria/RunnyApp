package com.example.runningapp.other

import android.graphics.Color
import com.example.runningapp.R

object Constants {

    const val RUNNING_DATABASE_NAME = "Running_db"

    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val TIMER_UPDATE_INTERVAL = 50L

    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
    const val KEY_BLOOD_TYPE = "KEY_BLOOD_TYPE"
    const val KEY_PICTURE_URI = "KEY_PICTURE_URI"
    const val KEY_STREAK_DAYS = "KEY_STREAK_DAYS"

    const val POLYLINE_COLOR = R.color.green
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1
}