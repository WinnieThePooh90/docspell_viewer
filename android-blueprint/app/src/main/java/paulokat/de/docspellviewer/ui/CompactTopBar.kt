package paulokat.de.docspellviewer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import paulokat.de.docspellviewer.AppInfo

val CompactTopBarHeight = 48.dp
internal val CompactTopBarIconButtonSize = 40.dp
internal val CompactTopBarIconSize = 22.dp

@Composable
fun CompactBackTopBar(
    title: String,
    onBack: () -> Unit,
    backContentDescription: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(CompactTopBarHeight)
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(CompactTopBarIconButtonSize)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = backContentDescription,
                    modifier = Modifier.size(CompactTopBarIconSize)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(lineHeight = 18.sp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            actions()
        }
    }
}

@Composable
fun CompactMainTopBar(
    activeAccountLabel: String?,
    isLoading: Boolean,
    onMenuClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onSync: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(CompactTopBarHeight)
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(CompactTopBarIconButtonSize)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = "Filter",
                    modifier = Modifier.size(CompactTopBarIconSize)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = AppInfo.NAME,
                    style = MaterialTheme.typography.titleSmall.copy(lineHeight = 18.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                activeAccountLabel?.takeIf { it.isNotBlank() }?.let { accountLabel ->
                    Text(
                        text = accountLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            lineHeight = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                    )
                }
            }

            IconButton(
                onClick = onSync,
                enabled = !isLoading,
                modifier = Modifier.size(CompactTopBarIconButtonSize)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Verbindung erneuern",
                    modifier = Modifier.size(CompactTopBarIconSize)
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(CompactTopBarIconButtonSize)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Einstellungen",
                    modifier = Modifier.size(CompactTopBarIconSize)
                )
            }
        }
    }
}
