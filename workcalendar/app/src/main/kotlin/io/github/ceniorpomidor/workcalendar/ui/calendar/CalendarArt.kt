package io.github.ceniorpomidor.workcalendar.ui.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ceniorpomidor.workcalendar.R
import io.github.ceniorpomidor.workcalendar.domain.model.CalendarArt

private val classicFont = FontFamily(Font(R.font.calendar_inter))
private val bandsFont = FontFamily(Font(R.font.calendar_condensed))
private val pastelFont = FontFamily(Font(R.font.calendar_lora))
private val comicFont = FontFamily(Font(R.font.calendar_nunito))
private val graffitiFont = FontFamily(Font(R.font.calendar_russo))
private val officeFont = FontFamily(Font(R.font.caveat))

@Immutable
internal data class CalendarStyle(
    val art: CalendarArt,
    val background: Color,
    val backgroundEnd: Color,
    val paper: Color,
    val ink: Color,
    val accent: Color,
) {
    val dark get() = paper.luminance() < 0.4f
    val rule get() = ink.copy(alpha = 0.13f)
    val secondaryInk get() = lerp(ink, paper, 0.24f)
    val holiday get() = if (dark) Color(0xFFE9ACA5) else Color(0xFF984842)
    val onAccent get() = if (dark) Color(0xFF15191D) else Color.White

    private val font get() = when (art) {
        CalendarArt.CLASSIC -> classicFont
        CalendarArt.BANDS -> bandsFont
        CalendarArt.AURORA -> pastelFont
        CalendarArt.COMIC -> comicFont
        CalendarArt.GRAFFITI -> graffitiFont
        CalendarArt.OFFICE -> officeFont
    }

    val titleStyle get() = TextStyle(
        fontFamily = font,
        fontSize = when (art) {
            CalendarArt.OFFICE -> 31.sp
            CalendarArt.GRAFFITI -> 19.sp
            else -> 22.sp
        },
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

    val weekdayStyle get() = titleStyle.copy(fontSize = if (art == CalendarArt.OFFICE) 17.sp else 11.sp)

    fun dateStyle(small: Boolean): TextStyle = titleStyle.copy(
        fontSize = when {
            art == CalendarArt.OFFICE -> if (small) 16.sp else 23.sp
            small -> 12.sp
            else -> 16.sp
        },
        fontFeatureSettings = if (art == CalendarArt.OFFICE) null else "tnum",
    )
}

@Composable
internal fun calendarStyle(art: CalendarArt): CalendarStyle {
    val scheme = MaterialTheme.colorScheme
    val night = scheme.surface.luminance() < 0.4f

    fun style(top: Long, bottom: Long, paper: Long, ink: Long, accent: Long) =
        CalendarStyle(art, Color(top), Color(bottom), Color(paper), Color(ink), Color(accent))

    return when (art) {
        CalendarArt.CLASSIC -> CalendarStyle(
            art, scheme.surface, scheme.surfaceContainer, scheme.surface, scheme.onSurface, scheme.primary,
        )
        CalendarArt.BANDS -> if (night) {
            style(0xFF171F2A, 0xFF222E3D, 0xFF202A37, 0xFFE7ECF3, 0xFFA9C4E4)
        } else {
            style(0xFFF3F7FB, 0xFFE1EAF4, 0xFFFBFCFE, 0xFF223246, 0xFF3D5F8A)
        }
        CalendarArt.AURORA -> if (night) {
            style(0xFF25202E, 0xFF342B36, 0xFF302A36, 0xFFF0E8F2, 0xFFD5BCE4)
        } else {
            style(0xFFE9E5F0, 0xFFF2E8E8, 0xFFFFFBFC, 0xFF382E44, 0xFF6A527F)
        }
        CalendarArt.COMIC -> if (night) {
            style(0xFF252421, 0xFF33302B, 0xFF302C27, 0xFFF6ECDC, 0xFFE4B786)
        } else {
            style(0xFFF4EDDD, 0xFFE9DECB, 0xFFFFF9EF, 0xFF342B25, 0xFF85532F)
        }
        CalendarArt.GRAFFITI -> if (night) {
            style(0xFF232426, 0xFF313332, 0xFF262729, 0xFFEDECE5, 0xFFD0C5A9)
        } else {
            style(0xFFE4E2DD, 0xFFD2D0CB, 0xFFF2F0EC, 0xFF2C2C2A, 0xFF515C4C)
        }
        CalendarArt.OFFICE -> if (night) {
            style(0xFF292620, 0xFF3E352B, 0xFF342E25, 0xFFF7ECD8, 0xFFE4C58E)
        } else {
            style(0xFFF0E9DA, 0xFFE3D7C7, 0xFFFEFAF0, 0xFF372E23, 0xFF775027)
        }
    }
}

internal fun Modifier.calendarBackdrop(style: CalendarStyle): Modifier =
    background(Brush.verticalGradient(listOf(style.background, style.backgroundEnd))).calendarTexture(style, 0.08f)

private fun Modifier.calendarTexture(style: CalendarStyle, strength: Float): Modifier {
    if (style.art != CalendarArt.COMIC && style.art != CalendarArt.GRAFFITI && style.art != CalendarArt.OFFICE) return this
    return drawWithCache {
        val marks = Path()
        val dots = mutableListOf<Offset>()
        val dotStep = 9.dp.toPx()
        val brickWidth = 52.dp.toPx()
        when (style.art) {
            CalendarArt.COMIC, CalendarArt.OFFICE -> {
                // Halftone on comic paper, pinholes on the desk mat.
                val spacing = if (style.art == CalendarArt.COMIC) dotStep else dotStep * 2
                for (row in 0..(size.height / spacing).toInt()) {
                    for (column in 0..(size.width / spacing).toInt()) {
                        dots.add(Offset((column + (row % 2) * 0.5f) * spacing, row * spacing))
                    }
                }
            }
            CalendarArt.GRAFFITI -> {
                val brickHeight = 24.dp.toPx()
                for (row in 0..(size.height / brickHeight).toInt()) {
                    val y = row * brickHeight
                    marks.moveTo(0f, y)
                    marks.lineTo(size.width, y)
                    for (column in 0..(size.width / brickWidth).toInt()) {
                        val x = (column + (row % 2) * 0.5f) * brickWidth
                        marks.moveTo(x, y)
                        marks.lineTo(x, y + brickHeight)
                    }
                }
            }
            else -> Unit
        }
        onDrawBehind {
            val texture = style.ink.copy(alpha = strength)
            if (dots.isNotEmpty()) drawPoints(dots, PointMode.Points, texture, strokeWidth = 1.5.dp.toPx())
            if (!marks.isEmpty) drawPath(marks, texture, style = Stroke(1.dp.toPx()))
        }
    }
}

internal fun Modifier.calendarSheet(style: CalendarStyle, textured: Boolean = false): Modifier {
    val radius = when (style.art) {
        CalendarArt.GRAFFITI -> 6.dp
        CalendarArt.BANDS, CalendarArt.CLASSIC -> 12.dp
        else -> 18.dp
    }
    val shape = RoundedCornerShape(radius)
    return clip(shape)
        .background(style.paper)
        .then(if (textured) Modifier.calendarTexture(style, 0.04f) else Modifier)
        .border(1.dp, style.rule, shape)
}

/** The same date artwork is used on the calendar and in the theme picker. */
@Composable
internal fun CalendarDate(
    number: Int,
    style: CalendarStyle,
    mark: CalendarMark?,
    variant: Int,
    today: Boolean,
    holiday: Boolean,
    small: Boolean,
    modifier: Modifier = Modifier,
    joinsLeft: Boolean = false,
    joinsRight: Boolean = false,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        val fill = mark?.fill(style)
        Canvas(Modifier.fillMaxSize()) {
            if (mark != null && fill != null) {
                when (style.art) {
                    CalendarArt.CLASSIC -> Unit // The whole tile is tinted.
                    CalendarArt.BANDS -> if (mark.isWork) {
                        val band = lerp(style.paper, style.accent, if (style.dark) 0.24f else 0.19f)
                        val top = size.height * 0.10f
                        val height = size.height * 0.80f
                        drawRoundRect(band, Offset(0f, top), Size(size.width, height), CornerRadius(height / 2))
                        if (joinsLeft) drawRect(band, Offset(0f, top), Size(size.width / 2, height))
                        if (joinsRight) drawRect(band, Offset(size.width / 2, top), Size(size.width / 2, height))
                    }
                    CalendarArt.AURORA -> {
                        val radius = size.minDimension * 0.45f
                        drawCircle(fill, radius)
                        drawCircle(mark.ink(style).copy(alpha = 0.30f), radius, style = Stroke(0.7.dp.toPx()))
                    }
                    CalendarArt.COMIC -> speechBubble(fill, style)
                    CalendarArt.GRAFFITI -> paintStroke(fill, style)
                    CalendarArt.OFFICE -> if (mark.isWork) paperNote(variant, style)
                }
            }
            if (today) drawCircle(style.accent, radius = size.minDimension * 0.47f)
        }
        val textColor = when {
            today -> style.onAccent
            holiday -> style.holiday
            else -> style.ink
        }
        val type = style.dateStyle(small)
        val fitNumber = style.art == CalendarArt.OFFICE || LocalDensity.current.fontScale > 1.2f
        BasicText(
            number.toString(),
            modifier = Modifier.rotate(if (style.art == CalendarArt.GRAFFITI && !today) -7f else 0f),
            style = type.copy(color = textColor),
            maxLines = 1,
            softWrap = false,
            autoSize = if (fitNumber) TextAutoSize.StepBased(8.sp, type.fontSize, 1.sp) else null,
        )
    }
}

