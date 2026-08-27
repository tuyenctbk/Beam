package com.example.beam.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.BatteryManager
import android.os.Environment
import android.os.PowerManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.beam.data.db.AppDatabase
import com.example.beam.data.model.ActiveTransfer
import com.example.beam.data.model.BatteryState
import com.example.beam.data.model.CleanupScanResult
import com.example.beam.data.model.FileCategory
import com.example.beam.data.model.FileItem
import com.example.beam.data.model.MediaMetadata
import com.example.beam.data.model.StorageInfo
import com.example.beam.data.model.TransferEntity
import com.example.beam.data.model.TransferProgressStatus
import com.example.beam.data.repository.BeamRepository
import com.example.beam.server.BeamWebServer
import com.example.beam.server.NetworkUtils
import com.example.beam.server.QrCodeUtils
import com.example.beam.service.BeamTransferService
import com.example.beam.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

enum class BatchOperation {
    NONE,
    MOVE,
    COPY
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("beam_tv_prefs", Context.MODE_PRIVATE)
    private val db = AppDatabase.getDatabase(application)
    private val repository = BeamRepository(application, db.transferDao())

    val transfers: StateFlow<List<TransferEntity>> = repository.allTransfers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _storageInfo = MutableStateFlow(StorageInfo())
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

    private val _currentDirectory = MutableStateFlow(Environment.getExternalStorageDirectory())
    val currentDirectory: StateFlow<File> = _currentDirectory.asStateFlow()

    private val _fileList = MutableStateFlow<List<FileItem>>(emptyList())
    val fileList: StateFlow<List<FileItem>> = _fileList.asStateFlow()

    private val _selectedCategory = MutableStateFlow<FileCategory?>(null)
    val selectedCategory: StateFlow<FileCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Multi-selection state
    private val _selectedFiles = MutableStateFlow<Set<FileItem>>(emptySet())
    val selectedFiles: StateFlow<Set<FileItem>> = _selectedFiles.asStateFlow()

    // Batch Staging (Move / Copy)
    private val _stagedBatchOp = MutableStateFlow(BatchOperation.NONE)
    val stagedBatchOp: StateFlow<BatchOperation> = _stagedBatchOp.asStateFlow()

    private val _stagedBatchFiles = MutableStateFlow<Set<FileItem>>(emptySet())
    val stagedBatchFiles: StateFlow<Set<FileItem>> = _stagedBatchFiles.asStateFlow()

    // Active Live File Transfers (determinate progress tracking)
    private val _activeTransfers = MutableStateFlow<List<ActiveTransfer>>(emptyList())
    val activeTransfers: StateFlow<List<ActiveTransfer>> = _activeTransfers.asStateFlow()

    // Preferences & Settings
    private val _autoAddToHistory = MutableStateFlow(prefs.getBoolean("pref_auto_add_history", true))
    val autoAddToHistory: StateFlow<Boolean> = _autoAddToHistory.asStateFlow()

    private val initialThemeMode = try {
        ThemeMode.valueOf(prefs.getString("pref_theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
    } catch (e: Exception) {
        ThemeMode.DARK
    }
    private val _themeMode = MutableStateFlow(initialThemeMode)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Server State
    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    private val _serverPort = MutableStateFlow(8080)
    val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    private val _serverUrl = MutableStateFlow("")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap: StateFlow<Bitmap?> = _qrCodeBitmap.asStateFlow()

    // Battery & Low Power State
    private val _batteryState = MutableStateFlow(BatteryState())
    val batteryState: StateFlow<BatteryState> = _batteryState.asStateFlow()

    private val _isLowPowerWarningDismissed = MutableStateFlow(false)
    val isLowPowerWarningDismissed: StateFlow<Boolean> = _isLowPowerWarningDismissed.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { updateBatteryState(it) }
        }
    }

    // UI Overlays & Feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _selectedFileForDetail = MutableStateFlow<FileItem?>(null)
    val selectedFileForDetail: StateFlow<FileItem?> = _selectedFileForDetail.asStateFlow()

    private val _selectedFileMetadata = MutableStateFlow<MediaMetadata?>(null)
    val selectedFileMetadata: StateFlow<MediaMetadata?> = _selectedFileMetadata.asStateFlow()

    private val _sha256Hash = MutableStateFlow<String?>(null)
    val sha256Hash: StateFlow<String?> = _sha256Hash.asStateFlow()

