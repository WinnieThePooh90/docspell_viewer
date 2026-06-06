package paulokat.de.docspellviewer.ui

import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import paulokat.de.docspellviewer.AppInfo
import paulokat.de.docspellviewer.AppLanguage
import paulokat.de.docspellviewer.R
import paulokat.de.docspellviewer.AppColorScheme
import paulokat.de.docspellviewer.BuildConfig
import paulokat.de.docspellviewer.DetailFieldId
import paulokat.de.docspellviewer.SidebarFilterId
import paulokat.de.docspellviewer.PAGE_SIZE_OPTIONS
import paulokat.de.docspellviewer.SettingsUiState
import paulokat.de.docspellviewer.StartPageStorage
import paulokat.de.docspellviewer.SupportLinks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAppLanguageChange: (AppLanguage) -> Unit,
    onColorSchemeChange: (AppColorScheme) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onPageSizeChange: (Int) -> Unit,
    onStartPageChange: (String) -> Unit,
    onDetailFieldVisibilityChange: (DetailFieldId, Boolean) -> Unit,
    onDetailFieldMoveUp: (DetailFieldId) -> Unit,
    onDetailFieldMoveDown: (DetailFieldId) -> Unit,
    onSidebarFilterVisibilityChange: (SidebarFilterId, Boolean) -> Unit,
    onSidebarFilterMoveUp: (SidebarFilterId) -> Unit,
    onSidebarFilterMoveDown: (SidebarFilterId) -> Unit,
    onRefreshStorageStats: () -> Unit,
    onClearAllOfflineData: () -> Unit,
    onClearAllViewerCache: () -> Unit,
    onOpenLicenses: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        onRefreshStorageStats()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                CompactBackTopBar(
                    title = stringResource(R.string.settings_title),
                    onBack = onBack,
                    backContentDescription = stringResource(R.string.cd_back)
                )
                FeedbackBanner(
                    message = state.saveMessage,
                    isError = state.saveMessageIsError
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${AppInfo.NAME} ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.settings_build, BuildConfig.BUILD_STAMP),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.language_section_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.language_section_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AppLanguage.entries.forEach { language ->
                LanguageOptionRow(
                    label = stringResource(language.displayNameRes),
                    selected = state.appLanguage == language,
                    onSelect = { onAppLanguageChange(language) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.settings_appearance),
                style = MaterialTheme.typography.titleMedium
            )
            AppColorScheme.entries.forEach { scheme ->
                ColorSchemeOptionRow(
                    label = stringResource(scheme.labelRes),
                    scheme = scheme,
                    selected = state.colorScheme == scheme,
                    onSelect = { onColorSchemeChange(scheme) }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_dark_mode),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.settings_dark_mode_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.useDarkTheme,
                    onCheckedChange = onDarkThemeChange
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.settings_page_size),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.settings_page_size_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PAGE_SIZE_OPTIONS.forEach { size ->
                PageSizeOptionRow(
                    label = size.toString(),
                    selected = state.pageSize == size,
                    onSelect = { onPageSizeChange(size) }
                )
            }
            Text(
                text = stringResource(R.string.settings_start_page),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            StartPageStorage.selectableOptions().forEach { option ->
                PageSizeOptionRow(
                    label = stringResource(option.labelRes),
                    selected = state.startPageId == option.storageKey,
                    onSelect = { onStartPageChange(option.storageKey) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.settings_detail_fields),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.settings_detail_fields_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            state.detailFieldVisibility.order.forEachIndexed { index, field ->
                VisibilityOrderRow(
                    label = stringResource(field.labelRes),
                    checked = state.detailFieldVisibility.isEnabled(field),
                    canMoveUp = index > 0,
                    canMoveDown = index < state.detailFieldVisibility.order.lastIndex,
                    onCheckedChange = { enabled ->
                        onDetailFieldVisibilityChange(field, enabled)
                    },
                    onMoveUp = { onDetailFieldMoveUp(field) },
                    onMoveDown = { onDetailFieldMoveDown(field) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.settings_filters),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.settings_filters_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            state.sidebarFilterVisibility.order.forEachIndexed { index, filter ->
                VisibilityOrderRow(
                    label = stringResource(filter.labelRes),
                    checked = state.sidebarFilterVisibility.isEnabled(filter),
                    canMoveUp = index > 0,
                    canMoveDown = index < state.sidebarFilterVisibility.order.lastIndex,
                    onCheckedChange = { enabled ->
                        onSidebarFilterVisibilityChange(filter, enabled)
                    },
                    onMoveUp = { onSidebarFilterMoveUp(filter) },
                    onMoveDown = { onSidebarFilterMoveDown(filter) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.settings_cache_offline),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = pluralStringResource(
                    R.plurals.settings_offline_documents,
                    state.totalOfflineDocumentCount,
                    state.totalOfflineDocumentCount
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            StorageStatRow(
                label = stringResource(R.string.settings_offline_storage),
                value = Formatter.formatFileSize(context, state.totalOfflineStorageBytes),
                buttonLabel = stringResource(R.string.settings_delete_data),
                onButtonClick = onClearAllOfflineData
            )
            StorageStatRow(
                label = stringResource(R.string.settings_viewer_cache),
                value = Formatter.formatFileSize(context, state.totalViewerCacheBytes),
                buttonLabel = stringResource(R.string.settings_clear_cache),
                onButtonClick = onClearAllViewerCache
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.settings_support),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.settings_support_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                BuyMeACoffeeButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(SupportLinks.BUY_ME_A_COFFEE_URL)
                        )
                        context.startActivity(intent)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedButton(
                onClick = onOpenPrivacy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_privacy))
            }

            OutlinedButton(
                onClick = onOpenLicenses,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_licenses))
            }
        }
    }
}

@Composable
private fun LanguageOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(
            text = label,
            modifier = Modifier
                .padding(start = 4.dp)
                .weight(1f)
        )
    }
}

