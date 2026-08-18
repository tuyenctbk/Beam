package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ActiveTransfer
import com.example.data.FileCategory
import com.example.data.FileItem
import com.example.data.StorageVolumeInfo
import com.example.ui.components.NavTab
import com.example.ui.components.QrCardSection
import com.example.ui.components.TvRemoteShortcutsBar
import com.example.ui.theme.BeamOnBackground
import com.example.ui.theme.BeamPrimary
import com.example.ui.theme.BeamPrimaryContainer
import com.example.ui.theme.BeamSecondary
import com.example.util.DiscoveredDevice

@Composable
fun TransferScreen(
    wifiSsid: String,
    ipAddress: String,
    port: Int,
    qrBitmap: ImageBitmap?,
    activeTransfers: Map<String, ActiveTransfer>,
    recentFiles: List<FileItem> = emptyList(),
    discoveredDevices: List<DiscoveredDevice> = emptyList(),
    storageVolumes: List<StorageVolumeInfo> = emptyList(),
    isLocalNetworkAvailable: Boolean = true,
    onTabSelected: (NavTab) -> Unit,
    onOpenFile: (FileItem) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Active Transfer Progress with Real-Time Speed Indicator (MB/s)
        if (activeTransfers.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                activeTransfers.values.forEach { transfer ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                            .testTag("active_transfer_card")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${stringResource(R.string.incoming_transfer)}: ${transfer.fileName}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "⚡ ${transfer.formattedSpeed}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = transfer.formattedSizeProgress,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { transfer.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Storage Usage Quick Bar
        if (storageVolumes.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .clickable { onTabSelected(NavTab.STORAGE) }
                    .padding(14.dp)
                    .testTag("storage_quick_bar")
            ) {
                storageVolumes.forEachIndexed { idx, volume ->
                    if (idx > 0) Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (volume.isUsb) Icons.Default.SdCard else Icons.Default.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = volume.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${volume.formattedUsed} / ${volume.formattedTotal} (${(volume.usedRatio * 100).toInt()}% used)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { volume.usedRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (volume.usedRatio > 0.9f) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Centerpiece QR Section
        QrCardSection(
            wifiSsid = wifiSsid,
            ipAddress = ipAddress,
            port = port,
            qrBitmap = qrBitmap,
            isNetworkAvailable = isLocalNetworkAvailable
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Recent Section (5 Most Recently Accessed or Transferred Files)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                .padding(18.dp)
                .testTag("recent_files_section")
        ) {
            // Header with title, count badge, and View All quick navigation button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.recent_section_title),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (recentFiles.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${recentFiles.take(5).size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Text(
                            text = stringResource(R.string.recent_section_desc),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Quick navigation to Files explorer
                var isNavFocused by remember { mutableStateOf(false) }
                val navScale by animateFloatAsState(
                    targetValue = if (isNavFocused) 1.05f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
                    label = "navScale"
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = navScale
                            scaleY = navScale
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isNavFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .border(1.dp, if (isNavFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .onFocusChanged { isNavFocused = it.isFocused }
                        .focusable()
                        .clickable { onTabSelected(NavTab.FILES) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("recent_view_all_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.view_all),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNavFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = if (isNavFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (recentFiles.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentFiles.take(5).forEachIndexed { idx, item ->
                        RecentFileCardItem(
                            item = item,
                            index = idx,
                            onOpenFile = onOpenFile
                        )
                    }
                }
            } else {
                // Empty state when no files accessed or transferred yet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_recent_files),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.no_recent_files_desc),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Discovered Devices Section (NSD Network Service Discovery)
        if (discoveredDevices.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                    .padding(16.dp)
                    .testTag("discovered_devices_section")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CastConnected,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.devices_on_network),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                discoveredDevices.forEachIndexed { idx, device ->
                    if (idx > 0) Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF0FDF4))
                            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = device.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = "${device.hostIp}:${device.port}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.online),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Quick Launch Grid Cards with D-Pad focus feedback
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickCategoryCard(
                title = stringResource(R.string.nav_files),
                icon = Icons.Default.Folder,
                onClick = { onTabSelected(NavTab.FILES) },
                modifier = Modifier.weight(1f)
            )
            QuickCategoryCard(
                title = stringResource(R.string.nav_apks),
                icon = Icons.Default.Android,
                onClick = { onTabSelected(NavTab.APKS) },
                modifier = Modifier.weight(1f)
            )
            QuickCategoryCard(
                title = stringResource(R.string.nav_remote),
                icon = Icons.Default.Keyboard,
                onClick = { onTabSelected(NavTab.REMOTE) },
                modifier = Modifier.weight(1f)
            )
        }

        // TV Remote Navigation Bar helper
        TvRemoteShortcutsBar()
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun QuickCategoryCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f),
        label = "cardScale"
    )

    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val bgColor = if (isFocused) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = if (isFocused) 8.dp else 2.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(if (isFocused) 2.dp else 1.dp, borderColor, RoundedCornerShape(24.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("quick_card_${title.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isFocused) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun RecentFileCardItem(
    item: FileItem,
    index: Int,
    onOpenFile: (FileItem) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "recentItemScale"
    )

    val isApk = item.extension.equals("apk", ignoreCase = true)

    val (categoryIcon, iconTint, iconBg) = when (item.category) {
        FileCategory.APKS -> Triple(Icons.Default.Android, Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f))
        FileCategory.VIDEOS -> Triple(Icons.Default.Movie, Color(0xFF8B5CF6), Color(0xFF8B5CF6).copy(alpha = 0.15f))
        FileCategory.PHOTOS -> Triple(Icons.Default.Image, Color(0xFFEC4899), Color(0xFFEC4899).copy(alpha = 0.15f))
        FileCategory.MUSIC -> Triple(Icons.Default.AudioFile, Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.15f))
        FileCategory.DOCUMENTS -> Triple(Icons.Default.Description, Color(0xFF3B82F6), Color(0xFF3B82F6).copy(alpha = 0.15f))
        FileCategory.ZIP -> Triple(Icons.Default.Unarchive, Color(0xFFF97316), Color(0xFFF97316).copy(alpha = 0.15f))
        else -> Triple(Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
    }

    val cardBg = if (isFocused) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    }

    val borderStroke = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(if (isFocused) 1.5.dp else 1.dp, borderStroke, RoundedCornerShape(16.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onOpenFile(item) }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("recent_file_item_$index"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.apkAppName ?: item.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(item.category.labelResId),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = iconTint
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = item.formattedSize,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer)
                .clickable { onOpenFile(item) }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isApk) stringResource(R.string.install_package) else stringResource(R.string.open),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = if (isFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}


