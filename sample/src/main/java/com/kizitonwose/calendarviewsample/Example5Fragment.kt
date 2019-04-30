package com.kizitonwose.calendarviewsample


import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.example_5_calendar_day.view.*
import kotlinx.android.synthetic.main.example_5_event_item_view.*
import kotlinx.android.synthetic.main.exmaple_5_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter


data class Flight(val time: LocalDateTime, val departure: Airport, val destination: Airport, @ColorRes val color: Int) {
    data class Airport(val city: String, val code: String)
}

class Example5FlightsAdapter : RecyclerView.Adapter<Example5FlightsAdapter.Example5FlightsViewHolder>() {

    val flights = mutableListOf<Flight>()

    private val formatter = DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Example5FlightsViewHolder {
        return Example5FlightsViewHolder(parent.inflate(R.layout.example_5_event_item_view))
    }

    override fun onBindViewHolder(viewHolder: Example5FlightsViewHolder, position: Int) {
        viewHolder.bind(flights[position])
    }

    override fun getItemCount(): Int = flights.size

    inner class Example5FlightsViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(flight: Flight) {
            itemFlightDateText.text = formatter.format(flight.time)
            itemFlightDateText.setBackgroundColor(itemView.context.getColorCompat(flight.color))

            itemDepartureAirportCodeText.text = flight.departure.code
            itemDepartureAirportCityText.text = flight.departure.city

            itemDestinationAirportCodeText.text = flight.destination.code
            itemDestinationAirportCityText.text = flight.destination.city
        }
    }
}


class Example5Fragment : BaseFragment(), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    override val titleRes: Int = R.string.example_5_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_5_fragment, container, false)
    }

    private var selectedDate: LocalDate? = null
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

    private val flightsAdapter = Example5FlightsAdapter()
    private val flights = generateFlights().groupBy { it.time.toLocalDate() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exFiveRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        exFiveRv.adapter = flightsAdapter
        exFiveRv.addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        flightsAdapter.notifyDataSetChanged()

        val daysOfWeek = daysOfWeekFromLocale()

        val currentMonth = YearMonth.now()
        exFiveCalendar.setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
        exFiveCalendar.scrollToMonth(currentMonth)

        exFiveCalendar.dateViewBinder = { view, day ->
            val textView = view.exFiveDayText
            val container = view.exFiveDayLayout
            textView.text = day.date.dayOfMonth.toString()

            val flightTopView = view.exFiveDayFlightTop
            val flightBottomView = view.exFiveDayFlightBottom

            flightTopView.background = null
            flightBottomView.background = null

            if (day.owner == DayOwner.THIS_MONTH) {
                textView.setTextColorRes(R.color.example_5_text_grey)
                container.setBackgroundResource(if (selectedDate == day.date) R.drawable.example_5_selected_bg else 0)

                val flights = flights[day.date]
                if (flights != null) {
                    if (flights.count() == 1) {
                        flightBottomView.setBackgroundColor(view.context.getColorCompat(flights[0].color))
                    } else {
                        flightTopView.setBackgroundColor(view.context.getColorCompat(flights[0].color))
                        flightBottomView.setBackgroundColor(view.context.getColorCompat(flights[1].color))
                    }
                }
            } else {
                textView.setTextColorRes(R.color.example_5_text_grey_light)
                container.background = null
            }

        }

        exFiveCalendar.dateClickListener = { day ->
            if (day.owner == DayOwner.THIS_MONTH) {
                if (selectedDate != day.date) {
                    val oldDate = selectedDate
                    selectedDate = day.date
                    exFiveCalendar.reloadDate(day.date)
                    oldDate?.let { exFiveCalendar.reloadDate(it) }
                    updateAdapterForDate(day.date)
                }
            }
        }

        exFiveCalendar.monthHeaderBinder = { view, month ->
            val legendLayout = view.legendLayout
            // Setup each header day text if we have not done that already.
            if (legendLayout.tag == null) {
                legendLayout.tag = month.yearMonth
                legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                    tv.text = daysOfWeek[index].name.take(3)
                    tv.setTextColorRes(R.color.example_5_text_grey)
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                }
            }
        }

        exFiveCalendar.monthScrollListener = { month ->
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            exFiveMonthYearText.text = title

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                exFiveCalendar.reloadDate(it)
                updateAdapterForDate(null)
            }
        }

        exFiveNextMonthImage.setOnClickListener {
            exFiveCalendar.getFirstVisibleMonth()?.let {
                exFiveCalendar.smoothScrollToMonth(it.next.yearMonth)
            }
        }

        exFivePreviousMonthImage.setOnClickListener {
            exFiveCalendar.getFirstVisibleMonth()?.let {
                exFiveCalendar.smoothScrollToMonth(it.previous.yearMonth)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.example_5_toolbar_color)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.colorPrimaryDark)
    }

    private fun updateAdapterForDate(date: LocalDate?) {
        flightsAdapter.flights.clear()
        flightsAdapter.flights.addAll(flights[date].orEmpty())
        flightsAdapter.notifyDataSetChanged()
    }

}
