package com.sekky.kibunya.Kibunlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.Kibuns
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ListKibunItemBinding


class KibunsAdapter(
    private var items: List<Kibuns>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var listener: OnItemClickListener
    lateinit var longListener: OnItemLongClickListener
    constructor() : this(listOf())
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListKibunItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
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
            // ユーザーアイコン
            if (item.user_id == null) {
                return
            }
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("profileIcon/${item.user_id}.jpg")
            imageRef.getBytes(1000000).addOnSuccessListener {
                // 画像が存在すればそれを表示
                Glide.with(context!!)
                    .load(imageRef)
                    .placeholder(R.drawable.noimage)
                    .into(holder.binding.userIcon)
            }.addOnFailureListener {
                // 取得失敗したらデフォルト画像表示
                holder.binding.userIcon.setImageResource(R.drawable.noimage)
            }
            // ユーザー名
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(item.user_id)
            docRef.get()
                .addOnSuccessListener { document ->
                    holder.binding.userName.text = document.get("name").toString()
                }
            // 添付画像あり表示
            if (item.image != "") {
                holder.binding.imageIcon.visibility = View.VISIBLE
            }
            // 日記時間表示
            holder.binding.kibunTime.text = Functions.getTimeString(item.time)

            holder.binding.kibunItemLinearLayout.setOnClickListener {
                listener.onClick(it, item)
            }
            holder.binding.kibunItemLinearLayout.setOnLongClickListener {
                longListener.onLongClick(it, item)
                true
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(view: View, data: Kibuns)
    }

    interface OnItemLongClickListener {
        fun onLongClick(view: View, data: Kibuns)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun setOnItemLongClickLstener(longListener: OnItemLongClickListener) {
        this.longListener = longListener
    }

    fun addItems(kibuns: List<Kibuns>) {
        items = kibuns
        notifyItemRangeChanged(0, items.count())
    }

    override fun getItemCount(): Int = items.count()

    private class ListKibunViewHolder(val binding: ListKibunItemBinding): RecyclerView.ViewHolder(binding.root)
}