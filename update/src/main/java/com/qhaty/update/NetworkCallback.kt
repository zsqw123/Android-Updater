package com.qhaty.update

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.qhaty.update.utils.logd


class NetworkCallbackImpl : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        NetObserve.netConnected.set(true)
        logd("网络连接")
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        NetObserve.netConnected.set(false)
        logd("网络断开")
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                logd("wifi网络已连接")
                NetObserve.wifiConnected.set(true)
                NetObserve.dataConnected.set(false)
            } else {
                logd("移动网络已连接")
                NetObserve.wifiConnected.set(false)
                NetObserve.dataConnected.set(true)
            }
        }
    }

    companion object {
        fun regist() {
            val request = NetworkRequest.Builder().build()
            val mConnectivityManager = Updater.context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            mConnectivityManager.registerNetworkCallback(request,
                NetworkCallbackImpl()
            )
        }
    }
}