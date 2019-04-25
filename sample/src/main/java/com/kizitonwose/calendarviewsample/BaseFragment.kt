package com.kizitonwose.calendarviewsample


import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.home_activity.*

interface HasToolbar {
    val toolbar: Toolbar? // Return null to hide the toolbar
}

interface HasBackButton

abstract class BaseFragment : Fragment() {

    override fun onStart() {
        super.onStart()
        if (this is HasToolbar) {
            requireActivity().homeToolbar.makeGone()
            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        }

        if (this is HasBackButton) {
            val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
            actionBar?.title = if (titleRes != null) context?.getString(titleRes!!) else ""
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onStop() {
        super.onStop()
        if (this is HasToolbar) {
            requireActivity().homeToolbar.makeVisible()
            (requireActivity() as AppCompatActivity).setSupportActionBar(requireActivity().homeToolbar)
        }

        if (this is HasBackButton) {
            val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
            actionBar?.title = context?.getString(R.string.app_name)
            actionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    abstract val titleRes: Int?
}
