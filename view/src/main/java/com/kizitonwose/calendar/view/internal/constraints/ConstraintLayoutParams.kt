package com.kizitonwose.calendar.view.internal.constraints

import android.os.Build
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout

internal open class ConstraintLayoutParams(source: ViewGroup.LayoutParams) :
    ConstraintLayout.LayoutParams(source) {
    init {
        if (source is ViewGroup.MarginLayoutParams) {
            this.leftMargin = source.leftMargin
            this.topMargin = source.topMargin
            this.rightMargin = source.rightMargin
            this.bottomMargin = source.bottomMargin
            if (Build.VERSION.SDK_INT >= 17) {
                this.marginStart = source.marginStart
                this.marginEnd = source.marginEnd
            }
        }
    }
}
