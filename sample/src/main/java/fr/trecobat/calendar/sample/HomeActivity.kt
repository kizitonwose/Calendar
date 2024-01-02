package fr.trecobat.calendar.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.trecobat.calendar.sample.compose.CalendarComposeActivity
import fr.trecobat.calendar.sample.databinding.HomeActivityBinding
import fr.trecobat.calendar.sample.view.CalendarViewActivity

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
