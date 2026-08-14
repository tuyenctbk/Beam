package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.R
import com.example.data.FileCategory
import com.example.data.FileItem
import com.example.util.ApkParser

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemCard(
    item: FileItem,
    onOpen: (FileItem) -> Unit,
    onDelete: (FileItem) -> Unit,
    onContextMenu: (FileItem) -> Unit,
    isBatchMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: ((FileItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.025f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f),
        label = "fileCardScale"
    )

    val apkDrawable = remember(item.path) {
        if (item.category == FileCategory.APKS) {
            ApkParser.getApkIcon(context, item.file)
        } else null
    }

    val (categoryIcon, iconBgColor, iconTint) = when (item.category) {
        FileCategory.APKS -> Triple(Icons.Default.Android, Color(0xFFDCFCE7), Color(0xFF16A34A))
        FileCategory.VIDEOS -> Triple(Icons.Default.Movie, Color(0xFFE0E7FF), Color(0xFF4338CA))
        FileCategory.PHOTOS -> Triple(Icons.Default.Image, Color(0xFFFFE4E6), Color(0xFFE11D48))
        FileCategory.MUSIC -> Triple(Icons.Default.AudioFile, Color(0xFFF3E8FF), Color(0xFF9333EA))
        FileCategory.ZIP -> Triple(Icons.Default.Unarchive, Color(0xFFFEF3C7), Color(0xFFD97706))
        FileCategory.DOCUMENTS -> Triple(Icons.Default.Description, Color(0xFFDBEAFE), Color(0xFF2563EB))
        else -> if (item.isDirectory) {
            Triple(Icons.Default.Folder, Color(0xFFF1F5F9), Color(0xFF475569))
        } else {
            Triple(Icons.Default.DownloadForOffline, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
        }
    }

    // High contrast D-pad TV focus visual feedback
    val cardBg = when {
        isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        isSelected -> Color(0xFFF0FDF4)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isFocused -> MaterialTheme.colorScheme.primary
        isSelected -> Color(0xFF16A34A)
        else -> MaterialTheme.colorScheme.outline
    }

    val borderWidth = if (isFocused) 2.dp else if (isSelected) 1.5.dp else 1.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = if (isFocused) 8.dp else 1.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .combinedClickable(
                onClick = {
                    if (isBatchMode) {
                        onToggleSelect?.invoke(item)
                    } else {
                        onOpen(item)
                    }
                },
                onLongClick = { onContextMenu(item) }
            )
            .padding(12.dp)
            .testTag("file_card_${item.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Batch Checkbox
                if (isBatchMode) {
                    Box(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .clickable { onToggleSelect?.invoke(item) }
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = "Select File",
                            tint = if (isSelected) Color(0xFF16A34A) else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Icon Box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (apkDrawable != null) {
                        Image(
                            bitmap = apkDrawable.toBitmap().asImageBitmap(),
                            contentDescription = item.apkAppName ?: item.name,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = stringResource(item.category.labelResId),
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.apkAppName ?: item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (item.apkPackageName != null) {
                            "${item.apkVersion} • ${item.formattedSize}"
                        } else {
                            "${stringResource(item.category.labelResId)} • ${item.formattedSize}"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!isBatchMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Primary Action Button (Install / Extract / Open / View)
                    val primaryLabel = when (item.category) {
                        FileCategory.APKS -> stringResource(R.string.install_package)
                        FileCategory.ZIP -> stringResource(R.string.extract_zip)
                        else -> if (item.isDirectory) stringResource(R.string.open) else stringResource(R.string.view)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onOpen(item) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("action_btn_${primaryLabel.lowercase()}")
                    ) {
                        Text(
                            text = primaryLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // D-Pad Context Menu Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onContextMenu(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2))
                            .clickable { onDelete(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
