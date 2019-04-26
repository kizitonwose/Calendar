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
import kotlinx.android.synthetic.main.exmaple_1_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter


class Example1Fragment : BaseFragment(), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    override val titleRes: Int = R.string.example_1_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_1_fragment, container, false)
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

        val now = YearMonth.now()
        exOneCalendar.setup(now.minusMonths(5), now.plusMonths(5), daysOfWeek.first())
        exOneCalendar.scrollToMonth(now)


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
            exOneMonthText.text = monthTitleFormatter.format(it.yearMonth)
        }

        legendLayout.children.forEach {
            // Change legend text colors to white
            (it as TextView).setTextColor(it.context.getColorCompat(R.color.example_1_white_light))
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
