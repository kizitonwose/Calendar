package com.kizitonwose.calendar.sample

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.graphics.minus
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.appbar.AppBarLayout
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.sample.utils.TestDayViewContainer
import com.kizitonwose.calendar.sample.utils.getRectInWindow
import com.kizitonwose.calendar.sample.utils.getView
import com.kizitonwose.calendar.sample.utils.openExampleAt
import com.kizitonwose.calendar.sample.utils.runOnMain
import com.kizitonwose.calendar.sample.view.CalendarViewActivity
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep
import java.time.LocalDate
import java.time.YearMonth

/**
 * These are UI behaviour tests.
 * The core logic tests are in the data project.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CalendarViewTests {

    @get:Rule
    val homeScreenRule = ActivityScenarioRule(CalendarViewActivity::class.java)

    private val currentMonth = YearMonth.now()

    @Test
    fun dayBinderIsCalledOnDayChanged() {
        openExampleAt(0)

        val calendarView = getView<CalendarView>(R.id.exOneCalendar)

        var boundDay: CalendarDay? = null

        val changedDate = currentMonth.atDay(4)

        runOnMain {
            calendarView.dayBinder = object : MonthDayBinder<TestDayViewContainer> {
                override fun create(view: View) = TestDayViewContainer(view)
                override fun bind(container: TestDayViewContainer, data: CalendarDay) {
                    boundDay = data
                }
            }
        }

        // Allow the calendar to be rebuilt due to dayBinder change.
        sleep(2000)

        runOnMain {
            calendarView.notifyDateChanged(changedDate)
        }

        // Allow time for date change event to be propagated.
        sleep(2000)

        assertEquals(changedDate, boundDay?.date)
        assertEquals(DayPosition.MonthDate, boundDay?.position)
    }

    @Test
    fun allBindersAreCalledOnMonthChanged() {
        openExampleAt(1)

        val calendarView = getView<CalendarView>(R.id.exTwoCalendar)

        val boundDays = mutableSetOf<CalendarDay>()
        var boundHeaderMonth: CalendarMonth? = null

        runOnMain {
            calendarView.dayBinder = object : MonthDayBinder<TestDayViewContainer> {
                override fun create(view: View) = TestDayViewContainer(view)
                override fun bind(container: TestDayViewContainer, data: CalendarDay) {
                    boundDays.add(data)
                }
            }
            calendarView.monthHeaderBinder =
                object : MonthHeaderFooterBinder<TestDayViewContainer> {
                    override fun create(view: View) = TestDayViewContainer(view)
                    override fun bind(container: TestDayViewContainer, data: CalendarMonth) {
                        boundHeaderMonth = data
                    }
                }
        }

        // Allow the calendar to be rebuilt due to dayBinder change.
        sleep(2000)

        runOnMain {
            boundDays.clear()
            boundHeaderMonth = null
            calendarView.notifyMonthChanged(currentMonth)
        }

        // Allow time for date change event to be propagated.
        sleep(2000)

        assertEquals(boundHeaderMonth?.yearMonth, currentMonth)
        val monthDatesCount = boundDays.count {
            it.position == DayPosition.MonthDate && it.date.yearMonth == currentMonth
        }
        assertEquals(monthDatesCount, currentMonth.lengthOfMonth())
    }

    @Test
    fun programmaticScrollWorksAsExpected() {
        openExampleAt(4)

        val calendarView = getView<CalendarView>(R.id.exFiveCalendar)

        assertNotNull(calendarView.findViewWithTag(currentMonth.atDay(1).hashCode()))

        val nextFourMonths = currentMonth.plusMonths(4)

        runOnMain {
            calendarView.scrollToMonth(nextFourMonths)
        }

        sleep(2000)

        assertNull(calendarView.findViewWithTag(currentMonth.atDay(1).hashCode()))
        assertNotNull(calendarView.findViewWithTag(nextFourMonths.atDay(1).hashCode()))
    }

    @Test
    fun scrollToDateWorksOnVerticalOrientation() {
        openExampleAt(1)

        val calendarView = getView<CalendarView>(R.id.exTwoCalendar)

        val targetDate = currentMonth.plusMonths(4).atDay(20)

        runOnMain {
            calendarView.scrollToDate(targetDate)
        }

        sleep(2000)

        val dayView = calendarView.findViewWithTag<View>(targetDate.hashCode())

        val calendarViewRect = Rect()
        calendarView.getGlobalVisibleRect(calendarViewRect)

        val dayViewRect = Rect()
        dayView.getGlobalVisibleRect(dayViewRect)

        assertEquals(dayViewRect.top, calendarViewRect.top)
    }

    @Test
    fun scrollToDateWorksOnHorizontalOrientation() {
        openExampleAt(5)

        val calendarView = getView<CalendarView>(R.id.exSixCalendar)

        val targetDate = currentMonth.plusMonths(3).atDay(18)

        runOnMain {
            calendarView.scrollToDate(targetDate)
        }

        sleep(2000)

        val dayView = calendarView.findViewWithTag<View>(targetDate.hashCode())

        val calendarViewRect = Rect()
        calendarView.getGlobalVisibleRect(calendarViewRect)

        val dayViewRect = Rect()
        dayView.getGlobalVisibleRect(dayViewRect)

        assertEquals(dayViewRect.left, calendarViewRect.left)
    }

    @Test
    fun monthScrollListenerIsCalledWhenScrolled() {
        openExampleAt(0)

        val calendarView = getView<CalendarView>(R.id.exOneCalendar)

        var targetCalMonth: CalendarMonth? = null
        calendarView.monthScrollListener = { month ->
            targetCalMonth = month
        }

        val twoMonthsAhead = currentMonth.plusMonths(2)
        runOnMain {
            calendarView.smoothScrollToMonth(twoMonthsAhead)
        }
        sleep(3000) // Enough time for smooth scrolling animation.
        assertEquals(twoMonthsAhead, targetCalMonth?.yearMonth)

        val fourMonthsAhead = currentMonth.plusMonths(4)
        runOnMain {
            calendarView.scrollToMonth(fourMonthsAhead)
        }
        sleep(3000)
        assertEquals(fourMonthsAhead, targetCalMonth?.yearMonth)

        val sixMonthsAhead = currentMonth.plusMonths(6)
        runOnMain {
            calendarView.smoothScrollToDate(sixMonthsAhead.atDay(1))
        }
        sleep(3000)
        assertEquals(sixMonthsAhead, targetCalMonth?.yearMonth)

        val eightMonthsAhead = currentMonth.plusMonths(8)
        runOnMain {
            calendarView.scrollToDate(eightMonthsAhead.atDay(1))
        }
        sleep(3000)
        assertEquals(eightMonthsAhead, targetCalMonth?.yearMonth)
    }

    @Test
    fun findVisibleDaysAndMonthsWorksOnVerticalOrientation() {
        openExampleAt(1)

        val calendarView = getView<CalendarView>(R.id.exTwoCalendar)

        runOnMain {
            // Scroll to a random date
            calendarView.scrollToDate(LocalDate.now().plusDays(120))
        }

        sleep(2000)

        // First visible day is the first day in the week row it belongs. (top-left)
        val firstVisibleMonth = calendarView.findFirstVisibleMonth()!!
        val firstVisibleDay = calendarView.findFirstVisibleDay()!!
        val weekOfFirstDay = firstVisibleMonth.weekDays.first { weekDays ->
            weekDays.any { day -> day == firstVisibleDay }
        }
        assertEquals(firstVisibleDay, weekOfFirstDay.first())

        // Last visible day is the last day in the week row it belongs. (bottom-right)
        val lastVisibleMonth = calendarView.findLastVisibleMonth()!!
        val lastVisibleDay = calendarView.findLastVisibleDay()!!
        val weekOfLastDate = lastVisibleMonth.weekDays.first { weekDays ->
            weekDays.any { day -> day == lastVisibleDay }
        }
        assertEquals(lastVisibleDay, weekOfLastDate.last())
    }

    @Test
    fun findVisibleDaysAndMonthsWorksOnHorizontalOrientation() {
        openExampleAt(5)

        val calendarView = getView<CalendarView>(R.id.exSixCalendar)

        runOnMain {
            // Scroll to a random date
            calendarView.scrollToDate(LocalDate.now().plusDays(120))
        }

        sleep(2000)

        // First visible day is the first day in the month column(day of week) where it belongs. (top-left)
        val firstVisibleMonth = calendarView.findFirstVisibleMonth()!!
        val firstVisibleDay = calendarView.findFirstVisibleDay()!!
        val daysWIthSameDayOfWeekAsFirstDay = firstVisibleMonth.weekDays.flatten()
            .filter { it.date.dayOfWeek == firstVisibleDay.date.dayOfWeek }
        assertEquals(firstVisibleDay, daysWIthSameDayOfWeekAsFirstDay.first())

        // Last visible day is the last day in the month column(day of week) where it belongs. (bottom-right)
        val lastVisibleMonth = calendarView.findLastVisibleMonth()!!
        val lastVisibleDay = calendarView.findLastVisibleDay()!!
        val daysWIthSameDayOfWeekAsLastDay = lastVisibleMonth.weekDays.flatten()
            .filter { it.date.dayOfWeek == lastVisibleDay.date.dayOfWeek }
        assertEquals(lastVisibleDay, daysWIthSameDayOfWeekAsLastDay.last())
    }

    @Test
    fun monthDataUpdateRetainsPosition() {
        openExampleAt(0)

        val calendarView = getView<CalendarView>(R.id.exOneCalendar)

        val targetVisibleMonth = currentMonth.plusMonths(2)

        var targetVisibleCalMonth: CalendarMonth? = null
        calendarView.monthScrollListener = { month ->
            targetVisibleCalMonth = month
        }

        runOnMain {
            calendarView.smoothScrollToMonth(targetVisibleMonth)
        }

        sleep(3000) // Enough time for smooth scrolling animation.

        runOnMain {
            calendarView.updateMonthData(endMonth = targetVisibleMonth.plusMonths(10))
        }

        sleep(2000) // Enough time for UI adjustments.

        assertEquals(targetVisibleCalMonth, calendarView.findFirstVisibleMonth())
    }

    @Test
    fun horizontalCalendarWithMatchParentAndRectangleDaySizeFillsParent() {
        calendarWithMatchParentAndRectangleDaySizeFillsParent(RecyclerView.HORIZONTAL)
    }

    @Test
    fun verticalCalendarWithMatchParentAndRectangleDaySizeFillsParent() {
        calendarWithMatchParentAndRectangleDaySizeFillsParent(RecyclerView.VERTICAL)
    }

    private fun calendarWithMatchParentAndRectangleDaySizeFillsParent(@RecyclerView.Orientation orientation: Int) {
        openExampleAt(7)

        val calendarView = getView<CalendarView>(R.id.exEightCalendar)
        val parent = calendarView.parent as ViewGroup
        val appBarLayout = getView<AppBarLayout>(R.id.exEightAppBarLayout)
        val targetMonth = currentMonth.plusMonths(2)

        runOnMain {
            calendarView.orientation = orientation
            calendarView.smoothScrollToMonth(targetMonth)
        }

        sleep(2000) // Smooth scrolling.

        val itemView = calendarView
            .findViewHolderForItemId(targetMonth.hashCode().toLong())!!
            .itemView as ViewGroup

        val children = itemView.children.toList()
        val weeks = children.drop(1).dropLast(1).map { it as ViewGroup }
        val monthHeader = children.first()
        val monthFooter = children.last()

        assertEquals(
            parent.getRectInWindow().minus(appBarLayout.getRectInWindow()).bounds,
            itemView.getRectInWindow(),
        )
        assertEquals(8, children.count())
        weeks.forEach { week ->
            assertTrue(week.width > 0)
            assertTrue(week.height > 0)

            week.children.forEach { day ->
                assertTrue(day.width > 0)
                assertTrue(day.height > 0)
            }
            assertEquals(week.width, week.children.sumOf { it.width })
        }
        assertTrue(monthHeader.width > 0)
        assertTrue(monthHeader.height > 0)
        assertTrue(monthFooter.width > 0)
        assertTrue(monthFooter.height > 0)
        assertEquals(calendarView.height, children.sumOf { it.height })
    }

    @Test
    fun squareCalendarWorksAsExpected() {
        openExampleAt(4)

        val calendarView = getView<CalendarView>(R.id.exFiveCalendar)

        val itemView = calendarView.children.first() as ViewGroup

        val children = itemView.children.toList()
        val weeks = children.drop(1).map { it as ViewGroup }
        val monthHeader = children.first()

        assertEquals(
            itemView.height,
            weeks.sumOf { it.height } + monthHeader.height,
        )
        assertEquals(7, children.count())
        weeks.forEach { week ->
            assertTrue(week.width > 0)
            assertTrue(week.height > 0)

            week.children.forEach { day ->
                assertTrue(day.width > 0)
                assertTrue(day.height > 0)
                assertEquals(day.height.toFloat(), day.width.toFloat(), 1f)
            }
            assertEquals(week.width, week.children.sumOf { it.width })
        }
        assertTrue(monthHeader.width > 0)
        assertTrue(monthHeader.height > 0)
    }

    private fun <T : View> getView(@IdRes id: Int): T = homeScreenRule.getView(id)
}
