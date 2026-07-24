package me.nancex.logophile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import me.nancex.logophile.R

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary, onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer, onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary, onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer, onSecondaryContainer = LightOnSecondaryContainer,
    background = LightBackground, onBackground = LightOnBackground,
    surface = LightSurface, onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant, onSurfaceVariant = LightOnSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary, onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer, onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary, onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer, onSecondaryContainer = DarkOnSecondaryContainer,
    background = DarkBackground, onBackground = DarkOnBackground,
    surface = DarkSurface, onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant, onSurfaceVariant = DarkOnSurfaceVariant
)

private val OceanColorScheme = lightColorScheme(
    primary = OceanPrimary, onPrimary = OceanOnPrimary,
    primaryContainer = OceanPrimaryContainer, onPrimaryContainer = OceanOnPrimaryContainer,
    secondary = OceanSecondary, onSecondary = OceanOnSecondary,
    secondaryContainer = OceanSecondaryContainer, onSecondaryContainer = OceanOnSecondaryContainer,
    background = OceanBackground, onBackground = OceanOnBackground,
    surface = OceanSurface, onSurface = OceanOnSurface
)

private val RoseColorScheme = lightColorScheme(
    primary = RosePrimary, onPrimary = RoseOnPrimary,
    primaryContainer = RosePrimaryContainer, onPrimaryContainer = RoseOnPrimaryContainer,
    secondary = RoseSecondary, onSecondary = RoseOnSecondary,
    secondaryContainer = RoseSecondaryContainer, onSecondaryContainer = RoseOnSecondaryContainer,
    background = RoseBackground, onBackground = RoseOnBackground,
    surface = RoseSurface, onSurface = RoseOnSurface
)

private val ForestColorScheme = lightColorScheme(
    primary = ForestPrimary, onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer, onPrimaryContainer = ForestOnPrimaryContainer,
    secondary = ForestSecondary, onSecondary = ForestOnSecondary,
    secondaryContainer = ForestSecondaryContainer, onSecondaryContainer = ForestOnSecondaryContainer,
    background = ForestBackground, onBackground = ForestOnBackground,
    surface = ForestSurface, onSurface = ForestOnSurface
)

private val EightBitFont = FontFamily(Font(R.font.eightbitoperator))

@Composable
fun LogophileTheme(
    theme: AppTheme = AppTheme.LIGHT,
    font: AppFont = AppFont.DEFAULT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.OCEAN -> OceanColorScheme
        AppTheme.ROSE -> RoseColorScheme
        AppTheme.FOREST -> ForestColorScheme
    }
    val wordFont = getWordFontFamily(font)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography.copy(
            displayLarge = AppTypography.displayLarge.copy(fontFamily = wordFont),
            bodyLarge = AppTypography.bodyLarge.copy(fontFamily = FontFamily.Default),
            bodyMedium = AppTypography.bodyMedium.copy(fontFamily = FontFamily.Default)
        ),
        content = content
    )
}

fun getWordFontFamily(font: AppFont): FontFamily = when (font) {
    AppFont.DEFAULT -> FontFamily.Default
    AppFont.SERIF -> FontFamily.Serif
    AppFont.MONOSPACE -> FontFamily.Monospace
    AppFont.EIGHT_BIT -> EightBitFont
}

fun getWordFontSizeMultiplier(font: AppFont): Float = when (font) {
    AppFont.EIGHT_BIT -> 1.6f
    else -> 1.0f
}
