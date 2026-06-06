package paulokat.de.docspellviewer

import android.content.Context
import java.io.File

/**
 * LRU-Cache fuer in der App geoeffnete PDF-Dokumente (Viewer).
 *
 * Obergrenze 150 MB: ca. 30–50 typische Dokumente, ohne den Geraetespeicher
 * stark zu belasten. Liegt im App-Cache; das System darf ihn bei Bedarf leeren.
 */
object DocumentViewerCache {
    const val MAX_CACHE_BYTES: Long = 150L * 1024 * 1024

    private const val CACHE_ROOT_NAME = "docspell_viewer"

    fun getCacheFile(context: Context, accountId: String, attachmentId: String): File {
        val dir = cacheDirectory(context, accountId)
        return File(dir, "${sanitizeAttachmentId(attachmentId)}.pdf")
    }

    fun getCachedFileIfPresent(context: Context, accountId: String, attachmentId: String): File? {
        val file = getCacheFile(context, accountId, attachmentId)
        if (file.exists() && file.length() > 0L) {
            markAccessed(file)
            return file
        }
        return null
    }

    fun store(context: Context, accountId: String, attachmentId: String, bytes: ByteArray): File {
        val dir = cacheDirectory(context, accountId)
        dir.mkdirs()
        val target = getCacheFile(context, accountId, attachmentId)
        ensureSpace(dir, bytes.size.toLong(), exclude = target)
        target.writeBytes(bytes)
        markAccessed(target)
        trimToMaxSize(dir, keep = target)
        return target
    }

    fun markAccessed(file: File) {
        file.setLastModified(System.currentTimeMillis())
    }

    private fun cacheDirectory(context: Context, accountId: String): File {
        val cleaned = accountId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(context.cacheDir, "$CACHE_ROOT_NAME/${cleaned.ifBlank { "default" }}")
    }

    private fun sanitizeAttachmentId(attachmentId: String): String {
        val cleaned = attachmentId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return cleaned.ifBlank { "attachment" }
    }

    private fun ensureSpace(dir: File, incomingBytes: Long, exclude: File) {
        val files = listCacheFiles(dir).filter { it.absolutePath != exclude.absolutePath }
        var total = files.sumOf { it.length() }
        if (total + incomingBytes <= MAX_CACHE_BYTES) {
            return
        }
        val sorted = files.sortedBy { it.lastModified() }
        for (file in sorted) {
            if (total + incomingBytes <= MAX_CACHE_BYTES) {
                break
            }
            total -= file.length()
            file.delete()
        }
    }

    private fun trimToMaxSize(dir: File, keep: File) {
        val files = listCacheFiles(dir).filter { it.absolutePath != keep.absolutePath }
        var total = files.sumOf { it.length() } + keep.length()
        if (total <= MAX_CACHE_BYTES) {
            return
        }
        for (file in files.sortedBy { it.lastModified() }) {
            if (total <= MAX_CACHE_BYTES) {
                break
            }
            total -= file.length()
            file.delete()
        }
    }

    private fun listCacheFiles(dir: File): List<File> {
        return dir.listFiles()?.filter { it.isFile && it.length() > 0L }.orEmpty()
    }

    fun getTotalSizeBytes(context: Context, accountId: String): Long {
        val dir = cacheDirectory(context, accountId)
        if (!dir.exists()) {
            return 0L
        }
        return listCacheFiles(dir).sumOf { it.length() }
    }

    fun clear(context: Context, accountId: String) {
        val dir = cacheDirectory(context, accountId)
        if (!dir.exists()) {
            return
        }
        listCacheFiles(dir).forEach { it.delete() }
    }

    fun clearAll(context: Context) {
        val root = File(context.cacheDir, CACHE_ROOT_NAME)
        if (root.exists()) {
            root.deleteRecursively()
        }
    }

    fun getLegacyRootSizeBytes(context: Context): Long {
        val root = File(context.cacheDir, CACHE_ROOT_NAME)
        if (!root.exists()) {
            return 0L
        }
        return root.listFiles()
            ?.filter { it.isFile && it.length() > 0L }
            ?.sumOf { it.length() }
            ?: 0L
    }
}