    private val _cleanupScanResult = MutableStateFlow(CleanupScanResult())
    val cleanupScanResult: StateFlow<CleanupScanResult> = _cleanupScanResult.asStateFlow()

    private val _showShareQrDialog = MutableStateFlow<String?>(null)
    val showShareQrDialog: StateFlow<String?> = _showShareQrDialog.asStateFlow()

    private var webServer: BeamWebServer? = null

    init {
        refreshStorageInfo()
        loadDirectory(Environment.getExternalStorageDirectory())
        startBeamServer()
        registerBatteryReceiver()
    }

    fun setAutoAddToHistory(enabled: Boolean) {
        _autoAddToHistory.value = enabled
        prefs.edit().putBoolean("pref_auto_add_history", enabled).apply()
        val context = getApplication<Application>()
        showToast(if (enabled) context.getString(R.string.toast_auto_history_enabled) else context.getString(R.string.toast_auto_history_disabled))
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("pref_theme_mode", mode.name).apply()
    }

    fun refreshStorageInfo() {
        viewModelScope.launch {
            _storageInfo.value = repository.getStorageInfo()
        }
    }

    fun loadDirectory(dir: File) {
        viewModelScope.launch {
            _selectedCategory.value = null
            _currentDirectory.value = dir
            _fileList.value = repository.getFilesForDirectory(dir.absolutePath)
        }
    }

    fun loadCategory(category: FileCategory) {
        viewModelScope.launch {
            _selectedCategory.value = category
            _fileList.value = repository.getFilesForCategory(category)
        }
    }

    fun navigateUp(): Boolean {
        val parent = _currentDirectory.value.parentFile
        val root = Environment.getExternalStorageDirectory()
        return if (parent != null && _currentDirectory.value != root) {
            loadDirectory(parent)
            true
        } else {
            false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Multi-Selection & Batch File Operations ---

    fun toggleFileSelection(item: FileItem) {
        val current = _selectedFiles.value.toMutableSet()
        if (current.contains(item)) {
            current.remove(item)
        } else {
            current.add(item)
        }
        _selectedFiles.value = current
    }

    fun selectAllFiles() {
        _selectedFiles.value = _fileList.value.toSet()
    }

    fun deselectAllFiles() {
        _selectedFiles.value = emptySet()
    }

    fun stageBatchMove(files: Set<FileItem>) {
        if (files.isEmpty()) return
        _stagedBatchOp.value = BatchOperation.MOVE
        _stagedBatchFiles.value = files
        _selectedFiles.value = emptySet()
        showToast("Select target folder and click 'Paste Here' to move ${files.size} items")
    }

    fun stageBatchCopy(files: Set<FileItem>) {
        if (files.isEmpty()) return
        _stagedBatchOp.value = BatchOperation.COPY
        _stagedBatchFiles.value = files
        _selectedFiles.value = emptySet()
        showToast("Select target folder and click 'Paste Here' to copy ${files.size} items")
    }

    fun cancelBatchStaging() {
        _stagedBatchOp.value = BatchOperation.NONE
        _stagedBatchFiles.value = emptySet()
        showToast("Batch operation cancelled")
    }

    fun executeBatchPaste(destinationDir: File) {
        val op = _stagedBatchOp.value
        val items = _stagedBatchFiles.value
        if (op == BatchOperation.NONE || items.isEmpty()) return

        viewModelScope.launch {
            var processed = 0
            for (item in items) {
                val success = when (op) {
                    BatchOperation.MOVE -> repository.moveFile(item.file, destinationDir)
                    BatchOperation.COPY -> repository.copyFile(item.file, destinationDir)
                    BatchOperation.NONE -> false
                }
                if (success) processed++
            }

            val opName = if (op == BatchOperation.MOVE) "Moved" else "Copied"
            showToast("$opName $processed items to ${destinationDir.name.ifEmpty { "Root" }}")

            _stagedBatchOp.value = BatchOperation.NONE
            _stagedBatchFiles.value = emptySet()
            refreshStorageInfo()

            if (_selectedCategory.value != null) {
                loadCategory(_selectedCategory.value!!)
            } else {
                loadDirectory(_currentDirectory.value)
            }
        }
    }

    fun deleteSelectedFiles() {
        viewModelScope.launch {
            val toDelete = _selectedFiles.value
            var deletedCount = 0
            var freedBytes = 0L

            for (item in toDelete) {
                if (repository.deleteFile(item.file)) {
                    deletedCount++
                    freedBytes += item.sizeBytes
                }
            }

            _selectedFiles.value = emptySet()
            showToast("Deleted $deletedCount files (${repository.getStorageInfo().formatBytes(freedBytes)} freed)")
            refreshStorageInfo()
            if (_selectedCategory.value != null) {
                loadCategory(_selectedCategory.value!!)
            } else {
                loadDirectory(_currentDirectory.value)
            }
        }
    }

    fun deleteFile(item: FileItem) {
        viewModelScope.launch {
            if (repository.deleteFile(item.file)) {
                showToast("Deleted ${item.name}")
                refreshStorageInfo()
                if (_selectedCategory.value != null) {
                    loadCategory(_selectedCategory.value!!)
                } else {
                    loadDirectory(_currentDirectory.value)
                }
            }
        }
    }

    // --- File Details & Hash Computation ---

    fun openFileDetail(item: FileItem) {
        _selectedFileForDetail.value = item
        _sha256Hash.value = null
        _selectedFileMetadata.value = null
        if (!item.isDirectory) {
            calculateSha256(item.file)
            viewModelScope.launch(Dispatchers.IO) {
                val metadata = com.example.beam.data.model.MediaMetadataUtils.extractMetadata(
                    getApplication(),
                    item.file,
                    item.category
                )
                _selectedFileMetadata.value = metadata
            }
        }
    }

    fun closeFileDetail() {
        _selectedFileForDetail.value = null
        _sha256Hash.value = null
        _selectedFileMetadata.value = null
    }

    private fun calculateSha256(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val digest = MessageDigest.getInstance("SHA-256")
                file.inputStream().use { inputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                }
                val hashBytes = digest.digest()
                val hexString = hashBytes.joinToString("") { "%02x".format(it) }
                _sha256Hash.value = hexString
            } catch (e: Exception) {
                _sha256Hash.value = "Error computing hash"
            }
        }
    }

