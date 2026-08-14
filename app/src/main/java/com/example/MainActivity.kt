package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FileCategory
import com.example.ui.BeamViewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.components.ConnectivityWarningBanner
import com.example.ui.components.NavTab
import com.example.ui.components.NotificationBanner
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.components.OnboardingOverlay
import com.example.ui.components.PowerSaverOverlay
import com.example.ui.components.RateAppDialog
import com.example.ui.components.ShareAppDialog
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.AboutModal
import com.example.ui.screens.ExplorerScreen
import com.example.ui.screens.HelpScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.RemoteInputScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StorageScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.theme.BeamTheme
import com.example.util.FileOpener

class MainActivity : ComponentActivity() {

    private val viewModel: BeamViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshFileList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase services and trace startup performance
        com.example.util.FirebaseManager.initialize(applicationContext)
        val startupTrace = com.example.util.FirebaseManager.startPerformanceTrace("app_startup_time")

        requestStoragePermissions()

        setContent {
            val isHighContrastDark by viewModel.isHighContrastDark.collectAsStateWithLifecycle()
            
            androidx.compose.runtime.LaunchedEffect(Unit) {
                com.example.util.FirebaseManager.stopPerformanceTrace(startupTrace)
            }

            BeamTheme(isHighContrastDark = isHighContrastDark) {
                MainAppScreen(viewModel)
            }
        }
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = android.net.Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (_: Exception) {}
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshNetworkInfo()
        viewModel.refreshStorageVolumes()
        viewModel.refreshFileList()
    }
}

