package com.kizitonwose.calendarview.model

/**
 * Wrapper around [InternalEvent] specifying event cells.
 */
internal sealed class EventModel {
    abstract val name: String?
    abstract val columnIndex: Int
    abstract val rowIndex: Int
    abstract val daySpan: Int
    abstract val active: Boolean
    abstract val apiModel: Event
    abstract val leftBoundaryStart: Boolean
    abstract val rightBoundaryEnd: Boolean

    data class Single(
        override val name: String?,
        override val columnIndex: Int,
        override val rowIndex: Int,
        override val active: Boolean,
        override val apiModel: Event,
        val original: InternalEvent.Single
    ) : EventModel() {
        override val daySpan: Int = 1
        override val leftBoundaryStart: Boolean = true
        override val rightBoundaryEnd: Boolean = true
    }

    data class AllDay(
        override val name: String?,
        override val columnIndex: Int,
        override val rowIndex: Int,
        override val daySpan: Int,
        override val active: Boolean,
        override val apiModel: Event,
        override val leftBoundaryStart: Boolean,
        override val rightBoundaryEnd: Boolean,
        val original: InternalEvent.AllDay.Original
    ) : EventModel()
}