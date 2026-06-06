package paulokat.de.docspellviewer

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class FavoriteDocumentSnapshot(
    val itemId: String,
    val name: String,
    val correspondent: String = "—",
    val corrOrgName: String? = null,
    val corrPersonName: String? = null,
    val previewUrl: String = "",
    val attachmentId: String? = null,
    val viewUrl: String? = null,
    val downloadUrl: String? = null,
    val downloadFileName: String = "dokument.pdf"
)

fun DocumentRow.toFavoriteSnapshot(): FavoriteDocumentSnapshot {
    return FavoriteDocumentSnapshot(
        itemId = id,
        name = name,
        correspondent = correspondent,
        corrOrgName = corrOrgName,
        corrPersonName = corrPersonName,
        previewUrl = previewUrl,
        attachmentId = attachmentId,
        viewUrl = viewUrl,
        downloadUrl = downloadUrl,
        downloadFileName = downloadFileName
    )
}

fun FavoriteDocumentSnapshot.toDocumentRow(
    apiBaseUrl: String,
    isOfflineAvailable: Boolean
): DocumentRow {
    val preview = previewUrl.ifBlank {
        DocspellUrls.itemPreview(apiBaseUrl, itemId)
    }
    return DocumentRow(
        id = itemId,
        name = name,
        correspondent = correspondent,
        corrOrgName = corrOrgName,
        corrPersonName = corrPersonName,
        previewUrl = preview,
        attachmentId = attachmentId,
        viewUrl = viewUrl,
        downloadUrl = downloadUrl,
        downloadFileName = downloadFileName,
        isOfflineAvailable = isOfflineAvailable
    )
}

class FavoriteDocumentStore(
    context: Context,
    private val accountId: String
) {
    private val prefs = context.applicationContext
        .getSharedPreferences(prefsName(accountId), Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(
        List::class.java,
        FavoriteDocumentSnapshot::class.java
    )
    private val listAdapter = moshi.adapter<List<FavoriteDocumentSnapshot>>(listType)

    fun isFavorite(itemId: String): Boolean {
        return loadAll().any { it.itemId == itemId }
    }

    fun toggle(snapshot: FavoriteDocumentSnapshot): Boolean {
        val current = loadAll().toMutableList()
        val index = current.indexOfFirst { it.itemId == snapshot.itemId }
        return if (index >= 0) {
            current.removeAt(index)
            saveAll(current)
            false
        } else {
            current.add(snapshot)
            saveAll(current.sortedBy { it.name.lowercase() })
            true
        }
    }

    fun listAll(): List<FavoriteDocumentSnapshot> {
        return loadAll().sortedBy { it.name.lowercase() }
    }

    private fun loadAll(): List<FavoriteDocumentSnapshot> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        return runCatching { listAdapter.fromJson(json) }.getOrNull().orEmpty()
    }

    private fun saveAll(items: List<FavoriteDocumentSnapshot>) {
        val json = listAdapter.toJson(items)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }

    fun deleteAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_PREFIX = "docspell_favorites_"
        private const val KEY_FAVORITES = "favorite_documents"

        fun prefsName(accountId: String): String {
            val cleaned = accountId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
            return "$PREFS_PREFIX${cleaned.ifBlank { "default" }}"
        }
    }
}
