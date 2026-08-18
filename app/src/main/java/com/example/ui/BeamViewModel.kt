package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActiveTransfer
import com.example.data.FileSortOption
import com.example.data.FileCategory
import com.example.data.FileItem
import com.example.data.StorageVolumeInfo
import com.example.data.db.AppDatabase
import com.example.data.db.ClipEntity
import com.example.data.db.TransferEntity
import com.example.server.BeamWebServer
import com.example.ui.components.TvToast
import com.example.ui.components.TvToastType
import com.example.util.ApkParser
import com.example.util.FileOpener
import com.example.util.NetworkType
import com.example.util.NetworkUtils
import com.example.util.QrCodeGenerator
import com.example.util.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import com.example.util.DiscoveredDevice
import com.example.util.NsdHelper

class BeamViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.transferDao()

    // Network Service Discovery (NSD)
    private val nsdHelper = NsdHelper(application)
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = nsdHelper.discoveredDevices

    // Network State Monitor
    private val _networkType = MutableStateFlow(NetworkUtils.getNetworkType(application))
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()

    private val _isLocalNetworkAvailable = MutableStateFlow(NetworkUtils.isLocalNetworkAvailable(application))
    val isLocalNetworkAvailable: StateFlow<Boolean> = _isLocalNetworkAvailable.asStateFlow()

    private val _networkWarning = MutableStateFlow<String?>(null)
    val networkWarning: StateFlow<String?> = _networkWarning.asStateFlow()

    // Server State
    private val _ipAddress = MutableStateFlow("127.0.0.1")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _wifiSsid = MutableStateFlow("Local Wi-Fi")
    val wifiSsid: StateFlow<String> = _wifiSsid.asStateFlow()

    private val _serverPort = MutableStateFlow(8080)
    val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    private val _qrBitmap = MutableStateFlow<ImageBitmap?>(null)
    val qrBitmap: StateFlow<ImageBitmap?> = _qrBitmap.asStateFlow()

    // Transfers
    private val _activeTransfers = MutableStateFlow<Map<String, ActiveTransfer>>(emptyMap())
    val activeTransfers: StateFlow<Map<String, ActiveTransfer>> = _activeTransfers.asStateFlow()

    val transferHistory: StateFlow<List<TransferEntity>> = dao.getAllTransfers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _recentTransferredFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val recentTransferredFiles: StateFlow<List<FileItem>> = _recentTransferredFiles.asStateFlow()

    val remoteClips: StateFlow<List<ClipEntity>> = dao.getAllClips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Storage Management & Low Capacity Detection (< 10%)
    private val _storageVolumes = MutableStateFlow<List<StorageVolumeInfo>>(emptyList())
    val storageVolumes: StateFlow<List<StorageVolumeInfo>> = _storageVolumes.asStateFlow()

    private val _selectedStorageIndex = MutableStateFlow(0)
    val selectedStorageIndex: StateFlow<Int> = _selectedStorageIndex.asStateFlow()

    private val _isLowStorage = MutableStateFlow(false)
    val isLowStorage: StateFlow<Boolean> = _isLowStorage.asStateFlow()

    private val _appCacheSizeBytes = MutableStateFlow(0L)
    val appCacheSizeBytes: StateFlow<Long> = _appCacheSizeBytes.asStateFlow()

    private val _largeFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val largeFiles: StateFlow<List<FileItem>> = _largeFiles.asStateFlow()

    // File Explorer & Batch Selection
    private val _currentDirectory = MutableStateFlow<File>(StorageUtils.getDefaultDownloadDir(application))
    val currentDirectory: StateFlow<File> = _currentDirectory.asStateFlow()

    private val _fileItems = MutableStateFlow<List<FileItem>>(emptyList())
    val fileItems: StateFlow<List<FileItem>> = _fileItems.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FileCategory.ALL)
    val selectedCategory: StateFlow<FileCategory> = _selectedCategory.asStateFlow()

    private val _selectedSortOption = MutableStateFlow(FileSortOption.DATE_DESC)
    val selectedSortOption: StateFlow<FileSortOption> = _selectedSortOption.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isBatchMode = MutableStateFlow(false)
    val isBatchMode: StateFlow<Boolean> = _isBatchMode.asStateFlow()

    private val _selectedFilePaths = MutableStateFlow<Set<String>>(emptySet())
    val selectedFilePaths: StateFlow<Set<String>> = _selectedFilePaths.asStateFlow()

    // TV-Grade Toast Notifications
    private val _tvToast = MutableStateFlow<TvToast?>(null)
    val tvToast: StateFlow<TvToast?> = _tvToast.asStateFlow()

    fun showTvToast(title: String, message: String, type: TvToastType, durationMs: Long = 4500L) {
        _tvToast.value = TvToast(
            id = System.currentTimeMillis(),
            title = title,
            message = message,
            type = type,
            durationMs = durationMs
        )
        _recentNotification.value = message
    }

    fun dismissTvToast() {
        _tvToast.value = null
        _recentNotification.value = null
    }

    // Preferences
    private val prefs = application.getSharedPreferences("beam_app_prefs", Context.MODE_PRIVATE)

    // Onboarding Overlay State
    private val _showOnboardingOverlay = MutableStateFlow(!prefs.getBoolean("onboarding_completed", false))
    val showOnboardingOverlay: StateFlow<Boolean> = _showOnboardingOverlay.asStateFlow()

    fun dismissOnboarding() {
        _showOnboardingOverlay.value = false
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        com.example.util.FirebaseManager.logUserAction("onboarding_completed")
    }

    fun replayOnboarding() {
        _showOnboardingOverlay.value = true
        com.example.util.FirebaseManager.logUserAction("onboarding_replayed")
    }

    // Auto Clean Old Files (>30 Days)
    private val _isAutoCleanOldFilesEnabled = MutableStateFlow(prefs.getBoolean("auto_clean_old_files", false))
    val isAutoCleanOldFilesEnabled: StateFlow<Boolean> = _isAutoCleanOldFilesEnabled.asStateFlow()

    fun toggleAutoCleanOldFiles() {
        val next = !_isAutoCleanOldFilesEnabled.value
        _isAutoCleanOldFilesEnabled.value = next
        prefs.edit().putBoolean("auto_clean_old_files", next).apply()
        com.example.util.FirebaseManager.logUserAction("toggle_auto_clean_files", next.toString())
        if (next) {
            cleanOldFiles(30)
        }
    }

    fun cleanOldFiles(daysThreshold: Int = 30) {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = _currentDirectory.value
            if (!dir.exists() || !dir.isDirectory) return@launch
            val now = System.currentTimeMillis()
            val thresholdMs = daysThreshold.toLong() * 24 * 60 * 60 * 1000L
            var deletedCount = 0
            var freedBytes = 0L

            dir.listFiles()?.forEach { file ->
                if (!file.isDirectory && (now - file.lastModified() > thresholdMs)) {
                    val size = file.length()
                    if (file.delete()) {
                        deletedCount++
                        freedBytes += size
                    }
                }
            }

            if (deletedCount > 0) {
                val freedMb = freedBytes / (1024 * 1024)
                _recentNotification.value = "Cleaned $deletedCount old file(s) (>30 days), freed $freedMb MB"
                com.example.util.FirebaseManager.logUserAction("storage_cleaned_old_files", "count_$deletedCount")
                refreshFileList()
            } else {
                _recentNotification.value = "No files older than 30 days found"
            }
        }
    }

    // Smart Share & Rate Dialogs
    private val _showShareAppDialog = MutableStateFlow(false)
    val showShareAppDialog: StateFlow<Boolean> = _showShareAppDialog.asStateFlow()

    private val _showRateAppDialog = MutableStateFlow(false)
    val showRateAppDialog: StateFlow<Boolean> = _showRateAppDialog.asStateFlow()

    fun dismissShareDialog() {
        _showShareAppDialog.value = false
    }

    fun neverShowShareDialog() {
        _showShareAppDialog.value = false
        prefs.edit().putBoolean("share_never_show", true).apply()
    }

    fun triggerShareDialogManually() {
        _showShareAppDialog.value = true
    }

    fun dismissRateDialog() {
        _showRateAppDialog.value = false
    }

    fun neverShowRateDialog() {
        _showRateAppDialog.value = false
        prefs.edit().putBoolean("rate_never_show", true).apply()
    }

    fun submitRating(stars: Int) {
        _showRateAppDialog.value = false
        prefs.edit().putBoolean("rate_never_show", true).apply()
        _recentNotification.value = "Thank you for rating Beam $stars stars!"
    }

    fun triggerRateDialogManually() {
        _showRateAppDialog.value = true
    }

    private fun checkSmartPromptsAfterTransfer() {
        val count = prefs.getInt("successful_transfers_count", 0) + 1
        prefs.edit().putInt("successful_transfers_count", count).apply()

        val neverShare = prefs.getBoolean("share_never_show", false)
        val neverRate = prefs.getBoolean("rate_never_show", false)

        if (count == 2 && !neverShare) {
            _showShareAppDialog.value = true
        } else if (count == 3 && !neverRate) {
            _showRateAppDialog.value = true
        }
    }

    // Power Saver & TV Idle State
    private val _isPowerSaverActive = MutableStateFlow(false)
    val isPowerSaverActive: StateFlow<Boolean> = _isPowerSaverActive.asStateFlow()

    private val _isHighContrastDark = MutableStateFlow(false)
    val isHighContrastDark: StateFlow<Boolean> = _isHighContrastDark.asStateFlow()

    fun toggleHighContrastDark() {
        _isHighContrastDark.value = !_isHighContrastDark.value
        _recentNotification.value = if (_isHighContrastDark.value) "High-Contrast Dark Mode Enabled" else "Light Theme Enabled"
    }

    private var lastUserActivityTime = System.currentTimeMillis()

    fun registerUserActivity() {
        lastUserActivityTime = System.currentTimeMillis()
        if (_isPowerSaverActive.value) {
            _isPowerSaverActive.value = false
            _recentNotification.value = "Awakened from Power Saver mode"
        }
    }

    fun togglePowerSaverManual() {
        _isPowerSaverActive.value = !_isPowerSaverActive.value
        if (!_isPowerSaverActive.value) {
            lastUserActivityTime = System.currentTimeMillis()
        }
    }

    private var webServer: BeamWebServer? = null

    // Remote Config Dynamic State
    private val _remoteWelcomeMessage = MutableStateFlow("Wireless File Stream Active")
    val remoteWelcomeMessage: StateFlow<String> = _remoteWelcomeMessage.asStateFlow()

    private var maxIdleMinutes: Long = 5L

    init {
        // Initialize Firebase Services (Analytics, Performance, Crashlytics, Remote Config)
        com.example.util.FirebaseManager.initialize(application)

        // Fetch & Sync Remote Config Defaults
        com.example.util.FirebaseManager.fetchAndActivateRemoteConfig { success ->
            if (success) {
                _remoteWelcomeMessage.value = com.example.util.FirebaseManager.getRemoteConfigString(
                    "welcome_banner_message",
                    "Wireless File Stream Active"
                )
                maxIdleMinutes = com.example.util.FirebaseManager.getRemoteConfigLong("max_idle_minutes", 5L)
                val autoCleanDays = com.example.util.FirebaseManager.getRemoteConfigLong("auto_clean_days", 30L).toInt()
                
                com.example.util.FirebaseManager.setCrashlyticsKey("remote_config_active", true)
                com.example.util.FirebaseManager.setCrashlyticsKey("max_idle_minutes", maxIdleMinutes.toString())
                
                if (_isAutoCleanOldFilesEnabled.value) {
                    cleanOldFiles(autoCleanDays)
                }
            }
        }

        refreshNetworkInfo()
        refreshStorageVolumes()
        refreshFileList()
        startServer()

        if (_isAutoCleanOldFilesEnabled.value) {
            cleanOldFiles(30)
        }

        // TV Idle Monitoring loop for Power Saver Mode using Remote Config idle threshold
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(10000L)
                val idleMs = System.currentTimeMillis() - lastUserActivityTime
                val thresholdMs = maxIdleMinutes * 60 * 1000L
                if (idleMs >= thresholdMs && !_isPowerSaverActive.value) {
                    _isPowerSaverActive.value = true
                    com.example.util.FirebaseManager.logUserAction("power_saver_activated", "idle_${maxIdleMinutes}_min")
                    com.example.util.FirebaseManager.setCrashlyticsKey("power_saver_active", true)
                }
            }
        }

        viewModelScope.launch {
            transferHistory.collect { entities ->
                refreshRecentTransferredFiles(entities)
            }
        }

        viewModelScope.launch {
            var wasLocalNetworkOk: Boolean? = null
            NetworkUtils.observeNetworkState(application).collect { netType ->
                _networkType.value = netType
                val isLocalOk = (netType == NetworkType.WIFI || netType == NetworkType.ETHERNET)
                _isLocalNetworkAvailable.value = isLocalOk

                if (wasLocalNetworkOk == true && !isLocalOk) {
                    showTvToast(
                        title = "Connection Lost",
                        message = "Local Wi-Fi disconnected. Beam server paused.",
                        type = TvToastType.NETWORK
                    )
                }
                wasLocalNetworkOk = isLocalOk

                if (!isLocalOk) {
                    _networkWarning.value = if (netType == NetworkType.CELLULAR) {
                        "Connected via Cellular Data. Web transfers require Wi-Fi or Local Ethernet."
                    } else {
                        "No Wi-Fi or Ethernet detected. Connect TV to local network to start receiving files."
                    }
                } else {
                    _networkWarning.value = null
                }

                refreshNetworkInfo()
                if (isLocalOk) {
                    startServer()
                }
            }
        }
    }

    fun refreshNetworkInfo() {
        val ip = NetworkUtils.getLocalIpAddress(getApplication())
        val ssid = NetworkUtils.getWifiSsid(getApplication())
        _ipAddress.value = ip
        _wifiSsid.value = ssid

        val url = "http://$ip:${_serverPort.value}"
        viewModelScope.launch(Dispatchers.Default) {
            val bitmap = QrCodeGenerator.generateQrBitmap(url)
            _qrBitmap.value = bitmap
        }
    }

    fun startServer() {
        if (webServer?.isRunning == true) return
        val context: Context = getApplication()

        webServer = BeamWebServer(
            context = context,
            port = _serverPort.value,
            onTransferStarted = { transfer ->
                val current = _activeTransfers.value.toMutableMap()
                current[transfer.id] = transfer
                _activeTransfers.value = current
            },
            onTransferProgress = { id, receivedBytes ->
                val current = _activeTransfers.value.toMutableMap()
                current[id]?.let { existing ->
                    current[id] = existing.copy(receivedBytes = receivedBytes)
                    _activeTransfers.value = current
                }
            },
            onTransferCompleted = { file, clientIp, checksum, checksumStatus ->
                viewModelScope.launch {
                    val current = _activeTransfers.value.toMutableMap()
                    current.values.find { it.fileName == file.name }?.let { active ->
                        current.remove(active.id)
                        _activeTransfers.value = current
                    }

                    // Save to Room DB
                    val entity = TransferEntity(
                        fileName = file.name,
                        filePath = file.absolutePath,
                        fileSize = file.length(),
                        clientIp = clientIp,
                        category = FileItem.determineCategory(file).name,
                        checksum = checksum,
                        checksumStatus = checksumStatus
                    )
                    dao.insertTransfer(entity)

                    val isCorrupted = checksumStatus.contains("CORRUPTED", ignoreCase = true)
                    if (isCorrupted) {
                        showTvToast(
                            title = "Transfer Failed",
                            message = "Integrity mismatch detected for ${file.name}",
                            type = TvToastType.ERROR
                        )
                    } else {
                        val sizeFormatted = StorageUtils.formatBytes(file.length())
                        val statusTag = if (checksumStatus.contains("Verified", ignoreCase = true)) "SHA-256 Verified" else "Verified"
                        showTvToast(
                            title = "Transfer Complete",
                            message = "${file.name} ($statusTag • $sizeFormatted)",
                            type = TvToastType.TRANSFER
                        )
                    }

                    checkSmartPromptsAfterTransfer()
                    refreshFileList()
                    refreshStorageVolumes()
                }
            },
            onRemoteClipReceived = { text, clientIp ->
                viewModelScope.launch {
                    val entity = ClipEntity(text = text, clientIp = clientIp)
                    dao.insertClip(entity)
                    showTvToast(
                        title = "Clipboard Beamed",
                        message = "Text from $clientIp copied to TV clipboard",
                        type = TvToastType.SUCCESS
                    )
                    // Also auto copy to TV clipboard for seamless input
                    withContext(Dispatchers.Main) {
                        FileOpener.copyToClipboard(getApplication(), text)
                    }
                }
            },
            getTargetDirectory = { _currentDirectory.value }
        )

        webServer?.start()
        _isServerRunning.value = true

        try {
            nsdHelper.registerService(_serverPort.value)
            nsdHelper.startDiscovery()
        } catch (_: Exception) {}
    }

    fun stopServer() {
        webServer?.stop()
        _isServerRunning.value = false
        nsdHelper.stopDiscovery()
        nsdHelper.unregisterService()
    }

    fun refreshStorageVolumes() {
        viewModelScope.launch(Dispatchers.IO) {
            val volumes = StorageUtils.getStorageVolumes(getApplication())
            _storageVolumes.value = volumes

            // Detect low capacity (< 10% free on internal or primary volume)
            val primary = volumes.firstOrNull { !it.isUsb } ?: volumes.firstOrNull()
            if (primary != null && primary.totalBytes > 0L) {
                val ratio = primary.freeBytes.toDouble() / primary.totalBytes.toDouble()
                _isLowStorage.value = (ratio < 0.10)
            } else {
                _isLowStorage.value = false
            }

            refreshCacheAndLargeFiles()
        }
    }

    fun refreshCacheAndLargeFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _appCacheSizeBytes.value = StorageUtils.getAppCacheSize(getApplication())
            val downloadDir = StorageUtils.getDefaultDownloadDir(getApplication())
            val largest = StorageUtils.getLargestFiles(downloadDir, limit = 8).map { file ->
                if (file.extension.equals("apk", ignoreCase = true)) {
                    ApkParser.parseApk(getApplication(), file)
                } else {
                    FileItem(file = file)
                }
            }
            _largeFiles.value = largest
        }
    }

    fun clearAppCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val freedBytes = StorageUtils.clearAppCache(getApplication())
            val freedFormatted = StorageUtils.formatBytes(freedBytes)
            refreshStorageVolumes()
            showTvToast(
                title = "Storage Cleaned",
                message = "Cache cleared! Freed $freedFormatted of temporary data.",
                type = TvToastType.SUCCESS
            )
            com.example.util.FirebaseManager.logUserAction("cache_cleared_freed", freedFormatted)
        }
    }

    suspend fun getTransferRecord(filePath: String): TransferEntity? {
        return withContext(Dispatchers.IO) {
            dao.getTransferByPath(filePath) ?: dao.getTransferByFileName(File(filePath).name)
        }
    }

    fun selectStorageVolume(index: Int) {
        val volumes = _storageVolumes.value
        if (index in volumes.indices) {
            _selectedStorageIndex.value = index
            val targetDir = File(volumes[index].path, "Download")
            if (!targetDir.exists()) targetDir.mkdirs()
            _currentDirectory.value = targetDir
            refreshFileList()
        }
    }

    fun setCategory(category: FileCategory) {
        _selectedCategory.value = category
        refreshFileList()
    }

    fun setSortOption(option: FileSortOption) {
        _selectedSortOption.value = option
        refreshFileList()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        refreshFileList()
    }

    fun navigateToDirectory(directory: File) {
        if (directory.isDirectory && directory.canRead()) {
            _currentDirectory.value = directory
            refreshFileList()
        }
    }

    fun navigateUp() {
        val parent = _currentDirectory.value.parentFile
        if (parent != null && parent.canRead()) {
            _currentDirectory.value = parent
            refreshFileList()
        }
    }

    fun refreshFileList() {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = _currentDirectory.value
            if (!dir.exists()) dir.mkdirs()

            val files = dir.listFiles() ?: arrayOf()
            val parsedFiles = files.map { file ->
                if (file.extension.equals("apk", ignoreCase = true)) {
                    ApkParser.parseApk(getApplication(), file)
                } else {
                    FileItem(file = file)
                }
            }

            // Filter by category and search
            val category = _selectedCategory.value
            val query = _searchQuery.value.trim().lowercase()
            val sortOption = _selectedSortOption.value

            val filtered = parsedFiles.filter { item ->
                val matchesCategory = when (category) {
                    FileCategory.ALL -> true
                    FileCategory.DOWNLOADS -> true
                    else -> item.category == category
                }
                val matchesQuery = query.isEmpty() ||
                        item.name.lowercase().contains(query) ||
                        (item.apkAppName?.lowercase()?.contains(query) == true)

                matchesCategory && matchesQuery
            }

            // Directories always stay on top, then apply sort option
            val comparator: Comparator<FileItem> = when (sortOption) {
                FileSortOption.DATE_DESC -> compareByDescending<FileItem> { it.isDirectory }.thenByDescending { it.lastModified }
                FileSortOption.DATE_ASC -> compareByDescending<FileItem> { it.isDirectory }.thenBy { it.lastModified }
                FileSortOption.NAME_ASC -> compareByDescending<FileItem> { it.isDirectory }.thenBy { (it.apkAppName ?: it.name).lowercase() }
                FileSortOption.NAME_DESC -> compareByDescending<FileItem> { it.isDirectory }.thenByDescending { (it.apkAppName ?: it.name).lowercase() }
                FileSortOption.SIZE_DESC -> compareByDescending<FileItem> { it.isDirectory }.thenByDescending { it.sizeBytes }
                FileSortOption.SIZE_ASC -> compareByDescending<FileItem> { it.isDirectory }.thenBy { it.sizeBytes }
            }

            _fileItems.value = filtered.sortedWith(comparator)
            refreshStorageVolumes()
        }
    }

    fun renameFile(fileItem: FileItem, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val source = fileItem.file
            if (source.exists()) {
                val destination = File(source.parentFile, newName)
                if (!destination.exists() && source.renameTo(destination)) {
                    showTvToast(
                        title = "File Renamed",
                        message = "${fileItem.name} renamed to $newName",
                        type = TvToastType.INFO
                    )
                    refreshFileList()
                } else {
                    showTvToast(
                        title = "Rename Failed",
                        message = "Could not rename ${fileItem.name}",
                        type = TvToastType.ERROR
                    )
                }
            }
        }
    }

    fun moveFile(fileItem: FileItem, targetDirectory: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val source = fileItem.file
            if (source.exists() && targetDirectory.exists()) {
                val destination = File(targetDirectory, source.name)
                if (source.renameTo(destination)) {
                    showTvToast(
                        title = "File Moved",
                        message = "${fileItem.name} relocated to ${targetDirectory.name}",
                        type = TvToastType.INFO
                    )
                    refreshFileList()
                } else {
                    // Try copy and delete fallback
                    try {
                        source.copyTo(destination, overwrite = true)
                        source.delete()
                        showTvToast(
                            title = "File Moved",
                            message = "${fileItem.name} relocated to ${targetDirectory.name}",
                            type = TvToastType.INFO
                        )
                        refreshFileList()
                    } catch (_: Exception) {
                        showTvToast(
                            title = "Move Failed",
                            message = "Could not move ${fileItem.name}",
                            type = TvToastType.ERROR
                        )
                    }
                }
            }
        }
    }

    fun deleteFile(fileItem: FileItem) {
        viewModelScope.launch(Dispatchers.IO) {
            if (fileItem.file.exists()) {
                val success = if (fileItem.isDirectory) {
                    fileItem.file.deleteRecursively()
                } else {
                    fileItem.file.delete()
                }
                if (success) {
                    showTvToast(
                        title = "File Deleted",
                        message = "${fileItem.name} removed permanently",
                        type = TvToastType.DELETE
                    )
                    refreshFileList()
                } else {
                    showTvToast(
                        title = "Delete Failed",
                        message = "Could not delete ${fileItem.name}",
                        type = TvToastType.ERROR
                    )
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearHistory()
            showTvToast(
                title = "Log Cleared",
                message = "Transfer history cleared",
                type = TvToastType.INFO
            )
        }
    }

    fun deleteClip(id: Long) {
        viewModelScope.launch {
            dao.deleteClip(id)
        }
    }

    fun recordFileAccess(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (file.exists() && file.isFile) {
                    val category = FileItem.determineCategory(file)
                    val entity = TransferEntity(
                        fileName = file.name,
                        filePath = file.absolutePath,
                        fileSize = file.length(),
                        clientIp = "TV Access",
                        category = category.name,
                        timestamp = System.currentTimeMillis(),
                        checksumStatus = "Accessed"
                    )
                    dao.insertTransfer(entity)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun refreshRecentTransferredFiles(entities: List<TransferEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            val downloadDir = StorageUtils.getDefaultDownloadDir(getApplication())
            val recentItems = mutableListOf<FileItem>()
            val takenNames = mutableSetOf<String>()

            for (entity in entities) {
                if (recentItems.size >= 5) break
                var file = File(entity.filePath)
                if (!file.exists()) {
                    file = File(downloadDir, entity.fileName)
                }
                if (file.exists() && file.isFile && !takenNames.contains(file.name)) {
                    takenNames.add(file.name)
                    val item = if (file.extension.equals("apk", ignoreCase = true)) {
                        ApkParser.parseApk(getApplication(), file)
                    } else {
                        FileItem(file = file)
                    }
                    recentItems.add(item)
                }
            }

            // If history is less than 5, fill with latest files in download folder
            if (recentItems.size < 5) {
                val allDownloadFiles = downloadDir.listFiles()?.filter { it.isFile }?.sortedByDescending { it.lastModified() } ?: emptyList()
                for (file in allDownloadFiles) {
                    if (recentItems.size >= 5) break
                    if (!takenNames.contains(file.name)) {
                        takenNames.add(file.name)
                        val item = if (file.extension.equals("apk", ignoreCase = true)) {
                            ApkParser.parseApk(getApplication(), file)
                        } else {
                            FileItem(file = file)
                        }
                        recentItems.add(item)
                    }
                }
            }

            _recentTransferredFiles.value = recentItems
        }
    }

    fun toggleBatchMode() {
        _isBatchMode.value = !_isBatchMode.value
        if (!_isBatchMode.value) {
            _selectedFilePaths.value = emptySet()
        }
    }

    fun toggleFileSelection(path: String) {
        val current = _selectedFilePaths.value.toMutableSet()
        if (current.contains(path)) {
            current.remove(path)
        } else {
            current.add(path)
        }
        _selectedFilePaths.value = current
    }

    fun selectAllInCurrentFolder() {
        val currentFiles = _fileItems.value.map { it.path }.toSet()
        _selectedFilePaths.value = currentFiles
    }

    fun clearSelection() {
        _selectedFilePaths.value = emptySet()
    }

    fun bulkDeleteSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val paths = _selectedFilePaths.value
            var count = 0
            var freedBytes = 0L
            paths.forEach { path ->
                val file = File(path)
                if (file.exists()) {
                    val size = if (file.isDirectory) 0L else file.length()
                    val deleted = if (file.isDirectory) file.deleteRecursively() else file.delete()
                    if (deleted) {
                        count++
                        freedBytes += size
                    }
                }
            }
            _selectedFilePaths.value = emptySet()
            _isBatchMode.value = false
            val freedFormatted = StorageUtils.formatBytes(freedBytes)
            showTvToast(
                title = "Files Deleted",
                message = "Deleted $count files ($freedFormatted freed)",
                type = TvToastType.DELETE
            )
            refreshFileList()
        }
    }

    fun bulkMoveSelected(targetDirectory: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val paths = _selectedFilePaths.value
            var count = 0
            paths.forEach { path ->
                val file = File(path)
                if (file.exists() && targetDirectory.exists()) {
                    val dest = File(targetDirectory, file.name)
                    if (file.renameTo(dest)) {
                        count++
                    } else {
                        try {
                            file.copyTo(dest, overwrite = true)
                            file.delete()
                            count++
                        } catch (_: Exception) {}
                    }
                }
            }
            _selectedFilePaths.value = emptySet()
            _isBatchMode.value = false
            showTvToast(
                title = "Files Relocated",
                message = "Moved $count items to ${targetDirectory.name}",
                type = TvToastType.INFO
            )
            refreshFileList()
        }
    }

    fun dismissNotification() {
        _recentNotification.value = null
        _tvToast.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopServer()
        nsdHelper.tearDown()
    }
}