    // --- Beam HTTP Transfer Server & Live Progress ---

    fun startBeamServer() {
        if (_isServerRunning.value) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ip = NetworkUtils.getLocalIpAddress(getApplication())
                val availablePort = NetworkUtils.findAvailablePort(startPort = 8080, maxPort = 8120)
                _serverPort.value = availablePort

                val url = if (availablePort == 80) "http://$ip" else "http://$ip:$availablePort"
                _serverUrl.value = url

                val qrBitmap = QrCodeUtils.generateQrCode(url, 320)
                _qrCodeBitmap.value = qrBitmap

                val beamDir = repository.getBeamUploadDir()
                webServer = BeamWebServer(
                    port = availablePort,
                    uploadDir = beamDir,
                    onFileUploadListener = { fileName, filePath, sizeBytes, clientIp ->
                        onFileReceived(fileName, filePath, sizeBytes, clientIp)
                    },
                    onClipboardListener = { text, clientIp ->
                        onClipboardReceived(text, clientIp)
                    },
                    onTransferProgress = { id, fileName, isUpload, bytesTransferred, totalBytes, speed, clientIp ->
                        updateTransferProgress(id, fileName, isUpload, bytesTransferred, totalBytes, speed, clientIp)
                    },
                    onTransferCompleted = { id, fileName, isUpload, sizeBytes, clientIp ->
                        finishTransferProgress(id)
                    }
                )
                webServer?.start()
                _isServerRunning.value = true
                BeamTransferService.start(getApplication(), "Beam Server Active", "Server running at $url")
            } catch (e: Exception) {
                e.printStackTrace()
                _isServerRunning.value = false
            }
        }
    }

    fun stopBeamServer() {
        webServer?.stop()
        webServer = null
        _isServerRunning.value = false
        BeamTransferService.stop(getApplication())
    }

    private fun updateTransferProgress(
        id: String,
        fileName: String,
        isUpload: Boolean,
        bytesTransferred: Long,
        totalBytes: Long,
        speedBytesPerSec: Long,
        clientIp: String
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            val progress = if (totalBytes > 0) (bytesTransferred.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
            val currentList = _activeTransfers.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == id }

            val updatedTransfer = ActiveTransfer(
                id = id,
                fileName = fileName,
                isUpload = isUpload,
                bytesTransferred = bytesTransferred,
                totalBytes = totalBytes,
                progress = progress,
                speedBytesPerSec = speedBytesPerSec,
                clientIp = clientIp,
                status = TransferProgressStatus.TRANSFERRING
            )

            if (index >= 0) {
                currentList[index] = updatedTransfer
            } else {
                currentList.add(updatedTransfer)
            }
            _activeTransfers.value = currentList

            // Update persistent notification with active transfer progress & MB/s speed
            val pct = (progress * 100).toInt()
            val app = getApplication<Application>()
            val speedStr = updatedTransfer.formattedSpeed
            val actionTitle = if (isUpload) app.getString(R.string.notification_receiving, fileName) else app.getString(R.string.notification_sending, fileName)
            val detailText = app.getString(R.string.notification_progress_detail, pct, updatedTransfer.formattedTransferred, updatedTransfer.formattedTotal, speedStr, clientIp)

            BeamTransferService.update(
                app,
                actionTitle,
                detailText,
                pct
            )
        }
    }

    private fun finishTransferProgress(id: String) {
        viewModelScope.launch(Dispatchers.Main) {
            delay(800) // Brief moment so user sees 100% completed
            _activeTransfers.value = _activeTransfers.value.filter { it.id != id }
            if (_activeTransfers.value.isEmpty() && _isServerRunning.value) {
                val app = getApplication<Application>()
                BeamTransferService.update(
                    app,
                    app.getString(R.string.notification_server_active),
                    app.getString(R.string.notification_server_ready),
                    -1
                )
            }
        }
    }

    fun dismissActiveTransfer(transfer: ActiveTransfer) {
        _activeTransfers.value = _activeTransfers.value.filter { it.id != transfer.id }
    }

    private fun onFileReceived(fileName: String, filePath: String, sizeBytes: Long, clientIp: String) {
        viewModelScope.launch {
            if (_autoAddToHistory.value) {
                val transfer = TransferEntity(
                    fileName = fileName,
                    filePath = filePath,
                    sizeBytes = sizeBytes,
                    clientIp = clientIp,
                    isClipboard = false
                )
                repository.recordTransfer(transfer)
            }

            val app = getApplication<Application>()
            val formattedSize = repository.getStorageInfo().formatBytes(sizeBytes)
            showToast(app.getString(R.string.toast_beamed, fileName, formattedSize))
            refreshStorageInfo()

            if (_selectedCategory.value == FileCategory.DOWNLOADS || _currentDirectory.value.name == "Beam") {
                loadDirectory(_currentDirectory.value)
            }
        }
    }

    private fun onClipboardReceived(text: String, clientIp: String) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            if (_autoAddToHistory.value) {
                val transfer = TransferEntity(
                    fileName = app.getString(R.string.remote_clipboard_title),
                    filePath = "",
                    sizeBytes = text.length.toLong(),
                    clientIp = clientIp,
                    isClipboard = true,
                    clipboardText = text
                )
                repository.recordTransfer(transfer)
            }
            showToast(app.getString(R.string.toast_remote_clipboard_received))
        }
    }

    fun deleteTransfer(transfer: TransferEntity) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            repository.deleteTransferById(transfer.id)
            showToast(app.getString(R.string.toast_history_item_removed))
        }
    }

    fun openTransferItem(transfer: TransferEntity, context: android.content.Context) {
        if (transfer.isClipboard) {
            val text = transfer.clipboardText ?: ""
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            if (clipboard != null) {
                val clip = android.content.ClipData.newPlainText(context.getString(R.string.remote_clipboard), text)
                clipboard.setPrimaryClip(clip)
                showToast(context.getString(R.string.toast_copied_to_clipboard, text))
            } else {
                showToast(text)
            }
        } else {
            val file = File(transfer.filePath)
            if (file.exists()) {
                openFileDetail(FileItem(file))
            } else {
                showToast(context.getString(R.string.toast_file_not_found, transfer.filePath))
            }
        }
    }

    fun openFileWithSystem(context: android.content.Context, file: File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val extension = file.extension.lowercase()
            val mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            showToast(context.getString(R.string.toast_no_app_found, file.name))
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearTransferHistory()
            showToast(getApplication<Application>().getString(R.string.toast_history_cleared))
        }
    }

    fun cleanCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val cacheDir = getApplication<Application>().cacheDir
            var freed = 0L
            cacheDir.listFiles()?.forEach {
                freed += it.length()
                it.deleteRecursively()
            }
            refreshStorageInfo()
            showToast(getApplication<Application>().getString(R.string.toast_cache_cleaned, repository.getStorageInfo().formatBytes(freed)))
        }
    }

    fun scanStorageCleanup() {
        viewModelScope.launch(Dispatchers.IO) {
            _cleanupScanResult.value = _cleanupScanResult.value.copy(isScanning = true)
            val context = getApplication<Application>()
            val junkFileList = mutableListOf<File>()
            var cacheSize = 0L
            var tempLogSize = 0L
            var staleDownloadSize = 0L

            // 1. App internal and external cache directories
            val cacheDirs = listOfNotNull(context.cacheDir, context.externalCacheDir)
            cacheDirs.forEach { dir ->
                dir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        cacheSize += file.length()
                        junkFileList.add(file)
                    }
                }
            }

            // 2. Temp, log, bak, part files in download/Beam storage
            val storageRoot = File(context.filesDir, "Beam")
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val searchDirs = listOfNotNull(storageRoot, downloadsDir)

            val junkExtensions = setOf("tmp", "temp", "bak", "log", "part", "crdownload")
            searchDirs.forEach { dir ->
                if (dir.exists() && dir.isDirectory) {
                    dir.walkTopDown().maxDepth(3).forEach { file ->
                        if (file.isFile) {
                            val ext = file.extension.lowercase()
                            if (junkExtensions.contains(ext) || file.name.startsWith("nanohttpd_")) {
                                tempLogSize += file.length()
                                junkFileList.add(file)
                            } else if (file.name.contains(".tmp") || file.name.endsWith(".part")) {
                                staleDownloadSize += file.length()
                                junkFileList.add(file)
                            }
                        }
                    }
                }
            }

            val totalJunk = cacheSize + tempLogSize + staleDownloadSize
            _cleanupScanResult.value = com.example.beam.data.model.CleanupScanResult(
                isScanning = false,
                totalJunkBytes = totalJunk,
                junkFiles = junkFileList,
                appCacheBytes = cacheSize,
                tempLogBytes = tempLogSize,
                staleDownloadBytes = staleDownloadSize,
                scanCompleted = true
            )
        }
    }

    fun performOneClickCleanup() {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val filesToDelete = _cleanupScanResult.value.junkFiles
            var freedBytes = 0L

            filesToDelete.forEach { file ->
                if (file.exists()) {
                    val length = file.length()
                    if (file.delete()) {
                        freedBytes += length
                    }
                }
            }

            // Also purge internal cache dir
            app.cacheDir.listFiles()?.forEach {
                freedBytes += it.length()
                it.deleteRecursively()
            }

            refreshStorageInfo()
            val formattedFreed = repository.getStorageInfo().formatBytes(freedBytes)
            showToast(app.getString(R.string.toast_cleanup_success, formattedFreed))

            _cleanupScanResult.value = com.example.beam.data.model.CleanupScanResult(
                isScanning = false,
                totalJunkBytes = 0L,
                junkFiles = emptyList(),
                scanCompleted = true
            )
        }
    }

    fun openShareQrDialog(url: String) {
        _showShareQrDialog.value = url
    }

    fun closeShareQrDialog() {
        _showShareQrDialog.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // --- Battery & Low Power Warning Logic ---

    private fun registerBatteryReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            }
            val stickyIntent = getApplication<Application>().registerReceiver(batteryReceiver, filter)
            stickyIntent?.let { updateBatteryState(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unregisterBatteryReceiver() {
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateBatteryState(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 100
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val powerManager = getApplication<Application>().getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isPowerSaveMode = powerManager?.isPowerSaveMode ?: false

        _batteryState.value = _batteryState.value.copy(
            batteryPercent = percent,
            isCharging = isCharging,
            isPowerSaveMode = isPowerSaveMode
        )
    }

    fun toggleSimulateLowBattery(simulate: Boolean) {
        _batteryState.value = _batteryState.value.copy(isSimulatedLowBattery = simulate)
        if (simulate) {
            _isLowPowerWarningDismissed.value = false
            showToast("Simulated Low Battery mode enabled (Warning active)")
        } else {
            showToast("Simulated Low Battery mode disabled")
        }
    }

    fun dismissLowPowerWarning() {
        _isLowPowerWarningDismissed.value = true
    }

    override fun onCleared() {
        super.onCleared()
        stopBeamServer()
        unregisterBatteryReceiver()
    }
}
