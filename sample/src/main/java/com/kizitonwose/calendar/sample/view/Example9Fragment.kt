package com.kizitonwose.calendar.sample.view

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.CalendarYear
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.databinding.Example9CalendarDayBinding
import com.kizitonwose.calendar.sample.databinding.Example9CalendarMonthHeaderBinding
import com.kizitonwose.calendar.sample.databinding.Example9CalendarYearHeaderBinding
import com.kizitonwose.calendar.sample.databinding.Example9FragmentBinding
import com.kizitonwose.calendar.sample.shared.displayText
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.YearHeaderFooterBinder
import java.time.LocalDate
import java.time.Year

class Example9Fragment : BaseFragment(R.layout.example_9_fragment), HasToolbar, HasBackButton {
    override val toolbar: Toolbar
        get() = binding.exNineToolbar

    override val titleRes: Int = R.string.example_9_title

    private lateinit var binding: Example9FragmentBinding

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding = Example9FragmentBinding.bind(view)
        configureBinders()
        binding.exNineCalendar.setup(
            Year.now(),
            Year.now().plusYears(50),
            firstDayOfWeekFromLocale(),
        )
    }

    private fun configureBinders() {
        val calendarView = binding.exNineCalendar

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = Example9CalendarDayBinding.bind(view).exNineDayText

            init {
                textView.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (selectedDate == day.date) {
                            selectedDate = null
                            calendarView.notifyDayChanged(day)
                        } else {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            calendarView.notifyDateChanged(day.date)
                            oldDate?.let { calendarView.notifyDateChanged(oldDate) }
                        }
                    }
                }
            }
        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    textView.makeVisible()
                    when (data.date) {
                        selectedDate -> {
                            textView.setTextColorRes(R.color.example_2_white)
                            textView.setBackgroundResource(R.drawable.example_2_selected_bg)
                        }

                        today -> {
                            textView.setTextColorRes(R.color.example_2_red)
                            textView.background = null
                        }

                        else -> {
                            textView.setTextColorRes(R.color.example_2_black)
                            textView.background = null
                        }
                    }
                } else {
                    textView.makeInVisible()
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val bind = Example9CalendarMonthHeaderBinding.bind(view)
            val textView = bind.exNineMonthHeaderText
            val legendLayout = bind.legendLayout.root
        }
        calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.textView.text = data.yearMonth.displayText()
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = true
                        val daysOfWeek = data.weekDays.first().map { it.date.dayOfWeek }
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].displayText(uppercase = true, narrow = true)
                                tv.setTextColorRes(R.color.example_3_black)
                                // TODO - YEAR set menium bold
//                                tv.sty
                            }
                    }
                }
            }

        class YearViewContainer(view: View) : ViewContainer(view) {
            val textView = Example9CalendarYearHeaderBinding.bind(view).exNineYearHeaderText
        }
        calendarView.yearHeaderBinder =
            object : YearHeaderFooterBinder<YearViewContainer> {
                override fun create(view: View) = YearViewContainer(view)
                override fun bind(container: YearViewContainer, data: CalendarYear) {
                    container.textView.text = data.year.value.toString()
                }
            }
    }
}
