package paulokat.de.docspellviewer

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

data class DetailAttachmentRow(
    val id: String,
    val name: String,
    val downloadUrl: String,
    val downloadFileName: String,
    val isAudio: Boolean = false
)

data class DocumentDetailContent(
    val customFields: List<DetailFieldRow> = emptyList(),
    val documentDate: String? = null,
    val created: String? = null,
    val updated: String? = null,
    val source: String? = null,
    val folder: String? = null,
    val direction: String? = null,
    val state: String? = null,
    val dueDate: String? = null,
    val notes: String? = null,
    val correspondent: String? = null,
    val concernedPerson: String? = null,
    val concernedEquipment: String? = null,
    val tags: String? = null,
    val attachments: List<DetailAttachmentRow> = emptyList()
) {
    fun hasVisibleMetadata(visibility: DetailFieldVisibility): Boolean {
        if (visibility.isEnabled(DetailFieldId.CUSTOM_FIELDS) && customFields.isNotEmpty()) {
            return true
        }
        if (visibility.isEnabled(DetailFieldId.ATTACHMENTS) && attachments.isNotEmpty()) {
            return true
        }
        return listOf(
            DetailFieldId.DOCUMENT_DATE to documentDate,
            DetailFieldId.CREATED to created,
            DetailFieldId.UPDATED to updated,
            DetailFieldId.SOURCE to source,
            DetailFieldId.FOLDER to folder,
            DetailFieldId.DIRECTION to direction,
            DetailFieldId.STATE to state,
            DetailFieldId.DUE_DATE to dueDate,
            DetailFieldId.NOTES to notes,
            DetailFieldId.CORRESPONDENT to correspondent,
            DetailFieldId.CONCERNED_PERSON to concernedPerson,
            DetailFieldId.CONCERNED_EQUIPMENT to concernedEquipment,
            DetailFieldId.TAGS to tags
        ).any { (field, value) -> visibility.isEnabled(field) && !value.isNullOrBlank() }
    }
}

fun OfflineDocumentMeta.toOfflineDetailContent(apiBaseUrl: String): DocumentDetailContent {
    val attachment = DetailAttachmentRow(
        id = attachmentId,
        name = downloadFileName.displayText(),
        downloadUrl = DocspellUrls.attachmentDownload(apiBaseUrl, attachmentId),
        downloadFileName = downloadFileName,
        isAudio = DownloadFileNames.isAudioFileName(downloadFileName)
    )
    return DocumentDetailContent(
        correspondent = correspondent.takeIf { it != "—" },
        attachments = listOf(attachment)
    )
}

fun ItemDetail.toDetailContent(apiBaseUrl: String): DocumentDetailContent {
    val customFields = customfields.mapNotNull { field ->
        val label = field.label?.trim().takeIf { !it.isNullOrEmpty() }
            ?: field.name?.trim().takeIf { !it.isNullOrEmpty() }
            ?: "Feld"
        val value = field.value?.trim().orEmpty()
        if (value.isEmpty()) {
            null
        } else {
            DetailFieldRow(label = label, value = value)
        }
    }

    val tagText = tags
        .mapNotNull { tag ->
            val name = tag.name?.trim().orEmpty().displayText()
            if (name.isEmpty()) {
                return@mapNotNull null
            }
            val category = tag.category?.trim().orEmpty().displayText()
            if (category.isNotEmpty()) "$name ($category)" else name
        }
        .distinct()
        .joinToString(", ")
        .takeIf { it.isNotEmpty() }

    val attachmentRows = attachments
        .sortedBy { it.position ?: Int.MAX_VALUE }
        .mapNotNull { attachment ->
            val id = attachment.id.trim()
            if (id.isEmpty()) {
                return@mapNotNull null
            }
            val rawName = attachment.name?.trim()?.takeIf { it.isNotEmpty() } ?: "Anhang"
            val displayName = rawName.displayText()
            DetailAttachmentRow(
                id = id,
                name = displayName,
                downloadUrl = DocspellUrls.attachmentDownload(apiBaseUrl, id),
                downloadFileName = rawName,
                isAudio = DownloadFileNames.isAudioFileName(rawName)
            )
        }

    return DocumentDetailContent(
        customFields = customFields.map { field ->
            field.copy(
                label = field.label.displayText(),
                value = field.value.displayText()
            )
        },
        documentDate = formatEpochMillis(itemDate),
        created = formatEpochMillis(created),
        updated = formatEpochMillis(updated),
        source = source?.trim()?.takeIf { it.isNotEmpty() }?.displayText(),
        folder = folder?.name?.trim()?.takeIf { it.isNotEmpty() }?.displayText(),
        direction = formatDirection(direction),
        state = state?.trim()?.takeIf { it.isNotEmpty() }?.displayText(),
        dueDate = formatEpochMillis(dueDate),
        notes = notes?.trim()?.takeIf { it.isNotEmpty() }?.displayText(),
        correspondent = formatCorrespondent(corrOrg?.name, corrPerson?.name)?.displayText(),
        concernedPerson = corrPersonLabel(concPerson?.name),
        concernedEquipment = concEquipment?.name?.trim()?.takeIf { it.isNotEmpty() }?.displayText(),
        tags = tagText?.displayText(),
        attachments = attachmentRows
    )
}

private fun corrPersonLabel(name: String?): String? {
    return name?.trim()?.takeIf { it.isNotEmpty() }?.displayText()
}

private fun formatDirection(direction: String?): String? {
    val raw = direction?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return when (raw.lowercase()) {
        "incoming" -> "Eingehend"
        "outgoing" -> "Ausgehend"
        else -> raw.displayText()
    }
}

fun ItemDetail.toViewerDocument(apiBaseUrl: String, previewUrl: String): DocumentRow? {
    val primary = attachments
        .sortedBy { it.position ?: Int.MAX_VALUE }
        .firstOrNull() ?: return null
    val attachmentId = primary.id.trim()
    if (attachmentId.isEmpty()) {
        return null
    }
    val downloadName = primary.name?.trim()?.takeIf { it.isNotEmpty() } ?: name
    return DocumentRow(
        id = id,
        name = name.displayText(),
        correspondent = formatCorrespondent(corrOrg?.name, corrPerson?.name)?.displayText() ?: "—",
        corrOrgName = corrOrg?.name?.trim()?.takeIf { it.isNotEmpty() }?.displayText(),
        corrPersonName = corrPerson?.name?.trim()?.takeIf { it.isNotEmpty() }?.displayText(),
        previewUrl = previewUrl,
        attachmentId = attachmentId,
        viewUrl = DocspellUrls.attachmentView(apiBaseUrl, attachmentId),
        downloadUrl = DocspellUrls.attachmentDownload(apiBaseUrl, attachmentId),
        downloadFileName = downloadName
    )
}

private fun formatCorrespondent(orgName: String?, personName: String?): String? {
    val org = orgName?.trim().orEmpty().displayText()
    val person = personName?.trim().orEmpty().displayText()
    return when {
        org.isNotEmpty() && person.isNotEmpty() -> "$org / $person"
        org.isNotEmpty() -> org
        person.isNotEmpty() -> person
        else -> null
    }
}

private fun formatEpochMillis(value: Long?): String? {
    if (value == null || value <= 0L) {
        return null
    }
    return Instant.ofEpochMilli(value)
        .atZone(ZoneId.systemDefault())
        .format(dateFormatter)
}
