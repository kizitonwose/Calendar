package com.kizitonwose.calendarview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarcore.*
import com.kizitonwose.calendardata.checkDateRange
import com.kizitonwose.calendarview.internal.CalenderPageSnapHelper
import com.kizitonwose.calendarview.internal.MarginValues
import com.kizitonwose.calendarview.internal.weekcalendar.WeekCalendarAdapter
import com.kizitonwose.calendarview.internal.weekcalendar.WeekCalendarLayoutManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

open class WeekCalendarView : RecyclerView {

    /**
     * The [MonthDayBinder] instance used for managing day cell views
     * creation and reuse. Changing the binder means that the view
     * creation logic could have changed too. We refresh the Calender.
     */
    var dayBinder: WeekDayBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The [MonthHeaderFooterBinder] instance used for managing header views.
     * The header view is shown above each month on the Calendar.
     */
    var weekHeaderBinder: WeekHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The [WeekHeaderFooterBinder] instance used for managing footer views.
     * The footer view is shown below each month on the Calendar.
     */
    var weekFooterBinder: WeekHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * Called when the calender scrolls to a new month. Mostly beneficial
     * if [ScrollMode] is [ScrollMode.PAGED].
     */
    var weekScrollListener: WeekScrollListener? = null

    /**
     * The xml resource that is inflated and used as the day cell view.
     * This must be provided.
     */
    var dayViewResource = 0
        set(value) {
            if (field != value) {
                if (value == 0) throw IllegalArgumentException("'dayViewResource' attribute not provided.")
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The xml resource that is inflated and used as a header for every month.
     * Set zero to disable.
     */
    var weekHeaderResource = 0
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The xml resource that is inflated and used as a footer for every week.
     * Set zero to disable.
     */
    var weekFooterResource = 0
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * A [ViewGroup] which is instantiated and used as the background for each week.
     * This class must have a constructor which takes only a [Context]. You should
     * exclude the name and constructor of this class from code obfuscation if enabled.
     */
    var weekViewClass: String? = null
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The scrolling behavior of the calendar. If [ScrollMode.PAGED],
     * the calendar will snap to the nearest month after a scroll or swipe action.
     * If [ScrollMode.CONTINUOUS], the calendar scrolls normally.
     */
    var scrollPaged = true
        set(value) {
            if (field != value) {
                field = value
                pagerSnapHelper.attachToRecyclerView(if (scrollPaged) this else null)
            }
        }

    var daySize: DaySize = DaySize.Square
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The margins, in pixels to be applied each week view.
     * this can be used to add a space between two items.
     */
    var weekMargins = MarginValues()
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
                calendarAdapter.notifyWeekScrollListenerIfNeeded()
            }
        }
    }

    private val pagerSnapHelper = CalenderPageSnapHelper()

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private var firstDayOfWeek: DayOfWeek? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, defStyleAttr)
    }

    private fun init(attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        if (isInEditMode) return
        setHasFixedSize(true)
        context.withStyledAttributes(
            attributeSet,
            R.styleable.CalendarView,
            defStyleAttr,
            defStyleRes
        ) {
            dayViewResource =
                getResourceId(R.styleable.WeekCalendarView_cv_dayViewResource, dayViewResource)
            weekHeaderResource =
                getResourceId(R.styleable.WeekCalendarView_cv_weekHeaderResource,
                    weekHeaderResource)
            weekFooterResource =
                getResourceId(R.styleable.WeekCalendarView_cv_weekFooterResource,
                    weekFooterResource)
            scrollPaged = getBoolean(R.styleable.WeekCalendarView_cv_scrollPaged, scrollPaged)
            daySize = DaySize.values()[
                    getInt(R.styleable.WeekCalendarView_cv_daySize, daySize.ordinal)
            ]
            weekViewClass = getString(R.styleable.WeekCalendarView_cv_weekViewClass)
        }
        // Initial scroll setup since we check field when assigning and default value is `true`
        if (scrollPaged) pagerSnapHelper.attachToRecyclerView(this)
        check(dayViewResource != 0) { "No value set for `cv_dayViewResource` attribute." }
    }

    private val calendarLayoutManager: WeekCalendarLayoutManager
        get() = layoutManager as WeekCalendarLayoutManager

    private val calendarAdapter: WeekCalendarAdapter
        get() = adapter as WeekCalendarAdapter

    private fun invalidateViewHolders() {
        // This does not remove visible views.
        // recycledViewPool.clear()

        // This removes all views but is internal.
        // removeAndRecycleViews()

        if (adapter == null || layoutManager == null) return
        val state = layoutManager?.onSaveInstanceState()
        adapter = adapter
        layoutManager?.onRestoreInstanceState(state)
        post { calendarAdapter.notifyWeekScrollListenerIfNeeded() }
    }

    /**
     * Scroll to a specific month on the calendar. This only
     * shows the view for the month without any animations.
     * For a smooth scrolling effect, use [smoothScrollToMonth]
     */
    fun scrollToWeek(date: LocalDate) {
        calendarLayoutManager.scrollToIndex(date)
    }

    fun smoothScrollToWeek(date: LocalDate) {
        calendarLayoutManager.smoothScrollToIndex(date)
    }

    fun scrollToDate(date: LocalDate) {
        calendarLayoutManager.scrollToDay(date)
    }

    fun smoothScrollToDate(date: LocalDate) {
        calendarLayoutManager.smoothScrollToDay(date)
    }

    fun scrollToWeek(day: WeekDay) = scrollToWeek(day.date)

    fun smoothScrollToWeek(day: WeekDay) = smoothScrollToWeek(day.date)

    fun scrollToDay(day: WeekDay) = scrollToDate(day.date)

    fun smoothScrollToDay(day: WeekDay) = smoothScrollToDate(day.date)

    /**
     * Notify the CalendarView to reload the cell for this [CalendarDay]
     * This causes [MonthDayBinder.bind] to be called with the [ViewContainer]
     * at this position. Use this to reload a date cell on the Calendar.
     */
    fun notifyDateChanged(date: LocalDate) {
        calendarAdapter.reloadDay(date)
    }

    fun notifyDayChanged(day: WeekDay) = notifyDateChanged(day.date)


    /**
     * Notify the CalendarView to reload the view for this [YearMonth]
     * This causes the following sequence pf events:
     * [MonthDayBinder.bind] will be called for all dates in this month.
     * [MonthHeaderFooterBinder.bind] will be called for this month's header view if available.
     * [MonthHeaderFooterBinder.bind] will be called for this month's footer view if available.
     */
    fun notifyWeekChanged(date: LocalDate) {
        calendarAdapter.reloadWeek(date)
    }

    fun notifyWeekChanged(day: WeekDay) = notifyWeekChanged(day.date)

    /**
     * Notify the CalendarView to reload all months.
     * Just like calling [notifyMonthChanged] for all months.
     */
    fun notifyCalendarChanged() {
        calendarAdapter.reloadCalendar()
    }

    /**
     * Find the first visible month on the CalendarView.
     *
     * @return The first visible month or null if not found.
     */
    fun findFirstVisibleWeek(): List<WeekDay>? {
        return calendarAdapter.findFirstVisibleWeek()
    }

    /**
     * Find the last visible month on the CalendarView.
     *
     * @return The last visible month or null if not found.
     */
    fun findLastVisibleWeek(): List<WeekDay>? {
        return calendarAdapter.findLastVisibleWeek()
    }

    /**
     * Find the first visible day on the CalendarView.
     * This is the day at the top-left corner of the calendar.
     *
     * @return The first visible day or null if not found.
     */
    fun findFirstVisibleDay(): WeekDay? {
        return calendarAdapter.findFirstVisibleDay()
    }

    /**
     * Find the last visible day on the CalendarView.
     * This is the day at the bottom-right corner of the calendar.
     *
     * @return The last visible day or null if not found.
     */
    fun findLastVisibleDay(): WeekDay? {
        return calendarAdapter.findLastVisibleDay()
    }

    /**
     * Setup the CalendarView.
     * See [updateMonthData] to change the [startMonth] and [endMonth] values.
     *
     * @param startMonth The first month on the calendar.
     * @param endMonth The last month on the calendar.
     * @param firstDayOfWeek An instance of [DayOfWeek] enum to be the first day of week.
     */
    fun setup(startDate: LocalDate, endDate: LocalDate, firstDayOfWeek: DayOfWeek) {
        checkDateRange(startDate = startDate, endDate = endDate)
        this.startDate = startDate
        this.endDate = endDate
        this.firstDayOfWeek = firstDayOfWeek

        removeOnScrollListener(scrollListenerInternal)
        addOnScrollListener(scrollListenerInternal)

        layoutManager = WeekCalendarLayoutManager(this)
        adapter = WeekCalendarAdapter(
            calView = this,
            startDate = startDate,
            endDate = endDate,
            firstDayOfWeek = firstDayOfWeek,
        )
    }

    /**
     * Update the CalendarView's start or end month, and optionally the first day of week.
     * This can be called only if you have called [setup] in the past.
     * The calendar can handle really large date ranges so you may want to setup
     * the calendar with a large date range instead of updating the range frequently.
     */
    @JvmOverloads
    fun updateWeekData(
        startDate: LocalDate = requireStartDate(),
        endDate: LocalDate = requireEndDate(),
        firstDayOfWeek: DayOfWeek = requireFirstDayOfWeek(),
    ) {
        checkDateRange(startDate = startDate, endDate = endDate)
        this.startDate = startDate
        this.endDate = endDate
        this.firstDayOfWeek = firstDayOfWeek
        updateAdapter()
    }

    private fun updateAdapter() {
        calendarAdapter.updateData(
            startDate = requireStartDate(),
            endDate = requireEndDate(),
            firstDayOfWeek = requireFirstDayOfWeek(),
        )
    }

    private fun requireStartDate(): LocalDate = startDate ?: throw getFieldException("startDate")

    private fun requireEndDate(): LocalDate = endDate ?: throw getFieldException("endDate")

    private fun requireFirstDayOfWeek(): DayOfWeek =
        firstDayOfWeek ?: throw getFieldException("firstDayOfWeek")

    private fun getFieldException(field: String) =
        IllegalStateException("`$field` is not set. Have you called `setup()`?")
}
