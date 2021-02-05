package com.kizitonwose.calendarview.model

import java.time.LocalDate

sealed class Event {

    abstract val id: String
    abstract val name: String
    abstract val active: Boolean

    /**
     * Single day event.
     */
    data class Single(
        override val id: String,
        override val name: String,
        override val active: Boolean,
        val start: LocalDate,
        val end: LocalDate = start
    ) : Event()

    /**
     * An event that takes place during the whole day or more.
     */
    data class AllDay(
        override val id: String,
        override val name: String,
        override val active: Boolean,
        val start: LocalDate,
        val end: LocalDate
    ) : Event()
}

internal sealed class InternalEvent {

    abstract val apiEvent: Event
    abstract val start: LocalDate
    abstract val end: LocalDate
    abstract val name: String
    abstract val active: Boolean

    /**
     * Single day event.
     */
    data class Single(
        override val apiEvent: Event,
        override val start: LocalDate,
        override val end: LocalDate,
        override val name: String,
        override val active: Boolean
    ) : InternalEvent() {
        init {
            if (!start.isEqual(end)) {
                throw IllegalStateException("Items must be the same day for a single day event.")
            }
        }
    }

    sealed class AllDay : InternalEvent() {

        /**
         * An all-day starting event that takes place for more than one day.
         */
        data class Original(
            override val apiEvent: Event,
            override val start: LocalDate,
            override val end: LocalDate,
            override val active: Boolean,
            override val name: String
        ) : AllDay() {
            val isSingleDay: Boolean = start.isEqual(end)
        }

        /**
         * An all-day partial event that started by [original] event.
         */
        data class Partial(
            override val apiEvent: Event,
            override val start: LocalDate,
            override val end: LocalDate,
            val original: Original
        ) : AllDay() {
            override val name: String = original.name
            override val active: Boolean = original.active

            /**
             * Check whether this is the last day.
             */
            val isEndingDay: Boolean = original.end.isEqual(end)
        }
    }
}

internal sealed class InternalEventWrapper {
    data class Single(val event: InternalEvent.Single) : InternalEventWrapper()
    data class Multiple(val events: List<InternalEvent.AllDay>) : InternalEventWrapper()
}