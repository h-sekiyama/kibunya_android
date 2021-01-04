package com.sekky.kibunya

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sekky.kibunya.databinding.ListKibunItemBinding

class KibunsAdapter(
    private var items: List<Kibuns>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    constructor() : this(listOf())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListKibunItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatCommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is ChatCommentViewHolder) {
            holder.binding.kibuns = item
        }
    }

    fun addItems(chats: List<Kibuns>) {
        items = chats
        notifyItemRangeChanged(0, items.count())
    }


    override fun getItemCount(): Int = items.count()

    private class ChatCommentViewHolder(val binding: ListKibunItemBinding)
        : RecyclerView.ViewHolder(binding.root)
}