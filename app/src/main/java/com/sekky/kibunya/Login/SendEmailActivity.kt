package com.sekky.kibunya.Login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivitySendEmailBinding

class SendEmailActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivitySendEmailBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_send_email)

        // ログイン画面に遷移
        binding.toMailLogin.setOnClickListener {
            val intent = Intent(this, EmailLoginActivity::class.java)
            startActivity(intent)
        }

        // メールログイン画面への遷移処理
        binding.toMailLogin.setOnClickListener {
            val intent = Intent(this, EmailLoginActivity::class.java)
            startActivity(intent)
        }
    }
}