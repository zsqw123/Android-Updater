package com.qhaty.update

object UpdateOptions {
    var autoUpdateWifi = false // WiFi下预加载
    var detectPeriod = 0L // 检测更新间隔 0L则每次启动APP均提醒
    var enableOnDebug = true // DEBUG模式允许检测更新
    var customProviderAuth: String? = null // 自定义fileProvider 默认为${packageName}.provider
    var autoDeleteOldApk = true // 自动删除旧安装包
}