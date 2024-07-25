package com.kizitonwose.calendar.core

@RequiresOptIn(
    message = "This calendar API is experimental and is " +
        "likely to change or to be removed in the future.",
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalCalendarApi
