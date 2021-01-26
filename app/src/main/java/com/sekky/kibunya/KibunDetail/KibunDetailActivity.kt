package com.sekky.kibunya.KibunDetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.storage.FirebaseStorage
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.Other.OtherActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityKibunDetailBinding
import kotlinx.android.synthetic.main.tab_layout.view.*

class KibunDetailActivity: AppCompatActivity() {

    private val binding: ActivityKibunDetailBinding by lazy {
        DataBindingUtil.setContentView<ActivityKibunDetailBinding>(this, R.layout.activity_kibun_detail)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text: String? = intent.getStringExtra("text")
        val date: String? = intent.getStringExtra("date")
        val name: String? = intent.getStringExtra("name")
        val kibun: Int = intent.getIntExtra("kibun", 0)
        val time: String? = intent.getStringExtra("time")
        val image: String? = intent.getStringExtra("image")
        val userId: String? = intent.getStringExtra("userId")

        // CloudStorageを使う準備
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profileIcon/${userId}.jpg")

        // 日時
        binding.date = date
        // ユーザーアイコン
        imageRef.getBytes(1000000).addOnSuccessListener {
            // 画像が存在すればそれを表示
            Glide.with(this)
                .load(imageRef)
                .placeholder(R.drawable.noimage)
//                .signature(ObjectKey(System.currentTimeMillis()))
                .into(binding.userImage)
        }.addOnFailureListener {
            // 取得失敗したらデフォルト画像表示
            binding.userImage.setImageResource(R.drawable.noimage)
        }
        // ユーザー名
        binding.name = name
        // 気分アイコン
        when (kibun) {
            0 -> binding.kibun.setImageResource(R.drawable.kibun_icon0)
            1 -> binding.kibun.setImageResource(R.drawable.kibun_icon1)
            2 -> binding.kibun.setImageResource(R.drawable.kibun_icon2)
            3 -> binding.kibun.setImageResource(R.drawable.kibun_icon3)
            4 -> binding.kibun.setImageResource(R.drawable.kibun_icon4)
        }
        // 時間
        binding.time = time
        // 画像
        if (image != "") {
            Glide.with(this)
                .load(Uri.parse(image))
                .signature(ObjectKey(System.currentTimeMillis()))
                .into(binding.kibunImage)
            binding.kibunImage.visibility = View.VISIBLE
        }

        // 本文
        binding.kibunText = text

        // タブ処理
        binding.tabLayout.tab_button0.setImageResource(R.drawable.tab0_on)
        binding.tabLayout.tab_button0.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button1.setOnClickListener {
            val intent = Intent(this, KibunInputActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button2.setOnClickListener {
            val intent = Intent(this, OtherActivity::class.java)
            startActivity(intent)
        }

        // 戻るボタン
        binding.leftButton.setOnClickListener {
            finish()
        }
    }
}