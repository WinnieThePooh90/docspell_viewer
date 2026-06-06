package paulokat.de.docspellviewer

import androidx.annotation.StringRes

enum class DetailFieldId(
    val preferenceKey: String,
    @StringRes val labelRes: Int,
    val defaultEnabled: Boolean
) {
    CUSTOM_FIELDS("custom_fields", R.string.field_custom_fields, true),
    DOCUMENT_DATE("document_date", R.string.field_document_date, true),
    CORRESPONDENT("correspondent", R.string.field_organization, true),
    TAGS("tags", R.string.field_tags, true),
    ATTACHMENTS("attachments", R.string.field_attachments, true),
    CREATED("created", R.string.field_created, false),
    UPDATED("updated", R.string.field_updated, false),
    SOURCE("source", R.string.field_source, false),
    FOLDER("folder", R.string.field_folder, false),
    DIRECTION("direction", R.string.field_direction, false),
    STATE("state", R.string.field_state, false),
    DUE_DATE("due_date", R.string.field_due_date, false),
    NOTES("notes", R.string.field_notes, false),
    CONCERNED_PERSON("concerned_person", R.string.field_person, false),
    CONCERNED_EQUIPMENT("concerned_equipment", R.string.field_equipment, false);

    companion object {
        val displayOrder: List<DetailFieldId> = listOf(
            CUSTOM_FIELDS,
            DOCUMENT_DATE,
            CORRESPONDENT,
            CONCERNED_PERSON,
            TAGS,
            ATTACHMENTS,
            CREATED,
            UPDATED,
            SOURCE,
            FOLDER,
            DIRECTION,
            STATE,
            DUE_DATE,
            NOTES,
            CONCERNED_EQUIPMENT
        )

    }
}

data class DetailFieldVisibility(
    val order: List<DetailFieldId> = DetailFieldId.displayOrder,
    val enabled: Set<DetailFieldId> = DetailFieldId.displayOrder
        .filter { it.defaultEnabled }
        .toSet()
) {
    fun isEnabled(field: DetailFieldId): Boolean = field in enabled

    fun orderedEnabledFields(): List<DetailFieldId> = order.filter { isEnabled(it) }

    fun withToggled(field: DetailFieldId, checked: Boolean): DetailFieldVisibility {
        val next = enabled.toMutableSet()
        if (checked) {
            next.add(field)
        } else {
            next.remove(field)
        }
        return copy(enabled = next)
    }

    fun moveUp(field: DetailFieldId): DetailFieldVisibility {
        val index = order.indexOf(field)
        if (index <= 0) {
            return this
        }
        val nextOrder = order.toMutableList()
        nextOrder[index - 1] = order[index].also { nextOrder[index] = order[index - 1] }
        return copy(order = nextOrder)
    }

    fun moveDown(field: DetailFieldId): DetailFieldVisibility {
        val index = order.indexOf(field)
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
        fun fromStored(orderString: String?, enabledKeys: Set<String>?): DetailFieldVisibility {
            val order = parseOrder(orderString)
            val enabled = when {
                enabledKeys == null -> order.filter { it.defaultEnabled }.toSet()
                enabledKeys.isEmpty() -> emptySet()
                else -> order.filter { it.preferenceKey in enabledKeys }.toSet()
            }
            return DetailFieldVisibility(order = order, enabled = enabled)
        }

        /** @deprecated Use [fromStored]. */
        fun fromPreferenceKeys(keys: Set<String>): DetailFieldVisibility {
            return fromStored(orderString = null, enabledKeys = keys)
        }

        private fun parseOrder(orderString: String?): List<DetailFieldId> {
            if (orderString.isNullOrBlank()) {
                return DetailFieldId.displayOrder
            }
            val parsed = orderString
                .split(',')
                .mapNotNull { key ->
                    DetailFieldId.entries.find { it.preferenceKey == key.trim() }
                }
            val result = parsed.toMutableList()
            DetailFieldId.displayOrder.forEach { field ->
                if (field !in result) {
                    result.add(field)
                }
            }
            return result
        }
    }
}
