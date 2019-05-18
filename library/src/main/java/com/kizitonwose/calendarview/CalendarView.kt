package com.kizitonwose.calendarview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.UNSPECIFIED
import androidx.annotation.Px
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kizitonwose.calendarview.adapter.*
import com.kizitonwose.calendarview.model.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

class CalendarView : RecyclerView {

    /**
     * The [DayBinder] instance used for managing day cell views
     * creation and reuse.
     */
    lateinit var dayBinder: DayBinder<*>

    /**
     * The [MonthHeaderFooterBinder] instance used for managing header views.
     * The header view is shown above each month on the Calendar.
     */
    var monthHeaderBinder: MonthHeaderFooterBinder<*>? = null

    /**
     * The [MonthHeaderFooterBinder] instance used for managing footer views.
     * The footer view is shown below each month on the Calendar.
     */
    var monthFooterBinder: MonthHeaderFooterBinder<*>? = null

    /**
     * Called when the calender scrolls to a new month. Mostly beneficial
     * if [ScrollMode] is [ScrollMode.PAGED].
     */
    var monthScrollListener: MonthScrollListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, 0)
    }

    private var dayViewRes: Int = 0
    private var monthHeaderRes: Int = 0
    private var monthFooterRes: Int = 0
    private var orientation = RecyclerView.VERTICAL
    private var scrollMode = ScrollMode.CONTINUOUS
    private var outDateStyle = OutDateStyle.END_OF_ROW
    private fun init(attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        if (isInEditMode) return
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.CalendarView, defStyleAttr, defStyleRes)
        dayViewRes = a.getResourceId(R.styleable.CalendarView_cv_dayViewResource, dayViewRes)
        monthHeaderRes = a.getResourceId(R.styleable.CalendarView_cv_monthHeaderResource, monthHeaderRes)
        monthFooterRes = a.getResourceId(R.styleable.CalendarView_cv_monthFooterResource, monthFooterRes)
        orientation = a.getInt(R.styleable.CalendarView_cv_orientation, orientation)
        scrollMode = ScrollMode.values()[a.getInt(R.styleable.CalendarView_cv_scrollMode, scrollMode.ordinal)]
        outDateStyle = OutDateStyle.values()[a.getInt(R.styleable.CalendarView_cv_outDateStyle, outDateStyle.ordinal)]
        a.recycle()
        if (dayViewRes == 0) throw IllegalArgumentException("'dayViewResource' attribute not provided.")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (autoSize && isInEditMode.not()) {
            val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
            val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
            val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

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

    @Px
    var monthPaddingStart = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    @Px
    var monthPaddingEnd = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    @Px
    var monthPaddingTop = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    @Px
    var monthPaddingBottom = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    private var autoSize = true
    private var sizedInternally = false
    @Px
    var dayWidth: Int = DAY_SIZE_SQUARE
        set(value) {
            field = value
            if (sizedInternally.not()) {
                autoSize = value == DAY_SIZE_SQUARE
                invalidateViewHolders()
            }
        }

    @Px
    var dayHeight: Int = DAY_SIZE_SQUARE
        set(value) {
            field = value
            if (sizedInternally.not()) {
                autoSize = value == DAY_SIZE_SQUARE
                invalidateViewHolders()
            }
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

    fun scrollToMonth(month: YearMonth) {
        calendarLayoutManager.scrollToMonth(month)
    }

    fun smoothScrollToMonth(month: YearMonth) {
        calendarLayoutManager.smoothScrollToMonth(month)
    }

    fun scrollToDate(date: LocalDate) {
        calendarLayoutManager.scrollToDate(date)
    }

    fun smoothScrollToDate(date: LocalDate) {
        calendarLayoutManager.smoothScrollToDate(date)
    }

    fun notifyDayChanged(day: CalendarDay) {
        calendarAdapter.reloadDay(day)
    }

    fun notifyDateChanged(date: LocalDate) {
        notifyDayChanged(CalendarDay(date, DayOwner.THIS_MONTH))
    }

    fun notifyDatesChanged(vararg date: LocalDate) {
        date.forEach {
            notifyDateChanged(it)
        }
    }

    fun notifyMonthChanged(month: YearMonth) {
        calendarAdapter.reloadMonth(month)
    }

    fun notifyCalendarChanged() {
        calendarAdapter.notifyDataSetChanged()
    }

    fun getFirstVisibleMonth(): CalendarMonth? {
        return calendarAdapter.getFirstVisibleMonth()
    }

    fun setup(startMonth: YearMonth, endMonth: YearMonth, firstDayOfWeek: DayOfWeek) {
        AndroidThreeTen.init(context) // The library checks for multiple calls.

        val config = CalendarConfig(outDateStyle, scrollMode, orientation)
        if (layoutManager == null) {
            clipToPadding = false
            layoutManager = CalendarLayoutManager(this, config)

            if (scrollMode == ScrollMode.PAGED) {
                PagerSnapHelper().attachToRecyclerView(this)
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        calendarAdapter.findVisibleMonthAndNotify()
                    }
                }
            })
        }
        adapter = CalendarAdapter(
            dayViewRes, monthHeaderRes, monthFooterRes, config,
            this, startMonth, endMonth, firstDayOfWeek
        )
    }

    companion object {
         const val DAY_SIZE_SQUARE = Int.MIN_VALUE
    }
}
