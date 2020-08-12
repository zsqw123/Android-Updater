package com.qhaty.update.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.qhaty.update.UpdateOptions
import java.io.File

/**
 * 直接打开APK
 */
internal fun Context.openApkByFilePath(file: File) {
    //防止有的系统 强制关闭安装未知来源的app 导致的crash
    try {
        println("open---------->")
        startActivity(constructOpenApkIntent(file))
        println("open----------11111>")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * /构造打开APK的Intent
 */
internal fun Context.constructOpenApkIntent(file: File): Intent {
    val intent = Intent(Intent.ACTION_VIEW)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //添加对目标应用临时授权该Uri所代表的文件
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
    val apkUri = getUri4File(this, file)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
    //查询所有符合 intent 跳转目标应用类型的应用，注意此方法必须放置setDataAndType的方法之后
    val resInfoList: List<ResolveInfo> = this.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    //然后全部授权
    for (resolveInfo in resInfoList) {
        val packageName = resolveInfo.activityInfo.packageName
        this.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return intent
}

/**
 * 获取文件的Uri
 * @param file 文件
 */
internal fun getUri4File(context: Context, file: File?): Uri {
    val fileProviderAuth = UpdateOptions.customProviderAuth ?: "${context.packageName}.provider"
    checkNotNull(file)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(context.applicationContext, fileProviderAuth, file)
    } else {
        Uri.fromFile(file)
    }
}
