package com.sekky.kibunya.Kibunlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.Kibuns
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ListKibunItemBinding


class KibunsAdapter(
    private var items: List<Kibuns>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var listener: OnItemClickListener
    constructor() : this(listOf())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListKibunItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListKibunViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is ListKibunViewHolder) {
            holder.binding.kibuns = item
            when (item.kibun) {
                0 -> holder.binding.kibunIcon.setImageResource(R.drawable.kibun_icon0)
                1 -> holder.binding.kibunIcon.setImageResource(R.drawable.kibun_icon1)
                2 -> holder.binding.kibunIcon.setImageResource(R.drawable.kibun_icon2)
                3 -> holder.binding.kibunIcon.setImageResource(R.drawable.kibun_icon3)
                4 -> holder.binding.kibunIcon.setImageResource(R.drawable.kibun_icon4)
            }
            // 日記時間表示
            holder.binding.kibunTime.text = Functions.getTimeString(item.time)
            holder.binding.kibunItemLinearLayout.setOnClickListener {
                listener.onClick(it, item)
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(view: View, data: Kibuns)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun addItems(kibuns: List<Kibuns>) {
        items = kibuns
        notifyItemRangeChanged(0, items.count())
    }

    override fun getItemCount(): Int = items.count()

    private class ListKibunViewHolder(val binding: ListKibunItemBinding): RecyclerView.ViewHolder(binding.root)
}