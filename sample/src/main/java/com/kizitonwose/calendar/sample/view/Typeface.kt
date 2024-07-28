package com.kizitonwose.calendar.sample.view

import android.content.Context
import android.graphics.Typeface
import androidx.core.graphics.TypefaceCompat

/**
 * See the comment on the [TypefaceCompat.create] method
 * with the weight param for various weight definitions.
 */
object Typeface {
    fun normal(context: Context) = TypefaceCompat.create(context, Typeface.SANS_SERIF, 400, false)
    fun medium(context: Context) = TypefaceCompat.create(context, Typeface.SANS_SERIF, 500, false)
    fun semiBold(context: Context) = TypefaceCompat.create(context, Typeface.SANS_SERIF, 600, false)
    fun bold(context: Context) = TypefaceCompat.create(context, Typeface.SANS_SERIF, 700, false)
}
