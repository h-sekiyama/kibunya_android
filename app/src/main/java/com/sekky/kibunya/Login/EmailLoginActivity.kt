package com.sekky.kibunya.Login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityEmailLoginBinding
import com.sekky.kibunya.databinding.ActivitySignUpBinding

class EmailLoginActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val binding: ActivityEmailLoginBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_email_login)

        // 背景タップでキーボードを隠すための処理
        Functions.addBackgroundFocus(binding.background)
        Functions.addKeyboardHide(this, binding.mailInput)
        Functions.addKeyboardHide(this, binding.passwordInput)

        // アドレス入力ボックスの入力監視
        binding.mailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateMailRegistorButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateMailRegistorButtonEnable(binding)
            }
        })
        // パスワード入力ボックスの入力監視
        binding.passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateMailRegistorButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateMailRegistorButtonEnable(binding)
            }
        })
        // ログインボタンタップ
        binding.loginButton.setOnClickListener {
            val emailText = binding.mailInput.text.toString()
            val passText = binding.passwordInput.text.toString()

            val user = auth.currentUser
            user!!.reload()
            auth.signInWithEmailAndPassword(emailText, passText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful && user.isEmailVerified) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("エラー")
                            .setMessage(Functions.getJapaneseErrorMessage(task.exception!!.message.toString()))
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
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
    fun updateMailRegistorButtonEnable(binding: ActivityEmailLoginBinding) {
        if (binding.mailInput.text.count() > 0 && binding.passwordInput.text.count() > 0) {
            binding.loginButton.isClickable = true
            binding.loginButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.loginButton.isClickable = false
            binding.loginButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }
}