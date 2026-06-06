package paulokat.de.docspellviewer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.io.File

typealias BytesProgressCallback = (bytesRead: Long, contentLength: Long) -> Unit

class DocumentActionHelper(
    private val context: Context,
    private val tokenStore: TokenStore,
    private val sessionManager: DocspellSessionManager
) {
    private val httpClient: OkHttpClient by lazy {
        DocspellApiFactory.createAuthenticatedClient(
            tokenStore = tokenStore,
            sessionManager = sessionManager
        )
    }

    suspend fun fetchPdfForViewer(
        pdfUrl: String,
        accountId: String,
        attachmentId: String,
        offlineItemId: String? = null,
        offlineStore: OfflineDocumentStore? = null,
        onProgress: BytesProgressCallback? = null
    ): Result<File> {
        return withContext(Dispatchers.IO) {
            runCatching {
                onProgress?.invoke(0L, -1L)
                if (!offlineItemId.isNullOrBlank() && offlineStore != null) {
                    offlineStore.getOfflineFile(offlineItemId)?.let { file ->
                        reportLoadedFile(file, onProgress)
                        return@runCatching file
                    }
                }
                requireAuthToken()
                DocumentViewerCache.getCachedFileIfPresent(context, accountId, attachmentId)?.let { file ->
                    reportLoadedFile(file, onProgress)
                    return@runCatching file
                }
                downloadAndCachePdf(pdfUrl, accountId, attachmentId, onProgress)
            }
        }
    }

    suspend fun downloadPdfBytes(
        pdfUrl: String,
        onProgress: BytesProgressCallback? = null
    ): Result<ByteArray> {
        return downloadAuthenticatedBytes(pdfUrl, onProgress)
    }

    suspend fun downloadAttachmentBytes(
        downloadUrl: String,
        onProgress: BytesProgressCallback? = null
    ): Result<ByteArray> {
        return downloadAuthenticatedBytes(downloadUrl, onProgress)
    }

    private suspend fun downloadAuthenticatedBytes(
        url: String,
        onProgress: BytesProgressCallback?
    ): Result<ByteArray> {
        requireAuthToken()
        return withContext(Dispatchers.IO) {
            runCatching {
                val request = authenticatedRequest(url)
                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    error(context.getString(R.string.error_download_http, response.code))
                }
                val body = response.body ?: error(context.getString(R.string.error_empty_response))
                body.use { readBodyWithProgress(it, response, onProgress) }
            }
        }
    }

    private fun downloadAndCachePdf(
        pdfUrl: String,
        accountId: String,
        attachmentId: String,
        onProgress: BytesProgressCallback?
    ): File {
        val request = authenticatedRequest(pdfUrl)
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            error(context.getString(R.string.error_load_http, response.code))
        }
        val body = response.body ?: error(context.getString(R.string.error_empty_response))
        val bytes = body.use { readBodyWithProgress(it, response, onProgress) }
        return DocumentViewerCache.store(context, accountId, attachmentId, bytes)
    }

    private fun readBodyWithProgress(
        body: ResponseBody,
        response: Response,
        onProgress: BytesProgressCallback?
    ): ByteArray {
        val contentLength = response.header("Content-Length")?.toLongOrNull()
            ?.takeIf { it > 0L }
            ?: body.contentLength()
        val source = body.source()
        val buffer = Buffer()
        var totalRead = 0L
        val chunkSize = 8 * 1024L
        try {
            while (true) {
                val read = source.read(buffer, chunkSize)
                if (read == -1L) {
                    break
                }
                totalRead += read
                onProgress?.invoke(totalRead, contentLength)
            }
            val finalLength = if (contentLength > 0L) contentLength else totalRead
            onProgress?.invoke(finalLength, finalLength)
        } finally {
            source.close()
        }
        return buffer.readByteArray()
    }

    private fun reportLoadedFile(file: File, onProgress: BytesProgressCallback?) {
        val size = file.length().coerceAtLeast(1L)
        onProgress?.invoke(size, size)
    }

    suspend fun fetchAttachmentToCache(
        downloadUrl: String,
        cacheKey: String,
        fileName: String,
        cacheSubDir: String = "attachments"
    ): Result<File> {
        requireAuthToken()

        return withContext(Dispatchers.IO) {
            runCatching {
                val ext = fileName.substringAfterLast('.', "").lowercase()
                    .takeIf { it.matches(Regex("[a-z0-9]{1,8}")) }
                    ?: "bin"
                val dir = File(context.cacheDir, cacheSubDir).apply { mkdirs() }
                val cacheFile = File(dir, "${cacheKey.hashCode()}.$ext")
                if (cacheFile.exists() && cacheFile.length() > 0L) {
                    return@runCatching cacheFile
                }

                val request = authenticatedRequest(downloadUrl)
                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    error(context.getString(R.string.error_load_http, response.code))
                }
                val body = response.body?.bytes() ?: error(context.getString(R.string.error_empty_response))
                cacheFile.writeBytes(body)
                cacheFile
            }
        }
    }

    suspend fun downloadToUri(downloadUrl: String, destinationUri: Uri): Result<String> {
        requireAuthToken()

        return withContext(Dispatchers.IO) {
            runCatching {
                val request = authenticatedRequest(downloadUrl)
                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    error(context.getString(R.string.error_download_http, response.code))
                }
                val body = response.body?.bytes() ?: error(context.getString(R.string.error_empty_response))
                context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                    output.write(body)
                } ?: error(context.getString(R.string.error_file_write_failed))
                resolveDisplayName(destinationUri) ?: "document"
            }
        }
    }

    fun showDownloadSuccess(fileName: String) {
        toast(context.getString(R.string.toast_saved_as, fileName))
    }

    fun showMessage(message: String) {
        toast(message)
    }

    fun showError(message: String) {
        toast(message)
    }

    private fun requireAuthToken() {
        if (tokenStore.getToken().isNullOrBlank()) {
            error(context.getString(R.string.error_not_logged_in))
        }
    }

    private fun authenticatedRequest(url: String): Request {
        val token = tokenStore.getToken().orEmpty()
        return Request.Builder()
            .url(url)
            .header("X-Docspell-Auth", token)
            .build()
    }

    private fun resolveDisplayName(uri: Uri): String? {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) {
                    return cursor.getString(index)
                }
            }
        return uri.lastPathSegment
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
