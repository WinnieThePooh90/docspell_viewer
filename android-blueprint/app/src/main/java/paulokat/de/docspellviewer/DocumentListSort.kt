package paulokat.de.docspellviewer

import androidx.annotation.StringRes

enum class DocumentListSort(val preferenceKey: String) {
    DATE_DESC("date_desc"),
    DATE_ASC("date_asc"),
    NAME_ASC("name_asc"),
    NAME_DESC("name_desc"),
    CORRESPONDENT_ASC("correspondent_asc"),
    CORRESPONDENT_DESC("correspondent_desc");

    companion object {
        val DEFAULT: DocumentListSort = DATE_DESC

        fun fromPreferenceKey(key: String?): DocumentListSort {
            if (key == "tag_asc" || key == "tag_desc") {
                return DEFAULT
            }
            return entries.firstOrNull { it.preferenceKey == key } ?: DEFAULT
        }
    }
}

/** Docspell search API returns results in item-date order (newest first) only. */
fun DocumentListSort.usesServerPagination(): Boolean = this == DocumentListSort.DATE_DESC

@StringRes
fun DocumentListSort.labelRes(): Int = when (this) {
    DocumentListSort.DATE_DESC -> R.string.home_sort_date_desc
    DocumentListSort.DATE_ASC -> R.string.home_sort_date_asc
    DocumentListSort.NAME_ASC -> R.string.home_sort_name_asc
    DocumentListSort.NAME_DESC -> R.string.home_sort_name_desc
    DocumentListSort.CORRESPONDENT_ASC -> R.string.home_sort_correspondent_asc
    DocumentListSort.CORRESPONDENT_DESC -> R.string.home_sort_correspondent_desc
}

private fun correspondentSortKey(correspondent: String): String {
    val normalized = correspondent.trim().lowercase()
    return if (normalized.isEmpty() || normalized == "—") {
        ""
    } else {
        normalized
    }
}

fun List<DocumentRow>.sortedByDocumentListSort(sort: DocumentListSort): List<DocumentRow> {
    val comparator = when (sort) {
        DocumentListSort.DATE_DESC -> compareByDescending<DocumentRow> { it.documentDate ?: Long.MIN_VALUE }
            .thenBy { it.id }
        DocumentListSort.DATE_ASC -> compareBy<DocumentRow> { it.documentDate ?: Long.MAX_VALUE }
            .thenBy { it.id }
        DocumentListSort.NAME_ASC -> compareBy<DocumentRow> { it.name.lowercase() }.thenBy { it.id }
        DocumentListSort.NAME_DESC -> compareByDescending<DocumentRow> { it.name.lowercase() }.thenBy { it.id }
        DocumentListSort.CORRESPONDENT_ASC -> compareBy<DocumentRow> {
            correspondentSortKey(it.correspondent).ifEmpty { "\uFFFF" }
        }.thenBy { it.name.lowercase() }
        DocumentListSort.CORRESPONDENT_DESC -> compareByDescending<DocumentRow> {
            correspondentSortKey(it.correspondent).ifEmpty { "" }
        }.thenBy { it.name.lowercase() }
    }
    return sortedWith(comparator)
}
