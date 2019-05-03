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
import androidx.core.view.children
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.adapter.DateViewBinder
import com.kizitonwose.calendarview.adapter.ViewContainer
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.example_6_calendar_day.view.*
import kotlinx.android.synthetic.main.example_6_calendar_header.view.*
import kotlinx.android.synthetic.main.exmaple_6_fragment.*
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter


class Example6Fragment : BaseFragment(), HasBackButton {

    override val titleRes: Int = R.string.example_6_title

    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_6_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup custom day size to fit two months on the screen.
        val dm = DisplayMetrics()
        val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(dm)

        // We want the immediately following/previous month
        // to be partially visible so we multiply by 0.8
        val monthWidth = (dm.widthPixels * 0.8).toInt()
        val dayWidth = monthWidth / 7
        exSixCalendar.dayWidth = dayWidth

        // We don't want a square calendar.
        exSixCalendar.dayHeight = (dayWidth * 1.5).toInt()


        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.exSixDayText
        }
        exSixCalendar.dateViewBinder = object : DateViewBinder<DayViewContainer> {
            override fun provide(view: View) = DayViewContainer(view)
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
        exSixCalendar.addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.HORIZONTAL))

        exSixCalendar.monthHeaderBinder = { view, month ->
            view.exSixMonthText.text = titleFormatter.format(month.yearMonth)

            val legendLayout = view.legendLayout
            // Setup each header day text if we have not done that already.
            if (legendLayout.tag == null) {
                legendLayout.tag = month.yearMonth
                legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                    tv.text = daysOfWeek[index].name.first().toString()
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                    tv.setTextColorRes(R.color.example_6_black)
                }
            }
        }

    }
}
