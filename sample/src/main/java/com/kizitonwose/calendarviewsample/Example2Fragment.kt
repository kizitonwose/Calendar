package com.kizitonwose.calendarviewsample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.google.android.material.snackbar.Snackbar
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarviewsample.databinding.Example2CalendarDayBinding
import com.kizitonwose.calendarviewsample.databinding.Example2CalendarHeaderBinding
import com.kizitonwose.calendarviewsample.databinding.Example2FragmentBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class Example2Fragment : BaseFragment(R.layout.example_2_fragment), HasToolbar, HasBackButton {

    override val toolbar: Toolbar?
        get() = binding.exTwoToolbar

    override val titleRes: Int = R.string.example_2_title

    private lateinit var binding: Example2FragmentBinding

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding = Example2FragmentBinding.bind(view)
        val daysOfWeek = daysOfWeekFromLocale()
        binding.legendLayout.root.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].name.first().toString()
                setTextColorRes(R.color.example_2_white)
            }
        }

        binding.exTwoCalendar.setup(YearMonth.now(), YearMonth.now().plusMonths(10), daysOfWeek.first())

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = Example2CalendarDayBinding.bind(view).exTwoDayText

            init {
                textView.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDate == day.date) {
                            selectedDate = null
                            binding.exTwoCalendar.notifyDayChanged(day)
                        } else {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            binding.exTwoCalendar.notifyDateChanged(day.date)
                            oldDate?.let { binding.exTwoCalendar.notifyDateChanged(oldDate) }
                        }
                        menuItem.isVisible = selectedDate != null
                    }
                }
            }
        }

        binding.exTwoCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.makeVisible()
                    when (day.date) {
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
        binding.exTwoCalendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                @SuppressLint("SetTextI18n") // Concatenation warning for `setText` call.
                container.textView.text = "${month.yearMonth.month.name.toLowerCase().capitalize()} ${month.year}"
            }
        }
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
            fragmentManager?.popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
