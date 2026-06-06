package paulokat.de.docspellviewer

import androidx.annotation.StringRes

enum class AppLanguage(val tag: String, @StringRes val displayNameRes: Int) {
    GERMAN("de", R.string.language_german),
    ENGLISH("en", R.string.language_english);

    companion object {
        fun fromTag(tag: String?): AppLanguage {
            return entries.firstOrNull { it.tag == tag } ?: ENGLISH
        }
    }
}
