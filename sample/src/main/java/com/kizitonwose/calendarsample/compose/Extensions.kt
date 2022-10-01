package com.kizitonwose.calendarsample.compose

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection

fun Modifier.clickable(
    enabled: Boolean = true,
    showRipple: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = if (showRipple) LocalIndication.current else null,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = onClick
    )
}

fun Modifier.clipHalf(start: Boolean = false): Modifier = drawWithContent {
    val half = size.width / 2f
    when (layoutDirection) {
        LayoutDirection.Ltr -> {
            if (start) {
                clipRect(left = half) { this@drawWithContent.drawContent() }
            } else {
                clipRect(right = half) { this@drawWithContent.drawContent() }
            }
        }
        LayoutDirection.Rtl -> {
            if (start) {
                clipRect(right = half) { this@drawWithContent.drawContent() }
            } else {
                clipRect(left = half) {
                    this@drawWithContent.drawContent()
                }
            }
        }
    }
}

