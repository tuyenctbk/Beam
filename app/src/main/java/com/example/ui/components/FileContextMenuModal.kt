package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.FileCategory
import com.example.data.FileItem
import com.example.ui.theme.BeamError
import com.example.ui.theme.BeamErrorBg
import com.example.ui.theme.BeamOnBackground
import com.example.ui.theme.BeamPrimary
import com.example.ui.theme.BeamPrimaryContainer
import com.example.ui.theme.BeamSecondary
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.focusable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun FileContextMenuModal(
    fileItem: FileItem?,
    onOpen: (FileItem) -> Unit,
    onPreview: (FileItem) -> Unit,
    onRename: (FileItem) -> Unit,
    onMove: (FileItem) -> Unit,
    onDelete: (FileItem) -> Unit,
    onDismiss: () -> Unit
) {
    if (fileItem == null) return

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                .padding(20.dp)
                .testTag("file_context_menu_dialog")
        ) {
            Column {
                // Header with File info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BeamPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fileItem.extension.take(3).uppercase().ifEmpty { "DIR" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BeamPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fileItem.apkAppName ?: fileItem.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BeamOnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${stringResource(fileItem.category.labelResId)} • ${fileItem.formattedSize}",
                            fontSize = 12.sp,
                            color = BeamSecondary
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Preview
                    ContextMenuItemRow(
                        title = stringResource(R.string.quick_preview),
                        subtitle = stringResource(R.string.quick_preview_desc),
                        icon = Icons.Default.Preview,
                        onClick = {
                            onDismiss()
                            onPreview(fileItem)
                        },
                        tag = "preview_option"
                    )

                    // Open / Install / Extract
                    val actionLabel = when (fileItem.category) {
                        FileCategory.APKS -> stringResource(R.string.install_package)
                        FileCategory.ZIP -> stringResource(R.string.extract_archive)
                        else -> if (fileItem.isDirectory) stringResource(R.string.open_folder) else stringResource(R.string.open_file)
                    }
                    ContextMenuItemRow(
                        title = actionLabel,
                        subtitle = "Launch default Android action",
                        icon = Icons.Default.OpenInNew,
                        onClick = {
                            onDismiss()
                            onOpen(fileItem)
                        },
                        tag = "open_option"
                    )

                    // Rename
                    ContextMenuItemRow(
                        title = stringResource(R.string.rename),
                        subtitle = stringResource(R.string.rename_desc),
                        icon = Icons.Default.Edit,
                        onClick = {
                            onDismiss()
                            onRename(fileItem)
                        },
                        tag = "rename_option"
                    )

                    // Move
                    ContextMenuItemRow(
                        title = stringResource(R.string.move_relocate),
                        subtitle = stringResource(R.string.move_relocate_desc),
                        icon = Icons.Default.DriveFileMove,
                        onClick = {
                            onDismiss()
                            onMove(fileItem)
                        },
                        tag = "move_option"
                    )

                    // Delete (Warning Color)
                    ContextMenuItemRow(
                        title = stringResource(R.string.delete_permanently),
                        subtitle = stringResource(R.string.delete_permanently_desc),
                        icon = Icons.Default.Delete,
                        iconTint = BeamError,
                        bgContainer = BeamErrorBg,
                        onClick = {
                            onDismiss()
                            onDelete(fileItem)
                        },
                        tag = "delete_option"
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = BeamPrimary,
    bgContainer: Color = BeamPrimaryContainer,
    onClick: () -> Unit,
    tag: String
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.025f else 1.0f, label = "rowScale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(if (isFocused) BeamPrimaryContainer.copy(alpha = 0.4f) else Color(0xFFF8FAFC))
            .border(
                if (isFocused) 2.dp else 1.dp,
                if (isFocused) BeamPrimary else Color(0xFFF1F5F9),
                RoundedCornerShape(16.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(12.dp)
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(bgContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BeamOnBackground
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = BeamSecondary
                )
            }
        }
    }
}
