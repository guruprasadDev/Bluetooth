package com.guru.bluetooth.helper

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

fun createAlertDialog(
    context: Context,
    title: String,
    message: String,
    positiveButtonText: String,
    negativeButtonText: String?,
    positiveButtonClickListener: DialogInterface.OnClickListener,
    negativeButtonClickListener: DialogInterface.OnClickListener?
): AlertDialog {
    val alertDialogBuilder = AlertDialog.Builder(context)
    alertDialogBuilder.apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton(positiveButtonText, positiveButtonClickListener)
        if (negativeButtonText != null && negativeButtonClickListener != null) {
            setNegativeButton(negativeButtonText, negativeButtonClickListener)
        }
    }
    return alertDialogBuilder.create()
}
