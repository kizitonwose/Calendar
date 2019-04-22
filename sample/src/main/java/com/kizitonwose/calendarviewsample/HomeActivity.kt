package com.kizitonwose.calendarviewsample

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_activity.*


class HomeActivity : AppCompatActivity() {

    private val examplesAdapter = HomeOptionsAdapter {
        val instance = it.clazz.getConstructor().newInstance() as Fragment
        supportFragmentManager.beginTransaction()
            .add(R.id.homeContainer, instance, it.clazz.simpleName)
            .addToBackStack(it.clazz.simpleName)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
        setSupportActionBar(homeToolbar)
        examplesRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        examplesRv.adapter = examplesAdapter
        examplesRv.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> onBackPressed().let { true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
