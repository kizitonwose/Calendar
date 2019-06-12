package com.kizitonwose.calendarview

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec.UNSPECIFIED
import androidx.annotation.Px
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kizitonwose.calendarview.model.*
import com.kizitonwose.calendarview.ui.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

class CalendarView : RecyclerView {

    /**
     * The [DayBinder] instance used for managing day cell views
     * creation and reuse. Changing the binder means that the view
     * creation logic could have changed too. We refresh the Calender.
     */
    var dayBinder: DayBinder<*>? = null
        set(value) {
            val oldValue = field
            field = value
            if (oldValue != null) {
                invalidateViewHolders()
            }
        }

    /**
     * The [MonthHeaderFooterBinder] instance used for managing header views.
     * The header view is shown above each month on the Calendar.
     */
    var monthHeaderBinder: MonthHeaderFooterBinder<*>? = null
        set(value) {
            val oldValue = field
            field = value
            if (oldValue != null) {
                invalidateViewHolders()
            }
        }

    /**
     * The [MonthHeaderFooterBinder] instance used for managing footer views.
     * The footer view is shown below each month on the Calendar.
     */
    var monthFooterBinder: MonthHeaderFooterBinder<*>? = null
        set(value) {
            val oldValue = field
            field = value
            if (oldValue != null) {
                invalidateViewHolders()
            }
        }

    /**
     * Called when the calender scrolls to a new month. Mostly beneficial
     * if [ScrollMode] is [ScrollMode.PAGED].
     */
    var monthScrollListener: MonthScrollListener? = null

    var dayViewRes = 0
        set(value) {
            if (field != value) {
                field = value
                updateAdapterViewConfig()
            }
        }

    var monthHeaderRes = 0
        set(value) {
            if (field != value) {
                field = value
                updateAdapterViewConfig()
            }
        }

    var monthFooterRes = 0
        set(value) {
            if (field != value) {
                field = value
                updateAdapterViewConfig()
            }
        }

    @Orientation
    var orientation = VERTICAL
        set(value) {
            if (field != value) {
                field = value
                setup(startMonth ?: return, endMonth ?: return, firstDayOfWeek ?: return)
            }
        }

    var scrollMode = ScrollMode.CONTINUOUS
        set(value) {
            if (field != value) {
                field = value
                pagerSnapHelper.attachToRecyclerView(if (value == ScrollMode.PAGED) this else null)
            }
        }

    var inDateStyle = InDateStyle.ALL_MONTHS
        set(value) {
            if (field != value) {
                field = value
                updateAdapterConfig()
            }
        }

    var outDateStyle = OutDateStyle.END_OF_ROW
        set(value) {
            if (field != value) {
                field = value
                updateAdapterConfig()
            }
        }

    var maxRowCount = 6
        set(value) {
            if (field != value) {
                field = value
                updateAdapterConfig()
            }
        }


    private var startMonth: YearMonth? = null
    private var endMonth: YearMonth? = null
    private var firstDayOfWeek: DayOfWeek? = null
    private var monthViewClass: String? = null

    private var autoSize = true
    private var sizedInternally = false

    internal val isVertical: Boolean
        get() = orientation == VERTICAL

