package com.qhaty.update.utils

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import androidx.databinding.Observable
import com.qhaty.update.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

suspend fun Context.update(update: Update) {
    val fileName = "wallpaper_v${update.versionName}.apk"
    val file = File(getExternalFilesDir("update"), fileName)
    val taskId = SPCenter.getDownloadTaskId()
    logd("==============")
    logd("taskID=$taskId")
    if (file.isFileExist()) {
        logd("【文件已经存在】")
        when {
            DownLoadCenter.isDownTaskSuccess(taskId) -> {
                logd("任务已经下载完成")
                //状态：完成
                withContext(Dispatchers.Main) {
                    Updater.callback?.invoke(update) {
                        openApkByFilePath(file)
                    }
                }
            }
            DownLoadCenter.isDownTaskPause(taskId) -> {
                logd("任务已经暂停")
                //启动下载
                logd("继续下载")
                DownLoadCenter.addRequest(update.apkFile, fileName, false)
            }
            DownLoadCenter.isDownTaskProcessing(taskId) -> {
                logd("任务正在执行当中")
            }
            else -> withContext(Dispatchers.Main) {
                Updater.callback?.invoke(update) {
                    openApkByFilePath(file)
                }
            }
        }
    } else {
        logd("开始下载回调")
        withContext(Dispatchers.Main) {
            Updater.callback?.invoke(update) {
                GlobalScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@update, R.string.start_download, Toast.LENGTH_SHORT).show()
                    }
                    DownLoadCenter.addRequest(update.apkFile, fileName)
                }
            }
        }
    }
    if (UpdateOptions.autoUpdateWifi) {
        NetObserve.wifiConnected.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (!file.isFileExist()) {
                    GlobalScope.launch(Dispatchers.IO) {
                        DownLoadCenter.onDownloadComplete = null
                        DownLoadCenter.addRequest(update.apkFile, fileName)
                    }
                }
            }
        })
    }

}

fun File.isFileExist(): Boolean {
    if (TextUtils.isEmpty(this.path)) return false
    return this.exists() && this.isFile
}