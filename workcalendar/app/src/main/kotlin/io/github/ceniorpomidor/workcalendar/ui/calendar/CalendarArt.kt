package io.github.ceniorpomidor.workcalendar.ui.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import io.github.ceniorpomidor.workcalendar.R
import io.github.ceniorpomidor.workcalendar.domain.model.CalendarArt
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.theme.AppTheme
import java.time.LocalDate

private val handwriting = FontFamily(Font(R.font.caveat))
private val ink = Color(0xFF292735)
private val paper = listOf(Color(0xFFFFE993), Color(0xFFAFE9D8), Color(0xFFFFCBB6), Color(0xFFDAD0FF))
private val pastels = listOf(Color(0xFFCEBAFA), Color(0xFF9BE6D7), Color(0xFFFFBCE9), Color(0xFFB1DFFC))
private val spray = listOf(Color(0xFFC6FF38), Color(0xFF5EEAFF), Color(0xFFFF78CA), Color(0xFFFFBF54))

@Composable
internal fun Modifier.calendarBackdrop(art: CalendarArt): Modifier {
    val dark = MaterialTheme.colorScheme.surface.luminance() < .4f
    val colors = when (art) {
        CalendarArt.CLASSIC -> return this
        CalendarArt.BANDS -> if (dark) listOf(Color(0xFF171C26), Color(0xFF202733)) else listOf(Color.White, Color(0xFFF2F5FA))
        CalendarArt.AURORA -> if (dark) listOf(Color(0xFF182247), Color(0xFF463957), Color(0xFF213E48)) else listOf(Color(0xFFB9DDFA), Color(0xFFCEBDEB), Color(0xFFF6C3CA))
        CalendarArt.COMIC -> if (dark) listOf(Color(0xFF362C36), Color(0xFF2E3744)) else listOf(Color(0xFFFFF3CB), Color(0xFFFFDFCA))
        CalendarArt.GRAFFITI -> listOf(Color(0xFF20202E), Color(0xFF382644), Color(0xFF1E2933))
        CalendarArt.OFFICE -> if (dark) listOf(Color(0xFF352D29), Color(0xFF443831)) else listOf(Color(0xFFECDCC1), Color(0xFFDDC4A1))
    }
    return background(Brush.linearGradient(colors))
}

@Composable
internal fun Modifier.monthSheet(art: CalendarArt): Modifier = when (art) {
    CalendarArt.CLASSIC -> this
    CalendarArt.GRAFFITI -> clip(RoundedCornerShape(12.dp)).background(Color(0xFF25232E))
    else -> clip(RoundedCornerShape(if (art == CalendarArt.BANDS) 4.dp else 18.dp)).background(MaterialTheme.colorScheme.surface)
}

private fun DrawScope.polygon(vararg points: Pair<Float, Float>): Path = Path().apply {
    points.forEachIndexed { i, (x, y) ->
        if (i == 0) moveTo(x * size.width, y * size.height) else lineTo(x * size.width, y * size.height)
    }
    close()
}

private fun DrawScope.paperNote(variant: Int, color: Color) {
    val outline = when (variant) {
        0 -> polygon(.05f to .06f, .98f to .12f, .9f to .96f, .03f to .87f)
        1 -> polygon(.02f to .15f, .93f to .03f, .98f to .72f, .78f to .93f, .1f to .99f)
        2 -> polygon(.03f to .1f, .96f to .04f, .98f to .94f, .77f to .86f, .58f to .97f, .38f to .89f, .1f to .97f)
        else -> polygon(.08f to .04f, .96f to .04f, .96f to .83f, .82f to .97f, .08f to .97f)
    }
    drawPath(outline, color)
    drawPath(outline, ink.copy(alpha = .25f), style = Stroke(1.dp.toPx()))
    drawRect(Color.White.copy(alpha = .65f), Offset(size.width * .33f, 0f), Size(size.width * .34f, size.height * .15f))
}

private fun DrawScope.comicBadge(color: Color) {
    val w = size.width
    val h = size.height
    val balloon = Path().apply {
        moveTo(.12f*w,.12f*h)
        cubicTo(.26f*w,0f,.82f*w,0f,.9f*w,.12f*h)
        cubicTo(w,.24f*h,w,.65f*h,.88f*w,.76f*h)
        quadraticTo(.76f*w,.86f*h,.46f*w,.82f*h)
        lineTo(.20f*w,.98f*h)
        lineTo(.24f*w,.82f*h)
        cubicTo(0f,.84f*h,0f,.22f*h,.12f*w,.12f*h)
        close()
    }
    drawPath(balloon, color)
    drawPath(balloon, ink, style = Stroke(1.8.dp.toPx()))
    drawCircle(Color.White, 1.6.dp.toPx(), Offset(.23f*w,.2f*h))
}

private fun DrawScope.brushMark(color: Color) {
    val slash = polygon(.07f to .26f, .94f to .04f, .83f to .27f, .99f to .22f, .88f to .75f, .12f to .94f, .2f to .72f, .01f to .8f)
    drawPath(slash, color)
    drawLine(color, Offset(size.width*.78f,size.height*.57f), Offset(size.width*.75f,size.height*.96f), 2.dp.toPx())
    drawCircle(color, 1.4.dp.toPx(), Offset(size.width*.06f,size.height*.11f))
}

