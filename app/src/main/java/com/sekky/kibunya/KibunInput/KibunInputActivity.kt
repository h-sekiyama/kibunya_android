package com.sekky.kibunya.KibunInput

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityKibunInputBinding
import kotlinx.android.synthetic.main.tab_layout.view.*

class KibunInputActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityKibunInputBinding>(this, R.layout.activity_kibun_input)

        // タブ処理
        binding.tabLayout.tab_button1.setImageResource(R.drawable.tab1_on)
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