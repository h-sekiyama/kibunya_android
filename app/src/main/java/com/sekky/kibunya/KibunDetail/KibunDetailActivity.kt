package com.sekky.kibunya.KibunDetail

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityKibunDetailBinding
import kotlinx.android.synthetic.main.tab_layout.view.*

class KibunDetailActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityKibunDetailBinding>(this, R.layout.activity_kibun_detail)

        val intent: Intent = getIntent()
        val text: String? = intent.getStringExtra("text")
        val date: String? = intent.getStringExtra("date")
        val name: String? = intent.getStringExtra("name")
        val kibun: Int = intent.getIntExtra("kibun", 0)
        val time: String? = intent.getStringExtra("time")
        val image: String? = intent.getStringExtra("image")

        // 日時
        binding.date = date
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
    }
}