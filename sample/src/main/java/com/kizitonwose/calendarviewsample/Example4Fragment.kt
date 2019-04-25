package com.kizitonwose.calendarviewsample


import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.synthetic.main.example_4_calendar_day.view.*
import kotlinx.android.synthetic.main.exmaple_4_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter

class Example4Fragment : BaseFragment(), HasToolbar, HasBackButton {

    override val toolbar: Toolbar?
        get() = exFourToolbar

    override val titleRes: Int = R.string.example_4_title

    private val today = LocalDate.now()

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    private var headerDateFormatter = DateTimeFormatter.ofPattern("d EEE'\n'MMM")


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_4_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val now = YearMonth.now()
        exFourCalendar.setDateRange(now.minusMonths(10), now.plusMonths(10))
        exFourCalendar.scrollToMonth(now)

        exFourCalendar.dateViewBinder = { view, day ->
            val textView = view.exFourDayText
            textView.text = day.date.dayOfMonth.toString()

            when (day.owner) {
                DayOwner.THIS_MONTH -> {
                    textView.makeVisible()
                    textView.setTextColorRes(R.color.example_3_black)
                }
                else -> {
                    textView.makeInVisible()
                }
            }
            when (day.date) {
                today -> {
                    textView.setTextColorRes(R.color.example_3_white)
                    textView.setBackgroundResource(R.drawable.example_3_today_bg)
                }
                else -> {
                    textView.background = null
                }
            }
        }

        exFourCalendar.dateClickListener = {

        }
    }

    override fun onStart() {
        super.onStart()
        val closeIndicator = requireContext().getDrawableCompat(R.drawable.ic_close)?.apply {
            setColorFilter(requireContext().getColorCompat(R.color.example_4_grey), PorterDuff.Mode.SRC_ATOP)
        }
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(closeIndicator)
    }
}
