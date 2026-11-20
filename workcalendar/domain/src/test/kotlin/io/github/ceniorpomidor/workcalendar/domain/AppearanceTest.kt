package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.color.ThemeColors
import io.github.ceniorpomidor.workcalendar.domain.color.Tones
import io.github.ceniorpomidor.workcalendar.domain.model.ColorPalette
import io.github.ceniorpomidor.workcalendar.domain.model.NavItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppearanceTest {
    private fun ThemeColors.pairs(): Map<String, Pair<Int, Int>> = mapOf(
        "primary" to (onPrimary to primary),
        "primaryContainer" to (onPrimaryContainer to primaryContainer),
        "secondaryContainer" to (onSecondaryContainer to secondaryContainer),
        "tertiaryContainer" to (onTertiaryContainer to tertiaryContainer),
        "surface" to (onSurface to surface),
        "surfaceVariant" to (onSurfaceVariant to surfaceContainerHighest),
        "inverse" to (inverseOnSurface to inverseSurface),
    )

    @Test
    fun `text is readable in every palette`() {
        for (palette in ColorPalette.entries) {
            for (dark in listOf(false, true)) {
                for (black in listOf(false, true)) {
                    val colors = ThemeColors.of(palette, customHue = 123, dark = dark, pureBlack = black)
                    for ((role, pair) in colors.pairs()) {
                        val contrast = Tones.contrast(pair.first, pair.second)
                        assertTrue(contrast >= 4.5, "$palette dark=$dark black=$black $role: $contrast")
                    }
                }
            }
        }
    }

    @Test
    fun `tones have the requested lightness`() {
        for (hue in 0 until 360 step 15) {
            for (tone in listOf(10, 30, 40, 80, 90, 98)) {
                val color = Tones.color(tone.toDouble(), hue.toDouble(), 0.12)
                assertEquals(tone.toDouble(), Tones.tone(color), 0.7, "hue $hue tone $tone")
            }
        }
        val graphite = ThemeColors.of(ColorPalette.GRAPHITE, 0, dark = false).primary
        assertEquals(graphite shr 16 and 0xFF, graphite and 0xFF)
        assertEquals(0xFF000000.toInt(), ThemeColors.of(ColorPalette.BLUE, 0, dark = true, pureBlack = true).background)
    }

    @Test
    fun `bottom bar always has the calendar and settings and at most five items`() {
        assertEquals(listOf(NavItem.CALENDAR, NavItem.SETTINGS), NavItem.normalize(emptyList()))
        assertEquals(
            listOf(NavItem.CALENDAR, NavItem.ALARM, NavItem.FINANCE, NavItem.SETTINGS),
            NavItem.normalize(listOf(NavItem.ALARM, NavItem.FINANCE, NavItem.ALARM)),
        )
        assertEquals(
            listOf(NavItem.SETTINGS, NavItem.CALENDAR, NavItem.FINANCE, NavItem.STATS, NavItem.HISTORY),
            NavItem.normalize(listOf(NavItem.SETTINGS, NavItem.CALENDAR, NavItem.FINANCE, NavItem.STATS, NavItem.HISTORY, NavItem.SEARCH)),
        )
    }
}
