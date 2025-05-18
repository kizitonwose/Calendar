package com.kizitonwose.calendar.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updatePadding
import com.kizitonwose.calendar.sample.compose.CalendarComposeActivity
import com.kizitonwose.calendar.sample.databinding.HomeActivityBinding
import com.kizitonwose.calendar.sample.view.CalendarViewActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.activityToolbar)
        applyInsets(binding)
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

    private fun applyInsets(binding: HomeActivityBinding) {
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root,
        ) { _, windowInsets ->
            val insets = windowInsets.getInsets(systemBars())
            binding.activityAppBar.updatePadding(top = insets.top)
            binding.root.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom,
            )
            windowInsets
        }
    }
}
