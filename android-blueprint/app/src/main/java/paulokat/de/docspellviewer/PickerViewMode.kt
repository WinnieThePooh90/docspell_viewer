package paulokat.de.docspellviewer

enum class PickerViewMode {
    LIST,
    TILES;

    companion object {
        fun fromStorage(value: String?): PickerViewMode {
            return entries.firstOrNull { it.name == value } ?: LIST
        }
    }
}

object PickerPageKey {
    const val TAGS = "tags"
    const val CORRESPONDENTS = "correspondent"
    const val CATEGORIES = "category"
    const val CUSTOM_FIELD_VALUES = "custom_fields_values"

    fun forFilter(filter: SidebarFilterId): String = filter.preferenceKey
}
