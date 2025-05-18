package com.kizitonwose.calendar.sample.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.material.appbar.AppBarLayout
import com.kizitonwose.calendar.sample.R

interface HasToolbar {
    val toolbar: Toolbar? // Return null to hide the toolbar
}

interface HasBackButton

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
    abstract val titleRes: Int?

    val activity: CalendarViewActivity
        get() = requireActivity() as CalendarViewActivity

    val activityToolbar: Toolbar
        get() = activity.binding.activityToolbar

    val activityAppBar: AppBarLayout
        get() = activity.binding.activityAppBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (this is MenuProvider) {
            requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.CREATED)
        }
    }

    override fun onStart() {
        super.onStart()
        if (this is HasToolbar) {
            // activityAppBar.makeGone() // Leaves blank space for some reason
            activityAppBar.updateLayoutParams<MarginLayoutParams> {
                height = 0
            }
            activity.setSupportActionBar(toolbar)
        }

        val actionBar = activity.supportActionBar
        if (this is HasBackButton) {
            actionBar?.title = if (titleRes != null) context?.getString(titleRes!!) else ""
            actionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            actionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    override fun onStop() {
        super.onStop()
        if (this is HasToolbar) {
            // activityAppBar.makeVisible()
            activityAppBar.updateLayoutParams<MarginLayoutParams> {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            activity.setSupportActionBar(activityToolbar)
        }

        val actionBar = activity.supportActionBar
        if (this is HasBackButton) {
            actionBar?.title = context?.getString(R.string.activity_title_view)
        }
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
