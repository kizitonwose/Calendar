package com.kizitonwose.calendarviewsample


import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.example_7_calendar_day.view.*
import kotlinx.android.synthetic.main.example_7_fragment.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter


class Example7Fragment : BaseFragment(), HasToolbar, HasBackButton {

    override val titleRes: Int = R.string.example_7_title

    override val toolbar: Toolbar?
        get() = exSevenToolbar

    private var selectedDate: LocalDate? = null

    private val dateFormatter = DateTimeFormatter.ofPattern("dd")
    private val dayFormatter = DateTimeFormatter.ofPattern("EEE")
    private val monthFormatter = DateTimeFormatter.ofPattern("MMM")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.example_7_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dm = DisplayMetrics()
        val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(dm)

        exSevenCalendar.dayWidth = dm.widthPixels / 5

        exSevenCalendar.dayHeight = (exSevenCalendar.dayWidth * 1.25).toInt()

        class DayViewContainer(view: View) : ViewContainer(view) {
            val dayText = view.exSevenDayText
            val dateText = view.exSevenDateText
            val monthText = view.exSevenMonthText
            val selectedView = view.exSevenSelectedView

            lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    val firstDay = exSevenCalendar.findFirstVisibleDay()
                    val lastDay = exSevenCalendar.findLastVisibleDay()
                    if (firstDay == day) {
                        // If the first date on screen was clicked, we scroll to the date to ensure
                        // it is fully visible if it was partially off the screen when clicked.
                        exSevenCalendar.smoothScrollToDate(day.date)
                    } else if (lastDay == day) {
                        // If the last date was clicked, we scroll to 4 days ago, this forces the
                        // clicked date to be fully visible if it was partially off the screen.
                        // We scroll to 4 days ago because we show max of five days on the screen
                        // so scrolling to 4 days ago brings the clicked date into full visibility
                        // at the end of the calendar view.
                        exSevenCalendar.smoothScrollToDate(day.date.minusDays(4))
                    }

                    // Example: If you want the clicked date to always be centered on the screen,
                    // you would use: exSevenCalendar.smoothScrollToDate(day.date.minusDays(2))

                    if (selectedDate != day.date) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        exSevenCalendar.notifyDateChanged(day.date)
                        oldDate?.let { exSevenCalendar.notifyDateChanged(it) }
                    }
                }
            }

            fun bind(day: CalendarDay) {
                this.day = day
                dateText.text = dateFormatter.format(day.date)
                dayText.text = dayFormatter.format(day.date)
                monthText.text = monthFormatter.format(day.date)

                dateText.setTextColor(view.context.getColorCompat(if (day.date == selectedDate) R.color.example_7_yellow else R.color.example_7_white))
                selectedView.isVisible = day.date == selectedDate
            }
        }

        exSevenCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) = container.bind(day)
        }

        val currentMonth = YearMonth.now()
        // Value for firstDayOfWeek does not matter since inDates and outDates are not generated.
        exSevenCalendar.setup(currentMonth, currentMonth.plusMonths(3), DayOfWeek.values().random())
        exSevenCalendar.scrollToDate(LocalDate.now())
    }
}
