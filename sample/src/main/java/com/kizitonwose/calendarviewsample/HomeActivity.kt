package com.kizitonwose.calendarviewsample

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarviewsample.databinding.HomeActivityBinding

class HomeActivity : AppCompatActivity() {

    internal lateinit var binding: HomeActivityBinding

    private val examplesAdapter = HomeOptionsAdapter {
        val fragment = it.createView()
        supportFragmentManager.beginTransaction()
            .run {
                if (fragment is Example1Fragment || fragment is Example4Fragment || fragment is Example5Fragment) {
                    return@run setCustomAnimations(
                        R.anim.slide_in_up,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_down
                    )
                }
                return@run setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            }
            .add(R.id.homeContainer, fragment, fragment.javaClass.simpleName)
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.homeToolbar)
        binding.examplesRv.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = examplesAdapter
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> onBackPressed().let { true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
