package com.guru.bluetooth.client

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.guru.bluetooth.BluetoothConnectionService
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer

class BluetoothClient(private val context: Context, device: BluetoothDevice) : Thread() {

    private val socket: BluetoothSocket?

    init {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            socket = device.createRfcommSocketToServiceRecord(BluetoothConnectionService.uuid)
        } else {
            socket = null
        }
    }

    override fun run() {
        if (socket == null) {
            return
        }

        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            socket.connect()
        } catch (e: IOException) {
            return
        }

        val outputStream = socket.outputStream
        val inputStream = socket.inputStream
        val file = File(BluetoothConnectionService.fileURI)
        val fileBytes: ByteArray
        try {
            fileBytes = ByteArray(file.length().toInt())
            val bufferedInputStream = BufferedInputStream(FileInputStream(file))
            bufferedInputStream.read(fileBytes, 0, fileBytes.size)
            bufferedInputStream.close()
        } catch (e: IOException) {
            return
        }

        val fileNameSize = ByteBuffer.allocate(4)
        fileNameSize.putInt(file.name.toByteArray().size)

        val fileSize = ByteBuffer.allocate(4)
        fileSize.putInt(fileBytes.size)

        outputStream.apply {
            write(fileNameSize.array())
            write(file.name.toByteArray())
            write(fileSize.array())
            write(fileBytes)
        }

        BluetoothConnectionService.isFileTransferSuccessful = true
        sleep(5000)
        outputStream.close()
        inputStream.close()
        socket.close()
    }
}