@Composable
private fun StorageStatRow(
    label: String,
    value: String,
    buttonLabel: String,
    onButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label $value",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        OutlinedButton(onClick = onButtonClick) {
            Text(buttonLabel)
        }
    }
}

@Composable
private fun BuyMeACoffeeButton(onClick: () -> Unit) {
    val bmcYellow = Color(0xFFFFDD00)
    val bmcBlack = Color(0xFF000000)

    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, bmcBlack),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = bmcYellow,
            contentColor = bmcBlack
        ),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        AsyncImage(
            model = "https://cdn.buymeacoffee.com/buttons/bmc-new-btn-logo.svg",
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "Buy me a coffee",
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = bmcBlack
        )
    }
}

@Composable
private fun VisibilityOrderRow(
    label: String,
    checked: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            modifier = Modifier
                .padding(start = 4.dp)
                .weight(1f)
        )
        IconButton(
            onClick = onMoveUp,
            enabled = canMoveUp,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = stringResource(R.string.cd_move_up)
            )
        }
        IconButton(
            onClick = onMoveDown,
            enabled = canMoveDown,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = stringResource(R.string.cd_move_down)
            )
        }
    }
}

@Composable
private fun ColorSchemeOptionRow(
    label: String,
    scheme: AppColorScheme,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(
            text = label,
            modifier = Modifier
                .padding(start = 4.dp)
                .weight(1f)
        )
        ColorSchemePreviewDot(scheme = scheme)
    }
}

@Composable
private fun ColorSchemePreviewDot(scheme: AppColorScheme) {
    val colors = when (scheme) {
        AppColorScheme.GRAYSCALE -> listOf(
            Color(0xFF212121),
            Color(0xFF9E9E9E),
            Color(0xFFFFFFFF)
        )
        AppColorScheme.VIOLET -> listOf(
            Color(0xFF6750A4),
            Color(0xFFEADDFF),
            Color(0xFF21005D)
        )
        AppColorScheme.BLUE -> listOf(
            Color(0xFF1565C0),
            Color(0xFFD1E4FF),
            Color(0xFF001D36)
        )
        AppColorScheme.GREEN -> listOf(
            Color(0xFF2E7D32),
            Color(0xFFC8E6C9),
            Color(0xFF1B5E20)
        )
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun PageSizeOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = label, modifier = Modifier.padding(start = 4.dp))
    }
}
