package com.kizitonwose.calendarviewsample


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.synthetic.main.calendar_day_legend.*
import kotlinx.android.synthetic.main.example_1_calendar_day.view.*
import kotlinx.android.synthetic.main.example_5_calendar_day.view.*
import kotlinx.android.synthetic.main.exmaple_1_fragment.*
import kotlinx.android.synthetic.main.exmaple_5_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter


class Example5Fragment : BaseFragment(), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    override val titleRes: Int = R.string.example_5_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_5_fragment, container, false)
    }

    private val selectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val daysOfWeek = daysOfWeekFromLocale()
        legendLayout.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].name.take(3).toUpperCase()
                setTextColorRes(R.color.example_2_white)
            }
        }

        val currentMonth = YearMonth.now()
        exFiveCalendar.setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
        exFiveCalendar.scrollToMonth(currentMonth)

        exFiveCalendar.dateViewBinder = { view, day ->
            val textView = view.exFiveDayText
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

        exFiveCalendar.dateClickListener = {
            if (it.owner == DayOwner.THIS_MONTH) {
                if (selectedDates.contains(it.date)) {
                    selectedDates.remove(it.date)
                } else {
                    selectedDates.add(it.date)
                }
                exFiveCalendar.reloadDay(it)
            }
        }

        exFiveCalendar.monthScrollListener = {
            exFiveYearText.text = it.yearMonth.year.toString()
            exFiveMonthText.text = monthTitleFormatter.format(it.yearMonth)
        }

    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.example_1_bg_light)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.colorPrimaryDark)
    }

}
