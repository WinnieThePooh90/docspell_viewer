package paulokat.de.docspellviewer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import paulokat.de.docspellviewer.AudioPlaybackUiState
import paulokat.de.docspellviewer.R
import paulokat.de.docspellviewer.DetailAttachmentRow
import paulokat.de.docspellviewer.DetailFieldId
import paulokat.de.docspellviewer.DetailFieldVisibility
import paulokat.de.docspellviewer.DocumentDetailContent
import paulokat.de.docspellviewer.DocumentDetailUiState
import paulokat.de.docspellviewer.PdfViewerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    state: DocumentDetailUiState,
    pdfViewerState: PdfViewerUiState,
    onBack: () -> Unit,
    onOpenDocument: () -> Unit,
    onAttachmentClick: (DetailAttachmentRow) -> Unit,
    onAudioPlayClick: (DetailAttachmentRow) -> Unit,
    onAudioPlayPause: () -> Unit,
    onAudioSeek: (Float) -> Unit,
    onAudioSkipBackward: () -> Unit,
    onAudioSkipForward: () -> Unit,
    onMakeOffline: () -> Unit,
    onDeleteOffline: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(
                            text = state.title.ifBlank { stringResource(R.string.detail_default_title) },
                            maxLines = 2
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                FeedbackBanner(
                    message = state.feedbackMessageRes?.let { stringResource(it) },
                    isError = state.feedbackMessageIsError
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    TransferProgressIndicator(
                        progressPercent = null,
                        statusText = stringResource(R.string.detail_loading),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    val content = state.content
                    val visibility = state.detailFieldVisibility
                    val hasMetadata = content.hasVisibleMetadata(visibility)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val viewerDocId = state.viewerDocument?.id
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            DocumentThumbnail(
                                previewUrl = state.previewUrl,
                                contentDescription = stringResource(R.string.cd_preview, state.title),
                                width = 140.dp,
                                height = 186.dp,
                                showOfflineFlag = state.isOfflineAvailable,
                                isOpeningDocument = viewerDocId?.let {
                                    pdfViewerState.isOpeningDocument(it)
                                } ?: false,
                                openProgressPercent = viewerDocId?.let {
                                    pdfViewerState.openingProgressFor(it)
                                } ?: 0,
                                modifier = Modifier.then(
                                    if (state.viewerDocument != null) {
                                        Modifier.clickable(
                                            enabled = viewerDocId == null ||
                                                !pdfViewerState.isOpeningDocument(viewerDocId),
                                            onClick = onOpenDocument
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                            )
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = if (state.isFavorite) {
                                        Icons.Filled.Star
                                    } else {
                                        Icons.Outlined.StarOutline
                                    },
                                    contentDescription = if (state.isFavorite) {
                                        stringResource(R.string.cd_remove_favorite)
                                    } else {
                                        stringResource(R.string.cd_add_favorite)
                                    },
                                    tint = if (state.isFavorite) {
                                        Color(0xFFFFC107)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }

                        if (!hasMetadata) {
                            Text(
                                text = stringResource(R.string.detail_no_metadata),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            DetailMetadataSection(
                                content = content,
                                visibility = visibility,
                                audioPlayback = state.audioPlayback,
                                onAttachmentClick = onAttachmentClick,
                                onAudioPlayClick = onAudioPlayClick
                            )
                        }

                        if (state.isOfflineWorking) {
                            TransferProgressIndicator(
                                progressPercent = state.transferProgressPercent,
                                statusText = stringResource(R.string.detail_saving_offline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterHorizontally)
                            )
                        } else if (state.viewerDocument != null || state.document?.attachmentId != null) {
                            if (state.isOfflineAvailable) {
                                OutlinedButton(
                                    onClick = onDeleteOffline,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.detail_delete_offline))
                                }
                            } else {
                                Button(
                                    onClick = onMakeOffline,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.detail_make_offline))
                                }
                            }
                        }

                        if (state.audioPlayback.isPlayerOpen) {
                            AudioMiniPlayer(
                                state = state.audioPlayback,
                                onPlayPause = onAudioPlayPause,
                                onSeek = onAudioSeek,
                                onSkipBackward = onAudioSkipBackward,
                                onSkipForward = onAudioSkipForward
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMetadataSection(
    content: DocumentDetailContent,
    visibility: DetailFieldVisibility,
    audioPlayback: AudioPlaybackUiState,
    onAttachmentClick: (DetailAttachmentRow) -> Unit,
    onAudioPlayClick: (DetailAttachmentRow) -> Unit
) {
    visibility.orderedEnabledFields().forEach { field ->
        when (field) {
            DetailFieldId.CUSTOM_FIELDS -> {
                content.customFields.forEach { customField ->
                    DetailField(label = customField.label, value = customField.value)
                }
            }
            DetailFieldId.ATTACHMENTS -> {
                if (content.attachments.isNotEmpty()) {
                    AttachmentsSection(
                        attachments = content.attachments,
                        audioPlayback = audioPlayback,
                        onAttachmentClick = onAttachmentClick,
                        onAudioPlayClick = onAudioPlayClick
                    )
                }
            }
            else -> showStandardField(visibility, field, standardFieldValue(content, field))
        }
    }
}

private fun standardFieldValue(content: DocumentDetailContent, field: DetailFieldId): String? {
    return when (field) {
        DetailFieldId.DOCUMENT_DATE -> content.documentDate
        DetailFieldId.CREATED -> content.created
        DetailFieldId.UPDATED -> content.updated
        DetailFieldId.DUE_DATE -> content.dueDate
        DetailFieldId.SOURCE -> content.source
        DetailFieldId.FOLDER -> content.folder
        DetailFieldId.DIRECTION -> content.direction
        DetailFieldId.STATE -> content.state
        DetailFieldId.CORRESPONDENT -> content.correspondent
        DetailFieldId.CONCERNED_PERSON -> content.concernedPerson
        DetailFieldId.CONCERNED_EQUIPMENT -> content.concernedEquipment
        DetailFieldId.NOTES -> content.notes
        DetailFieldId.TAGS -> content.tags
        DetailFieldId.CUSTOM_FIELDS, DetailFieldId.ATTACHMENTS -> null
    }
}

@Composable
private fun showStandardField(
    visibility: DetailFieldVisibility,
    field: DetailFieldId,
    value: String?
) {
    if (visibility.isEnabled(field) && !value.isNullOrBlank()) {
        DetailField(label = stringResource(field.labelRes), value = value)
    }
}

@Composable
private fun AttachmentsSection(
    attachments: List<DetailAttachmentRow>,
    audioPlayback: AudioPlaybackUiState,
    onAttachmentClick: (DetailAttachmentRow) -> Unit,
    onAudioPlayClick: (DetailAttachmentRow) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_attachments),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            attachments.forEach { attachment ->
                AttachmentRow(
                    attachment = attachment,
                    audioPlayback = audioPlayback,
                    onAttachmentClick = onAttachmentClick,
                    onAudioPlayClick = onAudioPlayClick
                )
            }
        }
    }
}

@Composable
private fun AttachmentRow(
    attachment: DetailAttachmentRow,
    audioPlayback: AudioPlaybackUiState,
    onAttachmentClick: (DetailAttachmentRow) -> Unit,
    onAudioPlayClick: (DetailAttachmentRow) -> Unit
) {
    val isActive = audioPlayback.attachmentId == attachment.id
    val isLoading = isActive && audioPlayback.isLoading

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (attachment.isAudio) {
            IconButton(
                onClick = { onAudioPlayClick(attachment) },
                enabled = !isLoading,
                modifier = Modifier.size(40.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.cd_play_audio)
                    )
                }
            }
        }
        Text(
            text = attachment.name,
            style = MaterialTheme.typography.bodyLarge,
            color = contentTextColor(),
            modifier = Modifier
                .weight(1f)
                .clickable { onAttachmentClick(attachment) }
        )
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
