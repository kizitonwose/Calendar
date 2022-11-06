package com.kizitonwose.calendar.view.internal.constraints

import androidx.core.view.ViewCompat

internal class DayCellHorizontalChain {
    private val chain = ConstraintChain(*days)

    fun getNextLink(): ConstraintChain.Link = chain.getNextLink()

    companion object Ids {
        private val days = IntArray(7) { ViewCompat.generateViewId() }
    }
}