@Composable
internal fun ArtDay(
    art: CalendarArt, day: CalendarDay, info: DayInfo?, chosen: Boolean, today: LocalDate,
    showHolidays: Boolean, height: Dp, compact: Boolean, joinsLeft: Boolean, joinsRight: Boolean,
    onClick: () -> Unit, onLongClick: () -> Unit,
) {
    val date = day.date
    val working = info?.shifts?.any { it.isActive } == true
    val payday = !info?.payouts.isNullOrEmpty()
    val status = info?.state ?: DayState.OFF
    val marked = status != DayState.OFF
    val variant = Math.floorMod(date.toEpochDay(), 4L).toInt()
    val street = art == CalendarArt.GRAFFITI
    val surfaceInk = if (street) Color.White else MaterialTheme.colorScheme.onSurface
    val labelInk = when {
        art == CalendarArt.BANDS && (working || date == today) -> Color.White
        marked && art != CalendarArt.BANDS && (art != CalendarArt.OFFICE || working) -> ink
        showHolidays && info?.specialDayOff == true -> AppTheme.status.holiday
        else -> surfaceInk
    }
    val stateColor = status.colors(AppTheme.status, Color.Transparent, surfaceInk).second
    val name = if (payday) "День выплаты, ${Formats.date(date)}" else Formats.dayTitle(date)
    Column(
        Modifier.fillMaxWidth().height(height).alpha(if (day.position == DayPosition.MonthDate) 1f else .45f)
            .semantics {
                selected = chosen
                contentDescription = name
            }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .then(if (chosen) Modifier.border(1.5.dp, if (street) Color.White else MaterialTheme.colorScheme.primary, RoundedCornerShape(9.dp)) else Modifier)
            .padding(top = if (height >= 46.dp) 3.dp else 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top,
    ) {
        Box(Modifier.fillMaxWidth().height(if (height < 41.dp) 21.dp else 30.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(horizontal = if (art == CalendarArt.BANDS) 0.dp else 3.dp)) {
                when (art) {
                    CalendarArt.BANDS -> {
                        if (working) {
                            val radius = size.height * .44f
                            drawRoundRect(Color(0xFF416ACD), Offset(0f,size.height*.06f), Size(size.width,radius*2), CornerRadius(radius))
                            if (joinsLeft) drawRect(Color(0xFF416ACD), Offset(0f,size.height*.06f), Size(size.width/2,radius*2))
                            if (joinsRight) drawRect(Color(0xFF416ACD), Offset(size.width/2,size.height*.06f), Size(size.width/2,radius*2))
                        }
                        if (date == today) drawCircle(Color(0xFF007E74), size.minDimension*.49f)
                    }
                    CalendarArt.AURORA -> if (marked) drawCircle(pastels[variant],size.minDimension*.49f)
                    CalendarArt.COMIC -> if (marked) comicBadge(paper[variant])
                    CalendarArt.GRAFFITI -> if (marked) brushMark(spray[variant])
                    CalendarArt.OFFICE -> if (working) paperNote(variant,paper[variant])
                    CalendarArt.CLASSIC -> Unit
                }
            }
            Text(
                date.dayOfMonth.toString(), color = labelInk,
                modifier = Modifier.rotate(if (street) -9f else 0f),
                style = TextStyle(
                    fontFamily = when (art) { CalendarArt.OFFICE -> handwriting; CalendarArt.GRAFFITI -> FontFamily.SansSerif; else -> FontFamily.Default },
                    fontSize = if (art == CalendarArt.OFFICE) 22.sp else 15.sp,
                    fontWeight = if (street || art == CalendarArt.COMIC || date == today) FontWeight.Black else FontWeight.Medium,
                    fontStyle = if (street) FontStyle.Italic else FontStyle.Normal,
                    shadow = if (street && marked) Shadow(Color.White, Offset(2f,1f), 0f) else null,
                ),
                maxLines = 1,
            )
        }
        val markers = buildString {
            if (date == today && art != CalendarArt.BANDS) append("•")
            if (payday) append("₽")
            if (info?.hasExtra == true) append("+")
            if (info?.note != null) append("·")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (marked) Spacer(Modifier.size(9.dp, 3.dp).clip(CircleShape).background(if (street) spray[variant] else stateColor))
            if (markers.isNotEmpty()) Text(markers, fontSize = 10.sp, lineHeight = 11.sp, color = surfaceInk, maxLines = 1)
        }
        if (!compact && height >= 46.dp) {
            info?.lines.orEmpty().take(if (height >= 66.dp) 2 else 1).forEach {
                Text(it, fontSize = 10.sp, lineHeight = 11.sp, maxLines = 1, color = surfaceInk, overflow = TextOverflow.Ellipsis)
            }
        }
        if (height >= 60.dp) info?.earned?.let { Text(compactMoney(it), fontSize = 9.sp, lineHeight = 10.sp, color = surfaceInk, maxLines = 1) }
    }
}
