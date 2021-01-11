package com.sekky.kibunya.Kibunlist

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout.VERTICAL
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.KibunDetail.KibunDetailActivity
import com.sekky.kibunya.Kibuns
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    }
                    startActivity(intent)
                }
            })
        }
    }
}