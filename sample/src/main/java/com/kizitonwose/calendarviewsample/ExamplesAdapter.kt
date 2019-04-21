package com.kizitonwose.calendarviewsample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.options_item_view.*

data class ExampleItem(@StringRes val titleRes: Int, @StringRes val subtitleRes: Int, val clazz: Class<*>)


class ExamplesAdapter(val onClick: (ExampleItem) -> Unit) :
    RecyclerView.Adapter<ExamplesAdapter.BaseObjectViewHolder>() {

    val examples = mutableListOf<ExampleItem>().apply {
        add(ExampleItem(R.string.example_1, 0, Example1Fragment::class.java))
        add(ExampleItem(R.string.example_2, 0, Example2Fragment::class.java))
        Unit
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseObjectViewHolder {
        return BaseObjectViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.options_item_view, parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: BaseObjectViewHolder, position: Int) {
        viewHolder.bind(examples[position])
    }

    override fun getItemCount(): Int = examples.size

    inner class BaseObjectViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            itemView.setOnClickListener {
                onClick(examples[adapterPosition])
            }
        }

        fun bind(item: ExampleItem) {
            val context = itemView.context

            itemOptionTitle.text = if (item.titleRes != 0) context.getString(item.titleRes) else null
            itemOptionTitle.isVisible = itemOptionTitle.text.isNotBlank()

            itemOptionSubtitle.text = if (item.subtitleRes != 0) context.getString(item.subtitleRes) else null
            itemOptionSubtitle.isVisible = itemOptionSubtitle.text.isNotBlank()
        }
    }

}