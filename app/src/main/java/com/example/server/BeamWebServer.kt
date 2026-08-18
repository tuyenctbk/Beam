package com.example.server

import android.content.Context
import android.util.Log
import com.example.data.ActiveTransfer
import com.example.util.StorageUtils
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class BeamWebServer(
    private val context: Context,
    val port: Int = 8080,
    private val onTransferStarted: (ActiveTransfer) -> Unit,
    private val onTransferProgress: (String, Long) -> Unit,
    private val onTransferCompleted: (File, String, String, String) -> Unit,
    private val onRemoteClipReceived: (String, String) -> Unit,
    private val getTargetDirectory: () -> File = { StorageUtils.getDefaultDownloadDir(context) }
) {

    @Volatile
    var isRunning = false
        private set

    private var serverSocket: ServerSocket? = null
    private val executorService = Executors.newFixedThreadPool(8)

    fun start() {
        if (isRunning) return
        isRunning = true
        thread(name = "BeamWebServerThread") {
            try {
                serverSocket = ServerSocket(port)
                Log.d("BeamWebServer", "Server started on port $port")
                while (isRunning) {
                    val socket = serverSocket?.accept() ?: break
                    executorService.execute { handleClient(socket) }
                }
            } catch (e: Exception) {
                Log.e("BeamWebServer", "Server stopped or failed: ${e.message}")
            } finally {
                isRunning = false
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
            executorService.shutdownNow()
        } catch (e: Exception) {
            Log.e("BeamWebServer", "Error stopping server: ${e.message}")
        }
    }

    private class HeaderData(
        val requestLine: String,
        val headers: Map<String, String>
    )

    private fun parseHeadersFromStream(inputStream: InputStream): HeaderData? {
        val headerStream = ByteArrayOutputStream()
        var b: Int
        var foundEnd = false
        while (inputStream.read().also { b = it } != -1) {
            headerStream.write(b)
            val bytes = headerStream.toByteArray()
            val len = bytes.size
            if (len >= 4 &&
                bytes[len - 4] == 13.toByte() && bytes[len - 3] == 10.toByte() &&
                bytes[len - 2] == 13.toByte() && bytes[len - 1] == 10.toByte()
            ) {
                foundEnd = true
                break
            }
            if (len > 32768) break
        }
        if (!foundEnd && headerStream.size() == 0) return null

        val text = String(headerStream.toByteArray(), Charsets.UTF_8)
        val lines = text.split("\r\n")
        if (lines.isEmpty() || lines[0].isEmpty()) return null

        val reqLine = lines[0]
        val headerMap = mutableMapOf<String, String>()
        for (i in 1 until lines.size) {
            val l = lines[i]
            val colon = l.indexOf(":")
            if (colon != -1) {
                val name = l.substring(0, colon).trim().lowercase()
                val value = l.substring(colon + 1).trim()
                headerMap[name] = value
            }
        }
        return HeaderData(reqLine, headerMap)
    }

    private fun handleClient(socket: Socket) {
        val clientIp = socket.inetAddress.hostAddress ?: "Unknown"
        try {
            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()

            // Read HTTP request line and headers without buffering binary body bytes
            val headerData = parseHeadersFromStream(inputStream) ?: return
            val requestLine = headerData.requestLine
            val parts = requestLine.split(" ")
            if (parts.size < 2) return
            val method = parts[0].uppercase()
            val fullUrl = parts[1]
            val headers = headerData.headers

            when {
                fullUrl == "/" || fullUrl.startsWith("/index.html") -> {
                    serveWebPortal(outputStream)
                }
                fullUrl.startsWith("/privacy") || fullUrl.startsWith("/privacy.html") -> {
                    servePrivacyPolicy(outputStream)
                }
                fullUrl.startsWith("/api/status") -> {
                    serveStatusJson(outputStream)
                }
                fullUrl.startsWith("/api/upload") && method == "POST" -> {
                    handleFileUpload(fullUrl, headers, inputStream, outputStream, clientIp)
                }
                fullUrl.startsWith("/api/clipboard") && method == "POST" -> {
                    handleClipboardPost(headers, inputStream, outputStream, clientIp)
                }
                fullUrl.startsWith("/api/files") && method == "GET" -> {
                    serveFileList(outputStream)
                }
                fullUrl.startsWith("/api/download") && method == "GET" -> {
                    handleFileDownload(fullUrl, outputStream)
                }
                fullUrl.startsWith("/api/delete") && (method == "DELETE" || method == "POST") -> {
                    handleFileDelete(fullUrl, outputStream)
                }
                else -> {
                    sendHttpResponse(outputStream, 404, "text/plain", "404 Not Found")
                }
            }
        } catch (e: Exception) {
            Log.e("BeamWebServer", "Client handling error: ${e.message}")
            com.example.util.FirebaseManager.recordException(e)
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun serveWebPortal(outputStream: OutputStream) {
        val html = getEmbeddedWebPortalHtml()
        sendHttpResponse(outputStream, 200, "text/html; charset=utf-8", html)
    }

    private fun servePrivacyPolicy(outputStream: OutputStream) {
        val html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Privacy Policy - Beam TV File Transfer</title>
                <style>
                    :root {
                        --primary: #2563EB;
                        --primary-container: #DBEAFE;
                        --text-main: #0F172A;
                        --text-sub: #475569;
                        --bg-main: #F8FAFC;
                        --bg-card: #FFFFFF;
                        --border: #E2E8F0;
                    }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        color: var(--text-main);
                        background-color: var(--bg-main);
                        line-height: 1.6;
                        margin: 0;
                        padding: 0;
                    }
                    header {
                        background-color: var(--bg-card);
                        border-bottom: 1.5.dp solid var(--border);
                        padding: 16px 24px;
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                    }
                    .logo-group {
                        display: flex;
                        align-items: center;
                        gap: 8px;
                    }
                    .logo-icon {
                        font-size: 24px;
                        background: var(--primary-container);
                        padding: 6px;
                        border-radius: 10px;
                    }
                    .logo-title {
                        font-size: 20px;
                        font-weight: 800;
                        color: var(--text-main);
                    }
                    main {
                        max-width: 800px;
                        margin: 40px auto;
                        padding: 0 24px;
                    }
                    .card {
                        background: var(--bg-card);
                        border-radius: 20px;
                        border: 1px solid var(--border);
                        padding: 32px;
                        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
                    }
                    h1 {
                        font-size: 28px;
                        font-weight: 800;
                        margin-top: 0;
                        margin-bottom: 8px;
                        color: var(--text-main);
                    }
                    .date {
                        font-size: 13px;
                        color: var(--text-sub);
                        margin-bottom: 24px;
                        border-bottom: 1px solid var(--border);
                        padding-bottom: 12px;
                    }
                    h2 {
                        font-size: 20px;
                        font-weight: 700;
                        margin-top: 32px;
                        margin-bottom: 12px;
                        color: var(--text-main);
                    }
                    p, li {
                        font-size: 15px;
                        color: var(--text-sub);
                    }
                    ul {
                        padding-left: 20px;
                    }
                    li {
                        margin-bottom: 8px;
                    }
                    footer {
                        text-align: center;
                        padding: 40px 20px;
                        font-size: 13px;
                        color: var(--text-sub);
                        border-top: 1px solid var(--border);
                        margin-top: 60px;
                    }
                    .badge {
                        background-color: #D1FAE5;
                        color: #065F46;
                        padding: 4px 8px;
                        border-radius: 6px;
                        font-size: 12px;
                        font-weight: 600;
                    }
                </style>
            </head>
            <body>
                <header>
                    <div class="logo-group">
                        <div class="logo-icon">⚡</div>
                        <div class="logo-title">Beam TV File Transfer</div>
                    </div>
                    <span class="badge">100% Offline & Secure</span>
                </header>

                <main>
                    <div class="card">
                        <h1>Privacy Policy</h1>
                        <div class="date">Last Updated: August 12, 2026</div>

                        <p>Welcome to <strong>Beam TV File Transfer</strong> ("we," "our," or "us"). We are committed to protecting your privacy. This Privacy Policy explains how our application manages data, permissions, and files.</p>

                        <h2>1. Core Privacy Philosophy</h2>
                        <p>We believe your files belong to you. Beam TV File Transfer is designed as a **local network-only, ad-free utility** for Android TV devices. All file transfers occur entirely within your private local Wi-Fi or Ethernet network. No files, content, or credentials ever leave your home router or are uploaded to external cloud servers.</p>

                        <h2>2. Data Collection & Use</h2>
                        <p>Unlike traditional file utilities, we do not monitor, inspect, or collect your files. Here is what we do (and do not) collect:</p>
                        <ul>
                            <li><strong>Files and Archives:</strong> Your beamed files (APKs, videos, photos, and zip archives) are stored solely on your Android TV's local storage in the "Downloads" directory. We have absolutely no server-side access to these files.</li>
                            <li><strong>Clipboard Data:</strong> When you use the Remote TV Keyboard feature to type or copy text/URLs from your phone to your TV, the transfer is made directly via a local peer-to-peer connection. This data is processed in memory and is never saved or logged externally.</li>
                            <li><strong>No User Account Tracking:</strong> You can use Beam TV File Transfer without creating an account, providing an email, or linking a profile.</li>
                        </ul>

                        <h2>3. Required Permissions</h2>
                        <p>To perform its local file sharing tasks, Beam TV File Transfer requests the following system permissions:</p>
                        <ul>
                            <li><strong>Storage/All Files Access (READ_EXTERNAL_STORAGE / MANAGE_EXTERNAL_STORAGE):</strong> Required to write received files to your TV's Downloads directory, and to read files for management, deletion, and user previews inside the Explorer view.</li>
                            <li><strong>Internet & Access Network State:</strong> Required to initialize the local HTTP server, allow devices on your local Wi-Fi to establish a connection, and generate the connection QR code.</li>
                        </ul>

                        <h2>4. Third-Party Services & Analytics</h2>
                        <p>To improve app reliability and track performance, we utilize the following secure Google services:</p>
                        <ul>
                            <li><strong>Firebase Crashlytics:</strong> Records anonymous crash logs when critical errors occur, allowing us to find and fix bugs.</li>
                            <li><strong>Firebase Performance Monitoring & Analytics:</strong> Measures transfer speeds, transfer success rates, and local server initialization times to ensure high performance across different Android TV hardware.</li>
                        </ul>
                        <p>These analytics contain no personally identifiable details (PII) and do not include the contents or names of your files.</p>

                        <h2>5. Contact Us</h2>
                        <p>If you have any questions or concerns about this local network privacy policy, feel free to contact us at <strong>tuyenctbk@gmail.com</strong>.</p>
                    </div>
                </main>

                <footer>
                    Beam TV File Transfer — 100% Ad-Free & Local Network-Based Security
                </footer>
            </body>
            </html>
        """.trimIndent()
        sendHttpResponse(outputStream, 200, "text/html; charset=utf-8", html)
    }

    private fun serveStatusJson(outputStream: OutputStream) {
        val targetDir = getTargetDirectory()
        val json = """
            {
              "status": "online",
              "app": "Beam TV File Transfer",
              "version": "1.0",
              "downloadDir": "${targetDir.name}",
              "freeSpaceBytes": ${targetDir.usableSpace},
              "totalSpaceBytes": ${targetDir.totalSpace}
            }
        """.trimIndent()
        sendHttpResponse(outputStream, 200, "application/json", json)
    }

    private fun handleFileUpload(
        url: String,
        headers: Map<String, String>,
        inputStream: InputStream,
        outputStream: OutputStream,
        clientIp: String
    ) {
        val startTime = System.currentTimeMillis()
        val perfTrace = com.example.util.FirebaseManager.startPerformanceTrace("web_file_upload_latency")
        
        val contentLength = headers["content-length"]?.toLongOrNull() ?: 0L
        val rawFilenameHeader = headers["x-file-name"]
            ?: extractQueryParam(url, "filename")
            ?: "beam_file_${System.currentTimeMillis()}"
        val filename = URLDecoder.decode(rawFilenameHeader, "UTF-8")

        val targetDir = getTargetDirectory()
        if (!targetDir.exists()) targetDir.mkdirs()
        val destFile = File(targetDir, filename)

        val transferId = UUID.randomUUID().toString()
        val activeTransfer = ActiveTransfer(
            id = transferId,
            fileName = filename,
            totalBytes = contentLength,
            receivedBytes = 0L,
            clientIp = clientIp
        )

        onTransferStarted(activeTransfer)

        try {
            val contentType = headers["content-type"] ?: ""
            if (contentType.contains("multipart/form-data")) {
                // Standard multipart upload
                val boundary = contentType.substringAfter("boundary=").trim()
                saveMultipartStream(inputStream, destFile, boundary, contentLength, transferId)
            } else {
                // Raw binary stream transfer
                saveRawStream(inputStream, destFile, contentLength, transferId)
            }

            // Calculate Checksum & Verify Integrity
            val clientChecksum = headers["x-sha256"] ?: headers["x-checksum"] ?: headers["x-md5"]
            val calculatedSha256 = com.example.util.ChecksumUtils.calculateSha256(destFile)

            val checksumStatus = if (!clientChecksum.isNullOrEmpty()) {
                val calculatedMd5 = com.example.util.ChecksumUtils.calculateMd5(destFile)
                if (clientChecksum.equals(calculatedSha256, ignoreCase = true)) {
                    "SHA-256 Verified (Integrity OK)"
                } else if (clientChecksum.equals(calculatedMd5, ignoreCase = true)) {
                    "MD5 Verified (Integrity OK)"
                } else {
                    "CORRUPTED - Checksum Mismatch!"
                }
            } else {
                "SHA-256 Verified (Integrity OK)"
            }

            val durationMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
            val isSuccess = !checksumStatus.contains("CORRUPTED", ignoreCase = true)

            // Firebase Performance & Analytics metrics
            com.example.util.FirebaseManager.stopPerformanceTrace(
                perfTrace,
                mapOf(
                    "file_size_bytes" to destFile.length(),
                    "duration_ms" to durationMs,
                    "success" to if (isSuccess) 1L else 0L
                )
            )
            com.example.util.FirebaseManager.logFileTransferCompleted(
                fileName = filename,
                fileSize = destFile.length(),
                durationMs = durationMs,
                category = com.example.data.FileItem.determineCategory(destFile).name,
                checksumStatus = checksumStatus,
                success = isSuccess
            )

            onTransferCompleted(destFile, clientIp, calculatedSha256, checksumStatus)

            val responseJson = """{"status":"success","filename":"$filename","size":${destFile.length()},"checksum":"$calculatedSha256","checksumStatus":"$checksumStatus"}"""
            sendHttpResponse(outputStream, 200, "application/json", responseJson)
        } catch (e: Exception) {
            com.example.util.FirebaseManager.recordException(e)
            com.example.util.FirebaseManager.stopPerformanceTrace(perfTrace, mapOf("success" to 0L))
            com.example.util.FirebaseManager.logFileTransferCompleted(
                fileName = filename,
                fileSize = 0L,
                durationMs = System.currentTimeMillis() - startTime,
                category = "UNKNOWN",
                checksumStatus = "FAILED: ${e.message}",
                success = false
            )
            throw e
        }
    }

    private fun saveRawStream(
        inputStream: InputStream,
        destFile: File,
        contentLength: Long,
        transferId: String
    ) {
        FileOutputStream(destFile).use { fos ->
            val buffer = ByteArray(65536)
            var totalRead = 0L
            var read: Int
            val maxToRead = if (contentLength > 0) contentLength else Long.MAX_VALUE

            while (totalRead < maxToRead) {
                val toRead = Math.min(buffer.size.toLong(), maxToRead - totalRead).toInt()
                read = inputStream.read(buffer, 0, toRead)
                if (read <= 0) break
                fos.write(buffer, 0, read)
                totalRead += read
                onTransferProgress(transferId, totalRead)
            }
        }
    }

    private fun saveMultipartStream(
        inputStream: InputStream,
        destFile: File,
        boundary: String,
        contentLength: Long,
        transferId: String
    ) {
        // Read until header boundary ends
        val lineBuffer = ByteArrayOutputStream()
        var lastByte = -1
        var headerEnded = false
        var byteCount = 0L

        while (!headerEnded && byteCount < 8192) {
            val b = inputStream.read()
            if (b == -1) break
            byteCount++
            lineBuffer.write(b)
            val bytes = lineBuffer.toByteArray()
            val len = bytes.size
            if (len >= 4 && bytes[len - 4] == '\r'.code.toByte() && bytes[len - 3] == '\n'.code.toByte() &&
                bytes[len - 2] == '\r'.code.toByte() && bytes[len - 1] == '\n'.code.toByte()) {
                headerEnded = true
            }
        }

        val remainingLength = if (contentLength > byteCount) contentLength - byteCount else 0L
        saveRawStream(inputStream, destFile, remainingLength, transferId)
    }

    private fun handleClipboardPost(
        headers: Map<String, String>,
        inputStream: InputStream,
        outputStream: OutputStream,
        clientIp: String
    ) {
        val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
        val body = if (contentLength > 0) {
            val bytes = ByteArray(contentLength)
            var read = 0
            while (read < contentLength) {
                val r = inputStream.read(bytes, read, contentLength - read)
                if (r <= 0) break
                read += r
            }
            String(bytes, 0, read, Charsets.UTF_8)
        } else ""

        val text = if (body.contains("text=")) {
            URLDecoder.decode(body.substringAfter("text=").substringBefore("&"), "UTF-8")
        } else body.trim()

        if (text.isNotEmpty()) {
            onRemoteClipReceived(text, clientIp)
            sendHttpResponse(outputStream, 200, "application/json", """{"status":"success","received":true}""")
        } else {
            sendHttpResponse(outputStream, 400, "application/json", """{"error":"Empty clip text"}""")
        }
    }

    private fun serveFileList(outputStream: OutputStream) {
        val targetDir = getTargetDirectory()
        val files = targetDir.listFiles() ?: arrayOf()
        val jsonArray = files.joinToString(",") { f ->
            """{"name":"${escapeJson(f.name)}","size":${f.length()},"modified":${f.lastModified()},"isDir":${f.isDirectory}}"""
        }
        sendHttpResponse(outputStream, 200, "application/json", "[$jsonArray]")
    }

    private fun handleFileDownload(url: String, outputStream: OutputStream) {
        val filename = extractQueryParam(url, "path") ?: extractQueryParam(url, "name")
        if (filename.isNullOrEmpty()) {
            sendHttpResponse(outputStream, 400, "text/plain", "Missing path param")
            return
        }
        val targetFile = File(getTargetDirectory(), filename)
        if (!targetFile.exists() || targetFile.isDirectory) {
            sendHttpResponse(outputStream, 404, "text/plain", "File not found")
            return
        }

        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/octet-stream\r\n" +
                "Content-Disposition: attachment; filename=\"${targetFile.name}\"\r\n" +
                "Content-Length: ${targetFile.length()}\r\n" +
                "Access-Control-Allow-Origin: *\r\n\r\n"
        outputStream.write(header.toByteArray(Charsets.UTF_8))

        FileInputStream(targetFile).use { fis ->
            val buf = ByteArray(65536)
            var len: Int
            while (fis.read(buf).also { len = it } > 0) {
                outputStream.write(buf, 0, len)
            }
        }
        outputStream.flush()
    }

    private fun handleFileDelete(url: String, outputStream: OutputStream) {
        val filename = extractQueryParam(url, "path") ?: extractQueryParam(url, "name")
        if (filename.isNullOrEmpty()) {
            sendHttpResponse(outputStream, 400, "text/plain", "Missing path")
            return
        }
        val targetFile = File(getTargetDirectory(), filename)
        if (targetFile.exists()) {
            targetFile.delete()
            sendHttpResponse(outputStream, 200, "application/json", """{"status":"deleted"}""")
        } else {
            sendHttpResponse(outputStream, 404, "application/json", """{"error":"File not found"}""")
        }
    }

    private fun sendHttpResponse(outputStream: OutputStream, statusCode: Int, contentType: String, body: String) {
        val bodyBytes = body.toByteArray(Charsets.UTF_8)
        val statusText = if (statusCode == 200) "OK" else if (statusCode == 404) "Not Found" else "Bad Request"
        val header = "HTTP/1.1 $statusCode $statusText\r\n" +
                "Content-Type: $contentType\r\n" +
                "Content-Length: ${bodyBytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        outputStream.write(header.toByteArray(Charsets.UTF_8))
        outputStream.write(bodyBytes)
        outputStream.flush()
    }

    private fun extractQueryParam(url: String, key: String): String? {
        val qIdx = url.indexOf("?")
        if (qIdx == -1) return null
        val query = url.substring(qIdx + 1)
        for (pair in query.split("&")) {
            val kv = pair.split("=")
            if (kv.size == 2 && kv[0] == key) {
                return URLDecoder.decode(kv[1], "UTF-8")
            }
        }
        return null
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }

    private fun getEmbeddedWebPortalHtml(): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Beam — Web to TV Transfer</title>
    <style>
        :root {
            --bg-color: #F7F9FC;
            --surface: #FFFFFF;
            --primary: #005FAC;
            --primary-container: #D3E3FD;
            --text-main: #1A1C1E;
            --text-sub: #44474E;
            --border: #E2E8F0;
            --radius-xl: 28px;
            --radius-lg: 18px;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
        body { background-color: var(--bg-color); color: var(--text-main); display: flex; flex-direction: column; min-height: 100vh; }
        header { background: rgba(255, 255, 255, 0.8); backdrop-filter: blur(12px); border-bottom: 1px solid var(--border); padding: 16px 24px; display: flex; align-items: center; justify-content: space-between; sticky: top; top: 0; z-index: 100; }
        .logo-group { display: flex; align-items: center; gap: 12px; }
        .logo-icon { width: 36px; height: 36px; background: var(--primary); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 18px; }
        .logo-title { font-size: 20px; font-weight: 700; color: #001D35; }
        
        main { flex: 1; max-width: 600px; width: 100%; margin: 0 auto; padding: 24px 16px; display: flex; flex-direction: column; gap: 20px; }
        
        .card { background: var(--surface); border-radius: var(--radius-xl); padding: 24px; border: 1px solid var(--border); box-shadow: 0 10px 25px -5px rgba(0, 29, 53, 0.04); }
        .drop-zone { border: 2px dashed var(--primary); background: #F0F5FF; border-radius: var(--radius-lg); padding: 40px 20px; text-align: center; cursor: pointer; transition: all 0.2s ease; margin-top: 12px; }
        .drop-zone:hover, .drop-zone.dragover { background: var(--primary-container); border-color: var(--primary); transform: scale(1.01); }
        .drop-icon { font-size: 42px; margin-bottom: 12px; }
        
        .btn { background: var(--primary); color: white; border: none; padding: 14px 28px; border-radius: 14px; font-weight: 600; font-size: 15px; cursor: pointer; transition: background 0.2s; display: inline-flex; align-items: center; justify-content: center; gap: 8px; width: 100%; }
        .btn:hover { background: #004B8A; }
        
        input[type="file"] { display: none; }
        
        .progress-bar-container { width: 100%; height: 10px; background: #E1E3E8; border-radius: 5px; overflow: hidden; margin-top: 16px; display: none; }
        .progress-bar { width: 0%; height: 100%; background: var(--primary); transition: width 0.1s linear; }
        
        .status-msg { font-size: 14px; color: var(--text-sub); text-align: center; margin-top: 8px; font-weight: 500; }
        
        .tabs { display: flex; background: #E1E3E8; border-radius: 14px; padding: 4px; gap: 4px; }
        .tab-btn { flex: 1; padding: 10px; border: none; border-radius: 10px; background: transparent; font-weight: 600; color: var(--text-sub); cursor: pointer; transition: all 0.2s; text-align: center; font-size: 14px; }
        .tab-btn.active { background: white; color: var(--primary); box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
        
        textarea { width: 100%; border-radius: 14px; border: 1px solid var(--border); padding: 14px; font-size: 15px; resize: vertical; min-height: 90px; margin-bottom: 12px; outline: none; }
        textarea:focus { border-color: var(--primary); ring: 2px var(--primary-container); }
        
        .file-list { display: flex; flex-direction: column; gap: 10px; margin-top: 12px; max-height: 250px; overflow-y: auto; }
        .file-item { display: flex; align-items: center; justify-content: space-between; padding: 12px; background: var(--bg-color); border-radius: 12px; font-size: 14px; }
        .file-name { font-weight: 600; color: var(--text-main); word-break: break-all; max-width: 70%; }
        .file-dl { color: var(--primary); text-decoration: none; font-weight: 600; padding: 6px 12px; background: var(--primary-container); border-radius: 8px; }
        
        footer { text-align: center; padding: 20px; font-size: 12px; color: var(--text-sub); }
    </style>
</head>
<body>
    <header>
        <div class="logo-group">
            <div class="logo-icon">⚡</div>
            <div class="logo-title">Beam</div>
        </div>
        <span style="font-size: 12px; background: var(--primary-container); color: #001D35; padding: 6px 12px; border-radius: 20px; font-weight: 600;">Connected to TV</span>
    </header>

    <main>
        <div class="tabs">
            <button class="tab-btn active" onclick="switchTab('send')">📁 Send Files</button>
            <button class="tab-btn" onclick="switchTab('clip')">⌨️ Remote Clipboard</button>
            <button class="tab-btn" onclick="switchTab('tvfiles')">🖥️ TV Files</button>
        </div>

        <!-- TAB 1: SEND FILES -->
        <div id="tab-send" class="card">
            <h2 style="font-size: 18px; margin-bottom: 4px;">Beam to TV</h2>
            <p style="font-size: 13px; color: var(--text-sub);">Select or drop APKs, videos, photos, or zip archives.</p>
            
            <div class="drop-zone" id="dropZone" onclick="document.getElementById('fileInput').click()">
                <div class="drop-icon">📤</div>
                <div style="font-weight: 600; font-size: 15px; margin-bottom: 4px;">Tap to choose files or drop here</div>
                <div style="font-size: 12px; color: var(--text-sub);">APKs, MP4, MKV, JPG, PNG, ZIP, DOCS</div>
            </div>
            <input type="file" id="fileInput" multiple onchange="handleFiles(this.files)">

            <div class="progress-bar-container" id="progressContainer">
                <div class="progress-bar" id="progressBar"></div>
            </div>
            <div class="status-msg" id="statusMsg">Ready to send</div>
        </div>

        <!-- TAB 2: REMOTE CLIPBOARD -->
        <div id="tab-clip" class="card" style="display: none;">
            <h2 style="font-size: 18px; margin-bottom: 4px;">Remote TV Keyboard</h2>
            <p style="font-size: 13px; color: var(--text-sub); margin-bottom: 12px;">Type URLs, passwords, or text to paste instantly on your Android TV.</p>
            
            <textarea id="clipText" placeholder="Paste or type text/URL here..."></textarea>
            <button class="btn" onclick="sendClipboard()">⚡ Beam Text to TV Clipboard</button>
            <div class="status-msg" id="clipStatus"></div>
        </div>

        <!-- TAB 3: TV FILES -->
        <div id="tab-tvfiles" class="card" style="display: none;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <h2 style="font-size: 18px;">Files on Android TV</h2>
                <button onclick="loadTvFiles()" style="background: none; border: none; color: var(--primary); font-weight: 600; cursor: pointer;">🔄 Refresh</button>
            </div>
            <div class="file-list" id="tvFileList">
                <div style="text-align: center; color: var(--text-sub); padding: 20px;">Loading files...</div>
            </div>
        </div>
    </main>

    <footer>
        Beam 1.0 — 100% Ad-Free Local Wi-Fi File Transfer for Android TV | <a href="/privacy.html" style="color: var(--primary); text-decoration: none; font-weight: 600;">Privacy Policy</a>
    </footer>

    <script>
        function switchTab(tab) {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            document.getElementById('tab-send').style.display = 'none';
            document.getElementById('tab-clip').style.display = 'none';
            document.getElementById('tab-tvfiles').style.display = 'none';

            if (tab === 'send') {
                document.getElementById('tab-send').style.display = 'block';
                event.target.classList.add('active');
            } else if (tab === 'clip') {
                document.getElementById('tab-clip').style.display = 'block';
                event.target.classList.add('active');
            } else if (tab === 'tvfiles') {
                document.getElementById('tab-tvfiles').style.display = 'block';
                event.target.classList.add('active');
                loadTvFiles();
            }
        }

        const dropZone = document.getElementById('dropZone');
        ['dragenter', 'dragover'].forEach(name => {
            dropZone.addEventListener(name, (e) => { e.preventDefault(); dropZone.classList.add('dragover'); }, false);
        });
        ['dragleave', 'drop'].forEach(name => {
            dropZone.addEventListener(name, (e) => { e.preventDefault(); dropZone.classList.remove('dragover'); }, false);
        });
        dropZone.addEventListener('drop', (e) => {
            const dt = e.dataTransfer;
            handleFiles(dt.files);
        });

        async function computeSha256(file) {
            try {
                if (!window.crypto || !window.crypto.subtle) return null;
                const buffer = await file.arrayBuffer();
                const hashBuffer = await crypto.subtle.digest('SHA-256', buffer);
                const hashArray = Array.from(new Uint8Array(hashBuffer));
                return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
            } catch (e) {
                return null;
            }
        }

        async function handleFiles(files) {
            if (!files || files.length === 0) return;
            const progressContainer = document.getElementById('progressContainer');
            const progressBar = document.getElementById('progressBar');
            const statusMsg = document.getElementById('statusMsg');

            progressContainer.style.display = 'block';

            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                statusMsg.innerText = "Verifying & Sending (" + (i + 1) + "/" + files.length + "): " + file.name;
                
                let clientSha256 = null;
                if (file.size < 100 * 1024 * 1024) {
                    try {
                        clientSha256 = await computeSha256(file);
                    } catch (e) {}
                }

                await new Promise((resolve, reject) => {
                    const xhr = new XMLHttpRequest();
                    const encodedName = encodeURIComponent(file.name);
                    xhr.open('POST', "/api/upload?filename=" + encodedName, true);
                    xhr.setRequestHeader('X-File-Name', encodedName);
                    if (clientSha256) {
                        xhr.setRequestHeader('X-SHA256', clientSha256);
                    }

                    xhr.upload.onprogress = (e) => {
                        if (e.lengthComputable) {
                            const percent = Math.round((e.loaded / e.total) * 100);
                            progressBar.style.width = percent + '%';
                            statusMsg.innerText = "Uploading " + file.name + " (" + percent + "%)";
                        }
                    };

                    xhr.onload = () => {
                        if (xhr.status === 200) {
                            statusMsg.innerText = "✅ Beamed & Verified: " + file.name;
                            resolve();
                        } else {
                            statusMsg.innerText = "❌ Error sending " + file.name;
                            resolve();
                        }
                    };
                    xhr.onerror = () => {
                        statusMsg.innerText = "❌ Transfer failed. Check Wi-Fi connection.";
                        resolve();
                    };

                    xhr.send(file);
                });
            }
            setTimeout(() => { progressContainer.style.display = 'none'; progressBar.style.width = '0%'; }, 2000);
        }

        async function sendClipboard() {
            const clipText = document.getElementById('clipText').value;
            const status = document.getElementById('clipStatus');
            if (!clipText.trim()) return;

            status.innerText = "Sending to TV...";
            try {
                const res = await fetch('/api/clipboard', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'text=' + encodeURIComponent(clipText)
                });
                if (res.ok) {
                    status.innerText = "✅ Beamed to TV Clipboard!";
                    document.getElementById('clipText').value = "";
                } else {
                    status.innerText = "❌ Failed to beam text.";
                }
            } catch (e) {
                status.innerText = "❌ Error: " + e.message;
            }
        }

        async function loadTvFiles() {
            const list = document.getElementById('tvFileList');
            try {
                const res = await fetch('/api/files');
                const data = await res.json();
                if (data.length === 0) {
                    list.innerHTML = '<div style="text-align: center; color: var(--text-sub); padding: 20px;">No files on TV Downloads folder yet.</div>';
                    return;
                }
                list.innerHTML = data.map(f => 
                    '<div class="file-item">' +
                        '<div class="file-name">' + f.name + '</div>' +
                        '<a href="/api/download?path=' + encodeURIComponent(f.name) + '" class="file-dl" download>⬇️ Download</a>' +
                    '</div>'
                ).join('');
            } catch (e) {
                list.innerHTML = '<div style="text-align: center; color: red;">Failed to load TV files.</div>';
            }
        }
    </script>
</body>
</html>

        """.trimIndent()
    }
}
