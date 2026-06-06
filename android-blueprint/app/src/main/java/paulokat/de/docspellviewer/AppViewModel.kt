package paulokat.de.docspellviewer

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

data class DocumentRow(
    val id: String,
    val name: String,
    val correspondent: String = "—",
    val corrOrgName: String? = null,
    val corrPersonName: String? = null,
    val previewUrl: String = "",
    val attachmentId: String? = null,
    val viewUrl: String? = null,
    val downloadUrl: String? = null,
    val downloadFileName: String = "dokument.pdf",
    val isOfflineAvailable: Boolean = false,
    val isFavorite: Boolean = false,
    val attachmentCount: Int = 0
)

data class OfflineListUiState(
    val documents: List<DocumentRow> = emptyList()
)

data class FavoritesListUiState(
    val documents: List<DocumentRow> = emptyList()
)

data class HomeUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val documents: List<DocumentRow> = emptyList(),
    val hasMoreResults: Boolean = false,
    val totalResultCount: Int? = null,
    val error: String? = null,
    @StringRes val statusResId: Int? = null,
    val statusQuery: String? = null,
    val needsSettings: Boolean = false,
    val activeAccountLabel: String? = null,
    val sidebarFilterVisibility: SidebarFilterVisibility = SidebarFilterVisibility(),
    val overviewShowSearchTable: Boolean = false,
    val navigateToOverviewNonce: Int = 0
) {
    fun hitsSummary(context: Context): String? {
        val total = totalResultCount ?: return null
        return context.getString(R.string.home_hits_summary, documents.size, total)
    }
}

data class PdfViewerUiState(
    val isLoading: Boolean = false,
    val loadingDocumentId: String? = null,
    val transferProgressPercent: Int? = null,
    val title: String = "",
    val pdfPath: String? = null,
    val error: String? = null
) {
    fun isOpeningDocument(documentId: String): Boolean {
        return loadingDocumentId == documentId &&
            (isLoading || (transferProgressPercent ?: 0) in 1..99)
    }

    fun openingProgressFor(documentId: String): Int {
        if (loadingDocumentId != documentId) {
            return 0
        }
        if (!isLoading && pdfPath != null) {
            return 100
        }
        return transferProgressPercent ?: 0
    }
}

data class AudioPlaybackUiState(
    val attachmentId: String? = null,
    val attachmentName: String? = null,
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val isPlayerOpen: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L
)

data class DocumentDetailUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val previewUrl: String = "",
    val content: DocumentDetailContent = DocumentDetailContent(),
    val detailFieldVisibility: DetailFieldVisibility = DetailFieldVisibility(),
    val document: DocumentRow? = null,
    val viewerDocument: DocumentRow? = null,
    val isOfflineAvailable: Boolean = false,
    val isOfflineWorking: Boolean = false,
    val transferProgressPercent: Int? = null,
    val audioPlayback: AudioPlaybackUiState = AudioPlaybackUiState(),
    val isFavorite: Boolean = false,
    @StringRes val feedbackMessageRes: Int? = null,
    val feedbackMessageIsError: Boolean = false,
    val error: String? = null
)

data class DownloadSaveRequest(
    val downloadUrl: String,
    val suggestedFileName: String,
    val mimeType: String
)

data class TagRow(
    val id: String,
    val name: String,
    val category: String?
)

data class TagsUiState(
    val isLoading: Boolean = false,
    val tags: List<TagRow> = emptyList(),
    val error: String? = null
)

data class PickerListItem(
    val id: String,
    val title: String,
    val subtitle: String? = null
) {
    companion object {
        const val ANY_VALUE_ID = "*"
    }
}

enum class CustomFieldPickerStep {
    NOT_APPLICABLE,
    CHOOSE_FIELD,
    CHOOSE_VALUE
}

data class PickerUiState(
    val isLoading: Boolean = false,
    val items: List<PickerListItem> = emptyList(),
    val error: String? = null,
    val customFieldStep: CustomFieldPickerStep = CustomFieldPickerStep.NOT_APPLICABLE,
    @StringRes val customFieldScreenTitleRes: Int? = null,
    val customFieldFieldLabel: String? = null
)

enum class CorrespondentType {
    ORGANIZATION,
    PERSON
}

data class CorrespondentRow(
    val id: String,
    val name: String,
    val type: CorrespondentType
)

data class SettingsUiState(
    val accounts: List<AccountListItem> = emptyList(),
    val editingAccountId: String? = null,
    val isNewAccount: Boolean = false,
    val displayName: String = "",
    val baseUrl: String = AccountStore.defaults.baseUrl,
    val account: String = AccountStore.defaults.account,
    val password: String = "",
    val showPassword: Boolean = false,
    val colorScheme: AppColorScheme = AppColorScheme.GRAYSCALE,
    val useDarkTheme: Boolean = false,
    val pageSize: Int = DEFAULT_PAGE_SIZE,
    val detailFieldVisibility: DetailFieldVisibility = DetailFieldVisibility(),
    val sidebarFilterVisibility: SidebarFilterVisibility = SidebarFilterVisibility(),
    val startPageId: String = StartPageStorage.SEARCH,
    val offlineDocumentCount: Int = 0,
    val offlineStorageBytes: Long = 0L,
    val viewerCacheBytes: Long = 0L,
    val totalOfflineDocumentCount: Int = 0,
    val totalOfflineStorageBytes: Long = 0L,
    val totalViewerCacheBytes: Long = 0L,
    val saveMessage: String? = null,
    val saveMessageIsError: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.ENGLISH
)

