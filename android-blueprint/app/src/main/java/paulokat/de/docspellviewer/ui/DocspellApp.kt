package paulokat.de.docspellviewer.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import paulokat.de.docspellviewer.resolveColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import paulokat.de.docspellviewer.DocumentRow
import paulokat.de.docspellviewer.R
import paulokat.de.docspellviewer.DownloadSaveRequest
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import paulokat.de.docspellviewer.AppViewModel
import paulokat.de.docspellviewer.CustomFieldPickerStep
import paulokat.de.docspellviewer.SidebarFilterId
import paulokat.de.docspellviewer.PdfViewerUiState
import paulokat.de.docspellviewer.PickerListItem
import paulokat.de.docspellviewer.PickerPageKey
import paulokat.de.docspellviewer.PickerUiState
import paulokat.de.docspellviewer.StartPageStorage
import paulokat.de.docspellviewer.TagRow
import paulokat.de.docspellviewer.TagsUiState

private const val ROUTE_HOME = "home"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_TAGS = "tags"
private const val ROUTE_CORRESPONDENTS = "correspondents"
private const val ROUTE_CATEGORIES = "categories"
private const val ROUTE_VIEWER = "viewer"
private const val ROUTE_DETAIL = "detail"
private const val ROUTE_LICENSES = "licenses"
private const val ROUTE_FILTER_PICKER = "filter_picker/{filterId}"

