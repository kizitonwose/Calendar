package com.kizitonwose.calendar.sample.shared

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.view.getColorCompat
import java.lang.ref.WeakReference

class StatusBarColorLifecycleObserver(
    activity: Activity,
    @ColorInt private val color: Int,
) : DefaultLifecycleObserver {
    private val isLightColor = ColorUtils.calculateLuminance(color) > 0.5
    private val defaultStatusBarColor = activity.getColorCompat(R.color.colorPrimaryDark)
    private val activity = WeakReference(activity)

    override fun onStart(owner: LifecycleOwner) {
        activity.get()?.window?.apply {
            statusBarColor = color
            if (isLightColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        activity.get()?.window?.apply {
            statusBarColor = defaultStatusBarColor
            if (isLightColor) decorView.systemUiVisibility = 0
        }
    }

    override fun onDestroy(owner: LifecycleOwner) = activity.clear()
}
