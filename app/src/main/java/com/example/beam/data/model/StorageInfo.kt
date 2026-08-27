package com.example.beam.data.model

import java.util.Locale

data class StorageInfo(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = totalBytes - freeBytes,
    val categorySizes: Map<FileCategory, Long> = emptyMap(),
    val storagePath: String = "/storage/emulated/0"
) {
    val freePercentage: Int
        get() = if (totalBytes > 0) ((freeBytes.toDouble() / totalBytes) * 100).toInt() else 0

    val usedPercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()) else 0f

    val isLowStorage: Boolean
        get() = freePercentage < 10

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[Math.min(digitGroups, units.size - 1)])
    }

    val formattedTotal: String get() = formatBytes(totalBytes)
    val formattedUsed: String get() = formatBytes(usedBytes)
    val formattedFree: String get() = formatBytes(freeBytes)
}
