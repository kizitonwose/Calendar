package com.kizitonwose.calenderviewsample

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.kizitonwose.calendarview.CalendarView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.YearMonth
import java.lang.Thread.sleep
import android.graphics.Rect
import android.util.Log
import com.kizitonwose.calendarview.adapter.MonthViewHolder
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarviewsample.*
import kotlinx.android.synthetic.main.exmaple_2_fragment.*
import kotlinx.android.synthetic.main.exmaple_6_fragment.*


@RunWith(AndroidJUnit4::class)
@LargeTest
class CalenderViewTests {

    @get:Rule
    private val homeScreenRule = ActivityTestRule<HomeActivity>(HomeActivity::class.java, true, false)

    @Before
    fun setup() {
        homeScreenRule.launchActivity(null)
    }

    @After
    fun teardown() {

    }

    @Test
    fun testProgrammaticScrollWorksAsExpected() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(4, click()))

        val currentMonth = YearMonth.now()

        val fragment = getFragment(Example5Fragment::class.java)

        val calendarView = fragment.requireView().findViewById<CalendarView>(R.id.exFiveCalendar)

        assertTrue(calendarView.getFirstVisibleMonth()?.yearMonth == currentMonth)

        val nextFourMonths = currentMonth.plusMonths(4)

        homeScreenRule.runOnUiThread {
            calendarView.scrollToMonth(nextFourMonths)
        }

        sleep(500)

        assertTrue(calendarView.getFirstVisibleMonth()?.yearMonth == nextFourMonths)
    }


    @Test
    fun testScrollToDateWorksOnVerticalOrientation() {
        onView(withId(R.id.examplesRv)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        val currentMonth = YearMonth.now()

        val fragment = getFragment(Example2Fragment::class.java)

        val targetDate = currentMonth.plusMonths(4).atDay(20)

        val calendarView = fragment.exTwoCalendar

        homeScreenRule.runOnUiThread {
            calendarView.scrollToDate(targetDate)
        }

        sleep(500)

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

        val currentMonth = YearMonth.now()

        val fragment = getFragment(Example6Fragment::class.java)

        val targetDate = currentMonth.plusMonths(3).atDay(18)

        val calendarView = fragment.exSixCalendar

        homeScreenRule.runOnUiThread {
            calendarView.scrollToDate(targetDate)
        }

        sleep(500)

        val vhForDateMonth = calendarView.findViewHolderForAdapterPosition(13) as MonthViewHolder
        val dayView = vhForDateMonth.bodyLayout.findViewById<View>(targetDate.hashCode())

        val calendarViewRect = Rect()
        calendarView.getGlobalVisibleRect(calendarViewRect)

        val dayViewRect = Rect()
        dayView.getGlobalVisibleRect(dayViewRect)

        assertTrue(calendarViewRect.left == dayViewRect.left)
    }

    private fun <T : Fragment> getFragment(clazz: Class<T>): T {
        return homeScreenRule.activity.supportFragmentManager
            .findFragmentByTag(clazz.simpleName) as T
    }
}


fun atPosition(position: Int, matcher: Matcher<View>): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("has object at position: $position")
            matcher.describeTo(description)
        }

        override fun matchesSafely(item: RecyclerView): Boolean {
            val holder = item.findViewHolderForAdapterPosition(position) ?: return false
            return matcher.matches(holder.itemView)
        }
    }
}