package com.kizitonwose.calendar.sample.view

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.databinding.Example5CalendarDayBinding
import com.kizitonwose.calendar.sample.databinding.Example5CalendarHeaderBinding
import com.kizitonwose.calendar.sample.databinding.Example5EventItemViewBinding
import com.kizitonwose.calendar.sample.databinding.Example5FragmentBinding
import com.kizitonwose.calendar.sample.shared.Flight
import com.kizitonwose.calendar.sample.shared.displayText
import com.kizitonwose.calendar.sample.shared.flightDateTimeFormatter
import com.kizitonwose.calendar.sample.shared.generateFlights
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class Example5FlightsAdapter :
    RecyclerView.Adapter<Example5FlightsAdapter.Example5FlightsViewHolder>() {

    val flights = mutableListOf<Flight>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Example5FlightsViewHolder {
        return Example5FlightsViewHolder(
            Example5EventItemViewBinding.inflate(parent.context.layoutInflater, parent, false),
        )
    }

    override fun onBindViewHolder(viewHolder: Example5FlightsViewHolder, position: Int) {
        viewHolder.bind(flights[position])
    }

    override fun getItemCount(): Int = flights.size

    inner class Example5FlightsViewHolder(val binding: Example5EventItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(flight: Flight) {
            binding.itemFlightDateText.apply {
                text = flightDateTimeFormatter.format(flight.time)
                setBackgroundColor(itemView.context.getColorCompat(flight.color))
            }

            binding.itemDepartureAirportCodeText.text = flight.departure.code
            binding.itemDepartureAirportCityText.text = flight.departure.city

            binding.itemDestinationAirportCodeText.text = flight.destination.code
            binding.itemDestinationAirportCityText.text = flight.destination.city
        }
    }
}

class Example5Fragment : BaseFragment(R.layout.example_5_fragment), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    override val titleRes: Int = R.string.example_5_title

    private var selectedDate: LocalDate? = null

    private val flightsAdapter = Example5FlightsAdapter()
    private val flights = generateFlights().groupBy { it.time.toLocalDate() }

    private lateinit var binding: Example5FragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addStatusBarColorUpdate(R.color.example_5_toolbar_color)
        binding = Example5FragmentBinding.bind(view)

        binding.exFiveRv.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = flightsAdapter
        }
        flightsAdapter.notifyDataSetChanged()

        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(200)
        val endMonth = currentMonth.plusMonths(200)
        configureBinders(daysOfWeek)
        binding.exFiveCalendar.setup(startMonth, endMonth, daysOfWeek.first())
        binding.exFiveCalendar.scrollToMonth(currentMonth)

        binding.exFiveCalendar.monthScrollListener = { month ->
            binding.exFiveMonthYearText.text = month.yearMonth.displayText()

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                binding.exFiveCalendar.notifyDateChanged(it)
                updateAdapterForDate(null)
            }
        }

        binding.exFiveNextMonthImage.setOnClickListener {
            binding.exFiveCalendar.findFirstVisibleMonth()?.let {
                binding.exFiveCalendar.smoothScrollToMonth(it.yearMonth.nextMonth)
            }
        }

        binding.exFivePreviousMonthImage.setOnClickListener {
            binding.exFiveCalendar.findFirstVisibleMonth()?.let {
                binding.exFiveCalendar.smoothScrollToMonth(it.yearMonth.previousMonth)
            }
        }
    }

    private fun updateAdapterForDate(date: LocalDate?) {
        flightsAdapter.flights.clear()
        flightsAdapter.flights.addAll(flights[date].orEmpty())
        flightsAdapter.notifyDataSetChanged()
    }

    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = Example5CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            val binding = this@Example5Fragment.binding
                            binding.exFiveCalendar.notifyDateChanged(day.date)
                            oldDate?.let { binding.exFiveCalendar.notifyDateChanged(it) }
                            updateAdapterForDate(day.date)
                        }
                    }
                }
            }
        }
        binding.exFiveCalendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val context = container.binding.root.context
                val textView = container.binding.exFiveDayText
                val layout = container.binding.exFiveDayLayout
                textView.text = data.date.dayOfMonth.toString()

                val flightTopView = container.binding.exFiveDayFlightTop
                val flightBottomView = container.binding.exFiveDayFlightBottom
                flightTopView.background = null
                flightBottomView.background = null

                if (data.position == DayPosition.MonthDate) {
                    textView.setTextColorRes(R.color.example_5_text_grey)
                    layout.setBackgroundResource(if (selectedDate == data.date) R.drawable.example_5_selected_bg else 0)

                    val flights = flights[data.date]
                    if (flights != null) {
                        if (flights.count() == 1) {
                            flightBottomView.setBackgroundColor(context.getColorCompat(flights[0].color))
                        } else {
                            flightTopView.setBackgroundColor(context.getColorCompat(flights[0].color))
                            flightBottomView.setBackgroundColor(context.getColorCompat(flights[1].color))
                        }
                    }
                } else {
                    textView.setTextColorRes(R.color.example_5_text_grey_light)
                    layout.background = null
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = Example5CalendarHeaderBinding.bind(view).legendLayout.root
        }

        val typeFace = Typeface.create("sans-serif-light", Typeface.NORMAL)
        binding.exFiveCalendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = data.yearMonth
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].displayText(uppercase = true)
                                tv.setTextColorRes(R.color.white)
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                tv.typeface = typeFace
                            }
                    }
                }
            }
    }
}
