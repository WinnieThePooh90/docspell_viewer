package paulokat.de.docspellviewer.ui

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Färbt Status- und Navigationsleiste passend zum Material-Farbschema
 * (sichtbarer Bereich außerhalb von [safeDrawingPadding]).
 */
@Composable
fun SyncSystemBarColors(
    statusBarColor: Color = MaterialTheme.colorScheme.primaryContainer,
    navigationBarColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    windowBackgroundColor: Color = MaterialTheme.colorScheme.background
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.decorView.setBackgroundColor(windowBackgroundColor.toArgb())
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = statusBarColor.luminance() > 0.5f
            controller.isAppearanceLightNavigationBars = navigationBarColor.luminance() > 0.5f
        }
    }
}
