package com.qhaty.update.utils

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object SignUtil {
    /**
     * MD5加密
     *
     * @param byteStr 需要加密的内容
     * @return 返回 byteStr的md5值
     */
    private fun encryptionMD5(byteStr: ByteArray): String {
        val messageDigest: MessageDigest?
        val md5StrBuff = StringBuffer()
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(byteStr)
            val byteArray = messageDigest.digest()
            for (i in byteArray.indices) {
                if (Integer.toHexString(0xFF and byteArray[i].toInt()).length == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF and byteArray[i].toInt()))
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF and byteArray[i].toInt()))
                }
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return md5StrBuff.toString()
    }

    /**
     * 获取app签名md5值,与“keytool -list -keystore D:\Desktop\app_key”‘keytool -printcert     *file D:\Desktop\CERT.RSA’获取的md5值一样
     */
    fun getSignMd5Str(application: Application): String {
        try {
            // 得到签名 MD5
            val signs =if (Build.VERSION.SDK_INT >= 28) {
                val packageInfo: PackageInfo = application.packageManager.getPackageInfo(
                    application.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES)
                 packageInfo.signingInfo.apkContentsSigners
            } else {
                val packageInfo: PackageInfo = application.packageManager.getPackageInfo(
                    application.packageName,
                    PackageManager.GET_SIGNATURES)
                packageInfo.signatures
            }
            return encryptionMD5(signs[0].toByteArray())
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }
}