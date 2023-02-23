package com.guru.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.guru.bluetooth.databinding.ActivityFilePickerBinding
import com.guru.bluetooth.extensions.showToast
import com.guru.bluetooth.helper.FilePickerHelper
import com.guru.bluetooth.helper.createAlertDialog
import java.io.File

class FilePickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilePickerBinding
    private var device: BluetoothDevice? = null
    private var fileURI: String? = null

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
        setDeviceInfoNameValue()
    }

    private fun initListener() {
        binding.apply {
            fileSelectButton.setOnClickListener { chooseFile() }
            fileSelectorSend.setOnClickListener { sendFile() }
        }
    }

    private fun setDeviceInfoNameValue() {
        //device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        )
            binding.deviceInfoNameValue.text = if (device?.name == null) {
                device?.address
            } else {
                device?.name
            }
    }

    private fun chooseFile() {
        getContent.launch("*/*")
    }

    private fun sendFile() {
        if (fileURI == null) {
            showToast("Please choose a file first")
        } else {
            val alertDialog = createAlertDialog(
                this,
                "Confirmation",
                "Are you sure want to send this file?",
                "Send",
                "Cancel",
                { _, _ -> fileURI?.let { sendFileIfSizeIsLessThan5MB(it) } },
                { _, _ -> showToast("Cancelled the file sending process") }
            )
            alertDialog.show()
        }
    }

    private fun sendFileIfSizeIsLessThan5MB(fileURI: String) {
        val fiveMB = 1024 * 1024 * 5
        val file = File(fileURI)

        fun sendFileToReceiver(device: BluetoothDevice, fileURI: String) {
            val intent = Intent(this, FileSenderActivity::class.java)
            intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, fileURI)
            startActivity(intent)
        }

        if (file.readBytes().size > fiveMB) {
            val alertDialog = createAlertDialog(
                this,
                "File too large",
                "This file is larger than the 5MB Limit",
                "OK",
                "",
                DialogInterface.OnClickListener { _, _ -> showToast("File sending failed") },
                null
            )
            alertDialog.show()
        } else {
            device?.let { sendFileToReceiver(it, fileURI) }
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val selectedFilePath = FilePickerHelper.getPath(this, uri)
                binding.fileInfoNameValue.text = selectedFilePath
                fileURI = selectedFilePath
            } ?: showToast("File choosing cancelled")
        }
}
