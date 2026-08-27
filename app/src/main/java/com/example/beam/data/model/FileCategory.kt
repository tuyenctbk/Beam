package com.example.beam.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

enum class FileCategory(
    val titleRes: Int,
    val icon: ImageVector,
    val color: Color,
    val extensions: Set<String>
) {
    ALL(
        titleRes = R.string.cat_all,
        icon = Icons.Default.Folder,
        color = Color(0xFF00F2FE),
        extensions = emptySet()
    ),
    PHOTOS(
        titleRes = R.string.cat_photos,
        icon = Icons.Default.Image,
        color = Color(0xFFFF4081),
        extensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic")
    ),
    VIDEOS(
        titleRes = R.string.cat_videos,
        icon = Icons.Default.Movie,
        color = Color(0xFF7C4DFF),
        extensions = setOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "ts")
    ),
    MUSIC(
        titleRes = R.string.cat_music,
        icon = Icons.Default.AudioFile,
        color = Color(0xFF00E676),
        extensions = setOf("mp3", "flac", "wav", "aac", "ogg", "m4a")
    ),
    DOCUMENTS(
        titleRes = R.string.cat_documents,
        icon = Icons.Default.Description,
        color = Color(0xFFFFB300),
        extensions = setOf("pdf", "txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx")
    ),
    APKS(
        titleRes = R.string.cat_apks,
        icon = Icons.Default.Android,
        color = Color(0xFF00E5FF),
        extensions = setOf("apk", "xapk", "apks")
    ),
    ZIP(
        titleRes = R.string.cat_zip,
        icon = Icons.Default.FolderZip,
        color = Color(0xFFFFAB40),
        extensions = setOf("zip", "rar", "7z", "tar", "gz")
    ),
    DOWNLOADS(
        titleRes = R.string.cat_downloads,
        icon = Icons.Default.Download,
        color = Color(0xFFE040FB),
        extensions = emptySet()
    );

    companion object {
        fun fromFileExtension(ext: String): FileCategory {
            val lower = ext.lowercase()
            return entries.firstOrNull { it != ALL && it != DOWNLOADS && lower in it.extensions } ?: DOCUMENTS
        }
    }
}
