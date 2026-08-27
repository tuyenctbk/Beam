package com.example.beam.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.beam.data.model.TransferEntity
import com.example.beam.data.model.TransferProgressStatus
import com.example.beam.ui.components.ActiveTransferCard
import com.example.beam.ui.components.BeamEmptyState
import com.example.beam.ui.components.EmptyStateType
import com.example.beam.ui.components.LowPowerWarningCard
import com.example.beam.ui.components.TvFocusableCard
import com.example.beam.ui.theme.BeamFocusCyan
import com.example.beam.ui.theme.BeamFocusCyanBright
import com.example.beam.ui.theme.BeamGreen
import com.example.beam.ui.theme.BeamNeonBlue
import com.example.beam.ui.theme.BeamRose
import com.example.beam.ui.viewmodel.MainViewModel

@Composable
fun WebBeamScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val serverUrl by viewModel.serverUrl.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()
    val isServerRunning by viewModel.isServerRunning.collectAsState()
    val transfers by viewModel.transfers.collectAsState()
    val activeTransfers by viewModel.activeTransfers.collectAsState()
    val batteryState by viewModel.batteryState.collectAsState()
    val isLowPowerWarningDismissed by viewModel.isLowPowerWarningDismissed.collectAsState()

    val activeLargeTransfer = activeTransfers.firstOrNull { it.status == TransferProgressStatus.TRANSFERRING && it.isLargeFile }
        ?: activeTransfers.firstOrNull { it.status == TransferProgressStatus.TRANSFERRING }
    val showLowPowerWarning = batteryState.isLowBattery && activeLargeTransfer != null && !isLowPowerWarningDismissed

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ================= LEFT COLUMN: QR Code Portal & Step Guide =================
        Column(
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // QR Code Main Portal Card
            TvFocusableCard(
                onClick = {
                    if (!isServerRunning) {
                        viewModel.startBeamServer()
                    } else {
                        viewModel.openShareQrDialog(serverUrl)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                focusBorderColor = BeamFocusCyan,
                testTag = "web_beam_qr_portal_card"
            ) { isFocused ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: Server Status Badge & Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isServerRunning) BeamGreen.copy(alpha = pulseAlpha) else BeamRose
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isServerRunning) stringResource(R.string.server_active_badge) else stringResource(R.string.server_offline_badge),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isServerRunning) BeamGreen else BeamRose,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Toggle server switch
                        TvFocusableCard(
                            onClick = {
                                if (isServerRunning) viewModel.stopBeamServer() else viewModel.startBeamServer()
                            },
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            defaultBgColor = if (isServerRunning) BeamRose.copy(alpha = 0.15f) else BeamGreen.copy(alpha = 0.15f),
                            focusBorderColor = if (isServerRunning) BeamRose else BeamGreen,
                            testTag = "toggle_server_btn"
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = if (isServerRunning) BeamRose else BeamGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isServerRunning) "Stop" else "Start",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isServerRunning) BeamRose else BeamGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // QR Code Frame with High Contrast White Backplate
                    if (isServerRunning && qrCodeBitmap != null) {
                        Box(
                            modifier = Modifier
                                .size(175.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White)
                                .border(3.dp, BeamFocusCyan, RoundedCornerShape(18.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = qrCodeBitmap!!.asImageBitmap(),
                                contentDescription = "Scan QR to connect",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(175.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.server_click_to_start),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // URL Display Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isServerRunning && serverUrl.isNotBlank()) serverUrl else "http://[TV_IP]:8080",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isServerRunning) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick 3-Step Illustrated Instructions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.how_to_beam_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    InstructionRow(
                        step = "1",
                        title = stringResource(R.string.beam_step_1_title),
                        desc = stringResource(R.string.beam_step_1_desc),
                        color = BeamFocusCyanBright
                    )

                    InstructionRow(
                        step = "2",
                        title = stringResource(R.string.beam_step_2_title),
                        desc = stringResource(R.string.beam_step_2_desc),
                        color = BeamNeonBlue
                    )

                    InstructionRow(
                        step = "3",
                        title = stringResource(R.string.beam_step_3_title),
                        desc = stringResource(R.string.beam_step_3_desc),
                        color = BeamGreen
                    )
                }
            }
        }

        // ================= RIGHT COLUMN: Live Transfers & History Feed =================
        Column(
            modifier = Modifier
                .weight(1.15f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Real-time Active Progress Indicators (Determinate Linear Progress Bars)
            AnimatedVisibility(
                visible = activeTransfers.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BeamFocusCyanBright
                    )
                    activeTransfers.forEach { transfer ->
                        ActiveTransferCard(
                            transfer = transfer,
                            onDismiss = { viewModel.dismissActiveTransfer(transfer) }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Live Feed Header with Clear Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = BeamFocusCyanBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.active_transfers_title),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (transfers.isNotEmpty()) {
                    TvFocusableCard(
                        onClick = { viewModel.clearHistory() },
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        defaultBgColor = BeamRose.copy(alpha = 0.15f),
                        focusBorderColor = BeamRose,
                        testTag = "clear_all_transfers_btn"
                    ) { isFocused ->
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = if (isFocused) Color.White else BeamRose,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
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

            // Transfer List or Empty State
            if (transfers.isEmpty() && activeTransfers.isEmpty()) {
                BeamEmptyState(
                    type = EmptyStateType.HISTORY_EMPTY,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(transfers, key = { it.id }) { transfer ->
                        TvFocusableCard(
                            onClick = { viewModel.openTransferItem(transfer, context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            testTag = "transfer_item_${transfer.id}"
                        ) { isFocused ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (transfer.isClipboard) BeamNeonBlue.copy(alpha = 0.2f)
                                                else BeamGreen.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (transfer.isClipboard) Icons.Default.ContentCopy else Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = if (transfer.isClipboard) BeamFocusCyanBright else BeamGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = transfer.fileName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${transfer.formattedDate}  •  ${transfer.clientIp}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!transfer.isClipboard) {
                                        Text(
                                            text = transfer.formatBytes(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BeamFocusCyanBright
                                        )
                                    }

                                    TvFocusableCard(
                                        onClick = { viewModel.deleteTransfer(transfer) },
                                        modifier = Modifier.size(32.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        focusBorderColor = BeamRose,
                                        testTag = "delete_transfer_${transfer.id}"
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete transfer",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
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
    }
}

@Composable
private fun InstructionRow(
    step: String,
    title: String,
    desc: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
