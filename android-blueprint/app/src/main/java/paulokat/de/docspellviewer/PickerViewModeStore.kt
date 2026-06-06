package paulokat.de.docspellviewer

import android.content.Context

class PickerViewModeStore(
    context: Context,
    accountId: String
) {
    private val prefs = context.applicationContext
        .getSharedPreferences(prefsName(accountId), Context.MODE_PRIVATE)

    fun get(pageKey: String): PickerViewMode {
        return PickerViewMode.fromStorage(prefs.getString(storageKey(pageKey), null))
    }

    fun set(pageKey: String, mode: PickerViewMode) {
        prefs.edit()
            .putString(storageKey(pageKey), mode.name)
            .apply()
    }

    companion object {
        private const val PREFS_PREFIX = "docspell_picker_view_"
        private const val KEY_PREFIX = "view_mode_"

        fun prefsName(accountId: String): String {
            val cleaned = accountId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
            return "$PREFS_PREFIX${cleaned.ifBlank { "default" }}"
        }

        private fun storageKey(pageKey: String): String {
            val cleaned = pageKey.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
            return "$KEY_PREFIX${cleaned.ifBlank { "default" }}"
        }
    }
}
