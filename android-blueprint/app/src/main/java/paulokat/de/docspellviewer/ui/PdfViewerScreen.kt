package paulokat.de.docspellviewer.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import paulokat.de.docspellviewer.DocumentLoadProgress
import paulokat.de.docspellviewer.PdfViewerUiState
import paulokat.de.docspellviewer.R
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    state: PdfViewerUiState,
    onRenderProgress: (Int) -> Unit,
    onRenderComplete: () -> Unit,
    onBack: () -> Unit
) {
    val loadingText = documentLoadingText()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.title.ifBlank { stringResource(R.string.detail_default_title) },
                        maxLines = 1
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.error != null -> {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                state.pdfPath != null -> {
                    val progress = state.transferProgressPercent ?: 0
                    Box(modifier = Modifier.fillMaxSize()) {
                        PdfPages(
                            filePath = state.pdfPath,
                            renderBaseProgress = remember(state.pdfPath) {
                                progress.coerceAtLeast(85)
                            },
                            onRenderProgress = onRenderProgress,
                            onRenderComplete = onRenderComplete
                        )
                        if (progress < 100) {
                            key(progress) {
                                TransferProgressIndicator(
                                    progressPercent = progress,
                                    statusText = loadingText,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
                state.isLoading -> {
                    TransferProgressIndicator(
                        progressPercent = state.transferProgressPercent,
                        statusText = loadingText,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfPages(
    filePath: String,
    renderBaseProgress: Int,
    onRenderProgress: (Int) -> Unit,
    onRenderComplete: () -> Unit
) {
    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    var pages by remember(filePath) { mutableStateOf<List<Bitmap>>(emptyList()) }
    var renderError by remember(filePath) { mutableStateOf<String?>(null) }

    LaunchedEffect(filePath, screenWidthDp) {
        renderError = null
        pages = emptyList()
        val result = withContext(Dispatchers.IO) {
            runCatching {
                renderPdfPages(
                    file = File(filePath),
                    screenWidthDp = screenWidthDp,
                    downloadProgress = renderBaseProgress,
                    onProgress = onRenderProgress
                )
            }
        }
        result
            .onSuccess { rendered ->
                pages = rendered
                onRenderComplete()
            }
            .onFailure {
                renderError = it.message ?: context.getString(R.string.pdf_render_failed)
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    when {
        renderError != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = renderError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        else -> {
            ZoomablePdfContent {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(pages, key = { index, _ -> "$filePath-$index" }) { index, bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.cd_page, index + 1),
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
    }
}

private const val MIN_PDF_ZOOM = 1f
private const val MAX_PDF_ZOOM = 4f

@Composable
private fun ZoomablePdfContent(content: @Composable () -> Unit) {
    var scale by remember { mutableFloatStateOf(MIN_PDF_ZOOM) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(MIN_PDF_ZOOM, MAX_PDF_ZOOM)
        if (scale > MIN_PDF_ZOOM) {
            offsetX += panChange.x
            offsetY += panChange.y
        } else {
            offsetX = 0f
            offsetY = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .transformable(state = transformableState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
        ) {
            content()
        }
    }
}

private fun renderPdfPages(
    file: File,
    screenWidthDp: Int,
    downloadProgress: Int,
    onProgress: (Int) -> Unit
): List<Bitmap> {
    val targetWidthPx = (screenWidthDp * 2).coerceAtLeast(720)
    val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(descriptor)
    val bitmaps = mutableListOf<Bitmap>()
    try {
        val pageCount = renderer.pageCount
        for (pageIndex in 0 until pageCount) {
            renderer.openPage(pageIndex).use { page ->
                val scale = (targetWidthPx.toFloat() / page.width).coerceIn(1f, 3f)
                val width = (page.width * scale).toInt().coerceAtLeast(1)
                val height = (page.height * scale).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
            }
            onProgress(
                DocumentLoadProgress.renderPercent(downloadProgress, pageIndex, pageCount)
            )
        }
    } finally {
        renderer.close()
        descriptor.close()
    }
    return bitmaps
}
