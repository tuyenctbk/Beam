package com.example

import com.example.beam.data.model.FileCategory
import com.example.beam.server.NetworkUtils
import com.example.beam.ui.screens.SortField
import com.example.beam.ui.screens.SortOrder
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun findAvailablePort_returnsValidPort() {
        val port = NetworkUtils.findAvailablePort(8080, 8100)
        assertTrue("Port should be >= 8080", port >= 8080)
    }

    @Test
    fun fileCategory_fromExtension_identifiesCorrectly() {
        assertEquals(FileCategory.PHOTOS, FileCategory.fromFileExtension("jpg"))
        assertEquals(FileCategory.PHOTOS, FileCategory.fromFileExtension("png"))
        assertEquals(FileCategory.VIDEOS, FileCategory.fromFileExtension("mp4"))
        assertEquals(FileCategory.MUSIC, FileCategory.fromFileExtension("mp3"))
        assertEquals(FileCategory.APKS, FileCategory.fromFileExtension("apk"))
        assertEquals(FileCategory.ZIP, FileCategory.fromFileExtension("zip"))
    }
}
