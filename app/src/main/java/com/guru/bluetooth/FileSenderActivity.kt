package com.guru.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import androidx.appcompat.app.AppCompatActivity
import com.guru.bluetooth.databinding.ActivityFileSenderBinding

class FileSenderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFileSenderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileSenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val device: BluetoothDevice? = getBluetoothDeviceFromIntent(intent)
        val fileURI: String? = getFileUriFromIntent(intent)
        val sendingResult = device?.let { startBluetoothClient(it, fileURI!!) }
        updateSendingResult(sendingResult == true)
    }

    private fun getBluetoothDeviceFromIntent(intent: Intent): BluetoothDevice? {
        return intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
    }

    private fun getFileUriFromIntent(intent: Intent): String? {
        return intent.getStringExtra(AlarmClock.EXTRA_MESSAGE)
    }

    private fun startBluetoothClient(device: BluetoothDevice, fileUri: String): Boolean {
        return BluetoothConnectionService().startClient(device, fileUri)
    }

    private fun updateSendingResult(sendingResult: Boolean) {
        if (sendingResult) {
            binding.sendLoading.setText("Successfully sent the file!")
        } else {
            binding.sendLoading.setText("Failed to send the file, please try again later.")
        }
    }
}
