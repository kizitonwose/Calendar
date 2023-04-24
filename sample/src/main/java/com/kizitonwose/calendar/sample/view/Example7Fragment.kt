package com.kizitonwose.calendar.sample.view

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.databinding.Example7CalendarDayBinding
import com.kizitonwose.calendar.sample.databinding.Example7FragmentBinding
import com.kizitonwose.calendar.sample.shared.displayText
import com.kizitonwose.calendar.sample.shared.getWeekPageTitle
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class Example7Fragment : BaseFragment(R.layout.example_7_fragment), HasToolbar, HasBackButton {

    override val titleRes: Int = R.string.example_7_title

    override val toolbar: Toolbar
        get() = binding.exSevenToolbar

    private var selectedDate = LocalDate.now()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd")

    private lateinit var binding: Example7FragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = Example7FragmentBinding.bind(view)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = Example7CalendarDayBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    if (selectedDate != day.date) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        binding.exSevenCalendar.notifyDateChanged(day.date)
                        oldDate?.let { binding.exSevenCalendar.notifyDateChanged(it) }
                    }
                }
            }

            fun bind(day: WeekDay) {
                this.day = day
                println("DayViewContainer bind: " + day.date)
                bind.exSevenDateText.text = dateFormatter.format(day.date)
                bind.exSevenDayText.text = day.date.dayOfWeek.displayText()

                val colorRes = if (day.date == selectedDate) {
                    R.color.example_7_yellow
                } else {
                    R.color.example_7_white
                }
                bind.exSevenDateText.setTextColor(view.context.getColorCompat(colorRes))
                bind.exSevenSelectedView.isVisible = day.date == selectedDate
                println("DayViewContainer bind over: " + day.date)
            }
        }

        binding.exSevenCalendar.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val selected = binding.exSevenCalendar.findFirstVisibleDay()?.date?.plusDays(2)
                        Toast.makeText(context, "Date selected: $selected", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )

        binding.exSevenCalendar.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                view.updateLayoutParams<ViewGroup.LayoutParams> {
                    val dm = DisplayMetrics()
                    val wm =
                        requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    wm.defaultDisplay.getMetrics(dm)
                    val dayWidth = dm.widthPixels / 5
                    val dayHeight = (dayWidth * 1.25).toInt()
                    width = dayWidth
                    height = dayHeight
                }
                return DayViewContainer(view)
            }

            override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
        }

        binding.exSevenCalendar.weekScrollListener = { weekDays ->
            binding.exSevenToolbar.title = getWeekPageTitle(weekDays)
        }

        val currentMonth = YearMonth.now()
        binding.exSevenCalendar.setup(
            currentMonth.minusMonths(5).atStartOfMonth(),
            currentMonth.plusMonths(5).atEndOfMonth(),
            firstDayOfWeekFromLocale(),
        )
        binding.exSevenCalendar.scrollToDate(LocalDate.now())
    }
}
