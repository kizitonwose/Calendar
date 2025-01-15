package com.kizitonwose.calendar.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.data.checkRange
import com.kizitonwose.calendar.view.internal.CalendarPageSnapHelper
import com.kizitonwose.calendar.view.internal.CalendarPageSnapHelperLegacy
import com.kizitonwose.calendar.view.internal.missingField
import com.kizitonwose.calendar.view.internal.monthcalendar.MonthCalendarAdapter
import com.kizitonwose.calendar.view.internal.monthcalendar.MonthCalendarLayoutManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * A month-based calendar view.
 *
 * @see WeekCalendarView
 * @see YearCalendarView
 */
public open class CalendarView : RecyclerView {
    /**
     * The [MonthDayBinder] instance used for managing day
     * cell view creation and reuse on the calendar.
     */
    public var dayBinder: MonthDayBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The [MonthHeaderFooterBinder] instance used for managing the
     * header views shown above each month on the calendar.
     */
    public var monthHeaderBinder: MonthHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The [MonthHeaderFooterBinder] instance used for managing the
     * footer views shown below each month on the calendar.
     */
    public var monthFooterBinder: MonthHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * Called when the calendar scrolls to a new month.
     * Mostly beneficial if [scrollPaged] is `true`.
     */
    public var monthScrollListener: MonthScrollListener? = null

    /**
     * The xml resource that is inflated and used as the day cell view.
     * This must be provided.
     */
    public var dayViewResource: Int = 0
        set(value) {
            if (field != value) {
                check(value != 0) { "Invalid 'dayViewResource' value." }
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The xml resource that is inflated and used as a header for each month.
     * Set zero to disable.
     */
    public var monthHeaderResource: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The xml resource that is inflated and used as a footer for each month.
     * Set zero to disable.
     */
    public var monthFooterResource: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The fully qualified class name of a [ViewGroup] that is instantiated
     * and used as the container for each month. This class must have a
     * constructor which takes only a [Context].
     *
     * **You should exclude the name and constructor of this class from code
     * obfuscation if enabled**.
     */
    public var monthViewClass: String? = null
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The [RecyclerView.Orientation] used for the layout manager.
     * This determines the scroll direction of the calendar.
     */
    @Orientation
    public var orientation: Int = HORIZONTAL
        set(value) {
            if (field != value) {
                field = value
                (layoutManager as? MonthCalendarLayoutManager)?.orientation = value
                updateSnapHelper()
            }
        }

    /**
     * The scrolling behavior of the calendar. If `true`, the calendar will
     * snap to the nearest month after a scroll or swipe action.
     * If `false`, the calendar scrolls normally.
     */
    public var scrollPaged: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateSnapHelper()
            }
        }

    /**
     * Determines how outDates are generated for each month on the calendar.
     * Can be [OutDateStyle.EndOfRow] or [OutDateStyle.EndOfGrid].
     *
     * @see [DayPosition]
     */
    public var outDateStyle: OutDateStyle = OutDateStyle.EndOfRow
        set(value) {
            if (field != value) {
                field = value
                if (adapter != null) updateAdapter()
            }
        }

    /**
     * Determines how the size of each day on the calendar is calculated.
     * See the [DaySize] class documentation to understand each value.
     */
    public var daySize: DaySize = DaySize.Square
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The margins, in pixels to be applied on each month view.
     * this can be used to add a space between two months.
     */
    public var monthMargins: MarginValues = MarginValues.ZERO
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * Helper class with methods that can be overridden
     * in the internal layout manager.
     */
    public var layoutHelper: LayoutHelper? = null

