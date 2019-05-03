package com.kizitonwose.calendarviewsample


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.google.android.material.snackbar.Snackbar
import com.kizitonwose.calendarview.adapter.DateViewBinder
import com.kizitonwose.calendarview.adapter.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.adapter.ViewContainer
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.synthetic.main.calendar_day_legend.*
import kotlinx.android.synthetic.main.example_2_calendar_day.view.*
import kotlinx.android.synthetic.main.example_2_calendar_header.view.*
import kotlinx.android.synthetic.main.exmaple_2_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter


class Example2Fragment : BaseFragment(), HasToolbar, HasBackButton {

    override val toolbar: Toolbar?
        get() = exTwoToolbar

    override val titleRes: Int = R.string.example_2_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.exmaple_2_fragment, container, false)
    }

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val daysOfWeek = daysOfWeekFromLocale()
        legendLayout.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].name.first().toString()
                setTextColorRes(R.color.example_2_white)
            }
        }

        exTwoCalendar.setup(YearMonth.now(), YearMonth.now().plusMonths(10), daysOfWeek.first())

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.exTwoDayText
        }
        exTwoCalendar.dateViewBinder = object : DateViewBinder<DayViewContainer> {
            override fun provide(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
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

        exTwoCalendar.dateClickListener = dateClick@{
            if (it.owner == DayOwner.THIS_MONTH) {
                if (selectedDate == it.date) {
                    selectedDate = null
                    exTwoCalendar.reloadDay(it)
                } else {
                    val oldDate = selectedDate
                    selectedDate = it.date
                    exTwoCalendar.reloadDate(it.date)
                    oldDate?.let { exTwoCalendar.reloadDate(oldDate) }
                }
                menuItem.isVisible = selectedDate != null
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = view.exTwoHeaderText
        }
        exTwoCalendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun provide(view: View) = MonthViewContainer(view)
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
