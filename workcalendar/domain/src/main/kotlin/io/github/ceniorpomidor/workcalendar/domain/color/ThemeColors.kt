package io.github.ceniorpomidor.workcalendar.domain.color

import io.github.ceniorpomidor.workcalendar.domain.model.ColorPalette
import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Colors of a tone: lightness [tone] as CIELAB L* (0 = black, 100 = white), like the tones of
 * Material 3. Hue and chroma are taken in OKLCH, which keeps the hue stable from dark to light
 * tones. Equal tones have equal luminance whatever the hue, so text contrast depends only on
 * the tones of the two colors.
 */
object Tones {
    /** sRGB color 0xFFRRGGBB with the largest chroma up to [chroma] that fits the sRGB gamut. */
    fun color(tone: Double, hue: Double, chroma: Double): Int {
        if (tone <= 0.0) return argb(0.0, 0.0, 0.0)
        if (tone >= 100.0) return argb(1.0, 1.0, 1.0)
        val targetY = lstarToY(tone)
        val rad = Math.toRadians(hue)
        val a = cos(rad)
        val b = sin(rad)
        fun solve(c: Double): DoubleArray? {
            var lo = 0.0
            var hi = 1.0
            repeat(32) {
                val mid = (lo + hi) / 2
                if (luminance(oklabToLinear(mid, c * a, c * b)) < targetY) lo = mid else hi = mid
            }
            val rgb = oklabToLinear((lo + hi) / 2, c * a, c * b)
            val inGamut = rgb.all { it in -GAMUT_EPS..1 + GAMUT_EPS }
            return if (inGamut && abs(luminance(rgb) - targetY) < 1e-4) rgb else null
        }
        val best = solve(chroma) ?: run {
            var lo = 0.0
            var hi = chroma
            var found = solve(0.0) ?: doubleArrayOf(targetY, targetY, targetY)
            repeat(20) {
                val mid = (lo + hi) / 2
                val rgb = solve(mid)
                if (rgb == null) {
                    hi = mid
                } else {
                    lo = mid
                    found = rgb
                }
            }
            found
        }
        return argb(best[0], best[1], best[2])
    }

    /** Relative luminance of an sRGB color (0..1). */
    fun luminance(argb: Int): Double = luminance(doubleArrayOf(toLinear(argb shr 16 and 0xFF), toLinear(argb shr 8 and 0xFF), toLinear(argb and 0xFF)))

    /** WCAG contrast ratio of two colors (1..21). */
    fun contrast(first: Int, second: Int): Double {
        val l1 = luminance(first)
        val l2 = luminance(second)
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }

    /** CIELAB lightness of a color (0..100). */
    fun tone(argb: Int): Double {
        val y = luminance(argb)
        return if (y > 216.0 / 24389.0) 116.0 * cbrt(y) - 16.0 else y * 24389.0 / 27.0
    }

    private const val GAMUT_EPS = 1e-4

    private fun lstarToY(tone: Double): Double {
        val ft = (tone + 16.0) / 116.0
        val cube = ft * ft * ft
        return if (cube > 216.0 / 24389.0) cube else tone * 27.0 / 24389.0
    }

    private fun luminance(rgb: DoubleArray): Double = 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]

    private fun oklabToLinear(l: Double, a: Double, b: Double): DoubleArray {
        val l1 = l + 0.3963377774 * a + 0.2158037573 * b
        val m1 = l - 0.1055613458 * a - 0.0638541728 * b
        val s1 = l - 0.0894841775 * a - 1.2914855480 * b
        val l3 = l1 * l1 * l1
        val m3 = m1 * m1 * m1
        val s3 = s1 * s1 * s1
        return doubleArrayOf(
            4.0767416621 * l3 - 3.3077115913 * m3 + 0.2309699292 * s3,
            -1.2684380046 * l3 + 2.6097574011 * m3 - 0.3413193965 * s3,
            -0.0041960863 * l3 - 0.7034186147 * m3 + 1.7076147010 * s3,
        )
    }

    private fun toLinear(channel: Int): Double {
        val c = channel / 255.0
        return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

    private fun toSrgb(linear: Double): Int {
        val c = linear.coerceIn(0.0, 1.0)
        val v = if (c <= 0.0031308) 12.92 * c else 1.055 * c.pow(1.0 / 2.4) - 0.055
        return (v * 255.0).roundToInt().coerceIn(0, 255)
    }

    private fun argb(r: Double, g: Double, b: Double): Int = (0xFF shl 24) or (toSrgb(r) shl 16) or (toSrgb(g) shl 8) or toSrgb(b)
}

/** Colors of one hue at different tones. Light tones get less chroma: pale containers instead of neon ones. */
class TonalPalette(val hue: Double, val chroma: Double) {
    private val cache = HashMap<Int, Int>()

    fun tone(tone: Int): Int = cache.getOrPut(tone) {
        val damping = 1.0 - 0.5 * ((tone - 70) / 30.0).coerceAtLeast(0.0)
        Tones.color(tone.toDouble(), hue, chroma * damping)
    }
}

