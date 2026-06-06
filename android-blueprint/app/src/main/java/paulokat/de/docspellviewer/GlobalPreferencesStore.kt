package paulokat.de.docspellviewer

import android.content.Context

class GlobalPreferencesStore(context: Context) {
    // In Application.attachBaseContext applicationContext is not set yet — use base context.
    private val prefs = (context.applicationContext ?: context)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLanguage(): AppLanguage {
        return AppLanguage.fromTag(
            prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.tag)
        )
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.tag).apply()
    }

    companion object {
        private const val PREFS_NAME = "docspell_app_global"
        private const val KEY_LANGUAGE = "app_language"
    }
}
