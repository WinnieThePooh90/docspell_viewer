package paulokat.de.docspellviewer

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

data class OfflineDocumentMeta(
    val itemId: String,
    val attachmentId: String,
    val name: String,
    val correspondent: String = "—",
    val corrOrgName: String? = null,
    val corrPersonName: String? = null,
    val downloadFileName: String = "dokument.pdf"
)

fun OfflineDocumentMeta.toDocumentRow(apiBaseUrl: String): DocumentRow {
    return DocumentRow(
        id = itemId,
        name = name.displayText(),
        correspondent = correspondent,
        corrOrgName = corrOrgName,
        corrPersonName = corrPersonName,
        previewUrl = DocspellUrls.itemPreview(apiBaseUrl, itemId),
        attachmentId = attachmentId,
        viewUrl = DocspellUrls.attachmentView(apiBaseUrl, attachmentId),
        downloadUrl = DocspellUrls.attachmentDownload(apiBaseUrl, attachmentId),
        downloadFileName = downloadFileName,
        isOfflineAvailable = true
    )
}

/**
 * Persistente Offline-Kopien: PDF-Viewer-Datei plus alle Anhaenge eines Items.
 */
class OfflineDocumentStore(
    context: Context,
    private val accountId: String
) {
    private val appContext = context.applicationContext
    private val offlineDir = File(appContext.filesDir, "$OFFLINE_ROOT/${sanitizeAccountId(accountId)}")
    private val prefs = appContext.getSharedPreferences(prefsName(accountId), Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val metaAdapter = moshi.adapter(OfflineDocumentMeta::class.java)
    private val detailContentAdapter = moshi.adapter(DocumentDetailContent::class.java)

    fun isAvailable(itemId: String): Boolean {
        return resolveViewerPdfFile(itemId) != null
    }

    /** PDF fuer den In-App-Viewer (konvertierte Ausgabe des Primaer-Anhangs). */
    fun getOfflineFile(itemId: String): File? {
        val file = resolveViewerPdfFile(itemId) ?: return null
        if (file.length() > 0L) {
            return file
        }
        file.delete()
        if (!hasAnyOfflineData(itemId)) {
            clearPrefsForItem(itemId)
        }
        return null
    }

    fun getOfflineAttachmentFile(
        itemId: String,
        attachmentId: String,
        fileName: String? = null
    ): File? {
        if (!isAvailable(itemId)) {
            return null
        }
        if (!fileName.isNullOrBlank()) {
            val exact = attachmentFile(itemId, attachmentId, fileName)
            if (exact.exists() && exact.length() > 0L) {
                return exact
            }
        }
        val prefix = "${sanitizeAttachmentId(attachmentId)}."
        return itemDirectory(itemId).listFiles()
            ?.firstOrNull { file ->
                file.isFile && file.name.startsWith(prefix) && file.length() > 0L
            }
    }

    fun getAvailableItemIds(): Set<String> {
        val ids = mutableSetOf<String>()
        prefs.all.keys.forEach { key ->
            when {
                key.startsWith(META_KEY_PREFIX) -> ids.add(key.removePrefix(META_KEY_PREFIX))
                key.startsWith(LEGACY_KEY_PREFIX) -> ids.add(key.removePrefix(LEGACY_KEY_PREFIX))
            }
        }
        return ids.filter { isAvailable(it) }.toSet()
    }

    fun listDocuments(): List<OfflineDocumentMeta> {
        return getAvailableItemIds()
            .mapNotNull { loadMeta(it) }
            .sortedBy { it.name.lowercase() }
    }

    fun save(
        itemId: String,
        attachmentId: String,
        pdfBytes: ByteArray,
        meta: OfflineDocumentMeta,
        detailContent: DocumentDetailContent? = null
    ) {
        offlineDir.mkdirs()
        itemDirectory(itemId).mkdirs()
        legacyViewerPdfFile(itemId).delete()
        viewerPdfFile(itemId).writeBytes(pdfBytes)
        val json = metaAdapter.toJson(meta.copy(itemId = itemId, attachmentId = attachmentId))
        val editor = prefs.edit().putString(metaPrefsKey(itemId), json)
        if (detailContent != null) {
            editor.putString(contentPrefsKey(itemId), detailContentAdapter.toJson(detailContent))
        }
        editor.apply()
    }

    fun saveAttachment(
        itemId: String,
        attachmentId: String,
        bytes: ByteArray,
        fileName: String
    ) {
        if (!isAvailable(itemId)) {
            return
        }
        itemDirectory(itemId).mkdirs()
        attachmentFile(itemId, attachmentId, fileName).writeBytes(bytes)
    }

    fun updateDetailContent(itemId: String, detailContent: DocumentDetailContent) {
        if (!isAvailable(itemId)) {
            return
        }
        prefs.edit()
            .putString(contentPrefsKey(itemId), detailContentAdapter.toJson(detailContent))
            .apply()
    }

    fun loadDetailContent(itemId: String, apiBaseUrl: String): DocumentDetailContent? {
        if (!isAvailable(itemId)) {
            return null
        }
        val json = prefs.getString(contentPrefsKey(itemId), null)
        if (json != null) {
            val parsed = runCatching { detailContentAdapter.fromJson(json) }.getOrNull()
            if (parsed != null) {
                return parsed.withRefreshedAttachmentUrls(apiBaseUrl)
            }
        }
        return loadMeta(itemId)?.toOfflineDetailContent(apiBaseUrl)
    }

    fun delete(itemId: String) {
        legacyViewerPdfFile(itemId).delete()
        itemDirectory(itemId).deleteRecursively()
        clearPrefsForItem(itemId)
    }

    fun getDocumentCount(): Int = getAvailableItemIds().size

    fun getTotalStorageBytes(): Long {
        var total = 0L
        if (offlineDir.exists()) {
            total += directoryTreeBytes(offlineDir)
        }
        val prefsFile = File(
            appContext.applicationInfo.dataDir,
            "shared_prefs/${prefsName(accountId)}.xml"
        )
        if (prefsFile.isFile) {
            total += prefsFile.length()
        }
        return total
    }

    fun deleteAll() {
        if (offlineDir.exists()) {
            offlineDir.deleteRecursively()
        }
        prefs.edit().clear().commit()
    }

    private fun directoryTreeBytes(dir: File): Long {
        return dir.walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }
    }

    fun getAttachmentId(itemId: String): String? {
        return loadMeta(itemId)?.attachmentId
    }

    private fun loadMeta(itemId: String): OfflineDocumentMeta? {
        if (!isAvailable(itemId)) {
            return null
        }
        val json = prefs.getString(metaPrefsKey(itemId), null)
        if (json != null) {
            val parsed = runCatching { metaAdapter.fromJson(json) }.getOrNull()
            if (parsed != null) {
                return parsed
            }
        }
        val legacyAttachment = prefs.getString(legacyPrefsKey(itemId), null)?.trim()
        if (!legacyAttachment.isNullOrEmpty()) {
            return OfflineDocumentMeta(
                itemId = itemId,
                attachmentId = legacyAttachment,
                name = "Dokument"
            )
        }
        return null
    }

    private fun resolveViewerPdfFile(itemId: String): File? {
        val legacy = legacyViewerPdfFile(itemId)
        if (legacy.exists() && legacy.length() > 0L) {
            return legacy
        }
        val viewer = viewerPdfFile(itemId)
        if (viewer.exists() && viewer.length() > 0L) {
            return viewer
        }
        return null
    }

    private fun hasAnyOfflineData(itemId: String): Boolean {
        return legacyViewerPdfFile(itemId).exists() ||
            itemDirectory(itemId).exists()
    }

    private fun itemDirectory(itemId: String): File {
        return File(offlineDir, sanitizeItemId(itemId))
    }

    private fun viewerPdfFile(itemId: String): File {
        return File(itemDirectory(itemId), VIEWER_PDF_NAME)
    }

    private fun legacyViewerPdfFile(itemId: String): File {
        return File(offlineDir, "${sanitizeItemId(itemId)}.pdf")
    }

    private fun attachmentFile(itemId: String, attachmentId: String, fileName: String): File {
        val ext = DownloadFileNames.extension(fileName)
        return File(itemDirectory(itemId), "${sanitizeAttachmentId(attachmentId)}.$ext")
    }

    private fun clearPrefsForItem(itemId: String) {
        prefs.edit()
            .remove(metaPrefsKey(itemId))
            .remove(legacyPrefsKey(itemId))
            .remove(contentPrefsKey(itemId))
            .apply()
    }

    private fun sanitizeItemId(itemId: String): String {
        val cleaned = itemId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return cleaned.ifBlank { "item" }
    }

    private fun sanitizeAttachmentId(attachmentId: String): String {
        val cleaned = attachmentId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return cleaned.ifBlank { "attachment" }
    }

    private fun metaPrefsKey(itemId: String): String = "$META_KEY_PREFIX$itemId"

    private fun contentPrefsKey(itemId: String): String = "$CONTENT_KEY_PREFIX$itemId"

    private fun legacyPrefsKey(itemId: String): String = "$LEGACY_KEY_PREFIX$itemId"

    companion object {
        private const val OFFLINE_ROOT = "docspell_offline"
        private const val PREFS_PREFIX = "docspell_offline_index_"
        private const val META_KEY_PREFIX = "meta_"
        private const val CONTENT_KEY_PREFIX = "content_"
        private const val LEGACY_KEY_PREFIX = "item_"
        private const val VIEWER_PDF_NAME = "viewer.pdf"

        fun prefsName(accountId: String): String = "$PREFS_PREFIX${sanitizeAccountId(accountId)}"

        fun clearLegacyRoot(context: Context) {
            val root = File(context.applicationContext.filesDir, OFFLINE_ROOT)
            if (root.exists()) {
                root.deleteRecursively()
            }
        }

        fun getLegacyRootSizeBytes(context: Context, accountIds: Collection<String>): Long {
            val appContext = context.applicationContext
            val root = File(appContext.filesDir, OFFLINE_ROOT)
            if (!root.exists()) {
                return 0L
            }
            val excludedDirs = accountIds.map { sanitizeAccountId(it) }.toSet()
            var total = 0L
            root.listFiles()?.forEach { entry ->
                when {
                    entry.isFile -> total += entry.length()
                    entry.isDirectory && entry.name !in excludedDirs ->
                        total += directoryTreeBytes(entry)
                }
            }
            return total
        }

        private fun directoryTreeBytes(dir: File): Long {
            return dir.walkTopDown()
                .filter { it.isFile }
                .sumOf { it.length() }
        }

        private fun sanitizeAccountId(accountId: String): String {
            val cleaned = accountId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
            return cleaned.ifBlank { "default" }
        }
    }
}

private fun DocumentDetailContent.withRefreshedAttachmentUrls(apiBaseUrl: String): DocumentDetailContent {
    return copy(
        attachments = attachments.map { attachment ->
            attachment.copy(
                name = attachment.name.displayText(),
                downloadUrl = DocspellUrls.attachmentDownload(apiBaseUrl, attachment.id),
                isAudio = DownloadFileNames.isAudioFileName(attachment.downloadFileName)
            )
        },
        correspondent = correspondent?.displayText(),
        tags = tags?.displayText(),
        customFields = customFields.map { field ->
            field.copy(
                label = field.label.displayText(),
                value = field.value.displayText()
            )
        }
    )
}
