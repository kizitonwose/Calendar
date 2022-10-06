package com.kizitonwose.calendersample

import android.graphics.Rect
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.kizitonwose.calendarcore.*
import com.kizitonwose.calendarsample.R
import com.kizitonwose.calendarsample.view.*
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.MonthDayBinder
import com.kizitonwose.calendarview.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ViewContainer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep
import java.time.LocalDate
import java.time.YearMonth

/**
 * These are UI behaviour tests.
 * The core functionality tests are in the library project.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CalenderViewTests {

    @get:Rule
    val homeScreenRule = ActivityTestRule(CalendarViewActivity::class.java, true, false)

    private val currentMonth = YearMonth.now()

    @Before
    fun setup() {
        homeScreenRule.launchActivity(null)
    }

    @After
    fun teardown() {
    }

    @Test
    fun dayBinderIsCalledOnDayChanged() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0,
            click()))

        class DayViewContainer(view: View) : ViewContainer(view)

        val calendarView =
            findFragment<Example1Fragment>().findViewById<CalendarView>(R.id.exOneCalendar)

        var boundDay: CalendarDay? = null

        val changedDate = currentMonth.atDay(4)

        homeScreenRule.runOnUiThread {
            calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, value: CalendarDay) {
                    boundDay = value
                }
            }
        }

        // Allow the calendar to be rebuilt due to dayBinder change.
        sleep(2000)

        homeScreenRule.runOnUiThread {
            calendarView.notifyDateChanged(changedDate)
        }

        // Allow time for date change event to be propagated.
        sleep(2000)

        assertTrue(boundDay?.date == changedDate)
        assertTrue(boundDay?.position == DayPosition.MonthDate)
    }

    @Test
    fun allBindersAreCalledOnMonthChanged() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1,
            click()))

        class TestViewContainer(view: View) : ViewContainer(view)

        val calendarView =
            findFragment<Example2Fragment>().findViewById<CalendarView>(R.id.exTwoCalendar)

        val boundDays = mutableSetOf<CalendarDay>()
        var boundHeaderMonth: CalendarMonth? = null

        homeScreenRule.runOnUiThread {
            calendarView.dayBinder = object : MonthDayBinder<TestViewContainer> {
                override fun create(view: View) = TestViewContainer(view)
                override fun bind(container: TestViewContainer, value: CalendarDay) {
                    boundDays.add(value)
                }
            }
            calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<TestViewContainer> {
                override fun create(view: View) = TestViewContainer(view)
                override fun bind(container: TestViewContainer, month: CalendarMonth) {
                    boundHeaderMonth = month
                }
            }
        }

        // Allow the calendar to be rebuilt due to dayBinder change.
        sleep(2000)

        homeScreenRule.runOnUiThread {
            boundDays.clear()
            boundHeaderMonth = null
            calendarView.notifyMonthChanged(currentMonth)
        }

        // Allow time for date change event to be propagated.
        sleep(2000)

        assertTrue(boundHeaderMonth?.yearMonth == currentMonth)
        val monthDatesCount = boundDays.count {
            it.position == DayPosition.MonthDate && it.date.yearMonth == currentMonth
        }
        assertTrue(monthDatesCount == currentMonth.lengthOfMonth())
    }

    @Test
    fun programmaticScrollWorksAsExpected() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(4,
            click()))

        val calendarView =
            findFragment<Example5Fragment>().findViewById<CalendarView>(R.id.exFiveCalendar)

        assertTrue(calendarView.findViewWithTag<View>(currentMonth.atDay(1).asMonthDay()
            .hashCode()) != null)

        val nextFourMonths = currentMonth.plusMonths(4)

        homeScreenRule.runOnUiThread {
            calendarView.scrollToMonth(nextFourMonths)
        }

        sleep(2000)

        assertTrue(calendarView.findViewWithTag<View>(currentMonth.atDay(1).asMonthDay()
            .hashCode()) == null)
        assertTrue(calendarView.findViewWithTag<View>(nextFourMonths.atDay(1).asMonthDay()
            .hashCode()) != null)
    }

    @Test
    fun scrollToDateWorksOnVerticalOrientation() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1,
            click()))

        val calendarView =
            findFragment<Example2Fragment>().findViewById<CalendarView>(R.id.exTwoCalendar)

        val targetDate = currentMonth.plusMonths(4).atDay(20)

        homeScreenRule.runOnUiThread {
            calendarView.scrollToDate(targetDate)
        }

        sleep(2000)

        val dayView = calendarView.findViewWithTag<View>(targetDate.asMonthDay().hashCode())

        val calendarViewRect = Rect()
        calendarView.getGlobalVisibleRect(calendarViewRect)

        val dayViewRect = Rect()
        dayView.getGlobalVisibleRect(dayViewRect)

        assertEquals(calendarViewRect.top, dayViewRect.top)
    }

    @Test
    fun scrollToDateWorksOnHorizontalOrientation() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(5,
            click()))

        val calendarView =
            findFragment<Example6Fragment>().findViewById<CalendarView>(R.id.exSixCalendar)

        val targetDate = currentMonth.plusMonths(3).atDay(18)

        homeScreenRule.runOnUiThread {
            calendarView.scrollToDate(targetDate)
        }

        sleep(2000)

        val dayView = calendarView.findViewWithTag<View>(targetDate.asMonthDay().hashCode())

        val calendarViewRect = Rect()
        calendarView.getGlobalVisibleRect(calendarViewRect)

        val dayViewRect = Rect()
        dayView.getGlobalVisibleRect(dayViewRect)

        assertEquals(calendarViewRect.left, dayViewRect.left)
    }

    @Test
    fun monthScrollListenerIsCalledWhenScrolled() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0,
            click()))

        val calendarView =
            findFragment<Example1Fragment>().findViewById<CalendarView>(R.id.exOneCalendar)

        var targetCalMonth: CalendarMonth? = null
        calendarView.monthScrollListener = { month ->
            targetCalMonth = month
        }

        val twoMonthsAhead = currentMonth.plusMonths(2)
        homeScreenRule.runOnUiThread {
            calendarView.smoothScrollToMonth(twoMonthsAhead)
        }
        sleep(3000) // Enough time for smooth scrolling animation.
        assertEquals(targetCalMonth?.yearMonth, twoMonthsAhead)

        val fourMonthsAhead = currentMonth.plusMonths(4)
        homeScreenRule.runOnUiThread {
            calendarView.scrollToMonth(fourMonthsAhead)
        }
        sleep(3000)
        assertEquals(targetCalMonth?.yearMonth, fourMonthsAhead)

        val sixMonthsAhead = currentMonth.plusMonths(6)
        homeScreenRule.runOnUiThread {
            calendarView.smoothScrollToDate(sixMonthsAhead.atDay(1))
        }
        sleep(3000)
        assertEquals(targetCalMonth?.yearMonth, sixMonthsAhead)

        val eightMonthsAhead = currentMonth.plusMonths(8)
        homeScreenRule.runOnUiThread {
            calendarView.scrollToDate(eightMonthsAhead.atDay(1))
        }
        sleep(3000)
        assertEquals(targetCalMonth?.yearMonth, eightMonthsAhead)
    }

    @Test
    fun findVisibleDaysAndMonthsWorksOnVerticalOrientation() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1,
            click()))

        val calendarView =
            findFragment<Example2Fragment>().findViewById<CalendarView>(R.id.exTwoCalendar)

        homeScreenRule.runOnUiThread {
            // Scroll to a random date
            calendarView.scrollToDate(LocalDate.now().plusDays(120))
        }

        sleep(2000)

        // First visible day is the first day in the week row it belongs. (top-left)
        val firstVisibleMonth = calendarView.findFirstVisibleMonth()!!
        val firstVisibleDay = calendarView.findFirstVisibleDay()!!
        val weekOfFirstDay = firstVisibleMonth.weekDays.first { it.any { it == firstVisibleDay } }
        assertTrue(weekOfFirstDay.first() == firstVisibleDay)

        // Last visible day is the last day in the week row it belongs. (bottom-right)
        val lastVisibleMonth = calendarView.findLastVisibleMonth()!!
        val lastVisibleDay = calendarView.findLastVisibleDay()!!
        val weekOfLastDate = lastVisibleMonth.weekDays.first { it.any { it == lastVisibleDay } }
        assertTrue(weekOfLastDate.last() == lastVisibleDay)
    }

    @Test
    fun findVisibleDaysAndMonthsWorksOnHorizontalOrientation() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(5,
            click()))

        val calendarView =
            findFragment<Example6Fragment>().findViewById<CalendarView>(R.id.exSixCalendar)

        homeScreenRule.runOnUiThread {
            // Scroll to a random date
            calendarView.scrollToDate(LocalDate.now().plusDays(120))
        }

        sleep(2000)

        // First visible day is the first day in the month column(day of week) where it belongs. (top-left)
        val firstVisibleMonth = calendarView.findFirstVisibleMonth()!!
        val firstVisibleDay = calendarView.findFirstVisibleDay()!!
        val daysWIthSameDayOfWeekAsFirstDay = firstVisibleMonth.weekDays.flatten()
            .filter { it.date.dayOfWeek == firstVisibleDay.date.dayOfWeek }
        assertTrue(daysWIthSameDayOfWeekAsFirstDay.first() == firstVisibleDay)

        // Last visible day is the last day in the month column(day of week) where it belongs. (bottom-right)
        val lastVisibleMonth = calendarView.findLastVisibleMonth()!!
        val lastVisibleDay = calendarView.findLastVisibleDay()!!
        val daysWIthSameDayOfWeekAsLastDay = lastVisibleMonth.weekDays.flatten()
            .filter { it.date.dayOfWeek == lastVisibleDay.date.dayOfWeek }
        assertTrue(daysWIthSameDayOfWeekAsLastDay.last() == lastVisibleDay)
    }

    @Test
    fun multipleSetupCallsRetainPositionIfCalendarHasBoundaries() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0,
            click()))

        val calendarView =
            findFragment<Example1Fragment>().findViewById<CalendarView>(R.id.exOneCalendar)

        val targetVisibleMonth = currentMonth.plusMonths(2)

        var targetVisibleCalMonth: CalendarMonth? = null
        calendarView.monthScrollListener = { month ->
            targetVisibleCalMonth = month
        }

        homeScreenRule.runOnUiThread {
            calendarView.smoothScrollToMonth(targetVisibleMonth)
        }

        sleep(5000) // Enough time for smooth scrolling animation.

        homeScreenRule.runOnUiThread {
            calendarView.setup(
                targetVisibleMonth.minusMonths(10),
                targetVisibleMonth.plusMonths(10),
                daysOfWeek().first()
            )
        }

        sleep(5000) // Enough time for setup to finish.

        assertTrue(calendarView.findFirstVisibleMonth() == targetVisibleCalMonth)
    }

    private inline fun <reified T : Fragment> findFragment(): T {
        return homeScreenRule.activity.supportFragmentManager
            .findFragmentByTag(T::class.java.simpleName) as T
    }

    private fun <T : View> Fragment.findViewById(@IdRes id: Int): T = requireView().findViewById(id)
    private fun LocalDate.asMonthDay(): CalendarDay = CalendarDay(this, DayPosition.MonthDate)
}
