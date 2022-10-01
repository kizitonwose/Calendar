package com.kizitonwose.calendarsample.compose

import android.os.Build
import android.view.View
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import com.kizitonwose.calendarsample.R
import com.kizitonwose.calendarsample.findActivity

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

@Composable
fun ToggleStatusBarColor(color: Color, isLight: Boolean = false) {
    val activity = LocalContext.current.findActivity()
    val defaultStatusBarColor = colorResource(R.color.colorPrimaryDark)
    DisposableEffect(LocalLifecycleOwner.current) {
        activity.window.apply {
            statusBarColor = color.toArgb()
            if (isLight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
        onDispose {
            activity.window.apply {
                statusBarColor = defaultStatusBarColor.toArgb()
                if (isLight) decorView.systemUiVisibility = 0
            }
        }
    }
}