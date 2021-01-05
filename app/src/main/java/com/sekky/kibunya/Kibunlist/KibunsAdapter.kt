package com.sekky.kibunya.Kibunlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sekky.kibunya.Kibuns
import com.sekky.kibunya.databinding.ListKibunItemBinding

class KibunsAdapter(
    private var items: List<Kibuns>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var listener: OnItemClickListener
    constructor() : this(listOf())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListKibunItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatCommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is ChatCommentViewHolder) {
            holder.binding.kibuns = item
            holder.binding.kibunItemLinearLayout.setOnClickListener( {
                listener.onClick(it, item)
            })
        }
    }

    interface OnItemClickListener {
        fun onClick(view: View, data: Kibuns)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun addItems(chats: List<Kibuns>) {
        items = chats
        notifyItemRangeChanged(0, items.count())
    }


    override fun getItemCount(): Int = items.count()

    private class ChatCommentViewHolder(val binding: ListKibunItemBinding)
        : RecyclerView.ViewHolder(binding.root)
}