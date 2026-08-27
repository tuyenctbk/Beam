package com.example.beam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.beam.data.model.FileCategory
import com.example.beam.data.model.FileItem
import com.example.beam.data.model.MediaMetadata
import com.example.beam.ui.theme.BeamFocusCyanBright
import com.example.beam.ui.theme.BeamGreen
import com.example.beam.ui.theme.BeamRose

@Composable
fun FileDetailModal(
    item: FileItem,
    sha256Hash: String?,
    metadata: MediaMetadata? = null,
    onDismiss: () -> Unit,
    onOpen: (FileItem) -> Unit,
    onDelete: (FileItem) -> Unit,
    onShareQr: ((FileItem) -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.confirm_delete_title),
            itemName = item.name,
            sizeFormatted = item.formattedSize,
            isDirectory = item.isDirectory,
            onConfirm = {
                showDeleteConfirm = false
                onDismiss()
                onDelete(item)
            },
            onDismiss = {
                showDeleteConfirm = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        val scrollState = rememberScrollState()
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(2.dp, BeamFocusCyanBright, RoundedCornerShape(22.dp))
                .testTag("file_detail_modal"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(22.dp)
            ) {
                // Large Preview Header for Images or Videos
                if (!item.isDirectory && (item.category == FileCategory.PHOTOS || item.category == FileCategory.VIDEOS)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .border(1.dp, BeamFocusCyanBright.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.category == FileCategory.PHOTOS) {
                            AsyncImage(
                                model = item.file,
                                contentDescription = item.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Video Preview Box
                            AsyncImage(
                                model = item.file,
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleFilled,
                                    contentDescription = "Play Video",
                                    tint = BeamFocusCyanBright,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }

                        // Quality / Resolution badge overlay
                        if (metadata?.hasResolution == true) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .border(1.dp, BeamFocusCyanBright, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = metadata.resolutionFormatted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BeamFocusCyanBright
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // File Name & Category Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(item.category.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.category.icon,
                            contentDescription = item.name,
                            tint = item.category.color,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(item.category.titleRes),
                            fontSize = 13.sp,
                            color = item.category.color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Metadata Details Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow(
                        label = stringResource(R.string.file_detail_size),
                        value = if (item.isDirectory) stringResource(R.string.items_count, item.itemCount) else item.formattedSize
                    )

                    if (metadata?.hasResolution == true) {
                        DetailRow(
                            label = stringResource(R.string.media_detail_resolution),
                            value = metadata.resolutionFormatted,
                            valueColor = BeamFocusCyanBright
                        )
                    }

                    if (metadata != null && metadata.bitrateBps > 0) {
                        DetailRow(
                            label = stringResource(R.string.media_detail_bitrate),
                            value = metadata.bitrateFormatted
                        )
                    }

                    if (metadata != null && metadata.durationMs > 0) {
                        DetailRow(
                            label = stringResource(R.string.media_detail_duration),
                            value = metadata.durationFormatted
                        )
                    }

                    DetailRow(
                        label = stringResource(R.string.file_detail_modified),
                        value = item.formattedDate
                    )

                    DetailRow(
                        label = stringResource(R.string.file_detail_path),
                        value = item.path,
                        isMonospace = true
                    )

                    if (!item.isDirectory) {
                        DetailRow(
                            label = stringResource(R.string.file_detail_sha256),
                            value = sha256Hash ?: stringResource(R.string.calculating_sha256),
                            isMonospace = true,
                            valueColor = BeamGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!item.isDirectory && onShareQr != null) {
                        TvFocusableCard(
                            onClick = { onShareQr(item) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            defaultBgColor = BeamFocusCyanBright.copy(alpha = 0.15f),
                            focusBorderColor = BeamFocusCyanBright,
                            testTag = "detail_share_qr_btn"
                        ) { isFocused ->
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = null,
                                    tint = BeamFocusCyanBright,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = stringResource(R.string.share_qr_btn),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BeamFocusCyanBright
                                )
                            }
                        }
                    }

                    TvFocusableCard(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        defaultBgColor = BeamRose.copy(alpha = 0.15f),
                        focusBorderColor = BeamRose,
                        testTag = "detail_delete_btn"
                    ) { isFocused ->
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = BeamRose,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(R.string.dialog_delete),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BeamRose
                            )
                        }
                    }

                    if (!item.isDirectory) {
                        TvFocusableCard(
                            onClick = {
                                onOpen(item)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            defaultBgColor = BeamFocusCyanBright,
                            focusBorderColor = Color.White,
                            testTag = "detail_open_btn"
                        ) { isFocused ->
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = stringResource(R.string.open_file_action),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            color = valueColor,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
    }
}
