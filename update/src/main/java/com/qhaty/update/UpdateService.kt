package com.qhaty.update

import android.util.Log
import com.qhaty.update.utils.logd
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.net.Proxy
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Update(
    @Json(name = "versionCode")
    val versionCode: Int,
    @Json(name = "new_version")
    val versionName: String,
    @Json(name = "update_log")
    val desc: String,
    @Json(name = "apk_file_url")
    val apkFile: String,
    @Json(name = "target_size")
    val size: String,
    @Json(name = "constraint")
    val force: Boolean
)

@Synchronized
suspend fun updateRequest(url: String): Update? {
    return withContext(Dispatchers.IO) {
        try {
            val mLogging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.d("TAG------update", "log: $message")
                }
            })
            mLogging.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().proxy(Proxy.NO_PROXY).addInterceptor(mLogging)
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .build()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) {
                delay(20000)
                logd("开始再次检测更新")
                updateRequest(url)
                return@withContext null
            }
            val moshi = Moshi.Builder().build()
            return@withContext moshi.adapter(Update::class.java).fromJson(response.body!!.string())
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}