package com.sekky.kibunya.KibunDetail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sekky.kibunya.Comments
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ListCommentItemBinding

class CommentAdapter(
    private var items: List<Comments>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    constructor() : this(listOf())
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListCommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ListCommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is ListCommentViewHolder) {
            holder.binding.comments = item
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
            // 日記時間表示
            holder.binding.commentTime.text = Functions.getYearMonthDayTimeString(item.time)
        }
    }

    fun addItems(comments: List<Comments>) {
        items = comments
        notifyItemRangeChanged(0, items.count())
    }

    override fun getItemCount(): Int = items.count()

    private class ListCommentViewHolder(val binding: ListCommentItemBinding): RecyclerView.ViewHolder(binding.root)
}