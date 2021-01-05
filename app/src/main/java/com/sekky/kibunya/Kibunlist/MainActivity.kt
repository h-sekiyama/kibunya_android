package com.sekky.kibunya.Kibunlist

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout.VERTICAL
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.sekky.kibunya.Kibuns
import com.sekky.kibunya.R
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
                ?.sortedByDescending { kibuns -> kibuns.date }
            val adapter = binding.kibunsList.adapter as KibunsAdapter
            if (results != null) {
                adapter.addItems(results)
            }
            adapter.setOnItemClickListener(object: KibunsAdapter.OnItemClickListener {
                override fun onClick(view: View, item: Kibuns) {
                    Toast.makeText(applicationContext, item.name, Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}