package com.kizitonwose.calendar.sample

import android.view.View
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.sample.utils.TestDayViewContainer
import com.kizitonwose.calendar.sample.utils.getView
import com.kizitonwose.calendar.sample.utils.openExampleAt
import com.kizitonwose.calendar.sample.utils.runOnMain
import com.kizitonwose.calendar.sample.view.CalendarViewActivity
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import com.kizitonwose.calendar.view.WeekHeaderFooterBinder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep
import java.time.YearMonth

/**
 * These are UI behaviour tests.
 * The core logic tests are in the data project.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class WeekCalendarViewTests {

    @get:Rule
    val homeScreenRule = ActivityScenarioRule(CalendarViewActivity::class.java)

    private val currentMonth = YearMonth.now()

    @Test
    fun dayBinderIsCalledOnDayChanged() {
        val calendarView = openAndGetWeekCalendarView()

        class DayViewContainer(view: View) : ViewContainer(view)

        var boundDay: WeekDay? = null

        val changedDate = currentMonth.atDay(4)

        runOnMain {
            calendarView.scrollToDate(changedDate)
            calendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: WeekDay) {
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
    }

    @Test
    fun allBindersAreCalledOnWeekChanged() {
        val calendarView = openAndGetWeekCalendarView()

        val boundDays = mutableSetOf<WeekDay>()
        var boundHeaderWeek: Week? = null

        runOnMain {
            calendarView.dayBinder = object : WeekDayBinder<TestDayViewContainer> {
                override fun create(view: View) = TestDayViewContainer(view)
                override fun bind(container: TestDayViewContainer, data: WeekDay) {
                    boundDays.add(data)
                }
            }
            calendarView.weekHeaderResource = R.layout.example_3_calendar_header
            calendarView.weekHeaderBinder = object : WeekHeaderFooterBinder<TestDayViewContainer> {
                override fun create(view: View) = TestDayViewContainer(view)
                override fun bind(container: TestDayViewContainer, data: Week) {
                    boundHeaderWeek = data
                }
            }
        }

        // Allow the calendar to be rebuilt due to binder change.
        sleep(2000)

        val firstDate = calendarView.findFirstVisibleDay()!!.date
        val lastDate = calendarView.findLastVisibleDay()!!.date

        runOnMain {
            boundDays.clear()
            boundHeaderWeek = null
            calendarView.notifyWeekChanged(firstDate)
        }

        // Allow time for date change event to be propagated.
        sleep(2000)

        assertEquals(firstDate, boundHeaderWeek?.days?.first()?.date)
        assertEquals(lastDate, boundHeaderWeek?.days?.last()?.date)
        assertTrue(boundDays.map { it.date }.contains(firstDate))
        assertTrue(boundDays.map { it.date }.contains(lastDate))
    }

    @Test
    fun programmaticScrollToDateWorksAsExpected() {
        val calendarView = openAndGetWeekCalendarView()

        val dateInFourMonths = currentMonth.plusMonths(4).atDay(1)

        runOnMain {
            calendarView.scrollToDate(dateInFourMonths)
        }

        sleep(2000)

        assertNotNull(calendarView.findViewWithTag(dateInFourMonths.hashCode()))
    }

    @Test
    fun programmaticScrollToWeekWorksAsExpected() {
        val calendarView = openAndGetWeekCalendarView()

        val dateInFourMonths = currentMonth.plusMonths(4).atDay(1)

        runOnMain {
            calendarView.scrollToWeek(dateInFourMonths)
        }

        sleep(2000)

        assertNotNull(calendarView.findViewWithTag(dateInFourMonths.hashCode()))
    }

    @Test
    fun weekScrollListenerIsCalledWhenScrolled() {
        val calendarView = openAndGetWeekCalendarView()

        var targetWeek: Week? = null
        calendarView.weekScrollListener = { weekDays ->
            targetWeek = weekDays
        }

        val dateInTwoMonths = currentMonth.plusMonths(2).atDay(1)
        runOnMain {
            calendarView.smoothScrollToWeek(dateInTwoMonths)
        }
        sleep(3000) // Enough time for smooth scrolling animation.
        assertTrue(targetWeek?.days.orEmpty().map { it.date }.contains(dateInTwoMonths))
    }

    @Test
    fun findVisibleWeekWorksAsExpected() {
        val calendarView = openAndGetWeekCalendarView()

        val dateInTwoMonths = currentMonth.plusMonths(2).atDay(1)
        runOnMain {
            calendarView.scrollToDate(dateInTwoMonths)
        }

        sleep(2000)

        val firstVisibleWeek = calendarView.findFirstVisibleWeek()?.days.orEmpty().map { it.date }
        assertTrue(firstVisibleWeek.contains(dateInTwoMonths))
    }

    @Test
    fun weekDataUpdateRetainsPosition() {
        val calendarView = openAndGetWeekCalendarView()

        val dateInTwoMonths = currentMonth.plusMonths(2).atDay(1)
        val dateInNineMonths = currentMonth.plusMonths(9).atDay(1)

        var targetVisibleDay: WeekDay? = null
        calendarView.weekScrollListener = { week ->
            targetVisibleDay = week.days.first()
        }

        runOnMain {
            calendarView.smoothScrollToWeek(dateInTwoMonths)
        }

        sleep(3000) // Enough time for smooth scrolling animation.

        runOnMain {
            calendarView.updateWeekData(endDate = dateInNineMonths)
        }

        sleep(2000) // Enough time for UI adjustments.

        assertEquals(targetVisibleDay, calendarView.findFirstVisibleDay())
    }

    private fun openAndGetWeekCalendarView(): WeekCalendarView {
        openExampleAt(6)
        return homeScreenRule.getView<WeekCalendarView>(R.id.exSevenCalendar)
    }
}
