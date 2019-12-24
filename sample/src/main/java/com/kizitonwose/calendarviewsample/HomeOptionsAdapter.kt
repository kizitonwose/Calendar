package com.kizitonwose.calendarviewsample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.home_options_item_view.*

data class ExampleItem(@StringRes val titleRes: Int, @StringRes val subtitleRes: Int)

class HomeOptionsAdapter(val onClick: (ExampleItem) -> Unit) :
    RecyclerView.Adapter<HomeOptionsAdapter.HomeOptionsViewHolder>() {

    val examples = listOf(
        ExampleItem(R.string.example_1_title, R.string.example_1_subtitle),
        ExampleItem(R.string.example_2_title, R.string.example_2_subtitle),
        ExampleItem(R.string.example_3_title, R.string.example_3_subtitle),
        ExampleItem(R.string.example_4_title, R.string.example_4_subtitle),
        ExampleItem(R.string.example_5_title, R.string.example_5_subtitle),
        ExampleItem(R.string.example_6_title, R.string.example_6_subtitle),
        ExampleItem(R.string.example_7_title, R.string.example_7_subtitle),
        ExampleItem(R.string.example_8_title, R.string.example_8_subtitle)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeOptionsViewHolder {
        return HomeOptionsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.home_options_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: HomeOptionsViewHolder, position: Int) {
        viewHolder.bind(examples[position])
    }

    override fun getItemCount(): Int = examples.size

    inner class HomeOptionsViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            itemView.setOnClickListener {
                onClick(examples[adapterPosition])
            }
        }

        fun bind(item: ExampleItem) {
            val context = itemView.context

            itemOptionTitle.text =
                if (item.titleRes != 0) context.getString(item.titleRes) else null
            itemOptionTitle.isVisible = itemOptionTitle.text.isNotBlank()

            itemOptionSubtitle.text =
                if (item.subtitleRes != 0) context.getString(item.subtitleRes) else null
            itemOptionSubtitle.isVisible = itemOptionSubtitle.text.isNotBlank()
        }
    }

}