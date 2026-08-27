package com.example.beam.data.model

import java.io.File
import java.util.Locale

data class CleanupScanResult(
    val isScanning: Boolean = false,
    val totalJunkBytes: Long = 0L,
    val junkFiles: List<File> = emptyList(),
    val appCacheBytes: Long = 0L,
    val tempLogBytes: Long = 0L,
    val staleDownloadBytes: Long = 0L,
    val scanCompleted: Boolean = false
) {
    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[Math.min(digitGroups, units.size - 1)])
    }

    val formattedTotal: String get() = formatBytes(totalJunkBytes)
    val formattedCache: String get() = formatBytes(appCacheBytes)
    val formattedTempLogs: String get() = formatBytes(tempLogBytes)
    val formattedStaleDownloads: String get() = formatBytes(staleDownloadBytes)
}
