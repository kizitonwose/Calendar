package com.kizitonwose.calendarview.ui

import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.kizitonwose.calendarview.R
import com.kizitonwose.calendarview.model.Event
import com.kizitonwose.calendarview.model.EventModel
import com.kizitonwose.calendarview.utils.inflate

internal class EventListBinder(
    private val eventCellConfig: EventCellConfig,
) : InternalEventListBinder<EventListViewContainer> {

    companion object {
        private const val TAG_SPACE = "tag_space"
        private const val TAG_EVENT = "tag_event"
    }

    private var columnWidth = 0

    private val eventCellHolders = mutableListOf<ViewContainer>()

    override fun postInflate(view: View) {
        populateDummyEvents(view as GridLayout)
    }

    override fun create(view: View): EventListViewContainer {
        return EventListViewContainer(view)
    }

    override fun bind(container: EventListViewContainer, events: List<EventModel>) {
        container.gridLayout.children
            .filter { it.tag == TAG_EVENT }
            .forEach { container.gridLayout.removeView(it) }

        val space: View = container.gridLayout.findViewWithTag(TAG_SPACE)
        columnWidth = space.measuredWidth

        for (event in events) {
            addEvent(
                grid = container.gridLayout,
                event = event.apiModel,
                rowIndex = event.rowIndex,
                columnIndex = event.columnIndex,
                daySpan = event.daySpan,
                leftBoundaryStart = event.leftBoundaryStart,
                rightBoundaryEnd = event.rightBoundaryEnd,
            )
        }
    }

    override fun recycle(container: EventListViewContainer) {
        eventCellHolders.forEach {
            eventCellConfig.eventCellBinder?.recycle(it)
            (it.view.parent as? ViewGroup)?.removeView(it.view)
        }
        eventCellHolders.clear()
        container.gridLayout.children
            .filter { it.tag != TAG_SPACE }
            .forEach { container.gridLayout.removeView(it) }
    }

    private fun addEvent(
        grid: GridLayout,
        event: Event,
        rowIndex: Int,
        columnIndex: Int,
        daySpan: Int,
        leftBoundaryStart: Boolean,
        rightBoundaryEnd: Boolean
    ) {
        val eventCellView = grid.inflate(eventCellConfig.layoutRes).apply { tag = TAG_EVENT }

        eventCellConfig.eventCellBinder?.let {
            val eventCellHolder = it.create(eventCellView)
            it.bind(
                container = eventCellHolder,
                event = event,
                leftBoundaryStart = leftBoundaryStart,
                rightBoundaryEnd = rightBoundaryEnd
            )

            grid.addView(
                eventCellHolder.view,
                (eventCellView.layoutParams as GridLayout.LayoutParams).apply {
                    rowSpec = GridLayout.spec(rowIndex, 1)
                    columnSpec = GridLayout.spec(columnIndex, daySpan, 1f)
                    width = columnWidth * daySpan
                }
            )

            eventCellHolders.add(eventCellHolder)
        }
    }

    private fun populateDummyEvents(grid: GridLayout) {
        for (row in 0..3) {
            for (column in 0..6) {
                val space = grid.inflate(R.layout.item_week_space).apply { tag = TAG_SPACE }
                if ((column + row) % 2 == 0) {
                    space.setBackgroundColor(ContextCompat.getColor(grid.context, android.R.color.holo_green_dark))
                } else {
                    space.setBackgroundColor(ContextCompat.getColor(grid.context, android.R.color.holo_red_dark))
                }
                space.layoutParams = (space.layoutParams as GridLayout.LayoutParams).apply {
                    this.rowSpec = GridLayout.spec(row, 1)
                    this.columnSpec = GridLayout.spec(column, 1, 1f);
                }
                grid.addView(space)
            }
        }
    }
}

internal data class EventCellConfig(
    @LayoutRes val layoutRes: Int,
    val eventCellBinder: EventCellBinder<ViewContainer>?
)

internal class EventListViewContainer(view: View) : ViewContainer(view) {
    val gridLayout: GridLayout = view.findViewById(R.id.grid)
}
