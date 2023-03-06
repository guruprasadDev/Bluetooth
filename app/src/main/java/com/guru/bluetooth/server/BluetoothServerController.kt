package com.guru.bluetooth.server

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.guru.bluetooth.BluetoothConnectionService
import java.io.IOException

class BluetoothServerController(private val context: Context) : Thread() {
    private var isCancelled: Boolean = false
    private var serverSocket: BluetoothServerSocket? = null

    init {
//       val bluetoothAdapter = (getSystemService(context,BLUETOOTH_SERVICE) as BluetoothManager).adapter
//use di for injecting bluetooth manager
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                this.serverSocket = bluetoothAdapter
                    .listenUsingRfcommWithServiceRecord(
                        "BluetoothFileTransfer",
                        BluetoothConnectionService.uuid
                    )
                this.isCancelled = false
            } else {
                this.serverSocket = null
                this.isCancelled = true
            }
        }
    }

    override fun run() {
        var socket: BluetoothSocket
        while (true) {
            if (isCancelled) {
                break//debug
            }

            try {
                socket = serverSocket!!.accept()
            } catch (e: IOException) {
                break
            }

            if (!isCancelled && socket != null) {
                BluetoothServer(socket).start()//
            }
        }
    }

    fun cancel() {
        this.isCancelled = true
        this.serverSocket?.close()
    }
}
