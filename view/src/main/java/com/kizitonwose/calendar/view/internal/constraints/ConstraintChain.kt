package com.kizitonwose.calendar.view.internal.constraints

import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID

internal class ConstraintChain(private vararg val ids: Int) {
    private var index = 0

    fun getNextLink(): Link {
        val index = index
        this.index += 1
        return when (index) {
            ids.indices.first -> Link(
                id = ids[index],
                previousId = PARENT_ID,
                nextId = ids.getOrNull(index + 1) ?: PARENT_ID,
            )
            ids.indices.last -> Link(
                id = ids[index],
                previousId = ids.getOrNull(index - 1) ?: PARENT_ID,
                nextId = PARENT_ID,
            )
            else -> Link(
                id = ids[index],
                previousId = ids[index - 1],
                nextId = ids[index + 1],
            )
        }
    }

    data class Link(val id: Int, val previousId: Int, val nextId: Int)
}
