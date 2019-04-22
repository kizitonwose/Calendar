package com.kizitonwose.calendarviewsample


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.synthetic.main.exmaple_2_fragment.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth


class Example3Fragment : BaseFragment(), HasToolbar, HasBackButton {

    override val toolbar: Toolbar?
        get() = exThreeToolbar

    override val titleRes: Int = R.string.example_3_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_3_fragment, container, false)
    }

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exThreeCalendar.setDateRange(YearMonth.now(), YearMonth.now().plusMonths(5))

        exThreeCalendar.dateViewBinder = { view, day ->
            val textView = view.exThreeDayText
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

        }

        exTwoCalendar.dateClickListener = dateClick@{

        }

        exThreeCalendar.monthHeaderBinder = { view, calMonth ->
            @SuppressLint("SetTextI18n") // Fix concatenation warning for `seText` call.
            view.exThreeHeaderText.text = "${calMonth.yearMonth.month.name.toLowerCase().capitalize()} ${calMonth.year}"
        }

//        legendLayout.children.forEachIndexed { index, view ->
//            (view as TextView).apply {
//                text = DayOfWeek.values()[index].name.first().toString()
//                setTextColorRes(R.color.example_2_white)
//            }
//        }

    }

}
