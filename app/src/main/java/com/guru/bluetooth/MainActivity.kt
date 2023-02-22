package com.guru.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.guru.bluetooth.databinding.ActivityMainBinding
import com.guru.bluetooth.extensions.showToast
import com.guru.bluetooth.helper.PermissionsHelper
import com.guru.bluetooth.server.BluetoothServerController

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val permissionsHelper = PermissionsHelper(this)
    private lateinit var pairedDevices: Set<BluetoothDevice>
    private lateinit var discoveredDevices: ArrayList<BluetoothDevice>
    private var isInScanningMode: Boolean = false
    private val requestEnableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            showToast("Bluetooth must be enabled")
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        permissionsHelper.requestAccessCoarseLocationPermissionIfNotGranted()
        permissionsHelper.requestReadExternalStoragePermissionIfNotGranted()

        discoveredDevices = ArrayList()

        val deviceFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, deviceFilter)

        val deviceNameFilter = IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED)
        registerReceiver(nameReceiver, deviceNameFilter)

        initListener()
        enableBluetooth()
        launchEnableBluetoothActivity()
        launchEnableDiscoverableActivity()
    }

    private fun initListener() {
        binding.mainEnterZone.setOnClickListener { enterScanningMode() }
        binding.mainRefreshUserList.setOnClickListener { refreshList() }
    }

    private fun enableBluetooth() {
        if (bluetoothAdapter == null) {
            showToast("This device does not support Bluetooth")
            this.finish()
            return
        }

        if (bluetoothAdapter?.isEnabled != true) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestEnableBtLauncher.launch(enableBtIntent)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val index = discoveredDevices.indexOf(device)
                    if (index == -1) {
                        discoveredDevices.add(device!!)
                    } else {
                        discoveredDevices[index] = device!!
                    }
                }
            }
        }
    }

    private val nameReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            when (action) {
                BluetoothDevice.ACTION_NAME_CHANGED -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val index = discoveredDevices.indexOf(device!!)
                    if (index == -1) {
                        discoveredDevices.add(device)
                    } else {
                        discoveredDevices[index] = device
                    }
                }
            }
        }
    }

    private fun refreshList() {
        if (!isInScanningMode) {
            showToast("Please enter the scanning mode first")
            return
        }

        pairedDevices = bluetoothAdapter?.bondedDevices as Set<BluetoothDevice>
        val list: ArrayList<BluetoothDevice> = ArrayList()
        val listDeviceNames: ArrayList<String> = ArrayList()
        if (pairedDevices.isNotEmpty()) {
            for (device: BluetoothDevice in pairedDevices) {
                list.add(device)
                listDeviceNames.add(device.name)
            }
        } else {
            showToast("No paired bluetooth devices found")
        }

        if (discoveredDevices.isNotEmpty()) {
            for (device: BluetoothDevice in discoveredDevices) {
                list.add(device)
                if (device.name == null) {
                    listDeviceNames.add(device.address)
                } else {
                    listDeviceNames.add(device.name)
                }
            }
        } else {
            showToast("No new bluetooth devices found")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listDeviceNames)
        binding.mainSelectUserList.adapter = adapter
        binding.mainSelectUserList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val device: BluetoothDevice = list[position]

                val intent = Intent(this, FilePickerActivity::class.java)
                intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
                startActivity(intent)
            }
    }

    private fun launchEnableBluetoothActivity() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetooth.launch(enableBtIntent)
    }

    private val enableBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleEnableBluetoothActivityResult(result.resultCode)
    }

    private fun handleEnableBluetoothActivityResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            if (bluetoothAdapter?.isEnabled == true) {
                showToast("Bluetooth has been enabled")
            } else {
                showToast("Bluetooth has been disabled")
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            showToast("Bluetooth has been cancelled")
        }
    }

    private fun launchEnableDiscoverableActivity() {
        val enableDiscoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        enableDiscoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        enableDiscoverable.launch(enableDiscoverableIntent)
    }

    private val enableDiscoverable = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleEnableDiscoverableActivityResult(result.resultCode)
    }

    private fun handleEnableDiscoverableActivityResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_CANCELED) {
            showToast("Bluetooth has been cancelled")
        }
    }

    private fun changeTextToConnected(statusTextView: TextView) {
        statusTextView.text = "Scanning Mode"
        statusTextView.setTextColor(Color.GREEN)

        binding.mainEnterZone.text = "Exit Scanning Mode"
    }

    private fun changeTextToDisconnected(statusTextView: TextView) {
        statusTextView.text = "Not in Scanning Mode"
        statusTextView.setTextColor(Color.RED)

        binding.mainEnterZone.text = "Enter Scanning Mode"
    }

    private fun enterScanningMode() {
        if (isInScanningMode) {
            exitScanningMode()
        } else {
            if (!bluetoothAdapter!!.isEnabled) {
                bluetoothAdapter!!.enable()
            }

            val discoverableIntent: Intent =
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300) //5 mins
                }
            startActivity(discoverableIntent)
            BluetoothConnectionService().startServer()

            changeTextToConnected(binding.statusTitle)
            isInScanningMode = true
            refreshList()
        }
    }

    private fun exitScanningMode() {
        BluetoothServerController().cancel()
        bluetoothAdapter?.cancelDiscovery()

        binding.mainSelectUserList.adapter = null
        showToast("Discoverability is disabled for now")
        changeTextToDisconnected(binding.statusTitle)
        isInScanningMode = false
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        unregisterReceiver(nameReceiver)
    }
}
