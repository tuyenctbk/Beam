package com.example.beam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.beam.data.model.FileCategory
import com.example.beam.ui.components.BeamBottomNavBar
import com.example.beam.ui.components.BeamNavigationRail
import com.example.beam.ui.components.FileDetailModal
import com.example.beam.ui.components.RemoteShortcutBar
import com.example.beam.ui.components.ShareQrModal
import com.example.beam.ui.components.TvTab
import com.example.beam.ui.components.TvTopBar
import com.example.beam.ui.screens.DashboardScreen
import com.example.beam.ui.screens.ExplorerScreen
import com.example.beam.ui.screens.SettingsScreen
import com.example.beam.ui.screens.StorageScreen
import com.example.beam.ui.screens.WebBeamScreen
import com.example.beam.ui.theme.BeamFocusCyan
import com.example.beam.ui.theme.BeamFocusCyanBright
import com.example.beam.ui.theme.BeamTheme
import com.example.beam.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            BeamTheme(themeMode = themeMode) {
                var selectedTab by remember { mutableStateOf(TvTab.DASHBOARD) }
                val selectedCategory by viewModel.selectedCategory.collectAsState()
                val currentDirectory by viewModel.currentDirectory.collectAsState()
                val selectedFileForDetail by viewModel.selectedFileForDetail.collectAsState()
                val selectedFileMetadata by viewModel.selectedFileMetadata.collectAsState()
                val sha256Hash by viewModel.sha256Hash.collectAsState()
                val showShareQrDialog by viewModel.showShareQrDialog.collectAsState()
                val toastMessage by viewModel.toastMessage.collectAsState()
                val serverUrl by viewModel.serverUrl.collectAsState()
                val isServerRunning by viewModel.isServerRunning.collectAsState()

                // TV Remote Back key navigation handler
                BackHandler(enabled = true) {
                    if (showShareQrDialog != null) {
                        viewModel.closeShareQrDialog()
                    } else if (selectedFileForDetail != null) {
                        viewModel.closeFileDetail()
                    } else if (selectedTab == TvTab.EXPLORER) {
                        if (selectedCategory != null) {
                            viewModel.loadDirectory(currentDirectory)
                        } else if (!viewModel.navigateUp()) {
                            selectedTab = TvTab.DASHBOARD
                        }
                    } else if (selectedTab != TvTab.DASHBOARD) {
                        selectedTab = TvTab.DASHBOARD
                    } else {
                        finish()
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val width = maxWidth
                    val isCompact = width < 600.dp
                    val isTablet = width in 600.dp..960.dp

                    Row(modifier = Modifier.fillMaxSize()) {
                        if (isTablet) {
                            BeamNavigationRail(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
                        }

                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .background(MaterialTheme.colorScheme.background),
                            contentWindowInsets = WindowInsets.statusBars,
                            containerColor = MaterialTheme.colorScheme.background,
                            topBar = {
                                if (!isTablet) {
                                    TvTopBar(
                                        selectedTab = selectedTab,
                                        onTabSelected = { selectedTab = it },
                                        serverUrl = serverUrl,
                                        isServerRunning = isServerRunning,
                                        isCompact = isCompact,
                                        onOpenShareQr = { viewModel.openShareQrDialog(serverUrl) }
                                    )
                                }
                            },
                            bottomBar = {
                                if (isCompact) {
                                    BeamBottomNavBar(
                                        selectedTab = selectedTab,
                                        onTabSelected = { selectedTab = it }
                                    )
                                } else if (!isTablet) {
                                    RemoteShortcutBar()
                                }
                            }
                        ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (selectedTab) {
                                TvTab.DASHBOARD -> {
                                    DashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToCategory = { category ->
                                            viewModel.loadCategory(category)
                                            selectedTab = TvTab.EXPLORER
                                        },
                                        onNavigateToTab = { tab -> selectedTab = tab }
                                    )
                                }
                                TvTab.EXPLORER -> {
                                    ExplorerScreen(viewModel = viewModel)
                                }
                                TvTab.WEB_BEAM -> {
                                    WebBeamScreen(viewModel = viewModel)
                                }
                                TvTab.STORAGE -> {
                                    StorageScreen(viewModel = viewModel)
                                }
                                TvTab.SETTINGS -> {
                                    SettingsScreen(viewModel = viewModel)
                                }
                            }

                            // File Details Modal Overlay
                            if (selectedFileForDetail != null) {
                                FileDetailModal(
                                    item = selectedFileForDetail!!,
                                    sha256Hash = sha256Hash,
                                    metadata = selectedFileMetadata,
                                    onDismiss = { viewModel.closeFileDetail() },
                                    onOpen = { fileItem -> viewModel.openFileWithSystem(this@MainActivity, fileItem.file) },
                                    onDelete = { item ->
                                        viewModel.deleteFile(item)
                                        viewModel.closeFileDetail()
                                    },
                                    onShareQr = { fileItem ->
                                        val fileUrl = if (serverUrl.isNotBlank()) {
                                            "${serverUrl.trimEnd('/')}/download?file=${fileItem.name}"
                                        } else {
                                            fileItem.file.absolutePath
                                        }
                                        viewModel.openShareQrDialog(fileUrl)
                                    }
                                )
                            }

                            // Share QR Dialog Overlay
                            if (showShareQrDialog != null) {
                                ShareQrModal(
                                    url = showShareQrDialog!!,
                                    onDismiss = { viewModel.closeShareQrDialog() },
                                    onCopyUrl = { copyUrl ->
                                        val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Beam Share Link", copyUrl)
                                        clipboard?.setPrimaryClip(clip)
                                        viewModel.showToast(getString(R.string.toast_copied_to_clipboard, copyUrl))
                                    }
                                )
                            }

                            // Floating Custom HUD Toast Notification
                            androidx.compose.animation.AnimatedVisibility(
                                visible = toastMessage != null,
                                enter = slideInVertically(initialOffsetY = { 60 }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { 60 }) + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = if (isCompact) 12.dp else 24.dp)
                            ) {
                                toastMessage?.let { msg ->
                                    LaunchedEffect(msg) {
                                        delay(3000)
                                        viewModel.clearToast()
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.5.dp, BeamFocusCyanBright, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 24.dp, vertical = 12.dp)
                                            .testTag("tv_hud_toast_banner")
                                    ) {
                                        Text(
                                            text = msg,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
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
