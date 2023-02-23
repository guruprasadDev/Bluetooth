package com.guru.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.guru.bluetooth.databinding.ActivityFileSenderBinding
import com.guru.bluetooth.utils.Constants.EXTRA_MESSAGE

class FileSenderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFileSenderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileSenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val device: BluetoothDevice? = getBluetoothDeviceFromIntent(intent)
        val fileURI: String? = getFileUriFromIntent(intent)
        val sendingResult: Boolean? = device?.let { startBluetoothClient(it, fileURI) }
        sendingResult?.let { updateSendingResult(it) }
    }

    private fun getBluetoothDeviceFromIntent(intent: Intent): BluetoothDevice? {
        return intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
    }

    private fun getFileUriFromIntent(intent: Intent): String? {
        return intent.getStringExtra(EXTRA_MESSAGE)
    }

    private fun startBluetoothClient(device: BluetoothDevice, fileUri: String?): Boolean? {
        return fileUri?.let { BluetoothConnectionService().startClient(device, it, this) }
    }

    private fun updateSendingResult(sendingResult: Boolean) {
        binding.sendLoading.text = if (sendingResult) {
            "Successfully sent the file!"
        } else {
            "Failed to send the file, please try again later."
        }
    }
}
