package com.kizitonwose.calendar.sample.view

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.databinding.Example8CalendarDayBinding
import com.kizitonwose.calendar.sample.databinding.Example8CalendarFooterBinding
import com.kizitonwose.calendar.sample.databinding.Example8CalendarHeaderBinding
import com.kizitonwose.calendar.sample.databinding.Example8FragmentBinding
import com.kizitonwose.calendar.sample.shared.displayText
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth

class Example8Fragment : BaseFragment(R.layout.example_8_fragment), HasToolbar {

    override val toolbar: Toolbar? = null

    override val titleRes: Int? = null

    private lateinit var binding: Example8FragmentBinding
    private val selectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addStatusBarColorUpdate(R.color.example_1_bg_light)
        binding = Example8FragmentBinding.bind(view)
        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = Example8CalendarDayBinding.bind(view).exEightDayText

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        dateClicked(date = day.date)
                    }
                }
            }
        }

        class MonthHeaderViewContainer(view: View) : ViewContainer(view) {
            val binding = Example8CalendarHeaderBinding.bind(view)
        }

        class MonthFooterViewContainer(view: View) : ViewContainer(view) {
            val binding = Example8CalendarFooterBinding.bind(view)
        }
        binding.exEightCalendar.apply {
            dayBinder = object : MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    container.day = data
                    bindDate(data.date, container.textView, data.position == DayPosition.MonthDate)
                }
            }
            monthHeaderBinder = object : MonthHeaderFooterBinder<MonthHeaderViewContainer> {
                override fun create(view: View) = MonthHeaderViewContainer(view)
                override fun bind(container: MonthHeaderViewContainer, data: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.binding.root.tag == null) {
                        container.binding.root.tag = data.yearMonth
                        container.binding.legendLayout.root.children
                            .map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].displayText()
                                tv.setTextColorRes(R.color.example_1_white)
                            }
                    }
                }
            }
            monthFooterBinder = object : MonthHeaderFooterBinder<MonthFooterViewContainer> {
                override fun create(view: View) = MonthFooterViewContainer(view)
                override fun bind(container: MonthFooterViewContainer, data: CalendarMonth) {
                    val count = data.weekDays.flatten()
                        .count {
                            it.position == DayPosition.MonthDate &&
                                selectedDates.contains(it.date)
                        }
                    container.binding.root.text = if (count == 0) {
                        getString(R.string.example_8_zero_selection)
                    } else {
                        resources.getQuantityString(R.plurals.example_8_selection, count, count)
                    }
                }
            }
            monthScrollListener = { updateTitle() }
            setup(startMonth, endMonth, daysOfWeek.first())
            scrollToMonth(currentMonth)
        }
    }

    private fun bindDate(date: LocalDate, textView: TextView, isSelectable: Boolean) {
        textView.text = date.dayOfMonth.toString()
        if (isSelectable) {
            when {
                selectedDates.contains(date) -> {
                    textView.setTextColorRes(R.color.example_1_bg)
                    textView.setBackgroundResource(R.drawable.example_8_selected_bg)
                }
                today == date -> {
                    textView.setTextColorRes(R.color.example_1_white)
                    textView.setBackgroundResource(R.drawable.example_8_today_bg)
                }
                else -> {
                    textView.setTextColorRes(R.color.example_1_white)
                    textView.background = null
                }
            }
        } else {
            textView.setTextColorRes(R.color.example_1_white_light)
            textView.background = null
        }
    }

    private fun dateClicked(date: LocalDate) {
        if (selectedDates.contains(date)) {
            selectedDates.remove(date)
        } else {
            selectedDates.add(date)
        }
        // We want to reload the footer text as well.
        binding.exEightCalendar.notifyMonthChanged(date.yearMonth)
    }

    private fun updateTitle() {
        val month = binding.exEightCalendar.findFirstVisibleMonth()?.yearMonth ?: return
        binding.exEightYearText.text = month.year.toString()
        binding.exEightMonthText.text = month.month.displayText(short = false)
    }
}
