package com.qhaty.update.utils

import android.content.Context
import android.os.Build
import android.util.Log

internal inline fun <reified T> T.logd(info: String) {
    Log.i(T::class.java.name + " => DEBUG-update", info)
}

internal fun getVersionCode(context: Context): Int {
    try {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) packageInfo.versionCode
        else packageInfo.longVersionCode.toInt()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0
}