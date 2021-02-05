package com.kizitonwose.calendarview.utils

import com.kizitonwose.calendarview.model.Event
import com.kizitonwose.calendarview.model.InternalEvent

internal fun List<Event>.flatMapToInternalEvents(): List<InternalEvent> {
    return flatMap { event ->
        when (event) {
            is Event.Single -> event.toInternalEvents()
            is Event.AllDay -> event.toInternalEvents()
        }
    }
}

internal fun Event.Single.toInternalEvents(): List<InternalEvent> {
    return if (start.isEqual(end)) {
        listOf(InternalEvent.Single(apiEvent = this, start = start, end = end, name = name, active = active))
    } else {
        Event
            .AllDay(
                id = id,
                start = start,
                end = end,
                name = name,
                active = active
            )
            .toInternalEvents()         // convert to all day
    }
}

internal fun Event.AllDay.toInternalEvents(): List<InternalEvent.AllDay> {
    return if (start.isEqual(end)) {
        listOf(
            InternalEvent.AllDay.Original(
                apiEvent = this,
                start = start,
                end = end,
                name = name,
                active = active
            )
        )
    } else {
        val original = InternalEvent.AllDay.Original(
            apiEvent = this,
            start = start,
            end = end,
            name = name,
            active = active
        )

        (start..end).mapIndexed { index, localDate ->
            if (index == 0) {
                original
            } else {
                InternalEvent.AllDay.Partial(
                    apiEvent = this,
                    start = localDate,
                    end = localDate,
                    original = original
                )
            }
        }
    }
}
