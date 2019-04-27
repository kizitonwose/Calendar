package com.kizitonwose.calendarview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kizitonwose.calendarview.adapter.*
import com.kizitonwose.calendarview.model.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

class CalendarView : RecyclerView {

    private lateinit var adapter: CalendarAdapter

    var dateClickListener: DateClickListener? = null

    var dateViewBinder: DateViewBinder? = null

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
        val dayViewRes = a.getResourceId(R.styleable.CalendarView_dayViewResource, 0)
        val monthHeaderRes = a.getResourceId(R.styleable.CalendarView_monthHeaderResource, 0)
        val monthFooterRes = a.getResourceId(R.styleable.CalendarView_monthFooterResource, 0)
        val orientation = a.getInt(R.styleable.CalendarView_orientation, RecyclerView.VERTICAL)
        val scrollMode = ScrollMode.values()[a.getInt(R.styleable.CalendarView_scrollMode, 0)]
        val outDateStyle = OutDateStyle.values()[a.getInt(R.styleable.CalendarView_outDateStyle, 0)]
        a.recycle()

        AndroidThreeTen.init(context) // The library checks for multiple calls.

        clipToPadding = false
        layoutManager = LinearLayoutManager(context, orientation, false)
        val config = CalendarConfig(outDateStyle, scrollMode)
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

    var monthPaddingStart = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    var monthPaddingEnd = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    var monthPaddingTop = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    var monthPaddingBottom = 0
        set(value) {
            field = value
            invalidateViewHolders()
        }

    var monthWidth = ViewGroup.LayoutParams.MATCH_PARENT
        set(value) {
            field = value
            invalidateViewHolders()
        }

    var monthHeight = ViewGroup.LayoutParams.WRAP_CONTENT
        set(value) {
            field = value
            invalidateViewHolders()
        }

    private fun invalidateViewHolders() {
        recycledViewPool.clear()
    }

    fun scrollToMonth(month: YearMonth) {
        adapter.scrollToMonth(month)
    }

    fun scrollToDate(date: LocalDate) {
        adapter.scrollToDate(date)
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
