package com.qhaty.update.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import com.qhaty.update.UpdateOptions
import com.qhaty.update.Updater
import java.io.File
import java.net.URI

object DownLoadCenter {

    private val downloadManager: DownloadManager by lazy { Updater.context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }
    private val appUpdateReceiver: AppUpdateReceiver by lazy { AppUpdateReceiver() }
    var onDownloadComplete: ((File) -> Unit)? = { Updater.context!!.openApkByFilePath(it) }

    //更新apk Wifi&Mobile
    fun addRequest(apkUrl: String, fileName: String?, isMobileMode: Boolean = false) {
        bindReceiver() //绑定广播接收者
        val uri = Uri.parse(apkUrl)
        logd("url=$apkUrl")
        val request = DownloadManager.Request(uri).apply {
            //设置在什么网络情况下进行下载
            setAllowedNetworkTypes(if (isMobileMode) DownloadManager.Request.NETWORK_MOBILE else DownloadManager.Request.NETWORK_WIFI)
            //设置通知栏标题
            setNotificationVisibility(if (isMobileMode) DownloadManager.Request.VISIBILITY_VISIBLE else DownloadManager.Request.VISIBILITY_HIDDEN)
            setTitle(fileName)
            setDescription(Updater.context!!.packageName)
            setAllowedOverRoaming(false)
            setVisibleInDownloadsUi(true)
            //设置文件存放目录
            setDestinationInExternalFilesDir(Updater.context!!, "update", fileName)
        }

        try {
            if (UpdateOptions.autoDeleteOldApk) {
                Updater.context!!.getExternalFilesDir("update")?.delete()
                Updater.context!!.getExternalFilesDir("update")?.mkdirs()
            }
            val id = downloadManager.enqueue(request)
            //存入到share里
            SPCenter.setDownloadTaskId(id)
        } catch (e: Exception) {
            //e.printStackTrace()
        }
    }


    //查询任务
    private fun queryTaskStatus(id: Long): String {
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        while (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        }
        cursor.close()
        return ""
    }

    //下载任务是否结束
    internal fun isDownTaskProcessing(id: Long) = queryTaskStatus(id) == "192"

    //下载任务是否暂停
    internal fun isDownTaskPause(id: Long) = queryTaskStatus(id) == "193"

    //下载任务是否成功
    internal fun isDownTaskSuccess(id: Long) = queryTaskStatus(id) == "200"


    //通过下载id获取 文件地址
    private fun getFilePathByTaskId(id: Long): String {
        var filePath = ""
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        while (cursor.moveToNext()) {
            filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)) ?: ""
        }
        cursor.close()
        return filePath
    }

    //下载完成
    private fun downloadComplete(intent: Intent) {
        logd("下载完成")
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        //判断ID是否一致
        if (id != SPCenter.getDownloadTaskId()) return
        logd("注销接收者")
        unbindReceiver()//注销接收者
        try {
            val uri = Uri.parse(getFilePathByTaskId(id)).toString()
            if (uri.isBlank()) {
                logd("下载了无效文件，请确定url是否可以成功请求")
                return
            }
            //必须try-catch
            val file = File(URI(uri))
            onDownloadComplete?.invoke(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //通过下载id 找到对应的文件地址
    @Deprecated("查询下载任务的状态")
    private fun queryDownTaskById(id: Long): String? {
        var filePath: String? = null
        val query = DownloadManager.Query()

        query.setFilterById(id)
        val cursor = downloadManager.query(query)

        while (cursor.moveToNext()) {
            val address = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            filePath = address
        }
        cursor.close()
        return filePath
    }


    //绑定广播接收者
    private fun bindReceiver() {
        //广播接收者
        appUpdateReceiver.onDownloadComplete = {
            downloadComplete(it)
        }
        val filter = IntentFilter()
        filter.addAction("android.intent.action.DOWNLOAD_COMPLETE")
        filter.addAction("android.intent.action.VIEW_DOWNLOADS")
        Updater.context!!.registerReceiver(appUpdateReceiver, filter)
    }

    //取消绑定广播接收者
    private fun unbindReceiver() {
        try {
            Updater.context!!.unregisterReceiver(appUpdateReceiver)
        } catch (e: Exception) {
            //nothing
        }
    }
}

object SPCenter {
    private val sp by lazy { Updater.context!!.getSharedPreferences("updater", Context.MODE_PRIVATE) }

    /**
     * 对应downloadManager
     * 是否正在下载 任务ID
     */
    private const val DOWNLOAD_TASK_ID = "download_task_id"
    private const val LAST_REMIND = "last_remind"

    internal fun setDownloadTaskId(apkTaskID: Long) {
        sp.edit().putLong(DOWNLOAD_TASK_ID, apkTaskID).apply()
    }

    internal fun getDownloadTaskId(): Long {
        return sp.getLong(DOWNLOAD_TASK_ID, -1L)
    }

    internal fun lastRemindTime(lastTime: Long) {
        sp.edit().putLong(LAST_REMIND, lastTime).apply()
    }

    internal fun lastRemindTime(): Long {
        return sp.getLong(LAST_REMIND, 0L)
    }
}