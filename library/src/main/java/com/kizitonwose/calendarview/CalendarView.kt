package com.kizitonwose.calendarview

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.*
import com.kizitonwose.calendarview.ui.*
import com.kizitonwose.calendarview.utils.Size
import com.kizitonwose.calendarview.utils.job
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

typealias Completion = () -> Unit

open class CalendarView : RecyclerView {

    /**
     * The [DayBinder] instance used for managing day cell views
     * creation and reuse. Changing the binder means that the view
     * creation logic could have changed too. We refresh the Calender.
     */
    var dayBinder: DayBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The [MonthHeaderFooterBinder] instance used for managing header views.
     * The header view is shown above each month on the Calendar.
     */
    var monthHeaderBinder: MonthHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The [MonthHeaderFooterBinder] instance used for managing footer views.
     * The footer view is shown below each month on the Calendar.
     */
    var monthFooterBinder: MonthHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * Called when the calender scrolls to a new month. Mostly beneficial
     * if [ScrollMode] is [ScrollMode.PAGED].
     */
    var monthScrollListener: MonthScrollListener? = null

    /**
     * The xml resource that is inflated and used as the day cell view.
     * This must be provided.
     */
    var dayViewResource = 0
        set(value) {
            if (field != value) {
                if (value == 0) throw IllegalArgumentException("'dayViewResource' attribute not provided.")
                field = value
                updateAdapterViewConfig()
            }
        }

    /**
     * The xml resource that is inflated and used as a header for every month.
     * Set zero to disable.
     */
    var monthHeaderResource = 0
        set(value) {
            if (field != value) {
                field = value
                updateAdapterViewConfig()
            }
        }

    /**
     * The xml resource that is inflated and used as a footer for every month.
     * Set zero to disable.
     */
    var monthFooterResource = 0
        set(value) {
            if (field != value) {
                field = value
                updateAdapterViewConfig()
            }
        }

    /**
     * A [ViewGroup] which is instantiated and used as the background for each month.
     * This class must have a constructor which takes only a [Context]. You should
     * exclude the name and constructor of this class from code obfuscation if enabled.
     */
    var monthViewClass: String? = null
        set(value) {
            if (field != value) {
                field = value
                updateAdapterViewConfig()
            }
        }

    /**
     * The [RecyclerView.Orientation] used for the layout manager.
     * This determines the scroll direction of the the calendar.
     */
    @Orientation
    var orientation = VERTICAL
        set(value) {
            if (field != value) {
                field = value
                setup(startMonth ?: return, endMonth ?: return, firstDayOfWeek ?: return)
            }
        }

    /**
     * The scrolling behavior of the calendar. If [ScrollMode.PAGED],
     * the calendar will snap to the nearest month after a scroll or swipe action.
     * If [ScrollMode.CONTINUOUS], the calendar scrolls normally.
     */
    var scrollMode = ScrollMode.CONTINUOUS
        set(value) {
            if (field != value) {
                field = value
                pagerSnapHelper.attachToRecyclerView(if (value == ScrollMode.PAGED) this else null)
            }
        }

    /**
     * Determines how inDates are generated for each month on the calendar.
     * If set to [InDateStyle.ALL_MONTHS], inDates will be generated for all months.
     * If set to [InDateStyle.FIRST_MONTH], inDates will be generated for the first month only.
     * If set to [InDateStyle.NONE], inDates will not be generated, this means there will
     * be no offset on any month.
     *
     * Note: This causes calendar data to be regenerated, consider using [updateMonthConfiguration]
     * if updating this property alongside [outDateStyle], [maxRowCount] or [hasBoundaries].
     */
    var inDateStyle = InDateStyle.ALL_MONTHS
        set(value) {
            if (field != value) {
                field = value
                updateAdapterMonthConfig()
            }
        }

