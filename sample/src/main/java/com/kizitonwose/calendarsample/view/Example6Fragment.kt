package com.kizitonwose.calendarsample.view

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import com.kizitonwose.calendarcore.CalendarDay
import com.kizitonwose.calendarcore.CalendarMonth
import com.kizitonwose.calendarcore.DayPosition
import com.kizitonwose.calendarcore.daysOfWeek
import com.kizitonwose.calendarsample.R
import com.kizitonwose.calendarsample.databinding.Example6CalendarDayBinding
import com.kizitonwose.calendarsample.databinding.Example6CalendarHeaderBinding
import com.kizitonwose.calendarsample.databinding.Example6FragmentBinding
import com.kizitonwose.calendarview.MonthDayBinder
import com.kizitonwose.calendarview.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ViewContainer
import com.kizitonwose.calendarview.internal.MarginValues
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// We assign this class to the `monthViewClass` attribute in XML.
// See usage in example_6_fragment.xml
class Example6MonthView(context: Context) : CardView(context) {

    init {
        setCardBackgroundColor(context.getColorCompat(R.color.example_6_month_bg_color))
        radius = dpToPx(8, context).toFloat()
        elevation = 8f
    }
}

class Example6Fragment : BaseFragment(R.layout.example_6_fragment), HasBackButton {

    override val titleRes: Int = R.string.example_6_title

    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    private lateinit var binding: Example6FragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = Example6FragmentBinding.bind(view)
        binding.exSixCalendar.apply {
            // Add margins around our card view.
            val horizontalMargin = dpToPx(8, requireContext())
            val verticalMargin = dpToPx(14, requireContext())
            monthMargins = MarginValues(
                start = horizontalMargin,
                end = horizontalMargin,
                top = verticalMargin,
                bottom = verticalMargin,
            )
        }

        // We don't want a square calendar.
        val dayWidth = dpToPx(28, view.context)
        val dayHeight = dpToPx(60, view.context)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = Example6CalendarDayBinding.bind(view).exSixDayText
        }
        binding.exSixCalendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                view.updateLayoutParams<ViewGroup.LayoutParams> {
                    width = dayWidth
                    height = dayHeight
                }
                return DayViewContainer(view)
            }

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val textView = container.textView

                if (day.position == DayPosition.MonthDate) {
                    textView.text = day.date.dayOfMonth.toString()
                    textView.makeVisible()
                } else {
                    textView.makeInVisible()
                }
            }
        }

        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        binding.exSixCalendar.setup(
            currentMonth.minusMonths(10),
            currentMonth.plusMonths(10),
            daysOfWeek.first())
        binding.exSixCalendar.scrollToMonth(currentMonth)

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val binding = Example6CalendarHeaderBinding.bind(view)
            val textView = binding.exSixMonthText
            val legendLayout = binding.legendLayout.root
        }
        binding.exSixCalendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                    container.textView.text = titleFormatter.format(month.yearMonth)
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = month.yearMonth
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].name.first().toString()
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                                tv.setTextColorRes(R.color.example_6_black)
                            }
                    }
                }
            }
    }
}