@Composable
fun MainAppScreen(viewModel: BeamViewModel) {
    val context = LocalContext.current

    val ipAddress by viewModel.ipAddress.collectAsStateWithLifecycle()
    val wifiSsid by viewModel.wifiSsid.collectAsStateWithLifecycle()
    val serverPort by viewModel.serverPort.collectAsStateWithLifecycle()
    val isServerRunning by viewModel.isServerRunning.collectAsStateWithLifecycle()
    val qrBitmap by viewModel.qrBitmap.collectAsStateWithLifecycle()

    val activeTransfers by viewModel.activeTransfers.collectAsStateWithLifecycle()
    val transferHistory by viewModel.transferHistory.collectAsStateWithLifecycle()
    val remoteClips by viewModel.remoteClips.collectAsStateWithLifecycle()

    val storageVolumes by viewModel.storageVolumes.collectAsStateWithLifecycle()
    val selectedStorageIndex by viewModel.selectedStorageIndex.collectAsStateWithLifecycle()

    val currentDirectory by viewModel.currentDirectory.collectAsStateWithLifecycle()
    val fileItems by viewModel.fileItems.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedSortOption by viewModel.selectedSortOption.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isBatchMode by viewModel.isBatchMode.collectAsStateWithLifecycle()
    val selectedFilePaths by viewModel.selectedFilePaths.collectAsStateWithLifecycle()
    val recentTransferredFiles by viewModel.recentTransferredFiles.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()
    val notificationMessage by viewModel.recentNotification.collectAsStateWithLifecycle()
    val networkWarning by viewModel.networkWarning.collectAsStateWithLifecycle()
    val isLocalNetworkAvailable by viewModel.isLocalNetworkAvailable.collectAsStateWithLifecycle()
    val isPowerSaverActive by viewModel.isPowerSaverActive.collectAsStateWithLifecycle()
    val isHighContrastDark by viewModel.isHighContrastDark.collectAsStateWithLifecycle()
    val showOnboardingOverlay by viewModel.showOnboardingOverlay.collectAsStateWithLifecycle()
    val isAutoCleanOldFilesEnabled by viewModel.isAutoCleanOldFilesEnabled.collectAsStateWithLifecycle()
    val showShareAppDialog by viewModel.showShareAppDialog.collectAsStateWithLifecycle()
    val showRateAppDialog by viewModel.showRateAppDialog.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(NavTab.BEAM) }
    var showAboutModal by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(activeTab) {
        com.example.util.FirebaseManager.logScreenView("Tab_${activeTab.name}")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        viewModel.registerUserActivity()
                    }
                }
            }
    ) {
        Scaffold(
            topBar = {
                TopHeaderBar(
                    isServerRunning = isServerRunning,
                    wifiSsid = wifiSsid,
                    onRefresh = {
                        viewModel.refreshNetworkInfo()
                        viewModel.refreshStorageVolumes()
                        viewModel.refreshFileList()
                    },
                    onPowerSaverToggle = { viewModel.togglePowerSaverManual() },
                    onInfoClick = { showAboutModal = true }
                )
            },
        bottomBar = {
            BottomNavBar(
                selectedTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Network Connectivity Warning Banner
                ConnectivityWarningBanner(warningMessage = networkWarning)

                // Toast notification banner for real-time events
                NotificationBanner(
                    message = notificationMessage,
                    onDismiss = { viewModel.dismissNotification() }
                )

                // Active Tab Content
                when (activeTab) {
                    NavTab.BEAM -> {
                        TransferScreen(
                            wifiSsid = wifiSsid,
                            ipAddress = ipAddress,
                            port = serverPort,
                            qrBitmap = qrBitmap,
                            activeTransfers = activeTransfers,
                            recentFiles = recentTransferredFiles,
                            discoveredDevices = discoveredDevices,
                            storageVolumes = storageVolumes,
                            isLocalNetworkAvailable = isLocalNetworkAvailable,
                            onTabSelected = { activeTab = it },
                            onOpenFile = { item -> FileOpener.openFile(context, item.file) }
                        )
                    }

                    NavTab.FILES -> {
                        ExplorerScreen(
                            fileItems = fileItems,
                            currentDirectory = currentDirectory,
                            selectedCategory = selectedCategory,
                            selectedSortOption = selectedSortOption,
                            searchQuery = searchQuery,
                            storageVolumes = storageVolumes,
                            isBatchMode = isBatchMode,
                            selectedFilePaths = selectedFilePaths,
                            onCategorySelected = { viewModel.setCategory(it) },
                            onSortSelected = { viewModel.setSortOption(it) },
                            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                            onNavigateUp = { viewModel.navigateUp() },
                            onOpenItem = { item ->
                                if (item.isDirectory) {
                                    viewModel.navigateToDirectory(item.file)
                                } else {
                                    FileOpener.openFile(context, item.file)
                                }
                            },
                            onDeleteItem = { viewModel.deleteFile(it) },
                            onRenameItem = { item, name -> viewModel.renameFile(item, name) },
                            onMoveItem = { item, target -> viewModel.moveFile(item, target) },
                            onToggleBatchMode = { viewModel.toggleBatchMode() },
                            onToggleFileSelection = { viewModel.toggleFileSelection(it) },
                            onSelectAll = { viewModel.selectAllInCurrentFolder() },
                            onClearSelection = { viewModel.clearSelection() },
                            onBulkDelete = { viewModel.bulkDeleteSelected() },
                            onBulkMove = { target -> viewModel.bulkMoveSelected(target) }
                        )
                    }

                    NavTab.APKS -> {
                        ExplorerScreen(
                            fileItems = fileItems,
                            currentDirectory = currentDirectory,
                            selectedCategory = FileCategory.APKS,
                            selectedSortOption = selectedSortOption,
                            searchQuery = searchQuery,
                            storageVolumes = storageVolumes,
                            isBatchMode = isBatchMode,
                            selectedFilePaths = selectedFilePaths,
                            onCategorySelected = { viewModel.setCategory(it) },
                            onSortSelected = { viewModel.setSortOption(it) },
                            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                            onNavigateUp = { viewModel.navigateUp() },
                            onOpenItem = { item -> FileOpener.openFile(context, item.file) },
                            onDeleteItem = { viewModel.deleteFile(it) },
                            onRenameItem = { item, name -> viewModel.renameFile(item, name) },
                            onMoveItem = { item, target -> viewModel.moveFile(item, target) },
                            onToggleBatchMode = { viewModel.toggleBatchMode() },
                            onToggleFileSelection = { viewModel.toggleFileSelection(it) },
                            onSelectAll = { viewModel.selectAllInCurrentFolder() },
                            onClearSelection = { viewModel.clearSelection() },
                            onBulkDelete = { viewModel.bulkDeleteSelected() },
                            onBulkMove = { target -> viewModel.bulkMoveSelected(target) }
                        )
                    }

                    NavTab.REMOTE -> {
                        RemoteInputScreen(
                            remoteClips = remoteClips,
                            onDeleteClip = { viewModel.deleteClip(it) }
                        )
                    }

                    NavTab.STORAGE -> {
                        StorageScreen(
                            storageVolumes = storageVolumes,
                            selectedIndex = selectedStorageIndex,
                            onSelectStorageIndex = { viewModel.selectStorageVolume(it) }
                        )
                    }

                    NavTab.HISTORY -> {
                        HistoryScreen(
                            transferHistory = transferHistory,
                            onClearHistory = { viewModel.clearHistory() }
                        )
                    }

                    NavTab.HELP -> {
                        HelpScreen(
                            ipAddress = ipAddress,
                            port = serverPort,
                            wifiSsid = wifiSsid,
                            onNavigateToBeam = { activeTab = NavTab.BEAM }
                        )
                    }

                    NavTab.SETTINGS -> {
                        SettingsScreen(
                            isHighContrastDark = isHighContrastDark,
                            isPowerSaverActive = isPowerSaverActive,
                            isAutoCleanOldFilesEnabled = isAutoCleanOldFilesEnabled,
                            serverPort = serverPort,
                            downloadDirName = currentDirectory.name,
                            onToggleHighContrastDark = { viewModel.toggleHighContrastDark() },
                            onTogglePowerSaver = { viewModel.togglePowerSaverManual() },
                            onToggleAutoCleanOldFiles = { viewModel.toggleAutoCleanOldFiles() },
                            onCleanOldFilesNow = { viewModel.cleanOldFiles(30) },
                            onReplayOnboarding = { viewModel.replayOnboarding() },
                            onTriggerShareDialog = { viewModel.triggerShareDialogManually() },
                            onTriggerRateDialog = { viewModel.triggerRateDialogManually() }
                        )
                    }
                }
            }
        }
    }

    if (showAboutModal) {
        AboutModal(onDismiss = { showAboutModal = false })
    }

    // Onboarding Quick Start Overlay
    OnboardingOverlay(
        isVisible = showOnboardingOverlay,
        onDismiss = { viewModel.dismissOnboarding() }
    )

    // Smart Share App Dialog
    ShareAppDialog(
        isVisible = showShareAppDialog,
        ipAddress = ipAddress,
        port = serverPort,
        onCopyLink = {
            FileOpener.copyToClipboard(context, "http://$ipAddress:$serverPort")
            viewModel.dismissShareDialog()
        },
        onDismiss = { viewModel.dismissShareDialog() },
        onDontShowAgain = { viewModel.neverShowShareDialog() }
    )

    // Smart Rate App Dialog
    RateAppDialog(
        isVisible = showRateAppDialog,
        onRateSubmitted = { stars -> viewModel.submitRating(stars) },
        onDismiss = { viewModel.dismissRateDialog() },
        onDontShowAgain = { viewModel.neverShowRateDialog() }
    )

    // TV Power Saver Overlay
    PowerSaverOverlay(
        isActive = isPowerSaverActive,
        onAwaken = { viewModel.registerUserActivity() }
    )
}
}
