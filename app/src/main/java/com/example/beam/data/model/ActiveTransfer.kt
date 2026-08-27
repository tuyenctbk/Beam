package com.example.beam.data.model

import java.util.Locale

data class ActiveTransfer(
    val id: String,
    val fileName: String,
    val isUpload: Boolean = true, // true = uploading to TV, false = downloading from TV
    val bytesTransferred: Long = 0L,
    val totalBytes: Long = 0L,
    val progress: Float = 0f, // 0.0f to 1.0f
    val speedBytesPerSec: Long = 0L,
    val clientIp: String = "Mobile Web",
    val status: TransferProgressStatus = TransferProgressStatus.TRANSFERRING
) {
    val percent: Int
        get() = (progress.coerceIn(0f, 1f) * 100).toInt()

    val formattedTransferred: String
        get() = formatBytes(bytesTransferred)

    val formattedTotal: String
        get() = if (totalBytes > 0) formatBytes(totalBytes) else "--"

    val formattedSpeed: String
        get() {
            if (speedBytesPerSec <= 0L) return "0.0 MB/s"
            val mbPerSec = speedBytesPerSec / (1024.0 * 1024.0)
            return if (mbPerSec >= 0.1) {
                String.format(Locale.US, "%.1f MB/s", mbPerSec)
            } else {
                val kbPerSec = speedBytesPerSec / 1024.0
                String.format(Locale.US, "%.1f KB/s", kbPerSec)
            }
        }

    val isLargeFile: Boolean
        get() = totalBytes >= 10 * 1024 * 1024L || totalBytes == 0L // 10MB or stream size

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
            return String.format(Locale.US, "%.1f %s", value, units[Math.min(digitGroups, units.size - 1)])
        }
    }
}

enum class TransferProgressStatus {
    TRANSFERRING,
    COMPLETED,
    FAILED
}
