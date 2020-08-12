package com.qhaty.update

import android.app.Application
import com.qhaty.update.utils.SPCenter
import com.qhaty.update.utils.getVersionCode
import com.qhaty.update.utils.logd
import com.qhaty.update.utils.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class Updater(mContext: Application, mUrl: String, mCallback: (Update, () -> Unit) -> Unit) {
    init {
        logd("updater初始化")
        context = mContext
        url = mUrl
        callback = mCallback
        NetworkCallbackImpl.regist()
        checkUpdate()
        logd("updater初始化完成")
    }

    companion object {
        var context: Application? = null
        var url: String? = null
        var callback: ((Update, () -> Unit) -> Unit)? = null

        fun checkUpdate() {
            if (UpdateOptions.detectPeriod != 0L && Date().time - SPCenter.lastRemindTime() < UpdateOptions.detectPeriod) return
            GlobalScope.launch(Dispatchers.IO) {
                if (!UpdateOptions.enableOnDebug && BuildConfig.DEBUG) return@launch
                logd("开始检测更新")
                val resp = creatUpdateService()?.getUpdate()
                if (resp == null) {
                    delay(20000)
                    logd("开始再次检测更新")
                    checkUpdate()
                    return@launch
                }
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    if (versionCode == body.versionCode) return@launch
                    context!!.update(body)
                }
            }
        }
    }
}

val versionCode by lazy { getVersionCode(Updater.context!!) }