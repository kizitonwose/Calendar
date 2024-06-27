package com.kizitonwose.calendar.data

fun <T : Comparable<T>> checkDateRange(start: T, end: T) {
    check(end >= start) {
        "start: $start is greater than end: $end"
    }
}
