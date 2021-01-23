package com.sekky.kibunya.Login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityForgotPasswordBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        // 背景タップでキーボードを隠すための処理
        Functions.addBackgroundFocus(binding.background, this)

        // アドレス入力ボックスの入力監視
        binding.mailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateMailSendButton(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateMailSendButton(binding)
            }
        })

        // パスワードリセットメール送信処理
        binding.sendMail.setOnClickListener {
            auth.sendPasswordResetEmail(binding.mailInput.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.sendCompletePasswordResetMail.visibility = View.VISIBLE
                    } else {
                        Functions.showAlertOneButton(this, "エラー", Functions.getJapaneseErrorMessage(task.exception!!.message.toString()))
                    }
                }
        }

        // メールログイン画面への遷移処理
        binding.toMailLogin.setOnClickListener {
            val intent = Intent(this, EmailLoginActivity::class.java)
            startActivity(intent)
        }
    }

    // メール送信ボタンの有効/無効の切り替え
    private fun updateMailSendButton(binding: ActivityForgotPasswordBinding) {
        if (binding.mailInput.text.count() > 0) {
            binding.sendMail.isEnabled = true
            binding.sendMail.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.sendMail.isEnabled = false
            binding.sendMail.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }
}