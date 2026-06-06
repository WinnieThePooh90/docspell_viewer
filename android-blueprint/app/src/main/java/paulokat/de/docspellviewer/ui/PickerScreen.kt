package paulokat.de.docspellviewer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import paulokat.de.docspellviewer.PickerListItem
import paulokat.de.docspellviewer.PickerUiState
import paulokat.de.docspellviewer.PickerViewMode
import paulokat.de.docspellviewer.R

private data class PickerSection(
    val title: String?,
    val items: List<PickerListItem>
)

@Composable
fun PickerScreen(
    title: String,
    state: PickerUiState,
    pickerPageKey: String,
    initialViewMode: PickerViewMode,
    onViewModeChange: (PickerViewMode) -> Unit,
    onItemClick: (PickerListItem) -> Unit,
    onBack: () -> Unit
) {
    PickerScreenWithViewMode(
        state = state,
        pickerPageKey = pickerPageKey,
        initialViewMode = initialViewMode,
        onViewModeChange = onViewModeChange,
        onItemClick = onItemClick
    ) { viewMode, onToggleViewMode ->
        Scaffold(
            topBar = {
                CompactBackTopBar(
                    title = title,
                    onBack = onBack,
                    backContentDescription = stringResource(R.string.cd_back),
                    actions = {
                        PickerViewModeToggle(
                            state = state,
                            viewMode = viewMode,
                            onToggle = onToggleViewMode
                        )
                    }
                )
            }
        ) { padding ->
            PickerScreenBodyContent(
                state = state,
                viewMode = viewMode,
                onItemClick = onItemClick,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun EmbeddedPickerPanel(
    title: String,
    state: PickerUiState,
    pickerPageKey: String,
    initialViewMode: PickerViewMode,
    onViewModeChange: (PickerViewMode) -> Unit,
    onItemClick: (PickerListItem) -> Unit,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    PickerScreenWithViewMode(
        state = state,
        pickerPageKey = pickerPageKey,
        initialViewMode = initialViewMode,
        onViewModeChange = onViewModeChange,
        onItemClick = onItemClick
    ) { viewMode, onToggleViewMode ->
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBack && onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(CompactTopBarIconButtonSize)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            modifier = Modifier.size(CompactTopBarIconSize)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                PickerViewModeToggle(
                    state = state,
                    viewMode = viewMode,
                    onToggle = onToggleViewMode
                )
            }
            PickerScreenBodyContent(
                state = state,
                viewMode = viewMode,
                onItemClick = onItemClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PickerScreenWithViewMode(
    state: PickerUiState,
    pickerPageKey: String,
    initialViewMode: PickerViewMode,
    onViewModeChange: (PickerViewMode) -> Unit,
    onItemClick: (PickerListItem) -> Unit,
    content: @Composable (
        viewMode: PickerViewMode,
        onToggleViewMode: (PickerViewMode) -> Unit
    ) -> Unit
) {
    var viewMode by remember(pickerPageKey) { mutableStateOf(initialViewMode) }
    LaunchedEffect(pickerPageKey, initialViewMode) {
        viewMode = initialViewMode
    }
    content(viewMode) { newMode ->
        viewMode = newMode
        onViewModeChange(newMode)
    }
}

@Composable
private fun PickerViewModeToggle(
    state: PickerUiState,
    viewMode: PickerViewMode,
    onToggle: (PickerViewMode) -> Unit
) {
    val showViewToggle = !state.isLoading && state.error == null && state.items.isNotEmpty()
    if (!showViewToggle) {
        return
    }
    IconButton(
        onClick = {
            val newMode = when (viewMode) {
                PickerViewMode.LIST -> PickerViewMode.TILES
                PickerViewMode.TILES -> PickerViewMode.LIST
            }
            onToggle(newMode)
        },
        modifier = Modifier.size(CompactTopBarIconButtonSize)
    ) {
        Icon(
            imageVector = when (viewMode) {
                PickerViewMode.LIST -> Icons.Default.GridView
                PickerViewMode.TILES -> Icons.Default.ViewList
            },
            contentDescription = when (viewMode) {
                PickerViewMode.LIST -> stringResource(R.string.cd_picker_show_tiles)
                PickerViewMode.TILES -> stringResource(R.string.cd_picker_show_list)
            },
            modifier = Modifier.size(CompactTopBarIconSize)
        )
    }
}

@Composable
private fun PickerScreenBodyContent(
    state: PickerUiState,
    viewMode: PickerViewMode,
    onItemClick: (PickerListItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val uncategorizedLabel = stringResource(R.string.picker_section_uncategorized)

    Column(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            state.error?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.items.isEmpty() && state.error == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.picker_no_entries),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (state.error == null) {
                when (viewMode) {
                    PickerViewMode.LIST -> PickerListContent(
                        items = state.items,
                        onItemClick = onItemClick
                    )
                    PickerViewMode.TILES -> PickerTileContent(
                        sections = buildPickerSections(state.items, uncategorizedLabel),
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerListContent(
    items: List<PickerListItem>,
    onItemClick: (PickerListItem) -> Unit
) {
    LazyColumn {
        items(items, key = { it.id }) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = pickerItemTitle(item),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    pickerItemSubtitle(item)?.takeIf { it.isNotBlank() }?.let { sub ->
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun PickerTileContent(
    sections: List<PickerSection>,
    onItemClick: (PickerListItem) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        sections.forEachIndexed { sectionIndex, section ->
            section.title?.let { sectionTitle ->
                item(key = "header-$sectionIndex-$sectionTitle") {
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            section.items.chunked(2).forEachIndexed { rowIndex, rowItems ->
                item(key = "row-$sectionIndex-$rowIndex-${rowItems.joinToString { it.id }}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            PickerTileButton(
                                item = item,
                                onClick = { onItemClick(item) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PickerTileButton(
    item: PickerListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(
            text = pickerItemTitle(item),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun pickerItemTitle(item: PickerListItem): String {
    return if (item.id == PickerListItem.ANY_VALUE_ID) {
        stringResource(R.string.picker_any_value)
    } else {
        item.title
    }
}

@Composable
private fun pickerItemSubtitle(item: PickerListItem): String? {
    return if (item.id == PickerListItem.ANY_VALUE_ID) {
        stringResource(R.string.picker_field_set)
    } else {
        item.subtitle
    }
}

private fun buildPickerSections(
    items: List<PickerListItem>,
    uncategorizedLabel: String
): List<PickerSection> {
    val anyValueItems = items.filter { it.id == PickerListItem.ANY_VALUE_ID }
    val regularItems = items.filter { it.id != PickerListItem.ANY_VALUE_ID }
    val hasSectionTitles = regularItems.any { !it.subtitle.isNullOrBlank() }

    val groupedSections = if (hasSectionTitles) {
        regularItems
            .groupBy { item ->
                item.subtitle?.trim()?.takeIf { it.isNotEmpty() } ?: uncategorizedLabel
            }
            .entries
            .sortedBy { (title, _) -> title.lowercase() }
            .map { (title, sectionItems) ->
                PickerSection(
                    title = title,
                    items = sectionItems.sortedBy { it.title.lowercase() }
                )
            }
    } else {
        listOf(
            PickerSection(
                title = null,
                items = regularItems.sortedBy { it.title.lowercase() }
            )
        )
    }

    return buildList {
        if (anyValueItems.isNotEmpty()) {
            add(PickerSection(title = null, items = anyValueItems))
        }
        addAll(groupedSections)
    }
}
