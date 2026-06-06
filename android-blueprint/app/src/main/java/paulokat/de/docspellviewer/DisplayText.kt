package paulokat.de.docspellviewer

import androidx.core.text.HtmlCompat
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Bereitet vom Server stammende Texte fuer die Anzeige auf (UTF-8, HTML-Entities, URL-Kodierung).
 */
object DisplayText {
    private val UNICODE_ESCAPE = Regex("""\\u([0-9a-fA-F]{4})""")

    fun normalize(text: String?): String {
        if (text.isNullOrBlank()) {
            return text.orEmpty()
        }
        var result = text.trim()
        if (result.contains('%')) {
            result = decodeUrlEncoding(result)
        }
        if (result.contains('&')) {
            result = decodeHtmlEntities(result)
        }
        if (result.contains("\\u")) {
            result = decodeLiteralUnicodeEscapes(result)
        }
        result = fixUtf8Mojibake(result)
        return result
    }

    private fun decodeUrlEncoding(value: String): String {
        return runCatching {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        }.getOrDefault(value)
    }

    private fun decodeHtmlEntities(value: String): String {
        return HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
    }

    private fun decodeLiteralUnicodeEscapes(value: String): String {
        return UNICODE_ESCAPE.replace(value) { match ->
            match.groupValues[1].toInt(16).toChar().toString()
        }
    }

    /** UTF-8 fälschlich als ISO-8859-1 gelesen (z. B. Ã¤ statt ä). */
    private fun fixUtf8Mojibake(value: String): String {
        if (!value.contains('Ã') && !value.contains('â')) {
            return value
        }
        return runCatching {
            String(value.toByteArray(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)
        }.getOrDefault(value)
    }
}

fun String.displayText(): String = DisplayText.normalize(this)
