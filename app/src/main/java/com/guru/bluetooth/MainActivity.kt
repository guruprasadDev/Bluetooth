package com.guru.bluetooth

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.guru.bluetooth.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var bluetoothDevice: BluetoothDevice? = null
    private val bluetoothFileTransfer by lazy { BluetoothFileTransfer(this) }
    private val requestEnableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Toast.makeText(this, "Bluetooth must be enabled", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private val requestDiscoverableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Toast.makeText(this, "Device must be discoverable", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            startServer()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                scanForDevices(this)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    private val deviceClickListener = { device: BluetoothDevice ->
        bluetoothDevice = device
        connectToDevice(device)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        enableBluetooth()
    }

    private fun setupViews() {
        binding.selectFileButton.setOnClickListener {
            selectFile()
        }

        binding.scanButton.setOnClickListener {
            scanForDevices(this)
        }

        binding.sendButton.setOnClickListener {
            val fileUri = binding.fileUriTextView.text.toString()
            if (fileUri.isNotEmpty() && bluetoothDevice != null) {
                sendFile(getFileUri(fileUri))
            } else {
                Toast.makeText(
                    this,
                    "Please select a file and connect to a device",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.receiveButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                bluetoothFileTransfer.receiveFile()
            }
        }
    }

    private fun enableBluetooth() {
        if (bluetoothAdapter == null) {//let
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show()
            finish()
        }

        if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestEnableBtLauncher.launch(enableBtIntent)
        }
    }
    
    private fun startServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                bluetoothFileTransfer.startServer()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to start server: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun scanForDevices(activity: Activity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, 0)
    }

    private fun getFileUri(uriString: String) = Uri.parse(uriString)

    private fun connectToDevice(device: BluetoothDevice) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                bluetoothFileTransfer.connectToDevice(device)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to connect to device: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun sendFile(fileUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                bluetoothFileTransfer.sendFile(fileUri)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "File sent successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to send file: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

