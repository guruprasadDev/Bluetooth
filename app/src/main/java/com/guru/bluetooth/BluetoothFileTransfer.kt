package com.guru.bluetooth

import android.Manifest.permission.BLUETOOTH
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.os.Process.myPid
import android.os.Process.myUid
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

@SuppressLint("MissingPermission")
class BluetoothFileTransfer(private val context: Context) {

    private val adapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val uuid: UUID by lazy { UUID.fromString(context.getString(R.string.uuid)) }

    private val name: String by lazy {
        if (context.checkPermission(
                BLUETOOTH,
                myPid(),
                myUid()
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            adapter?.name ?: "BluetoothTransfer"
        } else {
            throw SecurityException("BLUETOOTH permission not granted")
        }
    }

    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    private var outputStream: FileOutputStream? = null
    private var inputStream: FileInputStream? = null

    val outputFile = File("output.txt")
    val inputFile = File("input.txt")


    suspend fun startServer() = withContext(Dispatchers.IO) {
        serverSocket = adapter?.listenUsingInsecureRfcommWithServiceRecord(name, uuid)
        serverSocket?.accept()?.also { socket ->
            clientSocket = socket
            outputStream = FileOutputStream(outputFile)
            inputStream = FileInputStream(inputFile)
        }
    }

    suspend fun connectToDevice(device: BluetoothDevice) = withContext(Dispatchers.IO) {
        clientSocket = device.createRfcommSocketToServiceRecord(uuid)
        clientSocket?.connect()
        outputStream = FileOutputStream(outputFile)
        inputStream = FileInputStream(inputFile)
    }

    suspend fun sendFile(uri: Uri) = withContext(Dispatchers.IO) {
        val file = getFileFromUri(uri)
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        inputStream = FileInputStream(file)
        var bytesRead: Int
        var totalBytesRead: Long = 0

        while (inputStream!!.read(buffer).also { bytesRead = it } > 0) {
            outputStream?.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead
        }

        inputStream!!.close()
        outputStream?.flush()
    }

    suspend fun receiveFile() = withContext(Dispatchers.IO) {
        val fileName = "received_file"
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        var bytesRead: Int
        var totalBytesRead: Long = 0

        outputStream?.write(fileName.toByteArray())
        outputStream?.flush()

        while (inputStream?.read(buffer).also { bytesRead = it ?: -1 }!! > 0 && bytesRead != -1) {
            file.outputStream().write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead
        }

        file.outputStream().close()
        inputStream?.close()
    }



    private suspend fun getFileFromUri(uri: Uri): File = withContext(Dispatchers.IO) {
        val contentResolver: ContentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val name = cursor?.getString(nameIndex!!)
        cursor?.close()
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name!!)
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 1024 * 4
    }
}