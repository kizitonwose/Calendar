package com.kizitonwose.calendar.sample.utils

import android.graphics.Point
import android.graphics.Rect
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.kizitonwose.calendar.sample.R
import java.lang.Thread.sleep

internal fun View.getRectInWindow(): Rect {
    val point = getLocationInWindow()
    return Rect(point.x, point.y, point.x + width, point.y + height)
}

internal fun View.getLocationInWindow(): Point {
    val location = IntArray(2).apply(::getLocationInWindow)
    return Point(location[0], location[1])
}

internal fun runOnMain(action: () -> Unit) {
    InstrumentationRegistry.getInstrumentation().runOnMainSync(action)
}

internal fun openExampleAt(position: Int) {
    Espresso.onView(ViewMatchers.withId(R.id.examplesRecyclerview))
        .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(position, click()))
}

internal fun <T : View> ActivityScenarioRule<*>.getView(@IdRes id: Int): T {
    lateinit var view: T
    this.scenario.onActivity { activity ->
        view = activity.findViewById(id)
    }
    sleep(1000)
    return view
}
