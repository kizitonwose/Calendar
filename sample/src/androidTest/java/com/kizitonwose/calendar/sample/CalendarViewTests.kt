package com.kizitonwose.calendar.sample

import android.graphics.Rect
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.sample.view.CalendarViewActivity
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import org.junit.Assert.*
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
        onView(withId(R.id.examplesRecyclerview))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        val calendarView = getCalendarView(R.id.exOneCalendar)

        var boundDay: CalendarDay? = null

        val changedDate = currentMonth.atDay(4)

        homeScreenRule.scenario.onActivity {
            calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    boundDay = data
                }
            }
        }

        // Allow the calendar to be rebuilt due to dayBinder change.
        sleep(2000)

        homeScreenRule.scenario.onActivity {
            calendarView.notifyDateChanged(changedDate)
        }

        // Allow time for date change event to be propagated.
        sleep(2000)

        assertEquals(changedDate, boundDay?.date)
        assertEquals(DayPosition.MonthDate, boundDay?.position)
    }

    @Test
    fun allBindersAreCalledOnMonthChanged() {
        onView(withId(R.id.examplesRecyclerview))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        val calendarView = getCalendarView(R.id.exTwoCalendar)

        val boundDays = mutableSetOf<CalendarDay>()
        var boundHeaderMonth: CalendarMonth? = null

        homeScreenRule.scenario.onActivity {
            calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    boundDays.add(data)
                }
            }
            calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: CalendarMonth) {
                    boundHeaderMonth = data
                }
            }
        }

        // Allow the calendar to be rebuilt due to dayBinder change.
        sleep(2000)

        homeScreenRule.scenario.onActivity {
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
        onView(withId(R.id.examplesRecyclerview))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(4, click()))

        val calendarView = getCalendarView(R.id.exFiveCalendar)

        assertNotNull(calendarView.findViewWithTag(currentMonth.atDay(1).asMonthDay().hashCode()))

        val nextFourMonths = currentMonth.plusMonths(4)

        homeScreenRule.scenario.onActivity {
            calendarView.scrollToMonth(nextFourMonths)
        }

        sleep(2000)

        assertNull(calendarView.findViewWithTag(currentMonth.atDay(1).asMonthDay().hashCode()))
        assertNotNull(calendarView.findViewWithTag(nextFourMonths.atDay(1).asMonthDay().hashCode()))
    }

    @Test
    fun scrollToDateWorksOnVerticalOrientation() {
        onView(withId(R.id.examplesRecyclerview))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        val calendarView = getCalendarView(R.id.exTwoCalendar)

        val targetDate = currentMonth.plusMonths(4).atDay(20)

        homeScreenRule.scenario.onActivity {
            calendarView.scrollToDate(targetDate)
        }

        sleep(2000)

        val dayView = calendarView.findViewWithTag<View>(targetDate.asMonthDay().hashCode())

        val calendarViewRect = Rect()
        calendarView.getGlobalVisibleRect(calendarViewRect)

        val dayViewRect = Rect()
        dayView.getGlobalVisibleRect(dayViewRect)

        assertEquals(dayViewRect.top, calendarViewRect.top)
    }

    @Test
    fun scrollToDateWorksOnHorizontalOrientation() {
        onView(withId(R.id.examplesRecyclerview))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(5, click()))

        val calendarView = getCalendarView(R.id.exSixCalendar)

        val targetDate = currentMonth.plusMonths(3).atDay(18)

        homeScreenRule.scenario.onActivity {
            calendarView.scrollToDate(targetDate)
        }

        sleep(2000)

        val dayView = calendarView.findViewWithTag<View>(targetDate.asMonthDay().hashCode())

        val calendarViewRect = Rect()
        calendarView.getGlobalVisibleRect(calendarViewRect)

        val dayViewRect = Rect()
        dayView.getGlobalVisibleRect(dayViewRect)

        assertEquals(dayViewRect.left, calendarViewRect.left)
    }

    @Test
    fun monthScrollListenerIsCalledWhenScrolled() {
        onView(withId(R.id.examplesRecyclerview))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        val calendarView = getCalendarView(R.id.exOneCalendar)

        var targetCalMonth: CalendarMonth? = null
        calendarView.monthScrollListener = { month ->
            targetCalMonth = month
        }

        val twoMonthsAhead = currentMonth.plusMonths(2)
        homeScreenRule.scenario.onActivity {
            calendarView.smoothScrollToMonth(twoMonthsAhead)
        }
        sleep(3000) // Enough time for smooth scrolling animation.
        assertEquals(twoMonthsAhead, targetCalMonth?.yearMonth)

        val fourMonthsAhead = currentMonth.plusMonths(4)
        homeScreenRule.scenario.onActivity {
            calendarView.scrollToMonth(fourMonthsAhead)
        }
        sleep(3000)
        assertEquals(fourMonthsAhead, targetCalMonth?.yearMonth)

        val sixMonthsAhead = currentMonth.plusMonths(6)
        homeScreenRule.scenario.onActivity {
            calendarView.smoothScrollToDate(sixMonthsAhead.atDay(1))
        }
        sleep(3000)
        assertEquals(sixMonthsAhead, targetCalMonth?.yearMonth)

        val eightMonthsAhead = currentMonth.plusMonths(8)
        homeScreenRule.scenario.onActivity {
            calendarView.scrollToDate(eightMonthsAhead.atDay(1))
        }
        sleep(3000)
        assertEquals(eightMonthsAhead, targetCalMonth?.yearMonth)
    }

    @Test
    fun findVisibleDaysAndMonthsWorksOnVerticalOrientation() {
        onView(withId(R.id.examplesRecyclerview))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        val calendarView = getCalendarView(R.id.exTwoCalendar)

        homeScreenRule.scenario.onActivity {
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
        onView(withId(R.id.examplesRecyclerview))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(5, click()))

        val calendarView = getCalendarView(R.id.exSixCalendar)

        homeScreenRule.scenario.onActivity {
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
        onView(withId(R.id.examplesRecyclerview))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        val calendarView = getCalendarView(R.id.exOneCalendar)

        val targetVisibleMonth = currentMonth.plusMonths(2)

        var targetVisibleCalMonth: CalendarMonth? = null
        calendarView.monthScrollListener = { month ->
            targetVisibleCalMonth = month
        }

        homeScreenRule.scenario.onActivity {
            calendarView.smoothScrollToMonth(targetVisibleMonth)
        }

        sleep(3000) // Enough time for smooth scrolling animation.

        homeScreenRule.scenario.onActivity {
            calendarView.updateMonthData(
                endMonth = targetVisibleMonth.plusMonths(10),
            )
        }

        sleep(2000) // Enough time for UI adjustments.

        assertEquals(targetVisibleCalMonth, calendarView.findFirstVisibleMonth())
    }

    private fun getCalendarView(@IdRes id: Int): CalendarView {
        lateinit var calendarView: CalendarView
        homeScreenRule.scenario.onActivity { activity ->
            calendarView = activity.findViewById(id)
        }
        sleep(1000)
        return calendarView
    }

    private class DayViewContainer(view: View) : ViewContainer(view)

    private fun LocalDate.asMonthDay(): CalendarDay = CalendarDay(this, DayPosition.MonthDate)
}
