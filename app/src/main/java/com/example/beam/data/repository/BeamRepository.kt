package com.example.beam.data.repository

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.example.beam.data.db.TransferDao
import com.example.beam.data.model.FileCategory
import com.example.beam.data.model.FileItem
import com.example.beam.data.model.StorageInfo
import com.example.beam.data.model.TransferEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class BeamRepository(
    private val context: Context,
    private val transferDao: TransferDao
) {
    val allTransfers: Flow<List<TransferEntity>> = transferDao.getAllTransfers()

    fun getBeamUploadDir(): File {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val beamDir = File(downloadDir, "Beam")
        if (!beamDir.exists()) {
            beamDir.mkdirs()
        }
        return beamDir
    }

    suspend fun getStorageInfo(): StorageInfo = withContext(Dispatchers.IO) {
        val path = Environment.getExternalStorageDirectory().path
        val stat = StatFs(path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalBytes = totalBlocks * blockSize
        val freeBytes = availableBlocks * blockSize

        val categorySizes = mutableMapOf<FileCategory, Long>()
        FileCategory.entries.forEach { categorySizes[it] = 0L }

        val rootDir = Environment.getExternalStorageDirectory()
        scanDirectoryForCategorySizes(rootDir, categorySizes, maxDepth = 3, currentDepth = 0)

        StorageInfo(
            totalBytes = totalBytes,
            freeBytes = freeBytes,
            categorySizes = categorySizes,
            storagePath = path
        )
    }

    private fun scanDirectoryForCategorySizes(
        dir: File,
        sizes: MutableMap<FileCategory, Long>,
        maxDepth: Int,
        currentDepth: Int
    ) {
        if (currentDepth >= maxDepth || !dir.exists() || !dir.isDirectory) return
        val files = dir.listFiles() ?: return

        for (file in files) {
            if (file.name.startsWith(".")) continue
            if (file.isDirectory) {
                scanDirectoryForCategorySizes(file, sizes, maxDepth, currentDepth + 1)
            } else {
                val item = FileItem(file)
                val cat = item.category
                val len = file.length()
                sizes[cat] = (sizes[cat] ?: 0L) + len
                sizes[FileCategory.ALL] = (sizes[FileCategory.ALL] ?: 0L) + len
            }
        }
    }

    suspend fun getFilesForDirectory(dirPath: String): List<FileItem> = withContext(Dispatchers.IO) {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) return@withContext emptyList()

        val files = dir.listFiles() ?: return@withContext emptyList()
        files.filter { !it.name.startsWith(".") }
            .map { FileItem(it) }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    suspend fun getFilesForCategory(category: FileCategory): List<FileItem> = withContext(Dispatchers.IO) {
        val rootDir = Environment.getExternalStorageDirectory()
        val resultList = mutableListOf<FileItem>()
        scanCategoryFiles(rootDir, category, resultList, maxDepth = 4, currentDepth = 0)
        resultList.sortedByDescending { it.lastModified }
    }

    private fun scanCategoryFiles(
        dir: File,
        category: FileCategory,
        result: MutableList<FileItem>,
        maxDepth: Int,
        currentDepth: Int
    ) {
        if (currentDepth >= maxDepth || result.size > 200 || !dir.exists() || !dir.isDirectory) return
        val files = dir.listFiles() ?: return

        for (file in files) {
            if (file.name.startsWith(".")) continue
            if (file.isDirectory) {
                scanCategoryFiles(file, category, result, maxDepth, currentDepth + 1)
            } else {
                val item = FileItem(file)
                if (category == FileCategory.ALL || item.category == category) {
                    result.add(item)
                }
            }
        }
    }

    suspend fun recordTransfer(transfer: TransferEntity): Long = withContext(Dispatchers.IO) {
        transferDao.insertTransfer(transfer)
    }

    suspend fun clearTransferHistory() = withContext(Dispatchers.IO) {
        transferDao.clearHistory()
    }

    suspend fun deleteTransferById(id: Long) = withContext(Dispatchers.IO) {
        transferDao.deleteById(id)
    }

    suspend fun deleteFile(file: File): Boolean = withContext(Dispatchers.IO) {
        if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    suspend fun moveFile(source: File, destinationDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!destinationDir.exists()) destinationDir.mkdirs()
            val destFile = File(destinationDir, source.name)
            if (source.renameTo(destFile)) {
                true
            } else {
                if (source.isDirectory) {
                    source.copyRecursively(destFile, overwrite = true)
                } else {
                    source.copyTo(destFile, overwrite = true)
                }
                if (source.isDirectory) source.deleteRecursively() else source.delete()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun copyFile(source: File, destinationDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!destinationDir.exists()) destinationDir.mkdirs()
            var destFile = File(destinationDir, source.name)
            if (destFile.exists()) {
                val nameWithoutExt = source.nameWithoutExtension
                val ext = if (source.extension.isNotEmpty()) ".${source.extension}" else ""
                var counter = 1
                while (destFile.exists()) {
                    destFile = File(destinationDir, "${nameWithoutExt}_copy$counter$ext")
                    counter++
                }
            }
            if (source.isDirectory) {
                source.copyRecursively(destFile, overwrite = true)
            } else {
                source.copyTo(destFile, overwrite = true)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
