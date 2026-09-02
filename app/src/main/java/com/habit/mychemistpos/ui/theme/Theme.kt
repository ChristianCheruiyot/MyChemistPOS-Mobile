package com.habit.mychemistpos.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ─── Color Scheme ───────────────────────────────────────────────────────────────
private val ChemistColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryTint,
    onPrimaryContainer = PrimaryDark,

    secondary = PrimaryDark,
    onSecondary = Color.White,
    secondaryContainer = PrimaryTint,
    onSecondaryContainer = PrimaryDark,

    tertiary = Primary,
    onTertiary = Color.White,

    background = PageBackground,
    onBackground = TextPrimary,

    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = PageBackground,
    onSurfaceVariant = TextSecondary,

    outline = Border,
    outlineVariant = Border,

    error = Danger,
    onError = Color.White,
    errorContainer = DangerTint,
    onErrorContainer = Danger,
)

// ─── Shapes ─────────────────────────────────────────────────────────────────────
val ChemistShapes = Shapes(
    small = RoundedCornerShape(10.dp),   // Buttons, inputs
    medium = RoundedCornerShape(12.dp),  // Cards
    large = RoundedCornerShape(20.dp),   // Chips, bottom sheets
)

// ─── Theme ──────────────────────────────────────────────────────────────────────
@Composable
fun MyChemistPOSTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true      // Dark status bar icons
                isAppearanceLightNavigationBars = true  // Dark nav bar icons
            }
        }
    }

    MaterialTheme(
        colorScheme = ChemistColorScheme,
        typography = Typography,
        shapes = ChemistShapes,
        content = content
    )
}