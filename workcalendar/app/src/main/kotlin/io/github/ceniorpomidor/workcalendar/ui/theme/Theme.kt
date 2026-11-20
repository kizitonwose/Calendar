package io.github.ceniorpomidor.workcalendar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.ceniorpomidor.workcalendar.domain.color.ThemeColors
import io.github.ceniorpomidor.workcalendar.domain.model.AppearanceSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ThemeMode

// Error colors and other roles that do not depend on the palette.
private val LightBase: ColorScheme = lightColorScheme(
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkBase: ColorScheme = darkColorScheme(
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

/** Material color scheme of a generated palette. */
fun ThemeColors.toColorScheme(dark: Boolean): ColorScheme = (if (dark) DarkBase else LightBase).copy(
    primary = Color(primary),
    onPrimary = Color(onPrimary),
    primaryContainer = Color(primaryContainer),
    onPrimaryContainer = Color(onPrimaryContainer),
    inversePrimary = Color(inversePrimary),
    secondary = Color(secondary),
    onSecondary = Color(onSecondary),
    secondaryContainer = Color(secondaryContainer),
    onSecondaryContainer = Color(onSecondaryContainer),
    tertiary = Color(tertiary),
    onTertiary = Color(onTertiary),
    tertiaryContainer = Color(tertiaryContainer),
    onTertiaryContainer = Color(onTertiaryContainer),
    background = Color(background),
    onBackground = Color(onBackground),
    surface = Color(surface),
    onSurface = Color(onSurface),
    surfaceVariant = Color(surfaceVariant),
    onSurfaceVariant = Color(onSurfaceVariant),
    surfaceTint = Color(primary),
    inverseSurface = Color(inverseSurface),
    inverseOnSurface = Color(inverseOnSurface),
    outline = Color(outline),
    outlineVariant = Color(outlineVariant),
    surfaceBright = Color(surfaceBright),
    surfaceDim = Color(surfaceDim),
    surfaceContainerLowest = Color(surfaceContainerLowest),
    surfaceContainerLow = Color(surfaceContainerLow),
    surfaceContainer = Color(surfaceContainer),
    surfaceContainerHigh = Color(surfaceContainerHigh),
    surfaceContainerHighest = Color(surfaceContainerHighest),
)

/** Black background for a dark scheme (wallpaper colors); containers stay slightly lighter. */
private fun ColorScheme.withPureBlack(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = lerp(surfaceContainerLow, Color.Black, 0.45f),
    surfaceContainer = lerp(surfaceContainer, Color.Black, 0.4f),
    surfaceContainerHigh = lerp(surfaceContainerHigh, Color.Black, 0.3f),
    surfaceContainerHighest = lerp(surfaceContainerHighest, Color.Black, 0.25f),
)

/** Colors of day and shift states; the same in the calendar, lists, legend and widget. */
@Immutable
data class StatusPalette(
    val planned: Color,
    val onPlanned: Color,
    val inProgress: Color,
    val onInProgress: Color,
    val awaiting: Color,
    val onAwaiting: Color,
    val confirmed: Color,
    val onConfirmed: Color,
    val missed: Color,
    val onMissed: Color,
    val cancelled: Color,
    val onCancelled: Color,
    val extra: Color,
    val onExtra: Color,
    val vacation: Color,
    val onVacation: Color,
    val sick: Color,
    val onSick: Color,
    val otherAbsence: Color,
    val onOtherAbsence: Color,
    val holiday: Color,
    val positive: Color,
    val negative: Color,
    /** Day of an advance or salary payment. */
    val payday: Color,
    val onPayday: Color,
    val paydayAccent: Color,
)

val LightStatus = StatusPalette(
    planned = Color(0xFFDCE7FB),
    onPlanned = Color(0xFF1D4E9E),
    inProgress = Color(0xFFC9DBFF),
    onInProgress = Color(0xFF0E3C84),
    awaiting = Color(0xFFFFE3AE),
    onAwaiting = Color(0xFF6E4400),
    confirmed = Color(0xFFD2EFD9),
    onConfirmed = Color(0xFF1B5E2E),
    missed = Color(0xFFFAD4D0),
    onMissed = Color(0xFF8C1D18),
    cancelled = Color(0xFFE8E8EA),
    onCancelled = Color(0xFF5F6066),
    extra = Color(0xFFEADDFB),
    onExtra = Color(0xFF5A2A9C),
    vacation = Color(0xFFCDEEF2),
    onVacation = Color(0xFF0B5F67),
    sick = Color(0xFFF8D7E6),
    onSick = Color(0xFF8A1F4F),
    otherAbsence = Color(0xFFEDE1D4),
    onOtherAbsence = Color(0xFF5B4636),
    holiday = Color(0xFFD0453C),
    positive = Color(0xFF2E7D45),
    negative = Color(0xFFC62828),
    payday = Color(0xFFFFE9A8),
    onPayday = Color(0xFF6B4E00),
    paydayAccent = Color(0xFFE0A100),
)

val DarkStatus = StatusPalette(
    planned = Color(0xFF23395F),
    onPlanned = Color(0xFFC3D6FF),
    inProgress = Color(0xFF1D4479),
    onInProgress = Color(0xFFD8E5FF),
    awaiting = Color(0xFF4F3A10),
    onAwaiting = Color(0xFFFFD68A),
    confirmed = Color(0xFF1F4129),
    onConfirmed = Color(0xFFA8E3B6),
    missed = Color(0xFF5A211D),
    onMissed = Color(0xFFFFB4AB),
    cancelled = Color(0xFF34353A),
    onCancelled = Color(0xFFB9BAC0),
    extra = Color(0xFF3E2A5E),
    onExtra = Color(0xFFDCC7FF),
    vacation = Color(0xFF12474D),
    onVacation = Color(0xFFA2E7EE),
    sick = Color(0xFF4F2236),
    onSick = Color(0xFFFFB1D0),
    otherAbsence = Color(0xFF43382E),
    onOtherAbsence = Color(0xFFE5D3C2),
    holiday = Color(0xFFFF8A80),
    positive = Color(0xFF7BD893),
    negative = Color(0xFFFF8A80),
    payday = Color(0xFF4D3B00),
    onPayday = Color(0xFFFFDF8A),
    paydayAccent = Color(0xFFFFC83D),
)

val LocalStatusPalette = staticCompositionLocalOf { LightStatus }

object AppTheme {
    val status: StatusPalette
        @Composable get() = LocalStatusPalette.current
}

@Composable
fun WorkCalendarTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    appearance: AppearanceSettings = AppearanceSettings(),
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val pureBlack = dark && appearance.pureBlack
    val generated = remember(appearance.palette, appearance.customHue, dark, pureBlack) {
        ThemeColors.of(appearance.palette, appearance.customHue, dark, pureBlack).toColorScheme(dark)
    }
    val colors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val wallpaper = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        if (pureBlack) wallpaper.withPureBlack() else wallpaper
    } else {
        generated
    }
    // Status and navigation bar icons follow the theme of the app, not only the system one.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    CompositionLocalProvider(LocalStatusPalette provides if (dark) DarkStatus else LightStatus) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}
