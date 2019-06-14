package com.kizitonwose.calenderview


/**
 * These are core functionality tests.
 * The UI behaviour tests are in the sample project.
 */

/* TODO FIX
class CalenderViewTests {

    // You can see what May 2019 with Monday as the first
    // day of week looks like here: https://bit.ly/2HwWZ7o
    private val may2019 = YearMonth.of(2019, 5)
    private val firstDayOfWeek = DayOfWeek.MONDAY

    private val endOfRowConfig =
        MonthConfig(
            OutDateStyle.END_OF_ROW,
            ScrollMode.PAGED,
            RecyclerView.HORIZONTAL,
            null
        )

    private val endOfGridConfig =
        MonthConfig(
            OutDateStyle.END_OF_GRID,
            ScrollMode.PAGED,
            RecyclerView.HORIZONTAL,
            null
        )


    @Test
    fun `test in date generation works as expected`() {
        val month = CalendarMonth(may2019, endOfRowConfig, firstDayOfWeek)

        val validInDateIndices = (0..1)
        val inDatesInMonth = month.weekDays.flatten().filterIndexed { index, _ -> validInDateIndices.contains(index) }

        // In dates are in appropriate indices and have accurate count.
        assertTrue(inDatesInMonth.all { it.owner == DayOwner.PREVIOUS_MONTH })
    }

    @Test
    fun `test end of row out date generation works as expected`() {
        val month = CalendarMonth(may2019, endOfRowConfig, firstDayOfWeek)

        val monthIndices = month.weekDays.flatten().indices
        val validOutDateIndices = monthIndices.toList().takeLast(2)
        val outDatesInMonth = month.weekDays.flatten().filterIndexed { index, _ -> validOutDateIndices.contains(index) }

        // Out dates are in appropriate indices and have accurate count.
        assertTrue(outDatesInMonth.all { it.owner == DayOwner.NEXT_MONTH })
    }

    @Test
    fun `test end of grid out date generation works as expected`() {
        val month = CalendarMonth(may2019, endOfGridConfig, firstDayOfWeek)

        val monthIndices = month.weekDays.flatten().indices
        val validOutDateIndices = monthIndices.toList().takeLast(9)
        val outDatesInMonth = month.weekDays.flatten().filterIndexed { index, _ -> validOutDateIndices.contains(index) }

        // Out dates are in appropriate indices and have accurate count.
        assertTrue(outDatesInMonth.all { it.owner == DayOwner.NEXT_MONTH })
    }

    @Test
    fun `test first day of week is in correct position`() {
        val month = CalendarMonth(may2019, endOfRowConfig, firstDayOfWeek)

        assertTrue(month.weekDays.first().first().date.dayOfWeek == firstDayOfWeek)
    }

    @Test
    fun `test month helper functions work as expected`() {
        val month = CalendarMonth(may2019, endOfRowConfig, firstDayOfWeek)

        assertTrue(month.previous == CalendarMonth(may2019.minusMonths(1), endOfRowConfig, firstDayOfWeek))
        assertTrue(month.next == CalendarMonth(may2019.plusMonths(1), endOfRowConfig, firstDayOfWeek))
    }
}
*/