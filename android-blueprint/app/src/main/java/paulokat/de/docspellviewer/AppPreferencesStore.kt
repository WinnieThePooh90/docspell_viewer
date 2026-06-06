package paulokat.de.docspellviewer

import android.content.Context

const val DEFAULT_PAGE_SIZE = 50
val PAGE_SIZE_OPTIONS = listOf(10, 20, 50, 100)

data class AppPreferences(
    val colorScheme: AppColorScheme = AppColorScheme.GRAYSCALE,
    val useDarkTheme: Boolean = false,
    val pageSize: Int = DEFAULT_PAGE_SIZE,
    val detailFieldVisibility: DetailFieldVisibility = DetailFieldVisibility(),
    val sidebarFilterVisibility: SidebarFilterVisibility = SidebarFilterVisibility(),
    val startPageId: String = StartPageStorage.SEARCH,
    val appLanguage: AppLanguage = AppLanguage.ENGLISH
)

class AppPreferencesStore(
    private val context: Context,
    private val accountId: String
) {
    // In Application.attachBaseContext applicationContext is not set yet — use base context.
    private val appContext = context.applicationContext ?: context
    private val prefs = appContext.getSharedPreferences(prefsName(accountId), Context.MODE_PRIVATE)

    fun load(): AppPreferences {
        val legacyTheme = prefs.getString(KEY_THEME, null)
        val colorScheme = prefs.getString(KEY_COLOR_SCHEME, null)?.let { stored ->
            AppColorScheme.fromName(stored)
        } ?: AppColorScheme.migrateFromLegacyTheme(legacyTheme)
        val useDarkTheme = prefs.getBoolean(
            KEY_USE_DARK,
            AppColorScheme.legacyUsedDarkTheme(legacyTheme)
        )
        val pageSize = prefs.getInt(KEY_PAGE_SIZE, DEFAULT_PAGE_SIZE)
        val detailFields = prefs.getStringSet(KEY_DETAIL_FIELDS, null)
        val detailFieldOrder = prefs.getString(KEY_DETAIL_FIELDS_ORDER, null)
        val visibility = DetailFieldVisibility.fromStored(detailFieldOrder, detailFields)
        val sidebarFilters = prefs.getStringSet(KEY_SIDEBAR_FILTERS, null)
        val sidebarFilterOrder = prefs.getString(KEY_SIDEBAR_FILTERS_ORDER, null)
        val sidebarVisibility = SidebarFilterVisibility.fromStored(sidebarFilterOrder, sidebarFilters)
        val startPageId = StartPageStorage.normalizeStored(
            prefs.getString(KEY_START_PAGE, null)
        )
        val appLanguage = loadAppLanguage()
        return AppPreferences(
            colorScheme = colorScheme,
            useDarkTheme = useDarkTheme,
            pageSize = normalizePageSize(pageSize),
            detailFieldVisibility = visibility,
            sidebarFilterVisibility = sidebarVisibility,
            startPageId = startPageId,
            appLanguage = appLanguage
        )
    }

    fun save(preferences: AppPreferences) {
        prefs.edit()
            .putString(KEY_COLOR_SCHEME, preferences.colorScheme.name)
            .putBoolean(KEY_USE_DARK, preferences.useDarkTheme)
            .remove(KEY_THEME)
            .putInt(KEY_PAGE_SIZE, normalizePageSize(preferences.pageSize))
            .putStringSet(KEY_DETAIL_FIELDS, preferences.detailFieldVisibility.toEnabledPreferenceKeys())
            .putString(KEY_DETAIL_FIELDS_ORDER, preferences.detailFieldVisibility.toOrderPreferenceValue())
            .putStringSet(KEY_SIDEBAR_FILTERS, preferences.sidebarFilterVisibility.toEnabledPreferenceKeys())
            .putString(KEY_SIDEBAR_FILTERS_ORDER, preferences.sidebarFilterVisibility.toOrderPreferenceValue())
            .putString(KEY_START_PAGE, preferences.startPageId)
            .putString(KEY_APP_LANGUAGE, preferences.appLanguage.tag)
            .apply()
    }

    private fun loadAppLanguage(): AppLanguage {
        val stored = prefs.getString(KEY_APP_LANGUAGE, null)
        if (stored != null) {
            return AppLanguage.fromTag(stored)
        }
        val migrated = GlobalPreferencesStore(appContext).getLanguage()
        prefs.edit().putString(KEY_APP_LANGUAGE, migrated.tag).apply()
        return migrated
    }

    companion object {
        private const val PREFS_PREFIX = "docspell_app_preferences_"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_COLOR_SCHEME = "color_scheme"
        private const val KEY_USE_DARK = "use_dark_theme"
        private const val KEY_PAGE_SIZE = "search_page_size"
        private const val KEY_DETAIL_FIELDS = "detail_fields_visible"
        private const val KEY_DETAIL_FIELDS_ORDER = "detail_fields_order"
        private const val KEY_SIDEBAR_FILTERS = "sidebar_filters_visible"
        private const val KEY_SIDEBAR_FILTERS_ORDER = "sidebar_filters_order"
        private const val KEY_START_PAGE = "overview_start_page"
        private const val KEY_APP_LANGUAGE = "app_language"

        fun prefsName(accountId: String): String {
            val cleaned = accountId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
            return "$PREFS_PREFIX${cleaned.ifBlank { "default" }}"
        }

        fun normalizePageSize(value: Int): Int {
            return if (value in PAGE_SIZE_OPTIONS) value else DEFAULT_PAGE_SIZE
        }
    }
}
