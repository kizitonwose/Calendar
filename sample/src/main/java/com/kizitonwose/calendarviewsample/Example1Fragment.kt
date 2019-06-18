package com.kizitonwose.calendarviewsample


import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
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
                    Toast.makeText(it.context, day.date.toString(), Toast.LENGTH_LONG).show()
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

        weekModeCheckBox.setOnCheckedChangeListener { _, monthToWeek ->
            // We want the first visible day to remain visible when we change to week mode.
            val visibleDay = exOneCalendar.findFirstVisibleDay()

            val oneWeekHeight = exOneCalendar.dayHeight
            val oneMonthHeight = oneWeekHeight * 6

            val oldHeight = if (monthToWeek) oneMonthHeight else oneWeekHeight
            val newHeight = if (monthToWeek) oneWeekHeight else oneMonthHeight

            // Animate calendar height changes.
            val animator = ValueAnimator.ofInt(oldHeight, newHeight)
            animator.addUpdateListener { animator ->
                exOneCalendar.layoutParams = exOneCalendar.layoutParams.apply {
                    height = animator.animatedValue as Int
                }
            }
            animator.doOnStart {
                if (!monthToWeek) {
                    exOneCalendar.inDateStyle = InDateStyle.ALL_MONTHS
                    exOneCalendar.maxRowCount = 6
                    exOneCalendar.hasBoundaries = true
                }
            }
            animator.doOnEnd {
                if (monthToWeek) {
                    exOneCalendar.inDateStyle = InDateStyle.FIRST_MONTH
                    exOneCalendar.maxRowCount = 1
                    exOneCalendar.hasBoundaries = false
                }

                visibleDay?.let {
                    val actualDate = if (it.owner == DayOwner.PREVIOUS_MONTH) {
                        it.date.yearMonth.plusMonths(1).atDay(1)
                    } else {
                        it.date
                    }
                    exOneCalendar.scrollToDate(actualDate)
                }
            }
            animator.duration = 250
            animator.start()
        }

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
