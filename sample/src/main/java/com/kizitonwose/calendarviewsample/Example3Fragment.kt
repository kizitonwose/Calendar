package com.kizitonwose.calendarviewsample


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.DayOwner
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.example_3_calendar_day.view.*
import kotlinx.android.synthetic.main.exmaple_3_fragment.*
import kotlinx.android.synthetic.main.home_activity.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

data class Event(val id: String, val text: String, val date: LocalDate)

class Example3Fragment : BaseFragment(), HasBackButton {

    private val eventsAdapter = Example3EventsAdapter {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.example_3_dialog_delete_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteEvent(it)
            }
            .setNegativeButton(R.string.close, null)
            .show()
    }

    private val inputDialog by lazy {
        val editText = AppCompatEditText(requireContext())
        val layout = FrameLayout(requireContext()).apply {
            // Setting the padding on the EditText only pads the input area
            // not the entire EditText so we wrap it in a FrameLayout.
            val padding = dpToPx(20, requireContext())
            setPadding(padding, padding, padding, padding)
            addView(editText, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        }
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.example_3_input_dialog_title))
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                saveEvent(editText.text.toString())
                // Prepare EditText for reuse.
                editText.setText("")
            }
            .setNegativeButton(R.string.close, null)
            .create()
            .apply {
                setOnShowListener {
                    // Show the keyboard
                    val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                }
            }
    }

    override val titleRes: Int = R.string.example_3_title

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    private val titleSameYearFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    private val events = mutableMapOf<LocalDate, List<Event>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exmaple_3_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exThreeRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        exThreeRv.adapter = eventsAdapter
        exThreeRv.addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))

        val now = YearMonth.now()
        exThreeCalendar.setDateRange(now.minusMonths(10), now.plusMonths(10))
        exThreeCalendar.scrollToMonth(now)

        if (savedInstanceState == null) {
            exThreeCalendar.post {
                // Show today's events initially.
                selectDate(today)
            }
        }

        exThreeCalendar.dateViewBinder = { view, day ->
            val textView = view.exThreeDayText
            val dotView = view.exThreeDotView
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
                    dotView.makeInVisible()
                }
                selectedDate -> {
                    textView.setTextColorRes(R.color.example_3_blue)
                    textView.setBackgroundResource(R.drawable.example_3_selected_bg)
                    dotView.makeInVisible()
                }
                else -> {
                    textView.background = null
                    dotView.isVisible = events[day.date].orEmpty().isNotEmpty() && day.owner == DayOwner.THIS_MONTH
                }
            }
        }

        exThreeCalendar.dateClickListener = {
            if (it.owner == DayOwner.THIS_MONTH) {
                selectDate(it.date)
            }
        }

        exThreeCalendar.monthScrollListener = {
            requireActivity().homeToolbar.title = if (it.year == today.year) {
                titleSameYearFormatter.format(it.yearMonth)
            } else {
                titleFormatter.format(it.yearMonth)
            }

            selectDate(it.yearMonth.atDay(1))

            exThreeCalendar.post {
                exThreeCalendar.requestLayout()
            }
        }

        exThreeCalendar.monthHeaderBinder = { view, _ ->
            val legendLayout = view.legendLayout
            // Setup each header day text if we have not done that already.
            if ((legendLayout.children.first() as TextView).text.count() != 1) {
                val days = daysOfWeekFromSunday()
                legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                    tv.text = days[index].name.first().toString()
                    tv.setTextColorRes(R.color.example_3_black)
                }
            }
        }

        exThreeAddButton.setOnClickListener {
            inputDialog.show()
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { exThreeCalendar.reloadDate(it) }
            exThreeCalendar.reloadDate(date)
            updateAdapterForDate(date)
        }
    }

    private fun saveEvent(text: String) {
        if (text.isBlank()) {
            Toast.makeText(requireContext(), R.string.example_3_empty_input_text, Toast.LENGTH_LONG).show()
        } else {
            selectedDate?.let {
                events[it] = events[it].orEmpty().plus(Event(UUID.randomUUID().toString(), text, it))
                updateAdapterForDate(it)
            }
        }
    }

    private fun deleteEvent(event: Event) {
        val date = event.date
        events[date] = events[date].orEmpty().minus(event)
        updateAdapterForDate(date)
    }

    private fun updateAdapterForDate(date: LocalDate) {
        eventsAdapter.events.clear()
        eventsAdapter.events.addAll(events[date].orEmpty())
        eventsAdapter.notifyDataSetChanged()
        exThreeSelectedDateText.text = selectionFormatter.format(date)
    }
}
