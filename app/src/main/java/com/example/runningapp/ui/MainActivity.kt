package com.example.runningapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.runningapp.R

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