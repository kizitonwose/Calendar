package com.kizitonwose.calendarviewsample

import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarviewsample.databinding.HomeOptionsItemViewBinding

data class ExampleItem(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val createView: () -> BaseFragment
)

class HomeOptionsAdapter(val onClick: (ExampleItem) -> Unit) :
    RecyclerView.Adapter<HomeOptionsAdapter.HomeOptionsViewHolder>() {

    val examples = listOf(
        ExampleItem(R.string.example_1_title, R.string.example_1_subtitle) { Example1Fragment() },
        ExampleItem(R.string.example_2_title, R.string.example_2_subtitle) { Example2Fragment() },
        ExampleItem(R.string.example_3_title, R.string.example_3_subtitle) { Example3Fragment() },
        ExampleItem(R.string.example_4_title, R.string.example_4_subtitle) { Example4Fragment() },
        ExampleItem(R.string.example_5_title, R.string.example_5_subtitle) { Example5Fragment() },
        ExampleItem(R.string.example_6_title, R.string.example_6_subtitle) { Example6Fragment() },
        ExampleItem(R.string.example_7_title, R.string.example_7_subtitle) { Example7Fragment() }
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeOptionsViewHolder {
        return HomeOptionsViewHolder(
            HomeOptionsItemViewBinding.inflate(parent.context.layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: HomeOptionsViewHolder, position: Int) {
        viewHolder.bind(examples[position])
    }

    override fun getItemCount(): Int = examples.size

    inner class HomeOptionsViewHolder(private val binding: HomeOptionsItemViewBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onClick(examples[bindingAdapterPosition])
            }
        }

        fun bind(item: ExampleItem) {
            val context = itemView.context
            binding.itemOptionTitle.apply {
                text = if (item.titleRes != 0) context.getString(item.titleRes) else null
                isVisible = text.isNotBlank()
            }

            binding.itemOptionSubtitle.apply {
                text = if (item.subtitleRes != 0) context.getString(item.subtitleRes) else null
                isVisible = text.isNotBlank()
            }
        }
    }
}