/** Colors of all Material 3 roles (0xAARRGGBB) generated from one hue. */
data class ThemeColors(
    val primary: Int,
    val onPrimary: Int,
    val primaryContainer: Int,
    val onPrimaryContainer: Int,
    val inversePrimary: Int,
    val secondary: Int,
    val onSecondary: Int,
    val secondaryContainer: Int,
    val onSecondaryContainer: Int,
    val tertiary: Int,
    val onTertiary: Int,
    val tertiaryContainer: Int,
    val onTertiaryContainer: Int,
    val background: Int,
    val onBackground: Int,
    val surface: Int,
    val onSurface: Int,
    val surfaceVariant: Int,
    val onSurfaceVariant: Int,
    val inverseSurface: Int,
    val inverseOnSurface: Int,
    val outline: Int,
    val outlineVariant: Int,
    val surfaceBright: Int,
    val surfaceDim: Int,
    val surfaceContainerLowest: Int,
    val surfaceContainerLow: Int,
    val surfaceContainer: Int,
    val surfaceContainerHigh: Int,
    val surfaceContainerHighest: Int,
) {
    companion object {
        /** Hue of a preset palette in OKLCH degrees; null for the custom palette. */
        fun hueOf(palette: ColorPalette): Double? = when (palette) {
            ColorPalette.BLUE -> 258.0
            ColorPalette.INDIGO -> 280.0
            ColorPalette.VIOLET -> 305.0
            ColorPalette.PINK -> 350.0
            ColorPalette.RED -> 25.0
            ColorPalette.ORANGE -> 45.0
            ColorPalette.AMBER -> 75.0
            ColorPalette.OLIVE -> 110.0
            ColorPalette.GREEN -> 150.0
            ColorPalette.TEAL -> 200.0
            ColorPalette.GRAPHITE -> 258.0
            ColorPalette.CUSTOM -> null
        }

        /** Main color of a palette, shown in the palette picker. */
        fun swatch(palette: ColorPalette, customHue: Int, dark: Boolean): Int {
            val chroma = if (palette == ColorPalette.GRAPHITE) 0.0 else 0.12
            return TonalPalette(hueOf(palette) ?: customHue.toDouble(), chroma).tone(if (dark) 80 else 40)
        }

        /** Scheme of a palette ([customHue] is used by [ColorPalette.CUSTOM]). */
        fun of(palette: ColorPalette, customHue: Int, dark: Boolean, pureBlack: Boolean = false): ThemeColors =
            generate(hueOf(palette) ?: customHue.toDouble(), dark, monochrome = palette == ColorPalette.GRAPHITE, pureBlack = pureBlack)

        fun generate(hue: Double, dark: Boolean, monochrome: Boolean = false, pureBlack: Boolean = false): ThemeColors {
            val k = if (monochrome) 0.0 else 1.0
            val h = ((hue % 360.0) + 360.0) % 360.0
            val p = TonalPalette(h, 0.12 * k)
            val s = TonalPalette(h, 0.04 * k)
            val t = TonalPalette((h + 60.0) % 360.0, 0.07 * k)
            val n = TonalPalette(h, 0.010 * k)
            val nv = TonalPalette(h, 0.020 * k)
            val black = dark && pureBlack

            // Tone of a role in the light and in the dark scheme (Material 3 "tonal spot").
            fun tone(light: Int, darkTone: Int): Int = if (dark) darkTone else light
            return ThemeColors(
                primary = p.tone(tone(40, 80)),
                onPrimary = p.tone(tone(100, 20)),
                primaryContainer = p.tone(tone(90, 30)),
                onPrimaryContainer = p.tone(tone(10, 90)),
                inversePrimary = p.tone(tone(80, 40)),
                secondary = s.tone(tone(40, 80)),
                onSecondary = s.tone(tone(100, 20)),
                secondaryContainer = s.tone(tone(90, 30)),
                onSecondaryContainer = s.tone(tone(10, 90)),
                tertiary = t.tone(tone(40, 80)),
                onTertiary = t.tone(tone(100, 20)),
                tertiaryContainer = t.tone(tone(90, 30)),
                onTertiaryContainer = t.tone(tone(10, 90)),
                background = n.tone(tone(98, if (black) 0 else 6)),
                onBackground = n.tone(tone(10, 90)),
                surface = n.tone(tone(98, if (black) 0 else 6)),
                onSurface = n.tone(tone(10, 90)),
                surfaceVariant = nv.tone(tone(90, 30)),
                onSurfaceVariant = nv.tone(tone(30, 80)),
                inverseSurface = n.tone(tone(20, 90)),
                inverseOnSurface = n.tone(tone(95, 20)),
                outline = nv.tone(tone(50, 60)),
                outlineVariant = nv.tone(tone(80, 30)),
                surfaceBright = n.tone(tone(98, if (black) 20 else 24)),
                surfaceDim = n.tone(tone(87, if (black) 0 else 6)),
                surfaceContainerLowest = n.tone(tone(100, if (black) 0 else 4)),
                surfaceContainerLow = n.tone(tone(96, if (black) 6 else 10)),
                surfaceContainer = n.tone(tone(94, if (black) 8 else 12)),
                surfaceContainerHigh = n.tone(tone(92, if (black) 12 else 17)),
                surfaceContainerHighest = n.tone(tone(90, if (black) 17 else 22)),
            )
        }
    }
}
