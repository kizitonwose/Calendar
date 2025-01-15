package com.kizitonwose.calendar.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.view.internal.CalendarLayoutManager

/**
 * Helper class with properties that match the methods that can
 * be overridden in the internal [LinearLayoutManager]. This is
 * an abstract class instead of an interface so we can have
 * default values for properties as we need to call `super`
 * for properties that are not provided (null).
 */
public abstract class LayoutHelper {
    /**
     * Calculates the amount of extra space (in pixels) that should be laid out by
     * [CalendarLayoutManager] and stores the result in [extraLayoutSpace].
     * [extraLayoutSpace[0]] should be used for the extra space at the top in a vertical
     * calendar or left in a horizontal calendar, and extraLayoutSpace[1] should be used for
     * the extra space at the bottom in a vertical calendar or right in a horizontal calendar.
     *
     * @see [LinearLayoutManager.calculateExtraLayoutSpace]
     */
    public open val calculateExtraLayoutSpace: ((state: RecyclerView.State, extraLayoutSpace: IntArray) -> Unit)? = null
}
