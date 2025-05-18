package com.kizitonwose.calendar.sample.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.databinding.CalendarViewActivityBinding

class CalendarViewActivity : AppCompatActivity() {
    internal lateinit var binding: CalendarViewActivityBinding

    private val examplesAdapter = CalendarViewOptionsAdapter {
        val fragment = it.createView()
        val anim = it.animation
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(anim.enter, anim.exit, anim.popEnter, anim.popExit)
            .add(R.id.homeContainer, fragment, fragment.javaClass.simpleName)
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CalendarViewActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.activityToolbar)
        applyInsets(binding)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.examplesRecyclerview.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = examplesAdapter
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed().let { true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun applyInsets(binding: CalendarViewActivityBinding) {
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root,
        ) { _, windowInsets ->
            val insets = windowInsets.getInsets(systemBars())
            binding.activityAppBar.updatePadding(top = insets.top)
            binding.examplesRecyclerview.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom,
            )
            windowInsets
        }
    }
}
