package com.kizitonwose.calendar.view.internal

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

internal fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

internal const val NO_INDEX = -1

internal fun Rect.intersects(other: Rect): Boolean {
    return if (this.isEmpty || other.isEmpty) {
        false
    } else {
        Rect.intersects(this, other)
    }
}

internal fun missingField(field: String) = "`$field` is not set. Have you called `setup()`?"
