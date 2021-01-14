package com.sekky.kibunya.Login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivitySignUpBinding


class SignUpActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val binding: ActivitySignUpBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        // 背景タップでキーボードを隠すための処理
        Functions.addBackgroundFocus(binding.background)
        Functions.addKeyboardHide(this, binding.nameInput)
        Functions.addKeyboardHide(this, binding.mailInput)
        Functions.addKeyboardHide(this, binding.passwordInput)
        Functions.addKeyboardHide(this, binding.telInput)

        // 名前入力ボックスの入力監視
        binding.nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateMailRegistorButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateMailRegistorButtonEnable(binding)
            }
        })
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
        // 電話番号入力ボックスの入力監視
        binding.telInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTelRegistorButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateTelRegistorButtonEnable(binding)
            }
        })

        // メールアドレスで登録処理
        binding.registrationButton.setOnClickListener {

            val nameText = binding.nameInput.text.toString()
            val emailText = binding.mailInput.text.toString()
            val passText = binding.passwordInput.text.toString()

            auth.createUserWithEmailAndPassword(emailText, passText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        // 認証メール送信
                        user!!.sendEmailVerification()
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(nameText)
                            .build()
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(this, SendEmailActivity::class.java)
                                    startActivity(intent)
                                }
                            }
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

        // メールログイン画面への遷移処理
        binding.toMailLogin.setOnClickListener {
            val intent = Intent(this, EmailLoginActivity::class.java)
            startActivity(intent)
        }
    }

    // アドレスで登録ボタンの有効/無効の切り替え
    fun updateMailRegistorButtonEnable(binding: ActivitySignUpBinding) {
        if (binding.nameInput.text.count() in 1..16 && binding.mailInput.text.count() > 0 && binding.passwordInput.text.count() > 0) {
            binding.registrationButton.isClickable = true
            binding.registrationButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.registrationButton.isClickable = false
            binding.registrationButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }
    // 電話番号認証ボタンの有効/無効の切り替え
    fun updateTelRegistorButtonEnable(binding: ActivitySignUpBinding) {
        if (binding.telInput.text.count() == 11) {
            binding.sendAuthentication.isClickable = true
            binding.sendAuthentication.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.sendAuthentication.isClickable = false
            binding.sendAuthentication.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }
}