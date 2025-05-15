package ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark mode colors
private val DarkPrimary = Color(0xFF6C63FF)
private val DarkPrimaryVariant = Color(0xFF4A42D1)
private val DarkSecondary = Color(0xFF03DAC6)
private val DarkSecondaryVariant = Color(0xFF018786)
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkError = Color(0xFFCF6679)
private val DarkOnPrimary = Color.White
private val DarkOnSecondary = Color.Black
private val DarkOnBackground = Color.White
private val DarkOnSurface = Color.White
private val DarkOnError = Color.Black

// Light mode colors
private val LightPrimary = Color(0xFF6C63FF)
private val LightPrimaryVariant = Color(0xFF4A42D1)
private val LightSecondary = Color(0xFF03DAC6)
private val LightSecondaryVariant = Color(0xFF018786)
private val LightBackground = Color(0xFFF5F5F5)
private val LightSurface = Color.White
private val LightError = Color(0xFFB00020)
private val LightOnPrimary = Color.White
private val LightOnSecondary = Color.Black
private val LightOnBackground = Color.Black
private val LightOnSurface = Color.Black
private val LightOnError = Color.White

// Additional colors
val CardBackground = Color(0xFF2A2A2A)
val CardSurface = Color(0xFF333333)
val DividerColor = Color(0xFF3D3D3D)
val TextSecondary = Color(0xFFB0B0B0)

private val DarkColorPalette = darkColors(
    primary = DarkPrimary,
    primaryVariant = DarkPrimaryVariant,
    secondary = DarkSecondary,
    secondaryVariant = DarkSecondaryVariant,
    background = DarkBackground,
    surface = DarkSurface,
    error = DarkError,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onError = DarkOnError,
)

private val LightColorPalette = lightColors(
    primary = LightPrimary,
    primaryVariant = LightPrimaryVariant,
    secondary = LightSecondary,
    secondaryVariant = LightSecondaryVariant,
    background = LightBackground,
    surface = LightSurface,
    error = LightError,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onError = LightOnError
)

@Composable
fun LifeCommanderTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}