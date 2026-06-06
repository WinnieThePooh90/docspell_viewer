package paulokat.de.docspellviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import paulokat.de.docspellviewer.R
import paulokat.de.docspellviewer.SidebarFilterId
import paulokat.de.docspellviewer.SidebarFilterVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSidebarContent(
    visibility: SidebarFilterVisibility,
    onClose: () -> Unit,
    onFilterClick: (SidebarFilterId) -> Unit
) {
    val visibleFilters = visibility.orderedEnabledFilters()

    ModalDrawerSheet(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(),
        drawerContainerColor = FilterSidebarColors.background
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 4.dp, top = 20.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.filter_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = FilterSidebarColors.text
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_close_sidebar),
                    tint = FilterSidebarColors.icon
                )
            }
        }

        HorizontalDivider(color = FilterSidebarColors.divider)

        if (visibleFilters.isEmpty()) {
            Text(
                text = stringResource(R.string.filter_none_active),
                style = MaterialTheme.typography.bodyMedium,
                color = FilterSidebarColors.textMuted,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(FilterSidebarColors.listBackground)
            ) {
                itemsIndexed(
                    items = visibleFilters,
                    key = { _, filter -> filter.name }
                ) { index, filter ->
                    FilterSidebarItem(
                        icon = DocspellFilterIcons.icon(filter),
                        title = stringResource(filter.labelRes),
                        onClick = { onFilterClick(filter) }
                    )
                    if (index < visibleFilters.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = FilterSidebarColors.divider
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun FilterSidebarItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FilterSidebarColors.icon,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = FilterSidebarColors.text
        )
    }
}
