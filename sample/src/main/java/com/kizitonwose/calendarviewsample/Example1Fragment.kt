package com.kizitonwose.calendarviewsample


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.example_1_calendar_day.view.*
import kotlinx.android.synthetic.main.exmaple_1_fragment.*


class Example1Fragment : BaseFragment(), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    override val titleRes: Int = R.string.example_1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_1_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exOneCalendar.dateViewBinder = { view, day ->
            view.exOneText.text = day.date.dayOfMonth.toString()
        }
    }

}