    internal val isHorizontal: Boolean
        get() = !isVertical

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, 0)
    }

    private fun init(attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        if (isInEditMode) return
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.CalendarView, defStyleAttr, defStyleRes)
        dayViewRes = resNotZero(a.getResourceId(R.styleable.CalendarView_cv_dayViewResource, dayViewRes))
        monthHeaderRes = a.getResourceId(R.styleable.CalendarView_cv_monthHeaderResource, monthHeaderRes)
        monthFooterRes = a.getResourceId(R.styleable.CalendarView_cv_monthFooterResource, monthFooterRes)
        orientation = a.getInt(R.styleable.CalendarView_cv_orientation, orientation)
        scrollMode = ScrollMode.values()[a.getInt(R.styleable.CalendarView_cv_scrollMode, scrollMode.ordinal)]
        outDateStyle = OutDateStyle.values()[a.getInt(R.styleable.CalendarView_cv_outDateStyle, outDateStyle.ordinal)]
        inDateStyle = InDateStyle.values()[a.getInt(R.styleable.CalendarView_cv_inDateStyle, inDateStyle.ordinal)]
        maxRowCount = a.getInt(R.styleable.CalendarView_cv_maxRowCount, maxRowCount)
        monthViewClass = a.getString(R.styleable.CalendarView_cv_monthViewClass)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (autoSize && isInEditMode.not()) {
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)

            if (widthMode == UNSPECIFIED && heightMode == UNSPECIFIED) {
                throw UnsupportedOperationException("Cannot calculate the values for day Width/Height with the current configuration.")
            }

            // +0.5 => round to the nearest pixel
            val squareSize = (((widthSize - (monthPaddingStart + monthPaddingEnd)) / 7f) + 0.5).toInt()
            if (dayWidth != squareSize || dayHeight != squareSize) {
                sizedInternally = true
                dayWidth = squareSize
                dayHeight = squareSize
                sizedInternally = false
                invalidateViewHolders()
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    /**
     * The width, in pixels for each day cell view.
     * Set this to [DAY_SIZE_SQUARE] to have a nice
     * square item view.
     *
     * @see [DAY_SIZE_SQUARE]
     */
    @Px
    var dayWidth: Int = DAY_SIZE_SQUARE
        set(value) {
            field = value
            if (sizedInternally.not()) {
                autoSize = value == DAY_SIZE_SQUARE
                invalidateViewHolders()
            }
        }

    /**
     * The height, in pixels for each day cell view.
     * Set this to [DAY_SIZE_SQUARE] to have a nice
     * square item view.
     *
     * @see [DAY_SIZE_SQUARE]
     */
    @Px
    var dayHeight: Int = DAY_SIZE_SQUARE
        set(value) {
            field = value
            if (sizedInternally.not()) {
                autoSize = value == DAY_SIZE_SQUARE
                invalidateViewHolders()
            }
        }

    /**
     * The padding, in pixels to be applied
     * to the start of each month view.
     */
    @Px
    var monthPaddingStart = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The padding, in pixels to be applied
     * to the end of each month view.
     */
    @Px
    var monthPaddingEnd = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The padding, in pixels to be applied
     * to the top of each month view.
     */
    @Px
    var monthPaddingTop = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The padding, in pixels to be applied
     * to the bottom of each month view.
     */
    @Px
    var monthPaddingBottom = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The margin, in pixels to be applied
     * to the start of each month view.
     */
    @Px
    var monthMarginStart = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The margin, in pixels to be applied
     * to the end of each month view.
     */
    @Px
    var monthMarginEnd = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The margin, in pixels to be applied
     * to the top of each month view.
     */
    @Px
    var monthMarginTop = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The margin, in pixels to be applied
     * to the bottom of each month view.
     */
    @Px
    var monthMarginBottom = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    private val calendarLayoutManager: CalendarLayoutManager
        get() = layoutManager as CalendarLayoutManager

    private val calendarAdapter: CalendarAdapter
        get() = adapter as CalendarAdapter

    private fun invalidateViewHolders() {
        // This does not remove visible views.
        // recycledViewPool.clear()

        // This removes all views but is internal.
        // removeAndRecycleViews()

        if (adapter == null || layoutManager == null) return
        val state = layoutManager?.onSaveInstanceState()
        adapter = adapter
        layoutManager?.onRestoreInstanceState(state)
    }

    private fun updateAdapterConfig() {
        if (adapter != null) {
            calendarAdapter.monthConfig =
                MonthConfig(
                    outDateStyle, inDateStyle, maxRowCount,
                    startMonth ?: return, endMonth ?: return, firstDayOfWeek ?: return
                )
            calendarAdapter.notifyDataSetChanged()
        }
    }

    private fun updateAdapterViewConfig() {
        if (adapter != null) {
            calendarAdapter.viewConfig = ViewConfig(dayViewRes, monthHeaderRes, monthFooterRes, monthViewClass)
            invalidateViewHolders()
        }
    }

    /**
     * Scroll to a specific month on the calendar. This only
     * shows the view for the month without any animations.
     * For a smooth scrolling effect, use [smoothScrollToMonth]
     */
    fun scrollToMonth(month: YearMonth) {
        calendarLayoutManager.scrollToMonth(month)
    }

    /**
     * Scroll to a specific month on the calendar using a smooth scrolling animation.
     * Just like [scrollToMonth], but with a smooth scrolling animation.
     */
    fun smoothScrollToMonth(month: YearMonth) {
        calendarLayoutManager.smoothScrollToMonth(month)
    }

    /**
     * Scroll to a specific [CalendarDay]. This brings the date cell
     * view's top to the top of the CalendarVew in vertical mode or
     * the cell view's left edge to the left edge of the CalendarVew
     * in horizontal mode. No animation is performed.
     * For a smooth scrolling effect, use [smoothScrollToDay].
     */
    fun scrollToDay(day: CalendarDay) {
        calendarLayoutManager.scrollToDay(day)
    }

    /**
     * Shortcut for [scrollToDay] with a [CalendarDay] instance
     * which has a [DayOwner.THIS_MONTH] property.
     */
    fun scrollToDate(date: LocalDate) {
        scrollToDay(CalendarDay(date, DayOwner.THIS_MONTH))
    }

    /**
     * Scroll to a specific [CalendarDay] using a smooth scrolling animation.
     * Just like [scrollToDay], but with a smooth scrolling animation.
     */
    fun smoothScrollToDay(day: CalendarDay) {
        calendarLayoutManager.smoothScrollToDay(day)
    }

    /**
     * Shortcut for [smoothScrollToDay] with a [CalendarDay]
     * instance which has a [DayOwner.THIS_MONTH] property.
     */
    fun smoothScrollToDate(date: LocalDate) {
        smoothScrollToDay(CalendarDay(date, DayOwner.THIS_MONTH))
    }

    /**
     * Notify the CalendarView to reload the cell for this [CalendarDay]
     * This causes [DayBinder.bind] to be called with the [ViewContainer]
     * at this position. Use this to reload a date cell on the Calendar.
     */
    fun notifyDayChanged(day: CalendarDay) {
        calendarAdapter.reloadDay(day)
    }

    /**
     * Shortcut for [notifyDayChanged] with a [CalendarDay] instance
     * which has a [DayOwner.THIS_MONTH] property.
     */
    fun notifyDateChanged(date: LocalDate) {
        notifyDayChanged(CalendarDay(date, DayOwner.THIS_MONTH))
    }

    /**
     * Notify the CalendarView to reload multiple dates.
     * @see [notifyDateChanged]
     * @see [notifyDayChanged]
     */
    fun notifyDatesChanged(vararg dates: LocalDate) {
        dates.forEach {
            notifyDateChanged(it)
        }
    }

    /**
     * Notify the CalendarView to reload the view for this [YearMonth]
     * This causes the following sequence pf events:
     * [DayBinder.bind] will be called for all dates in this month.
     * [MonthHeaderFooterBinder.bind] will be called for this month's header view if available.
     * [MonthHeaderFooterBinder.bind] will be called for this month's footer view if available.
     */
    fun notifyMonthChanged(month: YearMonth) {
        calendarAdapter.reloadMonth(month)
    }

    /**
     * Notify the CalendarView to reload all months.
     * Essentially calls [RecyclerView.Adapter.notifyDataSetChanged] on the adapter.
     */
    fun notifyCalendarChanged() {
        calendarAdapter.notifyDataSetChanged()
    }

    /**
     * Find the first visible day on the CalendarView.
     * This is the day at the top-left of the calendar.
     *
     * @return The first visible month or null if not found.
     */
    fun findFirstVisibleDay(): CalendarDay? {
        return calendarAdapter.findFirstVisibleDay()
    }

    /**
     * Find the first visible month on the CalendarView.
     *
     * @return The first visible month or null if not found.
     */
    fun findFirstVisibleMonth(): CalendarMonth? {
        return calendarAdapter.findFirstVisibleMonth()
    }

    /**
     * Find the first completely visible month on the CalendarView.
     *
     * @return The first completely visible month or null if not found.
     */
    fun findFirstCompletelyVisibleMonth(): CalendarMonth? {
        return calendarAdapter.findFirstCompletelyVisibleMonth()
    }

    private val scrollListenerInternal = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                calendarAdapter.notifyMonthScrollListenerIfNeeded()
            }
        }
    }

    private val pagerSnapHelper = PagerSnapHelper()


    /**
     * Setup the CalendarView. You can call this any time to change the
     * the desired [startMonth], [endMonth] or [firstDayOfWeek] on the Calendar.
     *
     * @param startMonth The first month on the calendar.
     * @param endMonth The last month on the calendar.
     * @param firstDayOfWeek An instance of [DayOfWeek] enum to be the first day of week.
     */
    fun setup(startMonth: YearMonth, endMonth: YearMonth, firstDayOfWeek: DayOfWeek) {
        this.startMonth = startMonth
        this.endMonth = endMonth
        this.firstDayOfWeek = firstDayOfWeek
        AndroidThreeTen.init(context) // The library checks for multiple calls.

        clipToPadding = false
        clipChildren = false //#ClipChildrenFix

        removeOnScrollListener(scrollListenerInternal)
        addOnScrollListener(scrollListenerInternal)

        layoutManager = CalendarLayoutManager(this, orientation)
        adapter = CalendarAdapter(
            this,
            ViewConfig(dayViewRes, monthHeaderRes, monthFooterRes, monthViewClass),
            MonthConfig(
                outDateStyle,
                inDateStyle,
                maxRowCount,
                startMonth,
                endMonth,
                firstDayOfWeek
            )
        )
    }

    companion object {
        /**
         * A value for [dayWidth] and [dayHeight] which indicates that the day
         * cells should have equal width and height. Each view's width and height
         * will be the width of the calender divided by 7.
         */
        const val DAY_SIZE_SQUARE = Int.MIN_VALUE
    }

    private fun resNotZero(resource: Int): Int {
        if (resource == 0) throw IllegalArgumentException("'dayViewResource' attribute not provided.") else return resource
    }
}
