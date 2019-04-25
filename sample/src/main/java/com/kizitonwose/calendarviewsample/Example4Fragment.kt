package com.kizitonwose.calendarviewsample


import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.synthetic.main.example_4_calendar_day.view.*
import kotlinx.android.synthetic.main.example_4_calendar_header.view.*
import kotlinx.android.synthetic.main.exmaple_4_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter

class Example4Fragment : BaseFragment(), HasToolbar, HasBackButton {

    override val toolbar: Toolbar?
        get() = exFourToolbar

    override val titleRes: Int? = null

    private val today = LocalDate.now()

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    private var headerDateFormatter = DateTimeFormatter.ofPattern("EEE'\n'd MMM")

    private val startBackground: GradientDrawable by lazy {
        return@lazy requireContext().getDrawableCompat(R.drawable.example_4_continuous_selected_bg_start)!! as GradientDrawable
    }

    private val endBackground: GradientDrawable by lazy {
        return@lazy requireContext().getDrawableCompat(R.drawable.example_4_continuous_selected_bg_end)!! as GradientDrawable
    }

    private var radiusUpdated = false
    /**
     * We set the radius of the continuous selection background drawable dynamically
     * since the  view size is `match parent` hence we cannot determine the appropriate
     * radius value which would equal half of the view's size beforehand.
     */
    private fun updateDrawableRadius(textView: TextView) {
        if (radiusUpdated) return
        radiusUpdated = true
        val radius = (textView.height / 2).toFloat()
        startBackground.setCornerRadius(topLeft = radius, bottomLeft = radius)
        endBackground.setCornerRadius(topRight = radius, bottomRight = radius)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_4_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val now = YearMonth.now()
        exFourCalendar.setDateRange(now, now.plusMonths(12))
        exFourCalendar.scrollToMonth(now)

        exFourCalendar.dateViewBinder = { view, day ->
            val textView = view.exFourDayText
            val roundBgView = view.exFourRoundBgView

            textView.background = null
            roundBgView.makeInVisible()

            if (day.owner == DayOwner.THIS_MONTH) {
                textView.text = day.day.toString()

                if (day.date.isBefore(today)) {
                    textView.setTextColorRes(R.color.example_4_grey_past)
                } else {
                    when {
                        startDate == day.date && endDate == null -> {
                            textView.setTextColorRes(R.color.white)
                            roundBgView.makeVisible()
                            roundBgView.setBackgroundResource(R.drawable.example_4_single_selected_bg)
                        }
                        day.date == startDate -> {
                            textView.setTextColorRes(R.color.white)
                            updateDrawableRadius(textView)
                            textView.background = startBackground
                        }
                        startDate != null && endDate != null && (day.date > startDate && day.date < endDate) -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.example_4_continuous_selected_bg_middle)
                        }
                        day.date == endDate -> {
                            textView.setTextColorRes(R.color.white)
                            updateDrawableRadius(textView)
                            textView.background = endBackground
                        }
                        day.date == today -> {
                            textView.setTextColorRes(R.color.example_4_grey)
                            roundBgView.makeVisible()
                            roundBgView.setBackgroundResource(R.drawable.example_4_today_bg)
                        }
                        else -> textView.setTextColorRes(R.color.example_4_grey)
                    }
                }
            } else {
                textView.text = null

                 // <--- This part is to make the coloured selection background continuous across various months ---->

                val startDate = startDate
                val endDate = endDate
                if (startDate != null && endDate != null) {
                    // Mimic selection of inDates that are less than the startDate.
                    // Example: When 26 Feb 2019 is startDate and 5 Mar 2019 is endDate,
                    // this makes the inDates in Mar 2019 for 24 & 25 Feb 2019 look selected.
                    if ((day.owner == DayOwner.PREVIOUS_MONTH
                                && startDate.monthValue == day.date.monthValue
                                && endDate.monthValue != day.date.monthValue) ||
                        // Mimic selection of outDates that are greater than the endDate.
                        // Example: When 25 Apr 2019 is startDate and 2 May 2019 is endDate,
                        // this makes the outDates in Apr 2019 for 3 & 4 May 2019 look selected.
                        (day.owner == DayOwner.NEXT_MONTH
                                && startDate.monthValue != day.date.monthValue
                                && endDate.monthValue == day.date.monthValue) ||

                        // Mimic selection of in and out dates of intermediate
                        // months if the selection spans across multiple months.
                        (startDate < day.date && endDate > day.date
                                && startDate.monthValue != day.date.monthValue
                                && endDate.monthValue != day.date.monthValue)
                    ) {
                        textView.setBackgroundResource(R.drawable.example_4_continuous_selected_bg_middle)
                    }
                }
            }
        }

        exFourCalendar.dateClickListener = {
            if (it.owner == DayOwner.THIS_MONTH && (it.date == today || it.date.isAfter(today))) {
                val date = it.date
                if (startDate != null) {
                    if (date < startDate || endDate != null) {
                        startDate = date
                        endDate = null
                    } else if (date != startDate) {
                        endDate = date
                    }
                } else {
                    startDate = date
                }
                exFourCalendar.reloadCalendar()
                bindViews()
            }
        }

        exFourCalendar.monthHeaderBinder = { view, month ->
            val monthTitle = "${month.yearMonth.month.name.toLowerCase().capitalize()} ${month.year}"
            view.exFourHeaderText.text = monthTitle
        }

        bindViews()
    }

    override fun onStart() {
        super.onStart()
        val closeIndicator = requireContext().getDrawableCompat(R.drawable.ic_close)?.apply {
            setColorFilter(requireContext().getColorCompat(R.color.example_4_grey), PorterDuff.Mode.SRC_ATOP)
        }
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(closeIndicator)
    }

    private fun bindViews() {
        if (startDate != null) {
            exFourStartDateText.text = headerDateFormatter.format(startDate)
        } else {
            exFourStartDateText.text = getString(R.string.start_date)
        }
        if (endDate != null) {
            exFourEndDateText.text = headerDateFormatter.format(endDate)
        } else {
            exFourEndDateText.text = getString(R.string.end_date)
        }

        exFourSaveButton.isEnabled = startDate != null && endDate != null
    }
}
