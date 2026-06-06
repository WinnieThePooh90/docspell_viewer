package paulokat.de.docspellviewer

/**
 * Baut gültige Docspell-Queries für Sidebar-Filter.
 * Siehe https://docspell.org/docs/query/
 */
object FilterQueryBuilder {
    fun build(filterId: SidebarFilterId, queryValue: String): String? {
        val value = queryValue.trim()
        if (value.isEmpty()) {
            return null
        }
        return when (filterId) {
            SidebarFilterId.DOCUMENT_DATE -> dateEquals("date", value)
            SidebarFilterId.CREATED -> dateEquals("created", value)
            SidebarFilterId.DUE_DATE -> dateEquals("due", value)
            SidebarFilterId.DIRECTION -> directionQuery(value)
            SidebarFilterId.CONCERNED_PERSON -> nameQuery("conc.pers.name", value)
            SidebarFilterId.CONCERNED_EQUIPMENT -> nameQuery("conc.equip.name", value)
            SidebarFilterId.CUSTOM_FIELDS -> null
            SidebarFilterId.SOURCE,
            SidebarFilterId.FOLDER -> {
                val field = filterId.queryField ?: return null
                DocspellQueryNormalizer.buildFieldEqualsQuery(field, value)
            }
            else -> null
        }
    }

    /** Ein Tag im Format YYYY-MM-DD (Docspell-Datumsyntax). */
    private fun dateEquals(field: String, isoDate: String): String {
        val normalized = normalizeIsoDate(isoDate) ?: return DocspellQueryNormalizer.buildFieldEqualsQuery(field, isoDate)
        return DocspellQueryNormalizer.buildFieldEqualsQuery(field, normalized)
    }

    private fun normalizeIsoDate(value: String): String? {
        if (value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            return value
        }
        if (value.startsWith("ms") && value.length > 2) {
            return value
        }
        return null
    }

    private fun directionQuery(rawDirection: String): String {
        return when (rawDirection.lowercase()) {
            "incoming" -> "incoming:yes"
            "outgoing" -> "incoming:no"
            else -> DocspellQueryNormalizer.buildFieldEqualsQuery("incoming", rawDirection)
        }
    }

    private fun nameQuery(field: String, name: String): String {
        return if (name.contains('*')) {
            "$field:$name"
        } else {
            DocspellQueryNormalizer.buildFieldEqualsQuery(field, name)
        }
    }
}
