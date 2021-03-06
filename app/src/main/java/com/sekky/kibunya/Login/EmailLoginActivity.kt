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
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityEmailLoginBinding

class EmailLoginActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val binding: ActivityEmailLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_email_login)

        // 背景タップでキーボードを隠すための処理
        Functions.addBackgroundFocus(binding.background, this)

        // アドレス入力ボックスの入力監視
        binding.mailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateLoginButtonEnable(binding)
            }
        })
        // パスワード入力ボックスの入力監視
        binding.passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateLoginButtonEnable(binding)
            }
        })
        // ログインボタンタップ
        binding.loginButton.setOnClickListener {
            // プログレスバー表示
            binding.overlay.visibility = View.VISIBLE
            binding.progressbar.visibility = View.VISIBLE

            val emailText = binding.mailInput.text.toString()
            val passText = binding.passwordInput.text.toString()

            auth.signInWithEmailAndPassword(emailText, passText)
                .addOnCompleteListener(this) { task ->
                    // プログレスバー非表示
                    binding.overlay.visibility = View.GONE
                    binding.progressbar.visibility = View.GONE

                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user!!.reload()
                        if (user.isEmailVerified) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Functions.showAlertOneButton(this, "エラー", "メール認証が完了していません")
                        }
                    } else {
                        Functions.showAlertOneButton(this, "エラー", Functions.getJapaneseErrorMessage(task.exception!!.message.toString()))
                    }
                }
        }
        // パスワード忘れ画面へ遷移
        binding.toPasswordForgot.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        // 新規登録画面へ遷移
        binding.toRegistration.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    // ログインボタンの有効/無効の切り替え
    private fun updateLoginButtonEnable(binding: ActivityEmailLoginBinding) {
        if (binding.mailInput.text.count() > 0 && binding.passwordInput.text.count() > 0) {
            binding.loginButton.isEnabled = true
            binding.loginButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.loginButton.isEnabled = false
            binding.loginButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }
}