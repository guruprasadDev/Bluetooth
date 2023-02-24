package com.guru.bluetooth.viewmodel

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.guru.bluetooth.helper.PermissionsHelper
import com.guru.bluetooth.utils.Constants.REQUEST_ENABLE_BT

class MainViewModel(private val context: Context) : ViewModel() {
    private val _showToast = MutableLiveData<String>()
    val showToast: LiveData<String> = _showToast
    private val _deviceList = MutableLiveData<List<BluetoothDevice>>()

    private fun updateToastMessage(message: String) {
        _showToast.postValue(message)
    }

    fun enableBluetooth() {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            updateToastMessage("This device does not support Bluetooth")
        }

        if (!bluetoothAdapter.isEnabled) {
            updateToastMessage("Bluetooth enabled")
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (!PermissionsHelper.PermissionGranted.isPermissionGranted(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            ) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                (context as? Activity)?.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }
}
