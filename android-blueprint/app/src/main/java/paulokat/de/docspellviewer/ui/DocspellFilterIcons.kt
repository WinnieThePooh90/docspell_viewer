package paulokat.de.docspellviewer.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import paulokat.de.docspellviewer.SidebarFilterId

/**
 * Icons aligned with Docspell web UI ([Data/Icons.elm], Font Awesome classes noted per filter).
 */
object DocspellFilterIcons {
    fun icon(filter: SidebarFilterId): ImageVector = when (filter) {
        SidebarFilterId.TAGS -> Icons.Filled.LocalOffer // fa-tags
        SidebarFilterId.CORRESPONDENT -> Icons.Filled.Factory // fa-industry (organization)
        SidebarFilterId.CATEGORY -> Icons.Filled.Category
        SidebarFilterId.CUSTOM_FIELDS -> Icons.Filled.DashboardCustomize
        SidebarFilterId.DOCUMENT_DATE -> Icons.Filled.CalendarToday // fa-calendar
        SidebarFilterId.CREATED -> Icons.Filled.Schedule
        SidebarFilterId.SOURCE -> Icons.Filled.FileUpload // fa-upload
        SidebarFilterId.FOLDER -> Icons.Filled.Folder // fa-folder
        SidebarFilterId.DIRECTION -> Icons.Filled.SwapHoriz // fa-exchange-alt
        SidebarFilterId.DUE_DATE -> Icons.Filled.Notifications // fa-bell
        SidebarFilterId.CONCERNED_PERSON -> Icons.Filled.Person // fa-user
        SidebarFilterId.CONCERNED_EQUIPMENT -> Icons.Filled.Inventory2 // fa-box
    }
}