private fun DrawScope.speechBubble(fill: Color, style: CalendarStyle) {
    val x = size.width
    val y = size.height
    val bubble = Path().apply {
        moveTo(x * 0.28f, y * 0.09f)
        lineTo(x * 0.72f, y * 0.09f)
        cubicTo(x * 0.91f, y * 0.09f, x * 0.93f, y * 0.80f, x * 0.71f, y * 0.80f)
        lineTo(x * 0.43f, y * 0.80f)
        lineTo(x * 0.30f, y * 0.96f)
        lineTo(x * 0.31f, y * 0.80f)
        cubicTo(x * 0.07f, y * 0.84f, x * 0.09f, y * 0.09f, x * 0.28f, y * 0.09f)
        close()
    }
    translate(top = 1.5.dp.toPx()) { drawPath(bubble, style.ink.copy(alpha = 0.14f)) }
    drawPath(bubble, fill)
    drawPath(bubble, style.ink.copy(alpha = 0.66f), style = Stroke(1.dp.toPx()))
}

private fun DrawScope.paintStroke(fill: Color, style: CalendarStyle) {
    val x = size.width
    val y = size.height
    val swipe = polygon(
        0.14f to 0.32f, 0.88f to 0.08f, 0.83f to 0.25f, 0.94f to 0.25f,
        0.84f to 0.70f, 0.12f to 0.94f, 0.19f to 0.75f, 0.08f to 0.78f,
    )
    drawPath(swipe, fill)
    val edge = style.ink.copy(alpha = 0.18f)
    drawLine(edge, Offset(x * 0.15f, y * 0.84f), Offset(x * 0.81f, y * 0.63f), 0.8.dp.toPx())
    drawLine(edge, Offset(x * 0.79f, y * 0.65f), Offset(x * 0.77f, y * 0.88f), 0.8.dp.toPx())
    drawCircle(edge, 0.8.dp.toPx(), Offset(x * 0.14f, y * 0.22f))
    drawCircle(edge, 0.6.dp.toPx(), Offset(x * 0.85f, y * 0.86f))
}

