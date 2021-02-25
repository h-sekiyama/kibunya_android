package com.sekky.kibunya.Other

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nifcloud.mbaas.core.NCMBInstallation
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityViewFamilyListBinding
import kotlinx.android.synthetic.main.tab_layout.view.*
import org.json.JSONArray

class ViewFamilyListActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("WrongConstant")
    override fun onResume() {
        super.onResume()

        auth = FirebaseAuth.getInstance()

        val binding: ActivityViewFamilyListBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_view_family_list)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val familyRef = db.collection("families")
        val containsMyUidDocument = familyRef.whereArrayContains("user_id", user!!.uid)
        var familyId = ""
        var familyList: List<String> = listOf<String>()

        containsMyUidDocument.get().addOnSuccessListener { resultMap ->
            if (resultMap.documents.size == 0) {    // 家族が1人もいない
                binding.noFamilyLabel.visibility = View.VISIBLE
                binding.runawayButton.visibility = View.GONE
            } else {
                familyId = resultMap.documents[0].id
                familyList = resultMap.documents[0].get("user_id") as List<String>
                binding.familyList.adapter = FamilyAdapter()
                binding.familyList.layoutManager = LinearLayoutManager(this, VERTICAL, false)

                val adapter = binding.familyList.adapter as FamilyAdapter
                adapter.addItems(familyList)
            }
        }

        // 家出ボタン処理
        binding.runawayButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("家出しますか？")
                .setMessage("家出すると家族が1人もいなくなります")
                .setPositiveButton("家出する") { _, _ ->
                    familyRef.document(familyId).update("user_id", familyList.filter { it != user.uid }).addOnSuccessListener {
                        binding.familyList.adapter = FamilyAdapter()
                        binding.runawayButton.visibility = View.GONE
                        binding.noFamilyLabel.visibility = View.VISIBLE
                    }

                    // ニフクラから家族情報を削除
                    val installation = NCMBInstallation.getCurrentInstallation()
                    installation.channels = JSONArray("[]")
                    installation.saveInBackground()
                }
                .setNegativeButton("しない") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        // タブボタン処理
        binding.tabLayout.tab_button2.setImageResource(R.drawable.tab2_on)
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