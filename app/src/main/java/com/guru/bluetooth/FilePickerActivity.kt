package com.guru.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.guru.bluetooth.databinding.ActivityFilePickerBinding
import com.guru.bluetooth.extensions.showToast
import com.guru.bluetooth.helper.FilePickerHelper
import java.io.File

class FilePickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilePickerBinding
    private var device: BluetoothDevice? = null
    private var fileURI: String = ""

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
            fileSelectButton.setOnClickListener { filePicker() }
            fileSelectorSend.setOnClickListener { send() }
        }
    }

    @RequiresApi(33)
    @SuppressLint("MissingPermission")
    fun setDeviceInfoNameValue() {
        //device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        binding.deviceInfoNameValue.text.apply {
            if (device?.name == null) {
                device?.address
            } else {
                device?.name
            }
        }
    }

    private fun filePicker() {
        getContent.launch("*/*")
    }

    private fun send() {
        if (fileURI == "") {
            showToast("Please choose a file first")
        } else {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.apply {
                setTitle("Confirmation")
                setMessage("Are you sure want to send this file?")
                setPositiveButton("Send") { _, _ -> checkLessThan5MB(fileURI) }
                setNegativeButton("Cancel") { _, _ ->
                    showToast("Cancelled the file sending process")
                }
                alertDialogBuilder.show()
            }
        }
    }

    private fun checkLessThan5MB(fileURI: String) {
        val fiveMB = 1024 * 1024 * 5;
        val file = File(fileURI)

        if (file.readBytes().size > fiveMB) {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("File too large")
            alertDialogBuilder.setMessage("This file is larger than the 5MB Limit")
            alertDialogBuilder.setPositiveButton("OK") { _, _ ->
                showToast("File sending failed")
            }
            alertDialogBuilder.show()
        } else {
            val intent = Intent(this, FileSenderActivity::class.java)
            intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, fileURI)
            startActivity(intent)
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val selectedFilePath = FilePickerHelper.getPath(this, it)
                binding.fileInfoNameValue.text = selectedFilePath
                fileURI = selectedFilePath!!
            } ?: showToast("File choosing cancelled")
        }
}
