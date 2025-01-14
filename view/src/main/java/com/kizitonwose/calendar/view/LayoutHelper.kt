package com.kizitonwose.calendar.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.view.internal.CalendarLayoutManager

/**
 * An interface with methods that can be overridden
 * in the internal [LinearLayoutManager].
 */
public interface LayoutHelper {
    /**
     * Calculates the amount of extra space (in pixels) that should be laid out by
     * [CalendarLayoutManager] and stores the result in [extraLayoutSpace].
     * [extraLayoutSpace[0]] should be used for the extra space at the top in a vertical
     * calendar or left in a horizontal calendar, and extraLayoutSpace[1] should be used for
     * the extra space at the bottom in a vertical calendar or right in a horizontal calendar.
     *
     * @see [LinearLayoutManager.calculateExtraLayoutSpace]
     */
    public fun calculateExtraLayoutSpace(state: RecyclerView.State, extraLayoutSpace: IntArray) {}
}