private val noteTints = listOf(Color(0xFFE2C88E), Color(0xFFAFBEA3), Color(0xFFD1AE9C), Color(0xFFBDC2CE))

private fun DrawScope.paperNote(variant: Int, style: CalendarStyle) {
    val note = when (variant) {
        0 -> polygon(0.13f to 0.09f, 0.91f to 0.21f, 0.86f to 0.94f, 0.09f to 0.80f)
        1 -> polygon(0.08f to 0.20f, 0.85f to 0.09f, 0.91f to 0.65f, 0.76f to 0.90f, 0.14f to 0.96f)
        2 -> polygon(0.10f to 0.14f, 0.90f to 0.09f, 0.89f to 0.94f, 0.67f to 0.84f, 0.51f to 0.95f, 0.33f to 0.86f, 0.11f to 0.92f)
        else -> polygon(0.12f to 0.11f, 0.88f to 0.11f, 0.88f to 0.71f, 0.74f to 0.92f, 0.12f to 0.92f)
    }
    val paper = lerp(style.paper, noteTints[variant], if (style.dark) 0.33f else 0.48f)
    translate(left = 0.6.dp.toPx(), top = 1.4.dp.toPx()) { drawPath(note, Color.Black.copy(alpha = 0.15f)) }
    drawPath(note, paper)
    drawPath(note, style.ink.copy(alpha = 0.17f), style = Stroke(0.7.dp.toPx()))
    if (variant == 3) {
        drawPath(polygon(0.74f to 0.92f, 0.74f to 0.71f, 0.88f to 0.71f), lerp(paper, style.ink, 0.22f))
    } else {
        drawRect(
            lerp(paper, style.ink, 0.18f),
            Offset(size.width * (if (variant == 1) 0.26f else 0.36f), size.height * 0.06f),
            Size(size.width * 0.30f, size.height * 0.16f),
        )
    }
}

private fun DrawScope.polygon(vararg points: Pair<Float, Float>): Path = Path().apply {
    for ((i, point) in points.withIndex()) {
        val x = point.first * size.width
        val y = point.second * size.height
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
