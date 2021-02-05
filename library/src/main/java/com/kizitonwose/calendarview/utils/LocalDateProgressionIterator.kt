package com.kizitonwose.calendarview.utils

import java.time.LocalDate


class LocalDateProgressionIterator(
   first: LocalDate,
   private val last: LocalDate
) : Iterator<LocalDate> {

   private var hasNext: Boolean =
      first <= last

   private var next: LocalDate =
      if (hasNext) {
         first
      } else {
         last
      }

   override fun hasNext(): Boolean = hasNext

   override fun next(): LocalDate {
      val value = next
      if (value == last) {
         if (!hasNext) {
            throw NoSuchElementException()
         }
         hasNext = false
      } else {
         next = next.plusDays(1)
      }
      return value
   }
}
