package com.example.beam.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.beam.data.model.FileCategory
import com.example.beam.data.model.TransferProgressStatus
import com.example.beam.ui.components.ActiveTransferCard
import com.example.beam.ui.components.BeamEmptyState
import com.example.beam.ui.components.EmptyStateType
import com.example.beam.ui.components.LowPowerWarningCard
import com.example.beam.ui.components.StorageIndicator
import com.example.beam.ui.components.TvFocusableCard
import com.example.beam.ui.components.TvTab
import com.example.beam.ui.theme.BeamFocusCyan
import com.example.beam.ui.theme.BeamFocusCyanBright
import com.example.beam.ui.theme.BeamGreen
import com.example.beam.ui.theme.BeamNeonBlue
import com.example.beam.ui.theme.BeamPurple
import com.example.beam.ui.theme.BeamRose
import com.example.beam.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToCategory: (FileCategory) -> Unit,
    onNavigateToTab: (TvTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val storageInfo by viewModel.storageInfo.collectAsState()
    val transfers by viewModel.transfers.collectAsState()
    val activeTransfers by viewModel.activeTransfers.collectAsState()
    val batteryState by viewModel.batteryState.collectAsState()
    val isLowPowerWarningDismissed by viewModel.isLowPowerWarningDismissed.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val isServerRunning by viewModel.isServerRunning.collectAsState()

    val activeLargeTransfer = activeTransfers.firstOrNull { it.status == TransferProgressStatus.TRANSFERRING && it.isLargeFile }
        ?: activeTransfers.firstOrNull { it.status == TransferProgressStatus.TRANSFERRING }
    val showLowPowerWarning = batteryState.isLowBattery && activeLargeTransfer != null && !isLowPowerWarningDismissed

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isCompact = screenWidth < 600.dp
        val isTablet = screenWidth in 600.dp..900.dp
        val horizontalPadding = if (isCompact) 16.dp else if (isTablet) 20.dp else 24.dp
        val categoryColumns = if (isCompact) 2 else if (isTablet) 3 else 4

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 36.dp)
        ) {
            // Active Real-Time Transfer Progress Indicators (if any active upload/download)
            if (activeTransfers.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (showLowPowerWarning && activeLargeTransfer != null) {
                            LowPowerWarningCard(
                                batteryState = batteryState,
                                largeTransfer = activeLargeTransfer,
                                onDismiss = { viewModel.dismissLowPowerWarning() }
                            )
                        }

                        Text(
                            text = stringResource(R.string.active_transfers_header),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BeamFocusCyanBright
                        )
                        activeTransfers.forEach { transfer ->
                            ActiveTransferCard(
                                transfer = transfer,
                                onDismiss = { viewModel.dismissActiveTransfer(transfer) }
                            )
                        }
                    }
                }
            }

            // 1. Hero Cyber Action Card (Web Beam Quick Connect)
            item {
                TvFocusableCard(
                    onClick = { onNavigateToTab(TvTab.WEB_BEAM) },
                    modifier = Modifier.fillMaxWidth(),
                    focusBorderColor = BeamFocusCyan,
                    testTag = "dashboard_hero_beam_banner"
                ) { isFocused ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Gradient background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            BeamFocusCyan.copy(alpha = if (isFocused) 0.22f else 0.12f),
                                            BeamNeonBlue.copy(alpha = if (isFocused) 0.15f else 0.06f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = if (isCompact) 14.dp else 20.dp, vertical = if (isCompact) 14.dp else 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isCompact) 44.dp else 52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(BeamFocusCyan, BeamNeonBlue)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(if (isCompact) 24.dp else 28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(if (isCompact) 12.dp else 16.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(R.string.transfer_title),
                                            fontSize = if (isCompact) 16.sp else 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(BeamGreen.copy(alpha = 0.2f))
                                                .border(1.dp, BeamGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isServerRunning) "ACTIVE" else "READY",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BeamGreen
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (serverUrl.isNotBlank()) "Scan QR or visit: $serverUrl" else stringResource(R.string.transfer_subtitle),
                                        fontSize = if (isCompact) 11.sp else 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // CTA Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isFocused) BeamFocusCyan else BeamFocusCyan.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = if (isCompact) 10.dp else 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RocketLaunch,
                                    contentDescription = null,
                                    tint = if (isFocused) Color.Black else BeamFocusCyanBright,
                                    modifier = Modifier.size(14.dp)
                                )
                                if (!isCompact) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.open_web_beam),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFocused) Color.Black else BeamFocusCyanBright
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = if (isFocused) Color.Black else BeamFocusCyanBright,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Storage Overview Indicator Card
            item {
                StorageIndicator(
                    storageInfo = storageInfo,
                    onCleanCacheClick = { viewModel.cleanCache() }
                )
            }

            // 3. Category Grid Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_title),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.dashboard_subtitle),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TvFocusableCard(
                        onClick = { onNavigateToTab(TvTab.EXPLORER) },
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        defaultBgColor = Color.Transparent,
                        focusedBgColor = BeamFocusCyan.copy(alpha = 0.15f),
                        testTag = "dashboard_view_all_files_btn"
                    ) { isFocused ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.nav_explorer),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFocused) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = if (isFocused) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // 4. Categories Adaptive Grid
            val categories = listOf(
                FileCategory.PHOTOS,
                FileCategory.VIDEOS,
                FileCategory.MUSIC,
                FileCategory.DOCUMENTS,
                FileCategory.APKS,
                FileCategory.ZIP,
                FileCategory.DOWNLOADS,
                FileCategory.ALL
            )

            val chunkedCategories = categories.chunked(categoryColumns)
            chunkedCategories.forEach { rowCategories ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCategories.forEach { category ->
                            val categoryBytes = storageInfo.categorySizes[category] ?: 0L
                            val formattedCategorySize = if (category == FileCategory.ALL) {
                                storageInfo.formatBytes(storageInfo.usedBytes)
                            } else {
                                storageInfo.formatBytes(categoryBytes)
                            }

                            TvFocusableCard(
                                onClick = { onNavigateToCategory(category) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (isCompact) 74.dp else 80.dp),
                                focusBorderColor = category.color,
                                testTag = "category_card_${category.name.lowercase()}"
                            ) { isFocused ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(if (isCompact) 10.dp else 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isCompact) 36.dp else 40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(category.color.copy(alpha = if (isFocused) 0.3f else 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = stringResource(category.titleRes),
                                            tint = category.color,
                                            modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Text(
                                            text = stringResource(category.titleRes),
                                            fontSize = if (isCompact) 13.sp else 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (category == FileCategory.ALL) stringResource(R.string.cat_all) else formattedCategorySize,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Fill in remaining empty space in the last row if needed
                        val emptySlots = categoryColumns - rowCategories.size
                        repeat(emptySlots) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 5. Recently Received Items Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = BeamFocusCyanBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.recently_received_files),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (transfers.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.items_count, transfers.size),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            TvFocusableCard(
                                onClick = { viewModel.clearHistory() },
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                defaultBgColor = BeamRose.copy(alpha = 0.15f),
                                focusBorderColor = BeamRose,
                                testTag = "dashboard_clear_history_btn"
                            ) { isFocused ->
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CleaningServices,
                                        contentDescription = null,
                                        tint = if (isFocused) Color.White else BeamRose,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.clear_history),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFocused) Color.White else BeamRose
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Recent Transfers List or Empty State
            if (transfers.isEmpty()) {
                item {
                    BeamEmptyState(
                        type = EmptyStateType.HISTORY_EMPTY,
                        onPrimaryAction = { onNavigateToTab(TvTab.WEB_BEAM) }
                    )
                }
            } else {
                items(transfers.take(6), key = { it.id }) { transfer ->
                    TvFocusableCard(
                        onClick = { viewModel.openTransferItem(transfer, context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        testTag = "recent_transfer_${transfer.id}"
                    ) { isFocused ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (transfer.isClipboard) BeamPurple.copy(alpha = 0.2f) else BeamGreen.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (transfer.isClipboard) Icons.Default.ContentCopy else Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = if (transfer.isClipboard) BeamPurple else BeamGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (transfer.isClipboard) (transfer.clipboardText ?: "Text clip") else transfer.fileName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${transfer.formattedDate} • ${transfer.clientIp}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!transfer.isClipboard) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BeamFocusCyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = transfer.formatBytes(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BeamFocusCyanBright
                                        )
                                    }
                                }

                                // Quick Open Action Icon
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isFocused) BeamFocusCyanBright else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = "Open",
                                        tint = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
