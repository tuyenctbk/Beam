package com.example.data

import androidx.annotation.StringRes
import com.example.R
import java.io.File

enum class FileCategory(@StringRes val labelResId: Int, val label: String) {
    ALL(R.string.category_all, "All Files"),
    DOWNLOADS(R.string.recent_downloads, "Downloads"),
    APKS(R.string.category_apks, "APKs"),
    VIDEOS(R.string.category_videos, "Movies & Videos"),
    PHOTOS(R.string.category_photos, "Pictures"),
    ZIP(R.string.category_zip, "Archives"),
    MUSIC(R.string.category_music, "Audio"),
    DOCUMENTS(R.string.category_documents, "Documents")
}

enum class FileSortOption(@StringRes val labelResId: Int, val label: String) {
    DATE_DESC(R.string.sort_date, "Newest First"),
    DATE_ASC(R.string.sort_date, "Oldest First"),
    NAME_ASC(R.string.sort_name, "Name (A-Z)"),
    NAME_DESC(R.string.sort_name, "Name (Z-A)"),
    SIZE_DESC(R.string.sort_size, "Largest First"),
    SIZE_ASC(R.string.sort_size, "Smallest First")
}

data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val sizeBytes: Long = file.length(),
    val lastModified: Long = file.lastModified(),
    val isDirectory: Boolean = file.isDirectory,
    val category: FileCategory = determineCategory(file),
    val extension: String = file.extension.lowercase(),
    val apkPackageName: String? = null,
    val apkAppName: String? = null,
    val apkVersion: String? = null
) {
    val formattedSize: String
        get() {
            if (isDirectory) return "Folder"
            val kb = sizeBytes / 1024.0
            if (kb < 1024) return String.format("%.1f KB", kb)
            val mb = kb / 1024.0
            if (mb < 1024) return String.format("%.1f MB", mb)
            val gb = mb / 1024.0
            return String.format("%.2f GB", gb)
        }

    companion object {
        fun determineCategory(file: File): FileCategory {
            if (file.isDirectory) return FileCategory.ALL
            val ext = file.extension.lowercase()
            return when (ext) {
                "apk" -> FileCategory.APKS
                "mp4", "mkv", "avi", "webm", "mov", "ts", "3gp" -> FileCategory.VIDEOS
                "jpg", "jpeg", "png", "gif", "webp", "bmp" -> FileCategory.PHOTOS
                "zip", "rar", "7z", "tar", "gz" -> FileCategory.ZIP
                "mp3", "flac", "wav", "aac", "ogg", "m4a" -> FileCategory.MUSIC
                "pdf", "txt", "doc", "docx", "xls", "xlsx" -> FileCategory.DOCUMENTS
                else -> FileCategory.ALL
            }
        }
    }
}

data class ActiveTransfer(
    val id: String,
    val fileName: String,
    val totalBytes: Long,
    var receivedBytes: Long,
    val clientIp: String,
    val startTime: Long = System.currentTimeMillis()
) {
    val progress: Float
        get() = if (totalBytes > 0) (receivedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedSpeed: String
        get() {
            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000.0).coerceAtLeast(0.1)
            val speedKb = (receivedBytes / 1024.0) / elapsedSec
            return if (speedKb >= 1024) {
                String.format("%.2f MB/s", speedKb / 1024.0)
            } else {
                String.format("%.0f KB/s", speedKb)
            }
        }

    val formattedSizeProgress: String
        get() {
            val recMb = receivedBytes / (1024.0 * 1024.0)
            val totMb = totalBytes / (1024.0 * 1024.0)
            return if (totalBytes > 0) {
                String.format("%.1f MB / %.1f MB (%.0f%%)", recMb, totMb, progress * 100)
            } else {
                String.format("%.1f MB received", recMb)
            }
        }
}

data class StorageVolumeInfo(
    val name: String,
    val path: String,
    val totalBytes: Long,
    val freeBytes: Long,
    val isUsb: Boolean
) {
    val usedBytes: Long get() = (totalBytes - freeBytes).coerceAtLeast(0L)
    val usedRatio: Float get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()) else 0f
    
    val formattedTotal: String get() = String.format("%.1f GB", totalBytes / (1024.0 * 1024.0 * 1024.0))
    val formattedFree: String get() = String.format("%.1f GB", freeBytes / (1024.0 * 1024.0 * 1024.0))
    val formattedUsed: String get() = String.format("%.1f GB", usedBytes / (1024.0 * 1024.0 * 1024.0))
}

data class RemoteClipEntry(
    val id: Long = 0,
    val text: String,
    val clientIp: String,
    val timestamp: Long = System.currentTimeMillis()
)
