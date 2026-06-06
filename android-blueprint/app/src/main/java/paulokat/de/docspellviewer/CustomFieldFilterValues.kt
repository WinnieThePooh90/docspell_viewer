package paulokat.de.docspellviewer

import android.content.Context

internal fun ItemDetail.collectCustomFieldValues(fieldName: String): List<String> {
    val target = fieldName.trim()
    if (target.isEmpty()) {
        return emptyList()
    }
    return customfields.mapNotNull { field ->
        val matchesName = field.name?.equals(target, ignoreCase = true) == true
        val matchesLabel = field.label?.equals(target, ignoreCase = true) == true
        if (!matchesName && !matchesLabel) {
            null
        } else {
            field.value?.trim()?.takeIf { it.isNotEmpty() }
        }
    }
}

internal fun customFieldTypeLabel(context: Context, ftype: String?): String? {
    return when (ftype?.lowercase()) {
        "text" -> context.getString(R.string.custom_field_type_text)
        "numeric" -> context.getString(R.string.custom_field_type_numeric)
        "money" -> context.getString(R.string.custom_field_type_money)
        "date" -> context.getString(R.string.custom_field_type_date)
        "bool" -> context.getString(R.string.custom_field_type_bool)
        else -> ftype
    }
}
