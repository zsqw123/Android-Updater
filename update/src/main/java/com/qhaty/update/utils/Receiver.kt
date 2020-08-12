package com.qhaty.update.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

internal class AppUpdateReceiver : BroadcastReceiver() {
    var onDownloadComplete: ((intent: Intent) -> Unit)? = null

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                onDownloadComplete?.invoke(intent)
            }
        }
    }
}