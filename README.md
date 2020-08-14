# Android Updater

[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)

快捷的Android更新检测实现，支持Android Q  
***注意！*** **此开源库目前正处于测试阶段,API随时有可能发生变化,并可能存在部分bug,建议仅供学习使用**

## 使用

### 依赖

[![Download](https://api.bintray.com/packages/qhaty/updater/updater/images/download.svg)](https://bintray.com/qhaty/updater/updater/_latestVersion)

```gradle
dependencies {
    implementation 'com.qhaty:update:1.0.2'
}
```

若未找到依赖请尝试在项目根目录添加

```gradle
repositories {
    maven {
        url  "https://dl.bintray.com/qhaty/updater"
    }
}
```

### 调用

建议在MainActivity里调用该方法,Kotlin方法如下

```kotlin
    Updater(application, url) { update, callback -> ... }
```

| 参数 | 描述 |
| - | - |
| application | 传入Application |
| url | **类型:String** 检测更新json的url,如 <http://qq.com/apk/update.json> |
| update | **类型:Update** update参数 |
| callback | **类型() -> Unit** 接收到update时的回调,无论是否已经有下载缓存都会调用callback方法,若调用callback方法要对界面做出更新, 请注意外层使用runOnUiThread方法 |

适配Android7.0+:在manifest中修改或添加

```xml
<provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.provider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
 </provider>
```

resource文件```@xml/file_paths```修改或添加如下配置

```xml
<paths>
    <external-files-path
        name="AppUpdate"
        path="update" />
</paths>
```

### 后端配置

json格式
| 参数 | 描述 | 对应Update对象的属性 |
| - | - | - |
| versionCode | Int | versionCode |
| new_version | String 对应versionName | versionName |
| update_log | String 更新日志 | desc |
| apk_file_url | String apk文件链接 | apkFile |
| target_size | String apk文件大小 | size |
| constraint | Boolean 是否强制更新 | force |

### 更多选项

```kotlin
// 以下为默认值
object UpdateOptions {
    var autoUpdateWifi = false // WiFi下预加载
    var detectPeriod = 0L // 检测更新间隔 0L则每次启动APP均提醒 方式:Date().time
    var enableOnDebug = true // DEBUG模式允许检测更新
    var customProviderAuth: String? = null // 自定义fileProvider 默认为${packageName}.provider
    var autoDeleteOldApk = true // 自动删除旧安装包
}
```

在Updater创建之前对UpdateOptions进行配置即可,例如下面的方法可以将DEBUG模式检测更新关闭

```kotlin
UpdateOptions.enableOnDebug = false
```

也可以使用kotlin apply之类的方法进行操作
