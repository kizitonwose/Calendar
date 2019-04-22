package com.kizitonwose.calendarviewsample


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.example_3_calendar_day.view.*
import kotlinx.android.synthetic.main.exmaple_3_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth


class Example3Fragment : BaseFragment(), HasBackButton {

    override val titleRes: Int = R.string.example_3_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_3_fragment, container, false)
    }

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exThreeCalendar.setDateRange(YearMonth.now(), YearMonth.now().plusMonths(5))

        exThreeCalendar.dateViewBinder = { view, day ->
            val textView = view.exThreeDayText
            textView.text = day.date.dayOfMonth.toString()

            when (day.owner) {
                DayOwner.THIS_MONTH -> {
                    textView.makeVisible()
                    textView.setTextColorRes(R.color.example_3_black)
                }
                else -> {
                    textView.makeInVisible()
                }
            }
        }

        exThreeCalendar.dateClickListener = dateClick@{

        }

        exThreeCalendar.monthScrollListener = {

        }

        exThreeCalendar.monthHeaderBinder = { view, _ ->
            val legendLayout = view.legendLayout
            // Setup each header day text if we have not done that already.
            if ((legendLayout.children.first() as TextView).text.count() != 1) {
                val days = daysOfWeekFromSunday()
                legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                    tv.text = days[index].name.first().toString()
                    tv.setTextColorRes(R.color.example_3_black)
                }
            }
        }

    }

}
