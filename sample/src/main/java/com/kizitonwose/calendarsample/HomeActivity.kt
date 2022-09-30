package com.kizitonwose.calendarsample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kizitonwose.calendarsample.compose.CalendarComposeActivity
import com.kizitonwose.calendarsample.databinding.HomeActivityBinding
import com.kizitonwose.calendarsample.view.CalendarViewActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.activityToolbar)
        handleClicks(binding)
    }

    private fun handleClicks(binding: HomeActivityBinding) {
        binding.calendarViewSample.setOnClickListener {
            startActivity(Intent(this, CalendarViewActivity::class.java))
        }
        binding.calendarComposeSample.setOnClickListener {
            startActivity(Intent(this, CalendarComposeActivity::class.java))
        }
    }
}
