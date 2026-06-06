package paulokat.de.docspellviewer

import android.content.Context
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class FilterPickerOption(
    val display: String,
    val queryValue: String
)

private val displayDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun ItemSummary.collectSidebarFilterOptions(
    filterId: SidebarFilterId,
    context: Context
): List<FilterPickerOption> {
    return when (filterId) {
        SidebarFilterId.SOURCE -> listOfNotNull(
            source?.trim()?.takeIf { it.isNotEmpty() }?.let { FilterPickerOption(it, it) }
        )
        SidebarFilterId.FOLDER -> listOfNotNull(
            folder?.name?.trim()?.takeIf { it.isNotEmpty() }?.let { FilterPickerOption(it, it) }
        )
        SidebarFilterId.DIRECTION -> listOfNotNull(
            direction?.trim()?.takeIf { it.isNotEmpty() }?.let { raw ->
                FilterPickerOption(display = formatDirectionLabel(context, raw), queryValue = raw)
            }
        )
        SidebarFilterId.CONCERNED_PERSON -> listOfNotNull(
            concPerson?.name?.trim()?.takeIf { it.isNotEmpty() }?.let { FilterPickerOption(it, it) }
        )
        SidebarFilterId.CONCERNED_EQUIPMENT -> listOfNotNull(
            concEquipment?.name?.trim()?.takeIf { it.isNotEmpty() }?.let { FilterPickerOption(it, it) }
        )
        SidebarFilterId.DOCUMENT_DATE -> listOfNotNull(dateOption(date))
        SidebarFilterId.DUE_DATE -> listOfNotNull(dateOption(dueDate))
        else -> emptyList()
    }
}

internal fun ItemDetail.collectSidebarFilterOptions(filterId: SidebarFilterId): List<FilterPickerOption> {
    return when (filterId) {
        SidebarFilterId.CREATED -> listOfNotNull(dateOption(created))
        else -> emptyList()
    }
}

private fun dateOption(epochMillis: Long?): FilterPickerOption? {
    if (epochMillis == null || epochMillis <= 0L) {
        return null
    }
    val localDate = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    return FilterPickerOption(
        display = localDate.format(displayDateFormatter),
        queryValue = localDate.format(isoDateFormatter)
    )
}

private fun formatDirectionLabel(context: Context, raw: String): String {
    return when (raw.lowercase()) {
        "incoming" -> context.getString(R.string.direction_incoming)
        "outgoing" -> context.getString(R.string.direction_outgoing)
        else -> raw
    }
}
