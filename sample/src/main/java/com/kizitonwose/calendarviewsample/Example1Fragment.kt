package com.kizitonwose.calendarviewsample


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.utils.getColorCompat
import com.kizitonwose.calendarview.utils.setTextColorRes
import kotlinx.android.synthetic.main.calendar_day_legend.*
import kotlinx.android.synthetic.main.example_1_calendar_day.view.*
import kotlinx.android.synthetic.main.exmaple_1_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter


class Example1Fragment : BaseFragment(), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    override val titleRes: Int = R.string.example_1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_1_fragment, container, false)
    }

    private val selectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exOneCalendar.dateViewBinder = { view, day ->
            val textView = view.exOneDayText
            textView.text = day.date.dayOfMonth.toString()
            when (day.owner) {
                DayOwner.THIS_MONTH -> textView.setTextColorRes(R.color.example_1_white)
                else -> textView.setTextColorRes(R.color.example_1_white_light)
            }

            when {
                selectedDates.contains(day.date) -> {
                    textView.setTextColorRes(R.color.example_1_bg)
                    textView.setBackgroundResource(R.drawable.example_1_selected_bg)

                }
                today == day.date -> {
                    textView.setTextColorRes(R.color.example_1_white)
                    textView.setBackgroundResource(R.drawable.example_1_today_bg)
                }
                else -> textView.background = null
            }
        }

        exOneCalendar.dateClickListener = {
            if (it.owner == DayOwner.THIS_MONTH) {
                if (selectedDates.contains(it.date)) {
                    selectedDates.remove(it.date)
                } else {
                    selectedDates.add(it.date)
                }
                exOneCalendar.reloadDay(it)
            }
        }

        exOneCalendar.monthScrollListener = {
            exOneYearText.text = it.yearMonth.year.toString()
            exOneMonthText.text = DateTimeFormatter.ofPattern("MMMM").format(it.yearMonth)
        }

        legendLayout.children.forEach {
            // Change legend text colors to white
            (it as TextView).setTextColor(it.context.getColorCompat(R.color.example_1_white_light))
        }
    }

}
