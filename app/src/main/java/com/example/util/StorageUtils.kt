package com.example.util

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import com.example.data.StorageVolumeInfo
import java.io.File

object StorageUtils {

    fun getDefaultDownloadDir(context: Context): File {
        val publicDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (publicDownload != null && (publicDownload.exists() || publicDownload.mkdirs())) {
            return publicDownload
        }
        val appSpecific = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (appSpecific != null && (appSpecific.exists() || appSpecific.mkdirs())) {
            return appSpecific
        }
        return context.filesDir
    }

    fun getStorageVolumes(context: Context): List<StorageVolumeInfo> {
        val volumes = mutableListOf<StorageVolumeInfo>()

        // Internal Storage
        val internalDir = Environment.getExternalStorageDirectory()
        val totalInternal = internalDir.totalSpace
        val freeInternal = internalDir.usableSpace
        volumes.add(
            StorageVolumeInfo(
                name = "Internal Storage",
                path = internalDir.absolutePath,
                totalBytes = totalInternal,
                freeBytes = freeInternal,
                isUsb = false
            )
        )

        // External Storage (USB drives, SD Cards)
        try {
            val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
            for ((index, dir) in externalDirs.withIndex()) {
                if (dir != null && index > 0) {
                    val path = dir.absolutePath.split("/Android/data")[0]
                    val usbDir = File(path)
                    if (usbDir.exists()) {
                        volumes.add(
                            StorageVolumeInfo(
                                name = "USB / External Storage ${index}",
                                path = usbDir.absolutePath,
                                totalBytes = usbDir.totalSpace,
                                freeBytes = usbDir.usableSpace,
                                isUsb = true
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        return volumes
    }

    fun getCategoryDirectories(context: Context): List<Pair<String, File>> {
        val baseDownload = getDefaultDownloadDir(context)
        val movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            ?: File(baseDownload, "Movies")
        val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            ?: File(baseDownload, "Pictures")
        val music = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            ?: File(baseDownload, "Music")

        if (!movies.exists()) movies.mkdirs()
        if (!pictures.exists()) pictures.mkdirs()
        if (!music.exists()) music.mkdirs()

        return listOf(
            "Downloads" to baseDownload,
            "Movies & Videos" to movies,
            "Pictures" to pictures,
            "Audio & Music" to music
        )
    }
}
