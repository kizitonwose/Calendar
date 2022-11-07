package com.kizitonwose.calendar.sample.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.google.android.material.snackbar.Snackbar
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.databinding.Example2CalendarDayBinding
import com.kizitonwose.calendar.sample.databinding.Example2CalendarHeaderBinding
import com.kizitonwose.calendar.sample.databinding.Example2FragmentBinding
import com.kizitonwose.calendar.sample.shared.displayText
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class Example2Fragment : BaseFragment(R.layout.example_2_fragment), HasToolbar, HasBackButton {

    override val toolbar: Toolbar
        get() = binding.exTwoToolbar

    override val titleRes: Int = R.string.example_2_title

    private lateinit var binding: Example2FragmentBinding

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding = Example2FragmentBinding.bind(view)
        val daysOfWeek = daysOfWeek()
        binding.legendLayout.root.children.forEachIndexed { index, child ->
            (child as TextView).apply {
                text = daysOfWeek[index].name.first().toString()
                setTextColorRes(R.color.example_2_white)
            }
        }

        configureBinders()
        binding.exTwoCalendar.setup(
            YearMonth.now(),
            YearMonth.now().plusMonths(200),
            daysOfWeek.first(),
        )
    }

    private lateinit var menuItem: MenuItem
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.example_2_menu, menu)
        menuItem = menu.getItem(0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuItemDone) {
            val date = selectedDate ?: return false
            val text = "Selected: ${DateTimeFormatter.ofPattern("d MMMM yyyy").format(date)}"
            Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configureBinders() {
        val calendarView = binding.exTwoCalendar

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = Example2CalendarDayBinding.bind(view).exTwoDayText

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
                        menuItem.isVisible = selectedDate != null
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
            val textView = Example2CalendarHeaderBinding.bind(view).exTwoHeaderText
        }
        calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.textView.text = data.yearMonth.displayText()
                }
            }
    }
}
