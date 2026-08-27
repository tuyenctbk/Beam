package com.example.beam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED", // COMPLETED, FAILED, IN_PROGRESS
    val clientIp: String = "Mobile Web",
    val isClipboard: Boolean = false,
    val clipboardText: String? = null
) {
    fun formatBytes(): String {
        if (sizeBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
        val value = sizeBytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[Math.min(digitGroups, units.size - 1)])
    }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("HH:mm - MMM dd", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