    /**
     * Determines how outDates are generated for each month on the calendar.
     * If set to [OutDateStyle.END_OF_ROW], the calendar will generate outDates until
     * it reaches the first end of a row. This means that if a month has 6 rows,
     * it will display 6 rows and if a month has 5 rows, it will display 5 rows.
     * If set to [OutDateStyle.END_OF_GRID], the calendar will generate outDates until
     * it reaches the end of a 6 x 7 grid. This means that all months will have 6 rows.
     * If set to [OutDateStyle.NONE], no outDates will be generated.
     *
     * Note: This causes calendar data to be regenerated, consider using [updateMonthConfiguration]
     * if updating this value property [inDateStyle], [maxRowCount] or [hasBoundaries].
     */
    var outDateStyle = OutDateStyle.END_OF_ROW
        set(value) {
            if (field != value) {
                field = value
                updateAdapterMonthConfig()
            }
        }

    /**
     * The maximum number of rows(1 to 6) to show on each month. If a month has a total of 6
     * rows and [maxRowCount] is set to 4, there will be two appearances of that month on the,
     * calendar the first one will show 4 rows and the second one will show the remaining 2 rows.
     * To show a week mode calendar, set this value to 1.
     *
     * Note: This causes calendar data to be regenerated, consider using [updateMonthConfiguration]
     * if updating this property alongside [inDateStyle], [outDateStyle] or [hasBoundaries].
     */
    var maxRowCount = 6
        set(value) {
            if (!(1..6).contains(value)) throw IllegalArgumentException("'maxRowCount' should be between 1 to 6")
            if (field != value) {
                field = value
                updateAdapterMonthConfig()
            }
        }

    /**
     * Determines if dates of a month should stay in its section or can flow into another month's section.
     * If true, a section can only contain dates belonging to that month, its inDates and outDates.
     * if false, the dates are added continuously, irrespective of month sections.
     *
     * When this property is false, a few things behave slightly differently:
     * - If [InDateStyle] is either [InDateStyle.ALL_MONTHS] or [InDateStyle.FIRST_MONTH], only the first index
     *   will contain inDates.
     * - If [OutDateStyle] is either [OutDateStyle.END_OF_ROW] or [OutDateStyle.END_OF_GRID],
     *   only the last index will contain outDates.
     * - If [OutDateStyle] is [OutDateStyle.END_OF_GRID], outDates are generated for the last index until it
     *   satisfies the [maxRowCount] requirement.
     *
     * Note: This causes calendar data to be regenerated, consider using [updateMonthConfiguration]
     * if updating this property alongside [inDateStyle], [outDateStyle] or [maxRowCount].
     */
    var hasBoundaries = true
        set(value) {
            if (field != value) {
                field = value
                updateAdapterMonthConfig()
            }
        }

    /**
     * The duration in milliseconds of the animation used to adjust the CalendarView's
     * height when [scrollMode] is [ScrollMode.PAGED] and the CalendarView's height is
     * set to `wrap_content`. The height change happens when the CalendarView scrolls to
     * a month which has less or more rows than the previous one. Default value is 200.
     * To disable the animation, set this value to zero.
     */
    var wrappedPageHeightAnimationDuration = 200

    private val pagerSnapHelper = CalenderPageSnapHelper()

    private var startMonth: YearMonth? = null
    private var endMonth: YearMonth? = null
    private var firstDayOfWeek: DayOfWeek? = null

    private var autoSize = true
    private var autoSizeHeight = SQUARE
    private var sizedInternally = false

    internal val isVertical: Boolean
        get() = orientation == VERTICAL

    internal val isHorizontal: Boolean
        get() = !isVertical

