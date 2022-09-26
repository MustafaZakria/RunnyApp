package com.example.runningapp.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("Select * From running_table Order By timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("Select * From running_table Order By distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("Select * From running_table Order By timeInMillis DESC")
    fun getAllRunsSortedByTime(): LiveData<List<Run>>

    @Query("Select * From running_table Order By caloriesBurned DESC")
    fun getAllRunsSortedByCalories(): LiveData<List<Run>>

    @Query("Select * From running_table Order By avgSpeedInKMH DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query("Select Sum(timeInMillis) From running_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("Select Sum(distanceInMeters) From running_table")
    fun getTotalDistanceInMeters(): LiveData<Int>

    @Query("Select Avg(avgSpeedInKMH) From running_table")
    fun getTotalAvgSpeed(): LiveData<Float>

    @Query("Select Sum(caloriesBurned) From running_table")
    fun getTotalCaloriesBurned(): LiveData<Int>


}