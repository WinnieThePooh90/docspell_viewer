package paulokat.de.docspellviewer

/**
 * Wandelt einfache Suchbegriffe in gueltige Docspell-Query-Syntax um.
 * Siehe https://docspell.org/docs/query/
 */
object DocspellQueryNormalizer {
    /**
     * @return null = keine Filterung (neueste Dokumente), sonst Docspell-Query-String
     */
    fun normalize(userInput: String): String? {
        val trimmed = userInput.trim()
        if (trimmed.isEmpty() || trimmed == "*") {
            return null
        }
        if (looksLikeDocspellQuery(trimmed)) {
            return trimmed
        }
        val escaped = escapeForQuotedValue(trimmed)
        return if (trimmed.any { it.isWhitespace() }) {
            "names:*\"$escaped\"*"
        } else {
            "names:*$escaped*"
        }
    }

    private fun looksLikeDocspellQuery(query: String): Boolean {
        if (query.startsWith('!') || query.startsWith("(&") || query.startsWith("(|")) {
            return true
        }
        return query.contains('=') ||
            query.contains(':') ||
            query.contains('(') ||
            query.contains(')')
    }

    fun buildTagQuery(tagName: String): String =
        fieldEqualsQuery("tag", tagName)

    fun buildCategoryQuery(categoryName: String): String {
        val trimmed = categoryName.trim()
        val needsQuotes = trimmed.any { it.isWhitespace() || it == '"' || it == ',' || it == '(' || it == ')' }
        return if (needsQuotes) {
            "cat:\"${escapeForQuotedValue(trimmed)}\""
        } else {
            "cat:$trimmed"
        }
    }

    fun buildCorrespondentOrgQuery(organizationName: String): String =
        fieldEqualsQuery("corr.org.name", organizationName)

    fun buildCorrespondentPersonQuery(personName: String): String =
        fieldEqualsQuery("corr.pers.name", personName)

    fun buildFieldEqualsQuery(field: String, value: String): String =
        fieldEqualsQuery(field, value)

    /** Docspell-Syntax: [f:feldname=Wert](https://docspell.org/docs/query/#custom-fields) */
    fun buildCustomFieldQuery(fieldName: String, value: String): String {
        val name = fieldName.trim()
        val trimmed = value.trim()
        if (trimmed == "*") {
            return "f:$name:*"
        }
        return fieldEqualsQuery("f:$name", trimmed)
    }

    private fun fieldEqualsQuery(field: String, value: String): String {
        val trimmed = value.trim()
        val needsQuotes = trimmed.any { it.isWhitespace() || it == '"' || it == ',' || it == '(' || it == ')' }
        return if (needsQuotes) {
            "$field=\"${escapeForQuotedValue(trimmed)}\""
        } else {
            "$field=$trimmed"
        }
    }

    private fun escapeForQuotedValue(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }
}
