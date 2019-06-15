package com.kizitonwose.calendarviewsample


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.calendar_day_legend.*
import kotlinx.android.synthetic.main.example_1_calendar_day.view.*
import kotlinx.android.synthetic.main.exmaple_1_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter


class Example1Fragment : BaseFragment(), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    override val titleRes: Int = R.string.example_1_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_1_fragment, container, false)
    }

    private val selectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val daysOfWeek = daysOfWeekFromLocale()
        legendLayout.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].name.take(3).toUpperCase()
                setTextColorRes(R.color.example_1_white_light)
            }
        }

        val currentMonth = YearMonth.now()
        exOneCalendar.setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
        exOneCalendar.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = view.exOneDayText

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDates.contains(day.date)) {
                            selectedDates.remove(day.date)
                        } else {
                            selectedDates.add(day.date)
                        }
                        exOneCalendar.notifyDayChanged(day)
                    }
                }
            }
        }
        exOneCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    when {
                        selectedDates.contains(day.date) -> {
                            textView.setTextColorRes(R.color.example_1_bg)
                            textView.setBackgroundResource(R.drawable.example_1_selected_bg)

                        }
                        today == day.date -> {
                            textView.setTextColorRes(R.color.example_1_white)
                            textView.setBackgroundResource(R.drawable.example_1_today_bg)
                        }
                        else -> {
                            textView.setTextColorRes(R.color.example_1_white)
                            textView.background = null
                        }
                    }
                } else {
                    textView.setTextColorRes(R.color.example_1_white_light)
                    textView.background = null
                }
            }
        }

        exOneCalendar.monthScrollListener = {
            exOneYearText.text = it.yearMonth.year.toString()
            exOneMonthText.text = monthTitleFormatter.format(it.yearMonth)
        }

        maxRowSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (seekBar.progress == 0) {
                    seekBar.progress = 1 // SeekBar progress starts at 0
                } else {
                    // We want the first visible day to remain visible when the
                    // number of visible rows change. Though it may not remain
                    // the first visible date if maxRowCount increases.
                    val visibleDay = exOneCalendar.findFirstVisibleDay()
                    exOneCalendar.maxRowCount = seekBar.progress
                    visibleDay?.let { exOneCalendar.scrollToDay(it) }

                    maxRowText.text = getString(R.string.max_row_count, seekBar.progress)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Update text initially.
        maxRowText.text = getString(R.string.max_row_count, maxRowSeekBar.progress)
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.example_1_bg_light)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.colorPrimaryDark)
    }

}
