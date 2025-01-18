package com.kizitonwose.calendar.data

internal fun <T : Comparable<T>> checkRange(start: T, end: T) {
    check(end >= start) {
        "start: $start is greater than end: $end"
    }
}

internal fun <T> T.asUnit() = Unit

internal expect fun log(tag: String, message: String)
