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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import paulokat.de.docspellviewer.DocumentRow
import paulokat.de.docspellviewer.R
import paulokat.de.docspellviewer.OfflineListUiState
import paulokat.de.docspellviewer.PdfViewerUiState

@Composable
fun OfflineDocumentsContent(
    state: OfflineListUiState,
    pdfViewerState: PdfViewerUiState,
    imageReloadGeneration: Int,
    onOpenDocument: (DocumentRow) -> Unit,
    onOpenDetail: (DocumentRow) -> Unit
) {
    val tableTextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value - 2f).sp
    )
    val tableHeaderStyle = MaterialTheme.typography.titleSmall.copy(
        fontSize = (MaterialTheme.typography.titleSmall.fontSize.value - 2f).sp
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.size(width = 48.dp, height = 1.dp))
            Text(
                text = stringResource(R.string.home_column_document),
                modifier = Modifier.weight(0.6f),
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

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (state.documents.isEmpty()) {
                Text(
                    text = stringResource(R.string.offline_empty),
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
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
                                    showOfflineFlag = true,
                                    showFavoriteFlag = doc.isFavorite,
                                    isOpeningDocument = pdfViewerState.isOpeningDocument(doc.id),
                                    openProgressPercent = pdfViewerState.openingProgressFor(doc.id),
                                    modifier = Modifier.clickable(
                                        enabled = !pdfViewerState.isOpeningDocument(doc.id)
                                    ) { onOpenDocument(doc) }
                                )
                                Text(
                                    text = doc.name,
                                    modifier = Modifier
                                        .weight(0.6f)
                                        .clickable { onOpenDetail(doc) },
                                    style = tableTextStyle,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = contentTextColor()
                                )
                                Text(
                                    text = doc.correspondent,
                                    modifier = Modifier.weight(0.4f),
                                    style = tableTextStyle,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
