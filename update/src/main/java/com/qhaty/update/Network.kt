package com.qhaty.update

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField


class Network(app: Context) {
    init {
        appContext = app
    }

    companion object {
        var appContext: Context? = null
        private const val WIFI = 0
        private const val DATA = 1
        fun checkIfConn(): Boolean {
            networkConnected(appContext, WIFI)
            networkConnected(appContext, DATA)
            return NetObserve.netConnected.get()!!
        }

        fun checkIfData(): Boolean {
            networkConnected(appContext, DATA)
            return NetObserve.dataConnected.get()!!
        }

        private fun networkConnected(context: Context?, networkType: Int) {
            val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val field = when (networkType) {
                DATA -> NetObserve.netConnected
                else -> NetObserve.wifiConnected
            }
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                val sysCode = when (networkType) {
                    DATA -> ConnectivityManager.TYPE_MOBILE
                    else -> ConnectivityManager.TYPE_WIFI
                }
                val mNetworkInfo = cm.activeNetworkInfo
                if (mNetworkInfo != null && mNetworkInfo.type == sysCode) field.set(true);return
            } else {
                val network: Network? = cm.activeNetwork
                if (network != null) {
                    val nc = cm.getNetworkCapabilities(network)
                    val sysCode = when (networkType) {
                        DATA -> NetworkCapabilities.TRANSPORT_CELLULAR
                        else -> NetworkCapabilities.TRANSPORT_WIFI
                    }
                    if (nc != null && nc.hasTransport(sysCode)) field.set(true);return
                }
            }
        }
    }
}

class NetObserve : BaseObservable() {
    companion object {
        val netConnected by lazy { ObservableField(false) }
        val dataConnected by lazy { ObservableField(false) }
        val wifiConnected by lazy { ObservableField(false) }
    }
}