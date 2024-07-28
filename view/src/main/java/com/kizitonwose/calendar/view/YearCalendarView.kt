package com.kizitonwose.calendar.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.CalendarYear
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.data.checkRange
import com.kizitonwose.calendar.view.internal.CalendarPageSnapHelper
import com.kizitonwose.calendar.view.internal.CalendarPageSnapHelperLegacy
import com.kizitonwose.calendar.view.internal.monthcalendar.MonthCalendarLayoutManager
import com.kizitonwose.calendar.view.internal.yearcalendar.YearCalendarAdapter
import com.kizitonwose.calendar.view.internal.yearcalendar.YearCalendarLayoutManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

public open class YearCalendarView : RecyclerView {
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
     * The [MonthHeaderFooterBinder] instance used for managing header views.
     * The header view is shown above each month on the Calendar.
     */
    public var monthHeaderBinder: MonthHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The [MonthHeaderFooterBinder] instance used for managing footer views.
     * The footer view is shown below each month on the Calendar.
     */
    public var monthFooterBinder: MonthHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The [YearHeaderFooterBinder] instance used for managing header views.
     * The header view is shown above each year on the Calendar.
     */
    public var yearHeaderBinder: YearHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The [YearHeaderFooterBinder] instance used for managing footer views.
     * The footer view is shown below each year on the Calendar.
     */
    public var yearFooterBinder: YearHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * Called when the calendar scrolls to a new month.
     * Mostly beneficial if [scrollPaged] is `true`.
     */
    public var yearScrollListener: YearScrollListener? = null

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
     * The xml resource that is inflated and used as a header for every month.
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
     * The xml resource that is inflated and used as a footer for every month.
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
     * The xml resource that is inflated and used as a header for every year.
     * Set zero to disable.
     */
    public var yearHeaderResource: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The xml resource that is inflated and used as a footer for every year.
     * Set zero to disable.
     */
    public var yearFooterResource: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * A [ViewGroup] which is instantiated and used as the container for each year.
     * This class must have a constructor which takes only a [Context].
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
     * A [ViewGroup] which is instantiated and used as the container for each year.
     * This class must have a constructor which takes only a [Context].
     *
     * **You should exclude the name and constructor of this class from code
     * obfuscation if enabled**.
     */
    public var yearViewClass: String? = null
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    // TODO - YEAR doc
    @Px
    public var monthVerticalSpacing: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    @Px
    public var monthHorizontalSpacing: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    @IntRange(from = 1, to = 12)
    public var monthColumns: Int = 3
        set(value) {
            if (field != value) {
                require(value in 1..12) { "Month columns must be 1..12" }
                field = value
                invalidateViewHolders()
            }
        }

