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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.FileItem
import com.example.data.StorageVolumeInfo
import com.example.ui.theme.BeamOnBackground
import com.example.ui.theme.BeamPrimary
import com.example.ui.theme.BeamPrimaryContainer
import com.example.ui.theme.BeamSecondary
import java.io.File

enum class ModalMode {
    RENAME,
    MOVE
}

@Composable
fun RenameMoveModal(
    fileItem: FileItem?,
    mode: ModalMode,
    storageVolumes: List<StorageVolumeInfo>,
    onRenameConfirm: (FileItem, String) -> Unit,
    onMoveConfirm: (FileItem, File) -> Unit,
    onDismiss: () -> Unit
) {
    if (fileItem == null) return

    var inputName by remember(fileItem.name) { mutableStateOf(fileItem.name) }
    var selectedTargetFolder by remember { mutableStateOf<File?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                .padding(24.dp)
                .testTag(if (mode == ModalMode.RENAME) "rename_dialog" else "move_dialog")
        ) {
            Column {
                // Header Title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BeamPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (mode == ModalMode.RENAME) Icons.Default.Edit else Icons.Default.DriveFileMove,
                            contentDescription = null,
                            tint = BeamPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (mode == ModalMode.RENAME) stringResource(R.string.rename) else stringResource(R.string.move_relocate),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BeamOnBackground
                        )
                        Text(
                            text = fileItem.name,
                            fontSize = 12.sp,
                            color = BeamSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (mode == ModalMode.RENAME) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text(stringResource(R.string.new_name)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rename_input_field"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BeamPrimary,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { onDismiss() }
                                .padding(vertical = 12.dp)
                                .testTag("cancel_rename_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(R.string.cancel), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BeamSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BeamPrimary)
                                .clickable {
                                    if (inputName.isNotBlank() && inputName != fileItem.name) {
                                        onRenameConfirm(fileItem, inputName.trim())
                                    }
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp)
                                .testTag("save_rename_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(R.string.save_name), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    // MOVE MODE: Target Volume / Folder List
                    Text(
                        text = stringResource(R.string.select_destination),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BeamSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val targetDirs = remember(storageVolumes) {
                        storageVolumes.map { File(it.path, "Download") }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(180.dp)
                    ) {
                        items(targetDirs) { folder ->
                            val isSelected = selectedTargetFolder == folder || (selectedTargetFolder == null && folder == targetDirs.firstOrNull())
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) BeamPrimaryContainer else Color(0xFFF8FAFC))
                                    .border(1.dp, if (isSelected) BeamPrimary else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                    .clickable { selectedTargetFolder = folder }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = BeamPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = folder.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BeamOnBackground
                                        )
                                        Text(
                                            text = folder.absolutePath,
                                            fontSize = 11.sp,
                                            color = BeamSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = BeamPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { onDismiss() }
                                .padding(vertical = 12.dp)
                                .testTag("cancel_move_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(R.string.cancel), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BeamSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BeamPrimary)
                                .clickable {
                                    val target = selectedTargetFolder ?: targetDirs.firstOrNull()
                                    if (target != null) {
                                        onMoveConfirm(fileItem, target)
                                    }
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp)
                                .testTag("confirm_move_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(R.string.move_here), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
