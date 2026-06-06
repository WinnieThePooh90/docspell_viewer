package paulokat.de.docspellviewer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.res.stringResource
import paulokat.de.docspellviewer.AppViewModel
import paulokat.de.docspellviewer.CustomFieldPickerStep
import paulokat.de.docspellviewer.PickerListItem
import paulokat.de.docspellviewer.PickerPageKey
import paulokat.de.docspellviewer.PickerUiState
import paulokat.de.docspellviewer.SidebarFilterId
import paulokat.de.docspellviewer.StartPageStorage
import paulokat.de.docspellviewer.TagRow
import paulokat.de.docspellviewer.TagsUiState

@Composable
fun StartPageOverviewContent(
    startPageKey: String,
    viewModel: AppViewModel,
    tagsState: TagsUiState,
    correspondentsState: PickerUiState,
    categoriesState: PickerUiState,
    fieldFilterPickerState: PickerUiState,
    onFilterSearchApplied: () -> Unit
) {
    val filter = StartPageStorage.toSidebarFilter(startPageKey)
    LaunchedEffect(startPageKey, fieldFilterPickerState.customFieldStep) {
        when (filter) {
            SidebarFilterId.TAGS -> viewModel.loadTags()
            SidebarFilterId.CORRESPONDENT -> viewModel.loadCorrespondents()
            SidebarFilterId.CATEGORY -> viewModel.loadCategories()
            SidebarFilterId.CUSTOM_FIELDS -> {
                if (fieldFilterPickerState.customFieldStep != CustomFieldPickerStep.CHOOSE_VALUE) {
                    viewModel.loadFieldFilterPicker(SidebarFilterId.CUSTOM_FIELDS)
                }
            }
            null -> Unit
            else -> viewModel.loadFieldFilterPicker(filter)
        }
    }

    val pickerState = when (filter) {
        SidebarFilterId.TAGS -> tagsState.toPickerUiState()
        SidebarFilterId.CORRESPONDENT -> correspondentsState
        SidebarFilterId.CATEGORY -> categoriesState
        else -> fieldFilterPickerState
    }
    val pickerPageKey = when {
        filter == SidebarFilterId.CUSTOM_FIELDS &&
            fieldFilterPickerState.customFieldStep == CustomFieldPickerStep.CHOOSE_VALUE ->
            PickerPageKey.CUSTOM_FIELD_VALUES
        filter == SidebarFilterId.TAGS -> PickerPageKey.TAGS
        filter == SidebarFilterId.CORRESPONDENT -> PickerPageKey.CORRESPONDENTS
        filter == SidebarFilterId.CATEGORY -> PickerPageKey.CATEGORIES
        filter != null -> PickerPageKey.forFilter(filter)
        else -> startPageKey
    }
    val pickerTitle = when {
        filter == SidebarFilterId.CUSTOM_FIELDS &&
            fieldFilterPickerState.customFieldStep == CustomFieldPickerStep.CHOOSE_VALUE ->
            fieldFilterPickerState.customFieldFieldLabel.orEmpty()
        filter == SidebarFilterId.TAGS -> stringResource(paulokat.de.docspellviewer.R.string.picker_tags)
        filter == SidebarFilterId.CORRESPONDENT ->
            stringResource(paulokat.de.docspellviewer.R.string.picker_organization)
        filter == SidebarFilterId.CATEGORY ->
            stringResource(paulokat.de.docspellviewer.R.string.picker_category)
        fieldFilterPickerState.customFieldScreenTitleRes != null ->
            stringResource(fieldFilterPickerState.customFieldScreenTitleRes!!)
        filter != null -> stringResource(filter.labelRes)
        else -> ""
    }
    val showEmbeddedBack = filter == SidebarFilterId.CUSTOM_FIELDS &&
        fieldFilterPickerState.customFieldStep == CustomFieldPickerStep.CHOOSE_VALUE

    key(pickerPageKey) {
        EmbeddedPickerPanel(
            title = pickerTitle,
            state = pickerState,
            pickerPageKey = pickerPageKey,
            initialViewMode = viewModel.getPickerViewMode(pickerPageKey),
            onViewModeChange = { viewModel.setPickerViewMode(pickerPageKey, it) },
            showBack = showEmbeddedBack,
            onBack = if (showEmbeddedBack) {
                { viewModel.backCustomFieldFilterPicker() }
            } else {
                null
            },
            onItemClick = { item ->
                handleStartPageItemClick(
                    filter = filter,
                    fieldFilterPickerState = fieldFilterPickerState,
                    item = item,
                    viewModel = viewModel,
                    onFilterSearchApplied = onFilterSearchApplied
                )
            }
        )
    }
}

private fun handleStartPageItemClick(
    filter: SidebarFilterId?,
    fieldFilterPickerState: PickerUiState,
    item: PickerListItem,
    viewModel: AppViewModel,
    onFilterSearchApplied: () -> Unit
) {
    when (filter) {
        SidebarFilterId.TAGS -> {
            viewModel.searchByTag(item.title)
            onFilterSearchApplied()
        }
        SidebarFilterId.CORRESPONDENT -> {
            viewModel.searchByCorrespondent(item)
            onFilterSearchApplied()
        }
        SidebarFilterId.CATEGORY -> {
            viewModel.searchByCategory(item.title)
            onFilterSearchApplied()
        }
        SidebarFilterId.CUSTOM_FIELDS -> {
            if (fieldFilterPickerState.customFieldStep == CustomFieldPickerStep.CHOOSE_FIELD) {
                viewModel.loadCustomFieldValuePicker(
                    fieldName = item.id,
                    fieldLabel = item.title
                )
            } else {
                viewModel.searchByFieldFilter(item.id)
                onFilterSearchApplied()
            }
        }
        null -> Unit
        else -> {
            viewModel.searchByFieldFilter(item.id)
            onFilterSearchApplied()
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
