package com.kizitonwose.calendarviewsample


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.example_6_calendar_day.view.*
import kotlinx.android.synthetic.main.exmaple_6_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter


class Example6Fragment : BaseFragment(), HasBackButton {

    override val titleRes: Int = R.string.example_3_title
    private val today = LocalDate.now()

    private val titleSameYearFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_6_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        exSixCalendar.setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
        exSixCalendar.scrollToMonth(currentMonth)

        exSixCalendar.dateViewBinder = { view, day ->
            val textView = view.exSixDayText
            textView.text = day.date.dayOfMonth.toString()

            if (day.owner == DayOwner.THIS_MONTH) {
                textView.makeVisible()
                when (day.date) {

                }
            } else {
                textView.makeInVisible()
            }
        }

        exSixCalendar.monthHeaderBinder = { view, month ->
            val legendLayout = view.legendLayout
            // Setup each header day text if we have not done that already.
            if (legendLayout.tag == null) {
                legendLayout.tag = month.yearMonth
                legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                    tv.text = daysOfWeek[index].name.first().toString()
                    tv.setTextColorRes(R.color.example_6_black)
                }
            }
        }
    }
}
