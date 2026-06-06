package paulokat.de.docspellviewer

import androidx.annotation.StringRes

enum class SidebarFilterId(
    val preferenceKey: String,
    @StringRes val labelRes: Int,
    val defaultEnabled: Boolean,
    val detailFieldId: DetailFieldId? = null,
    val queryField: String? = null
) {
    TAGS("tags", R.string.field_tags, true, DetailFieldId.TAGS, "tag"),
    CORRESPONDENT(
        "correspondent",
        R.string.field_organization,
        true,
        DetailFieldId.CORRESPONDENT,
        null
    ),
    CONCERNED_PERSON(
        "concerned_person",
        R.string.field_person,
        false,
        DetailFieldId.CONCERNED_PERSON,
        null
    ),
    CATEGORY("category", R.string.field_category, true, null, "cat"),
    CUSTOM_FIELDS(
        "custom_fields",
        R.string.field_custom_fields,
        false,
        DetailFieldId.CUSTOM_FIELDS,
        null
    ),
    DOCUMENT_DATE(
        "document_date",
        R.string.field_document_date,
        false,
        DetailFieldId.DOCUMENT_DATE,
        "date"
    ),
    CREATED("created", R.string.field_created, false, DetailFieldId.CREATED, "created"),
    SOURCE("source", R.string.field_source, false, DetailFieldId.SOURCE, "source"),
    FOLDER("folder", R.string.field_folder, false, DetailFieldId.FOLDER, "folder"),
    DIRECTION("direction", R.string.field_direction, false, DetailFieldId.DIRECTION, null),
    DUE_DATE("due_date", R.string.field_due_date, false, DetailFieldId.DUE_DATE, "due"),
    CONCERNED_EQUIPMENT(
        "concerned_equipment",
        R.string.field_equipment,
        false,
        DetailFieldId.CONCERNED_EQUIPMENT,
        null
    );

    val usesDedicatedPicker: Boolean
        get() = this == TAGS || this == CORRESPONDENT || this == CATEGORY

    val supportsValuePicker: Boolean
        get() = !usesDedicatedPicker &&
            (this == CUSTOM_FIELDS || this == DOCUMENT_DATE || this == CREATED || this == DUE_DATE ||
                this == DIRECTION || this == CONCERNED_PERSON || this == CONCERNED_EQUIPMENT ||
                queryField != null)

    companion object {
        private val excludedDetailFields = setOf(
            DetailFieldId.TAGS,
            DetailFieldId.CORRESPONDENT,
            DetailFieldId.CONCERNED_PERSON,
            DetailFieldId.ATTACHMENTS,
            DetailFieldId.NOTES,
            DetailFieldId.UPDATED,
            DetailFieldId.STATE
        )

        val displayOrder: List<SidebarFilterId> = buildList {
            add(TAGS)
            add(CORRESPONDENT)
            add(CONCERNED_PERSON)
            add(CATEGORY)
            DetailFieldId.displayOrder.forEach { field ->
                if (field in excludedDetailFields) {
                    return@forEach
                }
                entries.firstOrNull { it.detailFieldId == field }?.let { add(it) }
            }
        }

    }
}

data class SidebarFilterVisibility(
    val order: List<SidebarFilterId> = SidebarFilterId.displayOrder,
    val enabled: Set<SidebarFilterId> = SidebarFilterId.displayOrder
        .filter { it.defaultEnabled }
        .toSet()
) {
    fun isEnabled(filter: SidebarFilterId): Boolean = filter in enabled

    fun orderedEnabledFilters(): List<SidebarFilterId> = order.filter { isEnabled(it) }

    fun withToggled(filter: SidebarFilterId, checked: Boolean): SidebarFilterVisibility {
        val next = enabled.toMutableSet()
        if (checked) {
            next.add(filter)
        } else {
            next.remove(filter)
        }
        return copy(enabled = next)
    }

    fun moveUp(filter: SidebarFilterId): SidebarFilterVisibility {
        val index = order.indexOf(filter)
        if (index <= 0) {
            return this
        }
        val nextOrder = order.toMutableList()
        nextOrder[index - 1] = order[index].also { nextOrder[index] = order[index - 1] }
        return copy(order = nextOrder)
    }

    fun moveDown(filter: SidebarFilterId): SidebarFilterVisibility {
        val index = order.indexOf(filter)
        if (index < 0 || index >= order.lastIndex) {
            return this
        }
        val nextOrder = order.toMutableList()
        nextOrder[index + 1] = order[index].also { nextOrder[index] = order[index + 1] }
        return copy(order = nextOrder)
    }

    fun toEnabledPreferenceKeys(): Set<String> = enabled.map { it.preferenceKey }.toSet()

    fun toOrderPreferenceValue(): String = order.joinToString(",") { it.preferenceKey }

    companion object {
        fun fromStored(orderString: String?, enabledKeys: Set<String>?): SidebarFilterVisibility {
            val order = parseOrder(orderString)
            val enabled = when {
                enabledKeys == null -> order.filter { it.defaultEnabled }.toSet()
                enabledKeys.isEmpty() -> emptySet()
                else -> order.filter { it.preferenceKey in enabledKeys }.toSet()
            }
            return SidebarFilterVisibility(order = order, enabled = enabled)
        }

        /** @deprecated Use [fromStored]. */
        fun fromPreferenceKeys(keys: Set<String>?): SidebarFilterVisibility {
            return fromStored(orderString = null, enabledKeys = keys)
        }

        private fun parseOrder(orderString: String?): List<SidebarFilterId> {
            if (orderString.isNullOrBlank()) {
                return SidebarFilterId.displayOrder
            }
            val parsed = orderString
                .split(',')
                .mapNotNull { key ->
                    SidebarFilterId.entries.find { it.preferenceKey == key.trim() }
                }
            val result = parsed.toMutableList()
            SidebarFilterId.displayOrder.forEach { filter ->
                if (filter !in result) {
                    result.add(filter)
                }
            }
            return result
        }
    }
}
