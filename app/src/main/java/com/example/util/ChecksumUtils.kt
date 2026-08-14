package com.example.util

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object ChecksumUtils {
    /**
     * Calculates the SHA-256 hash string for a given file.
     */
    fun calculateSha256(file: File): String {
        return try {
            if (!file.exists() || !file.canRead()) return "error"
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(65536)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "error"
        }
    }

    /**
     * Calculates the MD5 hash string for a given file.
     */
    fun calculateMd5(file: File): String {
        return try {
            if (!file.exists() || !file.canRead()) return "error"
            val digest = MessageDigest.getInstance("MD5")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(65536)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "error"
        }
    }
}