@Composable
fun DocspellApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val homeState by viewModel.homeState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    val tagsState by viewModel.tagsState.collectAsState()
    val correspondentsState by viewModel.correspondentsState.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val fieldFilterPickerState by viewModel.fieldFilterPickerState.collectAsState()
    val pdfViewerState by viewModel.pdfViewerState.collectAsState()
    val documentDetailState by viewModel.documentDetailState.collectAsState()
    val offlineListState by viewModel.offlineListState.collectAsState()
    val favoritesListState by viewModel.favoritesListState.collectAsState()
    val thumbnailReloadGeneration by viewModel.thumbnailReloadGeneration.collectAsState()
    var pendingDownload by remember { mutableStateOf<DownloadSaveRequest?>(null) }

    val saveDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument()
    ) { uri: Uri? ->
        val request = pendingDownload
        pendingDownload = null
        if (request != null && uri != null) {
            viewModel.completeDownload(request, uri)
        }
    }

    LaunchedEffect(pdfViewerState.pdfPath) {
        if (pdfViewerState.pdfPath != null) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != ROUTE_VIEWER) {
                navController.navigate(ROUTE_VIEWER)
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val canPopNavigation = navController.previousBackStackEntry != null
    val onCustomFieldValueStep = currentRoute?.startsWith("filter_picker/") == true &&
        fieldFilterPickerState.customFieldStep == CustomFieldPickerStep.CHOOSE_VALUE

    val onHomeRoute = currentRoute == ROUTE_HOME
    BackHandler(enabled = canPopNavigation || onCustomFieldValueStep || onHomeRoute) {
        if (onHomeRoute) {
            return@BackHandler
        }
        if (onCustomFieldValueStep) {
            viewModel.backCustomFieldFilterPicker()
            return@BackHandler
        }
        when (currentRoute) {
            ROUTE_DETAIL -> viewModel.closeDocumentDetail()
            ROUTE_VIEWER -> viewModel.closePdfViewer()
        }
        navController.popBackStack()
    }

    val colorScheme = settingsState.colorScheme.resolveColorScheme(settingsState.useDarkTheme)

    MaterialTheme(colorScheme = colorScheme) {
        SyncSystemBarColors()
        Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            NavHost(
                navController = navController,
                startDestination = ROUTE_HOME
            ) {
                composable(ROUTE_HOME) {
                    val showSearchOverview = StartPageStorage.isSearchPage(settingsState.startPageId) ||
                        homeState.overviewShowSearchTable
                    val overviewEmbeddedPickerBack = !showSearchOverview &&
                        settingsState.startPageId == SidebarFilterId.CUSTOM_FIELDS.preferenceKey &&
                        fieldFilterPickerState.customFieldStep == CustomFieldPickerStep.CHOOSE_VALUE
                    val canStepBackInOverview = showSearchOverview || overviewEmbeddedPickerBack
                    MainShellScreen(
                        homeState = homeState,
                        offlineState = offlineListState,
                        favoritesState = favoritesListState,
                        accountsState = settingsState,
                        pdfViewerState = pdfViewerState,
                        imageReloadGeneration = thumbnailReloadGeneration,
                        needsAccountSetup = homeState.needsSettings,
                        onOpenFilter = { filter ->
                            when (filter) {
                                SidebarFilterId.TAGS -> navController.navigate(ROUTE_TAGS)
                                SidebarFilterId.CORRESPONDENT -> navController.navigate(ROUTE_CORRESPONDENTS)
                                SidebarFilterId.CATEGORY -> navController.navigate(ROUTE_CATEGORIES)
                                else -> navController.navigate("filter_picker/${filter.name}")
                            }
                        },
                        onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                        onSync = viewModel::syncWithServer,
                        onOpenDocument = viewModel::startPdfViewer,
                        onOpenDetail = { doc ->
                            viewModel.openDocumentDetail(doc)
                            navController.navigate(ROUTE_DETAIL)
                        },
                        onRefreshAccounts = viewModel::refreshAccountsState,
                        onSelectAccount = viewModel::selectAccountForEdit,
                        onAddAccount = viewModel::addAccount,
                        onDeleteAccount = viewModel::deleteEditingAccount,
                        onActivateAccount = viewModel::activateAccount,
                        onDisplayNameChange = viewModel::onSettingsDisplayNameChange,
                        onBaseUrlChange = viewModel::onSettingsBaseUrlChange,
                        onAccountChange = viewModel::onSettingsAccountChange,
                        onPasswordChange = viewModel::onSettingsPasswordChange,
                        onShowPasswordChange = viewModel::onSettingsShowPasswordChange,
                        onSaveAccount = viewModel::saveActiveAccount,
                        onClearUserOfflineData = viewModel::clearUserOfflineDocuments,
                        onClearUserViewerCache = viewModel::clearUserViewerCache,
                        canStepBackInOverview = canStepBackInOverview,
                        onOverviewBack = {
                            when {
                                showSearchOverview -> viewModel.onOverviewTabSelected()
                                overviewEmbeddedPickerBack -> viewModel.backCustomFieldFilterPicker()
                            }
                        },
                        onTabSelected = { tab ->
                            when (tab) {
                                MainTab.Offline -> viewModel.loadOfflineDocuments()
                                MainTab.Favorites -> viewModel.loadFavoriteDocuments()
                                MainTab.Account -> viewModel.refreshAccountsState()
                                MainTab.Overview -> viewModel.onOverviewTabSelected()
                            }
                        },
                        onTabBack = { tab ->
                            when (tab) {
                                MainTab.Offline -> viewModel.loadOfflineDocuments()
                                MainTab.Favorites -> viewModel.loadFavoriteDocuments()
                                MainTab.Account -> viewModel.refreshAccountsState()
                                MainTab.Overview -> Unit
                            }
                        },
                        overviewContent = {
                            if (showSearchOverview) {
                                HomeScreenContent(
                                    state = homeState,
                                    pdfViewerState = pdfViewerState,
                                    imageReloadGeneration = thumbnailReloadGeneration,
                                    onQueryChange = viewModel::onQueryChange,
                                    onSearch = viewModel::runSearch,
                                    onOpenDocument = viewModel::startPdfViewer,
                                    onOpenDetail = { doc ->
                                        viewModel.openDocumentDetail(doc)
                                        navController.navigate(ROUTE_DETAIL)
                                    },
                                    onLoadMore = viewModel::loadMore,
                                    onCorrespondentClick = viewModel::searchByCorrespondentFromDocument
                                )
                            } else {
                                StartPageOverviewContent(
                                    startPageKey = settingsState.startPageId,
                                    viewModel = viewModel,
                                    tagsState = tagsState,
                                    correspondentsState = correspondentsState,
                                    categoriesState = categoriesState,
                                    fieldFilterPickerState = fieldFilterPickerState,
                                    onFilterSearchApplied = viewModel::onStartPageFilterApplied
                                )
                            }
                        }
                    )
                }
                composable(ROUTE_DETAIL) {
                    DocumentDetailScreen(
                        state = documentDetailState,
                        pdfViewerState = pdfViewerState,
                        imageReloadGeneration = thumbnailReloadGeneration,
                        onBack = {
                            viewModel.closeDocumentDetail()
                            navController.popBackStack()
                        },
                        onOpenDocument = {
                            val doc = documentDetailState.viewerDocument ?: return@DocumentDetailScreen
                            viewModel.startPdfViewer(doc)
                        },
                        onAttachmentClick = { attachment ->
                            val request = viewModel.createDownloadRequest(attachment)
                                ?: return@DocumentDetailScreen
                            pendingDownload = request
                            saveDocumentLauncher.launch(request.suggestedFileName)
                        },
                        onAudioPlayClick = viewModel::openAudioPlayer,
                        onAudioPlayPause = viewModel::toggleAudioPlayPause,
                        onAudioSeek = viewModel::seekAudio,
                        onAudioSkipBackward = viewModel::skipAudioBackward,
                        onAudioSkipForward = viewModel::skipAudioForward,
                        onMakeOffline = viewModel::makeDocumentOfflineAvailable,
                        onDeleteOffline = viewModel::deleteOfflineDocument,
                        onToggleFavorite = viewModel::toggleFavorite
                    )
                }
                composable(ROUTE_VIEWER) {
                    PdfViewerScreen(
                        state = pdfViewerState,
                        onRenderProgress = { percent ->
                            pdfViewerState.loadingDocumentId?.let { id ->
                                viewModel.updatePdfRenderProgress(id, percent)
                            }
                        },
                        onRenderComplete = {
                            pdfViewerState.loadingDocumentId?.let { id ->
                                viewModel.completePdfRender(id)
                            }
                        },
                        onBack = {
                            viewModel.closePdfViewer()
                            navController.popBackStack()
                        }
                    )
                }
                composable(ROUTE_TAGS) {
                    LaunchedEffect(Unit) { viewModel.loadTags() }
                    PickerScreen(
                        title = stringResource(R.string.picker_tags),
                        state = tagsState.toPickerUiState(),
                        pickerPageKey = PickerPageKey.TAGS,
                        initialViewMode = viewModel.getPickerViewMode(PickerPageKey.TAGS),
                        onViewModeChange = { viewModel.setPickerViewMode(PickerPageKey.TAGS, it) },
                        onItemClick = { item ->
                            viewModel.searchByTag(item.title)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(ROUTE_CORRESPONDENTS) {
                    LaunchedEffect(Unit) { viewModel.loadCorrespondents() }
                    PickerScreen(
                        title = stringResource(R.string.picker_organization),
                        state = correspondentsState,
                        pickerPageKey = PickerPageKey.CORRESPONDENTS,
                        initialViewMode = viewModel.getPickerViewMode(PickerPageKey.CORRESPONDENTS),
                        onViewModeChange = {
                            viewModel.setPickerViewMode(PickerPageKey.CORRESPONDENTS, it)
                        },
                        onItemClick = { item ->
                            viewModel.searchByCorrespondent(item)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(ROUTE_CATEGORIES) {
                    LaunchedEffect(Unit) { viewModel.loadCategories() }
                    PickerScreen(
                        title = stringResource(R.string.picker_category),
                        state = categoriesState,
                        pickerPageKey = PickerPageKey.CATEGORIES,
                        initialViewMode = viewModel.getPickerViewMode(PickerPageKey.CATEGORIES),
                        onViewModeChange = { viewModel.setPickerViewMode(PickerPageKey.CATEGORIES, it) },
                        onItemClick = { item ->
                            viewModel.searchByCategory(item.title)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = ROUTE_FILTER_PICKER,
                    arguments = listOf(
                        navArgument("filterId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val filterId = backStackEntry.arguments?.getString("filterId")
                    val filter = SidebarFilterId.entries.find { it.name == filterId }
                    if (filter == null) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        LaunchedEffect(filter) { viewModel.loadFieldFilterPicker(filter) }
                        val pickerTitle = fieldFilterPickerState.customFieldFieldLabel
                            ?: fieldFilterPickerState.customFieldScreenTitleRes?.let { stringResource(it) }
                            ?: stringResource(filter.labelRes)
                        val pickerPageKey = if (
                            filter == SidebarFilterId.CUSTOM_FIELDS &&
                            fieldFilterPickerState.customFieldStep == CustomFieldPickerStep.CHOOSE_VALUE
                        ) {
                            PickerPageKey.CUSTOM_FIELD_VALUES
                        } else {
                            PickerPageKey.forFilter(filter)
                        }
                        key(pickerPageKey) {
                        PickerScreen(
                            title = pickerTitle,
                            state = fieldFilterPickerState,
                            pickerPageKey = pickerPageKey,
                            initialViewMode = viewModel.getPickerViewMode(pickerPageKey),
                            onViewModeChange = { viewModel.setPickerViewMode(pickerPageKey, it) },
                            onItemClick = { item ->
                                if (filter == SidebarFilterId.CUSTOM_FIELDS &&
                                    fieldFilterPickerState.customFieldStep == CustomFieldPickerStep.CHOOSE_FIELD
                                ) {
                                    viewModel.loadCustomFieldValuePicker(
                                        fieldName = item.id,
                                        fieldLabel = item.title
                                    )
                                } else {
                                    viewModel.searchByFieldFilter(item.id)
                                    navController.popBackStack(ROUTE_HOME, inclusive = false)
                                }
                            },
                            onBack = {
                                if (filter == SidebarFilterId.CUSTOM_FIELDS &&
                                    fieldFilterPickerState.customFieldStep == CustomFieldPickerStep.CHOOSE_VALUE
                                ) {
                                    viewModel.backCustomFieldFilterPicker()
                                } else {
                                    navController.popBackStack()
                                }
                            }
                        )
                        }
                    }
                }
                composable(ROUTE_SETTINGS) {
                    LaunchedEffect(Unit) { viewModel.refreshSettingsState() }
                    SettingsScreen(
                        state = settingsState,
                        onColorSchemeChange = viewModel::onSettingsColorSchemeChange,
                        onDarkThemeChange = viewModel::onSettingsDarkThemeChange,
                        onPageSizeChange = viewModel::onSettingsPageSizeChange,
                        onStartPageChange = viewModel::onSettingsStartPageChange,
                        onDetailFieldVisibilityChange = viewModel::onDetailFieldVisibilityChange,
                        onDetailFieldMoveUp = viewModel::onDetailFieldMoveUp,
                        onDetailFieldMoveDown = viewModel::onDetailFieldMoveDown,
                        onSidebarFilterVisibilityChange = viewModel::onSidebarFilterVisibilityChange,
                        onSidebarFilterMoveUp = viewModel::onSidebarFilterMoveUp,
                        onSidebarFilterMoveDown = viewModel::onSidebarFilterMoveDown,
                        onRefreshStorageStats = viewModel::refreshStorageStats,
                        onClearAllOfflineData = viewModel::clearAllOfflineDocumentsGlobal,
                        onClearAllViewerCache = viewModel::clearViewerCacheGlobal,
                        onOpenLicenses = { navController.navigate(ROUTE_LICENSES) },
                        onAppLanguageChange = viewModel::onAppLanguageChange,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(ROUTE_LICENSES) {
                    LicensesScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

private fun TagsUiState.toPickerUiState(): PickerUiState {
    return PickerUiState(
        isLoading = isLoading,
        items = tags.map { it.toPickerItem() },
        error = error
    )
}

private fun TagRow.toPickerItem(): PickerListItem {
    return PickerListItem(id = id, title = name, subtitle = category)
}
