package com.kizitonwose.calendarviewsample


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.synthetic.main.calendar_day_legend.*
import kotlinx.android.synthetic.main.example_2_calendar_day.view.*
import kotlinx.android.synthetic.main.example_2_calendar_header.view.*
import kotlinx.android.synthetic.main.exmaple_2_fragment.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth


class Example2Fragment : BaseFragment(), HasToolbar, HasBackButton {

    override val toolbar: Toolbar?
        get() = exTwoToolbar

    override val titleRes: Int = R.string.example_2_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_2_fragment, container, false)
    }

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exTwoCalendar.setDateRange(YearMonth.now(), YearMonth.now().plusMonths(5))

        exTwoCalendar.dateViewBinder = { view, day ->
            val textView = view.exTwoDayText
            textView.text = day.date.dayOfMonth.toString()

            when (day.owner) {
                DayOwner.THIS_MONTH -> {
                    textView.makeVisible()
                    textView.setTextColorRes(R.color.example_2_black)
                }
                else -> {
                    textView.makeInVisible()
                }
            }

            when (day.date) {
                selectedDate -> {
                    textView.setTextColorRes(R.color.example_2_white)
                    textView.setBackgroundResource(R.drawable.example_2_selected_bg)
                }
                today -> {
                    textView.setTextColorRes(R.color.example_2_red)
                    textView.background = null
                }
                else -> textView.background = null
            }
        }

        exTwoCalendar.dateClickListener = dateClick@{
            if (it.owner == DayOwner.THIS_MONTH) {
                if (selectedDate == it.date) {
                    selectedDate = null
                    exTwoCalendar.reloadDay(it)
                } else {
                    val oldDate = selectedDate
                    selectedDate = it.date
                    exTwoCalendar.reloadDate(it.date)
                    exTwoCalendar.reloadDate(oldDate ?: return@dateClick)
                }
            }
        }

        exTwoCalendar.monthHeaderBinder = { view, calMonth ->
            @SuppressLint("SetTextI18n") // Fix concatenation warning for `setText` call.
            view.exTwoHeaderText.text = "${calMonth.yearMonth.month.name.toLowerCase().capitalize()} ${calMonth.year}"
        }

        legendLayout.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = DayOfWeek.values()[index].name.first().toString()
                setTextColorRes(R.color.example_2_white)
            }
        }

    }

}
