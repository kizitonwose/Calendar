package com.kizitonwose.calendarview.ui

import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

internal class CalenderPageSnapHelper : PagerSnapHelper() {

    /**
     * The default implementation of this method in [PagerSnapHelper.calculateDistanceToFinalSnap] uses the distance
     * between the target view center vs RecyclerView center as final snap distance. This does not always give the
     * desired result for calendar usage. For example in a vertical calendar when the RecyclerView is taller than
     * the item view(e.g two or more visible months), we don't actually want the item view's center to be at the
     * center of the RecyclerView when it snaps but instead we want the item view and RecyclerView top(in vertical)
     * or left(in horizontal) to match at the end of the snap.
     */
    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray {
        return IntArray(2).apply {
            this[0] = if (layoutManager.canScrollHorizontally())
                distanceToStart(targetView, getHorizontalHelper(layoutManager)) else 0

            this[1] = if (layoutManager.canScrollVertically())
                distanceToStart(targetView, getVerticalHelper(layoutManager)) else 0
        }
    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        val childStart = (helper.getDecoratedStart(targetView))
        val containerStart = helper.startAfterPadding
        return childStart - containerStart
    }

    private lateinit var verticalHelper: OrientationHelper
    private lateinit var horizontalHelper: OrientationHelper

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (!::verticalHelper.isInitialized || verticalHelper.layoutManager != layoutManager) {
            verticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return verticalHelper
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (!::horizontalHelper.isInitialized || horizontalHelper.layoutManager != layoutManager) {
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return horizontalHelper
    }
}
