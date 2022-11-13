package com.kizitonwose.calendar.sample

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.toSize
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.sample.compose.Example1Page
import com.kizitonwose.calendar.sample.compose.Example8Page
import com.kizitonwose.calendar.sample.shared.displayText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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

    @Test
    fun filledCalenderWithFooterWorksAsExpected() {
        composeTestRule.setContent {
            Example8Page()
        }

        val headerHeight = composeTestRule.onAllNodes(hasTestTag("MonthHeader"))
            .onFirst().size.height
        val footerHeight = composeTestRule.onAllNodes(hasTestTag("MonthFooter"))
            .onFirst().size.height
        val bodyHeight = composeTestRule.onAllNodes(hasTestTag("MonthBody"))
            .onFirst().size.height

        val monthHeight = composeTestRule.onNodeWithTag("Calendar")
            .size.height

        assertEquals(monthHeight, headerHeight + footerHeight + bodyHeight)
        assertNotEquals(0, headerHeight)
        assertNotEquals(0, footerHeight)
        assertNotEquals(0, bodyHeight)
    }
}

private val SemanticsNodeInteractionCollection.sizes: List<DpRect>
    get() {
        val nodes =
            fetchSemanticsNodes(errorMessageOnFail = "Failed to retrieve bounds of the node.")
        return nodes.map { node ->
            with(node.layoutInfo.density) {
                node.unclippedBoundsInRoot.let {
                    DpRect(it.left.toDp(), it.top.toDp(), it.right.toDp(), it.bottom.toDp())
                }
            }
        }
    }

private val SemanticsNodeInteraction.size: DpRect
    get() {
        val node = fetchSemanticsNode("Failed to retrieve bounds of the node.")
        return with(node.layoutInfo.density) {
            node.unclippedBoundsInRoot.let {
                DpRect(it.left.toDp(), it.top.toDp(), it.right.toDp(), it.bottom.toDp())
            }
        }
    }

private val SemanticsNode.unclippedBoundsInRoot: Rect
    get() {
        return if (layoutInfo.isPlaced) {
            Rect(positionInRoot, size.toSize())
        } else {
            Dp.Unspecified.value.let { Rect(it, it, it, it) }
        }
    }
