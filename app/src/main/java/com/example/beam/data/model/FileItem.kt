package com.example.beam.data.model

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val isDirectory: Boolean = file.isDirectory,
    val sizeBytes: Long = if (file.isDirectory) 0L else file.length(),
    val lastModified: Long = file.lastModified(),
    val isSelected: Boolean = false
) {
    val extension: String
        get() = if (isDirectory) "" else file.extension.lowercase()

    val category: FileCategory
        get() = if (isDirectory) FileCategory.ALL else FileCategory.fromFileExtension(extension)

    val itemCount: Int
        get() = if (isDirectory) (file.listFiles()?.size ?: 0) else 0

    val formattedSize: String
        get() {
            if (isDirectory) return ""
            if (sizeBytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
            val value = sizeBytes / Math.pow(1024.0, digitGroups.toDouble())
            return String.format(Locale.US, "%.1f %s", value, units[Math.min(digitGroups, units.size - 1)])
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }
}
