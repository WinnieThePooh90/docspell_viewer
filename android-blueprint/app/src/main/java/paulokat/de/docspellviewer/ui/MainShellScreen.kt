package paulokat.de.docspellviewer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import paulokat.de.docspellviewer.DocumentRow
import paulokat.de.docspellviewer.R
import paulokat.de.docspellviewer.FavoritesListUiState
import paulokat.de.docspellviewer.HomeUiState
import paulokat.de.docspellviewer.SidebarFilterId
import paulokat.de.docspellviewer.OfflineListUiState
import paulokat.de.docspellviewer.PdfViewerUiState
import paulokat.de.docspellviewer.SettingsUiState

enum class MainTab {
    Overview,
    Offline,
    Favorites,
    Account
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShellScreen(
    homeState: HomeUiState,
    offlineState: OfflineListUiState,
    favoritesState: FavoritesListUiState,
    accountsState: SettingsUiState,
    pdfViewerState: PdfViewerUiState,
    imageReloadGeneration: Int,
    needsAccountSetup: Boolean,
    onOpenFilter: (SidebarFilterId) -> Unit,
    onOpenSettings: () -> Unit,
    onSync: () -> Unit,
    onOpenDocument: (DocumentRow) -> Unit,
    onOpenDetail: (DocumentRow) -> Unit,
    onRefreshAccounts: () -> Unit,
    onSelectAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
    onDeleteAccount: () -> Unit,
    onActivateAccount: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onShowPasswordChange: (Boolean) -> Unit,
    onSaveAccount: () -> Unit,
    onClearUserOfflineData: () -> Unit,
    onClearUserViewerCache: () -> Unit,
    onTabSelected: (MainTab) -> Unit,
    onTabBack: (MainTab) -> Unit = {},
    overviewContent: @Composable () -> Unit,
    canStepBackInOverview: Boolean = false,
    onOverviewBack: () -> Unit = {},
    initialTab: MainTab = MainTab.Overview
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var tabBackStack by rememberSaveable { mutableStateOf(listOf<String>()) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun navigateToTab(tab: MainTab) {
        if (tab == selectedTab) {
            return
        }
        tabBackStack = tabBackStack + selectedTab.name
        selectedTab = tab
        onTabSelected(tab)
    }

    LaunchedEffect(needsAccountSetup) {
        if (needsAccountSetup && selectedTab != MainTab.Account) {
            navigateToTab(MainTab.Account)
        }
    }

    LaunchedEffect(homeState.navigateToOverviewNonce) {
        if (homeState.navigateToOverviewNonce > 0 && selectedTab != MainTab.Overview) {
            tabBackStack = emptyList()
            selectedTab = MainTab.Overview
            onTabSelected(MainTab.Overview)
        }
    }

    fun closeDrawerAnd(action: () -> Unit) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    BackHandler {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            selectedTab == MainTab.Overview && canStepBackInOverview -> onOverviewBack()
            tabBackStack.isNotEmpty() -> {
                val previousName = tabBackStack.last()
                tabBackStack = tabBackStack.dropLast(1)
                val previousTab = MainTab.entries.firstOrNull { it.name == previousName }
                    ?: MainTab.Overview
                selectedTab = previousTab
                onTabBack(previousTab)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FilterSidebarContent(
                visibility = homeState.sidebarFilterVisibility,
                onClose = { scope.launch { drawerState.close() } },
                onFilterClick = { filter -> closeDrawerAnd { onOpenFilter(filter) } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CompactMainTopBar(
                    activeAccountLabel = homeState.activeAccountLabel,
                    isLoading = homeState.isLoading,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isOpen) {
                                drawerState.close()
                            } else {
                                drawerState.open()
                            }
                        }
                    },
                    onOpenSettings = onOpenSettings,
                    onSync = onSync
                )
            },
            bottomBar = {
                MainBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = ::navigateToTab
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when (selectedTab) {
                    MainTab.Overview -> overviewContent()
                    MainTab.Offline -> OfflineDocumentsContent(
                        state = offlineState,
                        pdfViewerState = pdfViewerState,
                        imageReloadGeneration = imageReloadGeneration,
                        onOpenDocument = onOpenDocument,
                        onOpenDetail = onOpenDetail
                    )
                    MainTab.Favorites -> FavoritesDocumentsContent(
                        state = favoritesState,
                        pdfViewerState = pdfViewerState,
                        imageReloadGeneration = imageReloadGeneration,
                        onOpenDocument = onOpenDocument,
                        onOpenDetail = onOpenDetail
                    )
                    MainTab.Account -> AccountsScreenContent(
                        state = accountsState,
                        onRefresh = onRefreshAccounts,
                        onSelectAccount = onSelectAccount,
                        onAddAccount = onAddAccount,
                        onDeleteAccount = onDeleteAccount,
                        onActivateAccount = onActivateAccount,
                        onDisplayNameChange = onDisplayNameChange,
                        onBaseUrlChange = onBaseUrlChange,
                        onAccountChange = onAccountChange,
                        onPasswordChange = onPasswordChange,
                        onShowPasswordChange = onShowPasswordChange,
                        onSaveAccount = onSaveAccount,
                        onClearUserOfflineData = onClearUserOfflineData,
                        onClearUserViewerCache = onClearUserViewerCache
                    )
                }
            }
        }
    }
}

@Composable
private fun MainBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainBottomBarItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.tab_overview),
                    icon = Icons.Default.Dashboard,
                    selected = selectedTab == MainTab.Overview,
                    onClick = { onTabSelected(MainTab.Overview) }
                )
                MainBottomBarItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.tab_offline),
                    icon = Icons.Default.DownloadForOffline,
                    selected = selectedTab == MainTab.Offline,
                    onClick = { onTabSelected(MainTab.Offline) }
                )
                MainBottomBarItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.tab_favorites),
                    icon = Icons.Default.Star,
                    selected = selectedTab == MainTab.Favorites,
                    onClick = { onTabSelected(MainTab.Favorites) }
                )
                MainBottomBarItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.tab_account),
                    icon = Icons.Default.Person,
                    selected = selectedTab == MainTab.Account,
                    onClick = { onTabSelected(MainTab.Account) }
                )
            }
        }
    }
}

@Composable
private fun MainBottomBarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = contentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
