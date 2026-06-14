package paulokat.de.docspellviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import paulokat.de.docspellviewer.DocumentListSort
import paulokat.de.docspellviewer.DocumentRow
import paulokat.de.docspellviewer.R
import paulokat.de.docspellviewer.HomeUiState
import paulokat.de.docspellviewer.PdfViewerUiState
import paulokat.de.docspellviewer.labelRes

@Composable
fun HomeScreenContent(
    state: HomeUiState,
    pdfViewerState: PdfViewerUiState,
    imageReloadGeneration: Int,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenDocument: (DocumentRow) -> Unit,
    onOpenDetail: (DocumentRow) -> Unit,
    onLoadMore: () -> Unit,
    onCorrespondentClick: (DocumentRow) -> Unit,
    onDocumentListSortChange: (DocumentListSort) -> Unit
) {
    val context = LocalContext.current
    val hitsSummary = state.hitsSummary(context)
    val tableTextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value - 2f).sp
    )
    val tableHeaderStyle = MaterialTheme.typography.titleSmall.copy(
        fontSize = (MaterialTheme.typography.titleSmall.fontSize.value - 2f).sp
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.home_search_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            trailingIcon = {
                IconButton(
                    onClick = onSearch,
                    enabled = !state.isLoading,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.cd_search),
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )

        if (state.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        val statusText = state.statusResId?.let { stringResource(it) }
            ?: state.statusQuery.orEmpty()
        if ((statusText.isNotBlank() || hitsSummary != null) && !state.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                hitsSummary?.let { summary ->
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DocumentListSortHeaderButton(
                currentSort = state.documentListSort,
                onSortChange = onDocumentListSortChange,
                modifier = Modifier.size(width = 48.dp, height = 24.dp)
            )
            Text(
                text = stringResource(R.string.home_column_document),
                modifier = Modifier.weight(0.5f),
                style = tableHeaderStyle,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.home_column_correspondent),
                modifier = Modifier.weight(0.4f),
                style = tableHeaderStyle,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()

        val listState = rememberLazyListState()
        LaunchedEffect(state.documentListSort) {
            if (state.documents.isNotEmpty()) {
                listState.scrollToItem(0)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (state.documents.isEmpty() && !state.isLoading) {
                Text(
                    text = stringResource(R.string.home_no_documents),
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(state.documents, key = { it.id }) { doc ->
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DocumentThumbnail(
                                    previewUrl = doc.previewUrl,
                                    contentDescription = stringResource(R.string.cd_preview, doc.name),
                                    imageReloadGeneration = imageReloadGeneration,
                                    showOfflineFlag = doc.isOfflineAvailable,
                                    showFavoriteFlag = doc.isFavorite,
                                    attachmentCount = doc.attachmentCount,
                                    isOpeningDocument = pdfViewerState.isOpeningDocument(doc.id),
                                    openProgressPercent = pdfViewerState.openingProgressFor(doc.id),
                                    modifier = Modifier.clickable(
                                        enabled = !pdfViewerState.isOpeningDocument(doc.id)
                                    ) { onOpenDocument(doc) }
                                )
                                Text(
                                    text = doc.name,
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .clickable { onOpenDetail(doc) },
                                    style = tableTextStyle,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = contentTextColor()
                                )
                                val canFilterCorrespondent = !doc.corrOrgName.isNullOrBlank() ||
                                    !doc.corrPersonName.isNullOrBlank()
                                Text(
                                    text = doc.correspondent,
                                    modifier = Modifier
                                        .weight(0.4f)
                                        .then(
                                            if (canFilterCorrespondent) {
                                                Modifier.clickable { onCorrespondentClick(doc) }
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    style = tableTextStyle,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (canFilterCorrespondent) {
                                        contentTextColor()
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                    if (state.hasMoreResults && state.documents.isNotEmpty()) {
                        item(key = "load_more") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onLoadMore,
                                    enabled = !state.isLoadingMore,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.home_load_more))
                                }
                                if (state.isLoadingMore) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentListSortHeaderButton(
    currentSort: DocumentListSort,
    onSortChange: (DocumentListSort) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = stringResource(R.string.cd_sort_documents),
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DocumentListSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(sort.labelRes()),
                            fontWeight = if (sort == currentSort) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onSortChange(sort)
                    }
                )
            }
        }
    }
}
