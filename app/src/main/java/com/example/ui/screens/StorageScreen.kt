package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SdCardAlert
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.FileItem
import com.example.data.StorageVolumeInfo
import com.example.ui.components.TvRemoteShortcutsBar
import com.example.util.StorageUtils

@Composable
fun StorageScreen(
    storageVolumes: List<StorageVolumeInfo>,
    selectedIndex: Int,
    isLowStorage: Boolean = false,
    appCacheSize: Long = 0L,
    largeFiles: List<FileItem> = emptyList(),
    onSelectStorageIndex: (Int) -> Unit,
    onClearCache: () -> Unit = {},
    onDeleteLargeFile: (FileItem) -> Unit = {},
    onOpenFile: (FileItem) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.storage_locations_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = stringResource(R.string.storage_locations_desc),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Quality-of-Life Storage Suggestion Box (<10% storage capacity warning & recommendations)
            if (isLowStorage || appCacheSize > 5 * 1024 * 1024L) {
                item {
                    LowStorageSuggestionCard(
                        isCritical = isLowStorage,
                        cacheSize = appCacheSize,
                        largeFilesCount = largeFiles.size,
                        onClearCache = onClearCache
                    )
                }
            }

            // Storage Volumes Header
            item {
                Text(
                    text = "TARGET STORAGE DESTINATIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            itemsIndexed(storageVolumes) { index, volume ->
                val isSelected = index == selectedIndex
                var isFocused by remember { mutableStateOf(false) }

                val scale by animateFloatAsState(
                    targetValue = if (isFocused) 1.025f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f),
                    label = "storageCardScale"
                )

                val cardBg = when {
                    isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    isSelected -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.surface
                }

                val borderColor = when {
                    isFocused -> MaterialTheme.colorScheme.primary
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .shadow(elevation = if (isFocused) 10.dp else 2.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg)
                        .border(
                            width = if (isFocused || isSelected) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .onFocusChanged { isFocused = it.isFocused }
                        .focusable()
                        .clickable { onSelectStorageIndex(index) }
                        .padding(18.dp)
                        .testTag("storage_volume_$index")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (volume.isUsb) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (volume.isUsb) Icons.Default.SdCard else Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = if (volume.isUsb) Color(0xFFD97706) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = volume.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (volume.usedRatio > 0.90f) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFEF4444))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "<10% FREE",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.storage_free_of_total, volume.formattedFree, volume.formattedTotal),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected Target Storage",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Storage Usage Progress Bar
                        LinearProgressIndicator(
                            progress = { volume.usedRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (volume.usedRatio > 0.9f) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Path: ${volume.path}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Quick App Cache Cleanup Card (Always available)
            item {
                AppCacheMaintenanceCard(
                    cacheSizeBytes = appCacheSize,
                    onClearCache = onClearCache
                )
            }

            // Large Files Recommendation Section
            if (largeFiles.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.large_files_title).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }

                items(largeFiles) { fileItem ->
                    LargeFileCleanupRow(
                        fileItem = fileItem,
                        onOpenFile = { onOpenFile(fileItem) },
                        onDeleteFile = { onDeleteLargeFile(fileItem) }
                    )
                }
            }
        }

        // TV Remote Navigation Bar helper
        TvRemoteShortcutsBar()
    }
}

@Composable
fun LowStorageSuggestionCard(
    isCritical: Boolean,
    cacheSize: Long,
    largeFilesCount: Int,
    onClearCache: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f),
        label = "lowStorageScale"
    )

    val bgColor = if (isCritical) Color(0xFF2D1214) else Color(0xFF231F10)
    val borderColor = if (isFocused) Color.White else if (isCritical) Color(0xFFEF4444) else Color(0xFFF59E0B)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = if (isFocused) 12.dp else 4.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .padding(18.dp)
            .testTag("low_storage_suggestion_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isCritical) Color(0xFFEF4444) else Color(0xFFF59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCritical) Icons.Default.SdCardAlert else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isCritical) Color.White else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCritical) "⚠️ Internal Storage Critically Low (<10% Free)" else "💡 Storage Recommendation",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = if (isCritical)
                            "Free space has dropped below 10%. Delete large files or clear cache to prevent system slowness."
                        else
                            "Keep your TV storage lean by cleaning temporary files and reviewing unused downloads.",
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            // Action Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedCache = StorageUtils.formatBytes(cacheSize)
                CleanCacheButton(
                    label = "Clean Cache ($formattedCache)",
                    onClick = onClearCache
                )
            }
        }
    }
}

@Composable
fun AppCacheMaintenanceCard(
    cacheSizeBytes: Long,
    onClearCache: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f),
        label = "cacheCardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = if (isFocused) 8.dp else 2.dp, shape = RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(22.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .padding(16.dp)
            .testTag("app_cache_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.quick_clean_cache_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Current Temp Cache: ${StorageUtils.formatBytes(cacheSizeBytes)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            CleanCacheButton(
                label = "Clean Cache",
                onClick = onClearCache
            )
        }
    }
}

@Composable
fun CleanCacheButton(
    label: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "btnScale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = if (isFocused) 10.dp else 0.dp, shape = RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color.White else MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(14.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("clean_cache_btn"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CleaningServices,
                contentDescription = null,
                tint = if (isFocused) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFocused) Color.White else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun LargeFileCleanupRow(
    fileItem: FileItem,
    onOpenFile: () -> Unit,
    onDeleteFile: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.025f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f),
        label = "largeFileScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = if (isFocused) 8.dp else 1.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(18.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onOpenFile() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("large_file_${fileItem.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = fileItem.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Size: ${fileItem.formattedSize} • ${fileItem.formattedDate}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Quick Delete Button
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                var isDelFocused by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDelFocused) Color(0xFFEF4444) else Color(0xFFFEE2E2))
                        .border(
                            width = if (isDelFocused) 2.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .onFocusChanged { isDelFocused = it.isFocused }
                        .focusable()
                        .clickable { onDeleteFile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Large File",
                        tint = if (isDelFocused) Color.White else Color(0xFFDC2626),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
