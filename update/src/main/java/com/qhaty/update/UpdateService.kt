package com.qhaty.update

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.net.Proxy
import java.util.concurrent.TimeUnit

interface UpdateService {
    @GET("update.json")
    suspend fun getUpdate(): Response<Update>
}

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

var updateService: UpdateService? = null

@Synchronized
fun creatUpdateService(): UpdateService? {
    try {
        if (updateService != null) return updateService
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
        val retrofit = Retrofit.Builder().baseUrl(Updater.url!!).addConverterFactory(
            MoshiConverterFactory.create(Moshi.Builder().build())
        ).client(client).build()
        return retrofit.create(UpdateService::class.java).also { updateService = it }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
