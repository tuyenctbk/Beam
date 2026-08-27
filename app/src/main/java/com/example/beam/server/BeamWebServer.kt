package com.example.beam.server

import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.UUID

class BeamWebServer(
    port: Int = 8080,
    private val uploadDir: File,
    private val onFileUploadListener: (fileName: String, filePath: String, sizeBytes: Long, clientIp: String) -> Unit,
    private val onClipboardListener: (text: String, clientIp: String) -> Unit,
    private val onTransferProgress: (id: String, fileName: String, isUpload: Boolean, bytesTransferred: Long, totalBytes: Long, speedBytesPerSec: Long, clientIp: String) -> Unit = { _, _, _, _, _, _, _ -> },
    private val onTransferCompleted: (id: String, fileName: String, isUpload: Boolean, sizeBytes: Long, clientIp: String) -> Unit = { _, _, _, _, _ -> }
) : NanoHTTPD(port) {

    init {
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
        }
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val clientIp = session.remoteIpAddress ?: "Mobile Web"

        return when {
            uri == "/" -> serveWebPage()
            uri == "/files" && session.method == Method.GET -> serveFileListJson()
            uri == "/upload" && session.method == Method.POST -> handleFileUpload(session, clientIp)
            uri == "/clipboard" && session.method == Method.POST -> handleClipboardText(session, clientIp)
            uri.startsWith("/download/") -> serveDownloadFile(uri.substring("/download/".length), clientIp)
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found")
        }
    }

    private fun handleFileUpload(session: IHTTPSession, clientIp: String): Response {
        val transferId = UUID.randomUUID().toString()
        val contentLength = session.headers["content-length"]?.toLongOrNull() ?: 0L
        val estimatedTotal = if (contentLength > 0) contentLength else 1024 * 1024L
        val startTime = System.currentTimeMillis()

        return try {
            val params = session.parameters
            val targetFileName = params["fileName"]?.firstOrNull() ?: params["file"]?.firstOrNull() ?: "beamed_file_${System.currentTimeMillis()}"

            // Report initial transfer start
            onTransferProgress(transferId, targetFileName, true, 0L, estimatedTotal, 0L, clientIp)

            val files = HashMap<String, String>()
            session.parseBody(files)

            val resolvedName = session.parameters["fileName"]?.firstOrNull() ?: targetFileName
            val tempPath = files["file"] ?: files["upload"] ?: files.values.firstOrNull()

            if (tempPath != null) {
                val tempFile = File(tempPath)
                val categorySubfolder = getCategorySubfolder(resolvedName)
                val targetFile = File(categorySubfolder, resolvedName)
                val totalSize = tempFile.length()

                // Progress update during copy
                val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000.0).coerceAtLeast(0.1)
                val speed = (totalSize / elapsedSec).toLong()

                onTransferProgress(transferId, targetFile.name, true, totalSize, totalSize, speed, clientIp)

                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()

                val size = targetFile.length()
                onFileUploadListener(targetFile.name, targetFile.absolutePath, size, clientIp)
                onTransferCompleted(transferId, targetFile.name, true, size, clientIp)

                val jsonResponse = """{"status":"success","fileName":"${targetFile.name}","size":$size}"""
                newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse)
            } else {
                newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", """{"status":"error","message":"No file uploaded"}""")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", """{"status":"error","message":"${e.localizedMessage}"}""")
        }
    }

    private fun handleClipboardText(session: IHTTPSession, clientIp: String): Response {
        return try {
            val files = HashMap<String, String>()
            session.parseBody(files)
            val text = session.parameters["text"]?.firstOrNull() ?: files["postData"] ?: ""

            if (text.isNotBlank()) {
                onClipboardListener(text, clientIp)
                newFixedLengthResponse(Response.Status.OK, "application/json", """{"status":"success"}""")
            } else {
                newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", """{"status":"error","message":"Empty text"}""")
            }
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", """{"status":"error","message":"${e.localizedMessage}"}""")
        }
    }

    private fun getCategorySubfolder(fileName: String): File {
        val ext = File(fileName).extension.lowercase()
        val category = com.example.beam.data.model.FileCategory.fromFileExtension(ext)
        val folderName = when (category) {
            com.example.beam.data.model.FileCategory.PHOTOS -> "Images"
            com.example.beam.data.model.FileCategory.VIDEOS -> "Videos"
            com.example.beam.data.model.FileCategory.MUSIC -> "Audio"
            com.example.beam.data.model.FileCategory.DOCUMENTS -> "Documents"
            com.example.beam.data.model.FileCategory.APKS -> "APKs"
            com.example.beam.data.model.FileCategory.ZIP -> "Archives"
            else -> "Documents"
        }
        val targetDir = File(uploadDir, folderName)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        return targetDir
    }

    private fun serveFileListJson(): Response {
        val allFiles = uploadDir.walkTopDown()
            .filter { it.isFile && !it.name.startsWith(".") }
            .toList()
        val jsonArray = allFiles.joinToString(separator = ",", prefix = "[", postfix = "]") { file ->
            """{"name":"${file.name}","size":${file.length()},"modified":${file.lastModified()}}"""
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json", jsonArray)
    }

    private fun serveDownloadFile(fileName: String, clientIp: String): Response {
        val directFile = File(uploadDir, fileName)
        val file = if (directFile.exists() && directFile.isFile) {
            directFile
        } else {
            uploadDir.walkTopDown().firstOrNull { it.isFile && (it.name == fileName || it.relativeTo(uploadDir).path.replace("\\", "/") == fileName) }
        }

        if (file == null || !file.exists() || file.isDirectory) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File not found")
        }

        val transferId = UUID.randomUUID().toString()
        val totalBytes = file.length()
        val startTime = System.currentTimeMillis()

        return try {
            val rawFis = FileInputStream(file)
            val progressStream = ProgressTrackingInputStream(
                wrapped = rawFis,
                totalBytes = totalBytes,
                onProgress = { bytesRead, total ->
                    val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000.0).coerceAtLeast(0.1)
                    val speed = (bytesRead / elapsedSec).toLong()
                    onTransferProgress(transferId, file.name, false, bytesRead, total, speed, clientIp)
                    if (bytesRead >= total) {
                        onTransferCompleted(transferId, file.name, false, total, clientIp)
                    }
                }
            )

            newFixedLengthResponse(Response.Status.OK, "application/octet-stream", progressStream, totalBytes).apply {
                addHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
            }
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error reading file")
        }
    }

    private fun serveWebPage(): Response {
        val html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Beam TV - Wireless File Transfer</title>
                <style>
                    :root {
                        --bg-color: #080C14;
                        --card-bg: #141C2E;
                        --accent-cyan: #00F2FE;
                        --accent-blue: #4FACFE;
                        --text-primary: #FFFFFF;
                        --text-secondary: #A0AEC0;
                        --border-color: #22304E;
                        --success-color: #00E676;
                    }
                    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
                    body { background: var(--bg-color); color: var(--text-primary); padding: 24px 16px; min-height: 100vh; display: flex; flex-direction: column; align-items: center; }
                    .header { text-align: center; margin-bottom: 28px; }
                    .header h1 { font-size: 32px; font-weight: 800; background: linear-gradient(135deg, var(--accent-cyan), var(--accent-blue)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
                    .header p { color: var(--text-secondary); margin-top: 6px; font-size: 14px; }
                    .container { width: 100%; max-width: 520px; display: flex; flex-direction: column; gap: 20px; }
                    .card { background: var(--card-bg); border: 1px solid var(--border-color); border-radius: 18px; padding: 22px; box-shadow: 0 8px 30px rgba(0,0,0,0.5); }
                    .drop-zone { border: 2px dashed var(--accent-cyan); border-radius: 14px; padding: 32px 16px; text-align: center; cursor: pointer; transition: all 0.25s; background: rgba(0, 242, 254, 0.02); }
                    .drop-zone:hover { background: rgba(0, 242, 254, 0.08); border-color: #4FACFE; }
                    .drop-zone p { margin-top: 12px; font-weight: 600; font-size: 15px; }
                    input[type="file"] { display: none; }
                    .btn { width: 100%; background: linear-gradient(135deg, var(--accent-cyan), var(--accent-blue)); color: #080C14; font-weight: 800; border: none; padding: 14px; border-radius: 12px; font-size: 15px; cursor: pointer; margin-top: 16px; transition: transform 0.15s, opacity 0.15s; }
                    .btn:hover { opacity: 0.92; transform: translateY(-1px); }
                    .btn:disabled { opacity: 0.4; cursor: not-allowed; }
                    textarea { width: 100%; background: #080C14; border: 1px solid var(--border-color); color: #FFF; padding: 14px; border-radius: 10px; resize: vertical; min-height: 90px; margin-top: 10px; font-size: 14px; outline: none; }
                    textarea:focus { border-color: var(--accent-cyan); }
                    .progress-container { width: 100%; background: #080C14; border-radius: 8px; overflow: hidden; margin-top: 14px; height: 10px; border: 1px solid var(--border-color); display: none; }
                    .progress-bar { width: 0%; height: 100%; background: linear-gradient(90deg, var(--accent-cyan), var(--accent-blue)); transition: width 0.15s; }
                    .status { margin-top: 12px; font-size: 13px; text-align: center; font-weight: 600; }
                    .status.success { color: var(--success-color); }
                    .status.error { color: #FF3366; }
                    .file-list { margin-top: 12px; display: flex; flex-direction: column; gap: 8px; max-height: 240px; overflow-y: auto; }
                    .file-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 12px; background: #080C14; border: 1px solid var(--border-color); border-radius: 10px; font-size: 13px; }
                    .file-item a { color: var(--accent-cyan); text-decoration: none; font-weight: 700; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Beam TV</h1>
                    <p>Fast Wireless File Beaming & Storage for TV</p>
                </div>
                <div class="container">
                    <div class="card">
                        <h3>Beam Files to TV</h3>
                        <p style="color: var(--text-secondary); font-size: 13px; margin: 4px 0 16px;">Send photos, videos, APKs, or music directly to your TV.</p>
                        <div class="drop-zone" onclick="document.getElementById('fileInput').click()">
                            <svg width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="#00F2FE" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                            <p id="fileNameLabel">Tap or Drag Files Here</p>
                        </div>
                        <input type="file" id="fileInput" multiple onchange="onFilesSelected(this.files)">
                        <div class="progress-container" id="progressContainer"><div class="progress-bar" id="progressBar"></div></div>
                        <button class="btn" id="uploadBtn" onclick="uploadFiles()">Beam to TV</button>
                        <div class="status" id="fileStatus"></div>
                    </div>

                    <div class="card">
                        <h3>Send Remote Clipboard Text</h3>
                        <p style="color: var(--text-secondary); font-size: 13px; margin: 4px 0 10px;">Paste URLs or text to open on your Android TV.</p>
                        <textarea id="clipText" placeholder="Type or paste text/URL here..."></textarea>
                        <button class="btn" style="background: #22304E; color: #FFF;" onclick="sendClipboard()">Beam Text to TV</button>
                        <div class="status" id="clipStatus"></div>
                    </div>

                    <div class="card">
                        <h3>Files on TV (Download)</h3>
                        <p style="color: var(--text-secondary); font-size: 13px; margin: 4px 0 10px;">Download files stored in the TV Beam folder.</p>
                        <button class="btn" style="background: #1A3052; color: #00F2FE; margin-top: 4px; padding: 10px;" onclick="loadTvFiles()">Refresh TV Files</button>
                        <div class="file-list" id="tvFileList"></div>
                    </div>
                </div>

                <script>
                    let selectedFiles = [];
                    function onFilesSelected(files) {
                        if (files && files.length > 0) {
                            selectedFiles = Array.from(files);
                            const totalMb = (selectedFiles.reduce((acc, f) => acc + f.size, 0) / (1024*1024)).toFixed(2);
                            document.getElementById('fileNameLabel').innerText = selectedFiles.length === 1 
                                ? selectedFiles[0].name + ' (' + totalMb + ' MB)'
                                : selectedFiles.length + ' files selected (' + totalMb + ' MB)';
                        }
                    }

                    async function uploadFiles() {
                        if (selectedFiles.length === 0) {
                            document.getElementById('fileStatus').innerText = 'Please select at least one file first.';
                            document.getElementById('fileStatus').className = 'status error';
                            return;
                        }

                        const btn = document.getElementById('uploadBtn');
                        btn.disabled = true;
                        document.getElementById('progressContainer').style.display = 'block';

                        for (let i = 0; i < selectedFiles.length; i++) {
                            const file = selectedFiles[i];
                            document.getElementById('fileStatus').innerText = 'Beaming (' + (i + 1) + '/' + selectedFiles.length + '): ' + file.name + '...';
                            document.getElementById('fileStatus').className = 'status';

                            await new Promise((resolve, reject) => {
                                const formData = new FormData();
                                formData.append('file', file);
                                formData.append('fileName', file.name);

                                const xhr = new XMLHttpRequest();
                                xhr.upload.onprogress = function(e) {
                                    if (e.lengthComputable) {
                                        const filePercent = (e.loaded / e.total);
                                        const overallPercent = (((i + filePercent) / selectedFiles.length) * 100).toFixed(0);
                                        document.getElementById('progressBar').style.width = overallPercent + '%';
                                    }
                                };

                                xhr.onload = function() {
                                    if (xhr.status === 200) resolve();
                                    else reject(new Error(xhr.responseText || 'Upload failed'));
                                };
                                xhr.onerror = () => reject(new Error('Network error'));

                                xhr.open('POST', '/upload', true);
                                xhr.send(formData);
                            });
                        }

                        document.getElementById('fileStatus').innerText = 'Successfully beamed ' + selectedFiles.length + ' file(s) to TV!';
                        document.getElementById('fileStatus').className = 'status success';
                        document.getElementById('fileNameLabel').innerText = 'Tap or Drag Files Here';
                        document.getElementById('progressContainer').style.display = 'none';
                        document.getElementById('progressBar').style.width = '0%';
                        selectedFiles = [];
                        btn.disabled = false;
                        loadTvFiles();
                    }

                    function sendClipboard() {
                        const text = document.getElementById('clipText').value;
                        if (!text.trim()) return;
                        const xhr = new XMLHttpRequest();
                        xhr.open('POST', '/clipboard', true);
                        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                        xhr.onload = function() {
                            if (xhr.status === 200) {
                                document.getElementById('clipStatus').innerText = 'Text beamed to TV clipboard!';
                                document.getElementById('clipStatus').className = 'status success';
                                document.getElementById('clipText').value = '';
                            } else {
                                document.getElementById('clipStatus').innerText = 'Error sending text';
                                document.getElementById('clipStatus').className = 'status error';
                            }
                        };
                        xhr.send('text=' + encodeURIComponent(text));
                    }

                    function loadTvFiles() {
                        fetch('/files')
                            .then(res => res.json())
                            .then(files => {
                                const list = document.getElementById('tvFileList');
                                if (!files || files.length === 0) {
                                    list.innerHTML = '<p style="color: var(--text-secondary); font-size: 12px; padding: 8px;">No files on TV yet.</p>';
                                    return;
                                }
                                list.innerHTML = files.map(f => `
                                    <div class="file-item">
                                        <span>` + f.name + ` (` + (f.size / (1024*1024)).toFixed(2) + ` MB)</span>
                                        <a href="/download/` + encodeURIComponent(f.name) + `" download>Download</a>
                                    </div>
                                `).join('');
                            })
                            .catch(() => {});
                    }

                    loadTvFiles();
                </script>
            </body>
            </html>
        """.trimIndent()
        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }
}

class ProgressTrackingInputStream(
    private val wrapped: InputStream,
    private val totalBytes: Long,
    private val onProgress: (bytesRead: Long, totalBytes: Long) -> Unit
) : InputStream() {
    private var totalBytesRead = 0L
    private var lastReportTime = 0L

    override fun read(): Int {
        val b = wrapped.read()
        if (b != -1) {
            totalBytesRead++
            reportProgress()
        }
        return b
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val count = wrapped.read(b, off, len)
        if (count != -1) {
            totalBytesRead += count
            reportProgress()
        }
        return count
    }

    private fun reportProgress() {
        val now = System.currentTimeMillis()
        if (now - lastReportTime > 80 || totalBytesRead >= totalBytes) {
            lastReportTime = now
            onProgress(totalBytesRead, totalBytes)
        }
    }

    override fun close() {
        wrapped.close()
    }
}
