package com.sekky.kibunya.Kibunlist

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout.VERTICAL
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.KibunDetail.KibunDetailActivity
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibuns
import com.sekky.kibunya.Other.AddFamilyActivity
import com.sekky.kibunya.Other.OtherActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.tab_layout.view.*

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 招待から起動した場合はユーザーIDを取得し家族追加画面に遷移
        val uri: Uri? = this.intent.data
        if (uri != null) {
            val intent = Intent(this, AddFamilyActivity::class.java).apply {
                putExtra("userId", uri.toString().takeLast(28))
            }
            startActivity(intent)
        }

        // 今日の日付を表示
        binding.todayText.setText(Functions.getTodayString())
        init()
    }

    @SuppressLint("WrongConstant")
    private fun init() {
        binding.kibunsList.adapter = KibunsAdapter()
        binding.kibunsList.layoutManager = LinearLayoutManager(this, VERTICAL, false)

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        db.collection("kibuns").addSnapshotListener { resultsMap, exception ->
            val results = resultsMap?.toObjects(Kibuns::class.java)
                ?.sortedByDescending { kibuns -> kibuns.date }
            val adapter = binding.kibunsList.adapter as KibunsAdapter
            if (results != null) {
                adapter.addItems(results)
            }
            adapter.setOnItemClickListener(object: KibunsAdapter.OnItemClickListener {
                override fun onClick(view: View, data: Kibuns) {
                    val intent = Intent(this@MainActivity, KibunDetailActivity::class.java).apply {
                        putExtra("text", data.text)
                        putExtra("date", binding.todayText.text)
                        putExtra("name", data.name)
                        putExtra("kibun", data.kibun)
                        putExtra("time", Functions.getTimeString(data.time))
                        putExtra("image", data.image)
                        putExtra("userId", data.user_id)
                    }
                    startActivity(intent)
                }
            })
        }

        // タブボタン処理
        binding.tabLayout.tab_button0.setImageResource(R.drawable.tab0_on)
        binding.tabLayout.tab_button0.setOnClickListener {
            val intent = Intent(this@MainActivity, MainActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button1.setOnClickListener {
            val intent = Intent(this@MainActivity, KibunInputActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button2.setOnClickListener {
            val intent = Intent(this@MainActivity, OtherActivity::class.java)
            startActivity(intent)
        }
    }
}