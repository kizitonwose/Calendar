package com.kizitonwose.calendar.view.internal.constraints

import android.view.View
import androidx.core.view.ViewCompat

internal class ItemChildVerticalChain(headerId: Int?, footerId: Int?, weekSize: Int) {
    private val chain = ConstraintChain(
        *listOfNotNull(
            resolveHeaderFooterId(id = headerId, alternate = header),
            *weeks.take(weekSize).toTypedArray(),
            resolveHeaderFooterId(id = footerId, alternate = footer),
        ).toIntArray(),
    )

    fun getNextLink(): ConstraintChain.Link = chain.getNextLink()

    private fun resolveHeaderFooterId(id: Int?, alternate: Int): Int? {
        return when (id) {
            null -> null
            View.NO_ID -> alternate
            else -> id
        }
    }

    companion object Ids {
        private val header = ViewCompat.generateViewId()
        private val footer = ViewCompat.generateViewId()
        private val weeks = IntArray(6) { ViewCompat.generateViewId() }
    }
}
