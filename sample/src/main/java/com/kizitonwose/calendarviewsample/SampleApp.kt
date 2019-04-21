package com.kizitonwose.calendarviewsample

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen


class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize ThreeTenABP library
        AndroidThreeTen.init(this)

    }

}