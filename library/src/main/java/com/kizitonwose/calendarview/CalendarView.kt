package com.kizitonwose.calendarview

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Px
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kizitonwose.calendarview.adapter.*
import com.kizitonwose.calendarview.model.*
import com.kizitonwose.calendarview.utils.screenWidth
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

class CalendarView : RecyclerView {

    lateinit var dayBinder: DayBinder<*>

    var dateClickListener: DateClickListener? = null

    var monthHeaderBinder: MonthHeaderFooterBinder<*>? = null

    var monthFooterBinder: MonthHeaderFooterBinder<*>? = null

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

    @Px
    var dayWidth: Int = context.screenWidth / 7
        set(value) {
            field = value
            invalidateViewHolders()
        }

    @Px // A square calender is the default(dayHeight == dayWidth)
    var dayHeight: Int = dayWidth
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

    fun reloadDay(day: CalendarDay) {
        calendarAdapter.reloadDay(day)
    }

    fun reloadDate(date: LocalDate) {
        reloadDay(CalendarDay(date, DayOwner.THIS_MONTH))
    }

    fun reloadDates(vararg date: LocalDate) {
        date.forEach {
            reloadDate(it)
        }
    }

    fun reloadMonth(month: YearMonth) {
        calendarAdapter.reloadMonth(month)
    }

    fun reloadCalendar() {
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
}
