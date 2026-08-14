package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.example.data.FileCategory
import com.example.data.FileItem
import com.example.ui.theme.BeamError
import com.example.ui.theme.BeamErrorBg
import com.example.ui.theme.BeamOnBackground
import com.example.ui.theme.BeamPrimary
import com.example.ui.theme.BeamPrimaryContainer
import com.example.ui.theme.BeamSecondary
import com.example.util.ApkParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FilePreviewModal(
    fileItem: FileItem?,
    onOpen: (FileItem) -> Unit,
    onDelete: (FileItem) -> Unit,
    onDismiss: () -> Unit
) {
    if (fileItem == null) return

    val context = LocalContext.current
    val formattedDate = remember(fileItem.lastModified) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(fileItem.lastModified))
    }

    val imageBitmap = remember(fileItem.path) {
        if (fileItem.category == FileCategory.PHOTOS && fileItem.file.exists() && fileItem.file.isFile) {
            try {
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeFile(fileItem.path, opts)
            } catch (_: Exception) { null }
        } else null
    }

    val apkDrawable = remember(fileItem.path) {
        if (fileItem.category == FileCategory.APKS) {
            ApkParser.getApkIcon(context, fileItem.file)
        } else null
    }

    val textContentSnippet = remember(fileItem.path) {
        if ((fileItem.category == FileCategory.DOCUMENTS || fileItem.extension in listOf("txt", "json", "xml", "csv", "log", "md")) &&
            fileItem.file.exists() && fileItem.file.isFile && fileItem.sizeBytes < 5 * 1024 * 1024
        ) {
            try {
                fileItem.file.bufferedReader().use { reader ->
                    val lines = mutableListOf<String>()
                    var count = 0
                    var line = reader.readLine()
                    while (line != null && count < 30) {
                        lines.add(line)
                        count++
                        line = reader.readLine()
                    }
                    if (lines.isEmpty()) "(Empty text file)" else lines.joinToString("\n")
                }
            } catch (e: Exception) {
                "Unable to preview file contents: ${e.localizedMessage}"
            }
        } else null
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                .padding(20.dp)
                .testTag("file_preview_dialog")
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Quick Preview",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BeamSecondary
                        )
                        Text(
                            text = fileItem.apkAppName ?: fileItem.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BeamOnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Preview",
                            tint = BeamSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 320.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        imageBitmap != null -> {
                            Image(
                                bitmap = imageBitmap.asImageBitmap(),
                                contentDescription = fileItem.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }

                        textContentSnippet != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = textContentSnippet,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        apkDrawable != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    bitmap = apkDrawable.toBitmap().asImageBitmap(),
                                    contentDescription = fileItem.apkAppName ?: fileItem.name,
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = fileItem.apkPackageName ?: "",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = BeamSecondary
                                )
                                Text(
                                    text = "Version: ${fileItem.apkVersion ?: "1.0"}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BeamPrimary
                                )
                            }
                        }

                        else -> {
                            val categoryIcon = when (fileItem.category) {
                                FileCategory.VIDEOS -> Icons.Default.Movie
                                FileCategory.PHOTOS -> Icons.Default.Image
                                FileCategory.MUSIC -> Icons.Default.AudioFile
                                FileCategory.ZIP -> Icons.Default.Unarchive
                                FileCategory.DOCUMENTS -> Icons.Default.Description
                                FileCategory.APKS -> Icons.Default.Android
                                else -> if (fileItem.isDirectory) Icons.Default.Folder else Icons.Default.Description
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(BeamPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = categoryIcon,
                                        contentDescription = null,
                                        tint = BeamPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = fileItem.category.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BeamOnBackground
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Metadata Details Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Size: ${fileItem.formattedSize}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BeamOnBackground
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = BeamSecondary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Open Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BeamPrimary)
                            .clickable {
                                onDismiss()
                                onOpen(fileItem)
                            }
                            .padding(vertical = 12.dp)
                            .testTag("preview_open_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Open / Execute",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Delete Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(BeamErrorBg)
                            .clickable {
                                onDismiss()
                                onDelete(fileItem)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("preview_delete_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete File",
                            tint = BeamError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
