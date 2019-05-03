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

    lateinit var dateViewBinder: DateViewBinder<*>

    private lateinit var adapter: CalendarAdapter

    var dateClickListener: DateClickListener? = null

    var monthHeaderBinder: MonthHeaderFooterBinder? = null

    var monthFooterBinder: MonthHeaderFooterBinder? = null

    var monthScrollListener: MonthScrollListener? = null

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
        val dayViewRes = a.getResourceId(R.styleable.CalendarView_cv_dayViewResource, 0)
        val monthHeaderRes = a.getResourceId(R.styleable.CalendarView_cv_monthHeaderResource, 0)
        val monthFooterRes = a.getResourceId(R.styleable.CalendarView_cv_monthFooterResource, 0)
        val orientation = a.getInt(R.styleable.CalendarView_cv_orientation, RecyclerView.VERTICAL)
        val scrollMode = ScrollMode.values()[a.getInt(R.styleable.CalendarView_cv_scrollMode, 0)]
        val outDateStyle = OutDateStyle.values()[a.getInt(R.styleable.CalendarView_cv_outDateStyle, 0)]
        a.recycle()

        AndroidThreeTen.init(context) // The library checks for multiple calls.

        clipToPadding = false
        val config = CalendarConfig(outDateStyle, scrollMode, orientation)
        layoutManager = CalendarLayoutManager(this, config)
        adapter = CalendarAdapter(dayViewRes, monthHeaderRes, monthFooterRes, config)
        setAdapter(adapter)

        if (scrollMode == ScrollMode.PAGED) {
            PagerSnapHelper().attachToRecyclerView(this)
        }

        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    adapter.findVisibleMonthAndNotify()
                }
            }
        })
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

    private fun invalidateViewHolders() {
        // This does not remove visible views.
        // recycledViewPool.clear()

        // This removes all views but is internal.
        // removeAndRecycleViews()

        val state = calendarLayoutManager.onSaveInstanceState()
        setAdapter(adapter)
        calendarLayoutManager.onRestoreInstanceState(state)
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
        adapter.reloadDay(day)
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
        adapter.reloadMonth(month)
    }

    fun reloadCalendar() {
        adapter.notifyDataSetChanged()
    }

    fun setup(startMonth: YearMonth, endMonth: YearMonth, firstDayOfWeek: DayOfWeek) {
        adapter.setupDates(startMonth, endMonth, firstDayOfWeek)
    }

    fun getFirstVisibleMonth(): CalendarMonth? {
        return adapter.getFirstVisibleMonth()
    }
}
