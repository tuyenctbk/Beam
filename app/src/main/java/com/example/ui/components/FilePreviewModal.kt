package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.FileCategory
import com.example.data.FileItem
import com.example.util.ApkParser
import com.example.util.ChecksumUtils
import com.example.util.FileOpener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    val apkDrawable = remember(fileItem.path) {
        if (fileItem.category == FileCategory.APKS) {
            ApkParser.getApkIcon(context, fileItem.file)
        } else null
    }

    val isImageFile = remember(fileItem.path) {
        fileItem.category == FileCategory.PHOTOS && fileItem.file.exists() && fileItem.file.isFile
    }

    // Async checksum calculation for detail view
    val sha256Checksum by produceState(initialValue = "Calculating SHA-256...", key1 = fileItem.path) {
        value = withContext(Dispatchers.IO) {
            if (fileItem.file.exists() && fileItem.file.isFile) {
                ChecksumUtils.calculateSha256(fileItem.file)
            } else {
                "N/A"
            }
        }
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
                            text = stringResource(R.string.quick_preview),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = fileItem.apkAppName ?: fileItem.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    var isCloseFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isCloseFocused) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF1F5F9))
                            .border(if (isCloseFocused) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .onFocusChanged { isCloseFocused = it.isFocused }
                            .focusable()
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Preview",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 280.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isImageFile -> {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(fileItem.file)
                                    .crossfade(true)
                                    .build(),
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
                                    modifier = Modifier.size(68.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = fileItem.apkPackageName ?: "",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = stringResource(R.string.version_label, fileItem.apkVersion ?: "1.0"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
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
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = categoryIcon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(fileItem.category.labelResId),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata Details Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.sort_size)}: ${fileItem.formattedSize}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Checksum Verification Bar (SHA-256 Integrity status)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Integrity Verified",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.sha256_verified_title),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = sha256Checksum,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF15803D),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        var isCopyFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCopyFocused) Color(0xFF16A34A) else Color(0xFFDCFCE7))
                                .border(if (isCopyFocused) 1.5.dp else 0.dp, Color.White, RoundedCornerShape(8.dp))
                                .onFocusChanged { isCopyFocused = it.isFocused }
                                .focusable()
                                .clickable {
                                    FileOpener.copyToClipboard(context, sha256Checksum)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Copy Hash",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCopyFocused) Color.White else Color(0xFF166534)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row with TV focus animations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var isOpenFocused by remember { mutableStateOf(false) }
                    val openScale by animateFloatAsState(
                        targetValue = if (isOpenFocused) 1.04f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                        label = "openBtnScale"
                    )

                    // Open Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                scaleX = openScale
                                scaleY = openScale
                            }
                            .shadow(if (isOpenFocused) 8.dp else 0.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .border(
                                width = if (isOpenFocused) 2.dp else 0.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .onFocusChanged { isOpenFocused = it.isFocused }
                            .focusable()
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
                                text = stringResource(R.string.open_execute),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Delete Button
                    var isDeleteFocused by remember { mutableStateOf(false) }
                    val delScale by animateFloatAsState(
                        targetValue = if (isDeleteFocused) 1.04f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                        label = "delBtnScale"
                    )

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = delScale
                                scaleY = delScale
                            }
                            .shadow(if (isDeleteFocused) 8.dp else 0.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFEE2E2))
                            .border(
                                width = if (isDeleteFocused) 2.dp else 0.dp,
                                color = Color(0xFFEF4444),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .onFocusChanged { isDeleteFocused = it.isFocused }
                            .focusable()
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
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
