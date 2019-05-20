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
import com.kizitonwose.calendarviewsample.Example5Fragment
import com.kizitonwose.calendarviewsample.HomeActivity
import com.kizitonwose.calendarviewsample.R
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
    fun testPagedScrollingWorksAsExpected() {
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