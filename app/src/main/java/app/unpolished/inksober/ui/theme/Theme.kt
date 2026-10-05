package app.unpolished.inksober.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EInkLightColorScheme = lightColorScheme(
    primary = EInkBlack,
    onPrimary = EInkWhite,
    primaryContainer = EInkLightGray,
    onPrimaryContainer = EInkBlack,
    secondary = EInkDarkGray,
    onSecondary = EInkWhite,
    secondaryContainer = EInkLightGray,
    onSecondaryContainer = EInkBlack,
    tertiary = EInkBlack,
    onTertiary = EInkWhite,
    tertiaryContainer = EInkLightGray,
    onTertiaryContainer = EInkBlack,
    background = EInkWhite,
    onBackground = EInkBlack,
    surface = EInkWhite,
    onSurface = EInkBlack,
    surfaceVariant = EInkLightGray,
    onSurfaceVariant = EInkBlack,
    outline = EInkBlack,
    outlineVariant = EInkDarkGray,
    inverseSurface = EInkBlack,
    inverseOnSurface = EInkWhite,
    inversePrimary = EInkWhite
)

private val EInkDarkColorScheme = darkColorScheme(
    primary = EInkWhite,
    onPrimary = EInkBlack,
    primaryContainer = EInkDarkGray,
    onPrimaryContainer = EInkWhite,
    secondary = EInkLightGray,
    onSecondary = EInkBlack,
    secondaryContainer = EInkDarkGray,
    onSecondaryContainer = EInkWhite,
    tertiary = EInkWhite,
    onTertiary = EInkBlack,
    tertiaryContainer = EInkDarkGray,
    onTertiaryContainer = EInkWhite,
    background = EInkBlack,
    onBackground = EInkWhite,
    surface = EInkBlack,
    onSurface = EInkWhite,
    surfaceVariant = EInkDarkGray,
    onSurfaceVariant = EInkWhite,
    outline = EInkWhite,
    outlineVariant = EInkLightGray,
    inverseSurface = EInkWhite,
    inverseOnSurface = EInkBlack,
    inversePrimary = EInkBlack
)

@Composable
fun InkSoberTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EInkDarkColorScheme else EInkLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
