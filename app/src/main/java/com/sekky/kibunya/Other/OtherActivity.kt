package com.sekky.kibunya.Other

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.Login.SignUpActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityOtherBinding
import kotlinx.android.synthetic.main.tab_layout.view.*

class OtherActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityOtherBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_other)

        // 家族を追加するボタンタップ

        // 家族リストをみるボタンタップ

        // プロフィール変更ボタンタップ

        // ログアウトボタンタップ
        binding.logoutButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("ログアウト")
                .setMessage("ログアウトしますか？")
                .setPositiveButton("OK") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, SignUpActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("キャンセル") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        // タブボタン処理
        binding.tabLayout.tab_button0.setImageResource(R.drawable.tab0_on)
        binding.tabLayout.tab_button0.setOnClickListener {
            val intent = Intent(this@OtherActivity, MainActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button1.setOnClickListener {
            val intent = Intent(this@OtherActivity, KibunInputActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button2.setOnClickListener {
            val intent = Intent(this@OtherActivity, OtherActivity::class.java)
            startActivity(intent)
        }
    }
}