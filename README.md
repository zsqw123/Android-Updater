# Android Updater

快捷的Android更新检测实现  
***注意！*** **此开源库目前正处于新手阶段,可能存在部分bug,建议仅供学习使用**

## 使用

### 依赖

```gradle
dependencies {
    implementation 'com.qhaty:update:1.0.0'
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
| url | **类型:String** 检测更新json所在的目录url,若json文件位置为 <http://qq.com/apk/update.json> 则此处应为 <http://qq.com/apk> |
| update | **类型:Update** update参数 |
| callback | **类型() -> Unit** 接收到update时的回调 |

### 后端配置

json格式,文件名为update.json
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
    var detectPeriod = 0L // 检测更新间隔 0L则每次启动APP均提醒
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
