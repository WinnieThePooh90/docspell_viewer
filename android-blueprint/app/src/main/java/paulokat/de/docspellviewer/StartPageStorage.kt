package paulokat.de.docspellviewer

import androidx.annotation.StringRes

object StartPageStorage {
    const val SEARCH = "search"

    data class Option(
        val storageKey: String,
        @StringRes val labelRes: Int
    )

    fun selectableOptions(): List<Option> = buildList {
        add(Option(SEARCH, R.string.start_page_search))
        SidebarFilterId.displayOrder
            .filter { it.usesDedicatedPicker || it.supportsValuePicker }
            .forEach { filter ->
                add(Option(filter.preferenceKey, filter.labelRes))
            }
    }

    fun normalizeStored(value: String?): String {
        val key = value?.trim().orEmpty()
        if (key.isEmpty() || key == SEARCH) {
            return SEARCH
        }
        return if (selectableOptions().any { it.storageKey == key }) key else SEARCH
    }

    fun toSidebarFilter(storageKey: String): SidebarFilterId? {
        if (storageKey == SEARCH) {
            return null
        }
        return SidebarFilterId.entries.find { it.preferenceKey == storageKey }
    }

    fun isSearchPage(storageKey: String): Boolean = storageKey == SEARCH
}
