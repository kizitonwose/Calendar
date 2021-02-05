package com.kizitonwose.calendarview.ui

import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.kizitonwose.calendarview.R
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.EventModel
import com.kizitonwose.calendarview.utils.inflate

internal class EventListHolder(private val config: EventListConfig) {

    private lateinit var eventListView: View
    private lateinit var viewContainer: EventListViewContainer
    private var days: List<CalendarDay>? = null
    private var events: List<EventModel>? = null

    fun inflateEventListView(parent: LinearLayout): View {
        eventListView = parent.inflate(config.eventListViewRes).apply { config.eventListBinder.postInflate(this) }
        return eventListView
    }

    fun bindEventList(days: List<CalendarDay>, events: List<EventModel>) {
        this.days = days.toList()
        this.events = events.toList()

        if (!::viewContainer.isInitialized) {
            viewContainer = config.eventListBinder.create(eventListView)
        }

        val dayHash = this.events?.hashCode()
        if (viewContainer.view.tag != dayHash) {
            viewContainer.view.tag = dayHash
        }

        if (this.events != null) {
            if (!viewContainer.view.isVisible) {
                viewContainer.view.isVisible = true
            }
            config.eventListBinder.bind(viewContainer, events)
        } else if (!viewContainer.view.isGone) {
            viewContainer.view.isGone = true
        }
    }

    fun reloadViewIfNecessary(): Boolean {
        val days = this.days ?: emptyList()
        val events = this.events ?: emptyList()

        bindEventList(days, events)

        return true
    }

    fun recycle() {
        if (::viewContainer.isInitialized) {
            config.eventListBinder.recycle(viewContainer)
        }
    }
}

internal interface InternalEventListBinder<T : ViewContainer> {
    fun postInflate(view: View)
    fun create(view: View): T
    fun bind(container: T, events: List<EventModel>)
    fun recycle(container: T)
}

internal data class EventListConfig(
    val eventListBinder: EventListBinder
) {
    @LayoutRes
    val eventListViewRes: Int = R.layout.item_week_events
}
