package io.github.ceniorpomidor.workcalendar.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

fun Modifier.clipRounded(radius: Int): Modifier = this.clip(RoundedCornerShape(radius.dp))
