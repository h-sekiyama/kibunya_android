package com.sekky.kibunya.Other

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ListFamilyItemBinding


class FamilyAdapter(
    private var items: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    constructor() : this(listOf())
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListFamilyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ListFamilyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is ListFamilyViewHolder) {
            holder.binding.families = item

            val db: FirebaseFirestore = FirebaseFirestore.getInstance()
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("profileIcon/${item}.jpg")
            imageRef.getBytes(1000000).addOnSuccessListener {
                // 画像が存在すればそれを表示
                Glide.with(context!!)
                    .load(imageRef)
                    .placeholder(R.drawable.noimage)
                    .signature(ObjectKey(System.currentTimeMillis()))
                    .into(holder.binding.userIcon)
            }.addOnFailureListener {
                // 取得失敗したらデフォルト画像表示
                holder.binding.userIcon.setImageResource(R.drawable.noimage)
            }
            // ユーザー名
            val docRef = db.collection("users").document(item)
            docRef.get()
                .addOnSuccessListener { document ->
                    holder.binding.userName.text = document.get("name").toString()
                }
        }
    }

    fun addItems(families: List<String>) {
        items = families
        notifyItemRangeChanged(0, items.count())
    }

    override fun getItemCount(): Int = items.count()

    private class ListFamilyViewHolder(val binding: ListFamilyItemBinding): RecyclerView.ViewHolder(binding.root)
}