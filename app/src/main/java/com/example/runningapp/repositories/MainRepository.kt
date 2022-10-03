package com.example.runningapp.repositories

import androidx.lifecycle.viewModelScope
import com.example.runningapp.db.Run
import com.example.runningapp.db.RunDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import javax.inject.Inject


class MainRepository @Inject constructor(
    val runDao: RunDao
){
    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    suspend fun getRunById(id: Int) = runDao.getRunById(id)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTime() = runDao.getAllRunsSortedByTime()

    fun getAllRunsSortedByCalories() = runDao.getAllRunsSortedByCalories()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

    fun getTotalDistanceInMeters() = runDao.getTotalDistanceInMeters()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

}