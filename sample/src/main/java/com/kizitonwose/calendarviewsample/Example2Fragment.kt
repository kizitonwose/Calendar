package com.kizitonwose.calendarviewsample


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.utils.setTextColorRes
import kotlinx.android.synthetic.main.calendar_day_legend.*
import kotlinx.android.synthetic.main.example_2_calendar_day.view.*
import kotlinx.android.synthetic.main.exmaple_2_fragment.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate


class Example2Fragment : BaseFragment(), HasToolbar, HasBackButton {

    override val toolbar: Toolbar?
        get() = exTwoToolbar

    override val titleRes: Int = R.string.example_2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_2_fragment, container, false)
    }

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exTwoCalendar.dateViewBinder = { view, day ->
            val textView = view.exTwoText
            textView.text = day.date.dayOfMonth.toString()

            when (day.owner) {
                DayOwner.THIS_MONTH -> {
                    textView.makeVisible()
                    textView.setTextColorRes(R.color.example_2_black)
                }
                else -> {
                    textView.makeInVisible()
                    textView.setTextColorRes(R.color.example_1_white_light)
                }
            }

            when {
                selectedDate == day.date -> {
                    textView.setTextColorRes(R.color.example_2_white)
                    textView.setBackgroundResource(R.drawable.example_2_selected_bg)

                }
                today == day.date -> {
                    textView.setTextColorRes(R.color.example_2_red)
                    textView.background = null
                }
                else -> textView.background = null
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
                    exTwoCalendar.reloadDate(oldDate ?: return@dateClick)
                }
            }
        }

        legendLayout.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = DayOfWeek.values()[index].name.first().toString()
                setTextColorRes(R.color.example_2_white)
            }
        }

    }

}