    private val scrollListenerInternal = object : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                calendarAdapter.notifyMonthScrollListenerIfNeeded()
            }
        }
    }

    private val horizontalSnapHelper = CalendarPageSnapHelperLegacy()
    private val verticalSnapHelper = CalendarPageSnapHelper()
    private var pageSnapHelper: PagerSnapHelper = horizontalSnapHelper

    private var startMonth: YearMonth? = null
    private var endMonth: YearMonth? = null
    private var firstDayOfWeek: DayOfWeek? = null

    public constructor(context: Context) : super(context)

    public constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0, 0)
    }

    public constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, defStyleAttr)
    }

    private fun init(attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        if (isInEditMode) return
        itemAnimator = null
        setHasFixedSize(true)
        context.withStyledAttributes(
            attributeSet,
            R.styleable.CalendarView,
            defStyleAttr,
            defStyleRes,
        ) {
            dayViewResource = getResourceId(
                R.styleable.CalendarView_cv_dayViewResource,
                dayViewResource,
            )
            monthHeaderResource = getResourceId(
                R.styleable.CalendarView_cv_monthHeaderResource,
                monthHeaderResource,
            )
            monthFooterResource = getResourceId(
                R.styleable.CalendarView_cv_monthFooterResource,
                monthFooterResource,
            )
            orientation = getInt(R.styleable.CalendarView_cv_orientation, orientation)
            // Enable paged scrolling by default only for the horizontal calendar.
            scrollPaged = getBoolean(
                R.styleable.CalendarView_cv_scrollPaged,
                orientation == HORIZONTAL,
            )
            daySize = DaySize.entries[
                getInt(R.styleable.CalendarView_cv_daySize, daySize.ordinal),
            ]
            outDateStyle = OutDateStyle.entries[
                getInt(R.styleable.CalendarView_cv_outDateStyle, outDateStyle.ordinal),
            ]
            monthViewClass = getString(R.styleable.CalendarView_cv_monthViewClass)
        }
        check(dayViewResource != 0) { "No value set for `cv_dayViewResource` attribute." }
    }

    private val calendarLayoutManager: MonthCalendarLayoutManager
        get() = layoutManager as MonthCalendarLayoutManager

    private val calendarAdapter: MonthCalendarAdapter
        get() = adapter as MonthCalendarAdapter

    private fun invalidateViewHolders() {
        // This does not remove visible views.
        // recycledViewPool.clear()

        // This removes all views but is internal.
        // removeAndRecycleViews()

        if (adapter == null || layoutManager == null) return
        val state = layoutManager?.onSaveInstanceState()
        adapter = adapter
        layoutManager?.onRestoreInstanceState(state)
        post { calendarAdapter.notifyMonthScrollListenerIfNeeded() }
    }

    private fun updateSnapHelper() {
        if (!scrollPaged) {
            pageSnapHelper.attachToRecyclerView(null)
            return
        }
        if (
            (orientation == HORIZONTAL && pageSnapHelper !== horizontalSnapHelper) ||
            (orientation == VERTICAL && pageSnapHelper !== verticalSnapHelper)
        ) {
            // Remove the currently attached snap helper.
            pageSnapHelper.attachToRecyclerView(null)
            pageSnapHelper =
                if (orientation == HORIZONTAL) horizontalSnapHelper else verticalSnapHelper
        }
        pageSnapHelper.attachToRecyclerView(this)
    }

    /**
     * Scroll to a specific month on the calendar. This instantly
     * shows the view for the month without any animations.
     * For a smooth scrolling effect, use [smoothScrollToMonth]
     */
    public fun scrollToMonth(month: YearMonth) {
        calendarLayoutManager.scrollToIndex(month)
    }

    /**
     * Scroll to a specific month on the calendar using a smooth scrolling animation.
     * Just like [scrollToMonth], but with a smooth scrolling animation.
     */
    public fun smoothScrollToMonth(month: YearMonth) {
        calendarLayoutManager.smoothScrollToIndex(month)
    }

    /**
     * Scroll to a specific [CalendarDay]. This brings the date cell
     * view's top to the top of the CalendarVew in vertical mode or
     * the cell view's left edge to the left edge of the CalendarVew
     * in horizontal mode. No animation is performed.
     * For a smooth scrolling effect, use [smoothScrollToDay].
     */
    public fun scrollToDay(day: CalendarDay) {
        calendarLayoutManager.scrollToDay(day)
    }

    /**
     * Shortcut for [scrollToDay] with a [LocalDate] instance.
     */
    @JvmOverloads
    public fun scrollToDate(date: LocalDate, position: DayPosition = DayPosition.MonthDate) {
        scrollToDay(CalendarDay(date, position))
    }

    /**
     * Scroll to a specific [CalendarDay] using a smooth scrolling animation.
     * Just like [scrollToDay], but with a smooth scrolling animation.
     */
    public fun smoothScrollToDay(day: CalendarDay) {
        calendarLayoutManager.smoothScrollToDay(day)
    }

    /**
     * Shortcut for [smoothScrollToDay] with a [LocalDate] instance.
     */
    @JvmOverloads
    public fun smoothScrollToDate(date: LocalDate, position: DayPosition = DayPosition.MonthDate) {
        smoothScrollToDay(CalendarDay(date, position))
    }

    /**
     * Notify the calendar to reload the cell for this [CalendarDay]
     * This causes [MonthDayBinder.bind] to be called with the [ViewContainer]
     * at this position. Use this to reload a date cell on the Calendar.
     */
    public fun notifyDayChanged(day: CalendarDay) {
        calendarAdapter.reloadDay(day)
    }

    /**
     * Shortcut for [notifyDayChanged] with a [LocalDate] instance.
     */
    @JvmOverloads
    public fun notifyDateChanged(date: LocalDate, position: DayPosition = DayPosition.MonthDate) {
        notifyDayChanged(CalendarDay(date, position))
    }

    // This could replace the other `notifyDateChanged` with one DayPosition param if we add
    // the `JvmOverloads` annotation but that would break compatibility in places where the
    // method is called with named args: notifyDateChanged(date = *, position = DayPosition.*)
    // because assigning single elements to varargs in named form is not allowed.
    // May consider removing the other one at some point.

    /**
     * Notify the calendar to reload the cells for this [LocalDate] in the
     * specified day positions. This causes [MonthDayBinder.bind] to be called
     * with the [ViewContainer] at the relevant [DayPosition] values.
     */
    public fun notifyDateChanged(
        date: LocalDate,
        vararg position: DayPosition,
    ) {
        val days = position
            .ifEmpty { arrayOf(DayPosition.MonthDate) }
            .map { CalendarDay(date, it) }
            .toSet()
        calendarAdapter.reloadDay(*days.toTypedArray())
    }

    /**
     * Notify the calendar to reload the view for this [month].
     *
     * This causes the following sequence of events:
     * - [MonthHeaderFooterBinder.bind] will be called for this month's header view if available.
     * - [MonthDayBinder.bind] will be called for all dates in this month.
     * - [MonthHeaderFooterBinder.bind] will be called for this month's footer view if available.
     */
    public fun notifyMonthChanged(month: YearMonth) {
        calendarAdapter.reloadMonth(month)
    }

    /**
     * Notify the calendar to reload all months.
     * @see [notifyMonthChanged].
     */
    public fun notifyCalendarChanged() {
        calendarAdapter.reloadCalendar()
    }

    /**
     * Find the first visible month on the calendar.
     *
     * @return The first visible month or null if not found.
     */
    public fun findFirstVisibleMonth(): CalendarMonth? {
        return calendarAdapter.findFirstVisibleMonth()
    }

    /**
     * Find the last visible month on the calendar.
     *
     * @return The last visible month or null if not found.
     */
    public fun findLastVisibleMonth(): CalendarMonth? {
        return calendarAdapter.findLastVisibleMonth()
    }

    /**
     * Find the first visible day on the calendar.
     * This is the day at the top-left corner of the calendar.
     *
     * @return The first visible day or null if not found.
     */
    public fun findFirstVisibleDay(): CalendarDay? {
        return calendarAdapter.findFirstVisibleDay()
    }

    /**
     * Find the last visible day on the calendar.
     * This is the day at the bottom-right corner of the calendar.
     *
     * @return The last visible day or null if not found.
     */
    public fun findLastVisibleDay(): CalendarDay? {
        return calendarAdapter.findLastVisibleDay()
    }

    /**
     * Setup the calendar.
     * See [updateMonthData] to update these values.
     *
     * @param startMonth The first month on the calendar.
     * @param endMonth The last month on the calendar.
     * @param firstDayOfWeek A [DayOfWeek] to be the first day of week.
     *
     * @see [daysOfWeek]
     * @see [firstDayOfWeekFromLocale]
     */
    public fun setup(startMonth: YearMonth, endMonth: YearMonth, firstDayOfWeek: DayOfWeek) {
        checkRange(start = startMonth, end = endMonth)

        this.startMonth = startMonth
        this.endMonth = endMonth
        this.firstDayOfWeek = firstDayOfWeek

        removeOnScrollListener(scrollListenerInternal)
        addOnScrollListener(scrollListenerInternal)

        layoutManager = MonthCalendarLayoutManager(this)
        adapter = MonthCalendarAdapter(
            calView = this,
            outDateStyle = outDateStyle,
            startMonth = startMonth,
            endMonth = endMonth,
            firstDayOfWeek = firstDayOfWeek,
        )
    }

    /**
     * Update the calendar's start month or end month or the first day of week.
     * This can be called only if you have previously called [setup].
     * The calendar can handle really large date ranges so you may want to setup
     * the calendar with a large date range instead of updating the range frequently.
     */
    @JvmOverloads
    public fun updateMonthData(
        startMonth: YearMonth = requireStartMonth(),
        endMonth: YearMonth = requireEndMonth(),
        firstDayOfWeek: DayOfWeek = requireFirstDayOfWeek(),
    ) {
        checkRange(start = startMonth, end = endMonth)
        this.startMonth = startMonth
        this.endMonth = endMonth
        this.firstDayOfWeek = firstDayOfWeek
        updateAdapter()
    }

    private fun updateAdapter() {
        calendarAdapter.updateData(
            startMonth = requireStartMonth(),
            endMonth = requireEndMonth(),
            outDateStyle = outDateStyle,
            firstDayOfWeek = requireFirstDayOfWeek(),
        )
    }

    private fun requireStartMonth(): YearMonth = checkNotNull(startMonth) { missingField("startMonth") }

    private fun requireEndMonth(): YearMonth = checkNotNull(endMonth) { missingField("endMonth") }

    private fun requireFirstDayOfWeek(): DayOfWeek = checkNotNull(firstDayOfWeek) { missingField("firstDayOfWeek") }
}
