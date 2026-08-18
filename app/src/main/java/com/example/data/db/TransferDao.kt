package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE filePath = :filePath ORDER BY timestamp DESC LIMIT 1")
    suspend fun getTransferByPath(filePath: String): TransferEntity?

    @Query("SELECT * FROM transfers WHERE fileName = :fileName ORDER BY timestamp DESC LIMIT 1")
    suspend fun getTransferByFileName(fileName: String): TransferEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity): Long

    @Query("DELETE FROM transfers WHERE id = :id")
    suspend fun deleteTransfer(id: Long)

    @Query("DELETE FROM transfers")
    suspend fun clearHistory()

    @Query("SELECT * FROM remote_clips ORDER BY timestamp DESC")
    fun getAllClips(): Flow<List<ClipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: ClipEntity): Long

    @Query("DELETE FROM remote_clips WHERE id = :id")
    suspend fun deleteClip(id: Long)
}
