package paulokat.de.docspellviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import paulokat.de.docspellviewer.ThumbnailColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import paulokat.de.docspellviewer.R
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import coil.request.ImageRequest

private val OfflineBookmarkGreen = Color(0xFF4CAF50)
private val FavoriteStarAmber = Color(0xFFFFC107)
private val AttachmentCountBlue = Color(0xFF1565C0)

@Composable
fun DocumentThumbnail(
    previewUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    width: Dp = 48.dp,
    height: Dp = 64.dp,
    showOfflineFlag: Boolean = false,
    showFavoriteFlag: Boolean = false,
    attachmentCount: Int = 0,
    isOpeningDocument: Boolean = false,
    openProgressPercent: Int = 0
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(previewUrl.takeIf { it.isNotBlank() })
            .crossfade(true)
            .diskCacheKey(previewUrl)
            .memoryCacheKey(previewUrl)
            .build()
    )
    val painterState = painter.state
    val isPreviewLoading = previewUrl.isNotBlank() &&
        painterState is AsyncImagePainter.State.Loading
    val thumbnailBackground = ThumbnailColors.background(MaterialTheme.colorScheme)

    Box(
        modifier = modifier.size(width = width, height = height),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = width, height = height)
                .background(thumbnailBackground),
            contentAlignment = Alignment.Center
        ) {
            if (previewUrl.isNotBlank()) {
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(width = width, height = height),
                    contentScale = ContentScale.Crop
                )
                if (isPreviewLoading && !isOpeningDocument) {
                    PreviewLoadingOverlay(
                        width = width,
                        height = height,
                        backgroundColor = thumbnailBackground
                    )
                }
            }
        }
        if (showOfflineFlag && !isOpeningDocument) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = stringResource(R.string.cd_offline),
                tint = OfflineBookmarkGreen,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(2.dp)
                    .size(16.dp)
            )
        }
        if (showFavoriteFlag && !isOpeningDocument) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = stringResource(R.string.cd_favorite),
                tint = FavoriteStarAmber,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(16.dp)
            )
        }
        if (attachmentCount >= 2 && !isOpeningDocument) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
                    .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 3.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = attachmentCount.toString(),
                    color = AttachmentCountBlue,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        lineHeight = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }
        if (isOpeningDocument) {
            ThumbnailProgressOverlay(
                progressPercent = openProgressPercent,
                statusText = documentLoadingText(),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PreviewLoadingOverlay(
    width: Dp,
    height: Dp,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .background(backgroundColor.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(if (height >= 80.dp) 28.dp else 20.dp),
            strokeWidth = 2.dp,
            color = Color(0xFF757575),
            trackColor = Color(0xFFBDBDBD)
        )
    }
}

@Composable
private fun ThumbnailProgressOverlay(
    progressPercent: Int,
    statusText: String,
    modifier: Modifier = Modifier
) {
    val percent = progressPercent.coerceIn(0, 100)
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .widthIn(max = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp,
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
            Text(
                text = "$percent %",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
            Text(
                text = statusText,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 6.dp),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