class AppViewModel(
    private val appContext: Context,
    private val accountStore: AccountStore,
    private val tokenStore: TokenStore,
    private val sessionManager: DocspellSessionManager
) : ViewModel() {

    private var scopedStores: AccountScopedStores? = null
    private var activeAccount: DocspellAccount? = null
    private val offlineDocumentStore: OfflineDocumentStore
        get() = requireScopedStores().offlineDocuments
    private val favoriteDocumentStore: FavoriteDocumentStore
        get() = requireScopedStores().favorites
    private val appPreferencesStore: AppPreferencesStore
        get() = requireScopedStores().preferences

    private fun requireScopedStores(): AccountScopedStores {
        return scopedStores ?: error("No active account")
    }

    private val documentActions = DocumentActionHelper(appContext, tokenStore, sessionManager)
    private val audioPlaybackHelper = AudioPlaybackHelper()
    private var sessionRefreshJob: Job? = null
    private val _localeChangeEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val localeChangeEvent: SharedFlow<Unit> = _localeChangeEvent.asSharedFlow()
    private var localeAppliedForLanguage: AppLanguage? = null

    private fun currentAppLanguage(): AppLanguage = _settingsState.value.appLanguage

    private fun localizedContext(): Context =
        AppLocale.wrap(appContext, currentAppLanguage())

    private fun str(@StringRes resId: Int): String = localizedContext().getString(resId)

    private fun str(@StringRes resId: Int, vararg formatArgs: Any): String =
        localizedContext().getString(resId, *formatArgs)

    private var audioProgressJob: Job? = null
    private var settingsFeedbackJob: Job? = null
    private var detailFeedbackJob: Job? = null
    private var pdfLoadPulseJob: Job? = null

    private companion object {
        const val AUDIO_SKIP_MS = 10_000
        const val SETTINGS_FEEDBACK_DISMISS_MS = 5_000L
        const val DETAIL_FEEDBACK_DISMISS_MS = 5_000L
        const val PDF_LOAD_PULSE_MS = 100L
    }

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState.asStateFlow()

    private val _tagsState = MutableStateFlow(TagsUiState())
    val tagsState: StateFlow<TagsUiState> = _tagsState.asStateFlow()

    private val _correspondentsState = MutableStateFlow(PickerUiState())
    val correspondentsState: StateFlow<PickerUiState> = _correspondentsState.asStateFlow()

    private val _categoriesState = MutableStateFlow(PickerUiState())
    val categoriesState: StateFlow<PickerUiState> = _categoriesState.asStateFlow()

    private val _pdfViewerState = MutableStateFlow(PdfViewerUiState())
    val pdfViewerState: StateFlow<PdfViewerUiState> = _pdfViewerState.asStateFlow()

    private val _documentDetailState = MutableStateFlow(DocumentDetailUiState())
    val documentDetailState: StateFlow<DocumentDetailUiState> = _documentDetailState.asStateFlow()

    private val _offlineListState = MutableStateFlow(OfflineListUiState())
    val offlineListState: StateFlow<OfflineListUiState> = _offlineListState.asStateFlow()

    private val _favoritesListState = MutableStateFlow(FavoritesListUiState())
    val favoritesListState: StateFlow<FavoritesListUiState> = _favoritesListState.asStateFlow()

    private val _thumbnailReloadGeneration = MutableStateFlow(0)
    val thumbnailReloadGeneration: StateFlow<Int> = _thumbnailReloadGeneration.asStateFlow()

    private var repository: DocspellRepository? = null
    private var lastDocspellQuery: String? = null
    private var searchPageSize: Int = DEFAULT_PAGE_SIZE
    private var detailFieldVisibility: DetailFieldVisibility = DetailFieldVisibility()
    private var sidebarFilterVisibility: SidebarFilterVisibility = SidebarFilterVisibility()

    private val _fieldFilterPickerState = MutableStateFlow(PickerUiState())
    val fieldFilterPickerState: StateFlow<PickerUiState> = _fieldFilterPickerState.asStateFlow()
    private var activeFieldFilterId: SidebarFilterId? = null
    private var activeCustomFieldName: String? = null

    init {
        sessionManager.bindCredentials { activeServerSettings() }
        sessionManager.onSessionUpdated = { onAuthTokenUpdated() }
        viewModelScope.launch {
            val active = accountStore.getActive()
            if (active != null) {
                bindActiveAccount(active, reloadPreferences = true)
                refreshSettingsForm(active)
                runCatching {
                    autoLoginAndSearch("")
                }.onFailure { err ->
                    _homeState.value = _homeState.value.copy(
                        statusResId = R.string.status_login_failed,
                        statusQuery = null,
                        isLoading = false,
                        error = explainError(R.string.error_step_login, err)
                    )
                }
            } else {
                _homeState.value = _homeState.value.copy(
                    statusResId = R.string.status_create_account,
                    statusQuery = null,
                    error = null,
                    needsSettings = true,
                    activeAccountLabel = null
                )
                refreshSettingsAccountsList()
            }
        }
    }

    fun onQueryChange(value: String) {
        _homeState.value = _homeState.value.copy(query = value)
    }

    fun onSettingsDisplayNameChange(value: String) {
        clearSettingsFeedback { copy(displayName = value) }
    }

    fun onSettingsBaseUrlChange(value: String) {
        clearSettingsFeedback { copy(baseUrl = value) }
    }

    fun onSettingsAccountChange(value: String) {
        clearSettingsFeedback { copy(account = value) }
    }

    fun onSettingsPasswordChange(value: String) {
        clearSettingsFeedback { copy(password = value) }
    }

    fun onSettingsShowPasswordChange(value: Boolean) {
        _settingsState.value = _settingsState.value.copy(showPassword = value)
    }

    fun onSettingsColorSchemeChange(scheme: AppColorScheme) {
        clearSettingsFeedback { copy(colorScheme = scheme) }
        persistActiveAccountAppearance()
    }

    fun onSettingsDarkThemeChange(enabled: Boolean) {
        clearSettingsFeedback { copy(useDarkTheme = enabled) }
        persistActiveAccountAppearance()
    }

    fun onSettingsPageSizeChange(size: Int) {
        val normalized = AppPreferencesStore.normalizePageSize(size)
        searchPageSize = normalized
        clearSettingsFeedback { copy(pageSize = normalized) }
        persistActiveAccountAppPreferences { it.copy(pageSize = normalized) }
    }

    fun onSettingsStartPageChange(pageKey: String) {
        val normalized = StartPageStorage.normalizeStored(pageKey)
        clearSettingsFeedback { copy(startPageId = normalized) }
        _homeState.value = _homeState.value.copy(overviewShowSearchTable = false)
        persistActiveAccountAppPreferences { it.copy(startPageId = normalized) }
    }

    fun onOverviewTabSelected() {
        _homeState.value = _homeState.value.copy(overviewShowSearchTable = false)
    }

    fun onStartPageFilterApplied() {
        _homeState.value = _homeState.value.copy(overviewShowSearchTable = true)
    }

    fun onDetailFieldVisibilityChange(field: DetailFieldId, enabled: Boolean) {
        val next = detailFieldVisibility.withToggled(field, enabled)
        applyDetailFieldVisibility(next, persist = true)
    }

    fun onDetailFieldMoveUp(field: DetailFieldId) {
        applyDetailFieldVisibility(detailFieldVisibility.moveUp(field), persist = true)
    }

    fun onDetailFieldMoveDown(field: DetailFieldId) {
        applyDetailFieldVisibility(detailFieldVisibility.moveDown(field), persist = true)
    }

    fun onSidebarFilterVisibilityChange(filter: SidebarFilterId, enabled: Boolean) {
        val next = sidebarFilterVisibility.withToggled(filter, enabled)
        applySidebarFilterVisibility(next, persist = true)
    }

    fun onSidebarFilterMoveUp(filter: SidebarFilterId) {
        applySidebarFilterVisibility(sidebarFilterVisibility.moveUp(filter), persist = true)
    }

    fun onSidebarFilterMoveDown(filter: SidebarFilterId) {
        applySidebarFilterVisibility(sidebarFilterVisibility.moveDown(filter), persist = true)
    }

    private fun applyAppPreferences(preferences: AppPreferences) {
        searchPageSize = preferences.pageSize
        applyDetailFieldVisibility(preferences.detailFieldVisibility, persist = false)
        applySidebarFilterVisibility(preferences.sidebarFilterVisibility, persist = false)
        syncAppLanguage(preferences.appLanguage, recreateIfChanged = localeAppliedForLanguage != null)
        _settingsState.value = _settingsState.value.copy(
            colorScheme = preferences.colorScheme,
            useDarkTheme = preferences.useDarkTheme,
            pageSize = preferences.pageSize,
            detailFieldVisibility = preferences.detailFieldVisibility,
            sidebarFilterVisibility = preferences.sidebarFilterVisibility,
            startPageId = preferences.startPageId
        )
        _homeState.value = _homeState.value.copy(overviewShowSearchTable = false)
    }

    private fun applyDetailFieldVisibility(visibility: DetailFieldVisibility, persist: Boolean) {
        detailFieldVisibility = visibility
        _settingsState.value = _settingsState.value.copy(detailFieldVisibility = visibility)
        _documentDetailState.value = _documentDetailState.value.copy(
            detailFieldVisibility = visibility
        )
        if (persist) {
            val accountId = _settingsState.value.editingAccountId ?: activeAccount?.id ?: return
            val stores = AccountScopedStores.forAccount(appContext, accountId)
            val prefs = stores.preferences.load()
            stores.preferences.save(
                prefs.copy(detailFieldVisibility = visibility)
            )
        }
    }

    private fun applySidebarFilterVisibility(visibility: SidebarFilterVisibility, persist: Boolean) {
        sidebarFilterVisibility = visibility
        _settingsState.value = _settingsState.value.copy(sidebarFilterVisibility = visibility)
        _homeState.value = _homeState.value.copy(sidebarFilterVisibility = visibility)
        if (persist) {
            val accountId = _settingsState.value.editingAccountId ?: activeAccount?.id ?: return
            val stores = AccountScopedStores.forAccount(appContext, accountId)
            val prefs = stores.preferences.load()
            stores.preferences.save(
                prefs.copy(sidebarFilterVisibility = visibility)
            )
        }
    }

    fun refreshStorageStats() {
        val userStats = computeUserStorageStats()
        val totalStats = computeTotalStorageStats()
        _settingsState.value = _settingsState.value.copy(
            offlineDocumentCount = userStats.documentCount,
            offlineStorageBytes = userStats.offlineBytes,
            viewerCacheBytes = userStats.viewerCacheBytes,
            totalOfflineDocumentCount = totalStats.documentCount,
            totalOfflineStorageBytes = totalStats.offlineBytes,
            totalViewerCacheBytes = totalStats.viewerCacheBytes
        )
    }

    private fun computeUserStorageStats(): StorageStatsSnapshot {
        val accountId = activeAccount?.id ?: return StorageStatsSnapshot()
        val stores = scopedStores ?: AccountScopedStores.forAccount(appContext, accountId)
        return StorageStatsSnapshot(
            documentCount = stores.offlineDocuments.getDocumentCount(),
            offlineBytes = stores.offlineDocuments.getTotalStorageBytes(),
            viewerCacheBytes = DocumentViewerCache.getTotalSizeBytes(appContext, accountId)
        )
    }

    private fun computeTotalStorageStats(): StorageStatsSnapshot {
        var documentCount = 0
        var offlineBytes = 0L
        var viewerCacheBytes = 0L
        accountStore.list().forEach { account ->
            val stores = AccountScopedStores.forAccount(appContext, account.id)
            documentCount += stores.offlineDocuments.getDocumentCount()
            offlineBytes += stores.offlineDocuments.getTotalStorageBytes()
            viewerCacheBytes += DocumentViewerCache.getTotalSizeBytes(appContext, account.id)
        }
        viewerCacheBytes += DocumentViewerCache.getLegacyRootSizeBytes(appContext)
        val accountIds = accountStore.list().map { it.id }
        offlineBytes += OfflineDocumentStore.getLegacyRootSizeBytes(appContext, accountIds)
        return StorageStatsSnapshot(
            documentCount = documentCount,
            offlineBytes = offlineBytes,
            viewerCacheBytes = viewerCacheBytes
        )
    }

    fun clearUserOfflineDocuments() {
        viewModelScope.launch {
            val stores = scopedStores ?: return@launch
            stores.offlineDocuments.deleteAll()
            val detailDoc = _documentDetailState.value.document
            if (detailDoc != null) {
                applyOfflineRemoved(detailDoc.id)
            } else {
                refreshDocumentFlagsInHome()
            }
            refreshStorageStats()
            showSettingsFeedback(str(R.string.feedback_offline_deleted), isError = false)
        }
    }

    fun clearAllOfflineDocuments() = clearUserOfflineDocuments()

    fun clearAllOfflineDocumentsGlobal() {
        viewModelScope.launch {
            accountStore.list().forEach { account ->
                AccountScopedStores.forAccount(appContext, account.id).offlineDocuments.deleteAll()
            }
            OfflineDocumentStore.clearLegacyRoot(appContext)
            val detailDoc = _documentDetailState.value.document
            if (detailDoc != null) {
                applyOfflineRemoved(detailDoc.id)
            } else {
                refreshDocumentFlagsInHome()
            }
            loadOfflineDocuments()
            refreshStorageStats()
            showSettingsFeedback(str(R.string.feedback_all_offline_deleted), isError = false)
        }
    }

    fun clearUserViewerCache() {
        viewModelScope.launch {
            val accountId = activeAccount?.id ?: return@launch
            DocumentViewerCache.clear(appContext, accountId)
            refreshStorageStats()
            showSettingsFeedback(str(R.string.feedback_cache_cleared), isError = false)
        }
    }

    fun clearViewerCache() = clearUserViewerCache()

    fun clearViewerCacheGlobal() {
        viewModelScope.launch {
            DocumentViewerCache.clearAll(appContext)
            refreshStorageStats()
            showSettingsFeedback(str(R.string.feedback_all_cache_cleared), isError = false)
        }
    }

    private data class StorageStatsSnapshot(
        val documentCount: Int = 0,
        val offlineBytes: Long = 0L,
        val viewerCacheBytes: Long = 0L
    )

    fun refreshSettingsState() {
        loadAppearanceFromActiveAccount()
        refreshStorageStats()
    }

    fun refreshAccountsState() {
        refreshSettingsAccountsList()
        if (accountStore.list().isEmpty()) {
            addAccount()
            refreshStorageStats()
            return
        }
        val editingId = _settingsState.value.editingAccountId
        val account = editingId?.let { accountStore.getById(it) } ?: accountStore.getActive()
        if (account != null) {
            refreshAccountCredentialsForm(account)
        }
        refreshStorageStats()
    }

    fun addAccount() {
        val draft = accountStore.createNewDraft()
        _settingsState.value = _settingsState.value.copy(
            accounts = accountStore.toListItems(),
            editingAccountId = draft.id,
            isNewAccount = true,
            displayName = "",
            baseUrl = draft.baseUrl,
            account = "",
            password = "",
            saveMessage = null,
            saveMessageIsError = false
        )
    }

    fun selectAccountForEdit(id: String) {
        val account = accountStore.getById(id) ?: return
        refreshAccountCredentialsForm(account)
        _settingsState.value = _settingsState.value.copy(
            editingAccountId = id,
            isNewAccount = false
        )
        refreshStorageStats()
    }

    fun activateAccount(id: String) {
        viewModelScope.launch {
            switchAccount(id, fromSettings = true)
        }
    }

    fun deleteEditingAccount() {
        val id = _settingsState.value.editingAccountId ?: return
        viewModelScope.launch {
            AccountDataMigration.deleteAccountData(appContext, id)
            accountStore.delete(id)
            refreshSettingsAccountsList()
            val next = accountStore.getActive()
            if (next != null) {
                bindActiveAccount(next, reloadPreferences = true)
                refreshSettingsForm(next)
                refreshStorageStats()
                clearSession()
                repository = null
                showSettingsFeedback(str(R.string.feedback_account_deleted), isError = false)
                autoLoginAndSearch(_homeState.value.query)
            } else {
                scopedStores = null
                activeAccount = null
                clearSession()
                repository = null
                closeDocumentDetail()
                closePdfViewer()
                _homeState.value = _homeState.value.copy(
                    documents = emptyList(),
                    needsSettings = true,
                    activeAccountLabel = null,
                    statusResId = R.string.status_create_account,
                    statusQuery = null,
                    error = null,
                    isLoading = false
                )
                _settingsState.value = _settingsState.value.copy(
                    editingAccountId = null,
                    isNewAccount = false,
                    displayName = "",
                    baseUrl = AccountStore.defaults.baseUrl,
                    account = "",
                    password = ""
                )
                refreshStorageStats()
                showSettingsFeedback(str(R.string.feedback_account_deleted), isError = false)
            }
        }
    }

    fun saveActiveAccount() {
        val s = _settingsState.value
        val editingId = s.editingAccountId
        if (editingId == null) {
            showSettingsFeedback(str(R.string.feedback_no_account), isError = true)
            return
        }
        if (s.baseUrl.isBlank() || s.account.isBlank() || s.password.isBlank()) {
            showSettingsFeedback(str(R.string.feedback_fill_all_fields), isError = true)
            return
        }
        val normalizedBaseUrl = normalizeBaseUrl(s.baseUrl)
        if (!isValidApiBaseUrl(normalizedBaseUrl)) {
            showSettingsFeedback(str(R.string.feedback_invalid_base_url), isError = true)
            return
        }
        val existing = accountStore.getById(editingId)
        val account = DocspellAccount(
            id = editingId,
            displayName = s.displayName.ifBlank {
                AccountStore.defaultDisplayName(s.account, normalizedBaseUrl)
            },
            baseUrl = normalizedBaseUrl,
            account = s.account,
            password = s.password
        )
        val saved = accountStore.upsert(account)
        val wasNewAccount = s.isNewAccount || existing == null
        if (wasNewAccount) {
            accountStore.setActive(saved.id)
        }
        val preferences = AppPreferences(
            colorScheme = s.colorScheme,
            useDarkTheme = s.useDarkTheme,
            pageSize = s.pageSize,
            detailFieldVisibility = s.detailFieldVisibility,
            sidebarFilterVisibility = s.sidebarFilterVisibility,
            startPageId = s.startPageId,
            appLanguage = s.appLanguage
        )
        val isNowActive = accountStore.getActive()?.id == saved.id
        if (isNowActive) {
            bindActiveAccount(saved, reloadPreferences = false)
            appPreferencesStore.save(preferences)
            applyAppPreferences(preferences)
            clearSession()
            repository = null
            _homeState.value = _homeState.value.copy(
                needsSettings = false,
                navigateToOverviewNonce = if (wasNewAccount) {
                    _homeState.value.navigateToOverviewNonce + 1
                } else {
                    _homeState.value.navigateToOverviewNonce
                }
            )
            viewModelScope.launch {
                autoLoginAndSearch(_homeState.value.query)
            }
        } else {
            AccountScopedStores.forAccount(appContext, saved.id).preferences.save(preferences)
            refreshSettingsForm(saved)
        }
        refreshSettingsAccountsList()
        _settingsState.value = _settingsState.value.copy(isNewAccount = false)
        showSettingsFeedback(str(R.string.feedback_saved), isError = false)
    }

    /** @deprecated Use [saveActiveAccount]. */
    fun saveSettings() = saveActiveAccount()

    fun switchAccount(id: String) {
        viewModelScope.launch {
            switchAccount(id, fromSettings = false)
        }
    }

    private suspend fun switchAccount(id: String, fromSettings: Boolean) {
        val account = accountStore.getById(id) ?: return
        if (activeAccount?.id == id) {
            if (fromSettings) {
                refreshSettingsForm(account)
                refreshStorageStats()
            }
            return
        }
        accountStore.setActive(id)
        clearSession()
        repository = null
        closeDocumentDetail()
        closePdfViewer()
        clearThumbnailCaches()
        bindActiveAccount(account, reloadPreferences = true)
        if (fromSettings) {
            refreshSettingsForm(account)
        }
        refreshStorageStats()
        _homeState.value = _homeState.value.copy(
            documents = emptyList(),
            hasMoreResults = false,
            totalResultCount = null,
            error = null,
            needsSettings = false,
            overviewShowSearchTable = false
        )
        _offlineListState.value = OfflineListUiState()
        _favoritesListState.value = FavoritesListUiState()
        autoLoginAndSearch(_homeState.value.query)
    }

    private fun bindActiveAccount(account: DocspellAccount, reloadPreferences: Boolean) {
        activeAccount = account
        scopedStores = AccountScopedStores.forAccount(appContext, account.id)
        if (reloadPreferences) {
            applyAppPreferences(appPreferencesStore.load())
        }
        updateHomeActiveLabel()
        refreshSettingsAccountsList()
    }

    private fun refreshAccountCredentialsForm(account: DocspellAccount) {
        _settingsState.value = _settingsState.value.copy(
            editingAccountId = account.id,
            isNewAccount = false,
            displayName = account.displayName,
            baseUrl = account.baseUrl,
            account = account.account,
            password = account.password
        )
    }

    private fun loadAppearanceFromActiveAccount() {
        val account = accountStore.getActive() ?: return
        val prefs = AccountScopedStores.forAccount(appContext, account.id).preferences.load()
        searchPageSize = prefs.pageSize
        applyDetailFieldVisibility(prefs.detailFieldVisibility, persist = false)
        applySidebarFilterVisibility(prefs.sidebarFilterVisibility, persist = false)
        _settingsState.value = _settingsState.value.copy(
            colorScheme = prefs.colorScheme,
            useDarkTheme = prefs.useDarkTheme,
            pageSize = prefs.pageSize,
            detailFieldVisibility = prefs.detailFieldVisibility,
            sidebarFilterVisibility = prefs.sidebarFilterVisibility,
            startPageId = prefs.startPageId,
            appLanguage = prefs.appLanguage
        )
    }

    private fun persistActiveAccountAppearance() {
        val s = _settingsState.value
        persistActiveAccountAppPreferences {
            it.copy(
                colorScheme = s.colorScheme,
                useDarkTheme = s.useDarkTheme
            )
        }
    }

    private fun persistActiveAccountAppPreferences(
        update: (AppPreferences) -> AppPreferences
    ) {
        val accountId = activeAccount?.id ?: return
        val stores = AccountScopedStores.forAccount(appContext, accountId)
        stores.preferences.save(update(stores.preferences.load()))
    }

    /** Lädt Konto-Zugangsdaten und Erscheinungsbild des Kontos (z. B. nach Speichern oder Kontowechsel). */
    private fun refreshSettingsForm(account: DocspellAccount) {
        val prefs = AccountScopedStores.forAccount(appContext, account.id).preferences.load()
        refreshAccountCredentialsForm(account)
        _settingsState.value = _settingsState.value.copy(
            colorScheme = prefs.colorScheme,
            useDarkTheme = prefs.useDarkTheme,
            pageSize = prefs.pageSize,
            detailFieldVisibility = prefs.detailFieldVisibility,
            sidebarFilterVisibility = prefs.sidebarFilterVisibility,
            startPageId = prefs.startPageId,
            appLanguage = prefs.appLanguage
        )
        if (account.id == activeAccount?.id) {
            searchPageSize = prefs.pageSize
            applyDetailFieldVisibility(prefs.detailFieldVisibility, persist = false)
            applySidebarFilterVisibility(prefs.sidebarFilterVisibility, persist = false)
            syncAppLanguage(prefs.appLanguage, recreateIfChanged = localeAppliedForLanguage != null)
        }
    }

    private fun refreshSettingsAccountsList() {
        _settingsState.value = _settingsState.value.copy(
            accounts = accountStore.toListItems()
        )
    }

    private fun syncAppLanguage(language: AppLanguage, recreateIfChanged: Boolean) {
        _settingsState.value = _settingsState.value.copy(appLanguage = language)
        if (!recreateIfChanged) {
            localeAppliedForLanguage = language
            return
        }
        if (localeAppliedForLanguage != language) {
            localeAppliedForLanguage = language
            _localeChangeEvent.tryEmit(Unit)
        }
    }

    private fun updateHomeActiveLabel() {
        val account = activeAccount
        _homeState.value = _homeState.value.copy(
            activeAccountLabel = account?.displayName
        )
    }

    private fun clearThumbnailCaches() {
        runCatching {
            DocspellImageLoader.invalidateCaches(coil.Coil.imageLoader(appContext))
        }
    }

    private fun onAuthTokenUpdated() {
        clearThumbnailCaches()
        _thumbnailReloadGeneration.value++
        scheduleSessionRefresh()
    }

    private fun scheduleSessionRefresh() {
        sessionRefreshJob?.cancel()
        val delayMs = sessionManager.millisUntilProactiveRefresh() ?: return
        sessionRefreshJob = viewModelScope.launch {
            delay(delayMs)
            runCatching { sessionManager.refreshProactively() }
            scheduleSessionRefresh()
        }
    }

    private fun cancelSessionRefresh() {
        sessionRefreshJob?.cancel()
        sessionRefreshJob = null
    }

    private fun clearSession() {
        cancelSessionRefresh()
        sessionManager.clearSession()
    }

    private fun activeServerSettings(): ServerSettings? {
        return activeAccount?.toServerSettings()
    }

    private fun clearSettingsFeedback(update: SettingsUiState.() -> SettingsUiState) {
        settingsFeedbackJob?.cancel()
        settingsFeedbackJob = null
        _settingsState.value = _settingsState.value.update().copy(
            saveMessage = null,
            saveMessageIsError = false
        )
    }

    private fun showSettingsFeedback(message: String, isError: Boolean) {
        settingsFeedbackJob?.cancel()
        _settingsState.value = _settingsState.value.copy(
            saveMessage = message,
            saveMessageIsError = isError
        )
        settingsFeedbackJob = viewModelScope.launch {
            delay(SETTINGS_FEEDBACK_DISMISS_MS)
            _settingsState.value = _settingsState.value.copy(
                saveMessage = null,
                saveMessageIsError = false
            )
        }
    }

    fun loadTags() {
        viewModelScope.launch {
            _tagsState.value = TagsUiState(isLoading = true)
            if (!ensureRepository()) {
                _tagsState.value = TagsUiState(
                    isLoading = false,
                    error = str(R.string.error_login_check_settings)
                )
                return@launch
            }
            val repo = repository ?: return@launch
            runCatching { repo.loadTags() }
                .onSuccess { tags ->
                    _tagsState.value = TagsUiState(isLoading = false, tags = tags)
                }
                .onFailure { err ->
                    _tagsState.value = TagsUiState(
                        isLoading = false,
                        error = explainError(R.string.error_step_tags, err)
                    )
                }
        }
    }

    fun searchByTag(tagName: String) {
        applyFilterSearch(DocspellQueryNormalizer.buildTagQuery(tagName))
    }

    fun loadCorrespondents() {
        loadPickerList(
            stateFlow = _correspondentsState,
            errorLabel = str(R.string.error_step_organizations)
        ) { repository?.loadCorrespondents().orEmpty().map { c ->
            PickerListItem(
                id = "${c.type.name}:${c.id}",
                title = c.name,
                subtitle = when (c.type) {
                    CorrespondentType.ORGANIZATION -> str(R.string.correspondent_type_organization)
                    CorrespondentType.PERSON -> str(R.string.correspondent_type_person)
                }
            )
        } }
    }

    fun loadCategories() {
        loadPickerList(
            stateFlow = _categoriesState,
            errorLabel = str(R.string.error_step_categories)
        ) { repository?.loadCategories().orEmpty().map { cat ->
            PickerListItem(id = cat, title = cat.displayText())
        } }
    }

    fun searchByCorrespondent(item: PickerListItem) {
        val query = when {
            item.id.startsWith("${CorrespondentType.ORGANIZATION.name}:") ->
                DocspellQueryNormalizer.buildCorrespondentOrgQuery(item.title)
            item.id.startsWith("${CorrespondentType.PERSON.name}:") ->
                DocspellQueryNormalizer.buildCorrespondentPersonQuery(item.title)
            else -> DocspellQueryNormalizer.buildCorrespondentOrgQuery(item.title)
        }
        applyFilterSearch(query)
    }

    fun searchByCorrespondentFromDocument(doc: DocumentRow) {
        val org = doc.corrOrgName?.trim().orEmpty()
        val person = doc.corrPersonName?.trim().orEmpty()
        val query = when {
            org.isNotEmpty() -> DocspellQueryNormalizer.buildCorrespondentOrgQuery(org)
            person.isNotEmpty() -> DocspellQueryNormalizer.buildCorrespondentPersonQuery(person)
            else -> return
        }
        applyFilterSearch(query)
    }

    fun searchByCategory(categoryName: String) {
        applyFilterSearch(DocspellQueryNormalizer.buildCategoryQuery(categoryName))
    }

    fun getPickerViewMode(pageKey: String): PickerViewMode {
        return scopedStores?.pickerViewModes?.get(pageKey) ?: PickerViewMode.LIST
    }

    fun setPickerViewMode(pageKey: String, mode: PickerViewMode) {
        scopedStores?.pickerViewModes?.set(pageKey, mode)
    }

    fun loadFieldFilterPicker(filterId: SidebarFilterId) {
        activeFieldFilterId = filterId
        activeCustomFieldName = null
        if (!filterId.supportsValuePicker) {
            _fieldFilterPickerState.value = PickerUiState(
                isLoading = false,
                error = str(R.string.error_no_picker)
            )
            return
        }
        if (filterId == SidebarFilterId.CUSTOM_FIELDS) {
            _fieldFilterPickerState.value = PickerUiState(
                isLoading = true,
                customFieldStep = CustomFieldPickerStep.CHOOSE_FIELD,
                customFieldScreenTitleRes = filterId.labelRes,
                customFieldFieldLabel = null
            )
            loadPickerList(
                stateFlow = _fieldFilterPickerState,
                errorLabel = str(R.string.error_step_custom_fields)
            ) {
                val items = repository?.loadCustomFieldDefinitions(appContext).orEmpty()
                if (items.isEmpty()) {
                    throw IllegalStateException(str(R.string.error_no_custom_fields))
                }
                items
            }
            return
        }
        _fieldFilterPickerState.value = PickerUiState(
            isLoading = true,
            customFieldStep = CustomFieldPickerStep.NOT_APPLICABLE
        )
        loadPickerList(
            stateFlow = _fieldFilterPickerState,
            errorLabel = str(R.string.error_step_filter, str(filterId.labelRes))
        ) {
            repository?.loadSidebarFilterValues(appContext, filterId).orEmpty().map { option ->
                PickerListItem(id = option.queryValue, title = option.display)
            }
        }
    }

    fun loadCustomFieldValuePicker(fieldName: String, fieldLabel: String) {
        activeCustomFieldName = fieldName
        _fieldFilterPickerState.value = PickerUiState(
            isLoading = true,
            customFieldStep = CustomFieldPickerStep.CHOOSE_VALUE,
            customFieldScreenTitleRes = null,
            customFieldFieldLabel = fieldLabel
        )
        loadPickerList(
            stateFlow = _fieldFilterPickerState,
            errorLabel = str(R.string.error_step_filter, fieldLabel)
        ) {
            val values = repository?.loadCustomFieldValues(fieldName).orEmpty().map { value ->
                PickerListItem(id = value, title = value)
            }
            listOf(
                PickerListItem(
                    id = PickerListItem.ANY_VALUE_ID,
                    title = "",
                    subtitle = null
                )
            ) + values
        }
    }

    fun backCustomFieldFilterPicker() {
        loadFieldFilterPicker(SidebarFilterId.CUSTOM_FIELDS)
    }

    fun searchByFieldFilter(queryValue: String) {
        val filter = activeFieldFilterId ?: return
        val query = when (filter) {
            SidebarFilterId.CUSTOM_FIELDS -> {
                val fieldName = activeCustomFieldName ?: return
                DocspellQueryNormalizer.buildCustomFieldQuery(fieldName, queryValue)
            }
            else -> FilterQueryBuilder.build(filter, queryValue)
        }
        if (query == null) {
            _homeState.value = _homeState.value.copy(
                error = str(R.string.error_filter_invalid),
                statusResId = null,
                statusQuery = null
            )
            return
        }
        activeCustomFieldName = null
        applyFilterSearch(query)
    }

    private fun applyFilterSearch(docspellQuery: String) {
        _homeState.value = _homeState.value.copy(query = docspellQuery)
        viewModelScope.launch {
            if (!ensureRepository()) return@launch
            searchWithCurrentSession(docspellQuery)
        }
    }

    private fun loadPickerList(
        stateFlow: MutableStateFlow<PickerUiState>,
        errorLabel: String,
        loader: suspend () -> List<PickerListItem>
    ) {
        viewModelScope.launch {
            stateFlow.value = stateFlow.value.copy(isLoading = true, error = null)
            if (!ensureRepository()) {
                stateFlow.value = stateFlow.value.copy(
                    isLoading = false,
                    error = str(R.string.error_login_check_settings)
                )
                return@launch
            }
            runCatching { loader() }
                .onSuccess { items ->
                    stateFlow.value = stateFlow.value.copy(
                        isLoading = false,
                        items = items,
                        error = null
                    )
                }
                .onFailure { err ->
                    stateFlow.value = stateFlow.value.copy(
                        isLoading = false,
                        error = explainError(errorLabel, err)
                    )
                }
        }
    }

    private suspend fun ensureRepository(): Boolean {
        val settings = activeServerSettings()
        if (settings == null) {
            _homeState.value = _homeState.value.copy(error = str(R.string.error_no_server_data))
            return false
        }
        if (repository != null && !tokenStore.getToken().isNullOrBlank()) {
            runCatching { sessionManager.refreshProactively() }
            return true
        }
        return loginOnly(settings)
    }

    private suspend fun loginOnly(settings: ServerSettings): Boolean {
        val api = DocspellApiFactory.create(settings.baseUrl, tokenStore, sessionManager)
        val repo = DocspellRepository(api)
        val login = runCatching {
            repo.login(settings.account, settings.password)
        }.getOrElse { return false }

        if (!login.success || login.token.isNullOrBlank()) {
            return false
        }
        sessionManager.applySession(login)
        repository = repo
        return true
    }

    fun openDocumentDetail(doc: DocumentRow) {
        val offline = offlineDocumentStore.isAvailable(doc.id)
        val apiBaseUrl = activeAccount?.baseUrl.orEmpty()

        if (offline) {
            showOfflineDocumentDetail(doc, apiBaseUrl)
            tryRefreshDocumentDetailFromNetwork(doc, apiBaseUrl)
            return
        }

        loadDocumentDetailFromNetwork(doc, offline = false, apiBaseUrl = apiBaseUrl)
    }

    private fun showOfflineDocumentDetail(doc: DocumentRow, apiBaseUrl: String) {
        val meta = offlineDocumentStore.listDocuments().find { it.itemId == doc.id }
        val content = offlineDocumentStore.loadDetailContent(doc.id, apiBaseUrl)
            ?: meta?.toOfflineDetailContent(apiBaseUrl)
            ?: DocumentDetailContent()
        val viewerDoc = meta?.toDocumentRow(apiBaseUrl) ?: doc.copy(isOfflineAvailable = true)
        val title = meta?.name?.ifBlank { null } ?: doc.name
        _documentDetailState.value = DocumentDetailUiState(
            isLoading = false,
            title = title,
            previewUrl = doc.previewUrl,
            content = content,
            detailFieldVisibility = detailFieldVisibility,
            document = doc.copy(isOfflineAvailable = true),
            viewerDocument = viewerDoc,
            isOfflineAvailable = true
        ).withFavoriteFlag()
    }

    private fun loadDocumentDetailFromNetwork(
        doc: DocumentRow,
        offline: Boolean,
        apiBaseUrl: String
    ) {
        _documentDetailState.value = DocumentDetailUiState(
            isLoading = true,
            title = doc.name,
            previewUrl = doc.previewUrl,
            detailFieldVisibility = detailFieldVisibility,
            document = doc.copy(isOfflineAvailable = offline),
            isOfflineAvailable = offline
        ).withFavoriteFlag()
        viewModelScope.launch {
            if (!ensureRepository()) {
                if (offline) {
                    showOfflineDocumentDetail(doc, apiBaseUrl)
                } else {
                    _documentDetailState.value = _documentDetailState.value.copy(
                        isLoading = false,
                        error = str(R.string.error_not_logged_in)
                    )
                }
                return@launch
            }
            val repo = repository ?: return@launch
            runCatching { repo.loadItemDetail(doc.id) }
                .onSuccess { detail ->
                    val content = detail.toDetailContent(apiBaseUrl)
                    val viewerDoc = detail.toViewerDocument(apiBaseUrl, doc.previewUrl)
                    _documentDetailState.value = DocumentDetailUiState(
                        isLoading = false,
                        title = detail.name.ifBlank { doc.name },
                        previewUrl = doc.previewUrl,
                        content = content,
                        detailFieldVisibility = detailFieldVisibility,
                        document = doc.copy(isOfflineAvailable = offline),
                        viewerDocument = viewerDoc,
                        isOfflineAvailable = offline
                    ).withFavoriteFlag()
                }
                .onFailure { err ->
                    if (offline) {
                        showOfflineDocumentDetail(doc, apiBaseUrl)
                    } else {
                        _documentDetailState.value = _documentDetailState.value.copy(
                            isLoading = false,
                            error = explainError(R.string.error_step_details, err)
                        )
                    }
                }
        }
    }

    private fun tryRefreshDocumentDetailFromNetwork(doc: DocumentRow, apiBaseUrl: String) {
        viewModelScope.launch {
            if (!ensureRepository()) {
                return@launch
            }
            val repo = repository ?: return@launch
            runCatching { repo.loadItemDetail(doc.id) }
                .onSuccess { detail ->
                    val offline = offlineDocumentStore.isAvailable(doc.id)
                    val content = detail.toDetailContent(apiBaseUrl)
                    val viewerDoc = detail.toViewerDocument(apiBaseUrl, doc.previewUrl)
                    _documentDetailState.value = _documentDetailState.value.copy(
                        title = detail.name.ifBlank { doc.name },
                        content = content,
                        viewerDocument = viewerDoc,
                        document = doc.copy(isOfflineAvailable = offline),
                        isOfflineAvailable = offline
                    ).withFavoriteFlag()
                    if (offline) {
                        val primaryId = viewerDoc?.attachmentId
                            ?: offlineDocumentStore.getAttachmentId(doc.id)
                            ?: doc.attachmentId
                        if (!primaryId.isNullOrBlank()) {
                            saveAttachmentsOffline(doc.id, content.attachments, apiBaseUrl, primaryId)
                            offlineDocumentStore.updateDetailContent(doc.id, content)
                        }
                    }
                }
        }
    }

    fun makeDocumentOfflineAvailable() {
        val doc = _documentDetailState.value.viewerDocument
            ?: _documentDetailState.value.document
        val attachmentId = doc?.attachmentId
        val apiBase = activeAccount?.baseUrl
        if (doc == null || attachmentId.isNullOrBlank() || apiBase.isNullOrBlank()) {
            documentActions.showError(str(R.string.error_no_pdf_offline))
            return
        }
        if (offlineDocumentStore.isAvailable(doc.id)) {
            return
        }

        viewModelScope.launch {
            _documentDetailState.value = _documentDetailState.value.copy(
                isOfflineWorking = true,
                transferProgressPercent = 0
            )
            if (!ensureRepository()) {
                _documentDetailState.value = _documentDetailState.value.copy(
                    isOfflineWorking = false,
                    transferProgressPercent = null
                )
                documentActions.showError(str(R.string.error_not_logged_in))
                return@launch
            }
            val attachments = resolveAttachmentsForOffline(doc.id, apiBase)
            val pendingAttachments = offlineAttachmentsToDownload(attachments, attachmentId)
            val downloadFileCount = 1 + pendingAttachments.size
            var fileIndex = 0

            fun reportOfflineProgress(bytesRead: Long, contentLength: Long) {
                _documentDetailState.value = _documentDetailState.value.copy(
                    transferProgressPercent = DownloadProgress.combinedPercent(
                        fileIndex = fileIndex,
                        fileCount = downloadFileCount,
                        bytesRead = bytesRead,
                        contentLength = contentLength
                    ).coerceAtLeast(1)
                )
            }

            val url = DocspellUrls.attachmentPdf(apiBase, attachmentId)
            documentActions.downloadPdfBytes(url, onProgress = ::reportOfflineProgress)
                .onSuccess { bytes ->
                    fileIndex++
                    val detailContent = buildOfflineDetailContent(doc.id, attachments)
                    val meta = OfflineDocumentMeta(
                        itemId = doc.id,
                        attachmentId = attachmentId,
                        name = doc.name,
                        correspondent = doc.correspondent,
                        corrOrgName = doc.corrOrgName,
                        corrPersonName = doc.corrPersonName,
                        downloadFileName = doc.downloadFileName
                    )
                    offlineDocumentStore.save(
                        doc.id,
                        attachmentId,
                        bytes,
                        meta,
                        detailContent = detailContent
                    )
                    activeAccount?.id?.let { accountId ->
                        DocumentViewerCache.store(appContext, accountId, attachmentId, bytes)
                    }
                    for (attachment in pendingAttachments) {
                        val downloadUrl = attachment.downloadUrl.ifBlank {
                            DocspellUrls.attachmentDownload(apiBase, attachment.id)
                        }
                        documentActions.downloadAttachmentBytes(
                            downloadUrl,
                            onProgress = ::reportOfflineProgress
                        ).onSuccess { attachmentBytes ->
                            offlineDocumentStore.saveAttachment(
                                itemId = doc.id,
                                attachmentId = attachment.id,
                                bytes = attachmentBytes,
                                fileName = attachment.downloadFileName
                            )
                        }
                        fileIndex++
                    }
                    applyOfflineAvailable(doc.id)
                    showDetailFeedback(R.string.error_offline_available, isError = false)
                }
                .onFailure { err ->
                    _documentDetailState.value = _documentDetailState.value.copy(
                        isOfflineWorking = false,
                        transferProgressPercent = null
                    )
                    documentActions.showError(
                        err.message ?: str(R.string.error_offline_save_failed)
                    )
                }
        }
    }

    fun deleteOfflineDocument() {
        val doc = _documentDetailState.value.document ?: return
        viewModelScope.launch {
            _documentDetailState.value = _documentDetailState.value.copy(isOfflineWorking = true)
            offlineDocumentStore.delete(doc.id)
            applyOfflineRemoved(doc.id)
            showDetailFeedback(R.string.error_local_file_deleted, isError = false)
        }
    }

    private fun applyOfflineAvailable(itemId: String) {
        _documentDetailState.value = _documentDetailState.value.copy(
            isOfflineAvailable = true,
            isOfflineWorking = false,
            transferProgressPercent = null,
            document = _documentDetailState.value.document?.copy(isOfflineAvailable = true)
        )
        refreshDocumentFlagsInHome()
    }

    private fun applyOfflineRemoved(itemId: String) {
        _documentDetailState.value = _documentDetailState.value.copy(
            isOfflineAvailable = false,
            isOfflineWorking = false,
            transferProgressPercent = null,
            document = _documentDetailState.value.document?.copy(isOfflineAvailable = false)
        )
        refreshDocumentFlagsInHome()
    }

    fun loadOfflineDocuments() {
        val stores = scopedStores ?: run {
            _offlineListState.value = OfflineListUiState()
            return
        }
        val apiBase = activeAccount?.baseUrl.orEmpty()
        val docs = stores.offlineDocuments.listDocuments()
            .map { it.toDocumentRow(apiBase) }
            .let { applyListFlags(it) }
        _offlineListState.value = OfflineListUiState(documents = docs)
    }

    fun loadFavoriteDocuments() {
        val stores = scopedStores ?: run {
            _favoritesListState.value = FavoritesListUiState()
            return
        }
        val apiBase = activeAccount?.baseUrl.orEmpty()
        val offlineIds = stores.offlineDocuments.getAvailableItemIds()
        val docs = stores.favorites.listAll()
            .map { it.toDocumentRow(apiBase, it.itemId in offlineIds) }
            .let { applyListFlags(it) }
        _favoritesListState.value = FavoritesListUiState(documents = docs)
    }

    fun toggleFavorite() {
        val doc = _documentDetailState.value.document ?: return
        val nowFavorite = favoriteDocumentStore.toggle(doc.toFavoriteSnapshot())
        val messageRes = if (nowFavorite) {
            R.string.detail_favorite_added
        } else {
            R.string.detail_favorite_removed
        }
        showDetailFeedback(messageRes, isError = false)
        _documentDetailState.value = _documentDetailState.value.copy(isFavorite = nowFavorite)
        loadFavoriteDocuments()
        refreshDocumentFlagsInHome()
    }

    private fun DocumentDetailUiState.withFavoriteFlag(): DocumentDetailUiState {
        val itemId = document?.id ?: return this
        return copy(isFavorite = favoriteDocumentStore.isFavorite(itemId))
    }

    private fun showDetailFeedback(@StringRes messageRes: Int, isError: Boolean) {
        detailFeedbackJob?.cancel()
        _documentDetailState.value = _documentDetailState.value.copy(
            feedbackMessageRes = messageRes,
            feedbackMessageIsError = isError
        )
        detailFeedbackJob = viewModelScope.launch {
            delay(DETAIL_FEEDBACK_DISMISS_MS)
            _documentDetailState.value = _documentDetailState.value.copy(
                feedbackMessageRes = null,
                feedbackMessageIsError = false
            )
        }
    }

    private fun refreshDocumentFlagsInHome() {
        if (scopedStores == null) {
            return
        }
        val home = _homeState.value
        _homeState.value = home.copy(
            documents = applyListFlags(home.documents)
        )
        loadOfflineDocuments()
    }

    private fun applyListFlags(documents: List<DocumentRow>): List<DocumentRow> {
        val stores = scopedStores ?: return documents
        val offlineIds = stores.offlineDocuments.getAvailableItemIds()
        return documents.map { doc ->
            doc.copy(
                isOfflineAvailable = doc.id in offlineIds,
                isFavorite = stores.favorites.isFavorite(doc.id)
            )
        }
    }

    fun closeDocumentDetail() {
        detailFeedbackJob?.cancel()
        stopAudioProgressLoop()
        audioPlaybackHelper.stop()
        _documentDetailState.value = DocumentDetailUiState()
    }

    fun openAudioPlayer(attachment: DetailAttachmentRow) {
        if (!attachment.isAudio) {
            return
        }
        val audio = _documentDetailState.value.audioPlayback
        if (audio.attachmentId == attachment.id && audio.isPlayerOpen) {
            if (audio.isPlaying) {
                pauseAudioPlayback()
            } else if (audioPlaybackHelper.hasPreparedPlayer(attachment.id)) {
                resumeAudioPlayback()
            }
            return
        }

        stopAudioProgressLoop()
        audioPlaybackHelper.stop()
        viewModelScope.launch {
            val itemId = _documentDetailState.value.document?.id
            if (itemId != null && offlineDocumentStore.isAvailable(itemId)) {
                val offlineFile = offlineDocumentStore.getOfflineAttachmentFile(
                    itemId = itemId,
                    attachmentId = attachment.id,
                    fileName = attachment.downloadFileName
                )
                if (offlineFile != null) {
                    startAudioPlaybackFromFile(offlineFile, attachment)
                    return@launch
                }
                documentActions.showError(str(R.string.error_audio_not_offline))
                return@launch
            }
            if (!ensureRepository()) {
                documentActions.showError(str(R.string.error_not_logged_in))
                return@launch
            }
            updateAudioPlayback(
                AudioPlaybackUiState(
                    attachmentId = attachment.id,
                    attachmentName = attachment.name,
                    isLoading = true,
                    isPlayerOpen = true
                )
            )
            documentActions.fetchAttachmentToCache(
                downloadUrl = attachment.downloadUrl,
                cacheKey = attachment.id,
                fileName = attachment.downloadFileName
            ).onSuccess { file ->
                startAudioPlaybackFromFile(file, attachment)
            }.onFailure { err ->
                stopAudioProgressLoop()
                updateAudioPlayback(AudioPlaybackUiState())
                documentActions.showError(
                    err.message ?: str(R.string.error_audio_load_failed)
                )
            }
        }
    }

    private suspend fun resolveAttachmentsForOffline(
        itemId: String,
        apiBase: String
    ): List<DetailAttachmentRow> {
        val state = _documentDetailState.value
        if (state.document?.id == itemId && state.content.attachments.isNotEmpty()) {
            return state.content.attachments
        }
        if (!ensureRepository()) {
            return state.content.attachments
        }
        val repo = repository ?: return state.content.attachments
        return runCatching { repo.loadItemDetail(itemId) }
            .getOrNull()
            ?.toDetailContent(apiBase)
            ?.attachments
            .orEmpty()
            .ifEmpty { state.content.attachments }
    }

    private fun buildOfflineDetailContent(
        itemId: String,
        attachments: List<DetailAttachmentRow>
    ): DocumentDetailContent? {
        val state = _documentDetailState.value
        if (state.document?.id != itemId) {
            return null
        }
        val detail = state.content
        val hasDetail = detail.customFields.isNotEmpty() ||
            detail.documentDate != null ||
            detail.correspondent != null ||
            detail.tags != null
        if (!hasDetail && attachments.isEmpty()) {
            return null
        }
        return if (attachments.isNotEmpty()) {
            detail.copy(attachments = attachments)
        } else {
            detail
        }
    }

    private fun offlineAttachmentsToDownload(
        attachments: List<DetailAttachmentRow>,
        primaryAttachmentId: String
    ): List<DetailAttachmentRow> {
        return attachments.filter { att ->
            !(att.id == primaryAttachmentId &&
                !att.isAudio &&
                DownloadFileNames.extension(att.downloadFileName) == "pdf")
        }
    }

    private suspend fun saveAttachmentsOffline(
        itemId: String,
        attachments: List<DetailAttachmentRow>,
        apiBase: String,
        primaryAttachmentId: String
    ) {
        for (att in offlineAttachmentsToDownload(attachments, primaryAttachmentId)) {
            val downloadUrl = att.downloadUrl.ifBlank {
                DocspellUrls.attachmentDownload(apiBase, att.id)
            }
            documentActions.downloadAttachmentBytes(downloadUrl)
                .onSuccess { bytes ->
                    offlineDocumentStore.saveAttachment(
                        itemId = itemId,
                        attachmentId = att.id,
                        bytes = bytes,
                        fileName = att.downloadFileName
                    )
                }
        }
    }

    private suspend fun startAudioPlaybackFromFile(
        file: File,
        attachment: DetailAttachmentRow
    ) {
        updateAudioPlayback(
            AudioPlaybackUiState(
                attachmentId = attachment.id,
                attachmentName = attachment.name,
                isLoading = true,
                isPlayerOpen = true
            )
        )
        audioPlaybackHelper.play(file, attachment.id) {
            viewModelScope.launch {
                stopAudioProgressLoop()
                val audio = _documentDetailState.value.audioPlayback
                val duration = audio.durationMs.coerceAtLeast(
                    audioPlaybackHelper.getDurationMs().toLong()
                )
                updateAudioPlayback(
                    audio.copy(
                        isPlaying = false,
                        isLoading = false,
                        isPlayerOpen = true,
                        durationMs = duration,
                        positionMs = duration
                    )
                )
            }
        }.onSuccess {
            val duration = audioPlaybackHelper.getDurationMs().toLong()
            updateAudioPlayback(
                AudioPlaybackUiState(
                    attachmentId = attachment.id,
                    attachmentName = attachment.name,
                    isPlaying = true,
                    isPlayerOpen = true,
                    durationMs = duration,
                    positionMs = 0L
                )
            )
            startAudioProgressLoop()
        }.onFailure { err ->
            stopAudioProgressLoop()
            audioPlaybackHelper.stop()
            updateAudioPlayback(AudioPlaybackUiState())
            documentActions.showError(err.message ?: str(R.string.error_playback_failed))
        }
    }

    fun toggleAudioPlayPause() {
        val audio = _documentDetailState.value.audioPlayback
        if (!audio.isPlayerOpen || audio.attachmentId == null) {
            return
        }
        if (audio.isPlaying) {
            pauseAudioPlayback()
        } else if (audioPlaybackHelper.hasPreparedPlayer(audio.attachmentId)) {
            resumeAudioPlayback()
        }
    }

    fun seekAudio(fraction: Float) {
        val audio = _documentDetailState.value.audioPlayback
        val duration = audio.durationMs
        if (duration <= 0L || audio.attachmentId == null) {
            return
        }
        val targetMs = (duration * fraction.coerceIn(0f, 1f)).toLong()
        audioPlaybackHelper.seekTo(targetMs.toInt())
        updateAudioPlayback(audio.copy(positionMs = targetMs))
    }

    fun skipAudioBackward() {
        audioPlaybackHelper.skipBy(-AUDIO_SKIP_MS)
        syncAudioPositionFromPlayer()
    }

    fun skipAudioForward() {
        audioPlaybackHelper.skipBy(AUDIO_SKIP_MS)
        syncAudioPositionFromPlayer()
    }

    private fun pauseAudioPlayback() {
        audioPlaybackHelper.pause()
        updateAudioPlayback(_documentDetailState.value.audioPlayback.copy(isPlaying = false))
    }

    private fun resumeAudioPlayback() {
        audioPlaybackHelper.resume()
        val audio = _documentDetailState.value.audioPlayback
        updateAudioPlayback(audio.copy(isPlaying = true, isLoading = false))
        startAudioProgressLoop()
    }

    private fun syncAudioPositionFromPlayer() {
        val audio = _documentDetailState.value.audioPlayback
        val duration = audioPlaybackHelper.getDurationMs().toLong().coerceAtLeast(audio.durationMs)
        val position = audioPlaybackHelper.getCurrentPositionMs().toLong()
            .coerceIn(0, duration.coerceAtLeast(1))
        updateAudioPlayback(
            audio.copy(
                durationMs = duration,
                positionMs = position
            )
        )
    }

    private fun startAudioProgressLoop() {
        stopAudioProgressLoop()
        audioProgressJob = viewModelScope.launch {
            while (isActive) {
                val audio = _documentDetailState.value.audioPlayback
                val attachmentId = audio.attachmentId
                if (!audio.isPlayerOpen || attachmentId == null ||
                    !audioPlaybackHelper.hasPreparedPlayer(attachmentId)
                ) {
                    break
                }
                val duration = audioPlaybackHelper.getDurationMs().toLong().coerceAtLeast(0L)
                val position = if (duration > 0L) {
                    audioPlaybackHelper.getCurrentPositionMs().toLong().coerceIn(0, duration)
                } else {
                    0L
                }
                val playing = audioPlaybackHelper.isPlaying(attachmentId)
                updateAudioPlayback(
                    audio.copy(
                        durationMs = duration,
                        positionMs = position,
                        isPlaying = playing,
                        isLoading = false
                    )
                )
                delay(250)
            }
        }
    }

    private fun stopAudioProgressLoop() {
        audioProgressJob?.cancel()
        audioProgressJob = null
    }

    private fun updateAudioPlayback(state: AudioPlaybackUiState) {
        _documentDetailState.value = _documentDetailState.value.copy(audioPlayback = state)
    }

    override fun onCleared() {
        stopAudioProgressLoop()
        settingsFeedbackJob?.cancel()
        audioPlaybackHelper.release()
        super.onCleared()
    }

    fun startPdfViewer(doc: DocumentRow) {
        val attachmentId = doc.attachmentId
        val apiBase = activeAccount?.baseUrl
        val accountId = activeAccount?.id
        if (attachmentId.isNullOrBlank()) {
            documentActions.showError(str(R.string.error_no_attachment_open))
            return
        }
        if (accountId.isNullOrBlank()) {
            documentActions.showError(str(R.string.error_no_active_account))
            return
        }
        if (apiBase.isNullOrBlank() && offlineDocumentStore.getOfflineFile(doc.id) == null) {
            documentActions.showError(str(R.string.error_no_server))
            return
        }
        beginPdfLoad(doc.id, doc.name)
        viewModelScope.launch {
            val url = if (!apiBase.isNullOrBlank() && !attachmentId.isNullOrBlank()) {
                DocspellUrls.attachmentPdf(apiBase, attachmentId)
            } else {
                ""
            }
            documentActions.fetchPdfForViewer(
                pdfUrl = url,
                accountId = accountId,
                attachmentId = attachmentId,
                offlineItemId = doc.id,
                offlineStore = offlineDocumentStore,
                onProgress = { bytesRead, contentLength ->
                    applyPdfLoadProgress(
                        doc.id,
                        DocumentLoadProgress.displayPercent(bytesRead, contentLength)
                    )
                }
            )
                .onSuccess { file ->
                    finishPdfDownload(doc.id, doc.name, file.absolutePath)
                }
                .onFailure { error ->
                    pdfLoadPulseJob?.cancel()
                    _pdfViewerState.value = PdfViewerUiState(
                        title = doc.name,
                        error = error.message ?: str(R.string.error_load_failed)
                    )
                    documentActions.showError(error.message ?: str(R.string.error_load_failed))
                }
        }
    }

    private fun beginPdfLoad(documentId: String, title: String) {
        pdfLoadPulseJob?.cancel()
        _pdfViewerState.value = PdfViewerUiState(
            isLoading = true,
            loadingDocumentId = documentId,
            title = title,
            transferProgressPercent = 1
        )
        pdfLoadPulseJob = viewModelScope.launch {
            var pulse = 1
            while (isActive) {
                delay(PDF_LOAD_PULSE_MS)
                val state = _pdfViewerState.value
                if (!state.isLoading || state.loadingDocumentId != documentId) {
                    break
                }
                val current = state.transferProgressPercent ?: 0
                if (current >= 75) {
                    break
                }
                if (pulse < 70 && pulse <= current) {
                    pulse = (current + 2).coerceAtMost(70)
                } else if (pulse < 70) {
                    pulse += 1
                }
                if (pulse > current) {
                    applyPdfLoadProgress(documentId, pulse)
                }
            }
        }
    }

    private fun applyPdfLoadProgress(documentId: String, percent: Int) {
        val state = _pdfViewerState.value
        if (state.loadingDocumentId != documentId) {
            return
        }
        val next = DocumentLoadProgress.monotonic(state.transferProgressPercent, percent)
        if (next != state.transferProgressPercent) {
            _pdfViewerState.value = state.copy(transferProgressPercent = next)
        }
    }

    private fun finishPdfDownload(documentId: String, title: String, pdfPath: String) {
        pdfLoadPulseJob?.cancel()
        _pdfViewerState.value = PdfViewerUiState(
            isLoading = true,
            loadingDocumentId = documentId,
            title = title,
            pdfPath = pdfPath,
            transferProgressPercent = 85
        )
    }

    fun updatePdfRenderProgress(documentId: String, percent: Int) {
        val state = _pdfViewerState.value
        if (state.loadingDocumentId != documentId || state.pdfPath == null) {
            return
        }
        val next = DocumentLoadProgress.monotonic(state.transferProgressPercent, percent)
        if (next == state.transferProgressPercent && state.isLoading == (next < 100)) {
            return
        }
        _pdfViewerState.value = state.copy(
            transferProgressPercent = next,
            isLoading = next < 100
        )
    }

    fun completePdfRender(documentId: String) {
        val state = _pdfViewerState.value
        if (state.loadingDocumentId != documentId) {
            return
        }
        _pdfViewerState.value = state.copy(
            transferProgressPercent = 100,
            isLoading = false
        )
    }

    fun closePdfViewer() {
        pdfLoadPulseJob?.cancel()
        _pdfViewerState.value = PdfViewerUiState()
    }

    fun createDownloadRequest(downloadUrl: String, fileName: String): DownloadSaveRequest? {
        if (downloadUrl.isBlank()) {
            documentActions.showError(str(R.string.error_no_attachment_download))
            return null
        }
        val safeName = DownloadFileNames.sanitize(fileName)
        return DownloadSaveRequest(
            downloadUrl = downloadUrl,
            suggestedFileName = safeName,
            mimeType = DownloadFileNames.guessMimeType(safeName)
        )
    }

    fun createDownloadRequest(attachment: DetailAttachmentRow): DownloadSaveRequest? {
        return createDownloadRequest(attachment.downloadUrl, attachment.downloadFileName)
    }

    fun completeDownload(request: DownloadSaveRequest, destinationUri: Uri) {
        viewModelScope.launch {
            documentActions.downloadToUri(request.downloadUrl, destinationUri)
                .onSuccess { name -> documentActions.showDownloadSuccess(name) }
                .onFailure {
                    documentActions.showError(
                        it.message ?: str(R.string.error_download_failed)
                    )
                }
        }
    }

    fun runSearch() {
        viewModelScope.launch {
            if (activeServerSettings() == null) {
                _homeState.value = _homeState.value.copy(
                    error = str(R.string.error_no_server_data_settings)
                )
                return@launch
            }
            if (repository == null || tokenStore.getToken().isNullOrBlank()) {
                autoLoginAndSearch(_homeState.value.query)
            } else {
                searchWithCurrentSession(_homeState.value.query)
            }
        }
    }

    fun syncWithServer() {
        viewModelScope.launch {
            if (activeServerSettings() == null) {
                _homeState.value = _homeState.value.copy(
                    error = str(R.string.error_no_server_data_settings)
                )
                return@launch
            }
            clearSession()
            repository = null
            autoLoginAndSearch(_homeState.value.query)
        }
    }

    private suspend fun autoLoginAndSearch(query: String) {
        val settings = activeServerSettings()
        if (settings == null) {
            _homeState.value = _homeState.value.copy(
                statusResId = R.string.status_not_configured,
                statusQuery = null,
                error = str(R.string.error_open_settings),
                isLoading = false,
                needsSettings = true
            )
            return
        }

        _homeState.value = _homeState.value.copy(
            statusResId = R.string.status_logging_in,
            statusQuery = null,
            isLoading = true,
            error = null,
            documents = emptyList()
        )

        val repo = createRepository(settings.baseUrl)
        if (repo == null) {
            return
        }

        val login = runCatching {
            repo.login(settings.account, settings.password)
        }.getOrElse { err ->
            _homeState.value = _homeState.value.copy(
                statusResId = R.string.status_login_failed,
                statusQuery = null,
                isLoading = false,
                error = explainError(R.string.error_step_login, err)
            )
            return
        }

        if (!login.success || login.token.isNullOrBlank()) {
            _homeState.value = _homeState.value.copy(
                statusResId = R.string.status_login_failed,
                statusQuery = null,
                isLoading = false,
                error = str(R.string.error_invalid_credentials)
            )
            return
        }

        sessionManager.applySession(login)
        repository = repo
        searchWithCurrentSession(query)
    }

    fun loadMore() {
        val state = _homeState.value
        if (!state.hasMoreResults || state.isLoadingMore || state.isLoading) {
            return
        }
        val repo = repository ?: return
        val apiBaseUrl = activeAccount?.baseUrl ?: return

        viewModelScope.launch {
            _homeState.value = state.copy(isLoadingMore = true, error = null)
            val offset = state.documents.size
            runCatching {
                repo.loadDocuments(
                    docspellQuery = lastDocspellQuery,
                    apiBaseUrl = apiBaseUrl,
                    offset = offset,
                    limit = searchPageSize
                )
            }.onSuccess { newDocs ->
                val existingIds = state.documents.map { it.id }.toSet()
                val appended = newDocs.filter { it.id !in existingIds }
                val merged = state.documents + appended
                _homeState.value = _homeState.value.copy(
                    documents = applyListFlags(merged),
                    hasMoreResults = newDocs.size == searchPageSize,
                    isLoadingMore = false,
                    error = null
                )
            }.onFailure { err ->
                _homeState.value = _homeState.value.copy(
                    isLoadingMore = false,
                    error = explainError(R.string.error_step_load_more, err)
                )
            }
        }
    }

    private suspend fun searchWithCurrentSession(query: String) {
        val repo = repository ?: return
        val docspellQuery = resolveDocspellQuery(query)
        lastDocspellQuery = docspellQuery

        _homeState.value = _homeState.value.copy(
            statusResId = R.string.status_searching,
            statusQuery = null,
            isLoading = true,
            isLoadingMore = false,
            error = null,
            documents = emptyList(),
            hasMoreResults = false,
            totalResultCount = null
        )

        val apiBaseUrl = activeAccount?.baseUrl ?: return
        runCatching {
            val docs = repo.loadDocuments(
                docspellQuery = docspellQuery,
                apiBaseUrl = apiBaseUrl,
                offset = 0,
                limit = searchPageSize
            )
            val total = runCatching { repo.loadSearchResultCount(docspellQuery) }.getOrNull()
            docs to total
        }.onSuccess { (docs, total) ->
            _homeState.value = _homeState.value.copy(
                statusResId = if (docspellQuery == null) R.string.status_latest_documents else null,
                statusQuery = docspellQuery,
                documents = applyListFlags(docs),
                hasMoreResults = docs.size == searchPageSize,
                totalResultCount = total,
                isLoading = false,
                error = null
            )
        }.onFailure { err ->
            _homeState.value = _homeState.value.copy(
                statusResId = R.string.status_search_error,
                statusQuery = null,
                isLoading = false,
                documents = emptyList(),
                hasMoreResults = false,
                totalResultCount = null,
                error = explainError(R.string.error_step_search, err)
            )
        }
    }

    fun onAppLanguageChange(language: AppLanguage) {
        val accountId = _settingsState.value.editingAccountId
            ?: activeAccount?.id
            ?: return
        val stores = AccountScopedStores.forAccount(appContext, accountId)
        val prefs = stores.preferences.load()
        if (prefs.appLanguage == language) {
            _settingsState.value = _settingsState.value.copy(appLanguage = language)
            return
        }
        stores.preferences.save(prefs.copy(appLanguage = language))
        if (accountId == activeAccount?.id) {
            syncAppLanguage(language, recreateIfChanged = true)
        } else {
            _settingsState.value = _settingsState.value.copy(appLanguage = language)
        }
    }

    private fun explainError(step: String, err: Throwable): String {
        return HttpErrorExplanation.forThrowable(localizedContext(), step, err)
    }

    private fun explainError(@StringRes stepRes: Int, err: Throwable): String {
        return HttpErrorExplanation.forThrowable(localizedContext(), str(stepRes), err)
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        return if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    }

    private fun isValidApiBaseUrl(baseUrl: String): Boolean {
        return runCatching {
            val uri = Uri.parse(baseUrl.trim())
            val scheme = uri.scheme
            (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
    }

    private fun createRepository(baseUrl: String): DocspellRepository? {
        return runCatching {
            DocspellRepository(DocspellApiFactory.create(baseUrl, tokenStore, sessionManager))
        }.getOrElse { err ->
            _homeState.value = _homeState.value.copy(
                statusResId = R.string.status_login_failed,
                statusQuery = null,
                isLoading = false,
                error = explainError(R.string.error_step_login, err)
            )
            null
        }
    }

    private fun resolveDocspellQuery(userOrRawQuery: String): String? {
        val trimmed = userOrRawQuery.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.startsWith("tag=") || trimmed.startsWith("tag:") ||
            trimmed.startsWith("cat=") || trimmed.startsWith("cat:") ||
            trimmed.startsWith("corr.")
        ) {
            return trimmed
        }
        return DocspellQueryNormalizer.normalize(trimmed)
    }
}

class DocspellRepository(
    private val api: DocspellApi
) {
    suspend fun login(account: String, password: String): LoginResponse {
        val body = """
            {"account":"${escapeJson(account)}","password":"${escapeJson(password)}"}
        """.trimIndent().toRequestBody("application/json; charset=utf-8".toMediaType())
        return api.login(body)
    }

    suspend fun refreshSession(): LoginResponse = api.refreshSession()

    suspend fun loadTags(): List<TagRow> {
        val response = api.listTags(sort = "name")
        return response.items.map {
            TagRow(
                id = it.id,
                name = it.name.displayText(),
                category = it.category?.displayText()
            )
        }
    }

    suspend fun loadCorrespondents(): List<CorrespondentRow> {
        val orgs = api.listOrganizations(sort = "name").items.map {
            CorrespondentRow(
                id = it.id,
                name = it.name.displayText(),
                type = CorrespondentType.ORGANIZATION
            )
        }
        val persons = api.listPersons(sort = "name").items.map {
            CorrespondentRow(
                id = it.id,
                name = it.name.displayText(),
                type = CorrespondentType.PERSON
            )
        }
        return (orgs + persons).sortedBy { it.name.lowercase() }
    }

    suspend fun loadCategories(): List<String> {
        val tags = api.listTags(sort = "name").items
        return tags
            .mapNotNull { it.category?.trim()?.takeIf { c -> c.isNotEmpty() } }
            .distinct()
            .sortedBy { it.lowercase() }
    }

    suspend fun loadCustomFieldDefinitions(context: Context): List<PickerListItem> {
        return api.listCustomFields().items
            .sortedBy { (it.label?.ifBlank { null } ?: it.name).lowercase() }
            .map { field ->
                val display = field.label?.trim()?.takeIf { it.isNotEmpty() } ?: field.name
                PickerListItem(
                    id = field.name,
                    title = display,
                    subtitle = customFieldTypeLabel(context, field.ftype)
                )
            }
    }

    suspend fun loadCustomFieldValues(fieldName: String): List<String> {
        val response = api.searchItems(query = null, limit = 120, offset = 0, withDetails = true)
        val itemIds = response.groups
            .flatMap { it.items }
            .map { it.id }
            .distinct()
            .take(80)
        val values = linkedSetOf<String>()
        for (itemId in itemIds) {
            val detail = runCatching { api.getItem(itemId) }.getOrNull() ?: continue
            detail.collectCustomFieldValues(fieldName).forEach { values.add(it) }
        }
        return values.sortedBy { it.lowercase() }
    }

    suspend fun loadSidebarFilterValues(
        context: Context,
        filterId: SidebarFilterId
    ): List<FilterPickerOption> {
        val options = linkedMapOf<String, FilterPickerOption>()
        var offset = 0
        val pageSize = 100
        repeat(5) {
            val response = api.searchItems(
                query = null,
                limit = pageSize,
                offset = offset,
                withDetails = true
            )
            val batch = response.groups.flatMap { it.items }
            if (batch.isEmpty()) {
                return@repeat
            }
            batch.forEach { summary ->
                summary.collectSidebarFilterOptions(filterId, context).forEach { option ->
                    options.putIfAbsent(option.queryValue, option)
                }
            }
            offset += batch.size
            if (batch.size < pageSize) {
                return@repeat
            }
        }
        if (filterId == SidebarFilterId.CREATED && options.isEmpty()) {
            val response = api.searchItems(query = null, limit = 80, offset = 0, withDetails = true)
            val itemIds = response.groups.flatMap { it.items }.map { it.id }.distinct().take(40)
            for (itemId in itemIds) {
                val detail = runCatching { api.getItem(itemId) }.getOrNull() ?: continue
                detail.collectSidebarFilterOptions(filterId).forEach { option ->
                    options.putIfAbsent(option.queryValue, option)
                }
            }
        }
        return options.values.sortedBy { it.display.lowercase() }
    }

    suspend fun loadItemDetail(itemId: String): ItemDetail {
        return api.getItem(itemId)
    }

    suspend fun loadDocuments(
        docspellQuery: String?,
        apiBaseUrl: String,
        offset: Int,
        limit: Int
    ): List<DocumentRow> {
        val result = if (docspellQuery == null) {
            api.searchItems(
                query = null,
                limit = limit,
                offset = offset,
                withDetails = true
            )
        } else {
            api.searchItemsPost(
                SearchRequestBody(
                    offset = offset,
                    limit = limit,
                    withDetails = true,
                    query = docspellQuery
                )
            )
        }
        return result.groups.flatMap { it.items }.map { it.toDocumentRow(apiBaseUrl) }
    }

    suspend fun loadSearchResultCount(docspellQuery: String?): Int {
        return if (docspellQuery == null) {
            api.searchStats().count
        } else {
            api.searchStatsPost(
                SearchRequestBody(
                    offset = 0,
                    limit = 0,
                    withDetails = false,
                    query = docspellQuery
                )
            ).count
        }
    }

    private fun escapeJson(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }
}
