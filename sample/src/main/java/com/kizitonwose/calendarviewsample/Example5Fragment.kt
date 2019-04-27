package com.kizitonwose.calendarviewsample


import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.example_5_calendar_day.view.*
import kotlinx.android.synthetic.main.exmaple_5_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter


class Example5Fragment : BaseFragment(), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    override val titleRes: Int = R.string.example_5_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_5_fragment, container, false)
    }

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val daysOfWeek = daysOfWeekFromLocale()

        val currentMonth = YearMonth.now()
        exFiveCalendar.setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
        exFiveCalendar.scrollToMonth(currentMonth)

        exFiveCalendar.dateViewBinder = { view, day ->
            val textView = view.exFiveDayText
            val container = view.exFiveDayLayout
            textView.text = day.date.dayOfMonth.toString()
            when (day.owner) {
                DayOwner.THIS_MONTH -> textView.setTextColorRes(R.color.example_5_text_grey)
                else -> textView.setTextColorRes(R.color.example_5_text_grey_light)
            }

            if (selectedDate == day.date) {
                container.setBackgroundResource(R.drawable.example_5_selected_bg)
            } else {
                container.background = null
            }
        }

        exFiveCalendar.dateClickListener = {
            if (it.owner == DayOwner.THIS_MONTH) {
                if (selectedDate != it.date) {
                    val oldDate = selectedDate
                    selectedDate = it.date
                    exFiveCalendar.reloadDate(it.date)
                    oldDate?.let { exFiveCalendar.reloadDate(oldDate) }
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

        exFiveCalendar.monthScrollListener = {
            val title = "${monthTitleFormatter.format(it.yearMonth)} ${it.yearMonth.year}"
            exFiveMonthYearText.text = title
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

}
