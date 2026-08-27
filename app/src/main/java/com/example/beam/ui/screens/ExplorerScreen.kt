package com.example.beam.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.beam.data.model.FileCategory
import com.example.beam.data.model.FileItem
import com.example.beam.ui.components.BeamEmptyState
import com.example.beam.ui.components.DeleteConfirmationDialog
import com.example.beam.ui.components.EmptyStateType
import com.example.beam.ui.components.TvFocusableCard
import com.example.beam.ui.theme.BeamAmber
import com.example.beam.ui.theme.BeamBorder
import com.example.beam.ui.theme.BeamBorderLight
import com.example.beam.ui.theme.BeamFocusCyan
import com.example.beam.ui.theme.BeamFocusCyanBright
import com.example.beam.ui.theme.BeamGreen
import com.example.beam.ui.theme.BeamNeonBlue
import com.example.beam.ui.theme.BeamRose
import com.example.beam.ui.viewmodel.BatchOperation
import com.example.beam.ui.viewmodel.MainViewModel

enum class SortField {
    NAME,
    DATE,
    SIZE
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

sealed interface DeleteTarget {
    data class Single(val item: FileItem) : DeleteTarget
    data class Batch(val items: Set<FileItem>) : DeleteTarget
}

@Composable
fun ExplorerScreen(
    viewModel: MainViewModel,
    onNavigateToWebBeam: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val fileList by viewModel.fileList.collectAsState()
    val currentDirectory by viewModel.currentDirectory.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val stagedBatchOp by viewModel.stagedBatchOp.collectAsState()
    val stagedBatchFiles by viewModel.stagedBatchFiles.collectAsState()

    var sortField by remember { mutableStateOf(SortField.NAME) }
    var sortOrder by remember { mutableStateOf(SortOrder.ASCENDING) }
    var pendingDeleteTarget by remember { mutableStateOf<DeleteTarget?>(null) }

    // Filter and Sort files
    val filteredList = remember(fileList, searchQuery, sortField, sortOrder) {
        var list = fileList
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.trim()
            list = list.filter { it.name.contains(query, ignoreCase = true) }
        }

        when (sortField) {
            SortField.NAME -> {
                if (sortOrder == SortOrder.ASCENDING) {
                    list.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                } else {
                    list.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenByDescending { it.name.lowercase() })
                }
            }
            SortField.SIZE -> {
                if (sortOrder == SortOrder.DESCENDING) {
                    list.sortedWith(compareBy({ !it.isDirectory }, { -it.sizeBytes }))
                } else {
                    list.sortedWith(compareBy({ !it.isDirectory }, { it.sizeBytes }))
                }
            }
            SortField.DATE -> {
                if (sortOrder == SortOrder.DESCENDING) {
                    list.sortedWith(compareBy({ !it.isDirectory }, { -it.lastModified }))
                } else {
                    list.sortedWith(compareBy({ !it.isDirectory }, { it.lastModified }))
                }
            }
        }
    }

    // Deletion Confirmation Dialog
    pendingDeleteTarget?.let { target ->
        when (target) {
            is DeleteTarget.Single -> {
                DeleteConfirmationDialog(
                    title = if (target.item.isDirectory) stringResource(R.string.delete_folder_confirm) else stringResource(R.string.delete_file_confirm),
                    itemName = target.item.name,
                    sizeFormatted = target.item.formattedSize,
                    isDirectory = target.item.isDirectory,
                    onConfirm = {
                        viewModel.deleteFile(target.item)
                        pendingDeleteTarget = null
                    },
                    onDismiss = {
                        pendingDeleteTarget = null
                    }
                )
            }
            is DeleteTarget.Batch -> {
                val totalBytes = target.items.sumOf { it.sizeBytes }
                val formattedTotal = if (totalBytes > 0) viewModel.storageInfo.value.formatBytes(totalBytes) else null
                DeleteConfirmationDialog(
                    title = stringResource(R.string.delete_batch_confirm_title, target.items.size),
                    itemName = target.items.take(3).joinToString(", ") { it.name } + if (target.items.size > 3) stringResource(R.string.delete_batch_more_items, target.items.size - 3) else "",
                    itemCount = target.items.size,
                    sizeFormatted = formattedTotal,
                    isDirectory = target.items.any { it.isDirectory },
                    onConfirm = {
                        viewModel.deleteSelectedFiles()
                        pendingDeleteTarget = null
                    },
                    onDismiss = {
                        pendingDeleteTarget = null
                    }
                )
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isCompact = screenWidth < 600.dp
        val horizontalPadding = if (isCompact) 16.dp else 24.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding)
        ) {
            // Path & Directory Control Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Directory Path or Category Label
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BeamFocusCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selectedCategory != null) selectedCategory!!.icon else Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = if (selectedCategory != null) selectedCategory!!.color else BeamFocusCyanBright,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (selectedCategory != null) stringResource(selectedCategory!!.titleRes) else currentDirectory.name.ifEmpty { stringResource(R.string.storage_root) },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1
                        )
                        Text(
                            text = if (selectedCategory != null) stringResource(R.string.category_filter_count, filteredList.size) else currentDirectory.absolutePath,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Top Quick Actions & Multi-Select Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Parent Folder Up Button (if in subdirectory)
                    if (selectedCategory == null && currentDirectory.parentFile != null) {
                        TvFocusableCard(
                            onClick = { viewModel.navigateUp() },
                            modifier = Modifier.height(40.dp),
                            testTag = "explorer_btn_up"
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = BeamFocusCyanBright, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = stringResource(R.string.parent_dir), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Batch Selection / Operations Controls
                    if (selectedFiles.isNotEmpty()) {
                        // Move Batch Button
                        TvFocusableCard(
                            onClick = { viewModel.stageBatchMove(selectedFiles) },
                            modifier = Modifier.height(40.dp),
                            focusBorderColor = BeamAmber,
                            defaultBgColor = BeamAmber.copy(alpha = 0.15f),
                            testTag = "explorer_btn_batch_move"
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.ContentCut, contentDescription = null, tint = BeamAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.batch_move, selectedFiles.size),
                                    fontSize = 13.sp,
                                    color = BeamAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Copy Batch Button
                        TvFocusableCard(
                            onClick = { viewModel.stageBatchCopy(selectedFiles) },
                            modifier = Modifier.height(40.dp),
                            focusBorderColor = BeamGreen,
                            defaultBgColor = BeamGreen.copy(alpha = 0.15f),
                            testTag = "explorer_btn_batch_copy"
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = BeamGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.batch_copy, selectedFiles.size),
                                    fontSize = 13.sp,
                                    color = BeamGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Delete Batch Button
                        TvFocusableCard(
                            onClick = { pendingDeleteTarget = DeleteTarget.Batch(selectedFiles) },
                            modifier = Modifier.height(40.dp),
                            focusBorderColor = BeamRose,
                            defaultBgColor = BeamRose.copy(alpha = 0.15f),
                            testTag = "explorer_btn_batch_delete"
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = BeamRose, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.batch_delete, selectedFiles.size),
                                    fontSize = 13.sp,
                                    color = BeamRose,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        TvFocusableCard(
                            onClick = { viewModel.deselectAllFiles() },
                            modifier = Modifier.height(40.dp),
                            testTag = "explorer_btn_deselect_all"
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.deselect_all), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            }
                        }
                    } else {
                        TvFocusableCard(
                            onClick = { viewModel.selectAllFiles() },
                            modifier = Modifier.height(40.dp),
                            testTag = "explorer_btn_select_all"
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.SelectAll, contentDescription = null, tint = BeamFocusCyanBright, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = stringResource(R.string.select_all), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & Sorting Controls Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input with Clear Button
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text(stringResource(R.string.explorer_search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = BeamFocusCyanBright, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setSearchQuery("") },
                                modifier = Modifier.testTag("explorer_btn_clear_search")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("explorer_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = BeamFocusCyanBright,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )

                // Sort Selector Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.sort_by_label),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Sort by Name Chip
                    val isNameActive = sortField == SortField.NAME
                    TvFocusableCard(
                        onClick = {
                            if (isNameActive) {
                                sortOrder = if (sortOrder == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
                            } else {
                                sortField = SortField.NAME
                                sortOrder = SortOrder.ASCENDING
                            }
                        },
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        defaultBgColor = if (isNameActive) BeamFocusCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        focusBorderColor = BeamFocusCyanBright,
                        testTag = "sort_by_name_chip"
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SortByAlpha,
                                contentDescription = null,
                                tint = if (isNameActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isNameActive) {
                                    if (sortOrder == SortOrder.ASCENDING) stringResource(R.string.sort_name_asc) else stringResource(R.string.sort_name_desc)
                                } else {
                                    stringResource(R.string.sort_name)
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isNameActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNameActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurface
                            )
                            if (isNameActive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = BeamFocusCyanBright,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Sort by Date Chip
                    val isDateActive = sortField == SortField.DATE
                    TvFocusableCard(
                        onClick = {
                            if (isDateActive) {
                                sortOrder = if (sortOrder == SortOrder.DESCENDING) SortOrder.ASCENDING else SortOrder.DESCENDING
                            } else {
                                sortField = SortField.DATE
                                sortOrder = SortOrder.DESCENDING
                            }
                        },
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        defaultBgColor = if (isDateActive) BeamFocusCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        focusBorderColor = BeamFocusCyanBright,
                        testTag = "sort_by_date_chip"
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (isDateActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDateActive) {
                                    if (sortOrder == SortOrder.DESCENDING) stringResource(R.string.sort_date_desc) else stringResource(R.string.sort_date_asc)
                                } else {
                                    stringResource(R.string.sort_date)
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isDateActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDateActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurface
                            )
                            if (isDateActive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = BeamFocusCyanBright,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Sort by Size Chip
                    val isSizeActive = sortField == SortField.SIZE
                    TvFocusableCard(
                        onClick = {
                            if (isSizeActive) {
                                sortOrder = if (sortOrder == SortOrder.DESCENDING) SortOrder.ASCENDING else SortOrder.DESCENDING
                            } else {
                                sortField = SortField.SIZE
                                sortOrder = SortOrder.DESCENDING
                            }
                        },
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        defaultBgColor = if (isSizeActive) BeamFocusCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        focusBorderColor = BeamFocusCyanBright,
                        testTag = "sort_by_size_chip"
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DataUsage,
                                contentDescription = null,
                                tint = if (isSizeActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSizeActive) {
                                    if (sortOrder == SortOrder.DESCENDING) stringResource(R.string.sort_size_desc) else stringResource(R.string.sort_size_asc)
                                } else {
                                    stringResource(R.string.sort_size)
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSizeActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSizeActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSizeActive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = BeamFocusCyanBright,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Category Filter Chips Bar (Images, Videos, Documents, Audio)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filter_by_label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 10.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // All Files Chip
                    item {
                        val isAllActive = selectedCategory == null
                        TvFocusableCard(
                            onClick = {
                                if (!isAllActive) {
                                    viewModel.loadDirectory(currentDirectory)
                                }
                            },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(18.dp),
                            defaultBgColor = if (isAllActive) BeamFocusCyan.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                            focusBorderColor = BeamFocusCyanBright,
                            testTag = "filter_chip_all"
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (isAllActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.filter_all_files),
                                    fontSize = 12.sp,
                                    fontWeight = if (isAllActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAllActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Category Filter Chips: Images, Videos, Documents, Audio
                    val categoryFilters = listOf(
                        FileCategory.PHOTOS to R.string.filter_images,
                        FileCategory.VIDEOS to R.string.filter_videos,
                        FileCategory.DOCUMENTS to R.string.filter_documents,
                        FileCategory.MUSIC to R.string.filter_audio
                    )

                    items(categoryFilters) { (category, labelRes) ->
                        val isActive = selectedCategory == category
                        TvFocusableCard(
                            onClick = {
                                if (isActive) {
                                    viewModel.loadDirectory(currentDirectory)
                                } else {
                                    viewModel.loadCategory(category)
                                }
                            },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(18.dp),
                            defaultBgColor = if (isActive) category.color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                            focusBorderColor = category.color,
                            testTag = "filter_chip_${category.name.lowercase()}"
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    tint = if (isActive) category.color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(labelRes),
                                    fontSize = 12.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isActive) category.color else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Active Search Filter Feedback Bar
            if (searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.search_results_count, filteredList.size),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = BeamFocusCyanBright
                    )
                    Text(
                        text = stringResource(R.string.query_label, searchQuery.trim()),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Files List View
            if (filteredList.isEmpty()) {
                BeamEmptyState(
                    type = when {
                        searchQuery.isNotBlank() -> EmptyStateType.SEARCH_NO_RESULTS
                        selectedCategory != null -> EmptyStateType.CATEGORY_EMPTY
                        else -> EmptyStateType.EMPTY_FOLDER
                    },
                    searchQuery = searchQuery,
                    onPrimaryAction = when {
                        searchQuery.isNotBlank() -> ({ viewModel.setSearchQuery("") })
                        onNavigateToWebBeam != null -> ({ onNavigateToWebBeam.invoke() })
                        else -> null
                    },
                    onSecondaryAction = if (selectedCategory != null) ({ viewModel.loadDirectory(currentDirectory) }) else null
                )
            } else {
                // Column Header Bar (Name, Date, Size with active sort arrow indicators)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        // Name Header
                        val isNameActive = sortField == SortField.NAME
                        TvFocusableCard(
                            onClick = {
                                if (isNameActive) {
                                    sortOrder = if (sortOrder == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
                                } else {
                                    sortField = SortField.NAME
                                    sortOrder = SortOrder.ASCENDING
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            defaultBgColor = Color.Transparent,
                            focusBorderColor = BeamFocusCyanBright,
                            testTag = "col_header_name"
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.header_column_name),
                                    fontSize = 12.sp,
                                    fontWeight = if (isNameActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isNameActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isNameActive && sortOrder == SortOrder.DESCENDING) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isNameActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Date Header
                        val isDateActive = sortField == SortField.DATE
                        TvFocusableCard(
                            onClick = {
                                if (isDateActive) {
                                    sortOrder = if (sortOrder == SortOrder.DESCENDING) SortOrder.ASCENDING else SortOrder.DESCENDING
                                } else {
                                    sortField = SortField.DATE
                                    sortOrder = SortOrder.DESCENDING
                                }
                            },
                            modifier = Modifier
                                .width(130.dp)
                                .height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            defaultBgColor = Color.Transparent,
                            focusBorderColor = BeamFocusCyanBright,
                            testTag = "col_header_date"
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.header_column_date),
                                    fontSize = 12.sp,
                                    fontWeight = if (isDateActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isDateActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isDateActive && sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (isDateActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Size Header
                        val isSizeActive = sortField == SortField.SIZE
                        TvFocusableCard(
                            onClick = {
                                if (isSizeActive) {
                                    sortOrder = if (sortOrder == SortOrder.DESCENDING) SortOrder.ASCENDING else SortOrder.DESCENDING
                                } else {
                                    sortField = SortField.SIZE
                                    sortOrder = SortOrder.DESCENDING
                                }
                            },
                            modifier = Modifier
                                .width(110.dp)
                                .height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            defaultBgColor = Color.Transparent,
                            focusBorderColor = BeamFocusCyanBright,
                            testTag = "col_header_size"
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.header_column_size),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSizeActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSizeActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isSizeActive && sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (isSizeActive) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = if (stagedBatchOp != BatchOperation.NONE) 90.dp else 32.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("explorer_files_list")
                ) {
                    items(filteredList, key = { it.path }) { item ->
                        val isSelected = selectedFiles.contains(item)

                        TvFocusableCard(
                            onClick = {
                                if (item.isDirectory) {
                                    viewModel.loadDirectory(item.file)
                                } else {
                                    viewModel.openFileDetail(item)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            testTag = "file_item_${item.name.lowercase().replace(" ", "_").replace(".", "_")}"
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    // Multi-select Checkbox
                                    TvFocusableCard(
                                        onClick = { viewModel.toggleFileSelection(item) },
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        testTag = "file_checkbox_${item.name.lowercase().replace(" ", "_").replace(".", "_")}"
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                                contentDescription = "Select ${item.name}",
                                                tint = if (isSelected) BeamFocusCyanBright else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // File Thumbnail (Coil AsyncImage for Photos) or Category Icon
                                    if (item.category == FileCategory.PHOTOS && !item.isDirectory) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = item.file,
                                                contentDescription = "Thumbnail of ${item.name}",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(item.category.color.copy(alpha = 0.18f))
                                                .border(1.dp, item.category.color.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (item.isDirectory) Icons.Default.Folder else item.category.icon,
                                                contentDescription = item.name,
                                                tint = if (item.isDirectory) BeamFocusCyanBright else item.category.color,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Name, Date & Category Tag
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.formattedDate,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (!item.isDirectory && item.extension.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "•  ${item.extension.uppercase()}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = item.category.color
                                                )
                                            }
                                        }
                                    }
                                }

                                // Right Side: Size Badge & Quick Delete Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (!item.isDirectory) {
                                        Text(
                                            text = item.formattedSize,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BeamFocusCyanBright
                                        )
                                    }

                                    // Quick Single-item Delete Action
                                    TvFocusableCard(
                                        onClick = {
                                            pendingDeleteTarget = DeleteTarget.Single(item)
                                        },
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        focusBorderColor = BeamRose,
                                        testTag = "item_delete_btn_${item.name.lowercase().replace(" ", "_").replace(".", "_")}"
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete ${item.name}",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
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

        // Sticky Floating Batch Operation (Paste / Cancel) Banner
        if (stagedBatchOp != BatchOperation.NONE && stagedBatchFiles.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            2.dp,
                            if (stagedBatchOp == BatchOperation.MOVE) BeamAmber else BeamGreen,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (stagedBatchOp == BatchOperation.MOVE) BeamAmber.copy(alpha = 0.2f)
                                        else BeamGreen.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (stagedBatchOp == BatchOperation.MOVE) Icons.Default.ContentCut else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = if (stagedBatchOp == BatchOperation.MOVE) BeamAmber else BeamGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (stagedBatchOp == BatchOperation.MOVE) stringResource(R.string.staged_moving_banner, stagedBatchFiles.size)
                                    else stringResource(R.string.staged_copying_banner, stagedBatchFiles.size),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.staged_destination, currentDirectory.name.ifEmpty { stringResource(R.string.storage_root) }),
                                    fontSize = 11.sp,
                                    color = if (stagedBatchOp == BatchOperation.MOVE) BeamAmber else BeamGreen
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TvFocusableCard(
                                onClick = { viewModel.executeBatchPaste(currentDirectory) },
                                modifier = Modifier.height(42.dp),
                                focusBorderColor = if (stagedBatchOp == BatchOperation.MOVE) BeamAmber else BeamGreen,
                                defaultBgColor = if (stagedBatchOp == BatchOperation.MOVE) BeamAmber.copy(alpha = 0.25f) else BeamGreen.copy(alpha = 0.25f),
                                testTag = "explorer_btn_paste_here"
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = null,
                                        tint = if (stagedBatchOp == BatchOperation.MOVE) BeamAmber else BeamGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.paste_here_btn, stagedBatchFiles.size),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            TvFocusableCard(
                                onClick = { viewModel.cancelBatchStaging() },
                                modifier = Modifier.height(42.dp),
                                testTag = "explorer_btn_cancel_paste"
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.cancel),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
