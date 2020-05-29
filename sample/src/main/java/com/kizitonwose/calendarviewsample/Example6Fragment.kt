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
import kotlinx.android.synthetic.main.example_6_calendar_day.view.*
import kotlinx.android.synthetic.main.example_6_calendar_header.view.*
import kotlinx.android.synthetic.main.example_6_fragment.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// We assign this class to the `monthViewClass` attribute in XML.
// See usage in example_6_fragment.xml
class Example6MonthView(context: Context) : CardView(context) {

    init {
        setCardBackgroundColor(context.getColorCompat(R.color.example_6_month_bg_color))
        radius = dpToPx(8, context).toFloat()
        elevation = 8f
    }
}


class Example6Fragment : BaseFragment(), HasBackButton {

    override val titleRes: Int = R.string.example_6_title

    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.example_6_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup custom day size to fit two months on the screen.
        val dm = DisplayMetrics()
        val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(dm)

        // We want the immediately following/previous month to be
        // partially visible so we multiply the total width by 0.73
        val monthWidth = (dm.widthPixels * 0.73).toInt()
        val dayWidth = monthWidth / 7
        exSixCalendar.dayWidth = dayWidth

        // We don't want a square calendar.
        exSixCalendar.dayHeight = (dayWidth * 1.73).toInt()

        // Add margins around our card view.
        val horizontalMargin = dpToPx(8, requireContext())
        val verticalMargin = dpToPx(14, requireContext())
        exSixCalendar.monthMarginStart = horizontalMargin
        exSixCalendar.monthMarginEnd = horizontalMargin
        exSixCalendar.monthMarginTop = verticalMargin
        exSixCalendar.monthMarginBottom = verticalMargin

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.exSixDayText
        }
        exSixCalendar.dayBinder = object : DayBinder<DayViewContainer> {
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

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        exSixCalendar.setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
        exSixCalendar.scrollToMonth(currentMonth)

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = view.exSixMonthText
            val legendLayout = view.legendLayout
        }
        exSixCalendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                container.textView.text = titleFormatter.format(month.yearMonth)
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeek[index].name.first().toString()
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                        tv.setTextColorRes(R.color.example_6_black)
                    }
                }
            }
        }
    }
}
