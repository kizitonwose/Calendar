package com.kizitonwose.calendar.sample.view

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.children
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
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
import com.kizitonwose.calendar.view.MarginValues
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.YearHeaderFooterBinder
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

class Example9Fragment : BaseFragment(R.layout.example_9_fragment), HasToolbar, HasBackButton {
    override val toolbar: Toolbar
        get() = binding.exNineToolbar

    override val titleRes: Int = R.string.example_9_title

    private lateinit var binding: Example9FragmentBinding

    private var selectedDate: LocalDate? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = Example9FragmentBinding.bind(view)
        applyInsets(binding)
        val config = requireContext().resources.configuration
        val isTablet = config.smallestScreenWidthDp >= 600

        configureBinders(isTablet)

        binding.exNineCalendar.apply {
            val currentMonth = YearMonth.now()
            monthVerticalSpacing = dpToPx(20, requireContext())
            monthHorizontalSpacing = dpToPx(if (isTablet) 52 else 10, requireContext())
            yearMargins = MarginValues(
                horizontal = dpToPx(if (isTablet) 52 else 14, requireContext()),
            )
            isMonthVisible = {
                it.yearMonth >= currentMonth
            }
            setup(
                Year.of(currentMonth.year),
                Year.of(currentMonth.year).plusYears(50),
                firstDayOfWeekFromLocale(),
            )
        }
    }

    private fun configureBinders(isTablet: Boolean) {
        val calendarView = binding.exNineCalendar

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = Example9CalendarDayBinding.bind(view).exNineDayText.apply {
                textSize = if (isTablet) 10f else 9f
            }

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

        val monthNameTypeFace = Typeface.semiBold(requireContext())

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val bind = Example9CalendarMonthHeaderBinding.bind(view)
            val textView = bind.exNineMonthHeaderText.apply {
                setTypeface(monthNameTypeFace)
                textSize = if (isTablet) 16f else 14f
                updatePaddingRelative(start = dpToPx(if (isTablet) 10 else 6, requireContext()))
            }
            val legendLayout = bind.legendLayout.root
        }

        val legendTypeface = Typeface.medium(requireContext())

        calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.textView.text = data.yearMonth.month.displayText(short = false)
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = true
                        val daysOfWeek = data.weekDays.first().map { it.date.dayOfWeek }
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].displayText(uppercase = true, narrow = true)
                                tv.setTextColorRes(R.color.example_3_black)
                                tv.textSize = if (isTablet) 14f else 11f
                                tv.setTypeface(legendTypeface)
                            }
                    }
                }
            }

        class YearViewContainer(view: View) : ViewContainer(view) {
            val textView = Example9CalendarYearHeaderBinding.bind(view).exNineYearHeaderText.apply {
                textSize = if (isTablet) 52f else 44f
                updatePadding(bottom = dpToPx(if (isTablet) 16 else 10, requireContext()))
            }
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

private fun applyInsets(binding: Example9FragmentBinding) {
    ViewCompat.setOnApplyWindowInsetsListener(
        binding.root,
    ) { _, windowInsets ->
        val insets = windowInsets.getInsets(systemBars())
        binding.exNineAppBarLayout.updatePadding(top = insets.top)
        binding.exNineCalendar.updatePadding(
            left = insets.left,
            right = insets.right,
            bottom = insets.bottom,
        )
        binding.exNineCalendar.clipToPadding = false
        windowInsets
    }
}
