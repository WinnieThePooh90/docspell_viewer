package paulokat.de.docspellviewer

import android.content.Context
import java.io.File

/**
 * Moves legacy single-user offline/favorites data to the first migrated account namespace.
 */
object AccountDataMigration {
    fun migrateLegacyScopedData(context: Context, accountId: String) {
        migrateLegacyOffline(context, accountId)
        migrateLegacyFavorites(context, accountId)
        migrateLegacyAppPreferences(context, accountId)
        migrateLegacyViewerCache(context, accountId)
    }

    fun deleteAccountData(context: Context, accountId: String) {
        OfflineDocumentStore(context, accountId).deleteAll()
        FavoriteDocumentStore(context, accountId).deleteAll()
        context.applicationContext
            .getSharedPreferences(AppPreferencesStore.prefsName(accountId), Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun migrateLegacyOffline(context: Context, accountId: String) {
        val appContext = context.applicationContext
        val legacyDir = File(appContext.filesDir, LEGACY_OFFLINE_DIR)
        if (!legacyDir.exists()) {
            return
        }
        val targetRoot = File(appContext.filesDir, "$OFFLINE_ROOT/$accountId")
        targetRoot.mkdirs()
        legacyDir.listFiles()?.forEach { file ->
            val target = File(targetRoot, file.name)
            if (!file.renameTo(target)) {
                file.copyRecursively(target, overwrite = true)
                file.deleteRecursively()
            }
        }
        legacyDir.deleteRecursively()
        val legacyPrefs = appContext.getSharedPreferences(LEGACY_OFFLINE_PREFS, Context.MODE_PRIVATE)
        if (legacyPrefs.all.isNotEmpty()) {
            val targetPrefs = appContext.getSharedPreferences(
                OfflineDocumentStore.prefsName(accountId),
                Context.MODE_PRIVATE
            )
            val editor = targetPrefs.edit()
            legacyPrefs.all.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                }
            }
            editor.apply()
            legacyPrefs.edit().clear().apply()
        }
    }

    private fun migrateLegacyFavorites(context: Context, accountId: String) {
        val appContext = context.applicationContext
        val legacyPrefs = appContext.getSharedPreferences(LEGACY_FAVORITES_PREFS, Context.MODE_PRIVATE)
        val json = legacyPrefs.getString(LEGACY_FAVORITES_KEY, null) ?: return
        val targetPrefs = appContext.getSharedPreferences(
            FavoriteDocumentStore.prefsName(accountId),
            Context.MODE_PRIVATE
        )
        targetPrefs.edit().putString(LEGACY_FAVORITES_KEY, json).apply()
        legacyPrefs.edit().clear().apply()
    }

    private fun migrateLegacyAppPreferences(context: Context, accountId: String) {
        val appContext = context.applicationContext
        val legacyPrefs = appContext.getSharedPreferences(LEGACY_APP_PREFS, Context.MODE_PRIVATE)
        if (legacyPrefs.all.isEmpty()) {
            return
        }
        val targetPrefs = appContext.getSharedPreferences(
            AppPreferencesStore.prefsName(accountId),
            Context.MODE_PRIVATE
        )
        val editor = targetPrefs.edit()
        legacyPrefs.all.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Set<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    editor.putStringSet(key, value as Set<String>)
                }
            }
        }
        editor.apply()
        legacyPrefs.edit().clear().apply()
    }

    private const val LEGACY_OFFLINE_DIR = "docspell_offline"
    private const val OFFLINE_ROOT = "docspell_offline"
    private const val LEGACY_OFFLINE_PREFS = "docspell_offline_index"
    private const val LEGACY_FAVORITES_PREFS = "docspell_favorites"
    private const val LEGACY_FAVORITES_KEY = "favorite_documents"
    private const val LEGACY_APP_PREFS = "docspell_app_preferences"

    private fun migrateLegacyViewerCache(context: Context, accountId: String) {
        val cacheRoot = java.io.File(context.cacheDir, "docspell_viewer")
        if (!cacheRoot.isDirectory) {
            return
        }
        val legacyFiles = cacheRoot.listFiles()?.filter { it.isFile && it.length() > 0L }.orEmpty()
        if (legacyFiles.isEmpty()) {
            return
        }
        val cleaned = accountId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "default" }
        val targetDir = java.io.File(cacheRoot, cleaned)
        targetDir.mkdirs()
        legacyFiles.forEach { file ->
            val target = java.io.File(targetDir, file.name)
            if (!file.renameTo(target)) {
                file.copyTo(target, overwrite = true)
                file.delete()
            }
        }
    }
}
