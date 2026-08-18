package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.FileCategory
import com.example.data.FileItem
import com.example.data.FileSortOption
import com.example.data.StorageVolumeInfo
import com.example.ui.components.ConfirmDeleteModal
import com.example.ui.components.FileContextMenuModal
import com.example.ui.components.FileItemCard
import com.example.ui.components.FilePreviewModal
import com.example.ui.components.ModalMode
import com.example.ui.components.RenameMoveModal
import com.example.ui.components.TvRemoteShortcutsBar
import com.example.ui.theme.BeamError
import com.example.ui.theme.BeamOnBackground
import com.example.ui.theme.BeamOnPrimaryContainer
import com.example.ui.theme.BeamPrimary
import com.example.ui.theme.BeamPrimaryContainer
import com.example.ui.theme.BeamSecondary
import java.io.File

import androidx.compose.foundation.focusable
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ExplorerScreen(
    fileItems: List<FileItem>,
    currentDirectory: File,
    selectedCategory: FileCategory,
    selectedSortOption: FileSortOption,
    searchQuery: String,
    storageVolumes: List<StorageVolumeInfo>,
    isBatchMode: Boolean = false,
    selectedFilePaths: Set<String> = emptySet(),
    onCategorySelected: (FileCategory) -> Unit,
    onSortSelected: (FileSortOption) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onOpenItem: (FileItem) -> Unit,
    onDeleteItem: (FileItem) -> Unit,
    onRenameItem: (FileItem, String) -> Unit,
    onMoveItem: (FileItem, File) -> Unit,
    onToggleBatchMode: () -> Unit,
    onToggleFileSelection: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onBulkDelete: () -> Unit,
    onBulkMove: (File) -> Unit
) {
    var contextMenuFile by remember { mutableStateOf<FileItem?>(null) }
    var pendingDeleteFile by remember { mutableStateOf<FileItem?>(null) }
    var previewFile by remember { mutableStateOf<FileItem?>(null) }
    var renameFile by remember { mutableStateOf<FileItem?>(null) }
    var moveFile by remember { mutableStateOf<FileItem?>(null) }
    var showBatchMoveModal by remember { mutableStateOf(false) }
    var showBatchDeleteModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Search bar with real-time text input and instant clear
        var isSearchFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text(stringResource(R.string.search_placeholder), fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = if (isSearchFocused) BeamPrimary else BeamSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                            .clickable { onSearchQueryChanged("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.cancel),
                            tint = BeamSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .onFocusChanged { isSearchFocused = it.isFocused }
                .testTag("search_text_field"),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = BeamPrimary,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )

        // Category Quick-Filter Chips Row with Counts
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            items(FileCategory.entries) { category ->
                val isSelected = category == selectedCategory
                val categoryCount = remember(fileItems, category) {
                    if (category == FileCategory.ALL) {
                        fileItems.size
                    } else {
                        fileItems.count { item -> item.category == category }
                    }
                }
                var isChipFocused by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when {
                                isChipFocused -> BeamPrimaryContainer
                                isSelected -> BeamPrimaryContainer
                                else -> Color.White
                            }
                        )
                        .border(
                            if (isChipFocused) 2.dp else 1.dp,
                            if (isChipFocused || isSelected) BeamPrimary else Color(0xFFE2E8F0),
                            RoundedCornerShape(16.dp)
                        )
                        .onFocusChanged { isChipFocused = it.isFocused }
                        .focusable()
                        .clickable { onCategorySelected(category) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("category_chip_${category.name.lowercase()}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(category.labelResId),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected || isChipFocused) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected || isChipFocused) BeamOnPrimaryContainer else BeamSecondary
                        )

                        if (categoryCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected || isChipFocused) BeamPrimary else Color(0xFFF1F5F9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$categoryCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected || isChipFocused) Color.White else BeamSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sort Options & Batch Toggle Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = stringResource(R.string.sort_name),
                    tint = BeamSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    text = "${stringResource(R.string.sort_name)}: ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BeamSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(FileSortOption.entries) { option ->
                        val isSelected = option == selectedSortOption
                        var isSortFocused by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSortFocused -> BeamPrimaryContainer
                                        isSelected -> BeamPrimary
                                        else -> Color.White
                                    }
                                )
                                .border(
                                    if (isSortFocused) 2.dp else 1.dp,
                                    if (isSortFocused || isSelected) BeamPrimary else Color(0xFFE2E8F0),
                                    RoundedCornerShape(12.dp)
                                )
                                .onFocusChanged { isSortFocused = it.isFocused }
                                .focusable()
                                .clickable { onSortSelected(option) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("sort_chip_${option.name.lowercase()}")
                        ) {
                            Text(
                                text = stringResource(option.labelResId),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected || isSortFocused) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected && !isSortFocused) Color.White else if (isSortFocused) BeamOnPrimaryContainer else BeamSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Batch Selection Toggle Button
            var isBatchToggleFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isBatchMode) Color(0xFFDCFCE7) else if (isBatchToggleFocused) BeamPrimaryContainer else Color.White)
                    .border(
                        if (isBatchToggleFocused) 2.dp else 1.dp,
                        if (isBatchToggleFocused) BeamPrimary else if (isBatchMode) Color(0xFF16A34A) else Color(0xFFE2E8F0),
                        RoundedCornerShape(14.dp)
                    )
                    .onFocusChanged { isBatchToggleFocused = it.isFocused }
                    .focusable()
                    .clickable { onToggleBatchMode() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("batch_toggle_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = "Batch Select",
                        tint = if (isBatchMode) Color(0xFF16A34A) else BeamSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBatchMode) stringResource(R.string.cancel) else stringResource(R.string.batch_select),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBatchMode) Color(0xFF16A34A) else BeamSecondary
                    )
                }
            }
        }

        // Batch Action Controls Bar (when Batch Mode active)
        if (isBatchMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0FDF4))
                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${selectedFilePaths.size} / ${fileItems.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Select All
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                                .clickable {
                                    if (selectedFilePaths.size == fileItems.size) onClearSelection() else onSelectAll()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SelectAll, contentDescription = null, tint = BeamPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (selectedFilePaths.size == fileItems.size) stringResource(R.string.clear_selection) else stringResource(R.string.select_all),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BeamPrimary
                                )
                            }
                        }

                        // Bulk Move
                        if (selectedFilePaths.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BeamPrimary)
                                    .clickable { showBatchMoveModal = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.move_relocate), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Bulk Delete
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BeamError)
                                    .clickable { showBatchDeleteModal = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.delete), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Folder Path Breadcrumb
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                var isNavBackFocused by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isNavBackFocused) BeamPrimary else BeamPrimaryContainer)
                        .border(if (isNavBackFocused) 2.dp else 0.dp, BeamPrimary, CircleShape)
                        .onFocusChanged { isNavBackFocused = it.isFocused }
                        .focusable()
                        .clickable { onNavigateUp() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Navigate Up",
                        tint = if (isNavBackFocused) Color.White else BeamPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = currentDirectory.absolutePath,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BeamOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Files List
        if (fileItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseRadius by infiniteTransition.animateFloat(
                    initialValue = 40f,
                    targetValue = 220f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2800, easing = androidx.compose.animation.core.LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pulseRadius"
                )
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2800, easing = androidx.compose.animation.core.LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pulseAlpha"
                )

                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing glowing radar scans radiating outwards
                        drawCircle(
                            color = Color(0xFF38BDF8).copy(alpha = pulseAlpha),
                            radius = pulseRadius,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF2563EB).copy(alpha = pulseAlpha * 0.6f),
                            radius = pulseRadius * 0.7f,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(BeamPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = BeamPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_files_found),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BeamPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.no_recent_downloads),
                            fontSize = 12.sp,
                            color = BeamSecondary
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(fileItems, key = { it.path }) { item ->
                    FileItemCard(
                        item = item,
                        onOpen = { onOpenItem(it) },
                        onDelete = { pendingDeleteFile = it },
                        onContextMenu = { contextMenuFile = it },
                        isBatchMode = isBatchMode,
                        isSelected = selectedFilePaths.contains(item.path),
                        onToggleSelect = { onToggleFileSelection(it.path) }
                    )
                }
            }
        }

        // TV Remote Navigation Bar helper
        TvRemoteShortcutsBar()
    }

    // Modal Dialogs
    FileContextMenuModal(
        fileItem = contextMenuFile,
        onOpen = { onOpenItem(it) },
        onPreview = { previewFile = it },
        onRename = { renameFile = it },
        onMove = { moveFile = it },
        onDelete = { pendingDeleteFile = it },
        onDismiss = { contextMenuFile = null }
    )

    ConfirmDeleteModal(
        fileItem = pendingDeleteFile,
        onConfirm = {
            pendingDeleteFile?.let { onDeleteItem(it) }
            pendingDeleteFile = null
        },
        onDismiss = { pendingDeleteFile = null }
    )

    if (showBatchDeleteModal) {
        ConfirmDeleteModal(
            fileItem = FileItem(file = File("bulk_selected"), name = "${selectedFilePaths.size} selected items"),
            onConfirm = {
                onBulkDelete()
                showBatchDeleteModal = false
            },
            onDismiss = { showBatchDeleteModal = false }
        )
    }

    FilePreviewModal(
        fileItem = previewFile,
        onOpen = { onOpenItem(it) },
        onDelete = { pendingDeleteFile = it },
        onDismiss = { previewFile = null }
    )

    RenameMoveModal(
        fileItem = renameFile,
        mode = ModalMode.RENAME,
        storageVolumes = storageVolumes,
        onRenameConfirm = { item, newName -> onRenameItem(item, newName) },
        onMoveConfirm = { _, _ -> },
        onDismiss = { renameFile = null }
    )

    RenameMoveModal(
        fileItem = moveFile,
        mode = ModalMode.MOVE,
        storageVolumes = storageVolumes,
        onRenameConfirm = { _, _ -> },
        onMoveConfirm = { item, targetDir -> onMoveItem(item, targetDir) },
        onDismiss = { moveFile = null }
    )

    if (showBatchMoveModal) {
        RenameMoveModal(
            fileItem = FileItem(file = File("bulk_selected"), name = "${selectedFilePaths.size} selected items"),
            mode = ModalMode.MOVE,
            storageVolumes = storageVolumes,
            onRenameConfirm = { _, _ -> },
            onMoveConfirm = { _, targetDir ->
                onBulkMove(targetDir)
                showBatchMoveModal = false
            },
            onDismiss = { showBatchMoveModal = false }
        )
    }
}
