package com.kizitonwose.calendarview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kizitonwose.calendarview.adapter.*
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.OutDateStyle
import com.kizitonwose.calendarview.model.ScrollMode
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
        val firstDayOfWeek =
            DayOfWeek.values()[a.getInt(R.styleable.CalendarView_firstDayOfWeek, DayOfWeek.SUNDAY.ordinal)]
        a.recycle()

        AndroidThreeTen.init(context) // The library checks for multiple calls.

        clipToPadding = false
        layoutManager = LinearLayoutManager(context, orientation, false)
        val config = CalendarConfig(firstDayOfWeek, outDateStyle, scrollMode)
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

    fun reloadCalendar() {
        adapter.notifyDataSetChanged()
    }

    fun scrollToMonth(date: LocalDate) {
        adapter.scrollToMonth(date)
    }

    fun reloadDay(day: CalendarDay) {
        adapter.reloadDay(day)
    }

    fun reloadDate(date: LocalDate) {
        adapter.reloadDate(date)
    }

    fun reloadDates(vararg date: LocalDate) {
        date.forEach {
            adapter.reloadDate(it)
        }
    }

    fun setDateRange(startMonth: YearMonth, endMonth: YearMonth) {
        adapter.setDateRange(startMonth, endMonth)
    }

    fun setDateRange(startDate: LocalDate, endDate: LocalDate) {
        setDateRange(
            YearMonth.of(startDate.year, startDate.month),
            YearMonth.of(endDate.year, endDate.month)
        )
    }
}
