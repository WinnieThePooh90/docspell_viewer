package paulokat.de.docspellviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FeedbackBanner(message: String?, isError: Boolean) {
    if (message.isNullOrBlank()) {
        return
    }
    val backgroundColor = if (isError) {
        Color(0xFFFFEBEE)
    } else {
        Color(0xFFE8F5E9)
    }
    val textColor = if (isError) {
        Color(0xFFC62828)
    } else {
        Color(0xFF2E7D32)
    }
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = textColor
    )
}
