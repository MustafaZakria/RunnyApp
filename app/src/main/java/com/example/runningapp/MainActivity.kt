package com.example.runningapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.runningapp.db.RunDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

//@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

//    @Inject
//    lateinit var runDoa: RunDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        Log.d("rundoa", "${runDoa.hashCode()}")
    }
}