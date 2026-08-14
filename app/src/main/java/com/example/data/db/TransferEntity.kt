package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val clientIp: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val checksum: String = "",
    val checksumStatus: String = "SHA-256 Verified"
)

@Entity(tableName = "remote_clips")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val clientIp: String,
    val timestamp: Long = System.currentTimeMillis()
)
