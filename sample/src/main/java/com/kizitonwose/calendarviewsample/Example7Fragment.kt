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
        // Value for firstDayOfWeek does not matter since "hasBoundaries" is false.
        exSevenCalendar.setup(currentMonth, currentMonth.plusMonths(1), DayOfWeek.FRIDAY)
        exSevenCalendar.scrollToDate(currentMonth.atDay(1))
    }
}
