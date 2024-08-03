package com.example.runningapp.ui.fragments

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.runningapp.R
import com.example.runningapp.other.Constants.KEY_BLOOD_TYPE
import com.example.runningapp.other.Constants.KEY_NAME
import com.example.runningapp.other.Constants.KEY_PICTURE_URI
import com.example.runningapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_setup.etName
import kotlinx.android.synthetic.main.fragment_setup.etWeight
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var glide: Glide

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFromSharedPref()
        btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success) {
                Snackbar.make(view, "Saved changes", Toast.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Please enter all the fields", Toast.LENGTH_LONG).show()
            }
        }
        tvUploadPhoto.setOnClickListener {
            loadPhotoFromGallery()
        }
    }

    private fun loadPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        resultLauncher.launch(intent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri = result.data?.data as Uri
                ivProfile.setImageURI(uri)
                sharedPref.edit().putString(KEY_PICTURE_URI, uri.toString()).apply()
            }
        }

    private fun loadFromSharedPref() {
        val name = sharedPref.getString(KEY_NAME, "")
        val weight = sharedPref.getFloat(KEY_WEIGHT, 80f)
        val bloodType = sharedPref.getString(KEY_BLOOD_TYPE, "")
        val pictureUri = sharedPref.getString(KEY_PICTURE_URI, "")
        etName.setText(name)
        etWeight.setText(weight.toString())
        etBloodType.setText(bloodType)
        if (pictureUri?.isNotEmpty() == true) {
            val uri = Uri.parse(pictureUri)
            ivProfile.setImageURI(uri)
        }
    }

    private fun applyChangesToSharedPref(): Boolean {
        val name = etName.text.toString()
        val weight = etWeight.text.toString()
        val bloodType = etBloodType.text.toString()
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putString(KEY_BLOOD_TYPE, bloodType)
            .apply()

        return true
    }
}