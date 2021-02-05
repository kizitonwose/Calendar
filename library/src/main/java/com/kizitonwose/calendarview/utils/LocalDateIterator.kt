package com.kizitonwose.calendarview.utils

import java.time.LocalDate

//internal class LocalDateIterator(
//    private val startDate: LocalDate,
//    private val endDateInclusive: LocalDate,
//    private val stepDays: Long
//) : Iterator<LocalDate> {
//
//    private var currentDate = startDate
//
//    override fun hasNext(): Boolean {
//        return currentDate.plusDays(stepDays) <= endDateInclusive
//    }
//
//    override fun next(): LocalDate {
//        val next = currentDate
//        currentDate = currentDate.plusDays(stepDays)
//        return next
//    }
//}
//
//internal class LocalDateProgression(
//    override val start: LocalDate,
//    override val endInclusive: LocalDate,
//    val stepDays: Long = 1
//) : Iterable<LocalDate>,
//    ClosedRange<LocalDate> {
//
//    override fun iterator(): Iterator<LocalDate> = LocalDateIterator(start, endInclusive, stepDays)
//
//    infix fun step(days: Long) = LocalDateProgression(start, endInclusive, days)
//}
//
//internal operator fun LocalDate.rangeTo(other: LocalDate) = LocalDateProgression(this, other)