    private var configJob: Job? = null
    private var internalConfigUpdate = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, defStyleAttr)
    }

    private fun init(attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        if (isInEditMode) return
        setHasFixedSize(true)
        context.withStyledAttributes(attributeSet, R.styleable.CalendarView, defStyleAttr, defStyleRes) {
            dayViewResource = getResourceId(R.styleable.CalendarView_cv_dayViewResource, dayViewResource)
            monthHeaderResource = getResourceId(R.styleable.CalendarView_cv_monthHeaderResource, monthHeaderResource)
            monthFooterResource = getResourceId(R.styleable.CalendarView_cv_monthFooterResource, monthFooterResource)
            orientation = getInt(R.styleable.CalendarView_cv_orientation, orientation)
            scrollMode = ScrollMode.values()[getInt(R.styleable.CalendarView_cv_scrollMode, scrollMode.ordinal)]
            outDateStyle = OutDateStyle.values()[getInt(R.styleable.CalendarView_cv_outDateStyle, outDateStyle.ordinal)]
            inDateStyle = InDateStyle.values()[getInt(R.styleable.CalendarView_cv_inDateStyle, inDateStyle.ordinal)]
            maxRowCount = getInt(R.styleable.CalendarView_cv_maxRowCount, maxRowCount)
            monthViewClass = getString(R.styleable.CalendarView_cv_monthViewClass)
            hasBoundaries = getBoolean(R.styleable.CalendarView_cv_hasBoundaries, hasBoundaries)
            wrappedPageHeightAnimationDuration = getInt(
                R.styleable.CalendarView_cv_wrappedPageHeightAnimationDuration,
                wrappedPageHeightAnimationDuration
            )
        }
        check(dayViewResource != 0) { "No value set for `cv_dayViewResource` attribute." }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (autoSize && !isInEditMode) {
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)

            if (widthMode == UNSPECIFIED && heightMode == UNSPECIFIED) {
                throw UnsupportedOperationException("Cannot calculate the values for day Width/Height with the current configuration.")
            }

            // +0.5 => round to the nearest pixel
            val size = (((widthSize - (monthPaddingStart + monthPaddingEnd)) / 7f) + 0.5).toInt()

            val height = if (autoSizeHeight == SQUARE) size else autoSizeHeight
            val computedSize = daySize.copy(width = size, height = height)
            if (daySize != computedSize) {
                sizedInternally = true
                daySize = computedSize
                sizedInternally = false
                invalidateViewHolders()
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * The size in pixels for each day cell view.
     * Set this to [SIZE_SQUARE] to have a nice
     * square item view.
     *
     * @see [SIZE_SQUARE]
     */
    var daySize: Size = SIZE_SQUARE
        set(value) {
            field = value
            if (!sizedInternally) {
                autoSize = value == SIZE_SQUARE || value.width == SQUARE
                autoSizeHeight = value.height
                invalidateViewHolders()
            }
        }

    /**
     * The padding, in pixels to be applied
     * to the start of each month view.
     */
    @Px
    var monthPaddingStart = 0
        private set

    /**
     * The padding, in pixels to be applied
     * to the end of each month view.
     */
    @Px
    var monthPaddingEnd = 0
        private set

    /**
     * The padding, in pixels to be applied
     * to the top of each month view.
     */
    @Px
    var monthPaddingTop = 0
        private set

    /**
     * The padding, in pixels to be applied
     * to the bottom of each month view.
     */
    @Px
    var monthPaddingBottom = 0
        private set

    /**
     * The margin, in pixels to be applied
     * to the start of each month view.
     */
    @Px
    var monthMarginStart = 0
        private set

    /**
     * The margin, in pixels to be applied
     * to the end of each month view.
     */
    @Px
    var monthMarginEnd = 0
        private set

    /**
     * The margin, in pixels to be applied
     * to the top of each month view.
     */
    @Px
    var monthMarginTop = 0
        private set

    /**
     * The margin, in pixels to be applied
     * to the bottom of each month view.
     */
    @Px
    var monthMarginBottom = 0
        private set

    private val calendarLayoutManager: CalendarLayoutManager
        get() = layoutManager as CalendarLayoutManager

    private val calendarAdapter: CalendarAdapter
        get() = adapter as CalendarAdapter

    private fun updateAdapterViewConfig() {
        if (adapter != null) {
            calendarAdapter.viewConfig =
                ViewConfig(dayViewResource, monthHeaderResource, monthFooterResource, monthViewClass)
            invalidateViewHolders()
        }
    }

    private fun invalidateViewHolders() {
        // This does not remove visible views.
        // recycledViewPool.clear()

        // This removes all views but is internal.
        // removeAndRecycleViews()
        if (internalConfigUpdate) return

        if (adapter == null || layoutManager == null) return
        val state = layoutManager?.onSaveInstanceState()
        adapter = adapter
        layoutManager?.onRestoreInstanceState(state)
        post { calendarAdapter.notifyMonthScrollListenerIfNeeded() }
    }

    private fun updateAdapterMonthConfig(config: MonthConfig? = null) {
        if (internalConfigUpdate) return
        if (adapter != null) {
            calendarAdapter.monthConfig = config ?: MonthConfig(
                outDateStyle,
                inDateStyle,
                maxRowCount,
                startMonth ?: return,
                endMonth ?: return,
                firstDayOfWeek ?: return,
                hasBoundaries, Job()
            )
            calendarAdapter.notifyDataSetChanged()
            post { calendarAdapter.notifyMonthScrollListenerIfNeeded() }
        }
    }

    /**
     * Update [inDateStyle], [outDateStyle], [maxRowCount] and [hasBoundaries]
     * without generating the underlying calendar data multiple times.
     * See [updateMonthConfigurationAsync] if you wish to do this asynchronously.
     */
    fun updateMonthConfiguration(
        inDateStyle: InDateStyle = this.inDateStyle,
        outDateStyle: OutDateStyle = this.outDateStyle,
        maxRowCount: Int = this.maxRowCount,
        hasBoundaries: Boolean = this.hasBoundaries
    ) {
        configJob?.cancel()
        internalConfigUpdate = true
        this.inDateStyle = inDateStyle
        this.outDateStyle = outDateStyle
        this.maxRowCount = maxRowCount
        this.hasBoundaries = hasBoundaries
        internalConfigUpdate = false
        updateAdapterMonthConfig()
    }

    /**
     * Update [inDateStyle], [outDateStyle], [maxRowCount] and [hasBoundaries]
     * asynchronously without generating the underlying calendar data multiple times.
     * Useful if your [startMonth] and [endMonth] values are many years apart.
     * See [updateMonthConfiguration] if you wish to do this synchronously.
     */
    fun updateMonthConfigurationAsync(
        inDateStyle: InDateStyle = this.inDateStyle,
        outDateStyle: OutDateStyle = this.outDateStyle,
        maxRowCount: Int = this.maxRowCount,
        hasBoundaries: Boolean = this.hasBoundaries,
        completion: Completion? = null
    ) {
        configJob?.cancel()
        internalConfigUpdate = true
        this.inDateStyle = inDateStyle
        this.outDateStyle = outDateStyle
        this.maxRowCount = maxRowCount
        this.hasBoundaries = hasBoundaries
        internalConfigUpdate = false
        configJob = GlobalScope.launch {
            val monthConfig = generateMonthConfig(job)
            withContext(Main) {
                updateAdapterMonthConfig(monthConfig)
                completion?.invoke()
            }
        }
    }

    /**
     * Set the [monthPaddingStart], [monthPaddingTop], [monthPaddingEnd] and [monthPaddingBottom]
     * values without invalidating the view holders multiple times which would happen if these
     * values were set individually.
     */
    fun setMonthPadding(
        @Px start: Int = monthPaddingStart,
        @Px top: Int = monthPaddingTop,
        @Px end: Int = monthPaddingEnd,
        @Px bottom: Int = monthPaddingBottom
    ) {
        monthPaddingStart = start
        monthPaddingTop = top
        monthPaddingEnd = end
        monthPaddingBottom = bottom
        invalidateViewHolders()
    }

    /**
     * Set the [monthMarginStart], [monthMarginTop], [monthMarginEnd] and [monthMarginBottom]
     * values without invalidating the view holders multiple times which would happen if these
     * values were set individually.
     */
    fun setMonthMargins(
        @Px start: Int = monthMarginStart,
        @Px top: Int = monthMarginTop,
        @Px end: Int = monthMarginEnd,
        @Px bottom: Int = monthMarginBottom
    ) {
        monthMarginStart = start
        monthMarginTop = top
        monthMarginEnd = end
        monthMarginBottom = bottom
        invalidateViewHolders()
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
     * Shortcut for [scrollToDay] with a [LocalDate] instance.
     */
    @JvmOverloads
    fun scrollToDate(date: LocalDate, owner: DayOwner = DayOwner.THIS_MONTH) {
        scrollToDay(CalendarDay(date, owner))
    }

    /**
     * Scroll to a specific [CalendarDay] using a smooth scrolling animation.
     * Just like [scrollToDay], but with a smooth scrolling animation.
     */
    fun smoothScrollToDay(day: CalendarDay) {
        calendarLayoutManager.smoothScrollToDay(day)
    }

    /**
     * Shortcut for [smoothScrollToDay] with a [LocalDate] instance.
     */
    @JvmOverloads
    fun smoothScrollToDate(date: LocalDate, owner: DayOwner = DayOwner.THIS_MONTH) {
        smoothScrollToDay(CalendarDay(date, owner))
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
     * Shortcut for [notifyDayChanged] with a [LocalDate] instance.
     */
    @JvmOverloads
    fun notifyDateChanged(date: LocalDate, owner: DayOwner = DayOwner.THIS_MONTH) {
        notifyDayChanged(CalendarDay(date, owner))
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
    fun findFirstVisibleMonth(): CalendarMonth? {
        return calendarAdapter.findFirstVisibleMonth()
    }

    /**
     * Find the last visible month on the CalendarView.
     *
     * @return The last visible month or null if not found.
     */
    fun findLastVisibleMonth(): CalendarMonth? {
        return calendarAdapter.findLastVisibleMonth()
    }

    /**
     * Find the first visible day on the CalendarView.
     * This is the day at the top-left corner of the calendar.
     *
     * @return The first visible day or null if not found.
     */
    fun findFirstVisibleDay(): CalendarDay? {
        return calendarAdapter.findFirstVisibleDay()
    }

    /**
     * Find the last visible day on the CalendarView.
     * This is the day at the bottom-right corner of the calendar.
     *
     * @return The last visible day or null if not found.
     */
    fun findLastVisibleDay(): CalendarDay? {
        return calendarAdapter.findLastVisibleDay()
    }

    private val scrollListenerInternal = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                calendarAdapter.notifyMonthScrollListenerIfNeeded()
            }
        }
    }

    /**
     * Setup the CalendarView.
     * See [updateMonthRange] and [updateMonthRangeAsync] to change
     * the [startMonth] and [endMonth] values.
     *
     * @param startMonth The first month on the calendar.
     * @param endMonth The last month on the calendar.
     * @param firstDayOfWeek An instance of [DayOfWeek] enum to be the first day of week.
     */
    fun setup(startMonth: YearMonth, endMonth: YearMonth, firstDayOfWeek: DayOfWeek) {
        configJob?.cancel()
        this.startMonth = startMonth
        this.endMonth = endMonth
        this.firstDayOfWeek = firstDayOfWeek
        finishSetup(
            MonthConfig(
                outDateStyle, inDateStyle, maxRowCount, startMonth,
                endMonth, firstDayOfWeek, hasBoundaries, Job()
            )
        )
    }

    /**
     * Setup the CalendarView, asynchronously.
     * Useful if your [startMonth] and [endMonth] values are many years apart.
     * See [updateMonthRange] and [updateMonthRangeAsync] to change the
     * [startMonth] and [endMonth] values.
     *
     * Note: the setup MUST finish before any other methods can are called. To be
     * notified when the setup is finished, provide a [completion] parameter.
     *
     * @param startMonth The first month on the calendar.
     * @param endMonth The last month on the calendar.
     * @param firstDayOfWeek An instance of [DayOfWeek] enum to be the first day of week.
     */
    @JvmOverloads
    fun setupAsync(
        startMonth: YearMonth,
        endMonth: YearMonth,
        firstDayOfWeek: DayOfWeek,
        completion: Completion? = null
    ) {
        configJob?.cancel()
        this.startMonth = startMonth
        this.endMonth = endMonth
        this.firstDayOfWeek = firstDayOfWeek
        configJob = GlobalScope.launch {
            val monthConfig = MonthConfig(
                outDateStyle, inDateStyle, maxRowCount, startMonth,
                endMonth, firstDayOfWeek, hasBoundaries, job
            )
            withContext(Main) {
                finishSetup(monthConfig)
                completion?.invoke()
            }
        }
    }

    private fun finishSetup(monthConfig: MonthConfig) {
        removeOnScrollListener(scrollListenerInternal)
        addOnScrollListener(scrollListenerInternal)

        layoutManager = CalendarLayoutManager(this, orientation)
        adapter = CalendarAdapter(
            this,
            ViewConfig(dayViewResource, monthHeaderResource, monthFooterResource, monthViewClass),
            monthConfig
        )
    }

    /**
     * Update the CalendarView's start and end months.
     * This can be called only if you have called [setup] or [setupAsync] in the past.
     * See [updateMonthRangeAsync] if you wish to do this asynchronously.
     */
    @JvmOverloads
    fun updateMonthRange(startMonth: YearMonth = requireStartMonth(), endMonth: YearMonth = requireEndMonth()) {
        configJob?.cancel()
        this.startMonth = startMonth
        this.endMonth = endMonth
        val (config, diff) = getMonthUpdateData(Job())
        finishUpdateMonthRange(config, diff)
    }

    /**
     * Update the CalendarView's start and end months, asynchronously.
     * This can be called only if you have called [setup] or [setupAsync] in the past.
     * Useful if your [startMonth] and [endMonth] values are many years apart.
     * See [updateMonthRange] if you wish to do this synchronously.
     */
    @JvmOverloads
    fun updateMonthRangeAsync(
        startMonth: YearMonth = requireStartMonth(),
        endMonth: YearMonth = requireEndMonth(),
        completion: Completion? = null
    ) {
        configJob?.cancel()
        this.startMonth = startMonth
        this.endMonth = endMonth
        configJob = GlobalScope.launch {
            val (config, diff) = getMonthUpdateData(job)
            withContext(Main) {
                finishUpdateMonthRange(config, diff)
                completion?.invoke()
            }
        }
    }

    private fun getMonthUpdateData(job: Job): Pair<MonthConfig, DiffUtil.DiffResult> {
        val monthConfig = generateMonthConfig(job)
        val diffResult = DiffUtil.calculateDiff(
            MonthRangeDiffCallback(calendarAdapter.monthConfig.months, monthConfig.months),
            false
        )
        return Pair(monthConfig, diffResult)
    }

    private fun finishUpdateMonthRange(newConfig: MonthConfig, diffResult: DiffUtil.DiffResult) {
        calendarAdapter.monthConfig = newConfig
        diffResult.dispatchUpdatesTo(calendarAdapter)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        configJob?.cancel()
    }

    private class MonthRangeDiffCallback(
        private val oldItems: List<CalendarMonth>,
        private val newItems: List<CalendarMonth>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldItems.size

        override fun getNewListSize() = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldItems[oldItemPosition] == newItems[newItemPosition]

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            areItemsTheSame(oldItemPosition, newItemPosition)
    }

    private fun generateMonthConfig(job: Job): MonthConfig {
        return MonthConfig(
            outDateStyle,
            inDateStyle,
            maxRowCount,
            requireStartMonth(),
            requireEndMonth(),
            requireFirstDayOfWeek(),
            hasBoundaries,
            job
        )
    }

    private fun requireStartMonth(): YearMonth {
        return startMonth ?: throw IllegalStateException("`startMonth` is not set. Have you called `setup()`?")
    }

    private fun requireEndMonth(): YearMonth {
        return endMonth ?: throw IllegalStateException("`endMonth` is not set. Have you called `setup()`?")
    }

    private fun requireFirstDayOfWeek(): DayOfWeek {
        return firstDayOfWeek ?: throw IllegalStateException("`firstDayOfWeek` is not set. Have you called `setup()`?")
    }

    companion object {
        private const val SQUARE = Int.MIN_VALUE

        /**
         * A value for [daySize]  which indicates that the day cells should
         * have equal width and height. Each view's width and height will
         * be the width of the calender divided by 7.
         */
        val SIZE_SQUARE = Size(SQUARE, SQUARE)

        /**
         * A value for [daySize] which indicates that the day cells should
         * have width of the calender divided by 7 and provided height.
         */
        fun sizeAutoWidth(@Px height: Int) = Size(SQUARE, height)
    }
}
