package com.sekky.kibunya

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout.VERTICAL
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.sekky.kibunya.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    @SuppressLint("WrongConstant")
    private fun init() {
        binding.kibunsList.adapter = KibunsAdapter()
        binding.kibunsList.layoutManager = LinearLayoutManager(this, VERTICAL, false)

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        db.collection("kibuns").addSnapshotListener { resultsMap, exception ->
            val results = resultsMap?.toObjects(Kibuns::class.java)
                ?.sortedBy { kibuns -> kibuns.date }
            val adapter = binding.kibunsList.adapter as KibunsAdapter
            if (results != null) {
                adapter.addItems(results)
            }
        }
    }
}