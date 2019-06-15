package com.kizitonwose.calendarviewsample


import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.children
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.example_7_calendar_day.view.*
import kotlinx.android.synthetic.main.example_7_fragment.*
import org.threeten.bp.DayOfWeek

import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter


class Example7Fragment : BaseFragment(), HasBackButton {

    override val titleRes: Int = R.string.example_7_title

    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.example_7_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dm = DisplayMetrics()
        val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(dm)

        exSevenCalendar.dayWidth = dm.widthPixels / 5

        exSevenCalendar.dayHeight = (exSevenCalendar.dayWidth * 2)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.exSevenDayText
        }
        exSevenCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val textView = container.textView

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.text = day.date.dayOfMonth.toString()
                    textView.makeVisible()
                } else {
                    textView.makeInVisible()
                }
            }
        }

        val currentMonth = YearMonth.now()
        // Value for firstDayOfWeek does not matter since "hasBoundaries" is false.
        exSevenCalendar.setup(currentMonth, currentMonth.plusMonths(10), DayOfWeek.FRIDAY)
        exSevenCalendar.scrollToDate(currentMonth.atDay(1))
    }
}
