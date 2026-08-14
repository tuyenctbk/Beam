package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.example.data.FileItem
import java.io.File

object ApkParser {

    fun parseApk(context: Context, file: File): FileItem {
        val baseItem = FileItem(file = file)
        try {
            val pm = context.packageManager
            val info = pm.getPackageArchiveInfo(file.absolutePath, 0)
            val appInfo = info?.applicationInfo ?: return baseItem
            appInfo.sourceDir = file.absolutePath
            appInfo.publicSourceDir = file.absolutePath
            val appName = pm.getApplicationLabel(appInfo).toString()
            val pkgName = info.packageName
            val version = info.versionName ?: "1.0"
            return baseItem.copy(
                apkAppName = appName,
                apkPackageName = pkgName,
                apkVersion = version
            )
        } catch (_: Exception) {}
        return baseItem
    }

    fun getApkIcon(context: Context, file: File): Drawable? {
        try {
            val pm = context.packageManager
            val info = pm.getPackageArchiveInfo(file.absolutePath, 0)
            val appInfo = info?.applicationInfo ?: return null
            appInfo.sourceDir = file.absolutePath
            appInfo.publicSourceDir = file.absolutePath
            return pm.getApplicationIcon(appInfo)
        } catch (_: Exception) {}
        return null
    }
}

