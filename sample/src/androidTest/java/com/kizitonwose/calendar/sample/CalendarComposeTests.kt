package com.kizitonwose.calendar.sample

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.sample.compose.Example1Page
import com.kizitonwose.calendar.sample.shared.displayText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.YearMonth

/**
 * These are UI behaviour tests.
 * The core logic tests are in the data project.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CalendarComposeTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val currentMonth = YearMonth.now()
    private val nextMonth = currentMonth.nextMonth
    private val previousMonth = currentMonth.previousMonth

    @Test
    fun programmaticForwardScrollWorksAsExpected() {
        composeTestRule.setContent {
            Example1Page()
        }

        composeTestRule.onNodeWithTag("MonthTitle").assertTextEquals(currentMonth.displayText())
        composeTestRule.onNodeWithContentDescription("Next").performClick()
        composeTestRule.onNodeWithTag("MonthTitle").assertTextEquals(nextMonth.displayText())
    }

    @Test
    fun programmaticBackwardScrollWorksAsExpected() {
        composeTestRule.setContent {
            Example1Page()
        }

        composeTestRule.onNodeWithTag("MonthTitle").assertTextEquals(currentMonth.displayText())
        composeTestRule.onNodeWithContentDescription("Previous").performClick()
        composeTestRule.onNodeWithTag("MonthTitle").assertTextEquals(previousMonth.displayText())
    }

    @Test
    fun pagedForwardSwipeWorksAsExpected() {
        composeTestRule.setContent {
            Example1Page()
        }

        composeTestRule.onNodeWithTag("MonthTitle").assertTextEquals(currentMonth.displayText())
        composeTestRule.onNodeWithTag("Calendar").performTouchInput { swipeLeft() }
        composeTestRule.onNodeWithTag("MonthTitle").assertTextEquals(nextMonth.displayText())
    }

    @Test
    fun pagedBackwardSwipeWorksAsExpected() {
        composeTestRule.setContent {
            Example1Page()
        }

        composeTestRule.onNodeWithTag("MonthTitle").assertTextEquals(currentMonth.displayText())
        composeTestRule.onNodeWithTag("Calendar").performTouchInput { swipeRight() }
        composeTestRule.onNodeWithTag("MonthTitle").assertTextEquals(previousMonth.displayText())
    }
}
