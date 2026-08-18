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

    /**
     * Calculates total app cache size in bytes.
     */
    fun getAppCacheSize(context: Context): Long {
        var size = 0L
        try {
            context.cacheDir?.let { size += getFolderSize(it) }
            context.externalCacheDir?.let { size += getFolderSize(it) }
            context.codeCacheDir?.let { size += getFolderSize(it) }
        } catch (_: Exception) {}
        return size
    }

    private fun getFolderSize(dir: File): Long {
        var total = 0L
        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            total += if (file.isDirectory) getFolderSize(file) else file.length()
        }
        return total
    }

    /**
     * Clears all temporary app caches and returns the total bytes freed.
     */
    fun clearAppCache(context: Context): Long {
        val initialSize = getAppCacheSize(context)
        try {
            context.cacheDir?.deleteRecursively()
            context.externalCacheDir?.deleteRecursively()
            context.codeCacheDir?.deleteRecursively()
        } catch (_: Exception) {}
        val freed = (initialSize - getAppCacheSize(context)).coerceAtLeast(0L)
        return if (freed > 0) freed else initialSize
    }

    /**
     * Finds largest files in the given directory or downloads folder.
     */
    fun getLargestFiles(directory: File, limit: Int = 5): List<File> {
        val result = mutableListOf<File>()
        try {
            fun scan(dir: File) {
                dir.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        // Scan 1 level deep
                        file.listFiles()?.forEach { sub ->
                            if (sub.isFile) result.add(sub)
                        }
                    } else if (file.isFile) {
                        result.add(file)
                    }
                }
            }
            if (directory.exists()) scan(directory)
        } catch (_: Exception) {}
        return result.sortedByDescending { it.length() }.take(limit)
    }

    fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.2f GB", gb)
    }
}
