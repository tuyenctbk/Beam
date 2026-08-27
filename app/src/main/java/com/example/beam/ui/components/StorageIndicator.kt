package com.example.beam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.beam.data.model.FileCategory
import com.example.beam.data.model.StorageInfo
import com.example.beam.ui.theme.BeamAmber
import com.example.beam.ui.theme.BeamFocusCyanBright

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StorageIndicator(
    storageInfo: StorageInfo,
    modifier: Modifier = Modifier,
    onCleanCacheClick: (() -> Unit)? = null
) {
    TvFocusableCard(
        onClick = { onCleanCacheClick?.invoke() },
        modifier = modifier.fillMaxWidth(),
        testTag = "storage_indicator_card"
    ) { isFocused ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = stringResource(R.string.storage_indicator_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(
                            R.string.storage_used_vs_total,
                            storageInfo.formattedUsed,
                            storageInfo.formattedTotal,
                            storageInfo.freePercentage
                        ),
                        fontSize = 13.sp,
                        color = BeamFocusCyanBright,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (storageInfo.isLowStorage) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BeamAmber.copy(alpha = 0.2f))
                            .border(1.dp, BeamAmber, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = stringResource(R.string.low_storage_warning_title),
                            tint = BeamAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.low_storage_badge),
                            color = BeamAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-segment storage bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.fillMaxHeight()) {
                    val photosPct = (storageInfo.categorySizes[FileCategory.PHOTOS] ?: 0L).toFloat() / storageInfo.totalBytes.coerceAtLeast(1L)
                    val videosPct = (storageInfo.categorySizes[FileCategory.VIDEOS] ?: 0L).toFloat() / storageInfo.totalBytes.coerceAtLeast(1L)
                    val musicPct = (storageInfo.categorySizes[FileCategory.MUSIC] ?: 0L).toFloat() / storageInfo.totalBytes.coerceAtLeast(1L)
                    val docsPct = (storageInfo.categorySizes[FileCategory.DOCUMENTS] ?: 0L).toFloat() / storageInfo.totalBytes.coerceAtLeast(1L)
                    val apksPct = (storageInfo.categorySizes[FileCategory.APKS] ?: 0L).toFloat() / storageInfo.totalBytes.coerceAtLeast(1L)

                    val usedSoFarPct = photosPct + videosPct + musicPct + docsPct + apksPct
                    val systemAndAppsPct = (storageInfo.usedPercentage - usedSoFarPct).coerceAtLeast(0.05f)

                    if (photosPct > 0) SegmentBar(weight = photosPct, color = FileCategory.PHOTOS.color)
                    if (videosPct > 0) SegmentBar(weight = videosPct, color = FileCategory.VIDEOS.color)
                    if (musicPct > 0) SegmentBar(weight = musicPct, color = FileCategory.MUSIC.color)
                    if (docsPct > 0) SegmentBar(weight = docsPct, color = FileCategory.DOCUMENTS.color)
                    if (apksPct > 0) SegmentBar(weight = apksPct, color = FileCategory.APKS.color)
                    SegmentBar(weight = systemAndAppsPct, color = MaterialTheme.colorScheme.outlineVariant)
                    val freePct = (storageInfo.freePercentage.toFloat() / 100f).coerceAtLeast(0.01f)
                    SegmentBar(weight = freePct, color = Color.Transparent)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Adaptive Legend FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LegendItem(label = stringResource(R.string.legend_photos), color = FileCategory.PHOTOS.color)
                LegendItem(label = stringResource(R.string.legend_videos), color = FileCategory.VIDEOS.color)
                LegendItem(label = stringResource(R.string.legend_audio), color = FileCategory.MUSIC.color)
                LegendItem(label = stringResource(R.string.legend_docs), color = FileCategory.DOCUMENTS.color)
                LegendItem(label = stringResource(R.string.legend_apks), color = FileCategory.APKS.color)
                LegendItem(label = stringResource(R.string.legend_system_apps), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                LegendItem(label = stringResource(R.string.legend_free_space), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.SegmentBar(
    weight: Float,
    color: Color
) {
    if (weight > 0f) {
        Box(
            modifier = Modifier
                .weight(weight.coerceAtLeast(0.001f))
                .fillMaxHeight()
                .background(color)
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
