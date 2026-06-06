package paulokat.de.docspellviewer

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AppColorScheme(@StringRes val labelRes: Int) {
    GRAYSCALE(R.string.color_scheme_grayscale),
    VIOLET(R.string.color_scheme_violet),
    BLUE(R.string.color_scheme_blue),
    GREEN(R.string.color_scheme_green);

    companion object {
        fun fromName(value: String?): AppColorScheme {
            return entries.firstOrNull { it.name == value } ?: GRAYSCALE
        }

        fun migrateFromLegacyTheme(themeName: String?): AppColorScheme {
            return when (themeName) {
                LegacyThemeMode.DARK.name -> GRAYSCALE
                LegacyThemeMode.STANDARD.name -> VIOLET
                else -> GRAYSCALE
            }
        }

        fun legacyUsedDarkTheme(themeName: String?): Boolean {
            return themeName == LegacyThemeMode.DARK.name
        }
    }
}

/** @deprecated Nur für Migration aus älteren App-Versionen. */
private enum class LegacyThemeMode {
    STANDARD,
    DARK
}

fun AppColorScheme.resolveColorScheme(useDarkTheme: Boolean): ColorScheme {
    return if (useDarkTheme) {
        toDarkColorScheme()
    } else {
        toLightColorScheme()
    }
}

/**
 * Nur Akzentfarben (Header, Footer, Buttons) sind schema-abhängig.
 * Flächen, Text und Thumbnails nutzen überall neutrale Grautöne.
 */
private data class AccentPalette(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color
)

private fun AppColorScheme.lightAccent(): AccentPalette {
    return when (this) {
        AppColorScheme.GRAYSCALE -> AccentPalette(
            primary = Color(0xFF424242),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE0E0E0),
            onPrimaryContainer = Color(0xFF212121),
            surfaceContainer = Color(0xFFEEEEEE),
            surfaceContainerHigh = Color(0xFFE0E0E0)
        )
        AppColorScheme.VIOLET -> AccentPalette(
            primary = Color(0xFF6750A4),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEADDFF),
            onPrimaryContainer = Color(0xFF21005D),
            surfaceContainer = Color(0xFFF3EDF7),
            surfaceContainerHigh = Color(0xFFEADDFF)
        )
        AppColorScheme.BLUE -> AccentPalette(
            primary = Color(0xFF1565C0),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD1E4FF),
            onPrimaryContainer = Color(0xFF001D36),
            surfaceContainer = Color(0xFFE8F1FC),
            surfaceContainerHigh = Color(0xFFD1E4FF)
        )
        AppColorScheme.GREEN -> AccentPalette(
            primary = Color(0xFF2E7D32),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFC8E6C9),
            onPrimaryContainer = Color(0xFF1B5E20),
            surfaceContainer = Color(0xFFE8F5E9),
            surfaceContainerHigh = Color(0xFFC8E6C9)
        )
    }
}

private fun AppColorScheme.darkAccent(): AccentPalette {
    return when (this) {
        AppColorScheme.GRAYSCALE -> AccentPalette(
            primary = Color(0xFFBDBDBD),
            onPrimary = Color(0xFF212121),
            primaryContainer = Color(0xFF424242),
            onPrimaryContainer = Color(0xFFE0E0E0),
            surfaceContainer = Color(0xFF2A2A2A),
            surfaceContainerHigh = Color(0xFF424242)
        )
        AppColorScheme.VIOLET -> AccentPalette(
            primary = Color(0xFFD0BCFF),
            onPrimary = Color(0xFF381E72),
            primaryContainer = Color(0xFF4F378B),
            onPrimaryContainer = Color(0xFFEADDFF),
            surfaceContainer = Color(0xFF2D2640),
            surfaceContainerHigh = Color(0xFF4F378B)
        )
        AppColorScheme.BLUE -> AccentPalette(
            primary = Color(0xFF90CAF9),
            onPrimary = Color(0xFF003258),
            primaryContainer = Color(0xFF004881),
            onPrimaryContainer = Color(0xFFD1E4FF),
            surfaceContainer = Color(0xFF1A2A3A),
            surfaceContainerHigh = Color(0xFF004881)
        )
        AppColorScheme.GREEN -> AccentPalette(
            primary = Color(0xFFA5D6A7),
            onPrimary = Color(0xFF1B5E20),
            primaryContainer = Color(0xFF2E7D32),
            onPrimaryContainer = Color(0xFFC8E6C9),
            surfaceContainer = Color(0xFF1E2E1F),
            surfaceContainerHigh = Color(0xFF2E7D32)
        )
    }
}

private fun AppColorScheme.toLightColorScheme(): ColorScheme {
    val accent = lightAccent()
    return lightColorScheme(
        primary = accent.primary,
        onPrimary = accent.onPrimary,
        primaryContainer = accent.primaryContainer,
        onPrimaryContainer = accent.onPrimaryContainer,
        secondary = Color(0xFF616161),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFEEEEEE),
        onSecondaryContainer = Color(0xFF212121),
        tertiary = Color(0xFF757575),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF5F5F5),
        onTertiaryContainer = Color(0xFF212121),
        background = Color(0xFFFAFAFA),
        onBackground = Color(0xFF212121),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF212121),
        surfaceVariant = Color(0xFFE8E8E8),
        onSurfaceVariant = Color(0xFF616161),
        surfaceContainer = accent.surfaceContainer,
        surfaceContainerHigh = accent.surfaceContainerHigh,
        outline = Color(0xFF9E9E9E),
        outlineVariant = Color(0xFFBDBDBD)
    )
}

private fun AppColorScheme.toDarkColorScheme(): ColorScheme {
    val accent = darkAccent()
    return darkColorScheme(
        primary = accent.primary,
        onPrimary = accent.onPrimary,
        primaryContainer = accent.primaryContainer,
        onPrimaryContainer = accent.onPrimaryContainer,
        secondary = Color(0xFF9E9E9E),
        onSecondary = Color(0xFF212121),
        secondaryContainer = Color(0xFF616161),
        onSecondaryContainer = Color(0xFFE0E0E0),
        tertiary = Color(0xFF757575),
        onTertiary = Color(0xFF212121),
        tertiaryContainer = Color(0xFF424242),
        onTertiaryContainer = Color(0xFFE0E0E0),
        background = Color(0xFF121212),
        onBackground = Color(0xFFE0E0E0),
        surface = Color(0xFF1E1E1E),
        onSurface = Color(0xFFE0E0E0),
        surfaceVariant = Color(0xFF2C2C2C),
        onSurfaceVariant = Color(0xFFBDBDBD),
        surfaceContainer = accent.surfaceContainer,
        surfaceContainerHigh = accent.surfaceContainerHigh,
        outline = Color(0xFF757575),
        outlineVariant = Color(0xFF424242)
    )
}

/** Neutraler Thumbnail-Hintergrund (unabhängig vom Akzent-Schema). */
object ThumbnailColors {
    val backgroundLight = Color(0xFFE8E8E8)
    val backgroundDark = Color(0xFF2C2C2C)

    fun background(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) backgroundDark else backgroundLight
    }

    fun background(colorScheme: ColorScheme): Color {
        val bg = colorScheme.background
        val relativeLuminance = 0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue
        return background(relativeLuminance < 0.5f)
    }
}
