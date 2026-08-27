package com.example.beam.data.model

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.util.Locale

data class MediaMetadata(
    val width: Int = 0,
    val height: Int = 0,
    val durationMs: Long = 0L,
    val bitrateBps: Long = 0L,
    val mimeType: String? = null
) {
    val hasResolution: Boolean
        get() = width > 0 && height > 0

    val resolutionFormatted: String
        get() {
            if (!hasResolution) return "Unknown"
            val qualityLabel = when {
                width >= 3840 || height >= 2160 -> " (4K UHD)"
                width >= 2560 || height >= 1440 -> " (2K QHD)"
                width >= 1920 || height >= 1080 -> " (1080p FHD)"
                width >= 1280 || height >= 720 -> " (720p HD)"
                else -> ""
            }
            return "${width} x ${height}$qualityLabel"
        }

    val bitrateFormatted: String
        get() {
            if (bitrateBps <= 0) return "N/A"
            val mbps = bitrateBps / 1_000_000.0
            return if (mbps >= 0.1) {
                String.format(Locale.US, "%.1f Mbps", mbps)
            } else {
                val kbps = bitrateBps / 1_000.0
                String.format(Locale.US, "%.0f Kbps", kbps)
            }
        }

    val durationFormatted: String
        get() {
            if (durationMs <= 0) return "N/A"
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            val hours = (durationMs / (1000 * 60 * 60))
            return if (hours > 0) {
                String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }
}

object MediaMetadataUtils {
    fun extractMetadata(context: Context, file: File, category: FileCategory): MediaMetadata {
        if (!file.exists() || file.isDirectory) return MediaMetadata()

        return try {
            when (category) {
                FileCategory.PHOTOS -> {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, options)
                    MediaMetadata(
                        width = options.outWidth,
                        height = options.outHeight,
                        mimeType = options.outMimeType
                    )
                }
                FileCategory.VIDEOS, FileCategory.MUSIC -> {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(file.absolutePath)
                        val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                        val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                        val mimeStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)

                        MediaMetadata(
                            width = widthStr?.toIntOrNull() ?: 0,
                            height = heightStr?.toIntOrNull() ?: 0,
                            durationMs = durationStr?.toLongOrNull() ?: 0L,
                            bitrateBps = bitrateStr?.toLongOrNull() ?: 0L,
                            mimeType = mimeStr
                        )
                    } finally {
                        try {
                            retriever.release()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                else -> MediaMetadata()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MediaMetadata()
        }
    }
}
