package com.guru.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.widget.Toast
import com.guru.bluetooth.client.BluetoothClient
import com.guru.bluetooth.server.BluetoothServerController
import java.util.*

class BluetoothConnectionService {
    companion object {
        val uuid: UUID = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
        var fileURI: String? = null
        var isFileTransferSuccessful = false
    }

    fun startServer(context: Context) {
        BluetoothServerController(context).start()
    }

    fun startClient(device: BluetoothDevice, uri: String, context: Context): Boolean {
        fileURI = uri
        return try {
            val bluetoothClient = BluetoothClient(context, device)
            bluetoothClient.start()
            bluetoothClient.join()
            true // file transfer successful
        } catch (e: InterruptedException) {
            false // file transfer failed
        }
    }
}
