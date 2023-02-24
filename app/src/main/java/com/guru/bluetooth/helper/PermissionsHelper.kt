package com.guru.bluetooth.helper

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.guru.bluetooth.utils.Constants.REQUEST_ACCESS_COARSE_LOCATION
import com.guru.bluetooth.utils.Constants.REQUEST_READ_EXTERNAL_STORAGE

class PermissionsHelper(private val activity: Activity) {

    fun requestAccessCoarseLocationPermissionIfNotGranted() {
        val permission = android.Manifest.permission.ACCESS_COARSE_LOCATION
        requestPermissionIfNotGranted(permission, REQUEST_ACCESS_COARSE_LOCATION)
    }

    fun requestReadExternalStoragePermissionIfNotGranted() {
        val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        requestPermissionIfNotGranted(permission, REQUEST_READ_EXTERNAL_STORAGE)
    }

    private fun requestPermissionIfNotGranted(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(activity, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
            }
        }
    }
    object PermissionGranted {
        fun isPermissionGranted(context: Context, permission: String): Boolean {
            return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}