    public var isMonthVisible: (month: CalendarMonth) -> Boolean = { true }
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
     * Can be [DaySize.Square], [DaySize.SeventhWidth] or [DaySize.FreeForm].
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
    public var yearMargins: MarginValues = MarginValues()
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    private val scrollListenerInternal = object : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                calendarAdapter.notifyYearScrollListenerIfNeeded()
            }
        }
    }

    private val horizontalSnapHelper = CalendarPageSnapHelperLegacy()
    private val verticalSnapHelper = CalendarPageSnapHelper()
    private var pageSnapHelper: PagerSnapHelper = horizontalSnapHelper

    private var startYear: Year? = null
    private var endYear: Year? = null
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
            R.styleable.YearCalendarView,
            defStyleAttr,
            defStyleRes,
        ) {
            dayViewResource = getResourceId(
                R.styleable.YearCalendarView_cv_dayViewResource,
                dayViewResource,
            )
            monthHeaderResource = getResourceId(
                R.styleable.YearCalendarView_cv_monthHeaderResource,
                monthHeaderResource,
            )
            monthFooterResource = getResourceId(
                R.styleable.YearCalendarView_cv_monthFooterResource,
                monthFooterResource,
            )
            yearHeaderResource = getResourceId(
                R.styleable.YearCalendarView_cv_yearHeaderResource,
                yearHeaderResource,
            )
            yearFooterResource = getResourceId(
                R.styleable.YearCalendarView_cv_yearFooterResource,
                yearFooterResource,
            )
            orientation = getInt(R.styleable.YearCalendarView_cv_orientation, orientation)
            // Enable paged scrolling by default only for the horizontal calendar.
            scrollPaged = getBoolean(
                R.styleable.YearCalendarView_cv_scrollPaged,
                orientation == HORIZONTAL,
            )
            daySize = DaySize.entries[
                getInt(R.styleable.YearCalendarView_cv_daySize, daySize.ordinal),
            ]
            outDateStyle = OutDateStyle.entries[
                getInt(R.styleable.YearCalendarView_cv_outDateStyle, outDateStyle.ordinal),
            ]
            monthColumns = getInt(
                R.styleable.YearCalendarView_cv_monthColumns,
                monthColumns,
            )
            monthHorizontalSpacing = getDimensionPixelSize(
                R.styleable.YearCalendarView_cv_monthHorizontalSpacing,
                monthHorizontalSpacing,
            )
            monthVerticalSpacing = getDimensionPixelSize(
                R.styleable.YearCalendarView_cv_monthVerticalSpacing,
                monthVerticalSpacing,
            )
            monthViewClass = getString(R.styleable.YearCalendarView_cv_monthViewClass)
            yearViewClass = getString(R.styleable.YearCalendarView_cv_yearViewClass)
        }
        check(dayViewResource != 0) { "No value set for `cv_dayViewResource` attribute." }
    }

    private val calendarLayoutManager: YearCalendarLayoutManager
        get() = layoutManager as YearCalendarLayoutManager

    private val calendarAdapter: YearCalendarAdapter
        get() = adapter as YearCalendarAdapter

    private fun invalidateViewHolders() {
        // This does not remove visible views.
        // recycledViewPool.clear()

        // This removes all views but is internal.
        // removeAndRecycleViews()

        if (adapter == null || layoutManager == null) return
        val state = layoutManager?.onSaveInstanceState()
        adapter = adapter
        layoutManager?.onRestoreInstanceState(state)
        post { calendarAdapter.notifyYearScrollListenerIfNeeded() }
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
     * Scroll to a specific year on the calendar. This instantly
     * shows the view for the year without any animations.
     * For a smooth scrolling effect, use [smoothScrollToMonth]
     */
    public fun scrollToYear(year: Year) {
        calendarLayoutManager.scrollToIndex(year)
    }

    /**
     * Scroll to a specific year on the calendar using a smooth scrolling animation.
     * Just like [scrollToMonth], but with a smooth scrolling animation.
     */
    public fun smoothScrollToYear(year: Year) {
        calendarLayoutManager.smoothScrollToIndex(year)
    }

    /**
     * Scroll to a specific month on the calendar. This instantly
     * shows the view for the month without any animations.
     * For a smooth scrolling effect, use [smoothScrollToMonth]
     */
    public fun scrollToMonth(month: YearMonth) {
        calendarLayoutManager.scrollToMonth(month)
    }

    /**
     * Scroll to a specific month on the calendar using a smooth scrolling animation.
     * Just like [scrollToMonth], but with a smooth scrolling animation.
     */
    public fun smoothScrollToMonth(month: YearMonth) {
        calendarLayoutManager.smoothScrollToMonth(month)
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
     * Notify the CalendarView to reload the cell for this [CalendarDay]
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
     * Notify the CalendarView to reload the cells for this [LocalDate] in the
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
     * Notify the CalendarView to reload the view for this [YearMonth]
     * This causes the following sequence of events:
     * [MonthDayBinder.bind] will be called for all dates in this month.
     * [MonthHeaderFooterBinder.bind] will be called for this month's header view if available.
     * [MonthHeaderFooterBinder.bind] will be called for this month's footer view if available.
     */
    public fun notifyMonthChanged(month: YearMonth) {
        calendarAdapter.reloadMonth(month)
    }

    /**
     * Notify the CalendarView to reload the view for this [YearMonth]
     * This causes the following sequence of events:
     * [MonthDayBinder.bind] will be called for all dates in this month.
     * [MonthHeaderFooterBinder.bind] will be called for this month's header view if available.
     * [MonthHeaderFooterBinder.bind] will be called for this month's footer view if available.
     */
    public fun notifyYearChanged(year: Year) {
        calendarAdapter.reloadYear(year)
    }

    /**
     * Notify the CalendarView to reload all months.
     * @see [notifyMonthChanged].
     */
    public fun notifyCalendarChanged() {
        calendarAdapter.reloadCalendar()
    }

    /**
     * Find the first visible month on the CalendarView.
     *
     * @return The first visible month or null if not found.
     */
    public fun findFirstVisibleMonth(): CalendarMonth? {
        return calendarAdapter.findFirstVisibleMonth()
    }

    /**
     * Find the last visible month on the CalendarView.
     *
     * @return The last visible month or null if not found.
     */
    public fun findLastVisibleMonth(): CalendarMonth? {
        return calendarAdapter.findLastVisibleMonth()
    }

    /**
     * Find the first visible year on the CalendarView.
     *
     * @return The first visible year or null if not found.
     */
    public fun findFirstVisibleYear(): CalendarYear? {
        return calendarAdapter.findFirstVisibleYear()
    }

    /**
     * Find the last visible year on the CalendarView.
     *
     * @return The last visible year or null if not found.
     */
    public fun findLastVisibleYear(): CalendarYear? {
        return calendarAdapter.findLastVisibleYear()
    }

    /**
     * Find the first visible day on the CalendarView.
     * This is the day at the top-left corner of the calendar.
     *
     * @return The first visible day or null if not found.
     */
    public fun findFirstVisibleDay(): CalendarDay? {
        return calendarAdapter.findFirstVisibleDay()
    }

    /**
     * Find the last visible day on the CalendarView.
     * This is the day at the bottom-right corner of the calendar.
     *
     * @return The last visible day or null if not found.
     */
    public fun findLastVisibleDay(): CalendarDay? {
        return calendarAdapter.findLastVisibleDay()
    }

    /**
     * Setup the CalendarView.
     * See [updateMonthData] to update these values.
     *
     * @param startMonth The first month on the calendar.
     * @param endMonth The last month on the calendar.
     * @param firstDayOfWeek A [DayOfWeek] to be the first day of week.
     */
    public fun setup(startYear: Year, endYear: Year, firstDayOfWeek: DayOfWeek) {
        checkRange(start = startYear, end = endYear)
        this.startYear = startYear
        this.endYear = endYear
        this.firstDayOfWeek = firstDayOfWeek

        removeOnScrollListener(scrollListenerInternal)
        addOnScrollListener(scrollListenerInternal)

        layoutManager = YearCalendarLayoutManager(this)
        adapter = YearCalendarAdapter(
            calView = this,
            outDateStyle = outDateStyle,
            startYear = startYear,
            endYear = endYear,
            firstDayOfWeek = firstDayOfWeek,
        )
    }

    /**
     * Update the CalendarView's start month or end month or the first day of week.
     * This can be called only if you have called [setup] in the past.
     * The calendar can handle really large date ranges so you may want to setup
     * the calendar with a large date range instead of updating the range frequently.
     */
    @JvmOverloads
    public fun updateMonthData(
        startYear: Year = requireStartYear(),
        endYear: Year = requireEndYear(),
        firstDayOfWeek: DayOfWeek = requireFirstDayOfWeek(),
    ) {
        checkRange(start = startYear, end = endYear)
        this.startYear = startYear
        this.endYear = endYear
        this.firstDayOfWeek = firstDayOfWeek
        updateAdapter()
    }

    private fun updateAdapter() {
        calendarAdapter.updateData(
            startYear = requireStartYear(),
            endYear = requireEndYear(),
            outDateStyle = outDateStyle,
            firstDayOfWeek = requireFirstDayOfWeek(),
        )
    }

    private fun requireStartYear(): Year = startYear ?: throw getFieldException("startYear")

    private fun requireEndYear(): Year = endYear ?: throw getFieldException("endYear")

    private fun requireFirstDayOfWeek(): DayOfWeek =
        firstDayOfWeek ?: throw getFieldException("firstDayOfWeek")

    private fun getFieldException(field: String) =
        IllegalStateException("`$field` is not set. Have you called `setup()`?")
}
