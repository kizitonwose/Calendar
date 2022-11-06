package com.kizitonwose.calendar.sample.view

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.databinding.Example8CalendarDayBinding
import com.kizitonwose.calendar.sample.databinding.Example8FragmentBinding
import com.kizitonwose.calendar.sample.displayText
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth

class Example8Fragment : BaseFragment(R.layout.example_8_fragment), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    override val titleRes: Int = R.string.example_8_title

    private lateinit var binding: Example8FragmentBinding
    private val selectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = Example8FragmentBinding.bind(view)
        val daysOfWeek = daysOfWeek()
        binding.legendLayout.root.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                textView.text = daysOfWeek[index].displayText()
                textView.setTextColorRes(R.color.example_1_white)
            }

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
        binding.exEightCalendar.apply {
            dayBinder = object : MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    container.day = data
                    bindDate(data.date, container.textView, data.position == DayPosition.MonthDate)
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
        binding.exEightCalendar.notifyDateChanged(date)
    }

    private fun updateTitle() {
        val month = binding.exEightCalendar.findFirstVisibleMonth()?.yearMonth ?: return
        binding.exEightYearText.text = month.year.toString()
        binding.exEightMonthText.text = month.month.displayText(short = false)
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.statusBarColor =
            requireContext().getColorCompat(R.color.example_1_bg_light)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.statusBarColor =
            requireContext().getColorCompat(R.color.colorPrimaryDark)
    }
}
