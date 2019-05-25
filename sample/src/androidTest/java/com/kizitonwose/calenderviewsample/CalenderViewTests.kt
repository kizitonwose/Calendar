package com.kizitonwose.calenderviewsample

import android.graphics.Rect
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.kizitonwose.calendarview.adapter.DayBinder
import com.kizitonwose.calendarview.adapter.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.adapter.MonthViewHolder
import com.kizitonwose.calendarview.adapter.ViewContainer
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.utils.yearMonth
import com.kizitonwose.calendarviewsample.*
import kotlinx.android.synthetic.main.exmaple_1_fragment.*
import kotlinx.android.synthetic.main.exmaple_2_fragment.*
import kotlinx.android.synthetic.main.exmaple_5_fragment.*
import kotlinx.android.synthetic.main.exmaple_6_fragment.*
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.YearMonth
import java.lang.Thread.sleep


// Core functionality tests are in the library project.
@RunWith(AndroidJUnit4::class)
@LargeTest
class CalenderViewTests {

    @get:Rule
    private val homeScreenRule = ActivityTestRule<HomeActivity>(HomeActivity::class.java, true, false)

    private val currentMonth = YearMonth.now()

    @Before
    fun setup() {
        homeScreenRule.launchActivity(null)
    }

    @After
    fun teardown() {

    }

    @Test
    fun testDayBinderIsCalledOnDayChanged() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        class DayViewContainer(view: View) : ViewContainer(view)

        val calendarView = findFragment(Example1Fragment::class.java).exOneCalendar

        var boundDay: CalendarDay? = null

        val changedDate = currentMonth.atDay(4)

        homeScreenRule.runOnUiThread {
            calendarView.dayBinder = object : DayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    boundDay = day
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
        assertTrue(boundDay?.owner == DayOwner.THIS_MONTH)
    }

    @Test
    fun testAllBindersAreCalledOnMonthChanged() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        class TestViewContainer(view: View) : ViewContainer(view)

        val calendarView = findFragment(Example2Fragment::class.java).exTwoCalendar

        val boundDays = mutableSetOf<CalendarDay>()
        var boundHeaderMonth: CalendarMonth? = null

        homeScreenRule.runOnUiThread {
            calendarView.dayBinder = object : DayBinder<TestViewContainer> {
                override fun create(view: View) = TestViewContainer(view)
                override fun bind(container: TestViewContainer, day: CalendarDay) {
                    boundDays.add(day)
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
        assertTrue(boundDays.count { it.owner == DayOwner.THIS_MONTH && it.date.yearMonth == currentMonth } == currentMonth.lengthOfMonth())
    }

    @Test
    fun testProgrammaticScrollWorksAsExpected() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(4, click()))

        val calendarView = findFragment(Example5Fragment::class.java).exFiveCalendar

        assertTrue(calendarView.getFirstVisibleMonth()?.yearMonth == currentMonth)

        val nextFourMonths = currentMonth.plusMonths(4)

        homeScreenRule.runOnUiThread {
            calendarView.scrollToMonth(nextFourMonths)
        }

        sleep(2000)

        assertTrue(calendarView.getFirstVisibleMonth()?.yearMonth == nextFourMonths)
    }


    @Test
    fun testScrollToDateWorksOnVerticalOrientation() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        val calendarView = findFragment(Example2Fragment::class.java).exTwoCalendar

        val targetDate = currentMonth.plusMonths(4).atDay(20)

        homeScreenRule.runOnUiThread {
            calendarView.scrollToDate(targetDate)
        }

        sleep(2000)

        val vhForDateMonth = calendarView.findViewHolderForAdapterPosition(4) as MonthViewHolder
        val dayView = vhForDateMonth.bodyLayout.findViewById<View>(targetDate.hashCode())

        val calendarViewRect = Rect()
        calendarView.getGlobalVisibleRect(calendarViewRect)

        val dayViewRect = Rect()
        dayView.getGlobalVisibleRect(dayViewRect)

        assertTrue(calendarViewRect.top == dayViewRect.top)
    }

    @Test
    fun testScrollToDateWorksOnHorizontalOrientation() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(5, click()))

        val calendarView = findFragment(Example6Fragment::class.java).exSixCalendar

        val targetDate = currentMonth.plusMonths(3).atDay(18)

        homeScreenRule.runOnUiThread {
            calendarView.scrollToDate(targetDate)
        }

        sleep(2000)

        val vhForDateMonth = calendarView.findViewHolderForAdapterPosition(13) as MonthViewHolder
        val dayView = vhForDateMonth.bodyLayout.findViewById<View>(targetDate.hashCode())

        val calendarViewRect = Rect()
        calendarView.getGlobalVisibleRect(calendarViewRect)

        val dayViewRect = Rect()
        dayView.getGlobalVisibleRect(dayViewRect)

        assertTrue(calendarViewRect.left == dayViewRect.left)
    }

    @Test
    fun monthScrollListenerIsCalledWhenScrolled() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        val calendarView = findFragment(Example1Fragment::class.java).exOneCalendar

        val targetMonth = currentMonth.plusMonths(2)

        var targetCalMonth: CalendarMonth? = null
        calendarView.monthScrollListener = { month ->
            targetCalMonth = month
        }

        homeScreenRule.runOnUiThread {
            calendarView.smoothScrollToMonth(targetMonth)
        }

        sleep(5000) // Enough time for smooth scrolling animation.

        assertTrue(targetCalMonth?.yearMonth == targetMonth)
    }

    private fun <T : Fragment> findFragment(clazz: Class<T>): T {
        return homeScreenRule.activity.supportFragmentManager
            .findFragmentByTag(clazz.simpleName) as T
    }
}
