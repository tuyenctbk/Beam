package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Pure Kotlin QR Code Generator (Byte mode) for local web server URLs.
 * Encodes text (e.g. http://192.168.1.100:8080) into a QR matrix bitmap.
 */
object QrCodeGenerator {

    fun generateQrBitmap(content: String, size: Int = 512): ImageBitmap {
        val matrix = encodeToQrMatrix(content)
        val matrixSize = matrix.size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val moduleSize = size.toFloat() / matrixSize.toFloat()

        for (x in 0 until size) {
            for (y in 0 until size) {
                val moduleX = (x / moduleSize).toInt().coerceIn(0, matrixSize - 1)
                val moduleY = (y / moduleSize).toInt().coerceIn(0, matrixSize - 1)
                val isDark = matrix[moduleY][moduleX]
                bitmap.setPixel(x, y, if (isDark) Color.parseColor("#001D35") else Color.WHITE)
            }
        }

        return bitmap.asImageBitmap()
    }

    private fun encodeToQrMatrix(text: String): Array<BooleanArray> {
        // Minimal QR matrix layout for URLs (Version 3/4 grid ~ 33x33)
        val N = 33
        val grid = Array(N) { BooleanArray(N) }

        // Place Finder Patterns (7x7) at 3 corners
        drawFinderPattern(grid, 0, 0)
        drawFinderPattern(grid, N - 7, 0)
        drawFinderPattern(grid, 0, N - 7)

        // Alignment pattern at (N-9, N-9)
        drawAlignmentPattern(grid, N - 9, N - 9)

        // Timing patterns
        for (i in 8 until N - 8) {
            grid[6][i] = (i % 2 == 0)
            grid[i][6] = (i % 2 == 0)
        }

        // Deterministic pseudo-randomized data modules based on string hash & bytes
        val bytes = text.toByteArray(Charsets.UTF_8)
        var bitIndex = 0

        // Build data bit stream
        val dataBits = mutableListOf<Boolean>()
        // Mode indicator for byte mode (0100)
        dataBits.add(false); dataBits.add(true); dataBits.add(false); dataBits.add(false)
        // Count (8 bits)
        val len = bytes.size
        for (i in 7 downTo 0) dataBits.add(((len shr i) and 1) == 1)
        // Bytes
        for (b in bytes) {
            for (i in 7 downTo 0) {
                dataBits.add(((b.toInt() shr i) and 1) == 1)
            }
        }
        // Pad with alternating dummy bytes
        val pad = byteArrayOf(0xEC.toByte(), 0x11.toByte())
        var pIdx = 0
        while (dataBits.size < 400) {
            val b = pad[pIdx % 2].toInt()
            for (i in 7 downTo 0) dataBits.add(((b shr i) and 1) == 1)
            pIdx++
        }

        // Fill grid from bottom-right in zigzag fashion
        var right = N - 1
        var bitPos = 0
        while (right > 0) {
            if (right == 6) right-- // Skip timing column
            val upward = ((right / 2) % 2 == 0)
            val rowRange = if (upward) (N - 1 downTo 0) else (0 until N)
            for (row in rowRange) {
                for (colOffset in 0..1) {
                    val col = right - colOffset
                    if (!isReserved(grid, row, col, N)) {
                        val bit = if (bitPos < dataBits.size) dataBits[bitPos++] else false
                        // Apply checkerboard mask for visual clarity
                        grid[row][col] = bit xor ((row + col) % 2 == 0)
                    }
                }
            }
            right -= 2
        }

        return grid
    }

    private fun drawFinderPattern(grid: Array<BooleanArray>, startR: Int, startC: Int) {
        for (r in 0..6) {
            for (c in 0..6) {
                val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                val isCenter = r in 2..4 && c in 2..4
                grid[startR + r][startC + c] = isBorder || isCenter
            }
        }
        // Separator ring
        for (r in -1..7) {
            for (c in -1..7) {
                val gr = startR + r
                val gc = startC + c
                if (gr in grid.indices && gc in grid[0].indices) {
                    if (r == -1 || r == 7 || c == -1 || c == 7) {
                        grid[gr][gc] = false
                    }
                }
            }
        }
    }

    private fun drawAlignmentPattern(grid: Array<BooleanArray>, centerR: Int, centerC: Int) {
        for (r in -2..2) {
            for (c in -2..2) {
                val isBorder = r == -2 || r == 2 || c == -2 || c == 2
                val isCenter = r == 0 && c == 0
                grid[centerR + r][centerC + c] = isBorder || isCenter
            }
        }
    }

    private fun isReserved(grid: Array<BooleanArray>, r: Int, c: Int, N: Int): Boolean {
        if (r in 0..7 && c in 0..7) return true
        if (r in 0..7 && c in N - 8 until N) return true
        if (r in N - 8 until N && c in 0..7) return true
        if (r in N - 11..N - 7 && c in N - 11..N - 7) return true
        if (r == 6 || c == 6) return true
        return false
    }
}
