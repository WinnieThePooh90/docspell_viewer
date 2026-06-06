package paulokat.de.docspellviewer

import android.content.Context

class AccountScopedStores(
    private val appContext: Context,
    accountId: String
) {
    val offlineDocuments = OfflineDocumentStore(appContext, accountId)
    val favorites = FavoriteDocumentStore(appContext, accountId)
    val preferences = AppPreferencesStore(appContext, accountId)
    val pickerViewModes = PickerViewModeStore(appContext, accountId)

    companion object {
        fun forAccount(context: Context, accountId: String): AccountScopedStores {
            return AccountScopedStores(context.applicationContext, accountId)
        }
    }
}
