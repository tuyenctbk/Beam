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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.FileItem
import com.example.ui.theme.BeamError
import com.example.ui.theme.BeamErrorBg
import com.example.ui.theme.BeamOnBackground
import com.example.ui.theme.BeamPrimary
import com.example.ui.theme.BeamPrimaryContainer
import com.example.ui.theme.BeamSecondary

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ConfirmDeleteModal(
    fileItem: FileItem?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (fileItem == null) return

    var isCancelFocused by remember { mutableStateOf(false) }
    var isDeleteFocused by remember { mutableStateOf(false) }

    val cancelScale by animateFloatAsState(targetValue = if (isCancelFocused) 1.05f else 1.0f, label = "cancelScale")
    val deleteScale by animateFloatAsState(targetValue = if (isDeleteFocused) 1.05f else 1.0f, label = "deleteScale")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                .padding(24.dp)
                .testTag("confirm_delete_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Delete Icon Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(BeamErrorBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = stringResource(R.string.delete_permanently),
                        tint = BeamError,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.delete_permanently),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BeamOnBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = fileItem.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BeamPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (fileItem.isDirectory) stringResource(R.string.delete_warning_folder)
                    else stringResource(R.string.delete_warning_file, fileItem.formattedSize),
                    fontSize = 12.sp,
                    color = BeamSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                scaleX = cancelScale
                                scaleY = cancelScale
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isCancelFocused) BeamPrimaryContainer else Color(0xFFF1F5F9))
                            .border(
                                if (isCancelFocused) 2.dp else 1.dp,
                                if (isCancelFocused) BeamPrimary else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .onFocusChanged { isCancelFocused = it.isFocused }
                            .focusable()
                            .clickable { onDismiss() }
                            .padding(vertical = 12.dp)
                            .testTag("cancel_delete_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCancelFocused) BeamPrimary else BeamSecondary
                        )
                    }

                    // Delete Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                scaleX = deleteScale
                                scaleY = deleteScale
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(BeamError)
                            .border(
                                if (isDeleteFocused) 2.dp else 1.dp,
                                if (isDeleteFocused) Color.White else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .onFocusChanged { isDeleteFocused = it.isFocused }
                            .focusable()
                            .clickable { onConfirm() }
                            .padding(vertical = 12.dp)
                            .testTag("confirm_delete_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.delete),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
