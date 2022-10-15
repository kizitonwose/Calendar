package com.kizitonwose.calendar.sample.view

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.databinding.Example6CalendarDayBinding
import com.kizitonwose.calendar.sample.databinding.Example6CalendarHeaderBinding
import com.kizitonwose.calendar.sample.databinding.Example6FragmentBinding
import com.kizitonwose.calendar.sample.displayText
import com.kizitonwose.calendar.view.MarginValues
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.YearMonth

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

        val daysOfWeek = daysOfWeek()
        configureBinders(daysOfWeek)
        val currentMonth = YearMonth.now()
        binding.exSixCalendar.setup(
            currentMonth.minusMonths(10),
            currentMonth.plusMonths(10),
            daysOfWeek.first()
        )
        binding.exSixCalendar.scrollToMonth(currentMonth)
    }

    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        // We don't want a square calendar.
        val dayWidth = dpToPx(28, requireContext())
        val dayHeight = dpToPx(60, requireContext())

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

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                val textView = container.textView

                if (data.position == DayPosition.MonthDate) {
                    textView.text = data.date.dayOfMonth.toString()
                    textView.makeVisible()
                } else {
                    textView.makeInVisible()
                }
            }
        }
        class MonthViewContainer(view: View) : ViewContainer(view) {
            val binding = Example6CalendarHeaderBinding.bind(view)
            val textView = binding.exSixMonthText
            val legendLayout = binding.legendLayout.root
        }
        binding.exSixCalendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.textView.text = data.yearMonth.displayText(short = true)
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = data.yearMonth
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
